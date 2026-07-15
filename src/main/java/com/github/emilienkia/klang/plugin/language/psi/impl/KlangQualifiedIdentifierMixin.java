package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin for {@code qualifiedIdentifier} PSI nodes.
 *
 * <p>Phase 1 — attaches a {@link KlangQualifiedReference} when the qualified identifier
 * appears in a <em>type</em> or <em>directive</em> position (its parent is a
 * {@code typeSpec}, {@code typeName}, {@code baseSpec}, {@code annotationDef},
 * {@code usingDecl}, {@code friendDecl}, {@code staticDep} or {@code functionBody}).</p>
 *
 * <p>It returns {@code null} (no reference) when the qualified identifier is part of an
 * {@code identifierExpr} (handled by {@link KlangIdentifierExprMixin}) or names a module
 * ({@code moduleDeclaration}). For an {@code importDeclaration} target it returns a
 * {@link KlangModuleReference} (M3) that navigates to the imported module's declaration(s).
 * Overriding {@link #getReference()} — rather than relying on {@code psi.referenceContributor}
 * — is the mechanism that actually surfaces references through {@code findReferenceAt} for
 * {@code ASTWrapperPsiElement}-based PSI (same approach as {@code identifierExpr}).</p>
 */
public abstract class KlangQualifiedIdentifierMixin extends ASTWrapperPsiElement {

    public KlangQualifiedIdentifierMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        // M3 — an import target resolves to the imported module's `module` declaration(s).
        if (getParent() instanceof KlangImportDeclaration) {
            return new KlangModuleReference(this);
        }
        Boolean typesOnly = referenceKind(getParent());
        if (typesOnly == null) return null;
        return new KlangQualifiedReference(this, typesOnly);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        PsiReference tail = getReference();
        if (tail == null) return PsiReference.EMPTY_ARRAY;
        // A module name is navigated as a whole (KlangModuleReference); only type / directive
        // positions expose per-segment references so each qualified component is navigable.
        if (getParent() instanceof KlangImportDeclaration) {
            return new PsiReference[]{tail};
        }
        return KlangSegmentReference.build(this, tail);
    }

    /**
     * Classifies the parent of this qualified identifier.
     *
     * @return {@code null} if it is not a reference position (no reference attached);
     *         {@code TRUE} for a pure <em>type</em> position (resolution narrowed to
     *         type-like declarations); {@code FALSE} for a directive position that may
     *         legitimately target a function, namespace or element.
     */
    private static @Nullable Boolean referenceKind(@Nullable PsiElement parent) {
        if (parent instanceof KlangTypeSpec
                || parent instanceof KlangTypeName
                || parent instanceof KlangBaseSpec
                || parent instanceof KlangAnnotationDef) {
            return Boolean.TRUE;
        }
        if (parent instanceof KlangUsingDecl
                || parent instanceof KlangFriendDecl
                || parent instanceof KlangStaticDep
                || parent instanceof KlangFunctionBody) {
            return Boolean.FALSE;
        }
        return null;
    }
}

