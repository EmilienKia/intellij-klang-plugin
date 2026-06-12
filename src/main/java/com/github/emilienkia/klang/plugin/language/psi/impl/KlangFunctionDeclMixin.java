package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangPsiElementFactory;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin for functionDecl.
 *
 * The function name lives inside the {@code functionHead} sub-node, not as a
 * direct IDENTIFIER child of functionDecl.  Destructor and operator overloads
 * are excluded from named resolution (they have no plain identifier name).
 */
public abstract class KlangFunctionDeclMixin extends ASTWrapperPsiElement
        implements KlangNamedElement {

    public KlangFunctionDeclMixin(@NotNull ASTNode node) {
        super(node);
    }

    // ── PsiNameIdentifierOwner ────────────────────────────────────────────────

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        // functionHead > IDENTIFIER
        ASTNode head = getNode().findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head != null) {
            ASTNode id = head.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return id.getPsi();
        }
        // Destructor / operator overload: no simple name → return null
        return null;
    }

    @Override
    public @Nullable String getName() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        ASTNode head = getNode().findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head != null) {
            ASTNode idNode = head.findChildByType(KlangTypes.IDENTIFIER);
            if (idNode != null) {
                ASTNode newId = KlangPsiElementFactory.createIdentifier(getProject(), name);
                head.replaceChild(idNode, newId);
            }
        }
        return this;
    }
}

