package com.github.emilienkia.klang.plugin.language.structure;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Structure-view model for K-lang files.
 *
 * <p>Offers an alphabetical sorter (declaration order otherwise), marks leaf vs. expandable
 * nodes, and wires {@link KlangNamedElement} as the suitable class so the outline auto-scrolls
 * to follow the caret.</p>
 */
final class KlangStructureViewModel extends StructureViewModelBase
        implements StructureViewModel.ElementInfoProvider {

    KlangStructureViewModel(@NotNull PsiFile psiFile, Editor editor) {
        super(psiFile, editor, new KlangStructureViewElement(psiFile));
        withSuitableClasses(KlangNamedElement.class);
    }

    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{ Sorter.ALPHA_SORTER };
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        Object value = element.getValue();
        return value instanceof KlangNamespaceDecl
                || value instanceof KlangAggregateDecl
                || value instanceof KlangEnumDecl
                || value instanceof KlangUnionDecl;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        Object value = element.getValue();
        return value instanceof KlangFunctionDecl
                || value instanceof KlangVariableDecl
                || value instanceof KlangEnumEntry
                || value instanceof KlangUnionMemberDecl;
    }
}



