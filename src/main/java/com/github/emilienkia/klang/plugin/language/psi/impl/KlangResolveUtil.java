package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.lang.ASTNode;
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
            return resolveAbsolute(anchor, segments);
        } else if (segments.length == 1) {
            // §5.5 — Simple symbol: ascending resolution with using + climb
            return resolveSimple(anchor, segments[0]);
        } else {
            // §5.4 — Relative qualified: ascending for first segment, then descending
            // Also handles special forms (§11)
            return resolveQualified(anchor, segments);
        }
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

        String[] moduleName = getModuleName(file);

        // §6 Step 1: if first components match module name, strip them
        String[] withoutModule = stripModulePrefix(segments, moduleName);

        // §6 Step 2: DOWN_LOOKUP from file root
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
            // Resolve the base type name from the aggregate's context
            List<PsiElement> resolved = resolve(agg, baseText);
            for (PsiElement el : resolved) {
                if (el instanceof KlangAggregateDecl baseAgg) {
                    bases.add(baseAgg);
                }
            }
        }
        return bases;
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
        } else if (scope instanceof KlangFunctionDecl func) {
            // §3.3 — Function scope: parameters and named return var
            collectFunctionLocals(func, name, results);
        } else if (scope instanceof KlangAggregateDecl agg) {
            // §3.2 — Aggregate scope: member variables, methods, nested types
            collectFromDeclarationList(agg.getDeclarationList(), name, results, new HashSet<>(visited));
            // §8 — Inherited member lookup (BFS) if not found directly
            if (results.isEmpty()) {
                results.addAll(inheritedLookup(name, agg, agg));
            }
        } else if (scope instanceof KlangNamespaceDecl ns) {
            // §3.1 — Namespace scope: all declarations
            collectFromDeclarationList(ns.getDeclarationList(), name, results, new HashSet<>(visited));
        } else if (scope instanceof KlangFile) {
            // Module root — top-level declarations
            for (PsiElement child : scope.getChildren()) {
                if (child instanceof KlangDeclaration decl) {
                    collectFromDeclaration(decl, name, results, new HashSet<>(visited));
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
            for (KlangDeclaration decl : ns.getDeclarationList()) {
                KlangUsingDecl ud = decl.getUsingDecl();
                if (ud != null) usings.add(ud);
            }
        } else if (scope instanceof KlangFile) {
            for (PsiElement child : scope.getChildren()) {
                if (child instanceof KlangDeclaration decl) {
                    KlangUsingDecl ud = decl.getUsingDecl();
                    if (ud != null) usings.add(ud);
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
        if (container instanceof KlangFile) {
            for (PsiElement child : container.getChildren()) {
                if (child instanceof KlangDeclaration decl) unwrapDecl(decl, result);
            }
        } else if (container instanceof KlangNamespaceDecl ns) {
            for (KlangDeclaration decl : ns.getDeclarationList()) unwrapDecl(decl, result);
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
    static @NotNull String[] getModuleName(@NotNull PsiFile file) {
        KlangModuleDeclaration mod = PsiTreeUtil.getChildOfType(file, KlangModuleDeclaration.class);
        if (mod == null) return new String[0];
        String text = mod.getQualifiedIdentifier().getText().trim();
        if (text.startsWith("::")) text = text.substring(2);
        return Arrays.stream(text.split("::"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
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



