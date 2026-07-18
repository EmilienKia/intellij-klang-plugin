// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.github.emilienkia.klang.plugin.language.psi.impl.*;

public interface KlangTypes {

  IElementType ADDITIVE_EXPR = new KlangElementType("ADDITIVE_EXPR");
  IElementType AGGREGATE_DECL = new KlangElementType("AGGREGATE_DECL");
  IElementType ANNOTATION_DEF = new KlangElementType("ANNOTATION_DEF");
  IElementType ASSIGNMENT_EXPR = new KlangElementType("ASSIGNMENT_EXPR");
  IElementType ASSIGNMENT_OPERATOR = new KlangElementType("ASSIGNMENT_OPERATOR");
  IElementType BASE_CLAUSE = new KlangElementType("BASE_CLAUSE");
  IElementType BASE_SPEC = new KlangElementType("BASE_SPEC");
  IElementType BIN_AND_EXPR = new KlangElementType("BIN_AND_EXPR");
  IElementType BLOCK_STATEMENT = new KlangElementType("BLOCK_STATEMENT");
  IElementType BRACE_INIT_LIST = new KlangElementType("BRACE_INIT_LIST");
  IElementType BREAK_STATEMENT = new KlangElementType("BREAK_STATEMENT");
  IElementType CAST_EXPR = new KlangElementType("CAST_EXPR");
  IElementType CAST_OPERATOR_FUNCTION_HEAD = new KlangElementType("CAST_OPERATOR_FUNCTION_HEAD");
  IElementType CATCH_CLAUSE = new KlangElementType("CATCH_CLAUSE");
  IElementType CATCH_PARAMETER_DECL = new KlangElementType("CATCH_PARAMETER_DECL");
  IElementType CONDITIONAL_EXPR = new KlangElementType("CONDITIONAL_EXPR");
  IElementType COND_VAR_INITIALISER = new KlangElementType("COND_VAR_INITIALISER");
  IElementType CONTINUE_STATEMENT = new KlangElementType("CONTINUE_STATEMENT");
  IElementType DECLARATION = new KlangElementType("DECLARATION");
  IElementType DESIGNATED_INIT_ELEMENT = new KlangElementType("DESIGNATED_INIT_ELEMENT");
  IElementType DESIGNATED_MEMBER_NAME = new KlangElementType("DESIGNATED_MEMBER_NAME");
  IElementType DESTRUCTOR_HEAD = new KlangElementType("DESTRUCTOR_HEAD");
  IElementType ENUM_DECL = new KlangElementType("ENUM_DECL");
  IElementType ENUM_ENTRY = new KlangElementType("ENUM_ENTRY");
  IElementType EQUALITY_EXPR = new KlangElementType("EQUALITY_EXPR");
  IElementType EXCLUSIVE_BIN_OR_EXPR = new KlangElementType("EXCLUSIVE_BIN_OR_EXPR");
  IElementType EXPRESSION = new KlangElementType("EXPRESSION");
  IElementType EXPRESSION_LIST = new KlangElementType("EXPRESSION_LIST");
  IElementType EXPRESSION_STATEMENT = new KlangElementType("EXPRESSION_STATEMENT");
  IElementType FINALLY_CLAUSE = new KlangElementType("FINALLY_CLAUSE");
  IElementType FOREACH_STATEMENT = new KlangElementType("FOREACH_STATEMENT");
  IElementType FOREACH_VAR_DECL = new KlangElementType("FOREACH_VAR_DECL");
  IElementType FOR_STATEMENT = new KlangElementType("FOR_STATEMENT");
  IElementType FRIEND_DECL = new KlangElementType("FRIEND_DECL");
  IElementType FRIEND_FILTER = new KlangElementType("FRIEND_FILTER");
  IElementType FUNCTION_BODY = new KlangElementType("FUNCTION_BODY");
  IElementType FUNCTION_DECL = new KlangElementType("FUNCTION_DECL");
  IElementType FUNCTION_HEAD = new KlangElementType("FUNCTION_HEAD");
  IElementType FUNCTION_REF_QUALIFIER = new KlangElementType("FUNCTION_REF_QUALIFIER");
  IElementType FUNCTION_REF_TYPE = new KlangElementType("FUNCTION_REF_TYPE");
  IElementType FUNDAMENTAL_TYPE_SPEC = new KlangElementType("FUNDAMENTAL_TYPE_SPEC");
  IElementType GENERIC_DECLARATION = new KlangElementType("GENERIC_DECLARATION");
  IElementType IDENTIFIER_EXPR = new KlangElementType("IDENTIFIER_EXPR");
  IElementType IDENTIFIER_SEGMENT = new KlangElementType("IDENTIFIER_SEGMENT");
  IElementType IF_COND_VAR_DECL = new KlangElementType("IF_COND_VAR_DECL");
  IElementType IF_COND_VAR_DECL_LIST = new KlangElementType("IF_COND_VAR_DECL_LIST");
  IElementType IF_ELSE_STATEMENT = new KlangElementType("IF_ELSE_STATEMENT");
  IElementType IMPORT_DECLARATION = new KlangElementType("IMPORT_DECLARATION");
  IElementType INCLUSIVE_BIN_OR_EXPR = new KlangElementType("INCLUSIVE_BIN_OR_EXPR");
  IElementType INITIALISER = new KlangElementType("INITIALISER");
  IElementType INIT_ELEMENT = new KlangElementType("INIT_ELEMENT");
  IElementType LITERAL = new KlangElementType("LITERAL");
  IElementType LOGICAL_AND_EXPR = new KlangElementType("LOGICAL_AND_EXPR");
  IElementType LOGICAL_OR_EXPR = new KlangElementType("LOGICAL_OR_EXPR");
  IElementType MEMBER_INIT = new KlangElementType("MEMBER_INIT");
  IElementType MEMBER_INIT_LIST = new KlangElementType("MEMBER_INIT_LIST");
  IElementType MODULE_DECLARATION = new KlangElementType("MODULE_DECLARATION");
  IElementType MULTIPLICATIVE_EXPR = new KlangElementType("MULTIPLICATIVE_EXPR");
  IElementType NAMED_RETURN_INIT = new KlangElementType("NAMED_RETURN_INIT");
  IElementType NAMED_RETURN_VAR = new KlangElementType("NAMED_RETURN_VAR");
  IElementType NAMESPACE_DECL = new KlangElementType("NAMESPACE_DECL");
  IElementType NEW_EXPR = new KlangElementType("NEW_EXPR");
  IElementType OPERATOR_FUNCTION_HEAD = new KlangElementType("OPERATOR_FUNCTION_HEAD");
  IElementType OPERATOR_SYMBOL = new KlangElementType("OPERATOR_SYMBOL");
  IElementType PARAMETER_LIST = new KlangElementType("PARAMETER_LIST");
  IElementType PARAMETER_SPEC = new KlangElementType("PARAMETER_SPEC");
  IElementType PM_EXPR = new KlangElementType("PM_EXPR");
  IElementType POSTFIX_EXPR = new KlangElementType("POSTFIX_EXPR");
  IElementType POSTFIX_OP = new KlangElementType("POSTFIX_OP");
  IElementType PRIMARY_EXPR = new KlangElementType("PRIMARY_EXPR");
  IElementType PRIMITIVE_ARRAY_ELEMENT_TYPE = new KlangElementType("PRIMITIVE_ARRAY_ELEMENT_TYPE");
  IElementType QUALIFIED_IDENTIFIER = new KlangElementType("QUALIFIED_IDENTIFIER");
  IElementType RELATIONAL_EXPR = new KlangElementType("RELATIONAL_EXPR");
  IElementType RETURN_STATEMENT = new KlangElementType("RETURN_STATEMENT");
  IElementType RETURN_TYPE_OR_MEMBER_INIT_LIST = new KlangElementType("RETURN_TYPE_OR_MEMBER_INIT_LIST");
  IElementType SHIFTING_EXPR = new KlangElementType("SHIFTING_EXPR");
  IElementType SPACESHIP_EXPR = new KlangElementType("SPACESHIP_EXPR");
  IElementType SPECIFIER = new KlangElementType("SPECIFIER");
  IElementType STATEMENT = new KlangElementType("STATEMENT");
  IElementType STATIC_DEP = new KlangElementType("STATIC_DEP");
  IElementType STATIC_DEP_LIST = new KlangElementType("STATIC_DEP_LIST");
  IElementType TEMPLATE_ARG = new KlangElementType("TEMPLATE_ARG");
  IElementType TEMPLATE_ARG_LIST = new KlangElementType("TEMPLATE_ARG_LIST");
  IElementType TEMPLATE_DECLARATION = new KlangElementType("TEMPLATE_DECLARATION");
  IElementType TEMPLATE_PARAMETER = new KlangElementType("TEMPLATE_PARAMETER");
  IElementType TEMPLATE_PARAMETER_KIND = new KlangElementType("TEMPLATE_PARAMETER_KIND");
  IElementType TEMPLATE_PARAMETER_LIST = new KlangElementType("TEMPLATE_PARAMETER_LIST");
  IElementType TEMPLATE_QUALIFIED_SCOPE_EXPR = new KlangElementType("TEMPLATE_QUALIFIED_SCOPE_EXPR");
  IElementType THROWS_CLAUSE = new KlangElementType("THROWS_CLAUSE");
  IElementType THROW_STATEMENT = new KlangElementType("THROW_STATEMENT");
  IElementType TRY_CATCH_STATEMENT = new KlangElementType("TRY_CATCH_STATEMENT");
  IElementType TYPE_LIST = new KlangElementType("TYPE_LIST");
  IElementType TYPE_NAME = new KlangElementType("TYPE_NAME");
  IElementType TYPE_SPEC = new KlangElementType("TYPE_SPEC");
  IElementType TYPE_SPEC_LIST = new KlangElementType("TYPE_SPEC_LIST");
  IElementType TYPE_SUFFIX = new KlangElementType("TYPE_SUFFIX");
  IElementType UNARY_EXPR = new KlangElementType("UNARY_EXPR");
  IElementType UNION_DECL = new KlangElementType("UNION_DECL");
  IElementType UNION_MEMBER_DECL = new KlangElementType("UNION_MEMBER_DECL");
  IElementType USING_DECL = new KlangElementType("USING_DECL");
  IElementType USING_FILTER = new KlangElementType("USING_FILTER");
  IElementType VARIABLE_DECL = new KlangElementType("VARIABLE_DECL");
  IElementType VISIBILITY_DECL = new KlangElementType("VISIBILITY_DECL");
  IElementType WHILE_STATEMENT = new KlangElementType("WHILE_STATEMENT");

  IElementType BLOCK_COMMENT = new KlangTokenType("BLOCK_COMMENT");
  IElementType BLOCK_DOC_COMMENT = new KlangTokenType("BLOCK_DOC_COMMENT");
  IElementType BLOCK_DOC_COMMENT_BWD = new KlangTokenType("BLOCK_DOC_COMMENT_BWD");
  IElementType IDENTIFIER = new KlangTokenType("IDENTIFIER");
  IElementType INITELEMENT_1_0 = new KlangTokenType("initElement_1_0");
  IElementType KW_ABSTRACT = new KlangTokenType("abstract");
  IElementType KW_ANNOTATION = new KlangTokenType("annotation");
  IElementType KW_BOOL = new KlangTokenType("bool");
  IElementType KW_BREAK = new KlangTokenType("break");
  IElementType KW_BYTE = new KlangTokenType("byte");
  IElementType KW_CATCH = new KlangTokenType("catch");
  IElementType KW_CHAR = new KlangTokenType("char");
  IElementType KW_CLASS = new KlangTokenType("class");
  IElementType KW_CONST = new KlangTokenType("const");
  IElementType KW_CONTINUE = new KlangTokenType("continue");
  IElementType KW_DEFAULT = new KlangTokenType("default");
  IElementType KW_DELETE = new KlangTokenType("delete");
  IElementType KW_DOUBLE = new KlangTokenType("double");
  IElementType KW_ELSE = new KlangTokenType("else");
  IElementType KW_ENUM = new KlangTokenType("enum");
  IElementType KW_FINAL = new KlangTokenType("final");
  IElementType KW_FINALLY = new KlangTokenType("finally");
  IElementType KW_FLOAT = new KlangTokenType("float");
  IElementType KW_FOR = new KlangTokenType("for");
  IElementType KW_FRIEND = new KlangTokenType("friend");
  IElementType KW_GENERIC = new KlangTokenType("generic");
  IElementType KW_IF = new KlangTokenType("if");
  IElementType KW_IMPORT = new KlangTokenType("import");
  IElementType KW_INT = new KlangTokenType("int");
  IElementType KW_INTERFACE = new KlangTokenType("interface");
  IElementType KW_LONG = new KlangTokenType("long");
  IElementType KW_MODULE = new KlangTokenType("module");
  IElementType KW_NAMESPACE = new KlangTokenType("namespace");
  IElementType KW_NEW = new KlangTokenType("new");
  IElementType KW_OPERATOR = new KlangTokenType("operator");
  IElementType KW_OVERRIDE = new KlangTokenType("override");
  IElementType KW_PRIVATE = new KlangTokenType("private");
  IElementType KW_PROTECTED = new KlangTokenType("protected");
  IElementType KW_PUBLIC = new KlangTokenType("public");
  IElementType KW_RETURN = new KlangTokenType("return");
  IElementType KW_SHORT = new KlangTokenType("short");
  IElementType KW_STATIC = new KlangTokenType("static");
  IElementType KW_STRUCT = new KlangTokenType("struct");
  IElementType KW_TEMPLATE = new KlangTokenType("template");
  IElementType KW_THIS = new KlangTokenType("this");
  IElementType KW_THROW = new KlangTokenType("throw");
  IElementType KW_THROWS = new KlangTokenType("throws");
  IElementType KW_TRY = new KlangTokenType("try");
  IElementType KW_TYPENAME = new KlangTokenType("typename");
  IElementType KW_UNION = new KlangTokenType("union");
  IElementType KW_UNSIGNED = new KlangTokenType("unsigned");
  IElementType KW_USING = new KlangTokenType("using");
  IElementType KW_WHILE = new KlangTokenType("while");
  IElementType LINE_COMMENT = new KlangTokenType("LINE_COMMENT");
  IElementType LINE_DOC_COMMENT = new KlangTokenType("LINE_DOC_COMMENT");
  IElementType LINE_DOC_COMMENT_BWD = new KlangTokenType("LINE_DOC_COMMENT_BWD");
  IElementType LIT_CHAR = new KlangTokenType("LIT_CHAR");
  IElementType LIT_FALSE = new KlangTokenType("false");
  IElementType LIT_FLOAT = new KlangTokenType("LIT_FLOAT");
  IElementType LIT_INTEGER = new KlangTokenType("LIT_INTEGER");
  IElementType LIT_NULL = new KlangTokenType("null");
  IElementType LIT_STRING = new KlangTokenType("LIT_STRING");
  IElementType LIT_TRUE = new KlangTokenType("true");
  IElementType OP_AMP = new KlangTokenType("&");
  IElementType OP_AND = new KlangTokenType("&&");
  IElementType OP_AND_ASSIGN = new KlangTokenType("&=");
  IElementType OP_ARROW = new KlangTokenType("->");
  IElementType OP_ARROW_STAR = new KlangTokenType("->*");
  IElementType OP_ASSIGN = new KlangTokenType("=");
  IElementType OP_CARET = new KlangTokenType("^");
  IElementType OP_COLON = new KlangTokenType(":");
  IElementType OP_DEC = new KlangTokenType("--");
  IElementType OP_DIV = new KlangTokenType("/");
  IElementType OP_DIV_ASSIGN = new KlangTokenType("/=");
  IElementType OP_DOT = new KlangTokenType(".");
  IElementType OP_DOT_STAR = new KlangTokenType(".*");
  IElementType OP_EQ = new KlangTokenType("==");
  IElementType OP_GE = new KlangTokenType(">=");
  IElementType OP_GT = new KlangTokenType(">");
  IElementType OP_HASH = new KlangTokenType("#");
  IElementType OP_INC = new KlangTokenType("++");
  IElementType OP_LE = new KlangTokenType("<=");
  IElementType OP_LSHIFT = new KlangTokenType("<<");
  IElementType OP_LSHIFT_ASSIGN = new KlangTokenType("<<=");
  IElementType OP_LT = new KlangTokenType("<");
  IElementType OP_MINUS = new KlangTokenType("-");
  IElementType OP_MINUS_ASSIGN = new KlangTokenType("-=");
  IElementType OP_MOD_ASSIGN = new KlangTokenType("%=");
  IElementType OP_NEQ = new KlangTokenType("!=");
  IElementType OP_NOT = new KlangTokenType("!");
  IElementType OP_OR = new KlangTokenType("||");
  IElementType OP_OR_ASSIGN = new KlangTokenType("|=");
  IElementType OP_PERCENT = new KlangTokenType("%");
  IElementType OP_PIPE = new KlangTokenType("|");
  IElementType OP_PLUS = new KlangTokenType("+");
  IElementType OP_PLUS_ASSIGN = new KlangTokenType("+=");
  IElementType OP_POW = new KlangTokenType("**");
  IElementType OP_QUESTION = new KlangTokenType("?");
  IElementType OP_RSHIFT = new KlangTokenType(">>");
  IElementType OP_RSHIFT_ASSIGN = new KlangTokenType(">>=");
  IElementType OP_SPACESHIP = new KlangTokenType("<=>");
  IElementType OP_STAR = new KlangTokenType("*");
  IElementType OP_STAR_ASSIGN = new KlangTokenType("*=");
  IElementType OP_TILDE = new KlangTokenType("~");
  IElementType OP_XOR_ASSIGN = new KlangTokenType("^=");
  IElementType PUNC_AT = new KlangTokenType("@");
  IElementType PUNC_COMMA = new KlangTokenType(",");
  IElementType PUNC_ELLIPSIS = new KlangTokenType("...");
  IElementType PUNC_LBRACE = new KlangTokenType("{");
  IElementType PUNC_LBRACKET = new KlangTokenType("[");
  IElementType PUNC_LPAREN = new KlangTokenType("(");
  IElementType PUNC_RBRACE = new KlangTokenType("}");
  IElementType PUNC_RBRACKET = new KlangTokenType("]");
  IElementType PUNC_RPAREN = new KlangTokenType(")");
  IElementType PUNC_SCOPE = new KlangTokenType("::");
  IElementType PUNC_SEMICOLON = new KlangTokenType(";");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ADDITIVE_EXPR) {
        return new KlangAdditiveExprImpl(node);
      }
      else if (type == AGGREGATE_DECL) {
        return new KlangAggregateDeclImpl(node);
      }
      else if (type == ANNOTATION_DEF) {
        return new KlangAnnotationDefImpl(node);
      }
      else if (type == ASSIGNMENT_EXPR) {
        return new KlangAssignmentExprImpl(node);
      }
      else if (type == ASSIGNMENT_OPERATOR) {
        return new KlangAssignmentOperatorImpl(node);
      }
      else if (type == BASE_CLAUSE) {
        return new KlangBaseClauseImpl(node);
      }
      else if (type == BASE_SPEC) {
        return new KlangBaseSpecImpl(node);
      }
      else if (type == BIN_AND_EXPR) {
        return new KlangBinAndExprImpl(node);
      }
      else if (type == BLOCK_STATEMENT) {
        return new KlangBlockStatementImpl(node);
      }
      else if (type == BRACE_INIT_LIST) {
        return new KlangBraceInitListImpl(node);
      }
      else if (type == BREAK_STATEMENT) {
        return new KlangBreakStatementImpl(node);
      }
      else if (type == CAST_EXPR) {
        return new KlangCastExprImpl(node);
      }
      else if (type == CAST_OPERATOR_FUNCTION_HEAD) {
        return new KlangCastOperatorFunctionHeadImpl(node);
      }
      else if (type == CATCH_CLAUSE) {
        return new KlangCatchClauseImpl(node);
      }
      else if (type == CATCH_PARAMETER_DECL) {
        return new KlangCatchParameterDeclImpl(node);
      }
      else if (type == CONDITIONAL_EXPR) {
        return new KlangConditionalExprImpl(node);
      }
      else if (type == COND_VAR_INITIALISER) {
        return new KlangCondVarInitialiserImpl(node);
      }
      else if (type == CONTINUE_STATEMENT) {
        return new KlangContinueStatementImpl(node);
      }
      else if (type == DECLARATION) {
        return new KlangDeclarationImpl(node);
      }
      else if (type == DESIGNATED_INIT_ELEMENT) {
        return new KlangDesignatedInitElementImpl(node);
      }
      else if (type == DESIGNATED_MEMBER_NAME) {
        return new KlangDesignatedMemberNameImpl(node);
      }
      else if (type == DESTRUCTOR_HEAD) {
        return new KlangDestructorHeadImpl(node);
      }
      else if (type == ENUM_DECL) {
        return new KlangEnumDeclImpl(node);
      }
      else if (type == ENUM_ENTRY) {
        return new KlangEnumEntryImpl(node);
      }
      else if (type == EQUALITY_EXPR) {
        return new KlangEqualityExprImpl(node);
      }
      else if (type == EXCLUSIVE_BIN_OR_EXPR) {
        return new KlangExclusiveBinOrExprImpl(node);
      }
      else if (type == EXPRESSION) {
        return new KlangExpressionImpl(node);
      }
      else if (type == EXPRESSION_LIST) {
        return new KlangExpressionListImpl(node);
      }
      else if (type == EXPRESSION_STATEMENT) {
        return new KlangExpressionStatementImpl(node);
      }
      else if (type == FINALLY_CLAUSE) {
        return new KlangFinallyClauseImpl(node);
      }
      else if (type == FOREACH_STATEMENT) {
        return new KlangForeachStatementImpl(node);
      }
      else if (type == FOREACH_VAR_DECL) {
        return new KlangForeachVarDeclImpl(node);
      }
      else if (type == FOR_STATEMENT) {
        return new KlangForStatementImpl(node);
      }
      else if (type == FRIEND_DECL) {
        return new KlangFriendDeclImpl(node);
      }
      else if (type == FRIEND_FILTER) {
        return new KlangFriendFilterImpl(node);
      }
      else if (type == FUNCTION_BODY) {
        return new KlangFunctionBodyImpl(node);
      }
      else if (type == FUNCTION_DECL) {
        return new KlangFunctionDeclImpl(node);
      }
      else if (type == FUNCTION_HEAD) {
        return new KlangFunctionHeadImpl(node);
      }
      else if (type == FUNCTION_REF_QUALIFIER) {
        return new KlangFunctionRefQualifierImpl(node);
      }
      else if (type == FUNCTION_REF_TYPE) {
        return new KlangFunctionRefTypeImpl(node);
      }
      else if (type == FUNDAMENTAL_TYPE_SPEC) {
        return new KlangFundamentalTypeSpecImpl(node);
      }
      else if (type == GENERIC_DECLARATION) {
        return new KlangGenericDeclarationImpl(node);
      }
      else if (type == IDENTIFIER_EXPR) {
        return new KlangIdentifierExprImpl(node);
      }
      else if (type == IDENTIFIER_SEGMENT) {
        return new KlangIdentifierSegmentImpl(node);
      }
      else if (type == IF_COND_VAR_DECL) {
        return new KlangIfCondVarDeclImpl(node);
      }
      else if (type == IF_COND_VAR_DECL_LIST) {
        return new KlangIfCondVarDeclListImpl(node);
      }
      else if (type == IF_ELSE_STATEMENT) {
        return new KlangIfElseStatementImpl(node);
      }
      else if (type == IMPORT_DECLARATION) {
        return new KlangImportDeclarationImpl(node);
      }
      else if (type == INCLUSIVE_BIN_OR_EXPR) {
        return new KlangInclusiveBinOrExprImpl(node);
      }
      else if (type == INITIALISER) {
        return new KlangInitialiserImpl(node);
      }
      else if (type == INIT_ELEMENT) {
        return new KlangInitElementImpl(node);
      }
      else if (type == LITERAL) {
        return new KlangLiteralImpl(node);
      }
      else if (type == LOGICAL_AND_EXPR) {
        return new KlangLogicalAndExprImpl(node);
      }
      else if (type == LOGICAL_OR_EXPR) {
        return new KlangLogicalOrExprImpl(node);
      }
      else if (type == MEMBER_INIT) {
        return new KlangMemberInitImpl(node);
      }
      else if (type == MEMBER_INIT_LIST) {
        return new KlangMemberInitListImpl(node);
      }
      else if (type == MODULE_DECLARATION) {
        return new KlangModuleDeclarationImpl(node);
      }
      else if (type == MULTIPLICATIVE_EXPR) {
        return new KlangMultiplicativeExprImpl(node);
      }
      else if (type == NAMED_RETURN_INIT) {
        return new KlangNamedReturnInitImpl(node);
      }
      else if (type == NAMED_RETURN_VAR) {
        return new KlangNamedReturnVarImpl(node);
      }
      else if (type == NAMESPACE_DECL) {
        return new KlangNamespaceDeclImpl(node);
      }
      else if (type == NEW_EXPR) {
        return new KlangNewExprImpl(node);
      }
      else if (type == OPERATOR_FUNCTION_HEAD) {
        return new KlangOperatorFunctionHeadImpl(node);
      }
      else if (type == OPERATOR_SYMBOL) {
        return new KlangOperatorSymbolImpl(node);
      }
      else if (type == PARAMETER_LIST) {
        return new KlangParameterListImpl(node);
      }
      else if (type == PARAMETER_SPEC) {
        return new KlangParameterSpecImpl(node);
      }
      else if (type == PM_EXPR) {
        return new KlangPmExprImpl(node);
      }
      else if (type == POSTFIX_EXPR) {
        return new KlangPostfixExprImpl(node);
      }
      else if (type == POSTFIX_OP) {
        return new KlangPostfixOpImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new KlangPrimaryExprImpl(node);
      }
      else if (type == PRIMITIVE_ARRAY_ELEMENT_TYPE) {
        return new KlangPrimitiveArrayElementTypeImpl(node);
      }
      else if (type == QUALIFIED_IDENTIFIER) {
        return new KlangQualifiedIdentifierImpl(node);
      }
      else if (type == RELATIONAL_EXPR) {
        return new KlangRelationalExprImpl(node);
      }
      else if (type == RETURN_STATEMENT) {
        return new KlangReturnStatementImpl(node);
      }
      else if (type == RETURN_TYPE_OR_MEMBER_INIT_LIST) {
        return new KlangReturnTypeOrMemberInitListImpl(node);
      }
      else if (type == SHIFTING_EXPR) {
        return new KlangShiftingExprImpl(node);
      }
      else if (type == SPACESHIP_EXPR) {
        return new KlangSpaceshipExprImpl(node);
      }
      else if (type == SPECIFIER) {
        return new KlangSpecifierImpl(node);
      }
      else if (type == STATEMENT) {
        return new KlangStatementImpl(node);
      }
      else if (type == STATIC_DEP) {
        return new KlangStaticDepImpl(node);
      }
      else if (type == STATIC_DEP_LIST) {
        return new KlangStaticDepListImpl(node);
      }
      else if (type == TEMPLATE_ARG) {
        return new KlangTemplateArgImpl(node);
      }
      else if (type == TEMPLATE_ARG_LIST) {
        return new KlangTemplateArgListImpl(node);
      }
      else if (type == TEMPLATE_DECLARATION) {
        return new KlangTemplateDeclarationImpl(node);
      }
      else if (type == TEMPLATE_PARAMETER) {
        return new KlangTemplateParameterImpl(node);
      }
      else if (type == TEMPLATE_PARAMETER_KIND) {
        return new KlangTemplateParameterKindImpl(node);
      }
      else if (type == TEMPLATE_PARAMETER_LIST) {
        return new KlangTemplateParameterListImpl(node);
      }
      else if (type == TEMPLATE_QUALIFIED_SCOPE_EXPR) {
        return new KlangTemplateQualifiedScopeExprImpl(node);
      }
      else if (type == THROWS_CLAUSE) {
        return new KlangThrowsClauseImpl(node);
      }
      else if (type == THROW_STATEMENT) {
        return new KlangThrowStatementImpl(node);
      }
      else if (type == TRY_CATCH_STATEMENT) {
        return new KlangTryCatchStatementImpl(node);
      }
      else if (type == TYPE_LIST) {
        return new KlangTypeListImpl(node);
      }
      else if (type == TYPE_NAME) {
        return new KlangTypeNameImpl(node);
      }
      else if (type == TYPE_SPEC) {
        return new KlangTypeSpecImpl(node);
      }
      else if (type == TYPE_SPEC_LIST) {
        return new KlangTypeSpecListImpl(node);
      }
      else if (type == TYPE_SUFFIX) {
        return new KlangTypeSuffixImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new KlangUnaryExprImpl(node);
      }
      else if (type == UNION_DECL) {
        return new KlangUnionDeclImpl(node);
      }
      else if (type == UNION_MEMBER_DECL) {
        return new KlangUnionMemberDeclImpl(node);
      }
      else if (type == USING_DECL) {
        return new KlangUsingDeclImpl(node);
      }
      else if (type == USING_FILTER) {
        return new KlangUsingFilterImpl(node);
      }
      else if (type == VARIABLE_DECL) {
        return new KlangVariableDeclImpl(node);
      }
      else if (type == VISIBILITY_DECL) {
        return new KlangVisibilityDeclImpl(node);
      }
      else if (type == WHILE_STATEMENT) {
        return new KlangWhileStatementImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
