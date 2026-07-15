package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangEnumDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangTemplateParameter;
import com.github.emilienkia.klang.plugin.language.psi.KlangUnionDecl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PSI reference attached to a {@code qualifiedIdentifier} that appears in a
 * <em>type</em> or <em>directive</em> position (Phase 1):
 *
 * <ul>
 *   <li>{@code typeSpec} / {@code typeName} — types in variable / parameter / return /
 *       field declarations, casts, {@code new} expressions, type lists.</li>
 *   <li>{@code baseSpec} — base class / interface list.</li>
 *   <li>{@code annotationDef} — the {@code @Type} annotation type.</li>
 *   <li>{@code usingDecl} — the using <em>target</em>.</li>
 *   <li>{@code friendDecl} — the friend target type or function.</li>
 *   <li>{@code staticDep} — static dependency list.</li>
 *   <li>{@code functionBody} — the {@code -> target} redirect (§14).</li>
 * </ul>
 *
 * <p>Resolution is delegated to {@link KlangResolveUtil#resolve(PsiElement, String)};
 * see {@code docs/klang/name-resolution.md}. References registered for the contributor
 * are attached only in the positions above (never on {@code identifierExpr}, which is
 * handled by {@link KlangIdentifierExprMixin}, nor on the module / import declarations).</p>
 *
 * <p>When {@code typesOnly} is set (pure type positions), the candidate set is narrowed
 * to type-like declarations (aggregate / enum / union / template parameter) so a
 * same-named variable or function does not win in a type context. If filtering would
 * remove every candidate, the unfiltered set is kept so navigation is never lost.</p>
 */
public class KlangQualifiedReference extends PsiReferenceBase.Poly<PsiElement>
        implements PsiPolyVariantReference {

    private final boolean typesOnly;

    public KlangQualifiedReference(@NotNull PsiElement element, boolean typesOnly) {
        super(element, computeRange(element), false);
        this.typesOnly = typesOnly;
    }

    // ── PsiPolyVariantReference ───────────────────────────────────────────────

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        String refText = myElement.getText();
        if (refText == null || refText.isBlank()) return ResolveResult.EMPTY_ARRAY;

        List<PsiElement> found = KlangResolveUtil.resolve(myElement, refText.trim());
        // A type name immediately initialised through a parenthesised constructor call
        // ("p : Point(args)") or a "new Point(...)" expression references the constructor,
        // not just the aggregate type. Both are *unambiguous type positions* (the name must
        // denote a type), so a same-named free function is **not** a candidate here — restrict
        // to type-like declarations before swapping in their constructors. A bare expression
        // call "Point(args)" stays ambiguous and is handled by KlangReference, which keeps
        // both the constructor and the same-named free function.
        if (KlangResolveUtil.isTypeInitConstructorCall(myElement)
                || KlangResolveUtil.isNewExprConstructorCall(myElement)) {
            return KlangResolveUtil.preferConstructors(restrictToTypes(found)).stream()
                    .map(PsiElementResolveResult::new)
                    .toArray(ResolveResult[]::new);
        }
        // An annotation with arguments (@Foo(args) or @Foo{...}) should prefer the
        // constructor if one exists, otherwise fall back to the type.
        if (KlangResolveUtil.isAnnotationConstructorCall(myElement)) {
            return KlangResolveUtil.preferConstructors(restrictToTypes(found)).stream()
                    .map(PsiElementResolveResult::new)
                    .toArray(ResolveResult[]::new);
        }
        if (typesOnly) {
            found = restrictToTypes(found);
        }
        return found.stream()
                .map(PsiElementResolveResult::new)
                .toArray(ResolveResult[]::new);
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) {
        return KlangPsiElementFactory.renameIdentifierAt(
                myElement, getRangeInElement().getStartOffset(), newName);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isTypeLike(@NotNull PsiElement el) {
        return el instanceof KlangAggregateDecl
            || el instanceof KlangEnumDecl
            || el instanceof KlangUnionDecl
            || el instanceof KlangTemplateParameter;
    }

    /**
     * Narrows {@code found} to type-like declarations (aggregate / enum / union / template
     * parameter), so a same-named variable or <em>free function</em> does not win in a type
     * context. If filtering would remove every candidate, the unfiltered set is kept so
     * navigation is never lost.
     */
    private static List<PsiElement> restrictToTypes(@NotNull List<PsiElement> found) {
        if (found.isEmpty()) return found;
        List<PsiElement> types = found.stream()
                .filter(KlangQualifiedReference::isTypeLike)
                .collect(Collectors.toList());
        return types.isEmpty() ? found : types;
    }

    /**
     * Highlights only the last {@code ::}-separated segment of the qualified name, so
     * navigation/rename target the final component (e.g. {@code Point} in
     * {@code shapes::Point}). For a templated name like {@code Box<Point>} only the outer
     * identifier {@code Box} is highlighted — {@code ::} appearing inside the {@code <...>}
     * arguments is ignored — so each inner type argument keeps its own dedicated,
     * separately-navigable reference (e.g. {@code Point} inside {@code Box<shapes::Point>}).
     */
    private static TextRange computeRange(@NotNull PsiElement element) {
        String text = element.getText();
        if (text == null) return TextRange.EMPTY_RANGE;
        return KlangResolveUtil.lastSegmentRange(text);
    }
}

