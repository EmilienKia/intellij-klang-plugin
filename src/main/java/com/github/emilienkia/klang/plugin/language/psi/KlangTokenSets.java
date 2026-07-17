package com.github.emilienkia.klang.plugin.language.psi;

import com.intellij.psi.tree.TokenSet;

public class KlangTokenSets {

    // ── Comments ──────────────────────────────────────────────────────────────
    public static final TokenSet COMMENTS = TokenSet.create(
            KlangTypes.LINE_COMMENT,
            KlangTypes.BLOCK_COMMENT,
            KlangTypes.LINE_DOC_COMMENT,
            KlangTypes.LINE_DOC_COMMENT_BWD,
            KlangTypes.BLOCK_DOC_COMMENT,
            KlangTypes.BLOCK_DOC_COMMENT_BWD
    );

    // ── Keywords (all) ────────────────────────────────────────────────────────
    public static final TokenSet KEYWORDS = TokenSet.create(
            KlangTypes.KW_BOOL,       KlangTypes.KW_BYTE,      KlangTypes.KW_CHAR,
            KlangTypes.KW_SHORT,      KlangTypes.KW_INT,       KlangTypes.KW_LONG,
            KlangTypes.KW_FLOAT,      KlangTypes.KW_DOUBLE,    KlangTypes.KW_UNSIGNED,
            KlangTypes.KW_STRUCT,     KlangTypes.KW_CLASS,     KlangTypes.KW_INTERFACE,
            KlangTypes.KW_ANNOTATION, KlangTypes.KW_NAMESPACE, KlangTypes.KW_MODULE,
            KlangTypes.KW_IMPORT,     KlangTypes.KW_USING,     KlangTypes.KW_FRIEND,
            KlangTypes.KW_STATIC,     KlangTypes.KW_CONST,     KlangTypes.KW_ABSTRACT,
            KlangTypes.KW_FINAL,      KlangTypes.KW_OVERRIDE,
            KlangTypes.KW_PUBLIC,     KlangTypes.KW_PROTECTED, KlangTypes.KW_PRIVATE,
            KlangTypes.KW_THIS,       KlangTypes.KW_RETURN,
            KlangTypes.KW_IF,         KlangTypes.KW_ELSE,      KlangTypes.KW_WHILE,
            KlangTypes.KW_FOR,        KlangTypes.KW_BREAK,      KlangTypes.KW_CONTINUE,
            KlangTypes.KW_NEW,        KlangTypes.KW_DELETE,
            KlangTypes.KW_DEFAULT,    KlangTypes.KW_ENUM,      KlangTypes.KW_UNION,
            KlangTypes.KW_OPERATOR,
            KlangTypes.KW_TEMPLATE,   KlangTypes.KW_TYPENAME,  KlangTypes.KW_GENERIC,
            KlangTypes.KW_THROW,      KlangTypes.KW_THROWS,    KlangTypes.KW_TRY,
            KlangTypes.KW_CATCH,      KlangTypes.KW_FINALLY
    );

    /** Primitive / built-in types: bool byte char short int long float double unsigned */
    public static final TokenSet KEYWORDS_TYPE = TokenSet.create(
            KlangTypes.KW_BOOL,    KlangTypes.KW_BYTE,  KlangTypes.KW_CHAR,
            KlangTypes.KW_SHORT,   KlangTypes.KW_INT,   KlangTypes.KW_LONG,
            KlangTypes.KW_FLOAT,   KlangTypes.KW_DOUBLE, KlangTypes.KW_UNSIGNED
    );

    /** Declaration keywords: struct class interface annotation namespace module
     *  import using friend enum union template typename generic */
    public static final TokenSet KEYWORDS_DECL = TokenSet.create(
            KlangTypes.KW_STRUCT,     KlangTypes.KW_CLASS,     KlangTypes.KW_INTERFACE,
            KlangTypes.KW_ANNOTATION, KlangTypes.KW_NAMESPACE, KlangTypes.KW_MODULE,
            KlangTypes.KW_IMPORT,     KlangTypes.KW_USING,     KlangTypes.KW_FRIEND,
            KlangTypes.KW_ENUM,       KlangTypes.KW_UNION,
            KlangTypes.KW_TEMPLATE,   KlangTypes.KW_TYPENAME,
            KlangTypes.KW_GENERIC
    );

    /** Modifier keywords: static const abstract final override public protected private */
    public static final TokenSet KEYWORDS_MODIFIER = TokenSet.create(
            KlangTypes.KW_STATIC,    KlangTypes.KW_CONST,
            KlangTypes.KW_ABSTRACT,  KlangTypes.KW_FINAL,     KlangTypes.KW_OVERRIDE,
            KlangTypes.KW_PUBLIC,    KlangTypes.KW_PROTECTED, KlangTypes.KW_PRIVATE
    );

    /** Control-flow keywords: if else while for break continue return throw try catch finally throws */
    public static final TokenSet KEYWORDS_CONTROL = TokenSet.create(
            KlangTypes.KW_IF,     KlangTypes.KW_ELSE,
            KlangTypes.KW_WHILE,  KlangTypes.KW_FOR,
            KlangTypes.KW_BREAK,  KlangTypes.KW_CONTINUE,  KlangTypes.KW_RETURN,
            KlangTypes.KW_THROW,  KlangTypes.KW_THROWS,
            KlangTypes.KW_TRY,    KlangTypes.KW_CATCH,    KlangTypes.KW_FINALLY
    );

    /** Special / expression keywords: this new delete default operator */
    public static final TokenSet KEYWORDS_SPECIAL = TokenSet.create(
            KlangTypes.KW_THIS,     KlangTypes.KW_NEW,
            KlangTypes.KW_DELETE,
            KlangTypes.KW_DEFAULT,  KlangTypes.KW_OPERATOR
    );

    // ── Identifiers ───────────────────────────────────────────────────────────
    public static final TokenSet IDENTIFIERS = TokenSet.create(
            KlangTypes.IDENTIFIER
    );

    // ── Literals ──────────────────────────────────────────────────────────────
    /** All literals */
    public static final TokenSet LITERALS = TokenSet.create(
            KlangTypes.LIT_INTEGER, KlangTypes.LIT_FLOAT,
            KlangTypes.LIT_CHAR,    KlangTypes.LIT_STRING,
            KlangTypes.LIT_TRUE,    KlangTypes.LIT_FALSE,
            KlangTypes.LIT_NULL
    );

    /** Numeric literals only */
    public static final TokenSet NUMERIC_LITERALS = TokenSet.create(
            KlangTypes.LIT_INTEGER,
            KlangTypes.LIT_FLOAT
    );

    /** Character & string literals */
    public static final TokenSet STRING_LITERALS = TokenSet.create(
            KlangTypes.LIT_CHAR,
            KlangTypes.LIT_STRING
    );

    /** Boolean & null literals */
    public static final TokenSet KEYWORD_LITERALS = TokenSet.create(
            KlangTypes.LIT_TRUE,
            KlangTypes.LIT_FALSE,
            KlangTypes.LIT_NULL
    );

    // ── Operators ─────────────────────────────────────────────────────────────
    public static final TokenSet OPERATORS = TokenSet.create(
            KlangTypes.OP_PLUS,  KlangTypes.OP_MINUS,  KlangTypes.OP_STAR,
            KlangTypes.OP_DIV,   KlangTypes.OP_PERCENT, KlangTypes.OP_POW,
            KlangTypes.OP_AMP,   KlangTypes.OP_PIPE,    KlangTypes.OP_CARET,
            KlangTypes.OP_TILDE, KlangTypes.OP_HASH,
            KlangTypes.OP_LSHIFT, KlangTypes.OP_RSHIFT,
            KlangTypes.OP_AND,   KlangTypes.OP_OR,      KlangTypes.OP_NOT,
            KlangTypes.OP_EQ,    KlangTypes.OP_NEQ,
            KlangTypes.OP_LT,    KlangTypes.OP_GT,      KlangTypes.OP_LE, KlangTypes.OP_GE,
            KlangTypes.OP_SPACESHIP,
            KlangTypes.OP_ASSIGN,
            KlangTypes.OP_PLUS_ASSIGN, KlangTypes.OP_MINUS_ASSIGN, KlangTypes.OP_STAR_ASSIGN,
            KlangTypes.OP_DIV_ASSIGN,  KlangTypes.OP_MOD_ASSIGN,
            KlangTypes.OP_AND_ASSIGN,  KlangTypes.OP_OR_ASSIGN,    KlangTypes.OP_XOR_ASSIGN,
            KlangTypes.OP_LSHIFT_ASSIGN, KlangTypes.OP_RSHIFT_ASSIGN,
            KlangTypes.OP_INC,   KlangTypes.OP_DEC,
            KlangTypes.OP_DOT,   KlangTypes.OP_ARROW,
            KlangTypes.OP_DOT_STAR, KlangTypes.OP_ARROW_STAR,
            KlangTypes.OP_QUESTION, KlangTypes.OP_COLON
    );

    /** Arithmetic: + - * / % ** */
    public static final TokenSet OPERATORS_ARITH = TokenSet.create(
            KlangTypes.OP_PLUS,  KlangTypes.OP_MINUS,
            KlangTypes.OP_STAR,  KlangTypes.OP_DIV,
            KlangTypes.OP_PERCENT, KlangTypes.OP_POW
    );

    /** Assignment: = += -= *= /= %= &= |= ^= <<= >>= */
    public static final TokenSet OPERATORS_ASSIGN = TokenSet.create(
            KlangTypes.OP_ASSIGN,
            KlangTypes.OP_PLUS_ASSIGN,  KlangTypes.OP_MINUS_ASSIGN,
            KlangTypes.OP_STAR_ASSIGN,  KlangTypes.OP_DIV_ASSIGN,
            KlangTypes.OP_MOD_ASSIGN,   KlangTypes.OP_AND_ASSIGN,
            KlangTypes.OP_OR_ASSIGN,    KlangTypes.OP_XOR_ASSIGN,
            KlangTypes.OP_LSHIFT_ASSIGN, KlangTypes.OP_RSHIFT_ASSIGN
    );

    /** Comparison: == != < > <= >= <=> */
    public static final TokenSet OPERATORS_COMPARE = TokenSet.create(
            KlangTypes.OP_EQ,  KlangTypes.OP_NEQ,
            KlangTypes.OP_LT,  KlangTypes.OP_GT,
            KlangTypes.OP_LE,  KlangTypes.OP_GE,
            KlangTypes.OP_SPACESHIP
    );

    /** Logical: && || ! */
    public static final TokenSet OPERATORS_LOGICAL = TokenSet.create(
            KlangTypes.OP_AND, KlangTypes.OP_OR, KlangTypes.OP_NOT
    );

    /** Bitwise: & | ^ ~ << >> # */
    public static final TokenSet OPERATORS_BITWISE = TokenSet.create(
            KlangTypes.OP_AMP,    KlangTypes.OP_PIPE,  KlangTypes.OP_CARET,
            KlangTypes.OP_TILDE,  KlangTypes.OP_HASH,
            KlangTypes.OP_LSHIFT, KlangTypes.OP_RSHIFT
    );

    /** Member access: . -> .* ->* */
    public static final TokenSet OPERATORS_MEMBER = TokenSet.create(
            KlangTypes.OP_DOT,      KlangTypes.OP_ARROW,
            KlangTypes.OP_DOT_STAR, KlangTypes.OP_ARROW_STAR
    );

    /** Increment / decrement: ++ -- */
    public static final TokenSet OPERATORS_INCDEC = TokenSet.create(
            KlangTypes.OP_INC, KlangTypes.OP_DEC
    );

    // ── Punctuators ───────────────────────────────────────────────────────────
    public static final TokenSet PUNCTUATORS = TokenSet.create(
            KlangTypes.PUNC_LPAREN,   KlangTypes.PUNC_RPAREN,
            KlangTypes.PUNC_LBRACE,   KlangTypes.PUNC_RBRACE,
            KlangTypes.PUNC_LBRACKET, KlangTypes.PUNC_RBRACKET,
            KlangTypes.PUNC_SEMICOLON, KlangTypes.PUNC_COMMA,
            KlangTypes.PUNC_SCOPE,    KlangTypes.PUNC_ELLIPSIS,
            KlangTypes.PUNC_AT
    );

    public static final TokenSet PUNC_PARENS = TokenSet.create(
            KlangTypes.PUNC_LPAREN, KlangTypes.PUNC_RPAREN
    );

    public static final TokenSet PUNC_BRACES = TokenSet.create(
            KlangTypes.PUNC_LBRACE, KlangTypes.PUNC_RBRACE
    );

    public static final TokenSet PUNC_BRACKETS = TokenSet.create(
            KlangTypes.PUNC_LBRACKET, KlangTypes.PUNC_RBRACKET
    );
}
