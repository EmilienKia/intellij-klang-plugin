package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * Mixin for {@code identifierExpr} PSI nodes.
 *
 * Overrides {@link #getReference()} so that every identifier expression
 * in K-lang source automatically carries a {@link KlangReference} that
 * can be resolved to its declaration.
 */
public abstract class KlangIdentifierExprMixin extends ASTWrapperPsiElement {

    public KlangIdentifierExprMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new KlangReference(this);
    }
}
