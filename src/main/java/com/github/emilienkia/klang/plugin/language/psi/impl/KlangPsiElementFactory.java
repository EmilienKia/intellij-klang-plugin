package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.KlangFileType;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
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

    /**
     * Parses {@code text} (a complete, top-level {@code functionDecl} fragment, e.g.
     * {@code "override foo(x : int) : int { return x; }"}) and returns the resulting
     * {@link KlangFunctionDecl} node (detached copy — safe to use with {@link PsiElement#replace}).
     * Used by the Change Signature refactoring to synthesize a new declaration header while
     * preserving the original body/throws-clause text verbatim.
     */
    public static @NotNull KlangFunctionDecl createFunctionDecl(@NotNull Project project,
                                                                @NotNull String text) {
        PsiFile file = PsiFileFactory.getInstance(project)
                .createFileFromText("__dummy.k", KlangFileType.INSTANCE, text);
        KlangFunctionDecl decl = PsiTreeUtil.findChildOfType(file, KlangFunctionDecl.class);
        if (decl == null) {
            throw new IllegalArgumentException("Cannot parse function declaration: " + text);
        }
        return decl;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Renames the {@code IDENTIFIER} leaf that starts at {@code relativeStartOffset} inside
     * {@code element} (the reference range start) by replacing it with a fresh identifier
     * token. This lets PSI references implement {@code handleElementRename} without needing
     * a registered {@link com.intellij.psi.ElementManipulator}.
     *
     * @return {@code element} (its identifier child is replaced in place)
     */
    public static @NotNull PsiElement renameIdentifierAt(@NotNull PsiElement element,
                                                         int relativeStartOffset,
                                                         @NotNull String newName) {
        int absoluteStart = element.getTextRange().getStartOffset() + relativeStartOffset;
        ASTNode idLeaf = findIdentifierLeafAt(element.getNode(), absoluteStart);
        if (idLeaf != null) {
            ASTNode newId = createIdentifier(element.getProject(), newName);
            idLeaf.getTreeParent().replaceChild(idLeaf, newId);
        }
        return element;
    }

    private static ASTNode findIdentifierLeafAt(@NotNull ASTNode node, int absoluteStart) {
        if (node.getElementType() == KlangTypes.IDENTIFIER && node.getStartOffset() == absoluteStart) {
            return node;
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            ASTNode found = findIdentifierLeafAt(child, absoluteStart);
            if (found != null) return found;
        }
        return null;
    }

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
