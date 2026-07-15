package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.KlangPostfixOp;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * Mixin for {@code identifierExpr} PSI nodes.
 *
 * <p>Overrides {@link #getReference()} so every identifier expression carries a reference:
 * <ul>
 *   <li>when it is the right-hand side of a member access ({@code .}/{@code ->}, i.e. its
 *       parent is a {@code postfixOp}) → a {@link KlangMemberReference} resolved against the
 *       receiver's type (§10);</li>
 *   <li>otherwise → a {@link KlangReference} resolved through the lexical scope chain (§5).</li>
 * </ul>
 *
 * <p>{@link #getReferences()} additionally exposes one {@link KlangSegmentReference} per
 * <em>intermediate</em> {@code ::}-separated segment, so each qualified component (e.g.
 * {@code Policy} in {@code Policy::RUNTIME}) is independently navigable. The last segment keeps
 * the reference returned by {@link #getReference()}.</p>
 */
public abstract class KlangIdentifierExprMixin extends ASTWrapperPsiElement {

    public KlangIdentifierExprMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        // The member-access form is the only postfixOp alternative that contains an
        // identifierExpr, so a postfixOp parent unambiguously means "RHS of . / ->".
        if (getParent() instanceof KlangPostfixOp) {
            return new KlangMemberReference(this);
        }
        return new KlangReference(this);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        // Member access (recv.member) is a single, last-segment reference — no qualified prefix.
        if (getParent() instanceof KlangPostfixOp) {
            return new PsiReference[]{new KlangMemberReference(this)};
        }
        return KlangSegmentReference.build(this, new KlangReference(this));
    }
}
