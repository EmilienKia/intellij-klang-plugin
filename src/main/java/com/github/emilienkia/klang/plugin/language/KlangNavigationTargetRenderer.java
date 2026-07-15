package com.github.emilienkia.klang.plugin.language;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Popup renderer for gutter-navigation targets.
 *
 * <p>Shows a fully-qualified declaration label in the main text, and the declaration kind + file
 * in the container text, so overloaded methods are distinguishable even when they share a simple
 * name.</p>
 */
final class KlangNavigationTargetRenderer extends PsiElementListCellRenderer<PsiElement> {

    static final @NotNull KlangNavigationTargetRenderer INSTANCE = new KlangNavigationTargetRenderer();

    private KlangNavigationTargetRenderer() {
    }

    @Override
    public @NotNull String getElementText(PsiElement element) {
        return KlangNavigationPresentation.elementText(element);
    }

    @Override
    public @Nullable String getContainerText(PsiElement element, String name) {
        return KlangNavigationPresentation.containerText(element);
    }

    @Override
    protected int getIconFlags() {
        return 0;
    }

    @Override
    protected @Nullable Icon getIcon(PsiElement element) {
        Icon icon = KlangNavigationPresentation.icon(element);
        return icon != null ? icon : super.getIcon(element);
    }
}
