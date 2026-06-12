// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangPostfixOp extends PsiElement {

  @Nullable
  KlangBraceInitList getBraceInitList();

  @Nullable
  KlangExpression getExpression();

  @Nullable
  KlangExpressionList getExpressionList();

  @Nullable
  KlangIdentifierExpr getIdentifierExpr();

}
