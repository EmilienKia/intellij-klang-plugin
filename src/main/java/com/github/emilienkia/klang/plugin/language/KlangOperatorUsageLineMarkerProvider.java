package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangCastOperatorFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangOperatorFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangOperatorUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * <b>Reverse navigation for operator overloads (gutter markers).</b>
 *
 * <p>Operator overload <em>declarations</em> have no simple identifier name, so they are not
 * {@code KlangNamedElement}s and the standard Find-Usages cannot start from them. This provider
 * fills the gap with a gutter icon on each {@code operator <sym>} / {@code operator[]} declaration:
 * clicking it navigates to every usage ({@code a == b}, {@code -a}, {@code a[i]}, …) that resolves
 * back to that overload — the mirror image of {@link KlangOperatorGotoDeclarationHandler}, which
 * goes usage → definition.</p>
 *
 * <p>Usages are gathered project-wide by {@link KlangOperatorUtil#findOperatorUsages} (each
 * candidate token is resolved forward and kept when this declaration is among its targets, so the
 * two directions are exact inverses). The marker is attached to the {@code operator} keyword leaf
 * of the declaration head, honouring the platform's "line marker must target a leaf element"
 * contract. The cast {@code operator ()} is excluded (its usages are implicit conversion sites).</p>
 */
public final class KlangOperatorUsageLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public @NotNull String getName() {
        return KlangUi.Text.MARKER_PROVIDER_OPERATOR_USAGES;
    }

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // React only on the 'operator' keyword leaf of an operator-function head.
        if (element.getNode() == null || element.getNode().getElementType() != KlangTypes.KW_OPERATOR) {
            return;
        }
        PsiElement parent = element.getParent();
        // The cast operator 'operator ()' is a separate head and is out of scope here.
        if (parent instanceof KlangCastOperatorFunctionHead) return;
        if (!(parent instanceof KlangOperatorFunctionHead)) return;

        KlangFunctionDecl fn = PsiTreeUtil.getParentOfType(parent, KlangFunctionDecl.class);
        if (fn == null) return;
        String symbol = KlangOperatorUtil.operatorSymbolOf(fn);
        if (symbol == null) return;

        List<PsiElement> usages = KlangOperatorUtil.findOperatorUsages(fn);
        if (usages.isEmpty()) return;

        RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                .create(KlangUi.Icons.GUTTER_OPERATOR_USAGES)
                .setTargets(usages)
                .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                .setTooltipText(KlangUi.Text.TOOLTIP_GO_TO_OPERATOR_USAGES_PREFIX
                        + symbol
                        + KlangUi.Text.TOOLTIP_GO_TO_OPERATOR_USAGES_SUFFIX)
                .setPopupTitle(KlangUi.Text.POPUP_OPERATOR_USAGES_PREFIX
                        + symbol
                        + KlangUi.Text.POPUP_OPERATOR_USAGES_SUFFIX)
                .createLineMarkerInfo(element);
        result.add(info);
    }
}

