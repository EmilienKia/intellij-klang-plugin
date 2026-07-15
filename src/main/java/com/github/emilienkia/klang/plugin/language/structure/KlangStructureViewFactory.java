package com.github.emilienkia.klang.plugin.language.structure;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wires the K-lang file outline into the Structure tool window (Alt+7) and the
 * File Structure popup (Ctrl+F12). Registered via {@code lang.psiStructureViewFactory}.
 */
public final class KlangStructureViewFactory implements PsiStructureViewFactory {

    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        if (!(psiFile instanceof KlangFile)) {
            return null;
        }
        return new TreeBasedStructureViewBuilder() {
            @Override
            public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new KlangStructureViewModel(psiFile, editor);
            }
        };
    }
}

