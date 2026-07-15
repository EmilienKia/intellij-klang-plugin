package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.KlangNavigationPresentation;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangPsiElementFactory;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base mixin for named declarations whose name is carried by a direct IDENTIFIER child:
 * aggregateDecl, enumDecl, variableDecl.
 *
 * For functionDecl (name inside a functionHead sub-node) see KlangFunctionDeclMixin.
 */
public abstract class KlangNamedDeclMixin extends ASTWrapperPsiElement
        implements KlangNamedElement {

    public KlangNamedDeclMixin(@NotNull ASTNode node) {
        super(node);
    }

    // ── PsiNameIdentifierOwner ────────────────────────────────────────────────

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode idNode = getNode().findChildByType(KlangTypes.IDENTIFIER);
        return idNode != null ? idNode.getPsi() : null;
    }

    @Override
    public @Nullable String getName() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    /**
     * Point the element's text offset at the name identifier rather than the first token
     * (which, for aggregates/enums/unions, is a keyword or annotation). Navigation and
     * refactorings (notably {@code TargetElementUtil}, which gates whether Rename is
     * enabled) rely on this to recognise the caret as being on the declaration's name.
     */
    @Override
    public int getTextOffset() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getTextOffset() : super.getTextOffset();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        ASTNode idNode = getNode().findChildByType(KlangTypes.IDENTIFIER);
        if (idNode != null) {
            ASTNode newId = KlangPsiElementFactory.createIdentifier(getProject(), name);
            getNode().replaceChild(idNode, newId);
        }
        return this;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return new PresentationData(
                KlangNavigationPresentation.elementText(this),
                KlangNavigationPresentation.containerText(this),
                KlangNavigationPresentation.icon(this),
                null);
    }
}