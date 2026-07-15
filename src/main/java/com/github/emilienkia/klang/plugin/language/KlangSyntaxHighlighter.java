package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangTokenSets;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class KlangSyntaxHighlighter implements SyntaxHighlighter {

    // ── Comments ──────────────────────────────────────────────────────────────
    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("KLANG_LINE_COMMENT",  DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("KLANG_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    /** Documentation comments (Javadoc / Doxygen style): {@code ///} and {@code /** … *}{@code /}. */
    public static final TextAttributesKey LINE_DOC_COMMENT =
            createTextAttributesKey("KLANG_LINE_DOC_COMMENT",  DefaultLanguageHighlighterColors.DOC_COMMENT);
    public static final TextAttributesKey BLOCK_DOC_COMMENT =
            createTextAttributesKey("KLANG_BLOCK_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);

    // ── Keywords ──────────────────────────────────────────────────────────────
    /** Primitive / built-in types: bool byte char short int long float double unsigned */
    public static final TextAttributesKey KEYWORD_TYPE =
            createTextAttributesKey("KLANG_KEYWORD_TYPE",     DefaultLanguageHighlighterColors.KEYWORD);
    /** Declaration keywords: struct class interface annotation namespace module import using friend enum union template typename generic */
    public static final TextAttributesKey KEYWORD_DECL =
            createTextAttributesKey("KLANG_KEYWORD_DECL",     DefaultLanguageHighlighterColors.KEYWORD);
    /** Modifier keywords: static const abstract final override public protected private */
    public static final TextAttributesKey KEYWORD_MODIFIER =
            createTextAttributesKey("KLANG_KEYWORD_MODIFIER", DefaultLanguageHighlighterColors.KEYWORD);
    /** Control-flow keywords: if else while for break return throw try catch finally throws */
    public static final TextAttributesKey KEYWORD_CONTROL =
            createTextAttributesKey("KLANG_KEYWORD_CONTROL",  DefaultLanguageHighlighterColors.KEYWORD);
    /** Special / expression keywords: this new delete default operator */
    public static final TextAttributesKey KEYWORD_SPECIAL =
            createTextAttributesKey("KLANG_KEYWORD_SPECIAL",  DefaultLanguageHighlighterColors.KEYWORD);

    // ── Identifier ────────────────────────────────────────────────────────────
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("KLANG_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);

    // ── Semantic identifier roles — declaration sites (applied by KlangAnnotator) ──
    /** Identifier in a variable declaration: x : int = 0; */
    public static final TextAttributesKey IDENTIFIER_VAR_DECL =
            createTextAttributesKey("KLANG_IDENTIFIER_VAR_DECL",   DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    /** Identifier in a function declaration: sum(...) : int { } */
    public static final TextAttributesKey IDENTIFIER_FUN_DECL =
            createTextAttributesKey("KLANG_IDENTIFIER_FUN_DECL",   DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    /** Identifier in a constructor declaration: Point(x : float, y : float) { } */
    public static final TextAttributesKey IDENTIFIER_CONSTRUCTOR_DECL =
            createTextAttributesKey("KLANG_IDENTIFIER_CONSTRUCTOR_DECL", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    /** Identifier in a parameter declaration: n : int */
    public static final TextAttributesKey IDENTIFIER_PARAM_DECL =
            createTextAttributesKey("KLANG_IDENTIFIER_PARAM_DECL", DefaultLanguageHighlighterColors.PARAMETER);
    /** Identifier in a destructor declaration: ~Point() → "Point" */
    public static final TextAttributesKey IDENTIFIER_DESTRUCTOR_DECL =
            createTextAttributesKey("KLANG_IDENTIFIER_DESTRUCTOR_DECL", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    /** Operator symbol in an operator function declaration: operator == → "==" */
    public static final TextAttributesKey IDENTIFIER_OPERATOR_DECL =
            createTextAttributesKey("KLANG_IDENTIFIER_OPERATOR_DECL", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

    // ── Semantic identifier roles — usage/reference sites (applied by KlangSemanticHighlighter) ──
    /** Reference to a type (struct / class / interface / annotation / enum / union). */
    public static final TextAttributesKey IDENTIFIER_TYPE_REF =
            createTextAttributesKey("KLANG_IDENTIFIER_TYPE_REF",          DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    /** Identifier used as the callee of a call expression: f(…) or obj.f(…). */
    public static final TextAttributesKey IDENTIFIER_FUN_CALL =
            createTextAttributesKey("KLANG_IDENTIFIER_FUN_CALL",          DefaultLanguageHighlighterColors.FUNCTION_CALL);
    /** Reference to a function that is not in a call position. */
    public static final TextAttributesKey IDENTIFIER_FUN_REF =
            createTextAttributesKey("KLANG_IDENTIFIER_FUN_REF",           DefaultLanguageHighlighterColors.FUNCTION_CALL);
    /** Reference to a local variable or field. */
    public static final TextAttributesKey IDENTIFIER_VAR_REF =
            createTextAttributesKey("KLANG_IDENTIFIER_VAR_REF",           DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    /** Reference to a function parameter or catch-parameter. */
    public static final TextAttributesKey IDENTIFIER_PARAM_REF =
            createTextAttributesKey("KLANG_IDENTIFIER_PARAM_REF",         DefaultLanguageHighlighterColors.PARAMETER);
    /** Reference to a namespace or module. */
    public static final TextAttributesKey IDENTIFIER_NAMESPACE_REF =
            createTextAttributesKey("KLANG_IDENTIFIER_NAMESPACE_REF",     DefaultLanguageHighlighterColors.IDENTIFIER);
    /** Reference to an enum entry or union member constant. */
    public static final TextAttributesKey IDENTIFIER_ENUM_ENTRY_REF =
            createTextAttributesKey("KLANG_IDENTIFIER_ENUM_ENTRY_REF",    DefaultLanguageHighlighterColors.CONSTANT);
    /** Reference to a template / generic type parameter: T in template&lt;typename T&gt;. */
    public static final TextAttributesKey IDENTIFIER_TEMPLATE_PARAM_REF =
            createTextAttributesKey("KLANG_IDENTIFIER_TEMPLATE_PARAM_REF", DefaultLanguageHighlighterColors.IDENTIFIER);

    // ── Literals ──────────────────────────────────────────────────────────────
    public static final TextAttributesKey LIT_NUMBER =
            createTextAttributesKey("KLANG_NUMBER",          DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey LIT_FLOAT =
            createTextAttributesKey("KLANG_FLOAT",           DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey LIT_STRING =
            createTextAttributesKey("KLANG_STRING",          DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LIT_CHAR =
            createTextAttributesKey("KLANG_CHAR",            DefaultLanguageHighlighterColors.STRING);
    /** true / false / null — highlighted like predefined symbols */
    public static final TextAttributesKey LIT_KEYWORD =
            createTextAttributesKey("KLANG_KEYWORD_LITERAL", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);

    // ── Operators ─────────────────────────────────────────────────────────────
    public static final TextAttributesKey OPERATOR_ARITH =
            createTextAttributesKey("KLANG_OP_ARITH",   DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OPERATOR_ASSIGN =
            createTextAttributesKey("KLANG_OP_ASSIGN",  DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OPERATOR_COMPARE =
            createTextAttributesKey("KLANG_OP_COMPARE", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OPERATOR_LOGICAL =
            createTextAttributesKey("KLANG_OP_LOGICAL", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OPERATOR_BITWISE =
            createTextAttributesKey("KLANG_OP_BITWISE", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OPERATOR_INCDEC =
            createTextAttributesKey("KLANG_OP_INCDEC",  DefaultLanguageHighlighterColors.OPERATION_SIGN);
    /** Member access: . -> .* ->* */
    public static final TextAttributesKey OPERATOR_MEMBER =
            createTextAttributesKey("KLANG_OP_MEMBER",  DefaultLanguageHighlighterColors.DOT);

    // ── Punctuators ───────────────────────────────────────────────────────────
    public static final TextAttributesKey PUNC_PAREN =
            createTextAttributesKey("KLANG_PUNC_PAREN",     DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey PUNC_BRACE =
            createTextAttributesKey("KLANG_PUNC_BRACE",     DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey PUNC_BRACKET =
            createTextAttributesKey("KLANG_PUNC_BRACKET",   DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PUNC_SEMICOLON =
            createTextAttributesKey("KLANG_PUNC_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey PUNC_COMMA =
            createTextAttributesKey("KLANG_PUNC_COMMA",     DefaultLanguageHighlighterColors.COMMA);
    /** Scope resolution :: */
    public static final TextAttributesKey PUNC_SCOPE =
            createTextAttributesKey("KLANG_PUNC_SCOPE",     DefaultLanguageHighlighterColors.DOT);
    /** Annotation marker @ */
    public static final TextAttributesKey PUNC_ANNOTATION =
            createTextAttributesKey("KLANG_PUNC_ANNOTATION", DefaultLanguageHighlighterColors.METADATA);
    /** Other punctuation: ... ? : */
    public static final TextAttributesKey PUNC_OTHER =
            createTextAttributesKey("KLANG_PUNC_OTHER",     DefaultLanguageHighlighterColors.OPERATION_SIGN);
    /**
     * Template angle brackets: the {@code <} and {@code >} delimiting template parameter lists
     * (template&lt;T&gt;) and template argument lists (Box&lt;T&gt;, max&lt;int&gt;(…)).
     * Applied by the annotator — the lexer cannot distinguish these from comparison operators.
     * NOTE: brace pairing (OP_LT/OP_GT in KlangBraceMatcher) applies to ALL &lt;&gt; pairs until
     *       dedicated TEMPLATE_LT/TEMPLATE_GT tokens are introduced in the grammar.
     */
    public static final TextAttributesKey PUNC_TEMPLATE_BRACKET =
            createTextAttributesKey("KLANG_PUNC_TEMPLATE_BRACKET", DefaultLanguageHighlighterColors.BRACKETS);

    // ── Errors ────────────────────────────────────────────────────────────────
    /** Lexer bad character (unrecognised input) */
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("KLANG_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    // ── Token → keys mapping ─────────────────────────────────────────────────

    private static final TextAttributesKey[] EMPTY = new TextAttributesKey[0];

    private static final Map<IElementType, TextAttributesKey[]> TOKEN_MAP = new HashMap<>();

    static {
        // comments
        map(LINE_COMMENT,       KlangTypes.LINE_COMMENT);
        map(BLOCK_COMMENT,      KlangTypes.BLOCK_COMMENT);
        map(LINE_DOC_COMMENT,   KlangTypes.LINE_DOC_COMMENT);
        map(BLOCK_DOC_COMMENT,  KlangTypes.BLOCK_DOC_COMMENT);

        // keywords — types
        mapSet(KEYWORD_TYPE,     KlangTokenSets.KEYWORDS_TYPE);
        // keywords — declarations
        mapSet(KEYWORD_DECL,     KlangTokenSets.KEYWORDS_DECL);
        // keywords — modifiers
        mapSet(KEYWORD_MODIFIER, KlangTokenSets.KEYWORDS_MODIFIER);
        // keywords — control flow
        mapSet(KEYWORD_CONTROL,  KlangTokenSets.KEYWORDS_CONTROL);
        // keywords — special
        mapSet(KEYWORD_SPECIAL,  KlangTokenSets.KEYWORDS_SPECIAL);

        // identifier
        map(IDENTIFIER, KlangTypes.IDENTIFIER);

        // literals
        map(LIT_NUMBER,  KlangTypes.LIT_INTEGER);
        map(LIT_FLOAT,   KlangTypes.LIT_FLOAT);
        map(LIT_STRING,  KlangTypes.LIT_STRING);
        map(LIT_CHAR,    KlangTypes.LIT_CHAR);
        map(LIT_KEYWORD, KlangTypes.LIT_TRUE);
        map(LIT_KEYWORD, KlangTypes.LIT_FALSE);
        map(LIT_KEYWORD, KlangTypes.LIT_NULL);

        // operators
        mapSet(OPERATOR_ARITH,   KlangTokenSets.OPERATORS_ARITH);
        mapSet(OPERATOR_ASSIGN,  KlangTokenSets.OPERATORS_ASSIGN);
        mapSet(OPERATOR_COMPARE, KlangTokenSets.OPERATORS_COMPARE);
        mapSet(OPERATOR_LOGICAL, KlangTokenSets.OPERATORS_LOGICAL);
        mapSet(OPERATOR_BITWISE, KlangTokenSets.OPERATORS_BITWISE);
        mapSet(OPERATOR_INCDEC,  KlangTokenSets.OPERATORS_INCDEC);
        mapSet(OPERATOR_MEMBER,  KlangTokenSets.OPERATORS_MEMBER);

        // punctuators
        mapSet(PUNC_PAREN,   KlangTokenSets.PUNC_PARENS);
        mapSet(PUNC_BRACE,   KlangTokenSets.PUNC_BRACES);
        mapSet(PUNC_BRACKET, KlangTokenSets.PUNC_BRACKETS);
        map(PUNC_SEMICOLON,  KlangTypes.PUNC_SEMICOLON);
        map(PUNC_COMMA,      KlangTypes.PUNC_COMMA);
        map(PUNC_SCOPE,      KlangTypes.PUNC_SCOPE);
        map(PUNC_ANNOTATION, KlangTypes.PUNC_AT);
        map(PUNC_OTHER,      KlangTypes.PUNC_ELLIPSIS);
        map(PUNC_OTHER,      KlangTypes.OP_QUESTION);
        map(PUNC_OTHER,      KlangTypes.OP_COLON);

        // errors
        map(BAD_CHARACTER, TokenType.BAD_CHARACTER);
    }

    private static void map(TextAttributesKey key, IElementType type) {
        TOKEN_MAP.put(type, new TextAttributesKey[]{ key });
    }

    private static void mapSet(TextAttributesKey key, com.intellij.psi.tree.TokenSet set) {
        TextAttributesKey[] keys = new TextAttributesKey[]{ key };
        for (IElementType type : set.getTypes()) {
            TOKEN_MAP.put(type, keys);
        }
    }

    // ── SyntaxHighlighter impl ────────────────────────────────────────────────

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new KlangLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return TOKEN_MAP.getOrDefault(tokenType, EMPTY);
    }
}
