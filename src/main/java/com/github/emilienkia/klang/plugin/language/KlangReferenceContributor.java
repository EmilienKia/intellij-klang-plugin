package com.github.emilienkia.klang.plugin.language;

import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * Reference extension point for K-lang.
 *
 * <p>References are currently attached directly on PSI nodes via mixins —
 * {@code identifierExpr} ({@link com.github.emilienkia.klang.plugin.language.psi.impl.KlangIdentifierExprMixin})
 * for expression-context names, and {@code qualifiedIdentifier}
 * ({@link com.github.emilienkia.klang.plugin.language.psi.impl.KlangQualifiedIdentifierMixin})
 * for type / directive positions. Overriding {@code getReference()} is what surfaces
 * references through {@code findReferenceAt} for these {@code ASTWrapperPsiElement} nodes;
 * a plain {@code psi.referenceContributor} does not.</p>
 *
 * <p>This contributor is kept as a registered hook for future reference kinds that are
 * better expressed as patterns (e.g. injected or library references).</p>
 */
public class KlangReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // No pattern-based providers: see class javadoc (references are mixin-based).
    }
}

