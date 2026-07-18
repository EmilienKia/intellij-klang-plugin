// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.emilienkia.klang.plugin.language.psi.KlangTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.github.emilienkia.klang.plugin.language.psi.*;

public class KlangStatementImpl extends ASTWrapperPsiElement implements KlangStatement {

  public KlangStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public KlangBlockStatement getBlockStatement() {
    return findChildByClass(KlangBlockStatement.class);
  }

  @Override
  @Nullable
  public KlangBreakStatement getBreakStatement() {
    return findChildByClass(KlangBreakStatement.class);
  }

  @Override
  @Nullable
  public KlangContinueStatement getContinueStatement() {
    return findChildByClass(KlangContinueStatement.class);
  }

  @Override
  @Nullable
  public KlangExpressionStatement getExpressionStatement() {
    return findChildByClass(KlangExpressionStatement.class);
  }

  @Override
  @Nullable
  public KlangForStatement getForStatement() {
    return findChildByClass(KlangForStatement.class);
  }

  @Override
  @Nullable
  public KlangForeachStatement getForeachStatement() {
    return findChildByClass(KlangForeachStatement.class);
  }

  @Override
  @Nullable
  public KlangIfElseStatement getIfElseStatement() {
    return findChildByClass(KlangIfElseStatement.class);
  }

  @Override
  @Nullable
  public KlangReturnStatement getReturnStatement() {
    return findChildByClass(KlangReturnStatement.class);
  }

  @Override
  @Nullable
  public KlangThrowStatement getThrowStatement() {
    return findChildByClass(KlangThrowStatement.class);
  }

  @Override
  @Nullable
  public KlangTryCatchStatement getTryCatchStatement() {
    return findChildByClass(KlangTryCatchStatement.class);
  }

  @Override
  @Nullable
  public KlangUsingDecl getUsingDecl() {
    return findChildByClass(KlangUsingDecl.class);
  }

  @Override
  @Nullable
  public KlangVariableDecl getVariableDecl() {
    return findChildByClass(KlangVariableDecl.class);
  }

  @Override
  @Nullable
  public KlangWhileStatement getWhileStatement() {
    return findChildByClass(KlangWhileStatement.class);
  }

}
