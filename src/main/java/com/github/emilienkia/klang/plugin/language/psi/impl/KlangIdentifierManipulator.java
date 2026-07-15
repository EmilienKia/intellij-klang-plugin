package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Element manipulator for K-lang reference hosts ({@code identifierExpr},
 * {@code qualifiedIdentifier}, {@code memberInit}, {@code designatedMemberName}).
 *
 * <p>Registered so that {@link com.intellij.psi.PsiReferenceBase#getManipulator()} succeeds
 * during rename / bind operations (including the in-place variable renamer, which calls the
 * manipulator directly rather than {@code handleElementRename}). It rewrites the single
 * {@code IDENTIFIER} leaf at the start of the changed range, which is always the simple name
 * a reference points at.</p>
 */
public class KlangIdentifierManipulator extends AbstractElementManipulator<PsiElement> {

    @Override
    public PsiElement handleContentChange(@NotNull PsiElement element,
                                          @NotNull TextRange range,
                                          String newContent) throws IncorrectOperationException {
        return KlangPsiElementFactory.renameIdentifierAt(element, range.getStartOffset(), newContent);
    }
}

