package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangDestructorHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Gutter line markers for constructor and destructor declarations.
 *
 * <p>Adds visual indicators (gutter icons) on constructor and destructor declarations
 * to help users quickly identify these special methods:
 * <ul>
 *   <li><b>Constructors:</b> function whose name matches its enclosing aggregate name.
 *       Shows a "constructor" icon for quick visual recognition.</li>
 *   <li><b>Destructors:</b> functions with the destructor syntax (~Name).
 *       Shows a "destructor" icon.</li>
 * </ul>
 *
 * <p>Markers are attached to the leaf name {@code IDENTIFIER} of the declaration, as required by
 * the platform's "line marker must target a leaf element" contract.</p>
 */
public final class KlangConstructorDestructorLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public @NotNull String getName() {
        return KlangUi.Text.MARKER_PROVIDER_CTOR_DTOR;
    }

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Only react on leaf identifier tokens (the declaration's name carrier).
        if (element.getNode() == null || element.getNode().getElementType() != KlangTypes.IDENTIFIER) {
            return;
        }

        // Check if this is a destructor identifier
        KlangDestructorHead destructor = PsiTreeUtil.getParentOfType(element, KlangDestructorHead.class);
        if (destructor != null && element.equals(destructor.getIdentifier())) {
            KlangFunctionDecl owner = PsiTreeUtil.getParentOfType(destructor, KlangFunctionDecl.class);
            addDestructorMarker(owner != null ? owner : element, element, result);
            return;
        }

        // Check if this is a constructor identifier
        KlangFunctionHead funcHead = PsiTreeUtil.getParentOfType(element, KlangFunctionHead.class);
        if (funcHead != null && element.equals(funcHead.getIdentifier())) {
            KlangAggregateDecl enclosing = PsiTreeUtil.getParentOfType(funcHead, KlangAggregateDecl.class);
            if (enclosing != null && funcHead.getIdentifier().getText().equals(enclosing.getIdentifier().getText())) {
                KlangFunctionDecl owner = PsiTreeUtil.getParentOfType(funcHead, KlangFunctionDecl.class);
                addConstructorMarker(owner != null ? owner : element, element, result);
            }
        }
    }

    private static void addConstructorMarker(@NotNull PsiElement target,
                                             @NotNull PsiElement anchor,
                                             @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                .create(KlangUi.Icons.GUTTER_CTOR_DTOR)
                .setTargets(List.of(target))
                .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                .setTooltipText(KlangUi.Text.TOOLTIP_CONSTRUCTOR)
                .createLineMarkerInfo(anchor);
        result.add(info);
    }

    private static void addDestructorMarker(@NotNull PsiElement target,
                                            @NotNull PsiElement anchor,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                .create(KlangUi.Icons.GUTTER_CTOR_DTOR)
                .setTargets(List.of(target))
                .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                .setTooltipText(KlangUi.Text.TOOLTIP_DESTRUCTOR)
                .createLineMarkerInfo(anchor);
        result.add(info);
    }
}
