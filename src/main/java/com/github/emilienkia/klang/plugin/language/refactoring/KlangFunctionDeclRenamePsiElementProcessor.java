package com.github.emilienkia.klang.plugin.language.refactoring;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Propagates constructor/destructor rename to the owning aggregate and related declarations.
 *
 * When a constructor or destructor is renamed, its name must stay synchronized with its
 * owning aggregate. This processor:
 * - Detects renames of constructor (name matches aggregate) or destructor (~Name) declarations
 * - Renames the owning aggregate to the new name
 * - Renames all related constructors/destructors to keep names in sync
 *
 * This ensures bidirectional synchronization:
 * - Rename aggregate → update constructors/destructors
 * - Rename constructor/destructor → update aggregate and other constructors/destructors
 */
public final class KlangFunctionDeclRenamePsiElementProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        if (!(element instanceof KlangFunctionDecl func)) return false;

        // Only process constructors and destructors
        KlangAggregateDecl owner = PsiTreeUtil.getParentOfType(func, KlangAggregateDecl.class);
        if (owner == null) return false;

        String aggName = owner.getName();
        return isConstructor(func, aggName) || isDestructor(func);
    }

    @Override
    public void prepareRenaming(@NotNull PsiElement element,
                                @NotNull String newName,
                                @NotNull Map<PsiElement, String> allRenames) {
        if (!(element instanceof KlangFunctionDecl func)) return;

        KlangAggregateDecl aggregate = PsiTreeUtil.getParentOfType(func, KlangAggregateDecl.class);
        if (aggregate == null) return;

        String oldAggName = aggregate.getName();
        String newAggName;

        // Determine the new aggregate name from the constructor/destructor name
        if (isDestructor(func)) {
            // For destructors: name parameter is "NewName", extract from that
            newAggName = newName;
        } else if (isConstructor(func, oldAggName)) {
            // For constructors: name parameter is the new name
            newAggName = newName;
        } else {
            return;
        }

        // Rename the aggregate to the new name
        allRenames.put(aggregate, newAggName);

        // Also rename all constructors and destructors in this aggregate
        // (don't rely on aggregate processor being called, as the rename target is not the aggregate)
        for (KlangDeclaration decl : aggregate.getDeclarationList()) {
            KlangFunctionDecl otherFunc = decl.getFunctionDecl();
            if (otherFunc != null && otherFunc != func) {  // Skip the element being renamed
                if (isConstructor(otherFunc, oldAggName)) {
                    allRenames.put(otherFunc, newAggName);
                } else if (isDestructor(otherFunc)) {
                    allRenames.put(otherFunc, "~" + newAggName);
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

