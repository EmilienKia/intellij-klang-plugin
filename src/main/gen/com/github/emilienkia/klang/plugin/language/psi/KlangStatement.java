// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangStatement extends PsiElement {

  @Nullable
  KlangBlockStatement getBlockStatement();

  @Nullable
  KlangBreakStatement getBreakStatement();

  @Nullable
  KlangContinueStatement getContinueStatement();

  @Nullable
  KlangExpressionStatement getExpressionStatement();

  @Nullable
  KlangForStatement getForStatement();

  @Nullable
  KlangForeachStatement getForeachStatement();

  @Nullable
  KlangIfElseStatement getIfElseStatement();

  @Nullable
  KlangReturnStatement getReturnStatement();

  @Nullable
  KlangThrowStatement getThrowStatement();

  @Nullable
  KlangTryCatchStatement getTryCatchStatement();

  @Nullable
  KlangUsingDecl getUsingDecl();

  @Nullable
  KlangVariableDecl getVariableDecl();

  @Nullable
  KlangWhileStatement getWhileStatement();

}
