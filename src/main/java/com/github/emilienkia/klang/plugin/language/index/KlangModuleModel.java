package com.github.emilienkia.klang.plugin.language.index;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * <b>Multi-file indexing — directory-aware module model.</b>
 *
 * <p>Project-level service that turns the raw {@link KlangModuleNameIndex} into the K-lang
 * <em>module model</em>: it computes, for every {@code .k} file, the module it effectively belongs
 * to, using a <b>per-directory decision with ancestor inheritance</b>. The computed snapshot is
 * cached and refreshed on PSI modification (the index itself is the durable cache underneath).</p>
 *
 * <p>Per-directory rule (a directory's own {@code .k} files):</p>
 * <ul>
 *   <li>If <b>some file declares no module</b>, all declared files must agree on a <b>single</b>
 *       module {@code M} → {@code M} applies to the whole directory (this subsumes the classic
 *       "one file declares for all" / policy C). Disagreement is a {@link ProblemKind#MIXED_DIRECTORY}.</li>
 *   <li>If <b>every file declares a module</b>, the directory may host <b>several</b> modules
 *       (policy A): each file keeps its own.</li>
 *   <li>If <b>no file declares a module</b>, the directory <b>inherits</b> from the nearest
 *       ancestor that declares exactly one module (propagation downward); several declared modules
 *       in that ancestor is a {@link ProblemKind#AMBIGUOUS_INHERITANCE}. Reaching the root with
 *       nothing yields the single global <b>anonymous</b> module (policy B).</li>
 * </ul>
 *
 * <p>The auto-imported standard library (modules whose root namespace is {@code k}) is a
 * <em>library</em>, not part of the project's module shape: its files stay indexed (so {@code k::…}
 * resolves) but are <b>excluded</b> from the directory layout and from the policy summary.</p>
 *
 * <p>See {@code docs/klang/multifile-indexing-plan.md} for the full design (incl. the import-
 * resolution implications).</p>
 */
@Service(Service.Level.PROJECT)
public final class KlangModuleModel {

    /** Root namespace of the auto-imported standard library; excluded from policy detection. */
    private static final String LIBK_ROOT = "k";

    /** The anonymous-module sentinel (empty name). */
    private static final String ANONYMOUS = KlangModuleNameIndex.ANONYMOUS;

    /** Sentinel returned by the inheritance climb when an ancestor declares several modules. */
    private static final String AMBIGUOUS = "\u0000ambiguous";

    /**
     * Coarse, project-wide summary of the directory-aware module layout (the real grouping is
     * per file via {@link #moduleNameOf}). Kept for diagnostics / back-compat.
     */
    public enum Policy {
        /** No user file declares a module — one anonymous root. */
        ANONYMOUS_SINGLE,
        /** One user module, applied to at least one undeclared (inherited) file. */
        SINGLE_DECLARES_ALL,
        /** Several user modules, every file declaring its own. */
        PER_FILE_MODULES,
        /** A module-layout error was detected (mixed directory / ambiguous inheritance). */
        UNSUPPORTED
    }

    /** Kind of module-layout problem surfaced by {@link #problemFor}. */
    public enum ProblemKind {
        /** A directory mixes a module-less file with several distinct declared modules. */
        MIXED_DIRECTORY,
        /** A module-less directory inherits from an ancestor that declares several modules. */
        AMBIGUOUS_INHERITANCE
    }

    /** A module-layout problem attached to a specific file (best-effort resolution still applies). */
    public record LayoutProblem(@NotNull ProblemKind kind, @NotNull String message) {}

    private final @NotNull Project project;

    @SuppressWarnings("unused") // instantiated by the platform service container
    public KlangModuleModel(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull KlangModuleModel getInstance(@NotNull Project project) {
        return project.getService(KlangModuleModel.class);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** The project's detected module policy. */
    public @NotNull Policy policy() {
        return data().policy;
    }

    /** Every distinct module name declared in the project (never contains the anonymous sentinel). */
    public @NotNull Set<String> allModuleNames() {
        return Collections.unmodifiableSet(data().literalByModule.keySet());
    }

    /**
     * The files that <em>literally declare</em> {@code moduleName} (a {@code module …;} line).
     * Used to navigate an {@code import} target to its {@code module} declaration and as the
     * entry point of cross-module resolution (the chosen representative then merges the module's
     * whole effective root). Returns an empty set for an unknown module.
     */
    public @NotNull Set<VirtualFile> filesDeclaringModule(@NotNull String moduleName) {
        return new HashSet<>(data().literalByModule.getOrDefault(moduleName, Collections.emptySet()));
    }

    /**
     * The effective module name of {@code file} after applying the directory-aware module layout
     * (per-directory decision + ancestor inheritance) — the empty string
     * ({@link KlangModuleNameIndex#ANONYMOUS}) for the anonymous module.
     */
    public @NotNull String moduleNameOf(@NotNull KlangFile file) {
        VirtualFile vf = file.getVirtualFile();
        return vf != null ? data().effectiveOf.getOrDefault(vf, ANONYMOUS) : ANONYMOUS;
    }

    /** The set of files that form {@code file}'s module root (always includes {@code file}). */
    public @NotNull Set<VirtualFile> filesInModuleOf(@NotNull KlangFile file) {
        Snapshot snap = data();
        VirtualFile vf = file.getVirtualFile();
        String eff = vf != null ? snap.effectiveOf.getOrDefault(vf, ANONYMOUS) : ANONYMOUS;
        Set<VirtualFile> result = new HashSet<>(snap.effectiveByModule.getOrDefault(eff, Collections.emptySet()));
        if (vf != null) result.add(vf); // the anchor file is always part of its own root
        return result;
    }

    /** A search scope spanning {@code file}'s module root. */
    public @NotNull GlobalSearchScope moduleScope(@NotNull KlangFile file) {
        return GlobalSearchScope.filesScope(project, filesInModuleOf(file));
    }

    /**
     * The module-layout problem affecting {@code vf} (a mixed directory or an ambiguous inherited
     * module), or {@code null} when the file's module is unambiguous. Resolution still degrades
     * gracefully regardless; this is for diagnostics (inspections / annotators).
     */
    public @Nullable LayoutProblem problemFor(@NotNull VirtualFile vf) {
        return data().problems.get(vf);
    }

    // ── Snapshot computation (cached) ─────────────────────────────────────────

    private @NotNull Snapshot data() {
        return CachedValuesManager.getManager(project).getCachedValue(project, () ->
                CachedValueProvider.Result.create(compute(), PsiModificationTracker.MODIFICATION_COUNT));
    }

    private @NotNull Snapshot compute() {
        return ReadAction.compute(() -> {
            FileBasedIndex index = FileBasedIndex.getInstance();
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);

            // file -> declared module name (null = anonymous), plus the literal declarers per module.
            Map<VirtualFile, String> declaredOf = new HashMap<>();
            Map<String, Set<VirtualFile>> literalByModule = new HashMap<>();
            for (String key : index.getAllKeys(KlangModuleNameIndex.NAME, project)) {
                Collection<VirtualFile> files = index.getContainingFiles(KlangModuleNameIndex.NAME, key, scope);
                if (files.isEmpty()) continue; // stale key
                boolean anon = ANONYMOUS.equals(key);
                for (VirtualFile f : files) {
                    declaredOf.put(f, anon ? null : key);
                    if (!anon) literalByModule.computeIfAbsent(key, k -> new HashSet<>()).add(f);
                }
            }

            // Per-directory layout over the *user* files (libraries are excluded here).
            Map<VirtualFile, DirInfo> dirInfo = new HashMap<>();
            for (Map.Entry<VirtualFile, String> e : declaredOf.entrySet()) {
                String name = e.getValue();
                if (name != null && isLibrary(name)) continue;
                VirtualFile dir = e.getKey().getParent();
                if (dir == null) continue;
                DirInfo di = dirInfo.computeIfAbsent(dir, d -> new DirInfo());
                if (name == null) di.hasAnon = true; else di.declared.add(name);
            }

            // Effective module per file (directory decision + ancestor inheritance).
            Map<VirtualFile, String> effectiveOf = new HashMap<>();
            Map<VirtualFile, LayoutProblem> problems = new HashMap<>();
            Map<VirtualFile, String> climbMemo = new HashMap<>();
            for (Map.Entry<VirtualFile, String> e : declaredOf.entrySet()) {
                VirtualFile f = e.getKey();
                String name = e.getValue();
                if (name != null && isLibrary(name)) {
                    effectiveOf.put(f, name); // a library file keeps its own module
                } else {
                    effectiveOf.put(f, effectiveModuleOf(f, name, dirInfo, climbMemo, problems));
                }
            }

            // Group files by effective module (the ANONYMOUS key gathers all anonymous files).
            Map<String, Set<VirtualFile>> effectiveByModule = new HashMap<>();
            for (Map.Entry<VirtualFile, String> e : effectiveOf.entrySet()) {
                effectiveByModule.computeIfAbsent(e.getValue(), k -> new HashSet<>()).add(e.getKey());
            }

            Policy policy = summarize(effectiveByModule, declaredOf, effectiveOf, !problems.isEmpty(), this::isLibrary);
            return new Snapshot(policy, literalByModule, effectiveByModule, effectiveOf, problems);
        });
    }

    /**
     * The effective module of {@code f} (declaring {@code ownName}, or {@code null} if anonymous),
     * applying the per-directory decision and, for a module-less directory, the ancestor climb.
     */
    private static @NotNull String effectiveModuleOf(@NotNull VirtualFile f,
                                                     @Nullable String ownName,
                                                     @NotNull Map<VirtualFile, DirInfo> dirInfo,
                                                     @NotNull Map<VirtualFile, String> climbMemo,
                                                     @NotNull Map<VirtualFile, LayoutProblem> problems) {
        VirtualFile dir = f.getParent();
        DirInfo di = dir != null ? dirInfo.get(dir) : null;

        if (di != null && !di.declared.isEmpty()) {
            if (di.hasAnon) {
                // A module-less file is present: the declared files must agree on one module.
                if (di.declared.size() == 1) return di.declared.iterator().next();
                problems.put(f, new LayoutProblem(ProblemKind.MIXED_DIRECTORY,
                        "Directory mixes a module-less file with several declared modules " + di.declared
                                + "; files without a 'module' require a single common module."));
                return ownName != null ? ownName : ANONYMOUS; // best-effort
            }
            // Every file in the directory declares a module → multi-module directory is allowed.
            return ownName != null ? ownName : ANONYMOUS;
        }

        // No module declared in this directory → inherit from the nearest ancestor that declares one.
        String inherited = climb(dir != null ? dir.getParent() : null, dirInfo, climbMemo);
        if (AMBIGUOUS.equals(inherited)) {
            problems.put(f, new LayoutProblem(ProblemKind.AMBIGUOUS_INHERITANCE,
                    "Module is ambiguous: an ancestor directory declares several modules; "
                            + "declare a 'module' in this directory to disambiguate."));
            return ANONYMOUS; // best-effort
        }
        return inherited;
    }

    /** Climb ancestors from {@code dir}; the first that declares module(s) decides (1 → it; ≥2 → AMBIGUOUS). */
    private static @NotNull String climb(@Nullable VirtualFile dir,
                                         @NotNull Map<VirtualFile, DirInfo> dirInfo,
                                         @NotNull Map<VirtualFile, String> memo) {
        if (dir == null) return ANONYMOUS;
        String cached = memo.get(dir);
        if (cached != null) return cached;
        DirInfo di = dirInfo.get(dir);
        String res;
        if (di != null && !di.declared.isEmpty()) {
            res = di.declared.size() == 1 ? di.declared.iterator().next() : AMBIGUOUS;
        } else {
            res = climb(dir.getParent(), dirInfo, memo);
        }
        memo.put(dir, res);
        return res;
    }

    /** Coarse global policy summary derived from the per-file effective grouping (diagnostics). */
    private static @NotNull Policy summarize(@NotNull Map<String, Set<VirtualFile>> effectiveByModule,
                                             @NotNull Map<VirtualFile, String> declaredOf,
                                             @NotNull Map<VirtualFile, String> effectiveOf,
                                             boolean anyProblem,
                                             @NotNull Predicate<String> isLibrary) {
        if (anyProblem) return Policy.UNSUPPORTED;
        Set<String> userNames = new HashSet<>();
        for (String n : effectiveByModule.keySet()) {
            if (!ANONYMOUS.equals(n) && !isLibrary.test(n)) userNames.add(n);
        }
        if (userNames.isEmpty()) return Policy.ANONYMOUS_SINGLE;
        boolean inheritedUndeclared = false;
        for (Map.Entry<VirtualFile, String> e : declaredOf.entrySet()) {
            if (e.getValue() == null) {
                String eff = effectiveOf.get(e.getKey());
                if (eff != null && !ANONYMOUS.equals(eff)) { inheritedUndeclared = true; break; }
            }
        }
        if (userNames.size() == 1 && inheritedUndeclared) return Policy.SINGLE_DECLARES_ALL;
        return Policy.PER_FILE_MODULES;
    }

    /** Whether {@code moduleName} belongs to the standard library (root namespace {@code k}). */
    private static boolean isLibk(@NotNull String moduleName) {
        return moduleName.equals(LIBK_ROOT) || moduleName.startsWith(LIBK_ROOT + "::");
    }

    /**
     * Whether {@code moduleName} is a <em>library</em>: either the built-in standard library
     * ({@code k} / {@code k::…}) or a KDI-backed module configured in
     * {@link KdiLibraryService}. Library modules are excluded from the project's A/B/C policy
     * detection and from the anonymous root, but remain indexed for auto-import and resolution.
     */
    private boolean isLibrary(@NotNull String moduleName) {
        return isLibk(moduleName) || KdiLibraryService.getInstance(project).isKdiModule(moduleName);
    }

    /** Mutable per-directory accumulator used during {@link #compute()}. */
    private static final class DirInfo {
        /** Distinct declared (non-libk) module names in this directory; sorted → deterministic. */
        final Set<String> declared = new TreeSet<>();
        /** Whether some file in this directory declares no module. */
        boolean hasAnon = false;
    }

    /** Immutable snapshot of the directory-aware module model for a given PSI modification count. */
    private record Snapshot(@NotNull Policy policy,
                            @NotNull Map<String, Set<VirtualFile>> literalByModule,
                            @NotNull Map<String, Set<VirtualFile>> effectiveByModule,
                            @NotNull Map<VirtualFile, String> effectiveOf,
                            @NotNull Map<VirtualFile, LayoutProblem> problems) {
    }
}

