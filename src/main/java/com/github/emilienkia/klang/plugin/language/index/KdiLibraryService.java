package com.github.emilienkia.klang.plugin.language.index;

import com.github.emilienkia.kdi.Kdi;
import com.github.emilienkia.kdi.query.KdiSymbol;
import com.github.emilienkia.kdi.query.KdiSymbolIndex;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>KDI library support — Foundation.</b>
 *
 * <p>Project-level service that loads, caches and indexes the configured set of
 * {@code .kdi} (K Description Interface) files and directories. Each {@code .kdi} file
 * exposes the declarations of one K module without requiring its full source.</p>
 *
 * <p>Callers obtain the service via {@link #getInstance(Project)}, query available modules
 * with {@link #getKdiModuleNames()} / {@link #isKdiModule(String)}, and perform symbol
 * lookups through {@link #getIndexForModule(String)} or {@link #findByFqName(String)}.</p>
 *
 * <p>The loaded {@link Kdi} instances are cached per absolute path, keyed on the file's
 * last-modified timestamp, so they are only re-parsed when a file changes on disk.
 * The derived {@link ModuleIndex} (symbol index by module name + simple name) is rebuilt
 * lazily from the cache whenever it is invalidated.</p>
 *
 * <p>Paths are persisted in the project file via {@link PersistentStateComponent};
 * the settings UI is {@code KdiLibraryConfigurable} (Settings → K-lang → KDI Libraries).</p>
 */
@Service(Service.Level.PROJECT)
@State(name = "KdiLibraryService", storages = @Storage("klang-libraries.xml"))
public final class KdiLibraryService implements PersistentStateComponent<KdiLibraryService.State> {

    private static final Logger LOG = Logger.getInstance(KdiLibraryService.class);

    // ── Persistent state ────────────────────────────────────────────────────────

    /**
     * Serialised state: the list of configured {@code .kdi} file / directory paths.
     * XML-friendly: paths are plain strings, one element per list entry.
     */
    public static final class State {
        /** Configured {@code .kdi} file or directory paths (absolute or project-relative). */
        public List<String> paths = new ArrayList<>();

        /** Shallow copy constructor used to detect modifications. */
        public State copy() {
            State s = new State();
            s.paths = new ArrayList<>(paths);
            return s;
        }
    }

    // ── Instance state ──────────────────────────────────────────────────────────

    private volatile @NotNull State state = new State();

    /** Per-path cache: path (absolute) → (last-modified, parsed Kdi). */
    private final Map<String, CachedEntry> fileCache = new ConcurrentHashMap<>();

    /** Derived module index — rebuilt lazily when invalidated. */
    private volatile @Nullable ModuleIndex moduleIndex = null;

    // ── Constructor / factory ───────────────────────────────────────────────────

    @SuppressWarnings("unused") // instantiated by the platform service container
    public KdiLibraryService() {}

    public static @NotNull KdiLibraryService getInstance(@NotNull Project project) {
        return project.getService(KdiLibraryService.class);
    }

    // ── PersistentStateComponent ────────────────────────────────────────────────

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State loaded) {
        this.state = loaded;
        invalidate();
    }

    // ── Path management ─────────────────────────────────────────────────────────

    /** Read-only view of the currently configured paths. */
    public @NotNull List<String> getPaths() {
        return Collections.unmodifiableList(state.paths);
    }

    /**
     * Replace the configured path list and invalidate the cached index.
     * Called by the settings configurable on {@code apply()}.
     */
    public void setPaths(@NotNull List<String> paths) {
        state.paths = new ArrayList<>(paths);
        invalidate();
    }

    // ── Module queries ──────────────────────────────────────────────────────────

    /** Whether {@code moduleName} is backed by a configured {@code .kdi} file. */
    public boolean isKdiModule(@NotNull String moduleName) {
        return getModuleIndex().indexByModule.containsKey(moduleName);
    }

    /** All module names that have a corresponding {@code .kdi} index entry. */
    public @NotNull Set<String> getKdiModuleNames() {
        return Collections.unmodifiableSet(getModuleIndex().indexByModule.keySet());
    }

    /**
     * The full symbol index for {@code moduleName}, or {@code null} when not KDI-backed.
     * Use {@link KdiSymbolIndex#findByFqName(String)} / {@link KdiSymbolIndex#childrenOf(String)}
     * for resolution; use {@link KdiSymbolIndex#all()} for completion.
     */
    public @Nullable KdiSymbolIndex getIndexForModule(@NotNull String moduleName) {
        return getModuleIndex().indexByModule.get(moduleName);
    }

    /**
     * Names of modules that export at least one symbol with the given {@code simpleName}.
     * Used by the auto-import quick fix to suggest {@code import M;} candidates.
     */
    public @NotNull Set<String> getModulesForSimpleName(@NotNull String simpleName) {
        return getModuleIndex().modulesBySimpleName.getOrDefault(simpleName, Collections.emptySet());
    }

    /**
     * Find all KDI symbols across all loaded modules whose fully-qualified name matches
     * {@code fqName}. Overloaded functions return multiple results.
     */
    public @NotNull List<KdiSymbol> findByFqName(@NotNull String fqName) {
        List<KdiSymbol> result = new ArrayList<>();
        for (KdiSymbolIndex idx : getModuleIndex().indexByModule.values()) {
            result.addAll(idx.findByFqName(fqName));
        }
        return result;
    }

    // ── Cache management ────────────────────────────────────────────────────────

    /**
     * Invalidate the in-memory cache and derived index, forcing a full reload on the
     * next query. Called after path list changes or externally when a {@code .kdi} file
     * on disk is known to have changed.
     */
    public void invalidate() {
        fileCache.clear();
        moduleIndex = null;
    }

    // ── Internal ─────────────────────────────────────────────────────────────────

    private @NotNull ModuleIndex getModuleIndex() {
        ModuleIndex idx = moduleIndex;
        if (idx == null) {
            synchronized (this) {
                idx = moduleIndex;
                if (idx == null) {
                    idx = buildModuleIndex();
                    moduleIndex = idx;
                }
            }
        }
        return idx;
    }

    private @NotNull ModuleIndex buildModuleIndex() {
        Map<String, KdiSymbolIndex> byModule = new HashMap<>();
        Map<String, Set<String>> bySimpleName = new HashMap<>();

        for (String pathStr : state.paths) {
            try {
                Path path = Path.of(pathStr);
                if (!Files.exists(path)) {
                    LOG.debug("KDI path does not exist, skipping: " + pathStr);
                    continue;
                }
                if (Files.isDirectory(path)) {
                    loadDirectory(path, byModule, bySimpleName);
                } else if (isKdiFile(path)) {
                    loadKdiFile(path, byModule, bySimpleName);
                } else {
                    LOG.debug("KDI path is not a .kdi file or directory, skipping: " + pathStr);
                }
            } catch (Exception e) {
                LOG.warn("Failed to load KDI from path: " + pathStr, e);
            }
        }

        return new ModuleIndex(
                Collections.unmodifiableMap(byModule),
                Collections.unmodifiableMap(bySimpleName));
    }

    private void loadDirectory(@NotNull Path dir,
                               @NotNull Map<String, KdiSymbolIndex> byModule,
                               @NotNull Map<String, Set<String>> bySimpleName) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.kdi")) {
            for (Path file : stream) {
                try {
                    loadKdiFile(file, byModule, bySimpleName);
                } catch (Exception e) {
                    LOG.warn("Failed to load KDI file: " + file, e);
                }
            }
        }
    }

    private void loadKdiFile(@NotNull Path path,
                             @NotNull Map<String, KdiSymbolIndex> byModule,
                             @NotNull Map<String, Set<String>> bySimpleName) {
        String absPath = path.toAbsolutePath().toString().replace('\\', '/');

        // Determine the file's current modification stamp.
        long stamp = stampOf(absPath);

        // Check the per-file cache.
        CachedEntry cached = fileCache.get(absPath);
        Kdi kdi;
        if (cached != null && cached.stamp == stamp) {
            kdi = cached.kdi;
        } else {
            try {
                kdi = Kdi.read(path);
            } catch (Exception e) {
                LOG.warn("Cannot parse KDI file: " + path, e);
                return;
            }
            fileCache.put(absPath, new CachedEntry(stamp, kdi));
        }

        String moduleName = kdi.moduleName();
        if (moduleName == null || moduleName.isBlank()) {
            LOG.debug("KDI file has no module name, skipping: " + path);
            return;
        }

        KdiSymbolIndex index = kdi.index();
        byModule.put(moduleName, index);

        // Index every symbol's simple name for auto-import suggestions.
        for (KdiSymbol sym : index.all()) {
            String name = sym.getName();
            if (name != null && !name.isBlank()) {
                bySimpleName.computeIfAbsent(name, k -> new HashSet<>()).add(moduleName);
            }
        }
    }

    /** Modification stamp of a path — VirtualFile stamp when available, file-system mtime otherwise. */
    private static long stampOf(@NotNull String absPath) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(absPath);
        return vf != null ? vf.getModificationStamp() : Path.of(absPath).toFile().lastModified();
    }

    private static boolean isKdiFile(@NotNull Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".kdi") || name.endsWith(".kdi.json");
    }

    // ── Internal records ─────────────────────────────────────────────────────────

    private record CachedEntry(long stamp, @NotNull Kdi kdi) {}

    private record ModuleIndex(
            @NotNull Map<String, KdiSymbolIndex> indexByModule,
            @NotNull Map<String, Set<String>> modulesBySimpleName) {}
}

