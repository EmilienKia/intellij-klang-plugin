package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PSI reference for an <em>intermediate</em> {@code ::}-separated segment of a qualified name,
 * so each component is independently navigable (Go-to-Declaration / Find-Usages / rename).
 *
 * <p>For a name such as {@code Policy::RUNTIME} the <b>last</b> segment ({@code RUNTIME}) keeps
 * its existing reference ({@link KlangReference} / {@link KlangQualifiedReference}); every
 * earlier segment ({@code Policy}) gets one of these references, which resolves the qualified
 * <em>prefix</em> up to and including that segment (e.g. {@code Policy} →
 * {@code Retention::Policy}). Because the prefix is resolved through the normal
 * {@link KlangResolveUtil#resolve} entry point, it transparently benefits from the same
 * scope-chain, using-directive and annotation-argument rules as the whole name.</p>
 *
 * <p>An intermediate segment must denote a <em>container</em> (namespace / aggregate / enum /
 * union) since something follows it via {@code ::}; the candidate set is therefore narrowed to
 * containers, with a fail-soft fallback to the unfiltered set so navigation is never lost.</p>
 */
public class KlangSegmentReference extends PsiReferenceBase.Poly<PsiElement>
        implements PsiPolyVariantReference {

    private final String prefixText;

    public KlangSegmentReference(@NotNull PsiElement element,
                                 @NotNull TextRange range,
                                 @NotNull String prefixText) {
        super(element, range, false);
        this.prefixText = prefixText;
    }

    /**
     * Builds the per-segment reference array for {@code element}: a
     * {@link KlangSegmentReference} for every segment but the last, then {@code tail} for the
     * last segment. Returns {@code [tail]} for a single-segment (simple) name.
     */
    public static PsiReference @NotNull [] build(@NotNull PsiElement element,
                                                 @NotNull PsiReference tail) {
        String text = element.getText();
        if (text == null) return new PsiReference[]{tail};

        List<int[]> segments = KlangResolveUtil.segmentBounds(text);
        if (segments.size() <= 1) return new PsiReference[]{tail};

        List<PsiReference> refs = new ArrayList<>(segments.size());
        for (int i = 0; i < segments.size() - 1; i++) {
            int[] seg = segments.get(i);
            TextRange range = new TextRange(seg[0], seg[1]);
            String prefix = text.substring(0, seg[2]);
            refs.add(new KlangSegmentReference(element, range, prefix));
        }
        refs.add(tail);
        return refs.toArray(PsiReference.EMPTY_ARRAY);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        List<PsiElement> found = KlangResolveUtil.resolve(myElement, prefixText.trim());
        if (found.isEmpty()) return ResolveResult.EMPTY_ARRAY;

        List<PsiElement> containers = found.stream()
                .filter(KlangResolveUtil::isContainer)
                .collect(Collectors.toList());
        List<PsiElement> use = containers.isEmpty() ? found : containers;
        return use.stream()
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
}

