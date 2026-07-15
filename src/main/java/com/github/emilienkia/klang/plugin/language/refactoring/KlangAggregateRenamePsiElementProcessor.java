package com.github.emilienkia.klang.plugin.language.refactoring;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Propagates aggregate (class/struct/interface) rename across:
 * - Constructor declarations (functions with same name as aggregate)
 * - Destructor declarations (~Name)
 *
 * IntelliJ automatically updates all references when a declaration is renamed,
 * so we only need to rename the declaration elements here.
 */
public final class KlangAggregateRenamePsiElementProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof KlangAggregateDecl;
    }

    @Override
    public void prepareRenaming(@NotNull PsiElement element,
                                @NotNull String newName,
                                @NotNull Map<PsiElement, String> allRenames) {
        if (!(element instanceof KlangAggregateDecl aggregate)) return;

        String oldName = aggregate.getIdentifier().getText();
        
        // Find all constructors and destructors declared in this aggregate
        for (KlangDeclaration decl : aggregate.getDeclarationList()) {
            // KlangDeclaration is a wrapper — extract the function
            KlangFunctionDecl func = decl.getFunctionDecl();
            if (func != null) {
                // Check if this is a constructor (function name equals old aggregate name)
                if (isConstructor(func, oldName)) {
                    allRenames.put(func, newName);
                }
                // Check if this is a destructor
                else if (isDestructor(func)) {
                    // Rename destructor to ~NewName (strip ~ and rebuild)
                    allRenames.put(func, "~" + newName);
                }
            }
        }
    }

    /**
     * Check if a function is a constructor by comparing its name with the aggregate name.
     */
    private static boolean isConstructor(@NotNull KlangFunctionDecl func, @NotNull String aggregateName) {
        PsiElement nameId = func.getNameIdentifier();
        if (nameId == null) return false;
        return nameId.getText().equals(aggregateName);
    }

    /**
     * Check if a function is a destructor (has DestructorHead).
     */
    private static boolean isDestructor(@NotNull KlangFunctionDecl func) {
        return PsiTreeUtil.getChildOfType(func, KlangDestructorHead.class) != null;
    }
}
