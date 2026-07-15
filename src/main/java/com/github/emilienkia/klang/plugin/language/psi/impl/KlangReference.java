package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * PSI reference attached to a K-lang {@code identifierExpr}.
 *
 * <p>Delegates all resolution logic to {@link KlangResolveUtil}, which implements
 * the full K-lang symbol resolution algorithm:
 * <ul>
 *   <li><b>Simple symbol</b> (single identifier, no {@code ::} prefix):
 *       ascending scope walk with shadowing rule.</li>
 *   <li><b>Absolute qualified</b> (leading {@code ::}):
 *       strips optional module-name prefix, then descends from file root.</li>
 *   <li><b>Relative qualified</b> (multi-segment, no {@code ::} prefix):
 *       ascending search for first segment as a container, then descending.</li>
 * </ul>
 * {@code using} directives are honoured at each scope level.</p>
 */
public class KlangReference extends PsiReferenceBase.Poly<PsiElement>
        implements PsiPolyVariantReference {

    public KlangReference(@NotNull PsiElement element) {
        super(element, computeRange(element), false);
    }

    // ── PsiPolyVariantReference ───────────────────────────────────────────────

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        String refText = myElement.getText();
        if (refText == null || refText.isBlank()) return ResolveResult.EMPTY_ARRAY;

        List<PsiElement> found = KlangResolveUtil.resolve(myElement, refText.trim());
        // A type name used as the callee of a call (Name(args)) is a constructor call:
        // reference the constructor, not the aggregate type (§ type/constructor split).
        if (KlangResolveUtil.isExpressionConstructorCall(myElement)) {
            found = KlangResolveUtil.preferConstructors(found);
        }
        return found.stream()
                .map(PsiElementResolveResult::new)
                .toArray(ResolveResult[]::new);
    }

    // ── PsiReferenceBase ──────────────────────────────────────────────────────

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        // Expose all reachable named declarations (including nested) for completion
        return KlangResolveUtil.getAllCandidates(myElement).stream()
                .filter(el -> KlangResolveUtil.nameOf(el) != null)
                .toArray();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName)
            throws IncorrectOperationException {
        // Replaces the last identifier segment (the range computed by computeRange)
        return super.handleElementRename(newElementName);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Computes the range within the element that constitutes the reference.
     * For a qualified identifier like {@code k::io::print} we highlight only the last
     * segment {@code print}; for a templated name like {@code Box<Point>} we highlight only
     * the outer identifier {@code Box} (ignoring {@code ::} inside the {@code <...>} arguments)
     * so each inner type argument keeps its own dedicated, separately-navigable reference.
     */
    private static TextRange computeRange(PsiElement element) {
        String text = element.getText();
        if (text == null) return TextRange.EMPTY_RANGE;
        return KlangResolveUtil.lastSegmentRange(text);
    }
}
