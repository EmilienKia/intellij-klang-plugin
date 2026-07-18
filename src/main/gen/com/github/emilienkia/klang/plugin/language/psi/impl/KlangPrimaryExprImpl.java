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

public class KlangPrimaryExprImpl extends ASTWrapperPsiElement implements KlangPrimaryExpr {

  public KlangPrimaryExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitPrimaryExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public KlangAnnotationDef getAnnotationDef() {
    return findChildByClass(KlangAnnotationDef.class);
  }

  @Override
  @Nullable
  public KlangBraceInitList getBraceInitList() {
    return findChildByClass(KlangBraceInitList.class);
  }

  @Override
  @Nullable
  public KlangExpression getExpression() {
    return findChildByClass(KlangExpression.class);
  }

  @Override
  @Nullable
  public KlangIdentifierExpr getIdentifierExpr() {
    return findChildByClass(KlangIdentifierExpr.class);
  }

  @Override
  @Nullable
  public KlangLiteral getLiteral() {
    return findChildByClass(KlangLiteral.class);
  }

  @Override
  @Nullable
  public KlangPrimitiveArrayElementType getPrimitiveArrayElementType() {
    return findChildByClass(KlangPrimitiveArrayElementType.class);
  }

}
