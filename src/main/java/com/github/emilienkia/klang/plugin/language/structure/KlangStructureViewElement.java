package com.github.emilienkia.klang.plugin.language.structure;

import com.github.emilienkia.klang.plugin.language.KlangIcons;
import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in the K-lang structure view (file outline).
 *
 * <p>The root wraps the {@link KlangFile}; every other node wraps a navigable declaration
 * (namespace / aggregate / enum / union / function / variable / enum entry / union member).
 * Members are nested through {@link KlangDeclaration} wrapper nodes, which are unwrapped here.
 * Anonymous namespaces are <em>flattened</em>: their members surface at the parent level
 * (see TODO — a future toggle may restore them as explicit nodes).</p>
 */
final class KlangStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final PsiElement element;

    KlangStructureViewElement(@NotNull PsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        if (element instanceof PsiNamedElement named && named.getName() != null) {
            return named.getName();
        }
        if (element instanceof PsiFile file) {
            return file.getName();
        }
        return "";
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        if (element instanceof KlangFile file) {
            ItemPresentation p = file.getPresentation();
            if (p != null) return p;
            return new PresentationData(file.getName(), null, KlangIcons.FILE, null);
        }
        return new PresentationData(
                KlangStructurePresentation.text(element),
                KlangStructurePresentation.location(element),
                KlangStructurePresentation.icon(element), null);
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        List<StructureViewTreeElement> children = new ArrayList<>();
        collectChildren(element, children);
        return children.toArray(TreeElement.EMPTY_ARRAY);
    }

    /** Appends the structure-view children of {@code container} to {@code out}. */
    private static void collectChildren(@NotNull PsiElement container,
                                        @NotNull List<StructureViewTreeElement> out) {
        // Namespaces, aggregates and the file expose their members through DECLARATION wrappers.
        for (KlangDeclaration decl : PsiTreeUtil.getChildrenOfTypeAsList(container, KlangDeclaration.class)) {
            KlangNamespaceDecl ns = decl.getNamespaceDecl();
            if (ns != null) {
                if (ns.getName() == null) {
                    // Anonymous namespace: splice its members into the current level.
                    collectChildren(ns, out);
                } else {
                    out.add(new KlangStructureViewElement(ns));
                }
                continue;
            }
            PsiElement member = firstNonNull(
                    decl.getAggregateDecl(), decl.getEnumDecl(), decl.getUnionDecl(),
                    decl.getFunctionDecl(), decl.getVariableDecl());
            if (member != null) {
                out.add(new KlangStructureViewElement(member));
            }
            // visibilityDecl / usingDecl / friendDecl / empty ';' are intentionally omitted.
        }

        // Enum entries and union members are direct children (no DECLARATION wrapper).
        if (container instanceof KlangEnumDecl en) {
            for (KlangEnumEntry entry : en.getEnumEntryList()) {
                out.add(new KlangStructureViewElement(entry));
            }
        } else if (container instanceof KlangUnionDecl un) {
            for (KlangUnionMemberDecl m : un.getUnionMemberDeclList()) {
                out.add(new KlangStructureViewElement(m));
            }
        }
    }

    private static PsiElement firstNonNull(PsiElement... candidates) {
        for (PsiElement c : candidates) {
            if (c != null) return c;
        }
        return null;
    }

    // ── Navigation (delegated to the wrapped PSI element when navigable) ───────

    @Override
    public void navigate(boolean requestFocus) {
        if (element instanceof NavigatablePsiElement navigatable) {
            navigatable.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return element instanceof NavigatablePsiElement navigatable && navigatable.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element instanceof NavigatablePsiElement navigatable && navigatable.canNavigateToSource();
    }
}
