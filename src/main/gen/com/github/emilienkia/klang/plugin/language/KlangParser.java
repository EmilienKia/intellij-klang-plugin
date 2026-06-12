// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.github.emilienkia.klang.plugin.language.psi.KlangTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class KlangParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return unit(b, l + 1);
  }

  /* ********************************************************** */
  // multiplicativeExpr (('+' | '-') multiplicativeExpr)*
  public static boolean additiveExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ADDITIVE_EXPR, "<additive expression>");
    r = multiplicativeExpr(b, l + 1);
    r = r && additiveExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('+' | '-') multiplicativeExpr)*
  private static boolean additiveExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!additiveExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "additiveExpr_1", c)) break;
    }
    return true;
  }

  // ('+' | '-') multiplicativeExpr
  private static boolean additiveExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = additiveExpr_1_0_0(b, l + 1);
    r = r && multiplicativeExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '+' | '-'
  private static boolean additiveExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_PLUS);
    if (!r) r = consumeToken(b, OP_MINUS);
    return r;
  }

  /* ********************************************************** */
  // annotationDef* templateDeclaration? specifier*
  //                   ('struct' | 'class' | 'interface' | 'annotation')
  //                   IDENTIFIER
  //                   (':' baseClause)?
  //                   '{' declaration* '}'
  public static boolean aggregateDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, AGGREGATE_DECL, "<struct/class/interface/annotation declaration>");
    r = aggregateDecl_0(b, l + 1);
    r = r && aggregateDecl_1(b, l + 1);
    r = r && aggregateDecl_2(b, l + 1);
    r = r && aggregateDecl_3(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && aggregateDecl_5(b, l + 1);
    r = r && consumeToken(b, PUNC_LBRACE);
    r = r && aggregateDecl_7(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotationDef*
  private static boolean aggregateDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotationDef(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "aggregateDecl_0", c)) break;
    }
    return true;
  }

  // templateDeclaration?
  private static boolean aggregateDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl_1")) return false;
    templateDeclaration(b, l + 1);
    return true;
  }

  // specifier*
  private static boolean aggregateDecl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!specifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "aggregateDecl_2", c)) break;
    }
    return true;
  }

  // 'struct' | 'class' | 'interface' | 'annotation'
  private static boolean aggregateDecl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl_3")) return false;
    boolean r;
    r = consumeToken(b, KW_STRUCT);
    if (!r) r = consumeToken(b, KW_CLASS);
    if (!r) r = consumeToken(b, KW_INTERFACE);
    if (!r) r = consumeToken(b, KW_ANNOTATION);
    return r;
  }

  // (':' baseClause)?
  private static boolean aggregateDecl_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl_5")) return false;
    aggregateDecl_5_0(b, l + 1);
    return true;
  }

  // ':' baseClause
  private static boolean aggregateDecl_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_COLON);
    r = r && baseClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // declaration*
  private static boolean aggregateDecl_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregateDecl_7")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "aggregateDecl_7", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '@' qualifiedIdentifier
  //                 | '@' qualifiedIdentifier '(' expressionList? ')'
  //                 | '@' qualifiedIdentifier braceInitList
  public static boolean annotationDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationDef")) return false;
    if (!nextTokenIs(b, "<annotation>", PUNC_AT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_DEF, "<annotation>");
    r = annotationDef_0(b, l + 1);
    if (!r) r = annotationDef_1(b, l + 1);
    if (!r) r = annotationDef_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '@' qualifiedIdentifier
  private static boolean annotationDef_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationDef_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_AT);
    r = r && qualifiedIdentifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '@' qualifiedIdentifier '(' expressionList? ')'
  private static boolean annotationDef_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationDef_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_AT);
    r = r && qualifiedIdentifier(b, l + 1);
    r = r && consumeToken(b, PUNC_LPAREN);
    r = r && annotationDef_1_3(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean annotationDef_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationDef_1_3")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // '@' qualifiedIdentifier braceInitList
  private static boolean annotationDef_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationDef_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_AT);
    r = r && qualifiedIdentifier(b, l + 1);
    r = r && braceInitList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // conditionalExpr (assignmentOperator assignmentExpr)?
  public static boolean assignmentExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_EXPR, "<assignment expression>");
    r = conditionalExpr(b, l + 1);
    r = r && assignmentExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (assignmentOperator assignmentExpr)?
  private static boolean assignmentExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentExpr_1")) return false;
    assignmentExpr_1_0(b, l + 1);
    return true;
  }

  // assignmentOperator assignmentExpr
  private static boolean assignmentExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = assignmentOperator(b, l + 1);
    r = r && assignmentExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '=' | '*=' | '/=' | '%=' | '+=' | '-='
  //                      | '>>=' | '<<=' | '&=' | '^=' | '|='
  public static boolean assignmentOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_OPERATOR, "<assignment operator>");
    r = consumeToken(b, OP_ASSIGN);
    if (!r) r = consumeToken(b, OP_STAR_ASSIGN);
    if (!r) r = consumeToken(b, OP_DIV_ASSIGN);
    if (!r) r = consumeToken(b, OP_MOD_ASSIGN);
    if (!r) r = consumeToken(b, OP_PLUS_ASSIGN);
    if (!r) r = consumeToken(b, OP_MINUS_ASSIGN);
    if (!r) r = consumeToken(b, OP_RSHIFT_ASSIGN);
    if (!r) r = consumeToken(b, OP_LSHIFT_ASSIGN);
    if (!r) r = consumeToken(b, OP_AND_ASSIGN);
    if (!r) r = consumeToken(b, OP_XOR_ASSIGN);
    if (!r) r = consumeToken(b, OP_OR_ASSIGN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // baseSpec (',' baseSpec)*
  public static boolean baseClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "baseClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BASE_CLAUSE, "<base class list>");
    r = baseSpec(b, l + 1);
    r = r && baseClause_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' baseSpec)*
  private static boolean baseClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "baseClause_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!baseClause_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "baseClause_1", c)) break;
    }
    return true;
  }

  // ',' baseSpec
  private static boolean baseClause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "baseClause_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && baseSpec(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ('public' | 'protected' | 'private')? qualifiedIdentifier
  public static boolean baseSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "baseSpec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BASE_SPEC, "<base class specifier>");
    r = baseSpec_0(b, l + 1);
    r = r && qualifiedIdentifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('public' | 'protected' | 'private')?
  private static boolean baseSpec_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "baseSpec_0")) return false;
    baseSpec_0_0(b, l + 1);
    return true;
  }

  // 'public' | 'protected' | 'private'
  private static boolean baseSpec_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "baseSpec_0_0")) return false;
    boolean r;
    r = consumeToken(b, KW_PUBLIC);
    if (!r) r = consumeToken(b, KW_PROTECTED);
    if (!r) r = consumeToken(b, KW_PRIVATE);
    return r;
  }

  /* ********************************************************** */
  // equalityExpr ('&' equalityExpr)*
  public static boolean binAndExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binAndExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BIN_AND_EXPR, "<bitwise-and expression>");
    r = equalityExpr(b, l + 1);
    r = r && binAndExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('&' equalityExpr)*
  private static boolean binAndExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binAndExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!binAndExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "binAndExpr_1", c)) break;
    }
    return true;
  }

  // '&' equalityExpr
  private static boolean binAndExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binAndExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_AMP);
    r = r && equalityExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' statement* '}'
  public static boolean blockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement")) return false;
    if (!nextTokenIs(b, "<block { … }>", PUNC_LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_STATEMENT, "<block { … }>");
    r = consumeToken(b, PUNC_LBRACE);
    r = r && blockStatement_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // statement*
  private static boolean blockStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStatement_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "blockStatement_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '{' '}'
  //                 | '{' initElement (',' initElement)* '}'
  //                 | '{' designatedInitElement (',' designatedInitElement)* '}'
  public static boolean braceInitList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braceInitList")) return false;
    if (!nextTokenIs(b, "<brace initializer list { … }>", PUNC_LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BRACE_INIT_LIST, "<brace initializer list { … }>");
    r = parseTokens(b, 0, PUNC_LBRACE, PUNC_RBRACE);
    if (!r) r = braceInitList_1(b, l + 1);
    if (!r) r = braceInitList_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '{' initElement (',' initElement)* '}'
  private static boolean braceInitList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braceInitList_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LBRACE);
    r = r && initElement(b, l + 1);
    r = r && braceInitList_1_2(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' initElement)*
  private static boolean braceInitList_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braceInitList_1_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!braceInitList_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "braceInitList_1_2", c)) break;
    }
    return true;
  }

  // ',' initElement
  private static boolean braceInitList_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braceInitList_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && initElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '{' designatedInitElement (',' designatedInitElement)* '}'
  private static boolean braceInitList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braceInitList_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LBRACE);
    r = r && designatedInitElement(b, l + 1);
    r = r && braceInitList_2_2(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' designatedInitElement)*
  private static boolean braceInitList_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braceInitList_2_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!braceInitList_2_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "braceInitList_2_2", c)) break;
    }
    return true;
  }

  // ',' designatedInitElement
  private static boolean braceInitList_2_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braceInitList_2_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && designatedInitElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'break' ';'
  public static boolean breakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement")) return false;
    if (!nextTokenIs(b, "<break statement>", KW_BREAK)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BREAK_STATEMENT, "<break statement>");
    r = consumeTokens(b, 0, KW_BREAK, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' typeSpec ')' castExpr
  //            | unaryExpr
  public static boolean castExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "castExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CAST_EXPR, "<cast expression>");
    r = castExpr_0(b, l + 1);
    if (!r) r = unaryExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' typeSpec ')' castExpr
  private static boolean castExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "castExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && typeSpec(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && castExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'catch' '(' catchParameterDecl ')' blockStatement
  public static boolean catchClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchClause")) return false;
    if (!nextTokenIs(b, "<catch clause>", KW_CATCH)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CLAUSE, "<catch clause>");
    r = consumeTokens(b, 0, KW_CATCH, PUNC_LPAREN);
    r = r && catchParameterDecl(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && blockStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'const'? IDENTIFIER ':' typeSpec
  public static boolean catchParameterDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchParameterDecl")) return false;
    if (!nextTokenIs(b, "<catch parameter declaration>", IDENTIFIER, KW_CONST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CATCH_PARAMETER_DECL, "<catch parameter declaration>");
    r = catchParameterDecl_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, OP_COLON);
    r = r && typeSpec(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'const'?
  private static boolean catchParameterDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchParameterDecl_0")) return false;
    consumeToken(b, KW_CONST);
    return true;
  }

  /* ********************************************************** */
  // '=' conditionalExpr
  //                      | '(' expressionList? ')'
  //                      | braceInitList
  public static boolean condVarInitialiser(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condVarInitialiser")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COND_VAR_INITIALISER, "<condition variable initializer>");
    r = condVarInitialiser_0(b, l + 1);
    if (!r) r = condVarInitialiser_1(b, l + 1);
    if (!r) r = braceInitList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '=' conditionalExpr
  private static boolean condVarInitialiser_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condVarInitialiser_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ASSIGN);
    r = r && conditionalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' expressionList? ')'
  private static boolean condVarInitialiser_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condVarInitialiser_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && condVarInitialiser_1_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean condVarInitialiser_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condVarInitialiser_1_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // logicalOrExpr ('?' conditionalExpr ':' conditionalExpr)?
  public static boolean conditionalExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL_EXPR, "<conditional expression>");
    r = logicalOrExpr(b, l + 1);
    r = r && conditionalExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('?' conditionalExpr ':' conditionalExpr)?
  private static boolean conditionalExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalExpr_1")) return false;
    conditionalExpr_1_0(b, l + 1);
    return true;
  }

  // '?' conditionalExpr ':' conditionalExpr
  private static boolean conditionalExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_QUESTION);
    r = r && conditionalExpr(b, l + 1);
    r = r && consumeToken(b, OP_COLON);
    r = r && conditionalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // visibilityDecl
  //               | namespaceDecl
  //               | usingDecl
  //               | friendDecl
  //               | aggregateDecl
  //               | enumDecl
  //               | unionDecl
  //               | functionDecl
  //               | variableDecl
  public static boolean declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DECLARATION, "<declaration>");
    r = visibilityDecl(b, l + 1);
    if (!r) r = namespaceDecl(b, l + 1);
    if (!r) r = usingDecl(b, l + 1);
    if (!r) r = friendDecl(b, l + 1);
    if (!r) r = aggregateDecl(b, l + 1);
    if (!r) r = enumDecl(b, l + 1);
    if (!r) r = unionDecl(b, l + 1);
    if (!r) r = functionDecl(b, l + 1);
    if (!r) r = variableDecl(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '.' designatedMemberName '=' conditionalExpr
  //                         | '.' designatedMemberName '=' braceInitList
  //                         | '.' designatedMemberName '(' expressionList? ')'
  public static boolean designatedInitElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedInitElement")) return false;
    if (!nextTokenIs(b, "<designated initializer (.member = …)>", OP_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DESIGNATED_INIT_ELEMENT, "<designated initializer (.member = …)>");
    r = designatedInitElement_0(b, l + 1);
    if (!r) r = designatedInitElement_1(b, l + 1);
    if (!r) r = designatedInitElement_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '.' designatedMemberName '=' conditionalExpr
  private static boolean designatedInitElement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedInitElement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_DOT);
    r = r && designatedMemberName(b, l + 1);
    r = r && consumeToken(b, OP_ASSIGN);
    r = r && conditionalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '.' designatedMemberName '=' braceInitList
  private static boolean designatedInitElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedInitElement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_DOT);
    r = r && designatedMemberName(b, l + 1);
    r = r && consumeToken(b, OP_ASSIGN);
    r = r && braceInitList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '.' designatedMemberName '(' expressionList? ')'
  private static boolean designatedInitElement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedInitElement_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_DOT);
    r = r && designatedMemberName(b, l + 1);
    r = r && consumeToken(b, PUNC_LPAREN);
    r = r && designatedInitElement_2_3(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean designatedInitElement_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedInitElement_2_3")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (IDENTIFIER '::')* IDENTIFIER
  public static boolean designatedMemberName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedMemberName")) return false;
    if (!nextTokenIs(b, "<member name>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DESIGNATED_MEMBER_NAME, "<member name>");
    r = designatedMemberName_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (IDENTIFIER '::')*
  private static boolean designatedMemberName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedMemberName_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!designatedMemberName_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "designatedMemberName_0", c)) break;
    }
    return true;
  }

  // IDENTIFIER '::'
  private static boolean designatedMemberName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatedMemberName_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, PUNC_SCOPE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '~' IDENTIFIER
  public static boolean destructorHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "destructorHead")) return false;
    if (!nextTokenIs(b, "<destructor name (~Name)>", OP_TILDE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DESTRUCTOR_HEAD, "<destructor name (~Name)>");
    r = consumeTokens(b, 0, OP_TILDE, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // specifier* 'enum' IDENTIFIER (':' typeSpec)?
  //              '{' enumEntry* '}' ';'
  public static boolean enumDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENUM_DECL, "<enum declaration>");
    r = enumDecl_0(b, l + 1);
    r = r && consumeTokens(b, 0, KW_ENUM, IDENTIFIER);
    r = r && enumDecl_3(b, l + 1);
    r = r && consumeToken(b, PUNC_LBRACE);
    r = r && enumDecl_5(b, l + 1);
    r = r && consumeTokens(b, 0, PUNC_RBRACE, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // specifier*
  private static boolean enumDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDecl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!specifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDecl_0", c)) break;
    }
    return true;
  }

  // (':' typeSpec)?
  private static boolean enumDecl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDecl_3")) return false;
    enumDecl_3_0(b, l + 1);
    return true;
  }

  // ':' typeSpec
  private static boolean enumDecl_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDecl_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_COLON);
    r = r && typeSpec(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // enumEntry*
  private static boolean enumDecl_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDecl_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumEntry(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDecl_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER ('=' (LIT_INTEGER | IDENTIFIER) | braceInitList | '(' expressionList? ')')? 'default'? ';'
  public static boolean enumEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry")) return false;
    if (!nextTokenIs(b, "<enum entry>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENUM_ENTRY, "<enum entry>");
    r = consumeToken(b, IDENTIFIER);
    r = r && enumEntry_1(b, l + 1);
    r = r && enumEntry_2(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('=' (LIT_INTEGER | IDENTIFIER) | braceInitList | '(' expressionList? ')')?
  private static boolean enumEntry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry_1")) return false;
    enumEntry_1_0(b, l + 1);
    return true;
  }

  // '=' (LIT_INTEGER | IDENTIFIER) | braceInitList | '(' expressionList? ')'
  private static boolean enumEntry_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumEntry_1_0_0(b, l + 1);
    if (!r) r = braceInitList(b, l + 1);
    if (!r) r = enumEntry_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '=' (LIT_INTEGER | IDENTIFIER)
  private static boolean enumEntry_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ASSIGN);
    r = r && enumEntry_1_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LIT_INTEGER | IDENTIFIER
  private static boolean enumEntry_1_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry_1_0_0_1")) return false;
    boolean r;
    r = consumeToken(b, LIT_INTEGER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    return r;
  }

  // '(' expressionList? ')'
  private static boolean enumEntry_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry_1_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && enumEntry_1_0_2_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean enumEntry_1_0_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry_1_0_2_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // 'default'?
  private static boolean enumEntry_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEntry_2")) return false;
    consumeToken(b, KW_DEFAULT);
    return true;
  }

  /* ********************************************************** */
  // relationalExpr (('==' | '!=') relationalExpr)*
  public static boolean equalityExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EQUALITY_EXPR, "<equality expression>");
    r = relationalExpr(b, l + 1);
    r = r && equalityExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('==' | '!=') relationalExpr)*
  private static boolean equalityExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!equalityExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "equalityExpr_1", c)) break;
    }
    return true;
  }

  // ('==' | '!=') relationalExpr
  private static boolean equalityExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = equalityExpr_1_0_0(b, l + 1);
    r = r && relationalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '==' | '!='
  private static boolean equalityExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_EQ);
    if (!r) r = consumeToken(b, OP_NEQ);
    return r;
  }

  /* ********************************************************** */
  // binAndExpr ('^' binAndExpr)*
  public static boolean exclusiveBinOrExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exclusiveBinOrExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXCLUSIVE_BIN_OR_EXPR, "<bitwise-xor expression>");
    r = binAndExpr(b, l + 1);
    r = r && exclusiveBinOrExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('^' binAndExpr)*
  private static boolean exclusiveBinOrExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exclusiveBinOrExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!exclusiveBinOrExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exclusiveBinOrExpr_1", c)) break;
    }
    return true;
  }

  // '^' binAndExpr
  private static boolean exclusiveBinOrExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exclusiveBinOrExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_CARET);
    r = r && binAndExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // assignmentExpr (',' assignmentExpr)*
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION, "<expression>");
    r = assignmentExpr(b, l + 1);
    r = r && expression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' assignmentExpr)*
  private static boolean expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expression_1", c)) break;
    }
    return true;
  }

  // ',' assignmentExpr
  private static boolean expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && assignmentExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // assignmentExpr (',' assignmentExpr)*
  public static boolean expressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_LIST, "<expression list>");
    r = assignmentExpr(b, l + 1);
    r = r && expressionList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' assignmentExpr)*
  private static boolean expressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expressionList_1", c)) break;
    }
    return true;
  }

  // ',' assignmentExpr
  private static boolean expressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && assignmentExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expression? ';'
  public static boolean expressionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_STATEMENT, "<expression statement>");
    r = expressionStatement_0(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression?
  private static boolean expressionStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionStatement_0")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'finally' blockStatement
  public static boolean finallyClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finallyClause")) return false;
    if (!nextTokenIs(b, "<finally clause>", KW_FINALLY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FINALLY_CLAUSE, "<finally clause>");
    r = consumeToken(b, KW_FINALLY);
    r = r && blockStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'for' '(' (variableDecl | ';') expression? ';' expression? ')' statement
  public static boolean forStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement")) return false;
    if (!nextTokenIs(b, "<for statement>", KW_FOR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_STATEMENT, "<for statement>");
    r = consumeTokens(b, 0, KW_FOR, PUNC_LPAREN);
    r = r && forStatement_2(b, l + 1);
    r = r && forStatement_3(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    r = r && forStatement_5(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // variableDecl | ';'
  private static boolean forStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_2")) return false;
    boolean r;
    r = variableDecl(b, l + 1);
    if (!r) r = consumeToken(b, PUNC_SEMICOLON);
    return r;
  }

  // expression?
  private static boolean forStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_3")) return false;
    expression(b, l + 1);
    return true;
  }

  // expression?
  private static boolean forStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_5")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'friend' friendFilter? qualifiedIdentifier ';'
  public static boolean friendDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "friendDecl")) return false;
    if (!nextTokenIs(b, "<friend declaration>", KW_FRIEND)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FRIEND_DECL, "<friend declaration>");
    r = consumeToken(b, KW_FRIEND);
    r = r && friendDecl_1(b, l + 1);
    r = r && qualifiedIdentifier(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // friendFilter?
  private static boolean friendDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "friendDecl_1")) return false;
    friendFilter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'struct' | 'interface' | 'class'
  public static boolean friendFilter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "friendFilter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FRIEND_FILTER, "<friend filter (struct/interface/class)>");
    r = consumeToken(b, KW_STRUCT);
    if (!r) r = consumeToken(b, KW_INTERFACE);
    if (!r) r = consumeToken(b, KW_CLASS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // blockStatement
  //                | '->' ('default' | 'delete') ';'
  //                | '->' qualifiedIdentifier ('(' typeSpecList? ')')? ';'
  //                | ';'
  public static boolean functionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_BODY, "<function body>");
    r = blockStatement(b, l + 1);
    if (!r) r = functionBody_1(b, l + 1);
    if (!r) r = functionBody_2(b, l + 1);
    if (!r) r = consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '->' ('default' | 'delete') ';'
  private static boolean functionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ARROW);
    r = r && functionBody_1_1(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'default' | 'delete'
  private static boolean functionBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_1_1")) return false;
    boolean r;
    r = consumeToken(b, KW_DEFAULT);
    if (!r) r = consumeToken(b, KW_DELETE);
    return r;
  }

  // '->' qualifiedIdentifier ('(' typeSpecList? ')')? ';'
  private static boolean functionBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ARROW);
    r = r && qualifiedIdentifier(b, l + 1);
    r = r && functionBody_2_2(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('(' typeSpecList? ')')?
  private static boolean functionBody_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_2_2")) return false;
    functionBody_2_2_0(b, l + 1);
    return true;
  }

  // '(' typeSpecList? ')'
  private static boolean functionBody_2_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_2_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && functionBody_2_2_0_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeSpecList?
  private static boolean functionBody_2_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_2_2_0_1")) return false;
    typeSpecList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // annotationDef* (templateDeclaration | genericDeclaration)? specifier*
  //                  (functionHead | operatorFunctionHead | destructorHead)
  //                  '(' parameterList? ')'
  //                  namedReturnVar?
  //                  (':' returnTypeOrMemberInitList)?
  //                  throwsClause?
  //                  functionBody
  public static boolean functionDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DECL, "<function declaration>");
    r = functionDecl_0(b, l + 1);
    r = r && functionDecl_1(b, l + 1);
    r = r && functionDecl_2(b, l + 1);
    r = r && functionDecl_3(b, l + 1);
    r = r && consumeToken(b, PUNC_LPAREN);
    r = r && functionDecl_5(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && functionDecl_7(b, l + 1);
    r = r && functionDecl_8(b, l + 1);
    r = r && functionDecl_9(b, l + 1);
    r = r && functionBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotationDef*
  private static boolean functionDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotationDef(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionDecl_0", c)) break;
    }
    return true;
  }

  // (templateDeclaration | genericDeclaration)?
  private static boolean functionDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_1")) return false;
    functionDecl_1_0(b, l + 1);
    return true;
  }

  // templateDeclaration | genericDeclaration
  private static boolean functionDecl_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_1_0")) return false;
    boolean r;
    r = templateDeclaration(b, l + 1);
    if (!r) r = genericDeclaration(b, l + 1);
    return r;
  }

  // specifier*
  private static boolean functionDecl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!specifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionDecl_2", c)) break;
    }
    return true;
  }

  // functionHead | operatorFunctionHead | destructorHead
  private static boolean functionDecl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_3")) return false;
    boolean r;
    r = functionHead(b, l + 1);
    if (!r) r = operatorFunctionHead(b, l + 1);
    if (!r) r = destructorHead(b, l + 1);
    return r;
  }

  // parameterList?
  private static boolean functionDecl_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_5")) return false;
    parameterList(b, l + 1);
    return true;
  }

  // namedReturnVar?
  private static boolean functionDecl_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_7")) return false;
    namedReturnVar(b, l + 1);
    return true;
  }

  // (':' returnTypeOrMemberInitList)?
  private static boolean functionDecl_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_8")) return false;
    functionDecl_8_0(b, l + 1);
    return true;
  }

  // ':' returnTypeOrMemberInitList
  private static boolean functionDecl_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_COLON);
    r = r && returnTypeOrMemberInitList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // throwsClause?
  private static boolean functionDecl_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDecl_9")) return false;
    throwsClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean functionHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionHead")) return false;
    if (!nextTokenIs(b, "<function name>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_HEAD, "<function name>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '*' | '?' | '+'
  public static boolean functionRefQualifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionRefQualifier")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_REF_QUALIFIER, "<function reference qualifier (*/?/+)>");
    r = consumeToken(b, OP_STAR);
    if (!r) r = consumeToken(b, OP_QUESTION);
    if (!r) r = consumeToken(b, OP_PLUS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // functionRefQualifier '(' typeList? ')'
  public static boolean functionRefType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionRefType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_REF_TYPE, "<function reference type>");
    r = functionRefQualifier(b, l + 1);
    r = r && consumeToken(b, PUNC_LPAREN);
    r = r && functionRefType_2(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeList?
  private static boolean functionRefType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionRefType_2")) return false;
    typeList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'unsigned'? ('byte' | 'char' | 'short' | 'int' | 'long'
  //                                     | 'float' | 'double')
  //                       | 'bool'
  public static boolean fundamentalTypeSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fundamentalTypeSpec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNDAMENTAL_TYPE_SPEC, "<primitive type>");
    r = fundamentalTypeSpec_0(b, l + 1);
    if (!r) r = consumeToken(b, KW_BOOL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'unsigned'? ('byte' | 'char' | 'short' | 'int' | 'long'
  //                                     | 'float' | 'double')
  private static boolean fundamentalTypeSpec_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fundamentalTypeSpec_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fundamentalTypeSpec_0_0(b, l + 1);
    r = r && fundamentalTypeSpec_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'unsigned'?
  private static boolean fundamentalTypeSpec_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fundamentalTypeSpec_0_0")) return false;
    consumeToken(b, KW_UNSIGNED);
    return true;
  }

  // 'byte' | 'char' | 'short' | 'int' | 'long'
  //                                     | 'float' | 'double'
  private static boolean fundamentalTypeSpec_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fundamentalTypeSpec_0_1")) return false;
    boolean r;
    r = consumeToken(b, KW_BYTE);
    if (!r) r = consumeToken(b, KW_CHAR);
    if (!r) r = consumeToken(b, KW_SHORT);
    if (!r) r = consumeToken(b, KW_INT);
    if (!r) r = consumeToken(b, KW_LONG);
    if (!r) r = consumeToken(b, KW_FLOAT);
    if (!r) r = consumeToken(b, KW_DOUBLE);
    return r;
  }

  /* ********************************************************** */
  // 'generic' '<' templateParameterList '>'
  public static boolean genericDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericDeclaration")) return false;
    if (!nextTokenIs(b, "<generic declaration>", KW_GENERIC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GENERIC_DECLARATION, "<generic declaration>");
    r = consumeTokens(b, 0, KW_GENERIC, OP_LT);
    r = r && templateParameterList(b, l + 1);
    r = r && consumeToken(b, OP_GT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // templateQualifiedScopeExpr
  //                  | qualifiedIdentifier
  public static boolean identifierExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierExpr")) return false;
    if (!nextTokenIs(b, "<identifier>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IDENTIFIER_EXPR, "<identifier>");
    r = templateQualifiedScopeExpr(b, l + 1);
    if (!r) r = qualifiedIdentifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER templateArgList?
  public static boolean identifierSegment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierSegment")) return false;
    if (!nextTokenIs(b, "<identifier segment>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IDENTIFIER_SEGMENT, "<identifier segment>");
    r = consumeToken(b, IDENTIFIER);
    r = r && identifierSegment_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // templateArgList?
  private static boolean identifierSegment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierSegment_1")) return false;
    templateArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // specifier* IDENTIFIER ':' typeSpec condVarInitialiser?
  public static boolean ifCondVarDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifCondVarDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_COND_VAR_DECL, "<if condition variable declaration>");
    r = ifCondVarDecl_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, OP_COLON);
    r = r && typeSpec(b, l + 1);
    r = r && ifCondVarDecl_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // specifier*
  private static boolean ifCondVarDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifCondVarDecl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!specifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifCondVarDecl_0", c)) break;
    }
    return true;
  }

  // condVarInitialiser?
  private static boolean ifCondVarDecl_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifCondVarDecl_4")) return false;
    condVarInitialiser(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ifCondVarDecl (';' ifCondVarDecl)*
  public static boolean ifCondVarDeclList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifCondVarDeclList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_COND_VAR_DECL_LIST, "<if condition variable declaration list>");
    r = ifCondVarDecl(b, l + 1);
    r = r && ifCondVarDeclList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (';' ifCondVarDecl)*
  private static boolean ifCondVarDeclList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifCondVarDeclList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifCondVarDeclList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifCondVarDeclList_1", c)) break;
    }
    return true;
  }

  // ';' ifCondVarDecl
  private static boolean ifCondVarDeclList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifCondVarDeclList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_SEMICOLON);
    r = r && ifCondVarDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'if' '(' expression ')' statement ('else' statement)?
  //                   | 'if' '(' ifCondVarDecl ')' statement ('else' statement)?
  //                   | 'if' '(' ifCondVarDeclList ';' conditionalExpr ')' statement ('else' statement)?
  //                   | 'if' '(' ifCondVarDeclList ')' statement ('else' statement)?
  public static boolean ifElseStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement")) return false;
    if (!nextTokenIs(b, "<if statement>", KW_IF)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_ELSE_STATEMENT, "<if statement>");
    r = ifElseStatement_0(b, l + 1);
    if (!r) r = ifElseStatement_1(b, l + 1);
    if (!r) r = ifElseStatement_2(b, l + 1);
    if (!r) r = ifElseStatement_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'if' '(' expression ')' statement ('else' statement)?
  private static boolean ifElseStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KW_IF, PUNC_LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && statement(b, l + 1);
    r = r && ifElseStatement_0_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_0_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_0_5")) return false;
    ifElseStatement_0_5_0(b, l + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_0_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_0_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_ELSE);
    r = r && statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'if' '(' ifCondVarDecl ')' statement ('else' statement)?
  private static boolean ifElseStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KW_IF, PUNC_LPAREN);
    r = r && ifCondVarDecl(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && statement(b, l + 1);
    r = r && ifElseStatement_1_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_1_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_1_5")) return false;
    ifElseStatement_1_5_0(b, l + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_1_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_1_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_ELSE);
    r = r && statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'if' '(' ifCondVarDeclList ';' conditionalExpr ')' statement ('else' statement)?
  private static boolean ifElseStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KW_IF, PUNC_LPAREN);
    r = r && ifCondVarDeclList(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    r = r && conditionalExpr(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && statement(b, l + 1);
    r = r && ifElseStatement_2_7(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_2_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_2_7")) return false;
    ifElseStatement_2_7_0(b, l + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_2_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_2_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_ELSE);
    r = r && statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'if' '(' ifCondVarDeclList ')' statement ('else' statement)?
  private static boolean ifElseStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KW_IF, PUNC_LPAREN);
    r = r && ifCondVarDeclList(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && statement(b, l + 1);
    r = r && ifElseStatement_3_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_3_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_3_5")) return false;
    ifElseStatement_3_5_0(b, l + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_3_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_3_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_ELSE);
    r = r && statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'import' qualifiedIdentifier ';'
  public static boolean importDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclaration")) return false;
    if (!nextTokenIs(b, "<import declaration>", KW_IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IMPORT_DECLARATION, "<import declaration>");
    r = consumeToken(b, KW_IMPORT);
    r = r && qualifiedIdentifier(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // exclusiveBinOrExpr ('|' exclusiveBinOrExpr)*
  public static boolean inclusiveBinOrExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inclusiveBinOrExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INCLUSIVE_BIN_OR_EXPR, "<bitwise-or expression>");
    r = exclusiveBinOrExpr(b, l + 1);
    r = r && inclusiveBinOrExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('|' exclusiveBinOrExpr)*
  private static boolean inclusiveBinOrExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inclusiveBinOrExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inclusiveBinOrExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inclusiveBinOrExpr_1", c)) break;
    }
    return true;
  }

  // '|' exclusiveBinOrExpr
  private static boolean inclusiveBinOrExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inclusiveBinOrExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_PIPE);
    r = r && exclusiveBinOrExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // conditionalExpr
  //               | /*empty*/
  //     
  public static boolean initElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initElement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INIT_ELEMENT, "<initializer element>");
    r = conditionalExpr(b, l + 1);
    if (!r) r = consumeToken(b, INITELEMENT_1_0);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '=' conditionalExpr
  //               | '(' expressionList? ')' ('[' conditionalExpr ']')?
  //               | braceInitList
  public static boolean initialiser(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initialiser")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INITIALISER, "<initializer>");
    r = initialiser_0(b, l + 1);
    if (!r) r = initialiser_1(b, l + 1);
    if (!r) r = braceInitList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '=' conditionalExpr
  private static boolean initialiser_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initialiser_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ASSIGN);
    r = r && conditionalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' expressionList? ')' ('[' conditionalExpr ']')?
  private static boolean initialiser_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initialiser_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && initialiser_1_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && initialiser_1_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean initialiser_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initialiser_1_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // ('[' conditionalExpr ']')?
  private static boolean initialiser_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initialiser_1_3")) return false;
    initialiser_1_3_0(b, l + 1);
    return true;
  }

  // '[' conditionalExpr ']'
  private static boolean initialiser_1_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initialiser_1_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LBRACKET);
    r = r && conditionalExpr(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LIT_INTEGER
  //            | LIT_FLOAT
  //            | LIT_TRUE
  //            | LIT_FALSE
  //            | LIT_CHAR
  //            | LIT_STRING
  //            | LIT_NULL
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL, "<literal>");
    r = consumeToken(b, LIT_INTEGER);
    if (!r) r = consumeToken(b, LIT_FLOAT);
    if (!r) r = consumeToken(b, LIT_TRUE);
    if (!r) r = consumeToken(b, LIT_FALSE);
    if (!r) r = consumeToken(b, LIT_CHAR);
    if (!r) r = consumeToken(b, LIT_STRING);
    if (!r) r = consumeToken(b, LIT_NULL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inclusiveBinOrExpr ('&&' inclusiveBinOrExpr)*
  public static boolean logicalAndExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalAndExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LOGICAL_AND_EXPR, "<logical-and expression>");
    r = inclusiveBinOrExpr(b, l + 1);
    r = r && logicalAndExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('&&' inclusiveBinOrExpr)*
  private static boolean logicalAndExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalAndExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicalAndExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicalAndExpr_1", c)) break;
    }
    return true;
  }

  // '&&' inclusiveBinOrExpr
  private static boolean logicalAndExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalAndExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_AND);
    r = r && inclusiveBinOrExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // logicalAndExpr ('||' logicalAndExpr)*
  public static boolean logicalOrExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalOrExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LOGICAL_OR_EXPR, "<logical-or expression>");
    r = logicalAndExpr(b, l + 1);
    r = r && logicalOrExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('||' logicalAndExpr)*
  private static boolean logicalOrExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalOrExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicalOrExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicalOrExpr_1", c)) break;
    }
    return true;
  }

  // '||' logicalAndExpr
  private static boolean logicalOrExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalOrExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_OR);
    r = r && logicalAndExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER '(' expressionList? ')'
  public static boolean memberInit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "memberInit")) return false;
    if (!nextTokenIs(b, "<member initializer>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MEMBER_INIT, "<member initializer>");
    r = consumeTokens(b, 0, IDENTIFIER, PUNC_LPAREN);
    r = r && memberInit_2(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expressionList?
  private static boolean memberInit_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "memberInit_2")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // memberInit (',' memberInit)*
  public static boolean memberInitList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "memberInitList")) return false;
    if (!nextTokenIs(b, "<member initializer list>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MEMBER_INIT_LIST, "<member initializer list>");
    r = memberInit(b, l + 1);
    r = r && memberInitList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' memberInit)*
  private static boolean memberInitList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "memberInitList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!memberInitList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "memberInitList_1", c)) break;
    }
    return true;
  }

  // ',' memberInit
  private static boolean memberInitList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "memberInitList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && memberInit(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'module' qualifiedIdentifier ';'
  public static boolean moduleDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "moduleDeclaration")) return false;
    if (!nextTokenIs(b, "<module declaration>", KW_MODULE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MODULE_DECLARATION, "<module declaration>");
    r = consumeToken(b, KW_MODULE);
    r = r && qualifiedIdentifier(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // pmExpr (('*' | '/' | '%') pmExpr)*
  public static boolean multiplicativeExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MULTIPLICATIVE_EXPR, "<multiplicative expression>");
    r = pmExpr(b, l + 1);
    r = r && multiplicativeExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('*' | '/' | '%') pmExpr)*
  private static boolean multiplicativeExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!multiplicativeExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "multiplicativeExpr_1", c)) break;
    }
    return true;
  }

  // ('*' | '/' | '%') pmExpr
  private static boolean multiplicativeExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multiplicativeExpr_1_0_0(b, l + 1);
    r = r && pmExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*' | '/' | '%'
  private static boolean multiplicativeExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_STAR);
    if (!r) r = consumeToken(b, OP_DIV);
    if (!r) r = consumeToken(b, OP_PERCENT);
    return r;
  }

  /* ********************************************************** */
  // '=' conditionalExpr
  //                   | '(' expressionList? ')'
  public static boolean namedReturnInit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedReturnInit")) return false;
    if (!nextTokenIs(b, "<named return initializer>", OP_ASSIGN, PUNC_LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMED_RETURN_INIT, "<named return initializer>");
    r = namedReturnInit_0(b, l + 1);
    if (!r) r = namedReturnInit_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '=' conditionalExpr
  private static boolean namedReturnInit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedReturnInit_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ASSIGN);
    r = r && conditionalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' expressionList? ')'
  private static boolean namedReturnInit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedReturnInit_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && namedReturnInit_1_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean namedReturnInit_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedReturnInit_1_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER ':' typeSpec namedReturnInit?
  public static boolean namedReturnVar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedReturnVar")) return false;
    if (!nextTokenIs(b, "<named return variable>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMED_RETURN_VAR, "<named return variable>");
    r = consumeTokens(b, 0, IDENTIFIER, OP_COLON);
    r = r && typeSpec(b, l + 1);
    r = r && namedReturnVar_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // namedReturnInit?
  private static boolean namedReturnVar_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedReturnVar_3")) return false;
    namedReturnInit(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'namespace' IDENTIFIER? '{' declaration* '}'
  public static boolean namespaceDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceDecl")) return false;
    if (!nextTokenIs(b, "<namespace declaration>", KW_NAMESPACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMESPACE_DECL, "<namespace declaration>");
    r = consumeToken(b, KW_NAMESPACE);
    r = r && namespaceDecl_1(b, l + 1);
    r = r && consumeToken(b, PUNC_LBRACE);
    r = r && namespaceDecl_3(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // IDENTIFIER?
  private static boolean namespaceDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceDecl_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  // declaration*
  private static boolean namespaceDecl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceDecl_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namespaceDecl_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'new' typeName '(' expressionList? ')'
  //           | 'new' typeName '(' expressionList? ')' '[' expression ']'
  //           | 'new' typeName '[' expression? ']' braceInitList?
  //           | 'new' typeName braceInitList
  public static boolean newExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr")) return false;
    if (!nextTokenIs(b, "<new expression>", KW_NEW)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NEW_EXPR, "<new expression>");
    r = newExpr_0(b, l + 1);
    if (!r) r = newExpr_1(b, l + 1);
    if (!r) r = newExpr_2(b, l + 1);
    if (!r) r = newExpr_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'new' typeName '(' expressionList? ')'
  private static boolean newExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_NEW);
    r = r && typeName(b, l + 1);
    r = r && consumeToken(b, PUNC_LPAREN);
    r = r && newExpr_0_3(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean newExpr_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_0_3")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // 'new' typeName '(' expressionList? ')' '[' expression ']'
  private static boolean newExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_NEW);
    r = r && typeName(b, l + 1);
    r = r && consumeToken(b, PUNC_LPAREN);
    r = r && newExpr_1_3(b, l + 1);
    r = r && consumeTokens(b, 0, PUNC_RPAREN, PUNC_LBRACKET);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean newExpr_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_1_3")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // 'new' typeName '[' expression? ']' braceInitList?
  private static boolean newExpr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_NEW);
    r = r && typeName(b, l + 1);
    r = r && consumeToken(b, PUNC_LBRACKET);
    r = r && newExpr_2_3(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACKET);
    r = r && newExpr_2_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean newExpr_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_2_3")) return false;
    expression(b, l + 1);
    return true;
  }

  // braceInitList?
  private static boolean newExpr_2_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_2_5")) return false;
    braceInitList(b, l + 1);
    return true;
  }

  // 'new' typeName braceInitList
  private static boolean newExpr_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpr_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_NEW);
    r = r && typeName(b, l + 1);
    r = r && braceInitList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'operator' operatorSymbol
  //                        | 'operator' '(' ')'
  public static boolean operatorFunctionHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorFunctionHead")) return false;
    if (!nextTokenIs(b, "<operator function name>", KW_OPERATOR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPERATOR_FUNCTION_HEAD, "<operator function name>");
    r = operatorFunctionHead_0(b, l + 1);
    if (!r) r = parseTokens(b, 0, KW_OPERATOR, PUNC_LPAREN, PUNC_RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'operator' operatorSymbol
  private static boolean operatorFunctionHead_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorFunctionHead_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_OPERATOR);
    r = r && operatorSymbol(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '+'  | '-'   | '*'   | '/'   | '%'
  //                  | '&'  | '|'   | '^'   | '~'
  //                  | '<<' | '>>'
  //                  | '&&' | '||'  | '!'
  //                  | '==' | '!='  | '<'   | '>'   | '<=' | '>='
  //                  | '='  | '+='  | '-='  | '*='  | '/=' | '%='
  //                  | '&=' | '|='  | '^='  | '<<=' | '>>='
  //                  | '++' '_'
  //                  | '--' '_'
  //                  | '_'  '++'
  //                  | '_'  '--'
  public static boolean operatorSymbol(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorSymbol")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPERATOR_SYMBOL, "<operator symbol>");
    r = consumeToken(b, OP_PLUS);
    if (!r) r = consumeToken(b, OP_MINUS);
    if (!r) r = consumeToken(b, OP_STAR);
    if (!r) r = consumeToken(b, OP_DIV);
    if (!r) r = consumeToken(b, OP_PERCENT);
    if (!r) r = consumeToken(b, OP_AMP);
    if (!r) r = consumeToken(b, OP_PIPE);
    if (!r) r = consumeToken(b, OP_CARET);
    if (!r) r = consumeToken(b, OP_TILDE);
    if (!r) r = consumeToken(b, OP_LSHIFT);
    if (!r) r = consumeToken(b, OP_RSHIFT);
    if (!r) r = consumeToken(b, OP_AND);
    if (!r) r = consumeToken(b, OP_OR);
    if (!r) r = consumeToken(b, OP_NOT);
    if (!r) r = consumeToken(b, OP_EQ);
    if (!r) r = consumeToken(b, OP_NEQ);
    if (!r) r = consumeToken(b, OP_LT);
    if (!r) r = consumeToken(b, OP_GT);
    if (!r) r = consumeToken(b, OP_LE);
    if (!r) r = consumeToken(b, OP_GE);
    if (!r) r = consumeToken(b, OP_ASSIGN);
    if (!r) r = consumeToken(b, OP_PLUS_ASSIGN);
    if (!r) r = consumeToken(b, OP_MINUS_ASSIGN);
    if (!r) r = consumeToken(b, OP_STAR_ASSIGN);
    if (!r) r = consumeToken(b, OP_DIV_ASSIGN);
    if (!r) r = consumeToken(b, OP_MOD_ASSIGN);
    if (!r) r = consumeToken(b, OP_AND_ASSIGN);
    if (!r) r = consumeToken(b, OP_OR_ASSIGN);
    if (!r) r = consumeToken(b, OP_XOR_ASSIGN);
    if (!r) r = consumeToken(b, OP_LSHIFT_ASSIGN);
    if (!r) r = consumeToken(b, OP_RSHIFT_ASSIGN);
    if (!r) r = operatorSymbol_31(b, l + 1);
    if (!r) r = operatorSymbol_32(b, l + 1);
    if (!r) r = operatorSymbol_33(b, l + 1);
    if (!r) r = operatorSymbol_34(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '++' '_'
  private static boolean operatorSymbol_31(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorSymbol_31")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_INC);
    r = r && consumeToken(b, "_");
    exit_section_(b, m, null, r);
    return r;
  }

  // '--' '_'
  private static boolean operatorSymbol_32(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorSymbol_32")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_DEC);
    r = r && consumeToken(b, "_");
    exit_section_(b, m, null, r);
    return r;
  }

  // '_'  '++'
  private static boolean operatorSymbol_33(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorSymbol_33")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "_");
    r = r && consumeToken(b, OP_INC);
    exit_section_(b, m, null, r);
    return r;
  }

  // '_'  '--'
  private static boolean operatorSymbol_34(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorSymbol_34")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "_");
    r = r && consumeToken(b, OP_DEC);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // parameterSpec (',' parameterSpec)*
  public static boolean parameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_LIST, "<parameter list>");
    r = parameterSpec(b, l + 1);
    r = r && parameterList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' parameterSpec)*
  private static boolean parameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterList_1", c)) break;
    }
    return true;
  }

  // ',' parameterSpec
  private static boolean parameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && parameterSpec(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // annotationDef* specifier* (IDENTIFIER '...'? ':')? typeSpec ('...' IDENTIFIER)? ('=' conditionalExpr)?
  public static boolean parameterSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_SPEC, "<parameter>");
    r = parameterSpec_0(b, l + 1);
    r = r && parameterSpec_1(b, l + 1);
    r = r && parameterSpec_2(b, l + 1);
    r = r && typeSpec(b, l + 1);
    r = r && parameterSpec_4(b, l + 1);
    r = r && parameterSpec_5(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotationDef*
  private static boolean parameterSpec_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotationDef(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterSpec_0", c)) break;
    }
    return true;
  }

  // specifier*
  private static boolean parameterSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!specifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterSpec_1", c)) break;
    }
    return true;
  }

  // (IDENTIFIER '...'? ':')?
  private static boolean parameterSpec_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_2")) return false;
    parameterSpec_2_0(b, l + 1);
    return true;
  }

  // IDENTIFIER '...'? ':'
  private static boolean parameterSpec_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && parameterSpec_2_0_1(b, l + 1);
    r = r && consumeToken(b, OP_COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // '...'?
  private static boolean parameterSpec_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_2_0_1")) return false;
    consumeToken(b, PUNC_ELLIPSIS);
    return true;
  }

  // ('...' IDENTIFIER)?
  private static boolean parameterSpec_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_4")) return false;
    parameterSpec_4_0(b, l + 1);
    return true;
  }

  // '...' IDENTIFIER
  private static boolean parameterSpec_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PUNC_ELLIPSIS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('=' conditionalExpr)?
  private static boolean parameterSpec_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_5")) return false;
    parameterSpec_5_0(b, l + 1);
    return true;
  }

  // '=' conditionalExpr
  private static boolean parameterSpec_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterSpec_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ASSIGN);
    r = r && conditionalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // castExpr (('.*' | '->*') castExpr)*
  public static boolean pmExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pmExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PM_EXPR, "<pointer-to-member expression>");
    r = castExpr(b, l + 1);
    r = r && pmExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('.*' | '->*') castExpr)*
  private static boolean pmExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pmExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!pmExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pmExpr_1", c)) break;
    }
    return true;
  }

  // ('.*' | '->*') castExpr
  private static boolean pmExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pmExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = pmExpr_1_0_0(b, l + 1);
    r = r && castExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '.*' | '->*'
  private static boolean pmExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pmExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_DOT_STAR);
    if (!r) r = consumeToken(b, OP_ARROW_STAR);
    return r;
  }

  /* ********************************************************** */
  // primaryExpr postfixOp*
  public static boolean postfixExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, POSTFIX_EXPR, "<postfix expression>");
    r = primaryExpr(b, l + 1);
    r = r && postfixExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // postfixOp*
  private static boolean postfixExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!postfixOp(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "postfixExpr_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '++'
  //             | '--'
  //             | '...'
  //             | braceInitList
  //             | '[' ']' braceInitList
  //             | '[' expression ']'
  //             | '(' expressionList? ')'
  //             | ('.' | '->') identifierExpr
  public static boolean postfixOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, POSTFIX_OP, "<postfix operator>");
    r = consumeToken(b, OP_INC);
    if (!r) r = consumeToken(b, OP_DEC);
    if (!r) r = consumeToken(b, PUNC_ELLIPSIS);
    if (!r) r = braceInitList(b, l + 1);
    if (!r) r = postfixOp_4(b, l + 1);
    if (!r) r = postfixOp_5(b, l + 1);
    if (!r) r = postfixOp_6(b, l + 1);
    if (!r) r = postfixOp_7(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '[' ']' braceInitList
  private static boolean postfixOp_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixOp_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PUNC_LBRACKET, PUNC_RBRACKET);
    r = r && braceInitList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '[' expression ']'
  private static boolean postfixOp_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixOp_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LBRACKET);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' expressionList? ')'
  private static boolean postfixOp_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixOp_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && postfixOp_6_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean postfixOp_6_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixOp_6_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // ('.' | '->') identifierExpr
  private static boolean postfixOp_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixOp_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = postfixOp_7_0(b, l + 1);
    r = r && identifierExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '.' | '->'
  private static boolean postfixOp_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "postfixOp_7_0")) return false;
    boolean r;
    r = consumeToken(b, OP_DOT);
    if (!r) r = consumeToken(b, OP_ARROW);
    return r;
  }

  /* ********************************************************** */
  // literal
  //               | 'this'
  //               | '(' expression ')'
  //               | annotationDef
  //               | braceInitList
  //               | identifierExpr
  public static boolean primaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PRIMARY_EXPR, "<primary expression>");
    r = literal(b, l + 1);
    if (!r) r = consumeToken(b, KW_THIS);
    if (!r) r = primaryExpr_2(b, l + 1);
    if (!r) r = annotationDef(b, l + 1);
    if (!r) r = braceInitList(b, l + 1);
    if (!r) r = identifierExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' expression ')'
  private static boolean primaryExpr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '::'? identifierSegment ('::' identifierSegment)*
  public static boolean qualifiedIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier")) return false;
    if (!nextTokenIs(b, "<qualified identifier>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUALIFIED_IDENTIFIER, "<qualified identifier>");
    r = qualifiedIdentifier_0(b, l + 1);
    r = r && identifierSegment(b, l + 1);
    r = r && qualifiedIdentifier_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '::'?
  private static boolean qualifiedIdentifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier_0")) return false;
    consumeToken(b, PUNC_SCOPE);
    return true;
  }

  // ('::' identifierSegment)*
  private static boolean qualifiedIdentifier_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!qualifiedIdentifier_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "qualifiedIdentifier_2", c)) break;
    }
    return true;
  }

  // '::' identifierSegment
  private static boolean qualifiedIdentifier_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_SCOPE);
    r = r && identifierSegment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // shiftingExpr (('<' | '>' | '<=' | '>=') shiftingExpr)*
  public static boolean relationalExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RELATIONAL_EXPR, "<relational expression>");
    r = shiftingExpr(b, l + 1);
    r = r && relationalExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('<' | '>' | '<=' | '>=') shiftingExpr)*
  private static boolean relationalExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!relationalExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "relationalExpr_1", c)) break;
    }
    return true;
  }

  // ('<' | '>' | '<=' | '>=') shiftingExpr
  private static boolean relationalExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = relationalExpr_1_0_0(b, l + 1);
    r = r && shiftingExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '<' | '>' | '<=' | '>='
  private static boolean relationalExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_LT);
    if (!r) r = consumeToken(b, OP_GT);
    if (!r) r = consumeToken(b, OP_LE);
    if (!r) r = consumeToken(b, OP_GE);
    return r;
  }

  /* ********************************************************** */
  // 'return' expression? ';'
  public static boolean returnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement")) return false;
    if (!nextTokenIs(b, "<return statement>", KW_RETURN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_STATEMENT, "<return statement>");
    r = consumeToken(b, KW_RETURN);
    r = r && returnStatement_1(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression?
  private static boolean returnStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_1")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // memberInitList
  //                              | staticDepList
  //                              | typeSpec
  public static boolean returnTypeOrMemberInitList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnTypeOrMemberInitList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_TYPE_OR_MEMBER_INIT_LIST, "<return type or member initializer list>");
    r = memberInitList(b, l + 1);
    if (!r) r = staticDepList(b, l + 1);
    if (!r) r = typeSpec(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // additiveExpr (('<<' | '>>') additiveExpr)*
  public static boolean shiftingExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftingExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SHIFTING_EXPR, "<shift expression>");
    r = additiveExpr(b, l + 1);
    r = r && shiftingExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('<<' | '>>') additiveExpr)*
  private static boolean shiftingExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftingExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!shiftingExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "shiftingExpr_1", c)) break;
    }
    return true;
  }

  // ('<<' | '>>') additiveExpr
  private static boolean shiftingExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftingExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = shiftingExpr_1_0_0(b, l + 1);
    r = r && additiveExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '<<' | '>>'
  private static boolean shiftingExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftingExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_LSHIFT);
    if (!r) r = consumeToken(b, OP_RSHIFT);
    return r;
  }

  /* ********************************************************** */
  // 'public' | 'protected' | 'private'
  //             | 'static' | 'const'     | 'abstract' | 'final' | 'override'
  public static boolean specifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "specifier")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SPECIFIER, "<specifier (public/static/const/…)>");
    r = consumeToken(b, KW_PUBLIC);
    if (!r) r = consumeToken(b, KW_PROTECTED);
    if (!r) r = consumeToken(b, KW_PRIVATE);
    if (!r) r = consumeToken(b, KW_STATIC);
    if (!r) r = consumeToken(b, KW_CONST);
    if (!r) r = consumeToken(b, KW_ABSTRACT);
    if (!r) r = consumeToken(b, KW_FINAL);
    if (!r) r = consumeToken(b, KW_OVERRIDE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // blockStatement
  //             | returnStatement
  //             | breakStatement
  //             | ifElseStatement
  //             | whileStatement
  //             | forStatement
  //             | throwStatement
  //             | tryCatchStatement
  //             | usingDecl
  //             | variableDecl
  //             | expressionStatement
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = blockStatement(b, l + 1);
    if (!r) r = returnStatement(b, l + 1);
    if (!r) r = breakStatement(b, l + 1);
    if (!r) r = ifElseStatement(b, l + 1);
    if (!r) r = whileStatement(b, l + 1);
    if (!r) r = forStatement(b, l + 1);
    if (!r) r = throwStatement(b, l + 1);
    if (!r) r = tryCatchStatement(b, l + 1);
    if (!r) r = usingDecl(b, l + 1);
    if (!r) r = variableDecl(b, l + 1);
    if (!r) r = expressionStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // qualifiedIdentifier '(' ')'
  public static boolean staticDep(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "staticDep")) return false;
    if (!nextTokenIs(b, "<static dependency>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATIC_DEP, "<static dependency>");
    r = qualifiedIdentifier(b, l + 1);
    r = r && consumeTokens(b, 0, PUNC_LPAREN, PUNC_RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // staticDep (',' staticDep)*
  public static boolean staticDepList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "staticDepList")) return false;
    if (!nextTokenIs(b, "<static dependency list>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATIC_DEP_LIST, "<static dependency list>");
    r = staticDep(b, l + 1);
    r = r && staticDepList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' staticDep)*
  private static boolean staticDepList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "staticDepList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!staticDepList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "staticDepList_1", c)) break;
    }
    return true;
  }

  // ',' staticDep
  private static boolean staticDepList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "staticDepList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && staticDep(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // typeSpec | conditionalExpr
  public static boolean templateArg(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateArg")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TEMPLATE_ARG, "<template argument>");
    r = typeSpec(b, l + 1);
    if (!r) r = conditionalExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '<' templateArg (',' templateArg)* '>'
  public static boolean templateArgList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateArgList")) return false;
    if (!nextTokenIs(b, "<template argument list>", OP_LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TEMPLATE_ARG_LIST, "<template argument list>");
    r = consumeToken(b, OP_LT);
    r = r && templateArg(b, l + 1);
    r = r && templateArgList_2(b, l + 1);
    r = r && consumeToken(b, OP_GT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' templateArg)*
  private static boolean templateArgList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateArgList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!templateArgList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "templateArgList_2", c)) break;
    }
    return true;
  }

  // ',' templateArg
  private static boolean templateArgList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateArgList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && templateArg(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'template' '<' templateParameterList '>'
  public static boolean templateDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateDeclaration")) return false;
    if (!nextTokenIs(b, "<template declaration>", KW_TEMPLATE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TEMPLATE_DECLARATION, "<template declaration>");
    r = consumeTokens(b, 0, KW_TEMPLATE, OP_LT);
    r = r && templateParameterList(b, l + 1);
    r = r && consumeToken(b, OP_GT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // templateParameterKind '...'? IDENTIFIER (':' typeSpec)? ('=' conditionalExpr)?
  public static boolean templateParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TEMPLATE_PARAMETER, "<template parameter>");
    r = templateParameterKind(b, l + 1);
    r = r && templateParameter_1(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && templateParameter_3(b, l + 1);
    r = r && templateParameter_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '...'?
  private static boolean templateParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameter_1")) return false;
    consumeToken(b, PUNC_ELLIPSIS);
    return true;
  }

  // (':' typeSpec)?
  private static boolean templateParameter_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameter_3")) return false;
    templateParameter_3_0(b, l + 1);
    return true;
  }

  // ':' typeSpec
  private static boolean templateParameter_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameter_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_COLON);
    r = r && typeSpec(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('=' conditionalExpr)?
  private static boolean templateParameter_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameter_4")) return false;
    templateParameter_4_0(b, l + 1);
    return true;
  }

  // '=' conditionalExpr
  private static boolean templateParameter_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameter_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ASSIGN);
    r = r && conditionalExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'typename' | 'struct' | 'class' | 'interface'
  //                         | fundamentalTypeSpec
  //                         | IDENTIFIER
  public static boolean templateParameterKind(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameterKind")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TEMPLATE_PARAMETER_KIND, "<template parameter kind>");
    r = consumeToken(b, KW_TYPENAME);
    if (!r) r = consumeToken(b, KW_STRUCT);
    if (!r) r = consumeToken(b, KW_CLASS);
    if (!r) r = consumeToken(b, KW_INTERFACE);
    if (!r) r = fundamentalTypeSpec(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // templateParameter (',' templateParameter)*
  public static boolean templateParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TEMPLATE_PARAMETER_LIST, "<template parameter list>");
    r = templateParameter(b, l + 1);
    r = r && templateParameterList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' templateParameter)*
  private static boolean templateParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!templateParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "templateParameterList_1", c)) break;
    }
    return true;
  }

  // ',' templateParameter
  private static boolean templateParameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateParameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && templateParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '::'? IDENTIFIER ('::' IDENTIFIER)* templateArgList ('::' IDENTIFIER)+
  public static boolean templateQualifiedScopeExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateQualifiedScopeExpr")) return false;
    if (!nextTokenIs(b, "<template-qualified scope expression>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TEMPLATE_QUALIFIED_SCOPE_EXPR, "<template-qualified scope expression>");
    r = templateQualifiedScopeExpr_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && templateQualifiedScopeExpr_2(b, l + 1);
    r = r && templateArgList(b, l + 1);
    r = r && templateQualifiedScopeExpr_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '::'?
  private static boolean templateQualifiedScopeExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateQualifiedScopeExpr_0")) return false;
    consumeToken(b, PUNC_SCOPE);
    return true;
  }

  // ('::' IDENTIFIER)*
  private static boolean templateQualifiedScopeExpr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateQualifiedScopeExpr_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!templateQualifiedScopeExpr_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "templateQualifiedScopeExpr_2", c)) break;
    }
    return true;
  }

  // '::' IDENTIFIER
  private static boolean templateQualifiedScopeExpr_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateQualifiedScopeExpr_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PUNC_SCOPE, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('::' IDENTIFIER)+
  private static boolean templateQualifiedScopeExpr_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateQualifiedScopeExpr_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = templateQualifiedScopeExpr_4_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!templateQualifiedScopeExpr_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "templateQualifiedScopeExpr_4", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // '::' IDENTIFIER
  private static boolean templateQualifiedScopeExpr_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "templateQualifiedScopeExpr_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PUNC_SCOPE, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'throw' expression? ';'
  public static boolean throwStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwStatement")) return false;
    if (!nextTokenIs(b, "<throw statement>", KW_THROW)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, THROW_STATEMENT, "<throw statement>");
    r = consumeToken(b, KW_THROW);
    r = r && throwStatement_1(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression?
  private static boolean throwStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwStatement_1")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'throws' typeSpec (',' typeSpec)*
  public static boolean throwsClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwsClause")) return false;
    if (!nextTokenIs(b, "<throws clause>", KW_THROWS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, THROWS_CLAUSE, "<throws clause>");
    r = consumeToken(b, KW_THROWS);
    r = r && typeSpec(b, l + 1);
    r = r && throwsClause_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' typeSpec)*
  private static boolean throwsClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwsClause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!throwsClause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "throwsClause_2", c)) break;
    }
    return true;
  }

  // ',' typeSpec
  private static boolean throwsClause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwsClause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && typeSpec(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'try' blockStatement catchClause* finallyClause?
  public static boolean tryCatchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryCatchStatement")) return false;
    if (!nextTokenIs(b, "<try/catch statement>", KW_TRY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TRY_CATCH_STATEMENT, "<try/catch statement>");
    r = consumeToken(b, KW_TRY);
    r = r && blockStatement(b, l + 1);
    r = r && tryCatchStatement_2(b, l + 1);
    r = r && tryCatchStatement_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // catchClause*
  private static boolean tryCatchStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryCatchStatement_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!catchClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tryCatchStatement_2", c)) break;
    }
    return true;
  }

  // finallyClause?
  private static boolean tryCatchStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryCatchStatement_3")) return false;
    finallyClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // typeSpec (',' typeSpec)*
  public static boolean typeList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_LIST, "<type list>");
    r = typeSpec(b, l + 1);
    r = r && typeList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' typeSpec)*
  private static boolean typeList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeList_1", c)) break;
    }
    return true;
  }

  // ',' typeSpec
  private static boolean typeList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && typeSpec(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qualifiedIdentifier
  //            | fundamentalTypeSpec
  public static boolean typeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_NAME, "<type name>");
    r = qualifiedIdentifier(b, l + 1);
    if (!r) r = fundamentalTypeSpec(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // functionRefType
  //            | qualifiedIdentifier '::' functionRefType
  //            | 'const'? (fundamentalTypeSpec | qualifiedIdentifier) typeSuffix*
  public static boolean typeSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_SPEC, "<type>");
    r = functionRefType(b, l + 1);
    if (!r) r = typeSpec_1(b, l + 1);
    if (!r) r = typeSpec_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // qualifiedIdentifier '::' functionRefType
  private static boolean typeSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpec_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedIdentifier(b, l + 1);
    r = r && consumeToken(b, PUNC_SCOPE);
    r = r && functionRefType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'const'? (fundamentalTypeSpec | qualifiedIdentifier) typeSuffix*
  private static boolean typeSpec_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpec_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeSpec_2_0(b, l + 1);
    r = r && typeSpec_2_1(b, l + 1);
    r = r && typeSpec_2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'const'?
  private static boolean typeSpec_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpec_2_0")) return false;
    consumeToken(b, KW_CONST);
    return true;
  }

  // fundamentalTypeSpec | qualifiedIdentifier
  private static boolean typeSpec_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpec_2_1")) return false;
    boolean r;
    r = fundamentalTypeSpec(b, l + 1);
    if (!r) r = qualifiedIdentifier(b, l + 1);
    return r;
  }

  // typeSuffix*
  private static boolean typeSpec_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpec_2_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeSuffix(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeSpec_2_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // typeSpec (',' typeSpec)*
  public static boolean typeSpecList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpecList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_SPEC_LIST, "<type list>");
    r = typeSpec(b, l + 1);
    r = r && typeSpecList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' typeSpec)*
  private static boolean typeSpecList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpecList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeSpecList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeSpecList_1", c)) break;
    }
    return true;
  }

  // ',' typeSpec
  private static boolean typeSpecList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSpecList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_COMMA);
    r = r && typeSpec(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '[' LIT_INTEGER? ']'
  //              | '!'
  //              | '*'
  //              | '&'
  //              | '+'
  //              | '?'
  //              | '#'
  public static boolean typeSuffix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSuffix")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_SUFFIX, "<type suffix (*/&/[]/…)>");
    r = typeSuffix_0(b, l + 1);
    if (!r) r = consumeToken(b, OP_NOT);
    if (!r) r = consumeToken(b, OP_STAR);
    if (!r) r = consumeToken(b, OP_AMP);
    if (!r) r = consumeToken(b, OP_PLUS);
    if (!r) r = consumeToken(b, OP_QUESTION);
    if (!r) r = consumeToken(b, OP_HASH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '[' LIT_INTEGER? ']'
  private static boolean typeSuffix_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSuffix_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUNC_LBRACKET);
    r = r && typeSuffix_0_1(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // LIT_INTEGER?
  private static boolean typeSuffix_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeSuffix_0_1")) return false;
    consumeToken(b, LIT_INTEGER);
    return true;
  }

  /* ********************************************************** */
  // ('++' | '--' | '*' | '&' | '+' | '-' | '!' | '~' | '#') castExpr
  //             | newExpr
  //             | 'delete' castExpr
  //             | postfixExpr
  public static boolean unaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNARY_EXPR, "<unary expression>");
    r = unaryExpr_0(b, l + 1);
    if (!r) r = newExpr(b, l + 1);
    if (!r) r = unaryExpr_2(b, l + 1);
    if (!r) r = postfixExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('++' | '--' | '*' | '&' | '+' | '-' | '!' | '~' | '#') castExpr
  private static boolean unaryExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unaryExpr_0_0(b, l + 1);
    r = r && castExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '++' | '--' | '*' | '&' | '+' | '-' | '!' | '~' | '#'
  private static boolean unaryExpr_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_INC);
    if (!r) r = consumeToken(b, OP_DEC);
    if (!r) r = consumeToken(b, OP_STAR);
    if (!r) r = consumeToken(b, OP_AMP);
    if (!r) r = consumeToken(b, OP_PLUS);
    if (!r) r = consumeToken(b, OP_MINUS);
    if (!r) r = consumeToken(b, OP_NOT);
    if (!r) r = consumeToken(b, OP_TILDE);
    if (!r) r = consumeToken(b, OP_HASH);
    return r;
  }

  // 'delete' castExpr
  private static boolean unaryExpr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KW_DELETE);
    r = r && castExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // annotationDef* specifier*
  //               'union' IDENTIFIER
  //               (':' qualifiedIdentifier)?
  //               '{' unionMemberDecl* '}'
  public static boolean unionDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNION_DECL, "<union declaration>");
    r = unionDecl_0(b, l + 1);
    r = r && unionDecl_1(b, l + 1);
    r = r && consumeTokens(b, 0, KW_UNION, IDENTIFIER);
    r = r && unionDecl_4(b, l + 1);
    r = r && consumeToken(b, PUNC_LBRACE);
    r = r && unionDecl_6(b, l + 1);
    r = r && consumeToken(b, PUNC_RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotationDef*
  private static boolean unionDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDecl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotationDef(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionDecl_0", c)) break;
    }
    return true;
  }

  // specifier*
  private static boolean unionDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDecl_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!specifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionDecl_1", c)) break;
    }
    return true;
  }

  // (':' qualifiedIdentifier)?
  private static boolean unionDecl_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDecl_4")) return false;
    unionDecl_4_0(b, l + 1);
    return true;
  }

  // ':' qualifiedIdentifier
  private static boolean unionDecl_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDecl_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_COLON);
    r = r && qualifiedIdentifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unionMemberDecl*
  private static boolean unionDecl_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionDecl_6")) return false;
    while (true) {
      int c = current_position_(b);
      if (!unionMemberDecl(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unionDecl_6", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'const'? IDENTIFIER ':' typeSpec ';'
  public static boolean unionMemberDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionMemberDecl")) return false;
    if (!nextTokenIs(b, "<union member declaration>", IDENTIFIER, KW_CONST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNION_MEMBER_DECL, "<union member declaration>");
    r = unionMemberDecl_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, OP_COLON);
    r = r && typeSpec(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'const'?
  private static boolean unionMemberDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unionMemberDecl_0")) return false;
    consumeToken(b, KW_CONST);
    return true;
  }

  /* ********************************************************** */
  // moduleDeclaration? importDeclaration* declaration*
  static boolean unit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<compilation unit>");
    r = unit_0(b, l + 1);
    r = r && unit_1(b, l + 1);
    r = r && unit_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // moduleDeclaration?
  private static boolean unit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unit_0")) return false;
    moduleDeclaration(b, l + 1);
    return true;
  }

  // importDeclaration*
  private static boolean unit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unit_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!importDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unit_1", c)) break;
    }
    return true;
  }

  // declaration*
  private static boolean unit_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unit_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unit_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'using' usingFilter? (IDENTIFIER '=')? qualifiedIdentifier ';'
  public static boolean usingDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "usingDecl")) return false;
    if (!nextTokenIs(b, "<using declaration>", KW_USING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USING_DECL, "<using declaration>");
    r = consumeToken(b, KW_USING);
    r = r && usingDecl_1(b, l + 1);
    r = r && usingDecl_2(b, l + 1);
    r = r && qualifiedIdentifier(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // usingFilter?
  private static boolean usingDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "usingDecl_1")) return false;
    usingFilter(b, l + 1);
    return true;
  }

  // (IDENTIFIER '=')?
  private static boolean usingDecl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "usingDecl_2")) return false;
    usingDecl_2_0(b, l + 1);
    return true;
  }

  // IDENTIFIER '='
  private static boolean usingDecl_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "usingDecl_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, OP_ASSIGN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'namespace' | 'struct' | 'interface' | 'class'
  public static boolean usingFilter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "usingFilter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USING_FILTER, "<using filter (namespace/struct/interface/class)>");
    r = consumeToken(b, KW_NAMESPACE);
    if (!r) r = consumeToken(b, KW_STRUCT);
    if (!r) r = consumeToken(b, KW_INTERFACE);
    if (!r) r = consumeToken(b, KW_CLASS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // specifier* IDENTIFIER ':' typeSpec initialiser? ';'
  public static boolean variableDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DECL, "<variable declaration>");
    r = variableDecl_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, OP_COLON);
    r = r && typeSpec(b, l + 1);
    r = r && variableDecl_4(b, l + 1);
    r = r && consumeToken(b, PUNC_SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // specifier*
  private static boolean variableDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDecl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!specifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "variableDecl_0", c)) break;
    }
    return true;
  }

  // initialiser?
  private static boolean variableDecl_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDecl_4")) return false;
    initialiser(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ('public' | 'protected' | 'private') ':'
  public static boolean visibilityDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "visibilityDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VISIBILITY_DECL, "<visibility label (public/protected/private:)>");
    r = visibilityDecl_0(b, l + 1);
    r = r && consumeToken(b, OP_COLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'public' | 'protected' | 'private'
  private static boolean visibilityDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "visibilityDecl_0")) return false;
    boolean r;
    r = consumeToken(b, KW_PUBLIC);
    if (!r) r = consumeToken(b, KW_PROTECTED);
    if (!r) r = consumeToken(b, KW_PRIVATE);
    return r;
  }

  /* ********************************************************** */
  // 'while' '(' expression ')' statement
  public static boolean whileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStatement")) return false;
    if (!nextTokenIs(b, "<while statement>", KW_WHILE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT, "<while statement>");
    r = consumeTokens(b, 0, KW_WHILE, PUNC_LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, PUNC_RPAREN);
    r = r && statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
