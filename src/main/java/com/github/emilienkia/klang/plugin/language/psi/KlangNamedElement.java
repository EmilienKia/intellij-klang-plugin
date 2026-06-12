package com.github.emilienkia.klang.plugin.language.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * Common interface for all K-lang named declarations:
 * aggregateDecl, enumDecl, functionDecl, variableDecl.
 *
 * Extends {@link PsiNameIdentifierOwner} which provides getName(), setName()
 * and getNameIdentifier() — the three methods needed for Find Usages,
 * Rename refactoring and reference resolution.
 */
public interface KlangNamedElement extends PsiNameIdentifierOwner {
}
