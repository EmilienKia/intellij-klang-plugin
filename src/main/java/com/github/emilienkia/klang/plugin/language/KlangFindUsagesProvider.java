package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.github.emilienkia.klang.plugin.language.psi.KlangTokenSets;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.ElementDescriptionUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.usageView.UsageViewLongNameLocation;
import com.intellij.usageView.UsageViewNodeTextLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enables the <em>Find Usages</em> action for K-lang named declarations.
 *
 * <p>The actual occurrence search is driven by the references already wired across the
 * language (see {@code KlangReference} / {@code KlangQualifiedReference} /
 * {@code KlangMemberReference} and the navigation phases in
 * {@code docs/klang/navigation-plan.md}). This provider supplies the two remaining pieces
 * the platform needs:</p>
 * <ul>
 *   <li>a {@link WordsScanner} so the index knows which tokens are identifiers, comments
 *       and literals (the candidate occurrences to verify against references), and</li>
 *   <li>the presentation strings (type, descriptive name, node text) shown in the
 *       <em>Find Usages</em> tool window — reused from
 *       {@link KlangElementDescriptionProvider} via {@link ElementDescriptionUtil} so the
 *       labels stay consistent with Rename.</li>
 * </ul>
 */
public class KlangFindUsagesProvider implements FindUsagesProvider {

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new KlangLexerAdapter(),
                KlangTokenSets.IDENTIFIERS,
                KlangTokenSets.COMMENTS,
                KlangTokenSets.LITERALS);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        // Every K-lang declaration (aggregate/function/variable/parameter/enum entry/…)
        // implements KlangNamedElement (PsiNameIdentifierOwner).
        return element instanceof KlangNamedElement;
    }

    @Override
    public @Nullable String getHelpId(@NotNull PsiElement element) {
        // Returning null lets the platform fall back to the generic Find Usages help.
        return null;
    }

    @Override
    public @NotNull String getType(@NotNull PsiElement element) {
        return ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
    }

    @Override
    public @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE);
    }

    @Override
    public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (useFullName && element instanceof KlangNamedElement) {
            return KlangNavigationPresentation.elementText(element);
        }
        if (element instanceof PsiNamedElement named) {
            String name = named.getName();
            if (name != null) {
                return name;
            }
        }
        return ElementDescriptionUtil.getElementDescription(element, UsageViewNodeTextLocation.INSTANCE);
    }
}



