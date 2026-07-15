package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * Mixin for {@code designatedMemberName} nodes — exposes a
 * {@link KlangDesignatedMemberReference} on the {@code .field = …} designator.
 */
public abstract class KlangDesignatedMemberNameMixin extends ASTWrapperPsiElement {

    public KlangDesignatedMemberNameMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new KlangDesignatedMemberReference(this);
    }
}

