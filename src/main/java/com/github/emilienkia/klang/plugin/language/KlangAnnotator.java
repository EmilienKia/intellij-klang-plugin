package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangCastOperatorFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangDeclaration;
import com.github.emilienkia.klang.plugin.language.psi.KlangDestructorHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangOperatorFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangParameterSpec;
import com.github.emilienkia.klang.plugin.language.psi.KlangTemplateArgList;
import com.github.emilienkia.klang.plugin.language.psi.KlangTemplateDeclaration;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.KlangVariableDecl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KlangAnnotator implements Annotator {

    /**
     * Matches raw GrammarKit token-type references such as:
     *   KlangTokenType.IDENTIFIER
     *   KlangTokenType.LIT_INTEGER
     *   KlangElementType.SOME_RULE
     */
    private static final Pattern TOKEN_TYPE_PATTERN =
            Pattern.compile("Klang(?:Token|Element)Type\\.([A-Z_]+)");

    /** Human-readable labels for common token-type names. */
    private static final java.util.Map<String, String> TOKEN_LABELS = new java.util.HashMap<>();
    static {
        TOKEN_LABELS.put("IDENTIFIER",      "identifier");
        TOKEN_LABELS.put("LIT_INTEGER",     "integer literal");
        TOKEN_LABELS.put("LIT_FLOAT",       "floating-point literal");
        TOKEN_LABELS.put("LIT_STRING",      "string literal");
        TOKEN_LABELS.put("LIT_CHAR",        "character literal");
        TOKEN_LABELS.put("LIT_TRUE",        "'true'");
        TOKEN_LABELS.put("LIT_FALSE",       "'false'");
        TOKEN_LABELS.put("LIT_NULL",        "'null'");
        TOKEN_LABELS.put("LINE_COMMENT",    "line comment");
        TOKEN_LABELS.put("BLOCK_COMMENT",   "block comment");
        TOKEN_LABELS.put("LINE_DOC_COMMENT", "line documentation comment");
        TOKEN_LABELS.put("LINE_DOC_COMMENT_BWD", "backward line documentation comment");
        TOKEN_LABELS.put("BLOCK_DOC_COMMENT", "block documentation comment");
        TOKEN_LABELS.put("BLOCK_DOC_COMMENT_BWD", "backward block documentation comment");
        TOKEN_LABELS.put("PUNC_SEMICOLON",  "';'");
        TOKEN_LABELS.put("PUNC_COMMA",      "','");
        TOKEN_LABELS.put("PUNC_LPAREN",     "'('");
        TOKEN_LABELS.put("PUNC_RPAREN",     "')'");
        TOKEN_LABELS.put("PUNC_LBRACE",     "'{'");
        TOKEN_LABELS.put("PUNC_RBRACE",     "'}'");
        TOKEN_LABELS.put("PUNC_LBRACKET",   "'['");
        TOKEN_LABELS.put("PUNC_RBRACKET",   "']'");
        TOKEN_LABELS.put("PUNC_SCOPE",      "'::'");
        TOKEN_LABELS.put("PUNC_AT",         "'@'");
        TOKEN_LABELS.put("PUNC_ELLIPSIS",   "'...'");
        TOKEN_LABELS.put("OP_ASSIGN",       "'='");
        TOKEN_LABELS.put("OP_COLON",        "':'");
        TOKEN_LABELS.put("OP_QUESTION",     "'?'");
        TOKEN_LABELS.put("OP_DOT",          "'.'");
        TOKEN_LABELS.put("OP_ARROW",        "'->'");
        TOKEN_LABELS.put("OP_TILDE",        "'~'");
        TOKEN_LABELS.put("OP_SPACESHIP",    "'<=>'");
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiErrorElement err) {
            String message = humanize(err.getErrorDescription());
            holder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(element)
                    .create();
        } else if (element instanceof KlangFunctionHead head) {
            // Constructor detection: function name matches the nearest enclosing aggregate name
            KlangAggregateDecl enclosing = PsiTreeUtil.getParentOfType(head, KlangAggregateDecl.class);
            boolean isConstructor = enclosing != null
                    && head.getIdentifier().getText().equals(enclosing.getIdentifier().getText());
            TextAttributesKey key = isConstructor
                    ? KlangSyntaxHighlighter.IDENTIFIER_CONSTRUCTOR_DECL
                    : KlangSyntaxHighlighter.IDENTIFIER_FUN_DECL;
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(head.getIdentifier())
                    .textAttributes(key)
                    .create();
        } else if (element instanceof KlangVariableDecl decl) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(decl.getIdentifier())
                    .textAttributes(KlangSyntaxHighlighter.IDENTIFIER_VAR_DECL)
                    .create();
        } else if (element instanceof KlangParameterSpec param) {
            ASTNode id = param.getNode().findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(id.getTextRange())
                        .textAttributes(KlangSyntaxHighlighter.IDENTIFIER_PARAM_DECL)
                        .create();
            }
        } else if (element instanceof KlangDestructorHead dtor) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(dtor.getIdentifier())
                    .textAttributes(KlangSyntaxHighlighter.IDENTIFIER_DESTRUCTOR_DECL)
                    .create();
        } else if (element instanceof KlangOperatorFunctionHead op) {
            // operator == → color the symbol; operator[] → color the whole head ('operator [ ]')
            PsiElement target = op.getOperatorSymbol() != null ? op.getOperatorSymbol() : op;
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(target)
                    .textAttributes(KlangSyntaxHighlighter.IDENTIFIER_OPERATOR_DECL)
                    .create();
        } else if (element instanceof KlangCastOperatorFunctionHead cast) {
            // Cast / conversion operator 'operator ()' — color the whole head.
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(cast)
                    .textAttributes(KlangSyntaxHighlighter.IDENTIFIER_OPERATOR_DECL)
                    .create();
        } else if (element instanceof KlangTemplateDeclaration || element instanceof KlangTemplateArgList) {
            highlightTemplateBrackets(element, holder);
        } else if (element instanceof KlangDeclaration decl) {
            // Empty declaration: a stray ';' standing where a declaration is expected
            // (between two declarations, or after the last one — including the leftover ';'
            // after a block declaration's '}'). The 'declaration ::= … | ';'' alternative in
            // klang.bnf parses it without error to match the K compiler's leniency; here we
            // surface it as a non-fatal warning. Such a DECLARATION node has the ';' token as
            // its only (first) child. See the matching note in klang.bnf — keep both in sync.
            ASTNode first = decl.getNode().getFirstChildNode();
            if (first != null && first.getElementType() == KlangTypes.PUNC_SEMICOLON) {
                holder.newAnnotation(HighlightSeverity.WARNING,
                                "Superfluous ';': empty declaration (tolerated by the K compiler)")
                        .range(first.getTextRange())
                        .create();
            }
        }
    }

    /** Colors the opening {@code <} and closing {@code >} of a template bracket node. */
    private static void highlightTemplateBrackets(@NotNull PsiElement element,
                                                   @NotNull AnnotationHolder holder) {
        ASTNode node = element.getNode();
        ASTNode lt = node.findChildByType(KlangTypes.OP_LT);
        ASTNode gt = node.getLastChildNode();
        if (lt != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(lt.getTextRange())
                    .textAttributes(KlangSyntaxHighlighter.PUNC_TEMPLATE_BRACKET)
                    .create();
        }
        if (gt != null && gt.getElementType() == KlangTypes.OP_GT) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(gt.getTextRange())
                    .textAttributes(KlangSyntaxHighlighter.PUNC_TEMPLATE_BRACKET)
                    .create();
        }
    }

    /**
     * Replaces technical GrammarKit token/element type references with
     * human-readable labels, and strips any remaining class-name prefixes.
     */
    static String humanize(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder();
        Matcher m = TOKEN_TYPE_PATTERN.matcher(raw);
        while (m.find()) {
            String name = m.group(1);
            String label = TOKEN_LABELS.getOrDefault(name, name.toLowerCase().replace('_', ' '));
            m.appendReplacement(sb, Matcher.quoteReplacement(label));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}



