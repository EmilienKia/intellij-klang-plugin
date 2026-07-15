package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <b>Multi-file indexing — milestone M3.</b>
 *
 * <p>PSI reference attached to the {@code qualifiedIdentifier} of an {@code import M;}
 * declaration. It resolves the imported module name to the {@code module M;} declaration(s)
 * of that module elsewhere in the project (a module may be declared across several files), so
 * Go-to-Declaration on an import jumps to the module it brings in.</p>
 *
 * <p>Fails soft: if the module is not present in the project (e.g. an unbundled standard
 * library), resolution yields nothing rather than an error.</p>
 */
public class KlangModuleReference extends PsiReferenceBase.Poly<PsiElement>
        implements PsiPolyVariantReference {

    public KlangModuleReference(@NotNull PsiElement element) {
        super(element, fullRange(element), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        String name = myElement.getText();
        if (name == null || name.isBlank()) return ResolveResult.EMPTY_ARRAY;

        List<?> mods = KlangResolveUtil.findModuleDeclarations(myElement, name.trim());
        return mods.stream()
                .map(m -> new PsiElementResolveResult((PsiElement) m))
                .toArray(ResolveResult[]::new);
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    /** The whole module name is the reference range (navigate from anywhere on it). */
    private static @NotNull TextRange fullRange(@NotNull PsiElement element) {
        String text = element.getText();
        return text == null ? TextRange.EMPTY_RANGE : new TextRange(0, text.length());
    }
}

