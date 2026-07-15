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
        // Regular function: functionHead > IDENTIFIER
        ASTNode head = getNode().findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head != null) {
            ASTNode id = head.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return id.getPsi();
        }
        // Destructor: destructorHead > IDENTIFIER (for rename targets)
        ASTNode dtor = getNode().findChildByType(KlangTypes.DESTRUCTOR_HEAD);
        if (dtor != null) {
            ASTNode id = dtor.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return id.getPsi();
        }
        // Operator overload: no simple name → return null
        return null;
    }

    @Override
    public @Nullable String getName() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    /** See {@link KlangNamedDeclMixin#getTextOffset()} — point at the function name, not at
     *  any leading annotations / specifiers / template declaration. For unnamed functions
     *  (operator overloads, destructors) point at the operator/destructor head instead. */
    @Override
    public int getTextOffset() {
        PsiElement id = getNameIdentifier();
        if (id != null) return id.getTextOffset();
        ASTNode op = getNode().findChildByType(KlangTypes.OPERATOR_FUNCTION_HEAD);
        if (op != null) return op.getStartOffset();
        ASTNode cast = getNode().findChildByType(KlangTypes.CAST_OPERATOR_FUNCTION_HEAD);
        if (cast != null) return cast.getStartOffset();
        ASTNode dtor = getNode().findChildByType(KlangTypes.DESTRUCTOR_HEAD);
        if (dtor != null) return dtor.getStartOffset();
        return super.getTextOffset();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        // Handle regular constructor/function
        ASTNode head = getNode().findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head != null) {
            ASTNode idNode = head.findChildByType(KlangTypes.IDENTIFIER);
            if (idNode != null) {
                ASTNode newId = KlangPsiElementFactory.createIdentifier(getProject(), name);
                head.replaceChild(idNode, newId);
                return this;
            }
        }

        // Handle destructor (~Name)
        // The name parameter can be:
        // - "~NewName" (from aggregate rename propagation)
        // - "NewName" (from direct destructor rename in editor)
        ASTNode dtor = getNode().findChildByType(KlangTypes.DESTRUCTOR_HEAD);
        if (dtor != null) {
            ASTNode idNode = dtor.findChildByType(KlangTypes.IDENTIFIER);
            if (idNode != null) {
                // Extract the name without the ~ prefix if present
                String newName = name.startsWith("~") ? name.substring(1) : name;
                ASTNode newId = KlangPsiElementFactory.createIdentifier(getProject(), newName);
                dtor.replaceChild(idNode, newId);
                return this;
            }
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
