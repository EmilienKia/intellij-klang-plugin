/* ============================================================================
 *  K Language — JFlex Lexer Definition
 *
 *  Copyright 2023-2026 Emilien Kia
 *  Licensed under the Apache License, Version 2.0
 *
 *  Derived from klang.ebnf — the authoritative EBNF reference.
 *  Token names are kept in sync with klang.bnf / KlangTypes.
 *
 *  Regenerate the Java source with:
 *      jflex --skel idea-flex.skeleton src/.../klang.flex
 *  Output goes to src/main/gen/.../KlangLexer.java
 * ============================================================================ */

package com.github.emilienkia.klang.plugin.language;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;

%%

%class KlangLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

// ── Whitespace ────────────────────────────────────────────────────────────────
WHITE_SPACE             = [ \t\f\r\n]+

// ── Comments ──────────────────────────────────────────────────────────────────
// Documentation comments follow the Javadoc / Doxygen formalism. They are NOT part of
// the K grammar (the compiler handles them specially) but are recognised here so the
// editor can highlight them differently from ordinary comments.
//   - forward line doc:   /// …       (exactly three slashes; //// … stays a plain comment)
//   - backward line doc:  //! …       (attaches to the preceding declaration)
//   - forward block doc:  /** … */    (but /**/ is an empty plain block comment, not a doc)
//   - backward block doc: /*! … */    (attaches to the preceding declaration)
LINE_DOC_COMMENT        = "///" ( [^/\r\n] [^\r\n]* )?
LINE_DOC_COMMENT_BWD    = "//!" [^\r\n]*
BLOCK_DOC_COMMENT       = "/**" ~"*/"
BLOCK_DOC_COMMENT_BWD   = "/*!" ~"*/"
LINE_COMMENT            = "//" [^\r\n]*
BLOCK_COMMENT           = "/*" ([^*] | "*"+ [^/*])* "*"+ "/"

// ── Identifiers ───────────────────────────────────────────────────────────────
IDENT_START             = [A-Za-z_]
IDENT_PART              = [A-Za-z_0-9]
IDENTIFIER              = {IDENT_START} {IDENT_PART}*

// ── Integer literals ──────────────────────────────────────────────────────────
// EBNF IntegerSuffix: u | l | ul | lu | s | b  (case-insensitive)
INT_SUFFIX              = [uU][lL]? | [lL][uU]? | [sS] | [bB]
HEX_LITERAL             = "0x" [0-9A-Fa-f]+ {INT_SUFFIX}?
BIN_LITERAL             = "0b" [01]+         {INT_SUFFIX}?
OCT_LITERAL             = "0"  [0-7]+        {INT_SUFFIX}?
DEC_LITERAL             = [0-9]+             {INT_SUFFIX}?
LIT_INTEGER             = {HEX_LITERAL} | {BIN_LITERAL} | {OCT_LITERAL} | {DEC_LITERAL}

// ── Float literals ────────────────────────────────────────────────────────────
FLOAT_SUFFIX            = [fFdD]
LIT_FLOAT               = [0-9]+ "." [0-9]* {FLOAT_SUFFIX}?
                        | [0-9]+ ("." [0-9]*)? [eE] [+\-]? [0-9]+ {FLOAT_SUFFIX}?

// ── Character / String literals ───────────────────────────────────────────────
// EBNF EncodingPrefix: u8 | u16 | u32 | u | U  (must be adjacent to the quote)
ENC_PREFIX              = "u8" | "u16" | "u32" | "u" | "U"
// EBNF EscapeSequence: simple, octal, \x hex, \u 16-bit, \U 32-bit
ESCAPE_SEQ              = "\\" ( [nrtabfv\\'\"?0]
                              | [0-7] [0-7]? [0-7]?
                              | "x" [0-9A-Fa-f]+
                              | "u" [0-9A-Fa-f]{4}
                              | "U" [0-9A-Fa-f]{8} )
LIT_CHAR                = {ENC_PREFIX}? "'" ( {ESCAPE_SEQ} | [^'\\] ) "'"
LIT_STRING              = {ENC_PREFIX}? "\"" ( {ESCAPE_SEQ} | [^\"\\] )* "\""

%%

// ============================================================================
//  Rules
// ============================================================================

<YYINITIAL> {

    // ── Whitespace ────────────────────────────────────────────────────────
    {WHITE_SPACE}               { return TokenType.WHITE_SPACE; }

    // ── Comments ──────────────────────────────────────────────────────────
    // Documentation comments must be tried BEFORE the ordinary comment rules:
    // for equal-length matches JFlex picks the rule listed first.
    {LINE_DOC_COMMENT}          { return KlangTypes.LINE_DOC_COMMENT; }
    {LINE_DOC_COMMENT_BWD}      { return KlangTypes.LINE_DOC_COMMENT_BWD; }
    {BLOCK_DOC_COMMENT}         { return KlangTypes.BLOCK_DOC_COMMENT; }
    {BLOCK_DOC_COMMENT_BWD}     { return KlangTypes.BLOCK_DOC_COMMENT_BWD; }
    {LINE_COMMENT}              { return KlangTypes.LINE_COMMENT; }
    {BLOCK_COMMENT}             { return KlangTypes.BLOCK_COMMENT; }

    // ── Keywords (must come before IDENTIFIER rule) ────────────────────────
    "bool"                      { return KlangTypes.KW_BOOL; }
    "byte"                      { return KlangTypes.KW_BYTE; }
    "char"                      { return KlangTypes.KW_CHAR; }
    "short"                     { return KlangTypes.KW_SHORT; }
    "int"                       { return KlangTypes.KW_INT; }
    "long"                      { return KlangTypes.KW_LONG; }
    "float"                     { return KlangTypes.KW_FLOAT; }
    "double"                    { return KlangTypes.KW_DOUBLE; }
    "unsigned"                  { return KlangTypes.KW_UNSIGNED; }
    "struct"                    { return KlangTypes.KW_STRUCT; }
    "class"                     { return KlangTypes.KW_CLASS; }
    "interface"                 { return KlangTypes.KW_INTERFACE; }
    "annotation"                { return KlangTypes.KW_ANNOTATION; }
    "namespace"                 { return KlangTypes.KW_NAMESPACE; }
    "module"                    { return KlangTypes.KW_MODULE; }
    "import"                    { return KlangTypes.KW_IMPORT; }
    "using"                     { return KlangTypes.KW_USING; }
    "friend"                    { return KlangTypes.KW_FRIEND; }
    "static"                    { return KlangTypes.KW_STATIC; }
    "const"                     { return KlangTypes.KW_CONST; }
    "abstract"                  { return KlangTypes.KW_ABSTRACT; }
    "final"                     { return KlangTypes.KW_FINAL; }
    "override"                  { return KlangTypes.KW_OVERRIDE; }
    "public"                    { return KlangTypes.KW_PUBLIC; }
    "protected"                 { return KlangTypes.KW_PROTECTED; }
    "private"                   { return KlangTypes.KW_PRIVATE; }
    "this"                      { return KlangTypes.KW_THIS; }
    "return"                    { return KlangTypes.KW_RETURN; }
    "if"                        { return KlangTypes.KW_IF; }
    "else"                      { return KlangTypes.KW_ELSE; }
    "while"                     { return KlangTypes.KW_WHILE; }
    "for"                       { return KlangTypes.KW_FOR; }
    "break"                     { return KlangTypes.KW_BREAK; }
    "continue"                  { return KlangTypes.KW_CONTINUE; }
    "new"                       { return KlangTypes.KW_NEW; }
    "delete"                    { return KlangTypes.KW_DELETE; }
    "default"                   { return KlangTypes.KW_DEFAULT; }
    "enum"                      { return KlangTypes.KW_ENUM; }
    "union"                     { return KlangTypes.KW_UNION; }
    "operator"                  { return KlangTypes.KW_OPERATOR; }
    "template"                  { return KlangTypes.KW_TEMPLATE; }
    "typename"                  { return KlangTypes.KW_TYPENAME; }
    "generic"                   { return KlangTypes.KW_GENERIC; }

    // ── Exception handling ────────────────────────────────────────────────
    "throw"                     { return KlangTypes.KW_THROW; }
    "throws"                    { return KlangTypes.KW_THROWS; }
    "try"                       { return KlangTypes.KW_TRY; }
    "catch"                     { return KlangTypes.KW_CATCH; }
    "finally"                   { return KlangTypes.KW_FINALLY; }

    // ── Boolean / null literals ───────────────────────────────────────────
    "true"                      { return KlangTypes.LIT_TRUE; }
    "false"                     { return KlangTypes.LIT_FALSE; }
    "null"                      { return KlangTypes.LIT_NULL; }

    // ── Numeric / character / string literals ─────────────────────────────
    {LIT_FLOAT}                 { return KlangTypes.LIT_FLOAT; }
    {LIT_INTEGER}               { return KlangTypes.LIT_INTEGER; }
    {LIT_CHAR}                  { return KlangTypes.LIT_CHAR; }
    {LIT_STRING}                { return KlangTypes.LIT_STRING; }

    // ── Identifier ────────────────────────────────────────────────────────
    {IDENTIFIER}                { return KlangTypes.IDENTIFIER; }

    // ── Three-char operators (longest match first) ────────────────────────
    "->*"                       { return KlangTypes.OP_ARROW_STAR; }
    "<<="                       { return KlangTypes.OP_LSHIFT_ASSIGN; }
    ">>="                       { return KlangTypes.OP_RSHIFT_ASSIGN; }
    "<=>"                       { return KlangTypes.OP_SPACESHIP; }
    "..."                       { return KlangTypes.PUNC_ELLIPSIS; }

    // ── Two-char operators ────────────────────────────────────────────────
    "->"                        { return KlangTypes.OP_ARROW; }
    ".*"                        { return KlangTypes.OP_DOT_STAR; }
    "<<"                        { return KlangTypes.OP_LSHIFT; }
    ">>"                        { return KlangTypes.OP_RSHIFT; }
    "++"                        { return KlangTypes.OP_INC; }
    "--"                        { return KlangTypes.OP_DEC; }
    "**"                        { return KlangTypes.OP_POW; }
    "+="                        { return KlangTypes.OP_PLUS_ASSIGN; }
    "-="                        { return KlangTypes.OP_MINUS_ASSIGN; }
    "*="                        { return KlangTypes.OP_STAR_ASSIGN; }
    "/="                        { return KlangTypes.OP_DIV_ASSIGN; }
    "%="                        { return KlangTypes.OP_MOD_ASSIGN; }
    "&="                        { return KlangTypes.OP_AND_ASSIGN; }
    "|="                        { return KlangTypes.OP_OR_ASSIGN; }
    "^="                        { return KlangTypes.OP_XOR_ASSIGN; }
    "=="                        { return KlangTypes.OP_EQ; }
    "!="                        { return KlangTypes.OP_NEQ; }
    "<="                        { return KlangTypes.OP_LE; }
    ">="                        { return KlangTypes.OP_GE; }
    "&&"                        { return KlangTypes.OP_AND; }
    "||"                        { return KlangTypes.OP_OR; }
    "::"                        { return KlangTypes.PUNC_SCOPE; }

    // ── Single-char operators ─────────────────────────────────────────────
    "."                         { return KlangTypes.OP_DOT; }
    "?"                         { return KlangTypes.OP_QUESTION; }
    ":"                         { return KlangTypes.OP_COLON; }
    "!"                         { return KlangTypes.OP_NOT; }
    "~"                         { return KlangTypes.OP_TILDE; }
    "="                         { return KlangTypes.OP_ASSIGN; }
    "+"                         { return KlangTypes.OP_PLUS; }
    "-"                         { return KlangTypes.OP_MINUS; }
    "*"                         { return KlangTypes.OP_STAR; }
    "/"                         { return KlangTypes.OP_DIV; }
    "&"                         { return KlangTypes.OP_AMP; }
    "|"                         { return KlangTypes.OP_PIPE; }
    "^"                         { return KlangTypes.OP_CARET; }
    "%"                         { return KlangTypes.OP_PERCENT; }
    "#"                         { return KlangTypes.OP_HASH; }
    "<"                         { return KlangTypes.OP_LT; }
    ">"                         { return KlangTypes.OP_GT; }

    // ── Punctuators ───────────────────────────────────────────────────────
    "("                         { return KlangTypes.PUNC_LPAREN; }
    ")"                         { return KlangTypes.PUNC_RPAREN; }
    "{"                         { return KlangTypes.PUNC_LBRACE; }
    "}"                         { return KlangTypes.PUNC_RBRACE; }
    "["                         { return KlangTypes.PUNC_LBRACKET; }
    "]"                         { return KlangTypes.PUNC_RBRACKET; }
    ";"                         { return KlangTypes.PUNC_SEMICOLON; }
    ","                         { return KlangTypes.PUNC_COMMA; }
    "@"                         { return KlangTypes.PUNC_AT; }

    // ── Fall-through ──────────────────────────────────────────────────────
    [^]                         { return TokenType.BAD_CHARACTER; }
}

