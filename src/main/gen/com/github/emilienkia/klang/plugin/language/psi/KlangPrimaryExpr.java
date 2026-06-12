// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangPrimaryExpr extends PsiElement {

  @Nullable
  KlangAnnotationDef getAnnotationDef();

  @Nullable
  KlangBraceInitList getBraceInitList();

  @Nullable
  KlangExpression getExpression();

  @Nullable
  KlangIdentifierExpr getIdentifierExpr();

  @Nullable
  KlangLiteral getLiteral();

}
