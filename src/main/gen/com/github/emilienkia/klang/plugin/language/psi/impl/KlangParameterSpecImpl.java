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

public class KlangParameterSpecImpl extends ASTWrapperPsiElement implements KlangParameterSpec {

  public KlangParameterSpecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitParameterSpec(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<KlangAnnotationDef> getAnnotationDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, KlangAnnotationDef.class);
  }

  @Override
  @Nullable
  public KlangConditionalExpr getConditionalExpr() {
    return findChildByClass(KlangConditionalExpr.class);
  }

  @Override
  @NotNull
  public List<KlangSpecifier> getSpecifierList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, KlangSpecifier.class);
  }

  @Override
  @NotNull
  public KlangTypeSpec getTypeSpec() {
    return findNotNullChildByClass(KlangTypeSpec.class);
  }

}
