package com.github.emilienkia.klang.plugin.language;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Registers additional reference providers for K-lang PSI elements.
 *
 * Phase 1: identifierExpr references are already handled by
 * {@link com.github.emilienkia.klang.plugin.language.psi.impl.KlangIdentifierExprMixin#getReference()}.
 *
 * This contributor exists as an extension point for phase 2 (type references in
 * typeSpec, baseSpec, parameterSpec, etc.) without having to restructure the code.
 */
public class KlangReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // Phase 2 will register providers for typeSpec / baseSpec / qualifiedIdentifier here.
        // For now, identifierExpr is covered by its mixin.
    }
}

