package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * Mixin for {@code memberInit} nodes — exposes a {@link KlangMemberInitReference} on the
 * member/base name of a constructor initializer ({@code Name(args)}).
 */
public abstract class KlangMemberInitMixin extends ASTWrapperPsiElement {

    public KlangMemberInitMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new KlangMemberInitReference(this);
    }
}

