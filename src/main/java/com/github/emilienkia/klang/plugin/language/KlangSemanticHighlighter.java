package com.github.emilienkia.klang.plugin.language;
import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangCatchParameterDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangEnumDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangEnumEntry;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangForeachVarDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangIdentifierExpr;
import com.github.emilienkia.klang.plugin.language.psi.KlangIfCondVarDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangModuleDeclaration;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedReturnVar;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamespaceDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangParameterSpec;
import com.github.emilienkia.klang.plugin.language.psi.KlangQualifiedIdentifier;
import com.github.emilienkia.klang.plugin.language.psi.KlangTemplateParameter;
import com.github.emilienkia.klang.plugin.language.psi.KlangUnionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangUnionMemberDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangVariableDecl;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
/**
 * Semantic highlighting for K-lang identifier usages.
 * Design: docs/klang/semantic-highlighting-plan.md
 */
public class KlangSemanticHighlighter implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (DumbService.isDumb(element.getProject())) return;
        if (element instanceof KlangIdentifierExpr expr) {
            highlightIdentifierExpr(expr, holder);
        } else if (element instanceof KlangQualifiedIdentifier qi) {
            highlightQualifiedIdentifier(qi, holder);
        }
    }
    private static void highlightIdentifierExpr(@NotNull KlangIdentifierExpr expr,
                                                @NotNull AnnotationHolder holder) {
        PsiReference ref = expr.getReference();
        if (ref == null) return;
        PsiElement resolved = ref.resolve();
        if (resolved == null) return;
        TextAttributesKey key = keyForResolved(resolved, expr);
        if (key == null) return;
        applyKey(key, expr.getTextRange(), holder);
    }
    /**
     * Colours every ::-separated segment independently.
     * Intermediate segments use KlangResolveUtil.resolve() for full resolution engine.
     * The last segment uses the element's own PsiReference for context-aware filtering.
     */
    private static void highlightQualifiedIdentifier(@NotNull KlangQualifiedIdentifier qi,
                                                     @NotNull AnnotationHolder holder) {
        String text = qi.getText();
        int    base = qi.getTextRange().getStartOffset();
        List<int[]> bounds = KlangResolveUtil.segmentBounds(text);
        if (bounds.isEmpty()) return;
        for (int i = 0; i < bounds.size() - 1; i++) {
            int[]  seg        = bounds.get(i);
            String prefixText = text.substring(0, seg[2]);
            List<PsiElement> targets = KlangResolveUtil.resolve(qi, prefixText);
            if (targets.isEmpty()) continue;
            TextAttributesKey key = keyForResolved(targets.get(0), qi);
            if (key == null) continue;
            applyKey(key, TextRange.create(base + seg[0], base + seg[1]), holder);
        }
        PsiReference ref = qi.getReference();
        if (ref == null) return;
        PsiElement lastResolved = ref.resolve();
        if (lastResolved == null) return;
        TextAttributesKey lastKey = keyForResolved(lastResolved, qi);
        if (lastKey == null) return;
        int[] lastSeg = bounds.get(bounds.size() - 1);
        applyKey(lastKey, TextRange.create(base + lastSeg[0], base + lastSeg[1]), holder);
    }
    @Nullable
    public static TextAttributesKey keyForResolved(@NotNull PsiElement resolved,
                                                   @NotNull PsiElement usageSite) {
        if (resolved instanceof KlangFunctionDecl) {
            return KlangResolveUtil.isCalleeOfCall(usageSite)
                    ? KlangSyntaxHighlighter.IDENTIFIER_FUN_CALL
                    : KlangSyntaxHighlighter.IDENTIFIER_FUN_REF;
        }
        if (resolved instanceof KlangVariableDecl
                || resolved instanceof KlangNamedReturnVar
                || resolved instanceof KlangIfCondVarDecl
                || resolved instanceof KlangForeachVarDecl) {
            return KlangSyntaxHighlighter.IDENTIFIER_VAR_REF;
        }
        if (resolved instanceof KlangParameterSpec
                || resolved instanceof KlangCatchParameterDecl) {
            return KlangSyntaxHighlighter.IDENTIFIER_PARAM_REF;
        }
        if (resolved instanceof KlangAggregateDecl
                || resolved instanceof KlangEnumDecl
                || resolved instanceof KlangUnionDecl) {
            return KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF;
        }
        if (resolved instanceof KlangTemplateParameter) {
            return KlangSyntaxHighlighter.IDENTIFIER_TEMPLATE_PARAM_REF;
        }
        if (resolved instanceof KlangNamespaceDecl
                || resolved instanceof KlangModuleDeclaration) {
            return KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF;
        }
        if (resolved instanceof KlangEnumEntry
                || resolved instanceof KlangUnionMemberDecl) {
            return KlangSyntaxHighlighter.IDENTIFIER_ENUM_ENTRY_REF;
        }
        return null;
    }
    private static void applyKey(@NotNull TextAttributesKey key,
                                 @NotNull TextRange range,
                                 @NotNull AnnotationHolder holder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(range)
                .textAttributes(key)
                .create();
    }
}
