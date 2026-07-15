// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.emilienkia.klang.plugin.language.psi.KlangTypes.*;
import com.github.emilienkia.klang.plugin.language.psi.*;

public class KlangTemplateParameterImpl extends KlangNamedDeclMixin implements KlangTemplateParameter {

  public KlangTemplateParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitTemplateParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public KlangConditionalExpr getConditionalExpr() {
    return findChildByClass(KlangConditionalExpr.class);
  }

  @Override
  @NotNull
  public KlangTemplateParameterKind getTemplateParameterKind() {
    return findNotNullChildByClass(KlangTemplateParameterKind.class);
  }

  @Override
  @Nullable
  public KlangTypeSpec getTypeSpec() {
    return findChildByClass(KlangTypeSpec.class);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

}
