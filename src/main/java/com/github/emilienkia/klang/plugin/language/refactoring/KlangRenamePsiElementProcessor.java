package com.github.emilienkia.klang.plugin.language.refactoring;

import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Propagates method rename across the override/implementation hierarchy.
 */
public final class KlangRenamePsiElementProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof KlangFunctionDecl;
    }

    @Override
    public void prepareRenaming(@NotNull PsiElement element,
                                @NotNull String newName,
                                @NotNull Map<PsiElement, String> allRenames) {
        if (!(element instanceof KlangFunctionDecl functionDecl)) return;

        for (KlangFunctionDecl related : KlangResolveUtil.findRenameCompanionMethods(functionDecl)) {
            if (related != element) {
                allRenames.put(related, newName);
            }
        }
    }
}
