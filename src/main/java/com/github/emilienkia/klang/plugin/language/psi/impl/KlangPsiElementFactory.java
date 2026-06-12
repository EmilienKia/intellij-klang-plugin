package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.KlangFileType;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Utility factory to create PSI nodes from K-lang source fragments.
 * Used by mixins to support rename refactoring (setName).
 */
public final class KlangPsiElementFactory {

    private KlangPsiElementFactory() {}

    /**
     * Creates an IDENTIFIER AST node from a plain name string.
     * Internally parses a minimal variable declaration {@code x : int;} and
     * extracts the identifier token.
     */
    public static @NotNull ASTNode createIdentifier(@NotNull Project project,
                                                     @NotNull String name) {
        // Minimal valid K-lang fragment containing one IDENTIFIER
        String src = name + " : int;";
        PsiFile file = PsiFileFactory.getInstance(project)
                .createFileFromText("__dummy.k", KlangFileType.INSTANCE, src);
        ASTNode root = file.getNode();
        ASTNode id = findFirst(root, KlangTypes.IDENTIFIER);
        if (id == null) {
            throw new IllegalArgumentException("Cannot create identifier node for: " + name);
        }
        // Return a copy so the dummy file can be GC'd
        return id.copyElement();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ASTNode findFirst(ASTNode node,
                                     com.intellij.psi.tree.IElementType type) {
        if (node.getElementType() == type) return node;
        for (ASTNode child = node.getFirstChildNode(); child != null;
             child = child.getTreeNext()) {
            ASTNode found = findFirst(child, type);
            if (found != null) return found;
        }
        return null;
    }
}
