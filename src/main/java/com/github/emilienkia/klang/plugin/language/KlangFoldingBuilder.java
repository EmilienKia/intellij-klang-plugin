package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KlangFoldingBuilder implements FoldingBuilder {

    /** PSI node types whose body (the inner { … }) should be foldable. */
    private static final Set<IElementType> FOLDABLE_TYPES = Set.of(
            KlangTypes.FUNCTION_DECL,
            KlangTypes.AGGREGATE_DECL,
            KlangTypes.ENUM_DECL,
            KlangTypes.UNION_DECL,
            KlangTypes.NAMESPACE_DECL,
            KlangTypes.BLOCK_STATEMENT,
            KlangTypes.BRACE_INIT_LIST
    );

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull ASTNode node,
                                                          @NotNull Document document) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        collectFoldRegions(node, document, descriptors);
        return descriptors.toArray(FoldingDescriptor[]::new);
    }

    private void collectFoldRegions(ASTNode node, Document document,
                                    List<FoldingDescriptor> descriptors) {
        IElementType type = node.getElementType();

        if (FOLDABLE_TYPES.contains(type)) {
            // Find the opening brace child
            ASTNode lbrace = node.findChildByType(KlangTypes.PUNC_LBRACE);
            ASTNode rbrace = node.findChildByType(KlangTypes.PUNC_RBRACE);

            if (lbrace != null && rbrace != null) {
                int start = lbrace.getStartOffset();
                int end   = rbrace.getStartOffset() + rbrace.getTextLength();

                // Only fold if the block spans more than one line
                if (document.getLineNumber(start) < document.getLineNumber(end - 1)) {
                    TextRange range = new TextRange(start, end);
                    descriptors.add(new FoldingDescriptor(node, range, null,
                            placeholderFor(node)));
                }
            }
        }

        // Also fold block comments (ordinary and documentation)
        if (type == KlangTypes.BLOCK_COMMENT || type == KlangTypes.BLOCK_DOC_COMMENT) {
            TextRange range = node.getTextRange();
            if (document.getLineNumber(range.getStartOffset()) <
                document.getLineNumber(range.getEndOffset() - 1)) {
                String placeholder = type == KlangTypes.BLOCK_DOC_COMMENT ? "/**...*/" : "/*...*/";
                descriptors.add(new FoldingDescriptor(node, range, null, placeholder));
            }
        }

        for (ASTNode child = node.getFirstChildNode(); child != null;
             child = child.getTreeNext()) {
            collectFoldRegions(child, document, descriptors);
        }
    }

    /**
     * Returns a concise placeholder string shown when the region is collapsed.
     * For declarations, we try to include the name; for everything else, "{…}".
     */
    private static String placeholderFor(ASTNode node) {
        IElementType type = node.getElementType();

        if (type == KlangTypes.FUNCTION_DECL) {
            // Look for FUNCTION_HEAD > IDENTIFIER, DESTRUCTOR_HEAD, or OPERATOR_FUNCTION_HEAD
            ASTNode head = node.findChildByType(KlangTypes.FUNCTION_HEAD);
            if (head == null) head = node.findChildByType(KlangTypes.DESTRUCTOR_HEAD);
            if (head == null) head = node.findChildByType(KlangTypes.OPERATOR_FUNCTION_HEAD);
            if (head != null) return head.getText() + "(…) {…}";
            // Cast / conversion operator 'operator ()' has no parameter list.
            ASTNode castHead = node.findChildByType(KlangTypes.CAST_OPERATOR_FUNCTION_HEAD);
            if (castHead != null) return castHead.getText() + " {…}";
        }

        if (type == KlangTypes.AGGREGATE_DECL) {
            ASTNode id = node.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return id.getText() + " {…}";
        }

        if (type == KlangTypes.ENUM_DECL) {
            ASTNode id = node.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return "enum " + id.getText() + " {…}";
        }

        if (type == KlangTypes.UNION_DECL) {
            ASTNode id = node.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return "union " + id.getText() + " {…}";
        }

        if (type == KlangTypes.NAMESPACE_DECL) {
            ASTNode id = node.findChildByType(KlangTypes.IDENTIFIER);
            return id != null ? "namespace " + id.getText() + " {…}" : "namespace {…}";
        }

        if (type == KlangTypes.BRACE_INIT_LIST) {
            return "{…}";
        }

        // BLOCK_STATEMENT and fallback
        return "{…}";
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        return "{…}";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        // Nothing collapsed by default — let the user decide
        return false;
    }
}


