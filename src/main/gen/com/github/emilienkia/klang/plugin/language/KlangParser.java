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

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return unit(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // multiplicativeExpr (('+' | '-') multiplicativeExpr)*
  public static boolean additiveExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADDITIVE_EXPR, "<additive expression>");
    result_ = multiplicativeExpr(builder_, level_ + 1);
    result_ = result_ && additiveExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (('+' | '-') multiplicativeExpr)*
  private static boolean additiveExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!additiveExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "additiveExpr_1", pos_)) break;
    }
    return true;
  }

  // ('+' | '-') multiplicativeExpr
  private static boolean additiveExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = additiveExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && multiplicativeExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '+' | '-'
  private static boolean additiveExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_PLUS);
    if (!result_) result_ = consumeToken(builder_, OP_MINUS);
    return result_;
  }

  /* ********************************************************** */
  // annotationDef* (templateDeclaration | genericDeclaration)? specifier*
  //                   ('struct' | 'class' | 'interface' | 'annotation')
  //                   IDENTIFIER
  //                   (':' baseClause)?
  //                   '{' declaration* '}'
  public static boolean aggregateDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, AGGREGATE_DECL, "<struct/class/interface/annotation declaration>");
    result_ = aggregateDecl_0(builder_, level_ + 1);
    result_ = result_ && aggregateDecl_1(builder_, level_ + 1);
    result_ = result_ && aggregateDecl_2(builder_, level_ + 1);
    result_ = result_ && aggregateDecl_3(builder_, level_ + 1);
    pinned_ = result_; // pin = 4
    result_ = result_ && report_error_(builder_, consumeToken(builder_, IDENTIFIER));
    result_ = pinned_ && report_error_(builder_, aggregateDecl_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, PUNC_LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, aggregateDecl_7(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, PUNC_RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // annotationDef*
  private static boolean aggregateDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!annotationDef(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "aggregateDecl_0", pos_)) break;
    }
    return true;
  }

  // (templateDeclaration | genericDeclaration)?
  private static boolean aggregateDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_1")) return false;
    aggregateDecl_1_0(builder_, level_ + 1);
    return true;
  }

  // templateDeclaration | genericDeclaration
  private static boolean aggregateDecl_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_1_0")) return false;
    boolean result_;
    result_ = templateDeclaration(builder_, level_ + 1);
    if (!result_) result_ = genericDeclaration(builder_, level_ + 1);
    return result_;
  }

  // specifier*
  private static boolean aggregateDecl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "aggregateDecl_2", pos_)) break;
    }
    return true;
  }

  // 'struct' | 'class' | 'interface' | 'annotation'
  private static boolean aggregateDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_3")) return false;
    boolean result_;
    result_ = consumeToken(builder_, KW_STRUCT);
    if (!result_) result_ = consumeToken(builder_, KW_CLASS);
    if (!result_) result_ = consumeToken(builder_, KW_INTERFACE);
    if (!result_) result_ = consumeToken(builder_, KW_ANNOTATION);
    return result_;
  }

  // (':' baseClause)?
  private static boolean aggregateDecl_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_5")) return false;
    aggregateDecl_5_0(builder_, level_ + 1);
    return true;
  }

  // ':' baseClause
  private static boolean aggregateDecl_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_COLON);
    result_ = result_ && baseClause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // declaration*
  private static boolean aggregateDecl_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregateDecl_7")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!declaration(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "aggregateDecl_7", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '@' qualifiedIdentifier ('(' expressionList? ')' | braceInitList)?
  public static boolean annotationDef(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "annotationDef")) return false;
    if (!nextTokenIs(builder_, "<annotation>", PUNC_AT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANNOTATION_DEF, "<annotation>");
    result_ = consumeToken(builder_, PUNC_AT);
    result_ = result_ && qualifiedIdentifier(builder_, level_ + 1);
    result_ = result_ && annotationDef_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('(' expressionList? ')' | braceInitList)?
  private static boolean annotationDef_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "annotationDef_2")) return false;
    annotationDef_2_0(builder_, level_ + 1);
    return true;
  }

  // '(' expressionList? ')' | braceInitList
  private static boolean annotationDef_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "annotationDef_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = annotationDef_2_0_0(builder_, level_ + 1);
    if (!result_) result_ = braceInitList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' expressionList? ')'
  private static boolean annotationDef_2_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "annotationDef_2_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && annotationDef_2_0_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean annotationDef_2_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "annotationDef_2_0_0_1")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // conditionalExpr (assignmentOperator assignmentExpr)?
  public static boolean assignmentExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignmentExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGNMENT_EXPR, "<assignment expression>");
    result_ = conditionalExpr(builder_, level_ + 1);
    result_ = result_ && assignmentExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (assignmentOperator assignmentExpr)?
  private static boolean assignmentExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignmentExpr_1")) return false;
    assignmentExpr_1_0(builder_, level_ + 1);
    return true;
  }

  // assignmentOperator assignmentExpr
  private static boolean assignmentExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignmentExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = assignmentOperator(builder_, level_ + 1);
    result_ = result_ && assignmentExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '=' | '*=' | '/=' | '%=' | '+=' | '-='
  //                      | '>>=' | '<<=' | '&=' | '^=' | '|='
  public static boolean assignmentOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignmentOperator")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGNMENT_OPERATOR, "<assignment operator>");
    result_ = consumeToken(builder_, OP_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_STAR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_DIV_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_MOD_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_PLUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_MINUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_RSHIFT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_LSHIFT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_AND_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_XOR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_OR_ASSIGN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // baseSpec (',' baseSpec)*
  public static boolean baseClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "baseClause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BASE_CLAUSE, "<base class list>");
    result_ = baseSpec(builder_, level_ + 1);
    result_ = result_ && baseClause_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' baseSpec)*
  private static boolean baseClause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "baseClause_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!baseClause_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "baseClause_1", pos_)) break;
    }
    return true;
  }

  // ',' baseSpec
  private static boolean baseClause_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "baseClause_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && baseSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ('public' | 'protected' | 'private')? qualifiedIdentifier
  public static boolean baseSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "baseSpec")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BASE_SPEC, "<base class specifier>");
    result_ = baseSpec_0(builder_, level_ + 1);
    result_ = result_ && qualifiedIdentifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('public' | 'protected' | 'private')?
  private static boolean baseSpec_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "baseSpec_0")) return false;
    baseSpec_0_0(builder_, level_ + 1);
    return true;
  }

  // 'public' | 'protected' | 'private'
  private static boolean baseSpec_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "baseSpec_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, KW_PUBLIC);
    if (!result_) result_ = consumeToken(builder_, KW_PROTECTED);
    if (!result_) result_ = consumeToken(builder_, KW_PRIVATE);
    return result_;
  }

  /* ********************************************************** */
  // equalityExpr ('&' equalityExpr)*
  public static boolean binAndExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "binAndExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BIN_AND_EXPR, "<bitwise-and expression>");
    result_ = equalityExpr(builder_, level_ + 1);
    result_ = result_ && binAndExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('&' equalityExpr)*
  private static boolean binAndExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "binAndExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!binAndExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "binAndExpr_1", pos_)) break;
    }
    return true;
  }

  // '&' equalityExpr
  private static boolean binAndExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "binAndExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_AMP);
    result_ = result_ && equalityExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '{' statement* '}'
  public static boolean blockStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "blockStatement")) return false;
    if (!nextTokenIs(builder_, "<block { … }>", PUNC_LBRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BLOCK_STATEMENT, "<block { … }>");
    result_ = consumeToken(builder_, PUNC_LBRACE);
    result_ = result_ && blockStatement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // statement*
  private static boolean blockStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "blockStatement_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "blockStatement_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '{' '}'
  //                 | '{' initElement (',' initElement)* '}'
  //                 | '{' designatedInitElement (',' designatedInitElement)* '}'
  public static boolean braceInitList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braceInitList")) return false;
    if (!nextTokenIs(builder_, "<brace initializer list { … }>", PUNC_LBRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BRACE_INIT_LIST, "<brace initializer list { … }>");
    result_ = parseTokens(builder_, 0, PUNC_LBRACE, PUNC_RBRACE);
    if (!result_) result_ = braceInitList_1(builder_, level_ + 1);
    if (!result_) result_ = braceInitList_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '{' initElement (',' initElement)* '}'
  private static boolean braceInitList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braceInitList_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LBRACE);
    result_ = result_ && initElement(builder_, level_ + 1);
    result_ = result_ && braceInitList_1_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' initElement)*
  private static boolean braceInitList_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braceInitList_1_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!braceInitList_1_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "braceInitList_1_2", pos_)) break;
    }
    return true;
  }

  // ',' initElement
  private static boolean braceInitList_1_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braceInitList_1_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && initElement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '{' designatedInitElement (',' designatedInitElement)* '}'
  private static boolean braceInitList_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braceInitList_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LBRACE);
    result_ = result_ && designatedInitElement(builder_, level_ + 1);
    result_ = result_ && braceInitList_2_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' designatedInitElement)*
  private static boolean braceInitList_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braceInitList_2_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!braceInitList_2_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "braceInitList_2_2", pos_)) break;
    }
    return true;
  }

  // ',' designatedInitElement
  private static boolean braceInitList_2_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braceInitList_2_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && designatedInitElement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'break' ';'
  public static boolean breakStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "breakStatement")) return false;
    if (!nextTokenIs(builder_, "<break statement>", KW_BREAK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BREAK_STATEMENT, "<break statement>");
    result_ = consumeTokens(builder_, 0, KW_BREAK, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '(' typeSpec ')' castExpr
  //            | unaryExpr
  public static boolean castExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "castExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CAST_EXPR, "<cast expression>");
    result_ = castExpr_0(builder_, level_ + 1);
    if (!result_) result_ = unaryExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '(' typeSpec ')' castExpr
  private static boolean castExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "castExpr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && castExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'operator' '(' ')'
  public static boolean castOperatorFunctionHead(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "castOperatorFunctionHead")) return false;
    if (!nextTokenIs(builder_, "<cast operator function name (operator ())>", KW_OPERATOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CAST_OPERATOR_FUNCTION_HEAD, "<cast operator function name (operator ())>");
    result_ = consumeTokens(builder_, 0, KW_OPERATOR, PUNC_LPAREN, PUNC_RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'catch' '(' catchParameterDecl ')' blockStatement
  public static boolean catchClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "catchClause")) return false;
    if (!nextTokenIs(builder_, "<catch clause>", KW_CATCH)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CATCH_CLAUSE, "<catch clause>");
    result_ = consumeTokens(builder_, 0, KW_CATCH, PUNC_LPAREN);
    result_ = result_ && catchParameterDecl(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && blockStatement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'const'? IDENTIFIER ':' typeSpec
  public static boolean catchParameterDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "catchParameterDecl")) return false;
    if (!nextTokenIs(builder_, "<catch parameter declaration>", IDENTIFIER, KW_CONST)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CATCH_PARAMETER_DECL, "<catch parameter declaration>");
    result_ = catchParameterDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, IDENTIFIER, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'const'?
  private static boolean catchParameterDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "catchParameterDecl_0")) return false;
    consumeToken(builder_, KW_CONST);
    return true;
  }

  /* ********************************************************** */
  // '=' conditionalExpr
  //                      | '(' expressionList? ')'
  //                      | braceInitList
  public static boolean condVarInitialiser(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "condVarInitialiser")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COND_VAR_INITIALISER, "<condition variable initializer>");
    result_ = condVarInitialiser_0(builder_, level_ + 1);
    if (!result_) result_ = condVarInitialiser_1(builder_, level_ + 1);
    if (!result_) result_ = braceInitList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '=' conditionalExpr
  private static boolean condVarInitialiser_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "condVarInitialiser_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' expressionList? ')'
  private static boolean condVarInitialiser_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "condVarInitialiser_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && condVarInitialiser_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean condVarInitialiser_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "condVarInitialiser_1_1")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // logicalOrExpr ('?' conditionalExpr ':' conditionalExpr)?
  public static boolean conditionalExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditionalExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONDITIONAL_EXPR, "<conditional expression>");
    result_ = logicalOrExpr(builder_, level_ + 1);
    result_ = result_ && conditionalExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('?' conditionalExpr ':' conditionalExpr)?
  private static boolean conditionalExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditionalExpr_1")) return false;
    conditionalExpr_1_0(builder_, level_ + 1);
    return true;
  }

  // '?' conditionalExpr ':' conditionalExpr
  private static boolean conditionalExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditionalExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_QUESTION);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_COLON);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'continue' ';'
  public static boolean continueStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "continueStatement")) return false;
    if (!nextTokenIs(builder_, "<continue statement>", KW_CONTINUE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONTINUE_STATEMENT, "<continue statement>");
    result_ = consumeTokens(builder_, 0, KW_CONTINUE, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
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
  //               | ';'
  public static boolean declaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declaration")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECLARATION, "<declaration>");
    result_ = visibilityDecl(builder_, level_ + 1);
    if (!result_) result_ = namespaceDecl(builder_, level_ + 1);
    if (!result_) result_ = usingDecl(builder_, level_ + 1);
    if (!result_) result_ = friendDecl(builder_, level_ + 1);
    if (!result_) result_ = aggregateDecl(builder_, level_ + 1);
    if (!result_) result_ = enumDecl(builder_, level_ + 1);
    if (!result_) result_ = unionDecl(builder_, level_ + 1);
    if (!result_) result_ = functionDecl(builder_, level_ + 1);
    if (!result_) result_ = variableDecl(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, KlangParser::declarationRecover);
    return result_;
  }

  /* ********************************************************** */
  // '}'
  //     | 'public' | 'protected' | 'private'
  //     | 'static' | 'const' | 'abstract' | 'final' | 'override' | 'default'
  //     | 'namespace' | 'using' | 'friend'
  //     | 'struct' | 'class' | 'interface' | 'annotation'
  //     | 'enum' | 'union'
  //     | 'template' | 'generic' | 'operator'
  //     | '@' | '~' | ';' | IDENTIFIER
  static boolean declarationFirst(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declarationFirst")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null, "<declaration start>");
    result_ = consumeToken(builder_, PUNC_RBRACE);
    if (!result_) result_ = consumeToken(builder_, KW_PUBLIC);
    if (!result_) result_ = consumeToken(builder_, KW_PROTECTED);
    if (!result_) result_ = consumeToken(builder_, KW_PRIVATE);
    if (!result_) result_ = consumeToken(builder_, KW_STATIC);
    if (!result_) result_ = consumeToken(builder_, KW_CONST);
    if (!result_) result_ = consumeToken(builder_, KW_ABSTRACT);
    if (!result_) result_ = consumeToken(builder_, KW_FINAL);
    if (!result_) result_ = consumeToken(builder_, KW_OVERRIDE);
    if (!result_) result_ = consumeToken(builder_, KW_DEFAULT);
    if (!result_) result_ = consumeToken(builder_, KW_NAMESPACE);
    if (!result_) result_ = consumeToken(builder_, KW_USING);
    if (!result_) result_ = consumeToken(builder_, KW_FRIEND);
    if (!result_) result_ = consumeToken(builder_, KW_STRUCT);
    if (!result_) result_ = consumeToken(builder_, KW_CLASS);
    if (!result_) result_ = consumeToken(builder_, KW_INTERFACE);
    if (!result_) result_ = consumeToken(builder_, KW_ANNOTATION);
    if (!result_) result_ = consumeToken(builder_, KW_ENUM);
    if (!result_) result_ = consumeToken(builder_, KW_UNION);
    if (!result_) result_ = consumeToken(builder_, KW_TEMPLATE);
    if (!result_) result_ = consumeToken(builder_, KW_GENERIC);
    if (!result_) result_ = consumeToken(builder_, KW_OPERATOR);
    if (!result_) result_ = consumeToken(builder_, PUNC_AT);
    if (!result_) result_ = consumeToken(builder_, OP_TILDE);
    if (!result_) result_ = consumeToken(builder_, PUNC_SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // !declarationFirst
  static boolean declarationRecover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declarationRecover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !declarationFirst(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '.' designatedMemberName '=' conditionalExpr
  //                         | '.' designatedMemberName '=' braceInitList
  //                         | '.' designatedMemberName '(' expressionList? ')'
  public static boolean designatedInitElement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedInitElement")) return false;
    if (!nextTokenIs(builder_, "<designated initializer (.member = …)>", OP_DOT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DESIGNATED_INIT_ELEMENT, "<designated initializer (.member = …)>");
    result_ = designatedInitElement_0(builder_, level_ + 1);
    if (!result_) result_ = designatedInitElement_1(builder_, level_ + 1);
    if (!result_) result_ = designatedInitElement_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '.' designatedMemberName '=' conditionalExpr
  private static boolean designatedInitElement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedInitElement_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_DOT);
    result_ = result_ && designatedMemberName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '.' designatedMemberName '=' braceInitList
  private static boolean designatedInitElement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedInitElement_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_DOT);
    result_ = result_ && designatedMemberName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && braceInitList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '.' designatedMemberName '(' expressionList? ')'
  private static boolean designatedInitElement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedInitElement_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_DOT);
    result_ = result_ && designatedMemberName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && designatedInitElement_2_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean designatedInitElement_2_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedInitElement_2_3")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // (IDENTIFIER '::')* IDENTIFIER
  public static boolean designatedMemberName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedMemberName")) return false;
    if (!nextTokenIs(builder_, "<member name>", IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DESIGNATED_MEMBER_NAME, "<member name>");
    result_ = designatedMemberName_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (IDENTIFIER '::')*
  private static boolean designatedMemberName_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedMemberName_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!designatedMemberName_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "designatedMemberName_0", pos_)) break;
    }
    return true;
  }

  // IDENTIFIER '::'
  private static boolean designatedMemberName_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "designatedMemberName_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, IDENTIFIER, PUNC_SCOPE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '~' IDENTIFIER
  public static boolean destructorHead(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "destructorHead")) return false;
    if (!nextTokenIs(builder_, "<destructor name (~Name)>", OP_TILDE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DESTRUCTOR_HEAD, "<destructor name (~Name)>");
    result_ = consumeTokens(builder_, 0, OP_TILDE, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // specifier* 'enum' IDENTIFIER (':' typeSpec)?
  //              '{' enumEntry* '}'
  public static boolean enumDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumDecl")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENUM_DECL, "<enum declaration>");
    result_ = enumDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 1, KW_ENUM, IDENTIFIER);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, enumDecl_3(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, PUNC_LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, enumDecl_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, PUNC_RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // specifier*
  private static boolean enumDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumDecl_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "enumDecl_0", pos_)) break;
    }
    return true;
  }

  // (':' typeSpec)?
  private static boolean enumDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumDecl_3")) return false;
    enumDecl_3_0(builder_, level_ + 1);
    return true;
  }

  // ':' typeSpec
  private static boolean enumDecl_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumDecl_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // enumEntry*
  private static boolean enumDecl_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumDecl_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!enumEntry(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "enumDecl_5", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER ('=' (LIT_INTEGER | IDENTIFIER) | braceInitList | '(' expressionList? ')')? 'default'? ';'
  public static boolean enumEntry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry")) return false;
    if (!nextTokenIs(builder_, "<enum entry>", IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENUM_ENTRY, "<enum entry>");
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && enumEntry_1(builder_, level_ + 1);
    result_ = result_ && enumEntry_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('=' (LIT_INTEGER | IDENTIFIER) | braceInitList | '(' expressionList? ')')?
  private static boolean enumEntry_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry_1")) return false;
    enumEntry_1_0(builder_, level_ + 1);
    return true;
  }

  // '=' (LIT_INTEGER | IDENTIFIER) | braceInitList | '(' expressionList? ')'
  private static boolean enumEntry_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = enumEntry_1_0_0(builder_, level_ + 1);
    if (!result_) result_ = braceInitList(builder_, level_ + 1);
    if (!result_) result_ = enumEntry_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '=' (LIT_INTEGER | IDENTIFIER)
  private static boolean enumEntry_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && enumEntry_1_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LIT_INTEGER | IDENTIFIER
  private static boolean enumEntry_1_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry_1_0_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, LIT_INTEGER);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    return result_;
  }

  // '(' expressionList? ')'
  private static boolean enumEntry_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry_1_0_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && enumEntry_1_0_2_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean enumEntry_1_0_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry_1_0_2_1")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  // 'default'?
  private static boolean enumEntry_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "enumEntry_2")) return false;
    consumeToken(builder_, KW_DEFAULT);
    return true;
  }

  /* ********************************************************** */
  // relationalExpr (('==' | '!=') relationalExpr)*
  public static boolean equalityExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equalityExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EQUALITY_EXPR, "<equality expression>");
    result_ = relationalExpr(builder_, level_ + 1);
    result_ = result_ && equalityExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (('==' | '!=') relationalExpr)*
  private static boolean equalityExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equalityExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!equalityExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "equalityExpr_1", pos_)) break;
    }
    return true;
  }

  // ('==' | '!=') relationalExpr
  private static boolean equalityExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equalityExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = equalityExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && relationalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '==' | '!='
  private static boolean equalityExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equalityExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_EQ);
    if (!result_) result_ = consumeToken(builder_, OP_NEQ);
    return result_;
  }

  /* ********************************************************** */
  // binAndExpr ('^' binAndExpr)*
  public static boolean exclusiveBinOrExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exclusiveBinOrExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXCLUSIVE_BIN_OR_EXPR, "<bitwise-xor expression>");
    result_ = binAndExpr(builder_, level_ + 1);
    result_ = result_ && exclusiveBinOrExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('^' binAndExpr)*
  private static boolean exclusiveBinOrExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exclusiveBinOrExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!exclusiveBinOrExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "exclusiveBinOrExpr_1", pos_)) break;
    }
    return true;
  }

  // '^' binAndExpr
  private static boolean exclusiveBinOrExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exclusiveBinOrExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_CARET);
    result_ = result_ && binAndExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // assignmentExpr (',' assignmentExpr)*
  public static boolean expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION, "<expression>");
    result_ = assignmentExpr(builder_, level_ + 1);
    result_ = result_ && expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' assignmentExpr)*
  private static boolean expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!expression_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "expression_1", pos_)) break;
    }
    return true;
  }

  // ',' assignmentExpr
  private static boolean expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && assignmentExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // assignmentExpr (',' assignmentExpr)*
  public static boolean expressionList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_LIST, "<expression list>");
    result_ = assignmentExpr(builder_, level_ + 1);
    result_ = result_ && expressionList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' assignmentExpr)*
  private static boolean expressionList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!expressionList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "expressionList_1", pos_)) break;
    }
    return true;
  }

  // ',' assignmentExpr
  private static boolean expressionList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && assignmentExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // expression? ';'
  public static boolean expressionStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionStatement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_STATEMENT, "<expression statement>");
    result_ = expressionStatement_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // expression?
  private static boolean expressionStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionStatement_0")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'finally' blockStatement
  public static boolean finallyClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finallyClause")) return false;
    if (!nextTokenIs(builder_, "<finally clause>", KW_FINALLY)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FINALLY_CLAUSE, "<finally clause>");
    result_ = consumeToken(builder_, KW_FINALLY);
    result_ = result_ && blockStatement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'for' '(' (variableDecl | ';') expression? ';' expression? ')' statement
  public static boolean forStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forStatement")) return false;
    if (!nextTokenIs(builder_, "<for statement>", KW_FOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FOR_STATEMENT, "<for statement>");
    result_ = consumeTokens(builder_, 0, KW_FOR, PUNC_LPAREN);
    result_ = result_ && forStatement_2(builder_, level_ + 1);
    result_ = result_ && forStatement_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    result_ = result_ && forStatement_5(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // variableDecl | ';'
  private static boolean forStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forStatement_2")) return false;
    boolean result_;
    result_ = variableDecl(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, PUNC_SEMICOLON);
    return result_;
  }

  // expression?
  private static boolean forStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forStatement_3")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  // expression?
  private static boolean forStatement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forStatement_5")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'for' '(' foreachVarDecl '=' conditionalExpr ')' statement
  public static boolean foreachStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "foreachStatement")) return false;
    if (!nextTokenIs(builder_, "<foreach statement>", KW_FOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FOREACH_STATEMENT, "<foreach statement>");
    result_ = consumeTokens(builder_, 0, KW_FOR, PUNC_LPAREN);
    result_ = result_ && foreachVarDecl(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // specifier* IDENTIFIER ':' typeSpec
  public static boolean foreachVarDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "foreachVarDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FOREACH_VAR_DECL, "<foreach loop variable declaration>");
    result_ = foreachVarDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, IDENTIFIER, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // specifier*
  private static boolean foreachVarDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "foreachVarDecl_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "foreachVarDecl_0", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'friend' friendFilter? qualifiedIdentifier templateArgList? ';'
  public static boolean friendDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "friendDecl")) return false;
    if (!nextTokenIs(builder_, "<friend declaration>", KW_FRIEND)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FRIEND_DECL, "<friend declaration>");
    result_ = consumeToken(builder_, KW_FRIEND);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, friendDecl_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, qualifiedIdentifier(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, friendDecl_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, PUNC_SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // friendFilter?
  private static boolean friendDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "friendDecl_1")) return false;
    friendFilter(builder_, level_ + 1);
    return true;
  }

  // templateArgList?
  private static boolean friendDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "friendDecl_3")) return false;
    templateArgList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'struct' | 'interface' | 'class'
  public static boolean friendFilter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "friendFilter")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FRIEND_FILTER, "<friend filter (struct/interface/class)>");
    result_ = consumeToken(builder_, KW_STRUCT);
    if (!result_) result_ = consumeToken(builder_, KW_INTERFACE);
    if (!result_) result_ = consumeToken(builder_, KW_CLASS);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // blockStatement
  //                | '->' ('default' | 'delete') ';'
  //                | '->' qualifiedIdentifier ('(' typeSpecList? ')')? ';'
  //                | ';'
  public static boolean functionBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_BODY, "<function body>");
    result_ = blockStatement(builder_, level_ + 1);
    if (!result_) result_ = functionBody_1(builder_, level_ + 1);
    if (!result_) result_ = functionBody_2(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '->' ('default' | 'delete') ';'
  private static boolean functionBody_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ARROW);
    result_ = result_ && functionBody_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'default' | 'delete'
  private static boolean functionBody_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody_1_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, KW_DEFAULT);
    if (!result_) result_ = consumeToken(builder_, KW_DELETE);
    return result_;
  }

  // '->' qualifiedIdentifier ('(' typeSpecList? ')')? ';'
  private static boolean functionBody_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ARROW);
    result_ = result_ && qualifiedIdentifier(builder_, level_ + 1);
    result_ = result_ && functionBody_2_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('(' typeSpecList? ')')?
  private static boolean functionBody_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody_2_2")) return false;
    functionBody_2_2_0(builder_, level_ + 1);
    return true;
  }

  // '(' typeSpecList? ')'
  private static boolean functionBody_2_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody_2_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && functionBody_2_2_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // typeSpecList?
  private static boolean functionBody_2_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody_2_2_0_1")) return false;
    typeSpecList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // annotationDef* (templateDeclaration | genericDeclaration)? specifier*
  //                  ( (functionHead | operatorFunctionHead | destructorHead)
  //                    '(' parameterList? ')'
  //                    namedReturnVar?
  //                    (':' returnTypeOrMemberInitList)?
  //                  | castOperatorFunctionHead
  //                    (':' typeSpec)?
  //                  )
  //                  throwsClause?
  //                  functionBody
  public static boolean functionDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_DECL, "<function declaration>");
    result_ = functionDecl_0(builder_, level_ + 1);
    result_ = result_ && functionDecl_1(builder_, level_ + 1);
    result_ = result_ && functionDecl_2(builder_, level_ + 1);
    result_ = result_ && functionDecl_3(builder_, level_ + 1);
    result_ = result_ && functionDecl_4(builder_, level_ + 1);
    result_ = result_ && functionBody(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // annotationDef*
  private static boolean functionDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!annotationDef(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "functionDecl_0", pos_)) break;
    }
    return true;
  }

  // (templateDeclaration | genericDeclaration)?
  private static boolean functionDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_1")) return false;
    functionDecl_1_0(builder_, level_ + 1);
    return true;
  }

  // templateDeclaration | genericDeclaration
  private static boolean functionDecl_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_1_0")) return false;
    boolean result_;
    result_ = templateDeclaration(builder_, level_ + 1);
    if (!result_) result_ = genericDeclaration(builder_, level_ + 1);
    return result_;
  }

  // specifier*
  private static boolean functionDecl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "functionDecl_2", pos_)) break;
    }
    return true;
  }

  // (functionHead | operatorFunctionHead | destructorHead)
  //                    '(' parameterList? ')'
  //                    namedReturnVar?
  //                    (':' returnTypeOrMemberInitList)?
  //                  | castOperatorFunctionHead
  //                    (':' typeSpec)?
  private static boolean functionDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = functionDecl_3_0(builder_, level_ + 1);
    if (!result_) result_ = functionDecl_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (functionHead | operatorFunctionHead | destructorHead)
  //                    '(' parameterList? ')'
  //                    namedReturnVar?
  //                    (':' returnTypeOrMemberInitList)?
  private static boolean functionDecl_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = functionDecl_3_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && functionDecl_3_0_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && functionDecl_3_0_4(builder_, level_ + 1);
    result_ = result_ && functionDecl_3_0_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // functionHead | operatorFunctionHead | destructorHead
  private static boolean functionDecl_3_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_0_0")) return false;
    boolean result_;
    result_ = functionHead(builder_, level_ + 1);
    if (!result_) result_ = operatorFunctionHead(builder_, level_ + 1);
    if (!result_) result_ = destructorHead(builder_, level_ + 1);
    return result_;
  }

  // parameterList?
  private static boolean functionDecl_3_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_0_2")) return false;
    parameterList(builder_, level_ + 1);
    return true;
  }

  // namedReturnVar?
  private static boolean functionDecl_3_0_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_0_4")) return false;
    namedReturnVar(builder_, level_ + 1);
    return true;
  }

  // (':' returnTypeOrMemberInitList)?
  private static boolean functionDecl_3_0_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_0_5")) return false;
    functionDecl_3_0_5_0(builder_, level_ + 1);
    return true;
  }

  // ':' returnTypeOrMemberInitList
  private static boolean functionDecl_3_0_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_0_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_COLON);
    result_ = result_ && returnTypeOrMemberInitList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // castOperatorFunctionHead
  //                    (':' typeSpec)?
  private static boolean functionDecl_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = castOperatorFunctionHead(builder_, level_ + 1);
    result_ = result_ && functionDecl_3_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (':' typeSpec)?
  private static boolean functionDecl_3_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_1_1")) return false;
    functionDecl_3_1_1_0(builder_, level_ + 1);
    return true;
  }

  // ':' typeSpec
  private static boolean functionDecl_3_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_3_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // throwsClause?
  private static boolean functionDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDecl_4")) return false;
    throwsClause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean functionHead(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionHead")) return false;
    if (!nextTokenIs(builder_, "<function name>", IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_HEAD, "<function name>");
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '*' | '?' | '+'
  public static boolean functionRefQualifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionRefQualifier")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_REF_QUALIFIER, "<function reference qualifier (*/?/+)>");
    result_ = consumeToken(builder_, OP_STAR);
    if (!result_) result_ = consumeToken(builder_, OP_QUESTION);
    if (!result_) result_ = consumeToken(builder_, OP_PLUS);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // functionRefQualifier '(' typeList? ')'
  public static boolean functionRefType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionRefType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_REF_TYPE, "<function reference type>");
    result_ = functionRefQualifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && functionRefType_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // typeList?
  private static boolean functionRefType_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionRefType_2")) return false;
    typeList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'unsigned'? ('byte' | 'char' | 'short' | 'int' | 'long'
  //                                     | 'float' | 'double')
  //                       | 'bool'
  public static boolean fundamentalTypeSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fundamentalTypeSpec")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNDAMENTAL_TYPE_SPEC, "<primitive type>");
    result_ = fundamentalTypeSpec_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, KW_BOOL);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'unsigned'? ('byte' | 'char' | 'short' | 'int' | 'long'
  //                                     | 'float' | 'double')
  private static boolean fundamentalTypeSpec_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fundamentalTypeSpec_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = fundamentalTypeSpec_0_0(builder_, level_ + 1);
    result_ = result_ && fundamentalTypeSpec_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'unsigned'?
  private static boolean fundamentalTypeSpec_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fundamentalTypeSpec_0_0")) return false;
    consumeToken(builder_, KW_UNSIGNED);
    return true;
  }

  // 'byte' | 'char' | 'short' | 'int' | 'long'
  //                                     | 'float' | 'double'
  private static boolean fundamentalTypeSpec_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fundamentalTypeSpec_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, KW_BYTE);
    if (!result_) result_ = consumeToken(builder_, KW_CHAR);
    if (!result_) result_ = consumeToken(builder_, KW_SHORT);
    if (!result_) result_ = consumeToken(builder_, KW_INT);
    if (!result_) result_ = consumeToken(builder_, KW_LONG);
    if (!result_) result_ = consumeToken(builder_, KW_FLOAT);
    if (!result_) result_ = consumeToken(builder_, KW_DOUBLE);
    return result_;
  }

  /* ********************************************************** */
  // 'generic' '<' templateParameterList '>'
  public static boolean genericDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "genericDeclaration")) return false;
    if (!nextTokenIs(builder_, "<generic declaration>", KW_GENERIC)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GENERIC_DECLARATION, "<generic declaration>");
    result_ = consumeTokens(builder_, 0, KW_GENERIC, OP_LT);
    result_ = result_ && templateParameterList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_GT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // templateQualifiedScopeExpr
  //                  | qualifiedIdentifier
  public static boolean identifierExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifierExpr")) return false;
    if (!nextTokenIs(builder_, "<identifier>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IDENTIFIER_EXPR, "<identifier>");
    result_ = templateQualifiedScopeExpr(builder_, level_ + 1);
    if (!result_) result_ = qualifiedIdentifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER templateArgList?
  public static boolean identifierSegment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifierSegment")) return false;
    if (!nextTokenIs(builder_, "<identifier segment>", IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IDENTIFIER_SEGMENT, "<identifier segment>");
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && identifierSegment_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // templateArgList?
  private static boolean identifierSegment_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifierSegment_1")) return false;
    templateArgList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // specifier* IDENTIFIER ':' typeSpec condVarInitialiser?
  public static boolean ifCondVarDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifCondVarDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_COND_VAR_DECL, "<if condition variable declaration>");
    result_ = ifCondVarDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, IDENTIFIER, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    result_ = result_ && ifCondVarDecl_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // specifier*
  private static boolean ifCondVarDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifCondVarDecl_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ifCondVarDecl_0", pos_)) break;
    }
    return true;
  }

  // condVarInitialiser?
  private static boolean ifCondVarDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifCondVarDecl_4")) return false;
    condVarInitialiser(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ifCondVarDecl (';' ifCondVarDecl)*
  public static boolean ifCondVarDeclList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifCondVarDeclList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_COND_VAR_DECL_LIST, "<if condition variable declaration list>");
    result_ = ifCondVarDecl(builder_, level_ + 1);
    result_ = result_ && ifCondVarDeclList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (';' ifCondVarDecl)*
  private static boolean ifCondVarDeclList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifCondVarDeclList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ifCondVarDeclList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ifCondVarDeclList_1", pos_)) break;
    }
    return true;
  }

  // ';' ifCondVarDecl
  private static boolean ifCondVarDeclList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifCondVarDeclList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_SEMICOLON);
    result_ = result_ && ifCondVarDecl(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'if' '(' expression ')' statement ('else' statement)?
  //                   | 'if' '(' ifCondVarDecl ')' statement ('else' statement)?
  //                   | 'if' '(' ifCondVarDeclList ';' conditionalExpr ')' statement ('else' statement)?
  //                   | 'if' '(' ifCondVarDeclList ')' statement ('else' statement)?
  public static boolean ifElseStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement")) return false;
    if (!nextTokenIs(builder_, "<if statement>", KW_IF)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_ELSE_STATEMENT, "<if statement>");
    result_ = ifElseStatement_0(builder_, level_ + 1);
    if (!result_) result_ = ifElseStatement_1(builder_, level_ + 1);
    if (!result_) result_ = ifElseStatement_2(builder_, level_ + 1);
    if (!result_) result_ = ifElseStatement_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'if' '(' expression ')' statement ('else' statement)?
  private static boolean ifElseStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, KW_IF, PUNC_LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    result_ = result_ && ifElseStatement_0_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_0_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_0_5")) return false;
    ifElseStatement_0_5_0(builder_, level_ + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_0_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_0_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_ELSE);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'if' '(' ifCondVarDecl ')' statement ('else' statement)?
  private static boolean ifElseStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, KW_IF, PUNC_LPAREN);
    result_ = result_ && ifCondVarDecl(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    result_ = result_ && ifElseStatement_1_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_1_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_1_5")) return false;
    ifElseStatement_1_5_0(builder_, level_ + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_1_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_1_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_ELSE);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'if' '(' ifCondVarDeclList ';' conditionalExpr ')' statement ('else' statement)?
  private static boolean ifElseStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, KW_IF, PUNC_LPAREN);
    result_ = result_ && ifCondVarDeclList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    result_ = result_ && ifElseStatement_2_7(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_2_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_2_7")) return false;
    ifElseStatement_2_7_0(builder_, level_ + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_2_7_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_2_7_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_ELSE);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'if' '(' ifCondVarDeclList ')' statement ('else' statement)?
  private static boolean ifElseStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, KW_IF, PUNC_LPAREN);
    result_ = result_ && ifCondVarDeclList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    result_ = result_ && ifElseStatement_3_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('else' statement)?
  private static boolean ifElseStatement_3_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_3_5")) return false;
    ifElseStatement_3_5_0(builder_, level_ + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifElseStatement_3_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifElseStatement_3_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_ELSE);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'import' qualifiedIdentifier ';'
  public static boolean importDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "importDeclaration")) return false;
    if (!nextTokenIs(builder_, "<import declaration>", KW_IMPORT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IMPORT_DECLARATION, "<import declaration>");
    result_ = consumeToken(builder_, KW_IMPORT);
    result_ = result_ && qualifiedIdentifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // exclusiveBinOrExpr ('|' exclusiveBinOrExpr)*
  public static boolean inclusiveBinOrExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "inclusiveBinOrExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INCLUSIVE_BIN_OR_EXPR, "<bitwise-or expression>");
    result_ = exclusiveBinOrExpr(builder_, level_ + 1);
    result_ = result_ && inclusiveBinOrExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('|' exclusiveBinOrExpr)*
  private static boolean inclusiveBinOrExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "inclusiveBinOrExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!inclusiveBinOrExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "inclusiveBinOrExpr_1", pos_)) break;
    }
    return true;
  }

  // '|' exclusiveBinOrExpr
  private static boolean inclusiveBinOrExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "inclusiveBinOrExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_PIPE);
    result_ = result_ && exclusiveBinOrExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // conditionalExpr
  //               | /*empty*/
  //     
  public static boolean initElement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initElement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INIT_ELEMENT, "<initializer element>");
    result_ = conditionalExpr(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, INITELEMENT_1_0);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '=' conditionalExpr
  //               | '(' expressionList? ')' ('[' conditionalExpr ']')?
  //               | braceInitList
  public static boolean initialiser(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initialiser")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INITIALISER, "<initializer>");
    result_ = initialiser_0(builder_, level_ + 1);
    if (!result_) result_ = initialiser_1(builder_, level_ + 1);
    if (!result_) result_ = braceInitList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '=' conditionalExpr
  private static boolean initialiser_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initialiser_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' expressionList? ')' ('[' conditionalExpr ']')?
  private static boolean initialiser_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initialiser_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && initialiser_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && initialiser_1_3(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean initialiser_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initialiser_1_1")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  // ('[' conditionalExpr ']')?
  private static boolean initialiser_1_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initialiser_1_3")) return false;
    initialiser_1_3_0(builder_, level_ + 1);
    return true;
  }

  // '[' conditionalExpr ']'
  private static boolean initialiser_1_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initialiser_1_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LBRACKET);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LIT_INTEGER
  //            | LIT_FLOAT
  //            | LIT_TRUE
  //            | LIT_FALSE
  //            | LIT_CHAR
  //            | LIT_STRING
  //            | LIT_NULL
  public static boolean literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "literal")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LITERAL, "<literal>");
    result_ = consumeToken(builder_, LIT_INTEGER);
    if (!result_) result_ = consumeToken(builder_, LIT_FLOAT);
    if (!result_) result_ = consumeToken(builder_, LIT_TRUE);
    if (!result_) result_ = consumeToken(builder_, LIT_FALSE);
    if (!result_) result_ = consumeToken(builder_, LIT_CHAR);
    if (!result_) result_ = consumeToken(builder_, LIT_STRING);
    if (!result_) result_ = consumeToken(builder_, LIT_NULL);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // inclusiveBinOrExpr ('&&' inclusiveBinOrExpr)*
  public static boolean logicalAndExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicalAndExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOGICAL_AND_EXPR, "<logical-and expression>");
    result_ = inclusiveBinOrExpr(builder_, level_ + 1);
    result_ = result_ && logicalAndExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('&&' inclusiveBinOrExpr)*
  private static boolean logicalAndExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicalAndExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!logicalAndExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "logicalAndExpr_1", pos_)) break;
    }
    return true;
  }

  // '&&' inclusiveBinOrExpr
  private static boolean logicalAndExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicalAndExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_AND);
    result_ = result_ && inclusiveBinOrExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // logicalAndExpr ('||' logicalAndExpr)*
  public static boolean logicalOrExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicalOrExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOGICAL_OR_EXPR, "<logical-or expression>");
    result_ = logicalAndExpr(builder_, level_ + 1);
    result_ = result_ && logicalOrExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('||' logicalAndExpr)*
  private static boolean logicalOrExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicalOrExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!logicalOrExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "logicalOrExpr_1", pos_)) break;
    }
    return true;
  }

  // '||' logicalAndExpr
  private static boolean logicalOrExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicalOrExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_OR);
    result_ = result_ && logicalAndExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER '(' expressionList? ')'
  public static boolean memberInit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memberInit")) return false;
    if (!nextTokenIs(builder_, "<member initializer>", IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MEMBER_INIT, "<member initializer>");
    result_ = consumeTokens(builder_, 0, IDENTIFIER, PUNC_LPAREN);
    result_ = result_ && memberInit_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // expressionList?
  private static boolean memberInit_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memberInit_2")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // memberInit (',' memberInit)*
  public static boolean memberInitList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memberInitList")) return false;
    if (!nextTokenIs(builder_, "<member initializer list>", IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MEMBER_INIT_LIST, "<member initializer list>");
    result_ = memberInit(builder_, level_ + 1);
    result_ = result_ && memberInitList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' memberInit)*
  private static boolean memberInitList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memberInitList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!memberInitList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "memberInitList_1", pos_)) break;
    }
    return true;
  }

  // ',' memberInit
  private static boolean memberInitList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memberInitList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && memberInit(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'module' qualifiedIdentifier ';'
  public static boolean moduleDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "moduleDeclaration")) return false;
    if (!nextTokenIs(builder_, "<module declaration>", KW_MODULE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MODULE_DECLARATION, "<module declaration>");
    result_ = consumeToken(builder_, KW_MODULE);
    result_ = result_ && qualifiedIdentifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // pmExpr (('*' | '/' | '%') pmExpr)*
  public static boolean multiplicativeExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MULTIPLICATIVE_EXPR, "<multiplicative expression>");
    result_ = pmExpr(builder_, level_ + 1);
    result_ = result_ && multiplicativeExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (('*' | '/' | '%') pmExpr)*
  private static boolean multiplicativeExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!multiplicativeExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "multiplicativeExpr_1", pos_)) break;
    }
    return true;
  }

  // ('*' | '/' | '%') pmExpr
  private static boolean multiplicativeExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = multiplicativeExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && pmExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '*' | '/' | '%'
  private static boolean multiplicativeExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_STAR);
    if (!result_) result_ = consumeToken(builder_, OP_DIV);
    if (!result_) result_ = consumeToken(builder_, OP_PERCENT);
    return result_;
  }

  /* ********************************************************** */
  // '=' conditionalExpr
  //                   | '(' expressionList? ')'
  public static boolean namedReturnInit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedReturnInit")) return false;
    if (!nextTokenIs(builder_, "<named return initializer>", OP_ASSIGN, PUNC_LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NAMED_RETURN_INIT, "<named return initializer>");
    result_ = namedReturnInit_0(builder_, level_ + 1);
    if (!result_) result_ = namedReturnInit_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '=' conditionalExpr
  private static boolean namedReturnInit_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedReturnInit_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' expressionList? ')'
  private static boolean namedReturnInit_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedReturnInit_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && namedReturnInit_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean namedReturnInit_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedReturnInit_1_1")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER ':' typeSpec namedReturnInit?
  public static boolean namedReturnVar(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedReturnVar")) return false;
    if (!nextTokenIs(builder_, "<named return variable>", IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NAMED_RETURN_VAR, "<named return variable>");
    result_ = consumeTokens(builder_, 0, IDENTIFIER, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    result_ = result_ && namedReturnVar_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // namedReturnInit?
  private static boolean namedReturnVar_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedReturnVar_3")) return false;
    namedReturnInit(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'namespace' IDENTIFIER? '{' declaration* '}'
  public static boolean namespaceDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namespaceDecl")) return false;
    if (!nextTokenIs(builder_, "<namespace declaration>", KW_NAMESPACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NAMESPACE_DECL, "<namespace declaration>");
    result_ = consumeToken(builder_, KW_NAMESPACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, namespaceDecl_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, PUNC_LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, namespaceDecl_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, PUNC_RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // IDENTIFIER?
  private static boolean namespaceDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namespaceDecl_1")) return false;
    consumeToken(builder_, IDENTIFIER);
    return true;
  }

  // declaration*
  private static boolean namespaceDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namespaceDecl_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!declaration(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "namespaceDecl_3", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'new' typeName '(' expressionList? ')'
  //           | 'new' typeName '(' expressionList? ')' '[' expression ']'
  //           | 'new' typeName '[' expression? ']' braceInitList?
  //           | 'new' typeName braceInitList
  public static boolean newExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr")) return false;
    if (!nextTokenIs(builder_, "<new expression>", KW_NEW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NEW_EXPR, "<new expression>");
    result_ = newExpr_0(builder_, level_ + 1);
    if (!result_) result_ = newExpr_1(builder_, level_ + 1);
    if (!result_) result_ = newExpr_2(builder_, level_ + 1);
    if (!result_) result_ = newExpr_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'new' typeName '(' expressionList? ')'
  private static boolean newExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_NEW);
    result_ = result_ && typeName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && newExpr_0_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean newExpr_0_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_0_3")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  // 'new' typeName '(' expressionList? ')' '[' expression ']'
  private static boolean newExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_NEW);
    result_ = result_ && typeName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && newExpr_1_3(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, PUNC_RPAREN, PUNC_LBRACKET);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean newExpr_1_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_1_3")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  // 'new' typeName '[' expression? ']' braceInitList?
  private static boolean newExpr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_NEW);
    result_ = result_ && typeName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_LBRACKET);
    result_ = result_ && newExpr_2_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACKET);
    result_ = result_ && newExpr_2_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expression?
  private static boolean newExpr_2_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_2_3")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  // braceInitList?
  private static boolean newExpr_2_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_2_5")) return false;
    braceInitList(builder_, level_ + 1);
    return true;
  }

  // 'new' typeName braceInitList
  private static boolean newExpr_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpr_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_NEW);
    result_ = result_ && typeName(builder_, level_ + 1);
    result_ = result_ && braceInitList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'operator' operatorSymbol
  //                        | 'operator' '[' ']'
  public static boolean operatorFunctionHead(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorFunctionHead")) return false;
    if (!nextTokenIs(builder_, "<operator function name>", KW_OPERATOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERATOR_FUNCTION_HEAD, "<operator function name>");
    result_ = operatorFunctionHead_0(builder_, level_ + 1);
    if (!result_) result_ = parseTokens(builder_, 0, KW_OPERATOR, PUNC_LBRACKET, PUNC_RBRACKET);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'operator' operatorSymbol
  private static boolean operatorFunctionHead_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorFunctionHead_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_OPERATOR);
    result_ = result_ && operatorSymbol(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '+'  | '-'   | '*'   | '/'   | '%'
  //                  | '&'  | '|'   | '^'   | '~'
  //                  | '<<' | '>>'
  //                  | '&&' | '||'  | '!'
  //                  | '==' | '!='  | '<'   | '>'   | '<=' | '>=' | '<=>'
  //                  | '='  | '+='  | '-='  | '*='  | '/=' | '%='
  //                  | '&=' | '|='  | '^='  | '<<=' | '>>='
  //                  | '++' '_'
  //                  | '--' '_'
  //                  | '_'  '++'
  //                  | '_'  '--'
  public static boolean operatorSymbol(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorSymbol")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERATOR_SYMBOL, "<operator symbol>");
    result_ = consumeToken(builder_, OP_PLUS);
    if (!result_) result_ = consumeToken(builder_, OP_MINUS);
    if (!result_) result_ = consumeToken(builder_, OP_STAR);
    if (!result_) result_ = consumeToken(builder_, OP_DIV);
    if (!result_) result_ = consumeToken(builder_, OP_PERCENT);
    if (!result_) result_ = consumeToken(builder_, OP_AMP);
    if (!result_) result_ = consumeToken(builder_, OP_PIPE);
    if (!result_) result_ = consumeToken(builder_, OP_CARET);
    if (!result_) result_ = consumeToken(builder_, OP_TILDE);
    if (!result_) result_ = consumeToken(builder_, OP_LSHIFT);
    if (!result_) result_ = consumeToken(builder_, OP_RSHIFT);
    if (!result_) result_ = consumeToken(builder_, OP_AND);
    if (!result_) result_ = consumeToken(builder_, OP_OR);
    if (!result_) result_ = consumeToken(builder_, OP_NOT);
    if (!result_) result_ = consumeToken(builder_, OP_EQ);
    if (!result_) result_ = consumeToken(builder_, OP_NEQ);
    if (!result_) result_ = consumeToken(builder_, OP_LT);
    if (!result_) result_ = consumeToken(builder_, OP_GT);
    if (!result_) result_ = consumeToken(builder_, OP_LE);
    if (!result_) result_ = consumeToken(builder_, OP_GE);
    if (!result_) result_ = consumeToken(builder_, OP_SPACESHIP);
    if (!result_) result_ = consumeToken(builder_, OP_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_PLUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_MINUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_STAR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_DIV_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_MOD_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_AND_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_OR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_XOR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_LSHIFT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, OP_RSHIFT_ASSIGN);
    if (!result_) result_ = operatorSymbol_32(builder_, level_ + 1);
    if (!result_) result_ = operatorSymbol_33(builder_, level_ + 1);
    if (!result_) result_ = operatorSymbol_34(builder_, level_ + 1);
    if (!result_) result_ = operatorSymbol_35(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '++' '_'
  private static boolean operatorSymbol_32(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorSymbol_32")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_INC);
    result_ = result_ && consumeToken(builder_, "_");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '--' '_'
  private static boolean operatorSymbol_33(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorSymbol_33")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_DEC);
    result_ = result_ && consumeToken(builder_, "_");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '_'  '++'
  private static boolean operatorSymbol_34(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorSymbol_34")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "_");
    result_ = result_ && consumeToken(builder_, OP_INC);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '_'  '--'
  private static boolean operatorSymbol_35(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorSymbol_35")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "_");
    result_ = result_ && consumeToken(builder_, OP_DEC);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // parameterSpec (',' parameterSpec)*
  public static boolean parameterList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_LIST, "<parameter list>");
    result_ = parameterSpec(builder_, level_ + 1);
    result_ = result_ && parameterList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' parameterSpec)*
  private static boolean parameterList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameterList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameterList_1", pos_)) break;
    }
    return true;
  }

  // ',' parameterSpec
  private static boolean parameterList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && parameterSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // annotationDef* specifier* (IDENTIFIER '...'? ':')? typeSpec ('...' IDENTIFIER)? ('=' conditionalExpr)?
  public static boolean parameterSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_SPEC, "<parameter>");
    result_ = parameterSpec_0(builder_, level_ + 1);
    result_ = result_ && parameterSpec_1(builder_, level_ + 1);
    result_ = result_ && parameterSpec_2(builder_, level_ + 1);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    result_ = result_ && parameterSpec_4(builder_, level_ + 1);
    result_ = result_ && parameterSpec_5(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // annotationDef*
  private static boolean parameterSpec_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!annotationDef(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameterSpec_0", pos_)) break;
    }
    return true;
  }

  // specifier*
  private static boolean parameterSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameterSpec_1", pos_)) break;
    }
    return true;
  }

  // (IDENTIFIER '...'? ':')?
  private static boolean parameterSpec_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_2")) return false;
    parameterSpec_2_0(builder_, level_ + 1);
    return true;
  }

  // IDENTIFIER '...'? ':'
  private static boolean parameterSpec_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && parameterSpec_2_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '...'?
  private static boolean parameterSpec_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_2_0_1")) return false;
    consumeToken(builder_, PUNC_ELLIPSIS);
    return true;
  }

  // ('...' IDENTIFIER)?
  private static boolean parameterSpec_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_4")) return false;
    parameterSpec_4_0(builder_, level_ + 1);
    return true;
  }

  // '...' IDENTIFIER
  private static boolean parameterSpec_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, PUNC_ELLIPSIS, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('=' conditionalExpr)?
  private static boolean parameterSpec_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_5")) return false;
    parameterSpec_5_0(builder_, level_ + 1);
    return true;
  }

  // '=' conditionalExpr
  private static boolean parameterSpec_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterSpec_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // castExpr (('.*' | '->*') castExpr)*
  public static boolean pmExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pmExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PM_EXPR, "<pointer-to-member expression>");
    result_ = castExpr(builder_, level_ + 1);
    result_ = result_ && pmExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (('.*' | '->*') castExpr)*
  private static boolean pmExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pmExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!pmExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "pmExpr_1", pos_)) break;
    }
    return true;
  }

  // ('.*' | '->*') castExpr
  private static boolean pmExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pmExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = pmExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && castExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '.*' | '->*'
  private static boolean pmExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pmExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_DOT_STAR);
    if (!result_) result_ = consumeToken(builder_, OP_ARROW_STAR);
    return result_;
  }

  /* ********************************************************** */
  // primaryExpr postfixOp*
  public static boolean postfixExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, POSTFIX_EXPR, "<postfix expression>");
    result_ = primaryExpr(builder_, level_ + 1);
    result_ = result_ && postfixExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // postfixOp*
  private static boolean postfixExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!postfixOp(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "postfixExpr_1", pos_)) break;
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
  public static boolean postfixOp(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixOp")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, POSTFIX_OP, "<postfix operator>");
    result_ = consumeToken(builder_, OP_INC);
    if (!result_) result_ = consumeToken(builder_, OP_DEC);
    if (!result_) result_ = consumeToken(builder_, PUNC_ELLIPSIS);
    if (!result_) result_ = braceInitList(builder_, level_ + 1);
    if (!result_) result_ = postfixOp_4(builder_, level_ + 1);
    if (!result_) result_ = postfixOp_5(builder_, level_ + 1);
    if (!result_) result_ = postfixOp_6(builder_, level_ + 1);
    if (!result_) result_ = postfixOp_7(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '[' ']' braceInitList
  private static boolean postfixOp_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixOp_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, PUNC_LBRACKET, PUNC_RBRACKET);
    result_ = result_ && braceInitList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '[' expression ']'
  private static boolean postfixOp_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixOp_5")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LBRACKET);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' expressionList? ')'
  private static boolean postfixOp_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixOp_6")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && postfixOp_6_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean postfixOp_6_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixOp_6_1")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  // ('.' | '->') identifierExpr
  private static boolean postfixOp_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixOp_7")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = postfixOp_7_0(builder_, level_ + 1);
    result_ = result_ && identifierExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '.' | '->'
  private static boolean postfixOp_7_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfixOp_7_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_DOT);
    if (!result_) result_ = consumeToken(builder_, OP_ARROW);
    return result_;
  }

  /* ********************************************************** */
  // literal
  //               | 'this'
  //               | '(' expression ')'
  //               | annotationDef
  //               | braceInitList
  //               | primitiveArrayElementType
  //               | identifierExpr
  public static boolean primaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "primaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_EXPR, "<primary expression>");
    result_ = literal(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, KW_THIS);
    if (!result_) result_ = primaryExpr_2(builder_, level_ + 1);
    if (!result_) result_ = annotationDef(builder_, level_ + 1);
    if (!result_) result_ = braceInitList(builder_, level_ + 1);
    if (!result_) result_ = primitiveArrayElementType(builder_, level_ + 1);
    if (!result_) result_ = identifierExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '(' expression ')'
  private static boolean primaryExpr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "primaryExpr_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'bool' | 'byte' | 'char' | 'short' | 'int' | 'long' | 'float' | 'double'
  public static boolean primitiveArrayElementType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "primitiveArrayElementType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMITIVE_ARRAY_ELEMENT_TYPE, "<primitive array element type>");
    result_ = consumeToken(builder_, KW_BOOL);
    if (!result_) result_ = consumeToken(builder_, KW_BYTE);
    if (!result_) result_ = consumeToken(builder_, KW_CHAR);
    if (!result_) result_ = consumeToken(builder_, KW_SHORT);
    if (!result_) result_ = consumeToken(builder_, KW_INT);
    if (!result_) result_ = consumeToken(builder_, KW_LONG);
    if (!result_) result_ = consumeToken(builder_, KW_FLOAT);
    if (!result_) result_ = consumeToken(builder_, KW_DOUBLE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '::'? identifierSegment ('::' identifierSegment)*
  public static boolean qualifiedIdentifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedIdentifier")) return false;
    if (!nextTokenIs(builder_, "<qualified identifier>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, QUALIFIED_IDENTIFIER, "<qualified identifier>");
    result_ = qualifiedIdentifier_0(builder_, level_ + 1);
    result_ = result_ && identifierSegment(builder_, level_ + 1);
    result_ = result_ && qualifiedIdentifier_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '::'?
  private static boolean qualifiedIdentifier_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedIdentifier_0")) return false;
    consumeToken(builder_, PUNC_SCOPE);
    return true;
  }

  // ('::' identifierSegment)*
  private static boolean qualifiedIdentifier_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedIdentifier_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!qualifiedIdentifier_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "qualifiedIdentifier_2", pos_)) break;
    }
    return true;
  }

  // '::' identifierSegment
  private static boolean qualifiedIdentifier_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedIdentifier_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_SCOPE);
    result_ = result_ && identifierSegment(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // spaceshipExpr (('<' | '>' | '<=' | '>=') spaceshipExpr)*
  public static boolean relationalExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relationalExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RELATIONAL_EXPR, "<relational expression>");
    result_ = spaceshipExpr(builder_, level_ + 1);
    result_ = result_ && relationalExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (('<' | '>' | '<=' | '>=') spaceshipExpr)*
  private static boolean relationalExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relationalExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!relationalExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "relationalExpr_1", pos_)) break;
    }
    return true;
  }

  // ('<' | '>' | '<=' | '>=') spaceshipExpr
  private static boolean relationalExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relationalExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = relationalExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && spaceshipExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '<' | '>' | '<=' | '>='
  private static boolean relationalExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relationalExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_LT);
    if (!result_) result_ = consumeToken(builder_, OP_GT);
    if (!result_) result_ = consumeToken(builder_, OP_LE);
    if (!result_) result_ = consumeToken(builder_, OP_GE);
    return result_;
  }

  /* ********************************************************** */
  // 'return' expression? ';'
  public static boolean returnStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "returnStatement")) return false;
    if (!nextTokenIs(builder_, "<return statement>", KW_RETURN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RETURN_STATEMENT, "<return statement>");
    result_ = consumeToken(builder_, KW_RETURN);
    result_ = result_ && returnStatement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // expression?
  private static boolean returnStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "returnStatement_1")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // memberInitList
  //                              | staticDepList
  //                              | typeSpec
  public static boolean returnTypeOrMemberInitList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "returnTypeOrMemberInitList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RETURN_TYPE_OR_MEMBER_INIT_LIST, "<return type or member initializer list>");
    result_ = memberInitList(builder_, level_ + 1);
    if (!result_) result_ = staticDepList(builder_, level_ + 1);
    if (!result_) result_ = typeSpec(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // additiveExpr (('<<' | '>>') additiveExpr)*
  public static boolean shiftingExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftingExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SHIFTING_EXPR, "<shift expression>");
    result_ = additiveExpr(builder_, level_ + 1);
    result_ = result_ && shiftingExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (('<<' | '>>') additiveExpr)*
  private static boolean shiftingExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftingExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!shiftingExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "shiftingExpr_1", pos_)) break;
    }
    return true;
  }

  // ('<<' | '>>') additiveExpr
  private static boolean shiftingExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftingExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = shiftingExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && additiveExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '<<' | '>>'
  private static boolean shiftingExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftingExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_LSHIFT);
    if (!result_) result_ = consumeToken(builder_, OP_RSHIFT);
    return result_;
  }

  /* ********************************************************** */
  // shiftingExpr ('<=>' shiftingExpr)*
  public static boolean spaceshipExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "spaceshipExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SPACESHIP_EXPR, "<three-way comparison expression>");
    result_ = shiftingExpr(builder_, level_ + 1);
    result_ = result_ && spaceshipExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('<=>' shiftingExpr)*
  private static boolean spaceshipExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "spaceshipExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!spaceshipExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "spaceshipExpr_1", pos_)) break;
    }
    return true;
  }

  // '<=>' shiftingExpr
  private static boolean spaceshipExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "spaceshipExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_SPACESHIP);
    result_ = result_ && shiftingExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'public' | 'protected' | 'private'
  //             | 'static' | 'const'     | 'abstract' | 'final' | 'override' | 'default'
  public static boolean specifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "specifier")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SPECIFIER, "<specifier (public/static/const/…)>");
    result_ = consumeToken(builder_, KW_PUBLIC);
    if (!result_) result_ = consumeToken(builder_, KW_PROTECTED);
    if (!result_) result_ = consumeToken(builder_, KW_PRIVATE);
    if (!result_) result_ = consumeToken(builder_, KW_STATIC);
    if (!result_) result_ = consumeToken(builder_, KW_CONST);
    if (!result_) result_ = consumeToken(builder_, KW_ABSTRACT);
    if (!result_) result_ = consumeToken(builder_, KW_FINAL);
    if (!result_) result_ = consumeToken(builder_, KW_OVERRIDE);
    if (!result_) result_ = consumeToken(builder_, KW_DEFAULT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // blockStatement
  //             | returnStatement
  //             | breakStatement
  //             | continueStatement
  //             | ifElseStatement
  //             | whileStatement
  //             | foreachStatement
  //             | forStatement
  //             | throwStatement
  //             | tryCatchStatement
  //             | usingDecl
  //             | variableDecl
  //             | expressionStatement
  public static boolean statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT, "<statement>");
    result_ = blockStatement(builder_, level_ + 1);
    if (!result_) result_ = returnStatement(builder_, level_ + 1);
    if (!result_) result_ = breakStatement(builder_, level_ + 1);
    if (!result_) result_ = continueStatement(builder_, level_ + 1);
    if (!result_) result_ = ifElseStatement(builder_, level_ + 1);
    if (!result_) result_ = whileStatement(builder_, level_ + 1);
    if (!result_) result_ = foreachStatement(builder_, level_ + 1);
    if (!result_) result_ = forStatement(builder_, level_ + 1);
    if (!result_) result_ = throwStatement(builder_, level_ + 1);
    if (!result_) result_ = tryCatchStatement(builder_, level_ + 1);
    if (!result_) result_ = usingDecl(builder_, level_ + 1);
    if (!result_) result_ = variableDecl(builder_, level_ + 1);
    if (!result_) result_ = expressionStatement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // qualifiedIdentifier '(' ')'
  public static boolean staticDep(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "staticDep")) return false;
    if (!nextTokenIs(builder_, "<static dependency>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATIC_DEP, "<static dependency>");
    result_ = qualifiedIdentifier(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, PUNC_LPAREN, PUNC_RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // staticDep (',' staticDep)*
  public static boolean staticDepList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "staticDepList")) return false;
    if (!nextTokenIs(builder_, "<static dependency list>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATIC_DEP_LIST, "<static dependency list>");
    result_ = staticDep(builder_, level_ + 1);
    result_ = result_ && staticDepList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' staticDep)*
  private static boolean staticDepList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "staticDepList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!staticDepList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "staticDepList_1", pos_)) break;
    }
    return true;
  }

  // ',' staticDep
  private static boolean staticDepList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "staticDepList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && staticDep(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // typeSpec | conditionalExpr
  public static boolean templateArg(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateArg")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEMPLATE_ARG, "<template argument>");
    result_ = typeSpec(builder_, level_ + 1);
    if (!result_) result_ = conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '<' templateArg (',' templateArg)* '>'
  public static boolean templateArgList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateArgList")) return false;
    if (!nextTokenIs(builder_, "<template argument list>", OP_LT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEMPLATE_ARG_LIST, "<template argument list>");
    result_ = consumeToken(builder_, OP_LT);
    result_ = result_ && templateArg(builder_, level_ + 1);
    result_ = result_ && templateArgList_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_GT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' templateArg)*
  private static boolean templateArgList_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateArgList_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!templateArgList_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "templateArgList_2", pos_)) break;
    }
    return true;
  }

  // ',' templateArg
  private static boolean templateArgList_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateArgList_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && templateArg(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'template' '<' templateParameterList '>'
  public static boolean templateDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateDeclaration")) return false;
    if (!nextTokenIs(builder_, "<template declaration>", KW_TEMPLATE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEMPLATE_DECLARATION, "<template declaration>");
    result_ = consumeTokens(builder_, 0, KW_TEMPLATE, OP_LT);
    result_ = result_ && templateParameterList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_GT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // templateParameterKind '...'? IDENTIFIER (':' typeSpec)? ('=' conditionalExpr)?
  public static boolean templateParameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameter")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEMPLATE_PARAMETER, "<template parameter>");
    result_ = templateParameterKind(builder_, level_ + 1);
    result_ = result_ && templateParameter_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    result_ = result_ && templateParameter_3(builder_, level_ + 1);
    result_ = result_ && templateParameter_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '...'?
  private static boolean templateParameter_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameter_1")) return false;
    consumeToken(builder_, PUNC_ELLIPSIS);
    return true;
  }

  // (':' typeSpec)?
  private static boolean templateParameter_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameter_3")) return false;
    templateParameter_3_0(builder_, level_ + 1);
    return true;
  }

  // ':' typeSpec
  private static boolean templateParameter_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameter_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('=' conditionalExpr)?
  private static boolean templateParameter_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameter_4")) return false;
    templateParameter_4_0(builder_, level_ + 1);
    return true;
  }

  // '=' conditionalExpr
  private static boolean templateParameter_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameter_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_ASSIGN);
    result_ = result_ && conditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'typename' | 'struct' | 'class' | 'interface'
  //                         | fundamentalTypeSpec
  //                         | IDENTIFIER
  public static boolean templateParameterKind(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameterKind")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEMPLATE_PARAMETER_KIND, "<template parameter kind>");
    result_ = consumeToken(builder_, KW_TYPENAME);
    if (!result_) result_ = consumeToken(builder_, KW_STRUCT);
    if (!result_) result_ = consumeToken(builder_, KW_CLASS);
    if (!result_) result_ = consumeToken(builder_, KW_INTERFACE);
    if (!result_) result_ = fundamentalTypeSpec(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // templateParameter (',' templateParameter)*
  public static boolean templateParameterList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameterList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEMPLATE_PARAMETER_LIST, "<template parameter list>");
    result_ = templateParameter(builder_, level_ + 1);
    result_ = result_ && templateParameterList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' templateParameter)*
  private static boolean templateParameterList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameterList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!templateParameterList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "templateParameterList_1", pos_)) break;
    }
    return true;
  }

  // ',' templateParameter
  private static boolean templateParameterList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateParameterList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && templateParameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '::'? IDENTIFIER ('::' IDENTIFIER)* templateArgList ('::' IDENTIFIER)+
  public static boolean templateQualifiedScopeExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateQualifiedScopeExpr")) return false;
    if (!nextTokenIs(builder_, "<template-qualified scope expression>", IDENTIFIER, PUNC_SCOPE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEMPLATE_QUALIFIED_SCOPE_EXPR, "<template-qualified scope expression>");
    result_ = templateQualifiedScopeExpr_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    result_ = result_ && templateQualifiedScopeExpr_2(builder_, level_ + 1);
    result_ = result_ && templateArgList(builder_, level_ + 1);
    result_ = result_ && templateQualifiedScopeExpr_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '::'?
  private static boolean templateQualifiedScopeExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateQualifiedScopeExpr_0")) return false;
    consumeToken(builder_, PUNC_SCOPE);
    return true;
  }

  // ('::' IDENTIFIER)*
  private static boolean templateQualifiedScopeExpr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateQualifiedScopeExpr_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!templateQualifiedScopeExpr_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "templateQualifiedScopeExpr_2", pos_)) break;
    }
    return true;
  }

  // '::' IDENTIFIER
  private static boolean templateQualifiedScopeExpr_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateQualifiedScopeExpr_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, PUNC_SCOPE, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('::' IDENTIFIER)+
  private static boolean templateQualifiedScopeExpr_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateQualifiedScopeExpr_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = templateQualifiedScopeExpr_4_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!templateQualifiedScopeExpr_4_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "templateQualifiedScopeExpr_4", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '::' IDENTIFIER
  private static boolean templateQualifiedScopeExpr_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "templateQualifiedScopeExpr_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, PUNC_SCOPE, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'throw' expression? ';'
  public static boolean throwStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwStatement")) return false;
    if (!nextTokenIs(builder_, "<throw statement>", KW_THROW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, THROW_STATEMENT, "<throw statement>");
    result_ = consumeToken(builder_, KW_THROW);
    result_ = result_ && throwStatement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // expression?
  private static boolean throwStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwStatement_1")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'throws' typeSpec (',' typeSpec)*
  public static boolean throwsClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwsClause")) return false;
    if (!nextTokenIs(builder_, "<throws clause>", KW_THROWS)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, THROWS_CLAUSE, "<throws clause>");
    result_ = consumeToken(builder_, KW_THROWS);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    result_ = result_ && throwsClause_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' typeSpec)*
  private static boolean throwsClause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwsClause_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!throwsClause_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "throwsClause_2", pos_)) break;
    }
    return true;
  }

  // ',' typeSpec
  private static boolean throwsClause_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwsClause_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'try' blockStatement catchClause* finallyClause?
  public static boolean tryCatchStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryCatchStatement")) return false;
    if (!nextTokenIs(builder_, "<try/catch statement>", KW_TRY)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TRY_CATCH_STATEMENT, "<try/catch statement>");
    result_ = consumeToken(builder_, KW_TRY);
    result_ = result_ && blockStatement(builder_, level_ + 1);
    result_ = result_ && tryCatchStatement_2(builder_, level_ + 1);
    result_ = result_ && tryCatchStatement_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // catchClause*
  private static boolean tryCatchStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryCatchStatement_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!catchClause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tryCatchStatement_2", pos_)) break;
    }
    return true;
  }

  // finallyClause?
  private static boolean tryCatchStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryCatchStatement_3")) return false;
    finallyClause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // typeSpec (',' typeSpec)*
  public static boolean typeList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_LIST, "<type list>");
    result_ = typeSpec(builder_, level_ + 1);
    result_ = result_ && typeList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' typeSpec)*
  private static boolean typeList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!typeList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "typeList_1", pos_)) break;
    }
    return true;
  }

  // ',' typeSpec
  private static boolean typeList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // qualifiedIdentifier
  //            | fundamentalTypeSpec
  public static boolean typeName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeName")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_NAME, "<type name>");
    result_ = qualifiedIdentifier(builder_, level_ + 1);
    if (!result_) result_ = fundamentalTypeSpec(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // functionRefType
  //            | qualifiedIdentifier '::' functionRefType
  //            | 'const'? (fundamentalTypeSpec | qualifiedIdentifier) typeSuffix*
  public static boolean typeSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpec")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_SPEC, "<type>");
    result_ = functionRefType(builder_, level_ + 1);
    if (!result_) result_ = typeSpec_1(builder_, level_ + 1);
    if (!result_) result_ = typeSpec_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // qualifiedIdentifier '::' functionRefType
  private static boolean typeSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpec_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = qualifiedIdentifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SCOPE);
    result_ = result_ && functionRefType(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'const'? (fundamentalTypeSpec | qualifiedIdentifier) typeSuffix*
  private static boolean typeSpec_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpec_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = typeSpec_2_0(builder_, level_ + 1);
    result_ = result_ && typeSpec_2_1(builder_, level_ + 1);
    result_ = result_ && typeSpec_2_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'const'?
  private static boolean typeSpec_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpec_2_0")) return false;
    consumeToken(builder_, KW_CONST);
    return true;
  }

  // fundamentalTypeSpec | qualifiedIdentifier
  private static boolean typeSpec_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpec_2_1")) return false;
    boolean result_;
    result_ = fundamentalTypeSpec(builder_, level_ + 1);
    if (!result_) result_ = qualifiedIdentifier(builder_, level_ + 1);
    return result_;
  }

  // typeSuffix*
  private static boolean typeSpec_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpec_2_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!typeSuffix(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "typeSpec_2_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // typeSpec (',' typeSpec)*
  public static boolean typeSpecList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpecList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_SPEC_LIST, "<type list>");
    result_ = typeSpec(builder_, level_ + 1);
    result_ = result_ && typeSpecList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' typeSpec)*
  private static boolean typeSpecList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpecList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!typeSpecList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "typeSpecList_1", pos_)) break;
    }
    return true;
  }

  // ',' typeSpec
  private static boolean typeSpecList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSpecList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_COMMA);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '[' LIT_INTEGER? ']'
  //              | '!'
  //              | '*'
  //              | '&'
  //              | '+'
  //              | '?'
  //              | '#'
  public static boolean typeSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSuffix")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_SUFFIX, "<type suffix (*/&/[]/…)>");
    result_ = typeSuffix_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, OP_NOT);
    if (!result_) result_ = consumeToken(builder_, OP_STAR);
    if (!result_) result_ = consumeToken(builder_, OP_AMP);
    if (!result_) result_ = consumeToken(builder_, OP_PLUS);
    if (!result_) result_ = consumeToken(builder_, OP_QUESTION);
    if (!result_) result_ = consumeToken(builder_, OP_HASH);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '[' LIT_INTEGER? ']'
  private static boolean typeSuffix_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSuffix_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PUNC_LBRACKET);
    result_ = result_ && typeSuffix_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LIT_INTEGER?
  private static boolean typeSuffix_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeSuffix_0_1")) return false;
    consumeToken(builder_, LIT_INTEGER);
    return true;
  }

  /* ********************************************************** */
  // ('++' | '--' | '*' | '&' | '+' | '-' | '!' | '~' | '#') castExpr
  //             | newExpr
  //             | 'delete' castExpr
  //             | postfixExpr
  public static boolean unaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNARY_EXPR, "<unary expression>");
    result_ = unaryExpr_0(builder_, level_ + 1);
    if (!result_) result_ = newExpr(builder_, level_ + 1);
    if (!result_) result_ = unaryExpr_2(builder_, level_ + 1);
    if (!result_) result_ = postfixExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('++' | '--' | '*' | '&' | '+' | '-' | '!' | '~' | '#') castExpr
  private static boolean unaryExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unaryExpr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unaryExpr_0_0(builder_, level_ + 1);
    result_ = result_ && castExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '++' | '--' | '*' | '&' | '+' | '-' | '!' | '~' | '#'
  private static boolean unaryExpr_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unaryExpr_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OP_INC);
    if (!result_) result_ = consumeToken(builder_, OP_DEC);
    if (!result_) result_ = consumeToken(builder_, OP_STAR);
    if (!result_) result_ = consumeToken(builder_, OP_AMP);
    if (!result_) result_ = consumeToken(builder_, OP_PLUS);
    if (!result_) result_ = consumeToken(builder_, OP_MINUS);
    if (!result_) result_ = consumeToken(builder_, OP_NOT);
    if (!result_) result_ = consumeToken(builder_, OP_TILDE);
    if (!result_) result_ = consumeToken(builder_, OP_HASH);
    return result_;
  }

  // 'delete' castExpr
  private static boolean unaryExpr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unaryExpr_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, KW_DELETE);
    result_ = result_ && castExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // annotationDef* specifier*
  //               'union' IDENTIFIER
  //               (':' qualifiedIdentifier)?
  //               '{' unionMemberDecl* '}'
  public static boolean unionDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionDecl")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNION_DECL, "<union declaration>");
    result_ = unionDecl_0(builder_, level_ + 1);
    result_ = result_ && unionDecl_1(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 1, KW_UNION, IDENTIFIER);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, unionDecl_4(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, PUNC_LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, unionDecl_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, PUNC_RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // annotationDef*
  private static boolean unionDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionDecl_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!annotationDef(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unionDecl_0", pos_)) break;
    }
    return true;
  }

  // specifier*
  private static boolean unionDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionDecl_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unionDecl_1", pos_)) break;
    }
    return true;
  }

  // (':' qualifiedIdentifier)?
  private static boolean unionDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionDecl_4")) return false;
    unionDecl_4_0(builder_, level_ + 1);
    return true;
  }

  // ':' qualifiedIdentifier
  private static boolean unionDecl_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionDecl_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OP_COLON);
    result_ = result_ && qualifiedIdentifier(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // unionMemberDecl*
  private static boolean unionDecl_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionDecl_6")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!unionMemberDecl(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unionDecl_6", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'const'? IDENTIFIER ':' typeSpec ';'
  public static boolean unionMemberDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionMemberDecl")) return false;
    if (!nextTokenIs(builder_, "<union member declaration>", IDENTIFIER, KW_CONST)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNION_MEMBER_DECL, "<union member declaration>");
    result_ = unionMemberDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, IDENTIFIER, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'const'?
  private static boolean unionMemberDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unionMemberDecl_0")) return false;
    consumeToken(builder_, KW_CONST);
    return true;
  }

  /* ********************************************************** */
  // moduleDeclaration? importDeclaration* declaration*
  static boolean unit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unit")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null, "<compilation unit>");
    result_ = unit_0(builder_, level_ + 1);
    result_ = result_ && unit_1(builder_, level_ + 1);
    result_ = result_ && unit_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // moduleDeclaration?
  private static boolean unit_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unit_0")) return false;
    moduleDeclaration(builder_, level_ + 1);
    return true;
  }

  // importDeclaration*
  private static boolean unit_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unit_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!importDeclaration(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unit_1", pos_)) break;
    }
    return true;
  }

  // declaration*
  private static boolean unit_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unit_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!declaration(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unit_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'using' usingFilter? (IDENTIFIER '=')? qualifiedIdentifier ';'
  public static boolean usingDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "usingDecl")) return false;
    if (!nextTokenIs(builder_, "<using declaration>", KW_USING)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, USING_DECL, "<using declaration>");
    result_ = consumeToken(builder_, KW_USING);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, usingDecl_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, usingDecl_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, qualifiedIdentifier(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, PUNC_SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // usingFilter?
  private static boolean usingDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "usingDecl_1")) return false;
    usingFilter(builder_, level_ + 1);
    return true;
  }

  // (IDENTIFIER '=')?
  private static boolean usingDecl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "usingDecl_2")) return false;
    usingDecl_2_0(builder_, level_ + 1);
    return true;
  }

  // IDENTIFIER '='
  private static boolean usingDecl_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "usingDecl_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, IDENTIFIER, OP_ASSIGN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'namespace' | 'struct' | 'interface' | 'class'
  public static boolean usingFilter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "usingFilter")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, USING_FILTER, "<using filter (namespace/struct/interface/class)>");
    result_ = consumeToken(builder_, KW_NAMESPACE);
    if (!result_) result_ = consumeToken(builder_, KW_STRUCT);
    if (!result_) result_ = consumeToken(builder_, KW_INTERFACE);
    if (!result_) result_ = consumeToken(builder_, KW_CLASS);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // specifier* IDENTIFIER ':' typeSpec initialiser? ';'
  public static boolean variableDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variableDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VARIABLE_DECL, "<variable declaration>");
    result_ = variableDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, IDENTIFIER, OP_COLON);
    result_ = result_ && typeSpec(builder_, level_ + 1);
    result_ = result_ && variableDecl_4(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // specifier*
  private static boolean variableDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variableDecl_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!specifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "variableDecl_0", pos_)) break;
    }
    return true;
  }

  // initialiser?
  private static boolean variableDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variableDecl_4")) return false;
    initialiser(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ('public' | 'protected' | 'private') ':'
  public static boolean visibilityDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "visibilityDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VISIBILITY_DECL, "<visibility label (public/protected/private:)>");
    result_ = visibilityDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OP_COLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'public' | 'protected' | 'private'
  private static boolean visibilityDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "visibilityDecl_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, KW_PUBLIC);
    if (!result_) result_ = consumeToken(builder_, KW_PROTECTED);
    if (!result_) result_ = consumeToken(builder_, KW_PRIVATE);
    return result_;
  }

  /* ********************************************************** */
  // 'while' '(' expression ')' statement
  public static boolean whileStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "whileStatement")) return false;
    if (!nextTokenIs(builder_, "<while statement>", KW_WHILE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WHILE_STATEMENT, "<while statement>");
    result_ = consumeTokens(builder_, 0, KW_WHILE, PUNC_LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PUNC_RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

}
