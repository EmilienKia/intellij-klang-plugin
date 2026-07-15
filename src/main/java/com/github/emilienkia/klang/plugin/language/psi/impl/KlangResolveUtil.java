package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.github.emilienkia.klang.plugin.language.index.KlangBaseNameIndex;
import com.github.emilienkia.klang.plugin.language.index.KlangModuleModel;
import com.github.emilienkia.klang.plugin.language.index.KlangModuleScope;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Implements the K-lang symbol resolution algorithm as specified in
 * {@code docs/name-resolution.md}.
 *
 * <h3>Symbol classification</h3>
 * <ul>
 *   <li><b>Simple</b>: single identifier, not prefixed by {@code ::}.</li>
 *   <li><b>Absolute qualified</b>: prefixed by {@code ::} (regardless of segment count).</li>
 *   <li><b>Relative qualified</b>: multiple identifiers separated by {@code ::}, no leading {@code ::}.</li>
 * </ul>
 *
 * <h3>Resolution algorithm (§5)</h3>
 * <ol>
 *   <li>{@code this} → find nearest non-static member function's owner aggregate.</li>
 *   <li>Absolute (leading {@code ::}) → ROOT_LOOKUP from module root.</li>
 *   <li>Qualified (has {@code ::} but no leading {@code ::}) → QUALIFIED_LOOKUP with ascending + descending.</li>
 *   <li>Simple → SIMPLE_LOOKUP + USING_LOOKUP + upward scope climb.</li>
 * </ol>
 *
 * <h3>Additional special forms (§11)</h3>
 * <ul>
 *   <li>Enum entries: {@code Enum::entry}</li>
 *   <li>Union kind entries: {@code Union::Kind::entry}</li>
 *   <li>Annotation RTTI: {@code Annotation::annotation}</li>
 * </ul>
 */
public final class KlangResolveUtil {

    private KlangResolveUtil() {}

    /**
     * Root namespace of the K standard library ({@code k}). The {@code k} module is the only
     * module that is <em>auto-imported</em>: names targeting the {@code k} namespace resolve
     * (in qualified / absolute form) without an explicit {@code import}. Every other module is
     * airtight and requires an explicit {@code import} (§5.8).
     */
    private static final String LIBK_ROOT = "k";

    // ── Entry point ───────────────────────────────────────────────────────────

    /**
     * Resolve a reference text from the given anchor element.
     *
     * @param anchor  the PSI element at the usage site (typically the {@code identifierExpr} node)
     * @param refText the full text of the reference, e.g. {@code "k::io::print"},
     *                {@code "::Point"}, {@code "sum"}, {@code "this"}
     * @return the list of matching declarations (may be empty if unresolved,
     *         or contain multiple elements for overloads)
     */
    public static @NotNull List<PsiElement> resolve(@NotNull PsiElement anchor,
                                                    @NotNull String refText) {
        String text = refText.trim();
        if (text.isEmpty()) return Collections.emptyList();

        // §5.2 — 'this' keyword resolution
        if ("this".equals(text)) {
            return resolveThis(anchor);
        }

        // Drop template argument lists so a templated type/scope name resolves to its
        // declaration: "Vector<int>" → "Vector", "Map<K,V>::iterator" → "Map::iterator".
        text = stripTemplateArgs(text);
        if (text.isEmpty()) return Collections.emptyList();

        boolean absolute = text.startsWith("::");
        String  stripped = absolute ? text.substring(2) : text;
        // Split on "::" but only non-empty segments
        String[] segments = Arrays.stream(stripped.split("::"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (segments.length == 0) return Collections.emptyList();

        if (absolute) {
            // §5.3 / §6 — Absolute qualified: resolve from module root
            List<PsiElement> result = resolveAbsolute(anchor, segments);
            return result.isEmpty() ? resolveExternal(anchor, segments) : result;
        } else if (segments.length == 1) {
            // §5.5 — Simple symbol: ascending resolution with using + climb
            List<PsiElement> result = resolveSimple(anchor, segments[0]);
            if (result.isEmpty()) result = resolveInAnnotationArgument(anchor, segments);
            return result.isEmpty() ? resolveExternal(anchor, segments) : result;
        } else {
            // §5.4 — Relative qualified: ascending for first segment, then descending
            // Also handles special forms (§11)
            List<PsiElement> result = resolveQualified(anchor, segments);
            if (result.isEmpty()) result = resolveInAnnotationArgument(anchor, segments);
            return result.isEmpty() ? resolveExternal(anchor, segments) : result;
        }
    }

    // ── Annotation argument scope (§13 — inner enums) ─────────────────────────

    /**
     * Resolves a <em>relative</em> name that appears inside an annotation application's
     * arguments ({@code @Ann(args…)} / {@code @Ann{…}}) against the <b>applied annotation
     * type's own scope</b>.
     *
     * <p>Per the annotations spec (§13, <i>Inner enums</i>), an annotation's inner enum is
     * referenced from an application site by its short name — {@code @Severity(Level::HIGH)}
     * denotes {@code Severity::Level::HIGH}. Such a name is <em>not</em> in the lexical scope
     * chain of the application site (the inner enum lives in the annotation type body, which
     * is a sibling — not an ancestor — of the {@code @Ann(...)} prefix), so the standard
     * §5 resolution misses it. This fallback descends into the applied annotation type to
     * resolve the inner enum / enum entry (e.g. {@code Policy::RUNTIME} → {@code
     * Retention::Policy::RUNTIME}).</p>
     *
     * <p>Returns an empty list when the anchor is not inside an annotation argument, or when
     * the applied annotation type cannot be resolved.</p>
     */
    private static @NotNull List<PsiElement> resolveInAnnotationArgument(@NotNull PsiElement anchor,
                                                                         @NotNull String[] segments) {
        KlangAggregateDecl annType = appliedAnnotationTypeOfArgument(anchor);
        if (annType == null) return Collections.emptyList();
        List<PsiElement> results = new ArrayList<>();
        downLookup(annType, segments, 0, results, new HashSet<>());
        return results;
    }

    /**
     * If {@code anchor} lies within the <em>argument</em> part of an annotation application
     * ({@code @Ann(args…)} / {@code @Ann{…}}) — i.e. not within the {@code @Ann} type name
     * itself — returns the resolved applied annotation type, else {@code null}.
     */
    private static @Nullable KlangAggregateDecl appliedAnnotationTypeOfArgument(@NotNull PsiElement anchor) {
        KlangAnnotationDef def = PsiTreeUtil.getParentOfType(anchor, KlangAnnotationDef.class);
        if (def == null) return null;
        // Names inside the @Type identifier itself are resolved normally, not in this scope.
        KlangQualifiedIdentifier typeName = def.getQualifiedIdentifier();
        if (PsiTreeUtil.isAncestor(typeName, anchor, false)) return null;

        for (PsiElement el : resolve(def, typeName.getText().trim())) {
            if (el instanceof KlangAggregateDecl agg && isAnnotationType(agg)) {
                return agg;
            }
        }
        return null;
    }

    // ── §5.2 — 'this' keyword resolution ──────────────────────────────────────

    /**
     * Resolves the {@code this} keyword to the enclosing aggregate declaration
     * (the owner of the nearest non-static member function).
     */
    private static @NotNull List<PsiElement> resolveThis(@NotNull PsiElement anchor) {
        PsiElement current = anchor;
        while (current != null) {
            if (current instanceof KlangFunctionDecl func) {
                // Check if this function is a member function (inside an aggregate)
                PsiElement parent = func.getParent();
                // The function is wrapped in a KlangDeclaration, which itself is inside the aggregate
                if (parent instanceof KlangDeclaration) {
                    parent = parent.getParent();
                }
                if (parent instanceof KlangAggregateDecl agg) {
                    // Check it's not static
                    if (!hasStaticSpecifier(func)) {
                        return Collections.singletonList(agg);
                    }
                }
                // If it's not a member function, 'this' is invalid here
                return Collections.emptyList();
            }
            current = current.getParent();
        }
        return Collections.emptyList();
    }

    /** Check if a function has the 'static' specifier. */
    private static boolean hasStaticSpecifier(@NotNull KlangFunctionDecl func) {
        for (KlangSpecifier spec : func.getSpecifierList()) {
            if ("static".equals(spec.getText())) return true;
        }
        return false;
    }

    // ── §5.5 — Simple resolution (ascending with using + climb) ───────────────

    /**
     * Ascending resolution for a simple (single, non-absolute) identifier.
     * Implements the full algorithm: SIMPLE_LOOKUP → USING_LOOKUP → upward climb.
     * Stops at the first scope that provides a match (shadowing rule).
     */
    public static @NotNull List<PsiElement> resolveSimple(@NotNull PsiElement anchor,
                                                          @NotNull String name) {
        List<PsiElement> results = new ArrayList<>();
        Set<PsiElement>  visited = new HashSet<>();

        PsiTreeUtil.treeWalkUp(anchor, null, (scope, lastParent) -> {
            // §5.5 — SIMPLE_LOOKUP at this scope level
            collectFromScope(scope, lastParent, name, results, visited, false);
            if (!results.isEmpty()) return false; // stop — found (shadowing)

            // §5.6 — USING_LOOKUP at this scope level
            collectFromUsings(scope, lastParent, name, null, results, new HashSet<>(visited));
            // Stop if found (using directives shadow outer scopes)
            return results.isEmpty();
        });

        return results;
    }

    // ── §6 — Absolute name lookup from module root ────────────────────────────

    /**
     * Absolute qualified resolution.
     * Strips an optional matching module-name prefix, then performs a pure
     * descending search from the file root (§6).
     */
    public static @NotNull List<PsiElement> resolveAbsolute(@NotNull PsiElement anchor,
                                                            @NotNull String[] segments) {
        PsiFile file = anchor.getContainingFile();
        if (file == null || segments.length == 0) return Collections.emptyList();

        String[] moduleName = effectiveModuleName(file);

        // §6 Step 1: if first components match module name, strip them
        String[] withoutModule = stripModulePrefix(segments, moduleName);

        // §6 Step 2: DOWN_LOOKUP from file root (M2: getDirectChildren spans all module files)
        List<PsiElement> results = new ArrayList<>();
        downLookup(file, withoutModule, 0, results, new HashSet<>());

        // If stripping module prefix didn't find anything, try without stripping
        if (results.isEmpty() && withoutModule != segments) {
            downLookup(file, segments, 0, results, new HashSet<>());
        }

        return results;
    }

    // ── §5.4 — Qualified names (relative) — ascending + descending ────────────

    /**
     * Relative qualified resolution (multi-segment, non-absolute).
     * Handles special forms (§11) first, then implements QUALIFIED_LOOKUP (§5.4):
     * ascending search for the first segment as a container, then descending.
     */
    public static @NotNull List<PsiElement> resolveQualified(@NotNull PsiElement anchor,
                                                             @NotNull String[] segments) {
        if (segments.length == 0) return Collections.emptyList();

        // §11.3 — Annotation RTTI: last segment is "annotation"
        if ("annotation".equals(segments[segments.length - 1])) {
            List<PsiElement> annotResult = resolveAnnotationRtti(anchor, segments);
            if (!annotResult.isEmpty()) return annotResult;
        }

        // §11.2 — Union Kind entry: 3 segments, middle == "Kind"
        if (segments.length == 3 && "Kind".equals(segments[1])) {
            List<PsiElement> unionResult = resolveUnionKindEntry(anchor, segments);
            if (!unionResult.isEmpty()) return unionResult;
        }

        // §11.1 — Enum entry: 2 segments (EnumName::entryName)
        if (segments.length == 2) {
            List<PsiElement> enumResult = resolveEnumEntry(anchor, segments);
            if (!enumResult.isEmpty()) return enumResult;
        }

        // §5.4 — Standard qualified lookup with ascending climb for first segment
        return qualifiedLookup(anchor, segments);
    }

    /**
     * QUALIFIED_LOOKUP (§5.4): ascending search for the first segment as a container
     * (namespace / aggregate / enum), then pure descending for subsequent segments.
     * Also considers using directives at each level.
     */
    private static @NotNull List<PsiElement> qualifiedLookup(@NotNull PsiElement anchor,
                                                             @NotNull String[] segments) {
        List<PsiElement> results = new ArrayList<>();
        Set<PsiElement>  visited = new HashSet<>();

        PsiTreeUtil.treeWalkUp(anchor, null, (scope, lastParent) -> {
            // Try finding first segment as a container in this scope
            List<PsiElement> containers = new ArrayList<>();
            collectContainersFromScope(scope, lastParent, segments[0], containers, new HashSet<>(visited));

            for (PsiElement container : containers) {
                if (segments.length == 1) {
                    results.add(container);
                } else {
                    downLookup(container, segments, 1, results, new HashSet<>());
                }
            }
            if (!results.isEmpty()) return false;

            // Try using directives for the full qualified name
            collectFromUsings(scope, lastParent, segments[0], segments, results, new HashSet<>(visited));
            return results.isEmpty();
        });

        return results;
    }

    // ── §7 — Pure Downward Lookup (DOWN_LOOKUP) ───────────────────────────────

    /**
     * Pure downward lookup: descends through container scopes to resolve
     * {@code segments[from..]}. Never climbs back up.
     */
    static void downLookup(@NotNull PsiElement container,
                           @NotNull String[] segments, int from,
                           @NotNull List<PsiElement> results,
                           @NotNull Set<PsiElement> visited) {
        if (from >= segments.length || !visited.add(container)) return;

        String  name   = segments[from];
        boolean isLast = (from == segments.length - 1);

        for (PsiElement child : getDirectChildren(container)) {
            String childName = nameOf(child);
            if (!name.equals(childName)) continue;

            if (isLast) {
                results.add(child);
            } else if (isContainer(child)) {
                downLookup(child, segments, from + 1, results, visited);
            }
        }
    }

    // ── §8 — Inherited Member Lookup (BFS) ────────────────────────────────────

    /**
     * Search inherited members via BFS over base classes.
     * Returns all matching members found at the same BFS depth (for ambiguity check).
     */
    private static @NotNull List<PsiElement> inheritedLookup(@NotNull String name,
                                                             @NotNull KlangAggregateDecl aggregate,
                                                             @NotNull PsiElement anchor) {
        List<PsiElement> results = new ArrayList<>();
        List<KlangAggregateDecl> queue = getDirectBases(aggregate, anchor);
        Set<PsiElement> seen = new HashSet<>();
        seen.add(aggregate);

        while (!queue.isEmpty()) {
            List<KlangAggregateDecl> nextLevel = new ArrayList<>();
            for (KlangAggregateDecl base : queue) {
                if (!seen.add(base)) continue;
                // Search this base for the name
                for (KlangDeclaration decl : base.getDeclarationList()) {
                    PsiElement found = matchDeclarationByName(decl, name);
                    if (found != null) results.add(found);
                }
            }
            if (!results.isEmpty()) return results; // found at this depth — stop
            // Enqueue next level
            for (KlangAggregateDecl base : queue) {
                if (seen.contains(base)) {
                    nextLevel.addAll(getDirectBases(base, anchor));
                }
            }
            queue = nextLevel;
        }
        return results;
    }

    // ── Inheritance navigation (gutter line markers / type hierarchy) ─────────

    /**
     * Direct base aggregates of {@code agg} (its base names resolved from the aggregate's
     * enclosing lexical scope, §5). Public entry point for hierarchy/line-marker features.
     */
    public static @NotNull List<KlangAggregateDecl> directBases(@NotNull KlangAggregateDecl agg) {
        return getDirectBases(agg, agg);
    }

    /**
     * The {@code ::k::Throwable} aggregate declaration(s) visible from {@code anchor}, if any.
     * Resolved as an absolute name (with {@code import}/external-module fallback), so it only
     * succeeds when the K standard library's {@code Throwable} is present in the project (or
     * declared locally). Returns an empty set otherwise — callers should then <em>skip</em>
     * any Throwable-derivation validation (fail soft: no diagnostics without the base type).
     */
    public static @NotNull Set<KlangAggregateDecl> resolveThrowable(@NotNull PsiElement anchor) {
        Set<KlangAggregateDecl> result = new LinkedHashSet<>();
        for (PsiElement el : resolve(anchor, "::k::Throwable")) {
            if (el instanceof KlangAggregateDecl agg) result.add(agg);
        }
        return result;
    }

    /**
     * Whether {@code agg} <em>is</em>, or transitively <em>derives from</em>, any of the
     * {@code ancestors}. Performs a cycle-safe breadth-first walk of the base graph
     * ({@link #directBases}). Used to validate that an exception type is
     * {@code ::k::Throwable}-derived (§ ThrowStatement / CatchParameterDecl).
     */
    public static boolean derivesFromAny(@NotNull KlangAggregateDecl agg,
                                         @NotNull Set<KlangAggregateDecl> ancestors) {
        if (ancestors.isEmpty()) return false;
        if (ancestors.contains(agg)) return true;
        Set<KlangAggregateDecl> seen = new HashSet<>();
        Deque<KlangAggregateDecl> queue = new ArrayDeque<>();
        seen.add(agg);
        queue.add(agg);
        while (!queue.isEmpty()) {
            KlangAggregateDecl current = queue.poll();
            for (KlangAggregateDecl base : directBases(current)) {
                if (ancestors.contains(base)) return true;
                if (seen.add(base)) queue.add(base);
            }
        }
        return false;
    }

    /**
     * The aggregate that directly owns {@code func} as a member, or {@code null} when
     * {@code func} is a free (namespace-level) function.
     */
    public static @Nullable KlangAggregateDecl owningAggregate(@NotNull KlangFunctionDecl func) {
        PsiElement parent = func.getParent();
        if (parent instanceof KlangDeclaration) parent = parent.getParent();
        return parent instanceof KlangAggregateDecl agg ? agg : null;
    }

    /**
     * Find the base-class method(s) that {@code method} overrides: a BFS over the owning
     * aggregate's bases (§8) collecting member functions with the same simple name, stopping
     * at the nearest BFS depth that yields a match (§15 — a derived name hides all same-named
     * base overloads). Returns an empty list for free functions, unnamed functions
     * (operator / destructor) or when no base declares the name.
     */
    public static @NotNull List<KlangFunctionDecl> findOverriddenMethods(@NotNull KlangFunctionDecl method) {
        String name = method.getName();
        KlangAggregateDecl owner = owningAggregate(method);
        if (name == null || owner == null) return Collections.emptyList();

        List<KlangFunctionDecl> results = new ArrayList<>();
        List<KlangAggregateDecl> queue = getDirectBases(owner, owner);
        Set<PsiElement> seen = new HashSet<>();
        seen.add(owner);
        while (!queue.isEmpty()) {
            List<KlangAggregateDecl> nextLevel = new ArrayList<>();
            for (KlangAggregateDecl base : queue) {
                if (!seen.add(base)) continue;
                for (KlangDeclaration decl : base.getDeclarationList()) {
                    KlangFunctionDecl fn = decl.getFunctionDecl();
                    if (fn != null && fn != method && name.equals(fn.getName())) {
                        results.add(fn);
                    }
                }
            }
            if (!results.isEmpty()) return results; // nearest ancestors win (§15)
            for (KlangAggregateDecl base : queue) {
                nextLevel.addAll(getDirectBases(base, base));
            }
            queue = nextLevel;
        }
        return results;
    }

    /**
     * <b>B3 (project-wide).</b> Find the methods that override {@code baseMethod}: every member
     * function of a (transitive) subtype of the method's owning aggregate whose
     * {@link #findOverriddenMethods} set contains {@code baseMethod}. The subtype set is computed
     * project-wide via {@link #findSubAggregates} (index-narrowed), so override markers span the
     * whole project. Reuses the upward override computation so §15 nearest-ancestor hiding holds.
     */
    public static @NotNull List<KlangFunctionDecl> findOverridingMethods(@NotNull KlangFunctionDecl baseMethod) {
        KlangAggregateDecl owner = owningAggregate(baseMethod);
        if (owner == null) return Collections.emptyList();
        List<KlangFunctionDecl> result = new ArrayList<>();
        for (KlangAggregateDecl sub : findSubAggregates(owner)) {
            for (KlangDeclaration decl : sub.getDeclarationList()) {
                KlangFunctionDecl fn = decl.getFunctionDecl();
                if (fn != null && fn != baseMethod && findOverriddenMethods(fn).contains(baseMethod)) {
                    result.add(fn);
                }
            }
        }
        return result;
    }

    /**
     * Collect every declaration participating in the same override/implementation chain as
     * {@code method}: overridden methods up the hierarchy and overriding methods down the
     * hierarchy, transitively.
     */
    public static @NotNull List<KlangFunctionDecl> findRenameCompanionMethods(@NotNull KlangFunctionDecl method) {
        LinkedHashSet<KlangFunctionDecl> result = new LinkedHashSet<>();
        Deque<KlangFunctionDecl> queue = new ArrayDeque<>();
        result.add(method);
        queue.add(method);
        while (!queue.isEmpty()) {
            KlangFunctionDecl current = queue.poll();
            for (KlangFunctionDecl related : findOverriddenMethods(current)) {
                if (result.add(related)) queue.add(related);
            }
            for (KlangFunctionDecl related : findOverridingMethods(current)) {
                if (result.add(related)) queue.add(related);
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * <b>B3 (project-wide).</b> Find the aggregates that derive (directly or transitively) from
     * {@code base}, across the whole project. Uses {@link KlangBaseNameIndex} to visit only the
     * files that mention each base name, expanding transitively over the derivation relation
     * (so a grandchild in a third file is found). Falls back to a file-local scan when the index
     * is not ready (dumb mode).
     */
    public static @NotNull List<KlangAggregateDecl> findSubAggregates(@NotNull KlangAggregateDecl base) {
        Project project = base.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        LinkedHashSet<KlangAggregateDecl> result = new LinkedHashSet<>();
        Set<KlangAggregateDecl> seen = new HashSet<>();
        seen.add(base);
        Deque<KlangAggregateDecl> queue = new ArrayDeque<>();
        queue.add(base);
        try {
            while (!queue.isEmpty()) {
                KlangAggregateDecl current = queue.poll();
                String simple = current.getName();
                if (simple == null) continue;
                for (VirtualFile vf : KlangBaseNameIndex.filesContainingBase(project, simple)) {
                    PsiFile pf = psiManager.findFile(vf);
                    if (!(pf instanceof KlangFile)) continue;
                    for (KlangAggregateDecl candidate :
                            PsiTreeUtil.collectElementsOfType(pf, KlangAggregateDecl.class)) {
                        if (seen.contains(candidate)) continue;
                        if (directBases(candidate).contains(current)) {
                            seen.add(candidate);
                            result.add(candidate);
                            queue.add(candidate); // expand transitively
                        }
                    }
                }
            }
        } catch (IndexNotReadyException ignored) {
            return findSubAggregatesInFile(base); // dumb mode — file-local fallback
        }
        return new ArrayList<>(result);
    }

    /** File-local fallback for {@link #findSubAggregates} (used when the index is not ready). */
    private static @NotNull List<KlangAggregateDecl> findSubAggregatesInFile(@NotNull KlangAggregateDecl base) {
        PsiFile file = base.getContainingFile();
        if (file == null) return Collections.emptyList();
        List<KlangAggregateDecl> result = new ArrayList<>();
        for (KlangAggregateDecl candidate : PsiTreeUtil.collectElementsOfType(file, KlangAggregateDecl.class)) {
            if (candidate == base) continue;
            if (isSubtypeOf(candidate, base)) result.add(candidate);
        }
        return result;
    }

    // ── Abstract-method-implementation completeness (missing-implementation inspection) ─────

    /** Whether {@code fn} is a constructor or destructor (never part of override/abstract bookkeeping). */
    public static boolean isConstructorOrDestructor(@NotNull KlangFunctionDecl fn) {
        if (fn.getNode().findChildByType(KlangTypes.DESTRUCTOR_HEAD) != null) return true;
        if (fn.getNode().findChildByType(KlangTypes.FUNCTION_HEAD) == null) return false;
        String name = fn.getName();
        if (name == null) return false;
        KlangAggregateDecl owner = owningAggregate(fn);
        return owner != null && name.equals(owner.getName());
    }

    /** Whether {@code agg} is declared with the {@code interface} keyword. */
    public static boolean isInterface(@NotNull KlangAggregateDecl agg) {
        return agg.getNode().findChildByType(KlangTypes.KW_INTERFACE) != null;
    }

    /** Whether {@code agg} itself carries the {@code abstract} specifier. */
    public static boolean isAbstractAggregate(@NotNull KlangAggregateDecl agg) {
        return hasSpecifier(agg.getSpecifierList(), "abstract");
    }

    /**
     * Whether {@code method} needs a concrete override somewhere to be considered "implemented":
     * an explicit {@code abstract} specifier, or a non-{@code static}, non-{@code default}
     * member of an {@code interface} (implicit interface abstractness). Unlike the gutter-marker
     * provider's notion (which lumps {@code default} together with abstract for navigation
     * purposes), a {@code default} interface method already carries a real body and therefore
     * does <em>not</em> require an implementation here.
     */
    public static boolean requiresImplementation(@NotNull KlangFunctionDecl method) {
        List<KlangSpecifier> specs = method.getSpecifierList();
        if (hasSpecifier(specs, "abstract")) return true;
        KlangAggregateDecl owner = owningAggregate(method);
        return owner != null
                && isInterface(owner)
                && !hasSpecifier(specs, "static")
                && !hasSpecifier(specs, "default");
    }

    private static boolean hasSpecifier(@NotNull List<KlangSpecifier> specifiers, @NotNull String keyword) {
        return specifiers.stream().anyMatch(s -> keyword.equals(s.getText()));
    }

    /**
     * Nearest-ancestor "virtual slot" resolution for {@code name}: BFS starting at {@code agg}
     * itself (level 0 = {@code agg}'s own member functions named {@code name}), then its direct
     * bases, etc. Mirrors the same name-based hiding rule (§15) as {@link #findOverriddenMethods},
     * except the search <em>includes</em> the starting aggregate — it answers "what does calling
     * {@code name} virtually on an instance of {@code agg} dispatch to".
     */
    public static @NotNull List<KlangFunctionDecl> resolveVirtualSlot(@NotNull KlangAggregateDecl agg,
                                                                      @NotNull String name) {
        List<KlangFunctionDecl> results = new ArrayList<>();
        List<KlangAggregateDecl> queue = new ArrayList<>();
        queue.add(agg);
        Set<PsiElement> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            List<KlangAggregateDecl> nextLevel = new ArrayList<>();
            for (KlangAggregateDecl current : queue) {
                if (!seen.add(current)) continue;
                for (KlangDeclaration decl : current.getDeclarationList()) {
                    KlangFunctionDecl fn = decl.getFunctionDecl();
                    if (fn != null && !isConstructorOrDestructor(fn) && name.equals(fn.getName())) {
                        results.add(fn);
                    }
                }
            }
            if (!results.isEmpty()) return results; // nearest ancestors win (§15)
            for (KlangAggregateDecl current : queue) {
                nextLevel.addAll(getDirectBases(current, current));
            }
            queue = nextLevel;
        }
        return results;
    }

    /**
     * Every distinct method name that is abstract ({@link #requiresImplementation}) somewhere in
     * the transitive closure of {@code agg}'s bases (bases only — {@code agg} itself is excluded,
     * since we are collecting <em>inherited</em> obligations), together with one representative
     * abstract declaration per name (used for messages / stub generation). Cycle-safe BFS.
     */
    public static @NotNull Map<String, KlangFunctionDecl> collectAbstractSlots(@NotNull KlangAggregateDecl agg) {
        Map<String, KlangFunctionDecl> slots = new LinkedHashMap<>();
        Set<PsiElement> seen = new HashSet<>();
        seen.add(agg);
        Deque<KlangAggregateDecl> queue = new ArrayDeque<>(getDirectBases(agg, agg));
        while (!queue.isEmpty()) {
            KlangAggregateDecl current = queue.poll();
            if (!seen.add(current)) continue;
            for (KlangDeclaration decl : current.getDeclarationList()) {
                KlangFunctionDecl fn = decl.getFunctionDecl();
                if (fn == null || isConstructorOrDestructor(fn)) continue;
                String name = fn.getName();
                if (name == null) continue;
                if (requiresImplementation(fn)) {
                    slots.putIfAbsent(name, fn);
                }
            }
            for (KlangAggregateDecl base : getDirectBases(current, current)) {
                if (!seen.contains(base)) queue.add(base);
            }
        }
        return slots;
    }

    /**
     * Abstract methods inherited by {@code agg} for which it (and its hierarchy, following the
     * nearest-ancestor dispatch rule) provides no concrete override — i.e. the subset of
     * {@link #collectAbstractSlots} whose {@link #resolveVirtualSlot} from {@code agg} is either
     * empty or made up exclusively of still-abstract declarations. Empty when {@code agg} fully
     * implements its inherited abstract surface.
     */
    public static @NotNull List<KlangFunctionDecl> findMissingImplementations(@NotNull KlangAggregateDecl agg) {
        List<KlangFunctionDecl> missing = new ArrayList<>();
        for (Map.Entry<String, KlangFunctionDecl> slot : collectAbstractSlots(agg).entrySet()) {
            List<KlangFunctionDecl> resolved = resolveVirtualSlot(agg, slot.getKey());
            if (resolved.isEmpty() || resolved.stream().allMatch(KlangResolveUtil::requiresImplementation)) {
                missing.add(slot.getValue());
            }
        }
        return missing;
    }

    /**
     * Every method name inherited (transitively) by {@code agg} that is <em>not</em> already
     * declared locally by {@code agg} itself — the candidate set for a "Generate: Override/Implement
     * Members…" action. Includes both still-abstract members ({@link #requiresImplementation}, to be
     * pre-checked/required in the UI) and already-concrete/virtual ones (optional overrides), one
     * representative declaration per name following the same nearest-ancestor rule (§15) as
     * {@link #collectAbstractSlots}. {@code static} members are excluded (never virtual/overridable).
     */
    public static @NotNull Map<String, KlangFunctionDecl> collectOverridableSlots(@NotNull KlangAggregateDecl agg) {
        Set<String> ownNames = new HashSet<>();
        for (KlangDeclaration decl : agg.getDeclarationList()) {
            KlangFunctionDecl fn = decl.getFunctionDecl();
            if (fn != null && !isConstructorOrDestructor(fn) && fn.getName() != null) {
                ownNames.add(fn.getName());
            }
        }

        Map<String, KlangFunctionDecl> slots = new LinkedHashMap<>();
        Set<PsiElement> seen = new HashSet<>();
        seen.add(agg);
        Deque<KlangAggregateDecl> queue = new ArrayDeque<>(getDirectBases(agg, agg));
        while (!queue.isEmpty()) {
            KlangAggregateDecl current = queue.poll();
            if (!seen.add(current)) continue;
            for (KlangDeclaration decl : current.getDeclarationList()) {
                KlangFunctionDecl fn = decl.getFunctionDecl();
                if (fn == null || isConstructorOrDestructor(fn)) continue;
                if (hasSpecifier(fn.getSpecifierList(), "static")) continue;
                String name = fn.getName();
                if (name == null || ownNames.contains(name)) continue;
                slots.putIfAbsent(name, fn);
            }
            for (KlangAggregateDecl base : getDirectBases(current, current)) {
                if (!seen.contains(base)) queue.add(base);
            }
        }
        return slots;
    }

    /** Whether {@code candidate} derives, directly or transitively, from {@code base}. */
    private static boolean isSubtypeOf(@NotNull KlangAggregateDecl candidate,
                                       @NotNull KlangAggregateDecl base) {
        Set<PsiElement> seen = new HashSet<>();
        Deque<KlangAggregateDecl> queue = new ArrayDeque<>(directBases(candidate));
        while (!queue.isEmpty()) {
            KlangAggregateDecl b = queue.poll();
            if (!seen.add(b)) continue;
            if (b == base) return true;
            queue.addAll(directBases(b));
        }
        return false;
    }

    /**
     * Get the direct base aggregates of an aggregate by resolving its baseClause.
     */
    private static @NotNull List<KlangAggregateDecl> getDirectBases(
            @NotNull KlangAggregateDecl agg, @NotNull PsiElement anchor) {
        List<KlangAggregateDecl> bases = new ArrayList<>();
        KlangBaseClause baseClause = agg.getBaseClause();
        if (baseClause == null) return bases;

        for (KlangBaseSpec baseSpec : baseClause.getBaseSpecList()) {
            String baseText = baseSpec.getQualifiedIdentifier().getText().trim();
            // Resolve the base type name from the aggregate's *enclosing* scope, never from
            // within the aggregate itself: a base name looked up inside the aggregate would
            // re-enter inheritedLookup → getDirectBases → resolve and recurse infinitely
            // (and, per §5, base names are resolved in the lexical scope around the class).
            PsiElement enclosing = agg.getParent() != null ? agg.getParent() : agg;
            List<PsiElement> resolved = resolve(enclosing, baseText);
            for (PsiElement el : resolved) {
                if (el instanceof KlangAggregateDecl baseAgg && baseAgg != agg) {
                    bases.add(baseAgg);
                }
            }
        }
        return bases;
    }

    // ── §10 — Member lookup (public entry points for member access) ───────────

    /**
     * Resolve a member (field or method) of an aggregate, including inherited members
     * via BFS over base classes (§8, §10.4). Used by member-access resolution.
     */
    public static @NotNull List<PsiElement> resolveMember(@NotNull KlangAggregateDecl owner,
                                                          @NotNull String name,
                                                          @NotNull PsiElement anchor) {
        List<PsiElement> results = new ArrayList<>();
        collectFromDeclarationList(owner.getDeclarationList(), name, results, new HashSet<>());
        if (results.isEmpty()) results.addAll(inheritedLookup(name, owner, anchor));
        return results;
    }

    /**
     * Resolve a member of a {@code union} value ({@code union.member}) to its
     * {@code unionMemberDecl}. A union exposes only its declared alternatives as members — there
     * are no inherited members or methods to search (§10 / unions). Returns {@code null} when no
     * alternative matches {@code name}.
     */
    public static @Nullable PsiElement resolveUnionMember(@NotNull KlangUnionDecl union,
                                                          @NotNull String name) {
        for (KlangUnionMemberDecl member : union.getUnionMemberDeclList()) {
            if (name.equals(member.getIdentifier().getText())) return member;
        }
        return null;
    }

    /**
     * Unified call syntax (§10.3): collect free functions named {@code name}, visible from
     * {@code anchor}, whose first parameter type is (a base of) {@code receiverType}.
     */
    public static @NotNull List<PsiElement> resolveUnifiedCall(@NotNull String name,
                                                              @NotNull KlangAggregateDecl receiverType,
                                                              @NotNull PsiElement anchor) {
        List<PsiElement> named = new ArrayList<>();
        PsiTreeUtil.treeWalkUp(anchor, null, (scope, lastParent) -> {
            collectFromScope(scope, lastParent, name, named, new HashSet<>(), true);
            return true; // climb through every scope, collecting all overloads
        });
        List<PsiElement> result = new ArrayList<>();
        for (PsiElement el : named) {
            if (!(el instanceof KlangFunctionDecl fn) || result.contains(fn)) continue;
            // Only *free* functions participate in unified call syntax.
            if (PsiTreeUtil.getParentOfType(fn, KlangAggregateDecl.class) != null) continue;
            KlangParameterList params = fn.getParameterList();
            if (params == null || params.getParameterSpecList().isEmpty()) continue;
            KlangParameterSpec first = params.getParameterSpecList().get(0);
            KlangAggregateDecl firstType = KlangTypeUtil.aggregateOfTypeSpec(first.getTypeSpec(), first);
            if (firstType != null && isSameOrBase(firstType, receiverType, anchor)) {
                result.add(fn);
            }
        }
        return result;
    }

    // ── Constructor vs type disambiguation ────────────────────────────────────

    /**
     * Returns the constructor declarations of an aggregate: its member functions whose
     * simple name equals the aggregate's own name (K declares constructors C++-style,
     * e.g. {@code Point(x: float, y: float)} inside {@code struct Point}).
     *
     * @return the (possibly overloaded) constructor functions, or an empty list when the
     *         aggregate declares no explicit constructor.
     */
    public static @NotNull List<PsiElement> constructorsOf(@NotNull KlangAggregateDecl agg) {
        List<PsiElement> result = new ArrayList<>();
        String aggName = agg.getName();
        if (aggName == null) return result;
        for (KlangDeclaration decl : agg.getDeclarationList()) {
            KlangFunctionDecl fn = decl.getFunctionDecl();
            if (fn != null && aggName.equals(fn.getName())) result.add(fn);
        }
        return result;
    }

    /**
     * Rewrites a resolution result for a <em>constructor-call</em> context: every aggregate
     * is replaced by its constructor overloads, so the reference targets the constructor
     * rather than the type. An aggregate that declares no explicit constructor is kept as-is
     * (navigation still lands on the type). Non-aggregate results are preserved unchanged.
     */
    public static @NotNull List<PsiElement> preferConstructors(@NotNull List<PsiElement> resolved) {
        List<PsiElement> out = new ArrayList<>();
        for (PsiElement el : resolved) {
            if (el instanceof KlangAggregateDecl agg) {
                List<PsiElement> ctors = constructorsOf(agg);
                if (!ctors.isEmpty()) out.addAll(ctors);
                else out.add(agg); // no explicit constructor — fall back to the type
            } else {
                out.add(el);
            }
        }
        return out;
    }

    /**
     * True when {@code identifierExpr} is the callee of a call expression — i.e. it is the
     * {@code primaryExpr} of a {@code postfixExpr} whose first {@code postfixOp} is a
     * parenthesised argument list ({@code Name(args)}). When {@code Name} denotes an
     * aggregate type, such an occurrence is a constructor call (§ type/constructor split).
     */
    public static boolean isExpressionConstructorCall(@NotNull PsiElement identifierExpr) {
        PsiElement primary = identifierExpr.getParent();
        if (!(primary instanceof KlangPrimaryExpr)) return false;
        PsiElement postfix = primary.getParent();
        if (!(postfix instanceof KlangPostfixExpr pe)) return false;
        if (pe.getPrimaryExpr() != primary) return false;
        List<KlangPostfixOp> ops = pe.getPostfixOpList();
        return !ops.isEmpty() && firstTokenIsLParen(ops.get(0));
    }

    /**
     * True when {@code identifierExpr} is the <em>callee of a call expression</em> — either a
     * bare {@code Name(args)} (a {@link #isExpressionConstructorCall} occurrence) or the member
     * of a method-style call {@code recv.member(args)} / {@code recv->member(args)}.
     *
     * <p>This is the spec's <b>deferred-resolution</b> rule (§16): a name in callee position may
     * legitimately stay unresolved through the first name-resolution pass because it could be a
     * unified-call-syntax candidate or an unmodelled overload. The unresolved-reference inspection
     * therefore must <b>not</b> flag such a name. Keeping the rule here (next to the resolution
     * logic) keeps it testable and reusable.</p>
     */
    public static boolean isCalleeOfCall(@NotNull PsiElement identifierExpr) {
        // Bare expression call: Name(args)
        if (isExpressionConstructorCall(identifierExpr)) return true;

        // Member call: recv.member(args) — the identifierExpr is the RHS of a '.'/'->' postfixOp,
        // immediately followed by a parenthesised-argument postfixOp on the same postfixExpr.
        PsiElement memberOp = identifierExpr.getParent();
        if (memberOp instanceof KlangPostfixOp op && op.getParent() instanceof KlangPostfixExpr pe) {
            List<KlangPostfixOp> ops = pe.getPostfixOpList();
            int idx = ops.indexOf(op);
            if (idx >= 0 && idx + 1 < ops.size()) {
                return firstTokenIsLParen(ops.get(idx + 1));
            }
        }
        return false;
    }

    /**
     * True when {@code qualifiedIdentifier} is the type of a variable-like declaration that is
     * initialised through a parenthesised constructor call ({@code p : Point(args)}). The
     * declaration's type name then denotes the constructor being invoked, not merely the type.
     */
    public static boolean isTypeInitConstructorCall(@NotNull PsiElement qualifiedIdentifier) {
        PsiElement typeSpec = qualifiedIdentifier.getParent();
        if (!(typeSpec instanceof KlangTypeSpec)) return false;
        PsiElement decl = typeSpec.getParent();
        PsiElement init = null;
        if (decl instanceof KlangVariableDecl v)        init = v.getInitialiser();
        else if (decl instanceof KlangIfCondVarDecl c)  init = c.getCondVarInitialiser();
        else if (decl instanceof KlangNamedReturnVar r) init = r.getNamedReturnInit();
        return init != null && firstTokenIsLParen(init);
    }

    /**
     * True when {@code qualifiedIdentifier} is the constructed type of a {@code new} expression
     * ({@code new Point(args)}, {@code new Point{…}}, {@code new Point[n]}). The type name then
     * denotes the constructor being invoked, not merely the type.
     */
    public static boolean isNewExprConstructorCall(@NotNull PsiElement qualifiedIdentifier) {
        PsiElement typeName = qualifiedIdentifier.getParent();
        return typeName instanceof KlangTypeName && typeName.getParent() instanceof KlangNewExpr;
    }

    /**
     * True when {@code qualifiedIdentifier} is the annotation type in an annotation with
     * arguments ({@code @Foo(args)} or {@code @Foo{…}}). The annotation name then denotes
     * a constructor (if one exists), not merely the type. If no explicit constructor exists,
     * navigation falls back to the type.
     */
    public static boolean isAnnotationConstructorCall(@NotNull PsiElement qualifiedIdentifier) {
        PsiElement parent = qualifiedIdentifier.getParent();
        if (!(parent instanceof KlangAnnotationDef annot)) return false;
        // Annotation has constructor arguments if it has an expression list or brace init
        return annot.getExpressionList() != null || annot.getBraceInitList() != null;
    }

    /** True if the first non-whitespace token of {@code el} is an opening parenthesis. */
    private static boolean firstTokenIsLParen(@NotNull PsiElement el) {
        ASTNode n = el.getNode().getFirstChildNode();
        while (n != null && n.getElementType() == TokenType.WHITE_SPACE) n = n.getTreeNext();
        return n != null && n.getElementType() == KlangTypes.PUNC_LPAREN;
    }

    /** True if {@code candidate} is {@code type} or one of its (transitive) base aggregates. */
    private static boolean isSameOrBase(@NotNull KlangAggregateDecl candidate,
                                        @NotNull KlangAggregateDecl type,
                                        @NotNull PsiElement anchor) {
        if (candidate == type) return true;
        List<KlangAggregateDecl> queue = getDirectBases(type, anchor);
        Set<PsiElement> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            List<KlangAggregateDecl> next = new ArrayList<>();
            for (KlangAggregateDecl b : queue) {
                if (!seen.add(b)) continue;
                if (b == candidate) return true;
                next.addAll(getDirectBases(b, anchor));
            }
            queue = next;
        }
        return false;
    }

    // ── Operator overload resolution ──────────────────────────────────────────

    /**
     * Resolves an operator usage ({@code a == b}, {@code -a}, …) to its overload
     * declaration(s): first the member {@code operator} functions of {@code leftType}
     * (including inherited ones), then visible <em>free</em> {@code operator} functions whose
     * first parameter is (a base of) {@code leftType}. {@code symbol} is the operator's
     * textual form (e.g. {@code "=="}, {@code "+="}).
     */
    public static @NotNull List<PsiElement> resolveOperator(@NotNull String symbol,
                                                            @NotNull KlangAggregateDecl leftType,
                                                            @NotNull PsiElement anchor) {
        String want = normalizeOp(symbol);
        if (want.isEmpty()) return Collections.emptyList();

        List<PsiElement> result = new ArrayList<>();
        collectMemberOperators(leftType, want, anchor, result, new HashSet<>());

        // Free operator functions visible from the usage site (unified-call-like match).
        List<KlangFunctionDecl> frees = new ArrayList<>();
        PsiTreeUtil.treeWalkUp(anchor, null, (scope, lastParent) -> {
            collectFreeOperators(scope, want, frees);
            return true;
        });
        for (KlangFunctionDecl fn : frees) {
            if (result.contains(fn)) continue;
            if (PsiTreeUtil.getParentOfType(fn, KlangAggregateDecl.class) != null) continue;
            KlangParameterList params = fn.getParameterList();
            if (params == null || params.getParameterSpecList().isEmpty()) continue;
            KlangParameterSpec first = params.getParameterSpecList().get(0);
            KlangAggregateDecl firstType = KlangTypeUtil.aggregateOfTypeSpec(first.getTypeSpec(), first);
            if (firstType != null && isSameOrBase(firstType, leftType, anchor)) result.add(fn);
        }
        return result;
    }

    /** DFS over {@code agg} and (only if nothing is found locally) its bases for {@code operator symbol}. */
    private static void collectMemberOperators(@NotNull KlangAggregateDecl agg,
                                               @NotNull String want,
                                               @NotNull PsiElement anchor,
                                               @NotNull List<PsiElement> out,
                                               @NotNull Set<PsiElement> seen) {
        if (!seen.add(agg)) return;
        for (KlangDeclaration decl : agg.getDeclarationList()) {
            KlangFunctionDecl fn = decl.getFunctionDecl();
            if (matchesOperator(fn, want)) out.add(fn);
        }
        if (out.isEmpty()) {
            for (KlangAggregateDecl base : getDirectBases(agg, anchor)) {
                collectMemberOperators(base, want, anchor, out, seen);
            }
        }
    }

    /** Collect free {@code operator} functions declared directly in a file/namespace scope. */
    private static void collectFreeOperators(@NotNull PsiElement scope,
                                             @NotNull String want,
                                             @NotNull List<KlangFunctionDecl> out) {
        if (scope instanceof KlangFile) {
            for (PsiElement child : scope.getChildren()) {
                if (child instanceof KlangDeclaration decl && matchesOperator(decl.getFunctionDecl(), want)) {
                    out.add(decl.getFunctionDecl());
                }
            }
        } else if (scope instanceof KlangNamespaceDecl ns) {
            for (KlangDeclaration decl : ns.getDeclarationList()) {
                if (matchesOperator(decl.getFunctionDecl(), want)) out.add(decl.getFunctionDecl());
            }
        }
    }

    /** True if {@code fn} is an {@code operator} overload whose symbol equals {@code want}. */
    private static boolean matchesOperator(@Nullable KlangFunctionDecl fn, @NotNull String want) {
        if (fn == null) return false;
        KlangOperatorFunctionHead head = fn.getOperatorFunctionHead();
        if (head == null) return false;
        KlangOperatorSymbol sym = head.getOperatorSymbol();
        // No operatorSymbol child ⇒ the head is the subscript form 'operator [ ]'.
        // (The cast/conversion operator 'operator ()' is a distinct CastOperatorFunctionHead.)
        String txt = sym != null ? normalizeOp(sym.getText()) : "[]";
        return want.equals(txt);
    }

    /** Removes all whitespace from an operator's textual form so {@code "+ ="} matches {@code "+="}. */
    private static @NotNull String normalizeOp(@NotNull String s) {
        return s.replaceAll("\\s+", "");
    }

    // ── §11.1 — Enum Entry Lookup ─────────────────────────────────────────────

    /**
     * Resolve {@code EnumName::entryName} to an enum entry constant.
     */
    private static @NotNull List<PsiElement> resolveEnumEntry(@NotNull PsiElement anchor,
                                                              @NotNull String[] segments) {
        String enumName  = segments[0];
        String entryName = segments[1];

        // Walk up the scope chain looking for an enumeration named enumName
        List<PsiElement> results = new ArrayList<>();
        PsiTreeUtil.treeWalkUp(anchor, null, (scope, lastParent) -> {
            for (PsiElement child : getDirectChildren(scope)) {
                if (child instanceof KlangEnumDecl enumDecl && enumName.equals(enumDecl.getName())) {
                    // Found the enum — now look for the entry
                    for (KlangEnumEntry entry : enumDecl.getEnumEntryList()) {
                        ASTNode id = entry.getNode().findChildByType(KlangTypes.IDENTIFIER);
                        if (id != null && entryName.equals(id.getText())) {
                            results.add(entry);
                        }
                    }
                    // Even if entry not found, we've identified the enum — stop climbing
                    return false;
                }
            }
            return true;
        });

        return results;
    }

    // ── §11.2 — Union Kind Entry Lookup ───────────────────────────────────────

    /**
     * Resolve {@code UnionName::Kind::entryName} to a union member declaration.
     * The K language synthesizes a Kind enum from union member names.
     */
    private static @NotNull List<PsiElement> resolveUnionKindEntry(@NotNull PsiElement anchor,
                                                                    @NotNull String[] segments) {
        String unionName = segments[0];
        String entryName = segments[2];

        List<PsiElement> results = new ArrayList<>();
        PsiTreeUtil.treeWalkUp(anchor, null, (scope, lastParent) -> {
            for (PsiElement child : getDirectChildren(scope)) {
                if (child instanceof KlangUnionDecl unionDecl && unionName.equals(unionDecl.getName())) {
                    // Found the union — look for a member with the entry name
                    for (KlangUnionMemberDecl member : unionDecl.getUnionMemberDeclList()) {
                        if (entryName.equals(member.getIdentifier().getText())) {
                            results.add(member);
                        }
                    }
                    return false; // stop climbing
                }
            }
            return true;
        });

        return results;
    }

    // ── §11.3 — Annotation RTTI Lookup ────────────────────────────────────────

    /**
     * Resolve {@code AnnotationName::annotation} to the annotation aggregate declaration.
     */
    private static @NotNull List<PsiElement> resolveAnnotationRtti(@NotNull PsiElement anchor,
                                                                    @NotNull String[] segments) {
        // The annotation name is everything before the last "annotation" segment
        if (segments.length < 2) return Collections.emptyList();
        String[] annNameSegments = Arrays.copyOfRange(segments, 0, segments.length - 1);

        // Resolve the annotation type name
        List<PsiElement> resolved;
        if (annNameSegments.length == 1) {
            resolved = resolveSimple(anchor, annNameSegments[0]);
        } else {
            resolved = qualifiedLookup(anchor, annNameSegments);
        }

        // Filter for annotation aggregates only
        List<PsiElement> results = new ArrayList<>();
        for (PsiElement el : resolved) {
            if (el instanceof KlangAggregateDecl agg) {
                if (isAnnotationType(agg)) {
                    results.add(agg);
                }
            }
        }
        return results;
    }

    /** Check if an aggregate is declared as an annotation type. */
    private static boolean isAnnotationType(@NotNull KlangAggregateDecl agg) {
        ASTNode node = agg.getNode();
        ASTNode kwNode = node.findChildByType(KlangTypes.KW_ANNOTATION);
        return kwNode != null;
    }

    // ── Scope collection (§5.5 — SIMPLE_LOOKUP) ──────────────────────────────

    /**
     * Collect all declarations named {@code name} visible at a single scope level.
     * Handles position-sensitive scopes (blocks) and inherited members if in aggregate.
     *
     * @param scope     the scope to search
     * @param lastParent the child of scope from which we climbed (for position boundary)
     * @param name      the name to find
     * @param results   accumulator for results
     * @param visited   to avoid infinite recursion
     * @param allOverloads if true, collect all function overloads without stopping
     */
    static void collectFromScope(@NotNull PsiElement scope,
                                 @Nullable PsiElement lastParent,
                                 @NotNull String name,
                                 @NotNull List<PsiElement> results,
                                 @NotNull Set<PsiElement> visited,
                                 boolean allOverloads) {
        if (!visited.add(scope)) return;

        if (scope instanceof KlangBlockStatement block) {
            // §3.4 — Block scope: only declarations *before* lastParent are visible
            for (KlangStatement stmt : block.getStatementList()) {
                if (stmt == lastParent) break;
                KlangVariableDecl vd = stmt.getVariableDecl();
                if (vd != null && name.equals(vd.getName())) {
                    results.add(vd);
                    if (!allOverloads) return;
                }
            }
        } else if (scope instanceof KlangForStatement forStmt) {
            // §3.5 — For-statement scope: init variable visible inside loop
            KlangVariableDecl vd = forStmt.getVariableDecl();
            if (vd != null && name.equals(vd.getName())) {
                results.add(vd);
            }
        } else if (scope instanceof KlangIfElseStatement ifStmt) {
            // If-condition variables are visible inside the if body
            collectIfCondVars(ifStmt, name, results);
        } else if (scope instanceof KlangCatchClause catchClause) {
            // catch (e: T) { … } — the catch parameter is visible inside the catch block
            KlangCatchParameterDecl param = catchClause.getCatchParameterDecl();
            if (param != null && name.equals(param.getName())) {
                results.add(param);
            }
        } else if (scope instanceof KlangFunctionDecl func) {
            // §3.3 — Function scope: parameters and named return var
            collectFunctionLocals(func, name, results);
            // Template / generic type parameters declared on this function.
            collectTemplateParams(func.getTemplateDeclaration(), name, results);
            collectTemplateParams(func.getGenericDeclaration() == null
                    ? null : func.getGenericDeclaration().getTemplateParameterList(), name, results);
        } else if (scope instanceof KlangAggregateDecl agg) {
            // §3.2 — Aggregate scope: member variables, methods, nested types
            collectFromDeclarationList(agg.getDeclarationList(), name, results, new HashSet<>(visited));
            // Template type parameters declared on this aggregate.
            collectTemplateParams(agg.getTemplateDeclaration(), name, results);
            // §8 — Inherited member lookup (BFS) if not found directly
            if (results.isEmpty()) {
                results.addAll(inheritedLookup(name, agg, agg));
            }
        } else if (scope instanceof KlangNamespaceDecl ns) {
            // §3.1 — Namespace scope: all declarations. M2: a namespace may be re-opened in
            // sibling files of the same module — merge every same-path namespace's members.
            for (KlangNamespaceDecl sibling : siblingNamespaces(ns)) {
                collectFromDeclarationList(sibling.getDeclarationList(), name, results, new HashSet<>(visited));
            }
        } else if (scope instanceof KlangFile file) {
            // Module root — top-level declarations across *all* files of the module (M2).
            for (KlangFile moduleFile : moduleFiles(file)) {
                for (PsiElement child : moduleFile.getChildren()) {
                    if (child instanceof KlangDeclaration decl) {
                        collectFromDeclaration(decl, name, results, new HashSet<>(visited));
                    }
                }
            }
        }
    }

    /**
     * Collect variables declared in if-condition variable declarations.
     */
    private static void collectIfCondVars(@NotNull KlangIfElseStatement ifStmt,
                                          @NotNull String name,
                                          @NotNull List<PsiElement> results) {
        for (PsiElement child : ifStmt.getChildren()) {
            if (child instanceof KlangIfCondVarDecl condVar) {
                ASTNode idNode = condVar.getNode().findChildByType(KlangTypes.IDENTIFIER);
                if (idNode != null && name.equals(idNode.getText())) {
                    results.add(condVar);
                }
            } else if (child instanceof KlangIfCondVarDeclList condVarList) {
                for (KlangIfCondVarDecl condVar : condVarList.getIfCondVarDeclList()) {
                    ASTNode idNode = condVar.getNode().findChildByType(KlangTypes.IDENTIFIER);
                    if (idNode != null && name.equals(idNode.getText())) {
                        results.add(condVar);
                    }
                }
            }
        }
    }

    /** Variant that only collects containers. Used for qualified resolution step 1. */
    static void collectContainersFromScope(@NotNull PsiElement scope,
                                           @Nullable PsiElement lastParent,
                                           @NotNull String name,
                                           @NotNull List<PsiElement> results,
                                           @NotNull Set<PsiElement> visited) {
        List<PsiElement> all = new ArrayList<>();
        collectFromScope(scope, lastParent, name, all, visited, false);
        for (PsiElement el : all) {
            if (isContainer(el)) results.add(el);
        }
    }

    // ── §5.6 / §9 — Using Directive Lookup ────────────────────────────────────

    /**
     * Process all using directives in a scope for a given name.
     *
     * @param scope      the scope containing using directives
     * @param lastParent position boundary for ordered scopes
     * @param simpleName the first segment of the name being looked up
     * @param fullSegments if non-null, the full qualified segments (for aliased namespace traversal)
     * @param results    accumulator
     * @param visited    to avoid infinite recursion
     */
    private static void collectFromUsings(@NotNull PsiElement scope,
                                          @Nullable PsiElement lastParent,
                                          @NotNull String simpleName,
                                          @Nullable String[] fullSegments,
                                          @NotNull List<PsiElement> results,
                                          @NotNull Set<PsiElement> visited) {
        List<KlangUsingDecl> usings = collectUsingDecls(scope, lastParent);
        for (KlangUsingDecl usingDecl : usings) {
            handleUsing(usingDecl, simpleName, fullSegments, results, visited, scope);
            if (!results.isEmpty()) return;
        }
    }

    /**
     * Collect all using declarations visible at a scope level.
     */
    private static @NotNull List<KlangUsingDecl> collectUsingDecls(@NotNull PsiElement scope,
                                                                    @Nullable PsiElement lastParent) {
        List<KlangUsingDecl> usings = new ArrayList<>();
        if (scope instanceof KlangBlockStatement block) {
            for (KlangStatement stmt : block.getStatementList()) {
                if (stmt == lastParent) break;
                KlangUsingDecl ud = stmt.getUsingDecl();
                if (ud != null) usings.add(ud);
            }
        } else if (scope instanceof KlangAggregateDecl agg) {
            for (KlangDeclaration decl : agg.getDeclarationList()) {
                KlangUsingDecl ud = decl.getUsingDecl();
                if (ud != null) usings.add(ud);
            }
        } else if (scope instanceof KlangNamespaceDecl ns) {
            // M2 — include using directives from the namespace re-opened in sibling files.
            for (KlangNamespaceDecl sibling : siblingNamespaces(ns)) {
                for (KlangDeclaration decl : sibling.getDeclarationList()) {
                    KlangUsingDecl ud = decl.getUsingDecl();
                    if (ud != null) usings.add(ud);
                }
            }
        } else if (scope instanceof KlangFile file) {
            // M2 — module-level using directives from every file of the module.
            for (KlangFile moduleFile : moduleFiles(file)) {
                for (PsiElement child : moduleFile.getChildren()) {
                    if (child instanceof KlangDeclaration decl) {
                        KlangUsingDecl ud = decl.getUsingDecl();
                        if (ud != null) usings.add(ud);
                    }
                }
            }
        }
        return usings;
    }

    /**
     * Process a single {@code using} directive (§9).
     *
     * <ul>
     *   <li>§9.1: {@code using namespace X;} → inject all content of X</li>
     *   <li>§9.2: {@code using Alias = namespace X;} → namespace accessible under alias</li>
     *   <li>§9.3: {@code using X::y;} → inject y from X (or with alias)</li>
     * </ul>
     */
    static void handleUsing(@NotNull KlangUsingDecl usingDecl,
                            @NotNull String name,
                            @Nullable String[] fullSegments,
                            @NotNull List<PsiElement> results,
                            @NotNull Set<PsiElement> visited,
                            @NotNull PsiElement currentScope) {
        if (!visited.add(usingDecl)) return;

        KlangUsingFilter filter  = usingDecl.getUsingFilter();
        PsiElement       aliasId = usingDecl.getIdentifier();
        String           target  = usingDecl.getQualifiedIdentifier().getText().trim();

        boolean isNamespaceUsing = filter != null && "namespace".equals(filter.getText());

        if (isNamespaceUsing && aliasId == null) {
            // §9.1 — using namespace X; → inject all content of X
            List<PsiElement> nsDecls = resolve(usingDecl, target);
            for (PsiElement ns : nsDecls) {
                if (ns instanceof KlangNamespaceDecl nsDecl) {
                    if (fullSegments != null && fullSegments.length > 1) {
                        // For qualified names, DOWN_LOOKUP the full segments from the namespace
                        downLookup(nsDecl, fullSegments, 0, results, new HashSet<>(visited));
                    } else {
                        // For simple names, search within the namespace
                        collectFromDeclarationList(nsDecl.getDeclarationList(), name,
                                results, new HashSet<>(visited));
                    }
                }
            }
        } else if (isNamespaceUsing && aliasId != null) {
            // §9.2 — using Alias = namespace X; → namespace accessible under alias
            String alias = aliasId.getText();
            if (fullSegments != null && fullSegments.length >= 1 && alias.equals(fullSegments[0])) {
                // Match: rest of segments resolved within target namespace
                List<PsiElement> nsDecls = resolve(usingDecl, target);
                for (PsiElement ns : nsDecls) {
                    if (ns instanceof KlangNamespaceDecl nsDecl) {
                        if (fullSegments.length == 1) {
                            results.add(nsDecl);
                        } else {
                            String[] rest = Arrays.copyOfRange(fullSegments, 1, fullSegments.length);
                            downLookup(nsDecl, rest, 0, results, new HashSet<>(visited));
                        }
                    }
                }
            } else if (name.equals(alias) && (fullSegments == null || fullSegments.length == 1)) {
                // Simple name matches alias — return the namespace itself
                results.addAll(resolve(usingDecl, target));
            }
        } else if (aliasId != null) {
            // §9.3 with alias — using Alias = X::y;
            String alias = aliasId.getText();
            if (name.equals(alias)) {
                results.addAll(resolve(usingDecl, target));
            } else if (fullSegments != null && fullSegments.length >= 1 && alias.equals(fullSegments[0])) {
                // Qualified name starting with alias: resolve target, then descend
                List<PsiElement> targetDecls = resolve(usingDecl, target);
                if (fullSegments.length == 1) {
                    results.addAll(targetDecls);
                } else {
                    String[] rest = Arrays.copyOfRange(fullSegments, 1, fullSegments.length);
                    for (PsiElement td : targetDecls) {
                        if (isContainer(td)) {
                            downLookup(td, rest, 0, results, new HashSet<>(visited));
                        }
                    }
                }
            }
        } else {
            // §9.3 without alias — using X::y; → alias is last segment of target
            String lastSeg = lastSegment(target);
            if (name.equals(lastSeg)) {
                results.addAll(resolve(usingDecl, target));
            } else if (fullSegments != null && fullSegments.length >= 1 && lastSeg.equals(fullSegments[0])) {
                // Qualified name starting with the injected name
                List<PsiElement> targetDecls = resolve(usingDecl, target);
                if (fullSegments.length == 1) {
                    results.addAll(targetDecls);
                } else {
                    String[] rest = Arrays.copyOfRange(fullSegments, 1, fullSegments.length);
                    for (PsiElement td : targetDecls) {
                        if (isContainer(td)) {
                            downLookup(td, rest, 0, results, new HashSet<>(visited));
                        }
                    }
                }
            }
        }
    }

    // ── Declaration-list traversal ────────────────────────────────────────────

    static void collectFromDeclarationList(@NotNull List<KlangDeclaration> decls,
                                           @NotNull String name,
                                           @NotNull List<PsiElement> results,
                                           @NotNull Set<PsiElement> visited) {
        for (KlangDeclaration decl : decls) {
            collectFromDeclaration(decl, name, results, visited);
        }
    }

    static void collectFromDeclaration(@NotNull KlangDeclaration decl,
                                       @NotNull String name,
                                       @NotNull List<PsiElement> results,
                                       @NotNull Set<PsiElement> visited) {
        KlangAggregateDecl agg = decl.getAggregateDecl();
        if (agg != null && name.equals(agg.getName())) { results.add(agg); return; }

        KlangEnumDecl enumDecl = decl.getEnumDecl();
        if (enumDecl != null && name.equals(enumDecl.getName())) { results.add(enumDecl); return; }

        KlangUnionDecl unionDecl = decl.getUnionDecl();
        if (unionDecl != null && name.equals(unionDecl.getName())) { results.add(unionDecl); return; }

        KlangFunctionDecl func = decl.getFunctionDecl();
        if (func != null && name.equals(func.getName())) { results.add(func); return; }

        KlangVariableDecl var = decl.getVariableDecl();
        if (var != null && name.equals(var.getName())) { results.add(var); return; }

        KlangNamespaceDecl ns = decl.getNamespaceDecl();
        if (ns != null) {
            PsiElement id = ns.getIdentifier();
            if (id != null && name.equals(id.getText())) { results.add(ns); return; }
        }
    }

    /** Collect parameters and named return variable from a function declaration. */
    static void collectFunctionLocals(@NotNull KlangFunctionDecl func,
                                      @NotNull String name,
                                      @NotNull List<PsiElement> results) {
        // Parameters
        KlangParameterList params = func.getParameterList();
        if (params != null) {
            for (KlangParameterSpec ps : params.getParameterSpecList()) {
                PsiElement id = parameterNameIdentifier(ps);
                if (id != null && name.equals(id.getText())) {
                    results.add(ps);
                }
            }
        }
        // Named return variable
        KlangNamedReturnVar rv = func.getNamedReturnVar();
        if (rv != null && name.equals(rv.getIdentifier().getText())) {
            results.add(rv);
        }
    }

    /** Collect matching template type parameters declared by a {@code template<…>} block. */
    static void collectTemplateParams(@Nullable KlangTemplateDeclaration td,
                                      @NotNull String name,
                                      @NotNull List<PsiElement> results) {
        if (td != null) collectTemplateParams(td.getTemplateParameterList(), name, results);
    }

    /** Collect matching template type parameters from a parameter list ({@code template}/{@code generic}). */
    static void collectTemplateParams(@Nullable KlangTemplateParameterList list,
                                      @NotNull String name,
                                      @NotNull List<PsiElement> results) {
        if (list == null) return;
        for (KlangTemplateParameter p : list.getTemplateParameterList()) {
            if (name.equals(p.getName())) results.add(p);
        }
    }

    /** Match a declaration by name and return the concrete inner element, or null. */
    private static @Nullable PsiElement matchDeclarationByName(@NotNull KlangDeclaration decl,
                                                               @NotNull String name) {
        KlangAggregateDecl agg = decl.getAggregateDecl();
        if (agg != null && name.equals(agg.getName())) return agg;

        KlangEnumDecl enumDecl = decl.getEnumDecl();
        if (enumDecl != null && name.equals(enumDecl.getName())) return enumDecl;

        KlangUnionDecl unionDecl = decl.getUnionDecl();
        if (unionDecl != null && name.equals(unionDecl.getName())) return unionDecl;

        KlangFunctionDecl func = decl.getFunctionDecl();
        if (func != null && name.equals(func.getName())) return func;

        KlangVariableDecl var = decl.getVariableDecl();
        if (var != null && name.equals(var.getName())) return var;

        KlangNamespaceDecl ns = decl.getNamespaceDecl();
        if (ns != null) {
            PsiElement id = ns.getIdentifier();
            if (id != null && name.equals(id.getText())) return ns;
        }

        return null;
    }

    // ── Direct children of a container ────────────────────────────────────────

    /**
     * Returns the direct named children of a container scope, unwrapping
     * {@link KlangDeclaration} wrappers.  Used by downLookup.
     */
    static @NotNull List<PsiElement> getDirectChildren(@NotNull PsiElement container) {
        List<PsiElement> result = new ArrayList<>();
        if (container instanceof KlangFile file) {
            // M2 — the module root spans all files of the module: merge their top-level decls.
            for (KlangFile moduleFile : moduleFiles(file)) {
                for (PsiElement child : moduleFile.getChildren()) {
                    if (child instanceof KlangDeclaration decl) unwrapDecl(decl, result);
                }
            }
        } else if (container instanceof KlangNamespaceDecl ns) {
            // M2 — merge a re-opened namespace's declarations across sibling files.
            for (KlangNamespaceDecl sibling : siblingNamespaces(ns)) {
                for (KlangDeclaration decl : sibling.getDeclarationList()) unwrapDecl(decl, result);
            }
        } else if (container instanceof KlangAggregateDecl agg) {
            for (KlangDeclaration decl : agg.getDeclarationList()) unwrapDecl(decl, result);
        } else if (container instanceof KlangEnumDecl enumDecl) {
            result.addAll(enumDecl.getEnumEntryList());
        } else if (container instanceof KlangUnionDecl unionDecl) {
            result.addAll(unionDecl.getUnionMemberDeclList());
        }
        return result;
    }

    /** Unwrap a {@link KlangDeclaration} and add the concrete inner element. */
    static void unwrapDecl(@NotNull KlangDeclaration decl, @NotNull List<PsiElement> result) {
        if      (decl.getAggregateDecl()  != null) result.add(decl.getAggregateDecl());
        else if (decl.getEnumDecl()       != null) result.add(decl.getEnumDecl());
        else if (decl.getUnionDecl()      != null) result.add(decl.getUnionDecl());
        else if (decl.getFunctionDecl()   != null) result.add(decl.getFunctionDecl());
        else if (decl.getVariableDecl()   != null) result.add(decl.getVariableDecl());
        else if (decl.getNamespaceDecl()  != null) result.add(decl.getNamespaceDecl());
        else if (decl.getUsingDecl()      != null) result.add(decl.getUsingDecl());
    }

    // ── Predicates ────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the element is a "container" — it can hold sub-declarations
     * and act as a prefix in a qualified identifier.
     */
    static boolean isContainer(@NotNull PsiElement el) {
        return el instanceof KlangNamespaceDecl
            || el instanceof KlangAggregateDecl
            || el instanceof KlangEnumDecl
            || el instanceof KlangUnionDecl;
    }

    // ── Name extraction ───────────────────────────────────────────────────────

    /**
     * Returns the declared name of a PSI element, or {@code null} if it lacks a simple name.
     */
    @Nullable
    public static String nameOf(@NotNull PsiElement el) {
        if (el instanceof KlangNamedElement ne) return ne.getName();
        if (el instanceof KlangNamespaceDecl ns) {
            PsiElement id = ns.getIdentifier();
            return id != null ? id.getText() : null;
        }
        if (el instanceof KlangParameterSpec ps) {
            PsiElement id = parameterNameIdentifier(ps);
            return id != null ? id.getText() : null;
        }
        if (el instanceof KlangNamedReturnVar rv) {
            return rv.getIdentifier().getText();
        }
        if (el instanceof KlangEnumEntry entry) {
            ASTNode id = entry.getNode().findChildByType(KlangTypes.IDENTIFIER);
            return id != null ? id.getText() : null;
        }
        if (el instanceof KlangUnionMemberDecl member) {
            return member.getIdentifier().getText();
        }
        if (el instanceof KlangIfCondVarDecl condVar) {
            ASTNode id = condVar.getNode().findChildByType(KlangTypes.IDENTIFIER);
            return id != null ? id.getText() : null;
        }
        return null;
    }

    /**
     * Returns the declared name identifier of a parameter, or {@code null} for an
     * anonymous parameter.
     *
     * <p>Since {@code parameterSpec} references {@code IDENTIFIER} more than once
     * (leading {@code name :} form and trailing {@code Type... name} pack form),
     * GrammarKit no longer generates a single {@code getIdentifier()} accessor.
     * The parameter name is the first direct {@code IDENTIFIER} child token — the
     * type's own identifiers are nested inside the {@code typeSpec} child node and
     * are therefore not matched here.</p>
     */
    @Nullable
    public static PsiElement parameterNameIdentifier(@NotNull KlangParameterSpec ps) {
        ASTNode id = ps.getNode().findChildByType(KlangTypes.IDENTIFIER);
        return id != null ? id.getPsi() : null;
    }

    // ── Module utilities ──────────────────────────────────────────────────────

    /**
     * Returns the module name segments from the file's {@code module} declaration.
     * For {@code module demo::colors;} returns {@code ["demo", "colors"]}.
     * Returns an empty array if no module declaration is present.
     */
    public static @NotNull String[] getModuleName(@NotNull PsiFile file) {
        KlangModuleDeclaration mod = PsiTreeUtil.getChildOfType(file, KlangModuleDeclaration.class);
        if (mod == null) return new String[0];
        String text = mod.getQualifiedIdentifier().getText().trim();
        if (text.startsWith("::")) text = text.substring(2);
        return Arrays.stream(text.split("::"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    // ── Multi-file module root (M2) ───────────────────────────────────────────

    /**
     * The set of {@link KlangFile}s that form {@code element}'s module root, the anchor's own
     * file first (so same-file shadowing keeps priority). Resilient: if the module index is
     * not ready (dumb mode) or the model is unavailable, it degrades to the single containing
     * file so resolution never throws.
     */
    private static @NotNull List<KlangFile> moduleFiles(@NotNull PsiElement element) {
        PsiFile pf = element.getContainingFile();
        KlangFile self = pf instanceof KlangFile ? (KlangFile) pf : null;
        try {
            List<KlangFile> files = KlangModuleScope.moduleRootFiles(element);
            if (!files.isEmpty()) return files;
        } catch (IndexNotReadyException ignored) {
            // index still building — fall back to the single file
        }
        return self != null ? Collections.singletonList(self) : Collections.emptyList();
    }

    /**
     * The module-name segments that apply to {@code file} after the project module policy
     * (A/B/C) is taken into account — e.g. under policy C a file with no {@code module}
     * declaration still belongs to the single declared module. Falls back to the file-local
     * {@code module} declaration when the model is unavailable.
     */
    private static @NotNull String[] effectiveModuleName(@NotNull PsiFile file) {
        if (file instanceof KlangFile kf) {
            try {
                String name = KlangModuleModel.getInstance(file.getProject()).moduleNameOf(kf);
                return Arrays.stream(name.split("::"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);
            } catch (IndexNotReadyException ignored) {
                // fall through to the file-local module declaration
            }
        }
        return getModuleName(file);
    }

    /**
     * All namespace declarations that share {@code ns}'s qualified path across the files of the
     * module (the K "re-opened namespace" rule), including {@code ns} itself. Anonymous
     * namespaces (or namespaces nested inside an aggregate rather than the module root) are not
     * merged across files — the singleton {@code [ns]} is returned.
     */
    private static @NotNull List<KlangNamespaceDecl> siblingNamespaces(@NotNull KlangNamespaceDecl ns) {
        // Build the path of namespace names from the module root down to ns.
        Deque<String> path = new ArrayDeque<>();
        PsiElement current = ns;
        while (current instanceof KlangNamespaceDecl n) {
            PsiElement id = n.getIdentifier();
            if (id == null) return Collections.singletonList(ns); // anonymous — not merged
            path.addFirst(id.getText());
            PsiElement parent = n.getParent();
            if (parent instanceof KlangDeclaration) parent = parent.getParent();
            current = parent;
        }
        if (!(current instanceof KlangFile)) {
            return Collections.singletonList(ns); // not a module-root namespace
        }

        String[] segments = path.toArray(new String[0]);
        List<KlangNamespaceDecl> result = new ArrayList<>();
        for (KlangFile moduleFile : moduleFiles(ns)) {
            collectNamespacesByPath(moduleFile, segments, 0, result);
        }
        if (!result.contains(ns)) result.add(ns); // always include the origin namespace
        return result;
    }

    /** Recursively collect namespaces matching {@code segments[from..]} under {@code container}. */
    private static void collectNamespacesByPath(@NotNull PsiElement container,
                                                @NotNull String[] segments, int from,
                                                @NotNull List<KlangNamespaceDecl> out) {
        if (from >= segments.length) return;
        for (KlangDeclaration decl : topLevelDeclarations(container)) {
            KlangNamespaceDecl n = decl.getNamespaceDecl();
            if (n == null) continue;
            PsiElement id = n.getIdentifier();
            if (id == null || !segments[from].equals(id.getText())) continue;
            if (from == segments.length - 1) out.add(n);
            else collectNamespacesByPath(n, segments, from + 1, out);
        }
    }

    /** The {@link KlangDeclaration} children of a single file or namespace (never merged). */
    private static @NotNull List<KlangDeclaration> topLevelDeclarations(@NotNull PsiElement container) {
        if (container instanceof KlangNamespaceDecl ns) return ns.getDeclarationList();
        if (container instanceof KlangFile file) {
            List<KlangDeclaration> decls = new ArrayList<>();
            for (PsiElement child : file.getChildren()) {
                if (child instanceof KlangDeclaration decl) decls.add(decl);
            }
            return decls;
        }
        return Collections.emptyList();
    }

    // ── §5.8 — External module fallback (imports, M3) ─────────────────────────

    /**
     * EXTERNAL_LOOKUP (§5.8): once resolution within the current module root fails, resolve
     * {@code segments} against the modules visible from the current file.
     *
     * <p>An {@code import M;} grants <b>qualified access only</b>: the name must be prefixed
     * with the imported module's name ({@code M::x} / {@code ::M::x}), which is then stripped
     * before descending into {@code M}. Importing a module does <b>not</b> inject its symbols
     * unqualified — that requires a {@code using} directive (§9), which is handled earlier in
     * the scope climb. The leading {@code ::} of an absolute name is already removed by the
     * caller, so both forms reach this method as the module-prefixed segment list.</p>
     *
     * <p>The standard library {@code k} is the sole exception: it is <b>auto-imported</b>, so a
     * name targeting the {@code k} namespace resolves against the project's {@code k}-rooted
     * modules even without an explicit {@code import}.</p>
     *
     * <p>Fails soft: unknown modules (e.g. an absent {@code k} standard library) yield nothing.</p>
     */
    private static @NotNull List<PsiElement> resolveExternal(@NotNull PsiElement anchor,
                                                             @NotNull String[] segments) {
        PsiFile file = anchor.getContainingFile();
        if (!(file instanceof KlangFile)) return Collections.emptyList();

        List<PsiElement> results = new ArrayList<>();
        Set<String> searched = new HashSet<>();

        // (1) Explicitly imported modules — qualified access only.
        for (String[] modSegs : importedModuleSegments(file)) {
            searchModuleByPrefix(anchor, modSegs, segments, results, searched);
            if (!results.isEmpty()) return results;
        }

        // (2) Auto-imported standard library: a name in the 'k' namespace needs no import.
        if (segments.length >= 1 && LIBK_ROOT.equals(segments[0])) {
            for (String[] modSegs : libkModuleSegments(anchor.getProject())) {
                searchModuleByPrefix(anchor, modSegs, segments, results, searched);
                if (!results.isEmpty()) return results;
            }
        }

        return results;
    }

    /**
     * Resolve {@code segments} against the module named by {@code modSegs} via the
     * <em>qualified</em> form only: the name must start with the module name, which is stripped
     * before a pure downward descent. Names that are not prefixed by the module name (including
     * simple, unqualified names) are not matched — an {@code import} never grants unqualified
     * access (§5.8). {@code searched} guards against re-scanning the same module.
     */
    private static void searchModuleByPrefix(@NotNull PsiElement anchor,
                                             @NotNull String[] modSegs,
                                             @NotNull String[] segments,
                                             @NotNull List<PsiElement> results,
                                             @NotNull Set<String> searched) {
        String moduleName = String.join("::", modSegs);
        if (moduleName.isEmpty() || !searched.add(moduleName)) return;

        String[] stripped = stripModulePrefix(segments, modSegs);
        if (stripped == segments) return; // not prefixed by the module name → no access

        List<KlangFile> modFiles;
        try {
            modFiles = KlangModuleScope.filesOfModule(anchor.getProject(), moduleName);
        } catch (IndexNotReadyException ignored) {
            return; // index building — fail soft
        }
        if (modFiles.isEmpty()) return;
        // getDirectChildren(rep) merges every file of the imported module (M2), so one
        // representative file is enough to descend the imported module's merged root.
        downLookup(modFiles.get(0), stripped, 0, results, new HashSet<>());
    }

    /** The libk module names (root namespace {@code k}) declared in the project, as segments. */
    private static @NotNull List<String[]> libkModuleSegments(@NotNull Project project) {
        List<String[]> result = new ArrayList<>();
        Set<String> names;
        try {
            names = KlangModuleModel.getInstance(project).allModuleNames();
        } catch (IndexNotReadyException ignored) {
            return result; // index building — fail soft
        }
        for (String name : names) {
            if (name.equals(LIBK_ROOT) || name.startsWith(LIBK_ROOT + "::")) {
                String[] segs = Arrays.stream(name.split("::"))
                        .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
                if (segs.length > 0) result.add(segs);
            }
        }
        return result;
    }

    /** The module names imported by {@code file}, each split into {@code ::}-separated segments. */
    private static @NotNull List<String[]> importedModuleSegments(@NotNull PsiFile file) {
        List<String[]> result = new ArrayList<>();
        for (KlangImportDeclaration imp : PsiTreeUtil.findChildrenOfType(file, KlangImportDeclaration.class)) {
            String text = imp.getQualifiedIdentifier().getText().trim();
            if (text.startsWith("::")) text = text.substring(2);
            String[] segs = Arrays.stream(text.split("::"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            if (segs.length > 0) result.add(segs);
        }
        return result;
    }

    /**
     * The {@code module} declaration(s) of the module named {@code moduleName} across the project
     * — the navigation target for an {@code import} reference. Empty when the module is unknown
     * (e.g. the standard library is not present in the project).
     */
    public static @NotNull List<KlangModuleDeclaration> findModuleDeclarations(@NotNull PsiElement anchor,
                                                                              @NotNull String moduleName) {
        String name = moduleName.trim();
        if (name.startsWith("::")) name = name.substring(2);
        name = String.join("::", Arrays.stream(name.split("::"))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new));
        if (name.isEmpty()) return Collections.emptyList();

        List<KlangModuleDeclaration> result = new ArrayList<>();
        try {
            for (KlangFile f : KlangModuleScope.filesOfModule(anchor.getProject(), name)) {
                KlangModuleDeclaration mod = PsiTreeUtil.getChildOfType(f, KlangModuleDeclaration.class);
                if (mod != null) result.add(mod);
            }
        } catch (IndexNotReadyException ignored) {
            // index building — fail soft
        }
        return result;
    }

    /**
     * If {@code segments} starts with {@code moduleName}, returns the remaining tail;
     * otherwise returns {@code segments} unchanged.
     */
    static @NotNull String[] stripModulePrefix(@NotNull String[] segments,
                                               @NotNull String[] moduleName) {
        if (moduleName.length == 0 || segments.length <= moduleName.length) return segments;
        for (int i = 0; i < moduleName.length; i++) {
            if (!moduleName[i].equals(segments[i])) return segments;
        }
        return Arrays.copyOfRange(segments, moduleName.length, segments.length);
    }

    /** Returns the last {@code ::}-separated segment of a qualified name text. */
    static @NotNull String lastSegment(@NotNull String qualText) {
        int idx = qualText.lastIndexOf("::");
        return idx >= 0 ? qualText.substring(idx + 2).trim() : qualText.trim();
    }

    /**
     * Computes the text range (relative to {@code text}) of the last {@code ::}-separated
     * segment's <em>identifier</em>, ignoring {@code ::} that appear inside template-argument
     * groups ({@code <...>}) and excluding the last segment's own template-argument list.
     *
     * <p>So {@code Box<Point>} → {@code Box}, {@code shapes::Point} → {@code Point},
     * {@code Box<shapes::Point>} → {@code Box} (the inner {@code shapes::Point} keeps its own
     * nested reference) and {@code outer::Vector<int>} → {@code Vector}. Keeping the outer
     * type name on its own range frees the inner argument range for its dedicated reference,
     * so both the outer type and each inner type argument are independently navigable.</p>
     */
    public static @NotNull TextRange lastSegmentRange(@NotNull String text) {
        if (text.isEmpty()) return TextRange.EMPTY_RANGE;
        // Start: just after the last depth-0 "::" (template-argument "::" are ignored).
        int depth = 0;
        int start = 0;
        for (int i = 0; i + 1 < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                if (depth > 0) depth--;
            } else if (depth == 0 && c == ':' && text.charAt(i + 1) == ':') {
                start = i + 2;
                i++; // skip the second ':'
            }
        }
        // Skip any whitespace before the last segment's identifier.
        while (start < text.length() && Character.isWhitespace(text.charAt(start))) start++;
        // End: the first depth-0 '<' at/after start (the last segment's template args), else end.
        int end = text.length();
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) == '<') { end = i; break; }
        }
        while (end > start && Character.isWhitespace(text.charAt(end - 1))) end--;
        return new TextRange(start, end);
    }

    /**
     * Splits a (possibly absolute / templated) qualified name into its {@code ::}-separated
     * segments, returning one descriptor per segment as {@code [idStart, idEnd, prefixEnd]}:
     *
     * <ul>
     *   <li>{@code [idStart, idEnd)} — the segment <em>identifier</em>'s range (trailing
     *       template arguments excluded), used to highlight / rename that single component;</li>
     *   <li>{@code prefixEnd} — the offset just past the whole segment (before the following
     *       {@code ::}), so {@code text.substring(0, prefixEnd)} is the qualified prefix that
     *       <em>resolves</em> this segment (e.g. {@code Policy} for the first part of
     *       {@code Policy::RUNTIME}).</li>
     * </ul>
     *
     * <p>{@code ::} appearing inside template-argument groups ({@code <...>}) is ignored, so
     * {@code Box<a::b>::iterator} yields the two segments {@code Box} and {@code iterator}.
     * A leading {@code ::} (absolute name) is skipped — it does not start an empty segment.</p>
     */
    public static @NotNull List<int[]> segmentBounds(@NotNull String text) {
        List<int[]> result = new ArrayList<>();
        int len = text.length();
        int i = text.startsWith("::") ? 2 : 0;
        int segStart = i;
        int depth = 0;
        for (; i < len; i++) {
            char c = text.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                if (depth > 0) depth--;
            } else if (depth == 0 && c == ':' && i + 1 < len && text.charAt(i + 1) == ':') {
                addSegment(text, segStart, i, result);
                i++;                // skip the second ':'
                segStart = i + 1;
            }
        }
        addSegment(text, segStart, len, result);
        return result;
    }

    /** Appends a {@code [idStart, idEnd, prefixEnd]} descriptor for {@code text[segStart, prefixEnd)}. */
    private static void addSegment(@NotNull String text, int segStart, int prefixEnd,
                                   @NotNull List<int[]> out) {
        int s = segStart;
        while (s < prefixEnd && Character.isWhitespace(text.charAt(s))) s++;
        int idEnd = prefixEnd;
        for (int j = s; j < prefixEnd; j++) {
            if (text.charAt(j) == '<') { idEnd = j; break; }
        }
        while (idEnd > s && Character.isWhitespace(text.charAt(idEnd - 1))) idEnd--;
        if (idEnd > s) out.add(new int[]{s, idEnd, prefixEnd});
    }

    /**
     * Removes balanced {@code <...>} template-argument groups from a name, so a templated
     * type or scope name can be resolved to its (untemplated) declaration. Depth-aware, so
     * nested arguments such as {@code Map<K, Vector<V>>} are removed wholesale.
     */
    static @NotNull String stripTemplateArgs(@NotNull String s) {        if (s.indexOf('<') < 0) return s;
        StringBuilder sb = new StringBuilder(s.length());
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                if (depth > 0) depth--;
            } else if (depth == 0) {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    // ── Completion candidates ─────────────────────────────────────────────────

    /**
     * Returns all named elements reachable from the anchor's position (for code completion).
     * Includes elements from the scope chain, inherited members, and using directives.
     */
    public static @NotNull List<PsiElement> getAllCandidates(@NotNull PsiElement anchor) {
        PsiFile file = anchor.getContainingFile();
        if (file == null) return Collections.emptyList();
        List<PsiElement> results = new ArrayList<>();
        collectAllFromScope(file, results, new HashSet<>());
        return results;
    }

    static void collectAllFromScope(@NotNull PsiElement scope,
                                    @NotNull List<PsiElement> results,
                                    @NotNull Set<PsiElement> visited) {
        if (!visited.add(scope)) return;
        for (PsiElement child : getDirectChildren(scope)) {
            if (nameOf(child) != null) results.add(child);
            if (isContainer(child)) collectAllFromScope(child, results, visited);
        }
    }
}


