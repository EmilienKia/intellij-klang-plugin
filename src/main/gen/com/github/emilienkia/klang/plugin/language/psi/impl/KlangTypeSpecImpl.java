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

public class KlangTypeSpecImpl extends ASTWrapperPsiElement implements KlangTypeSpec {

  public KlangTypeSpecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitTypeSpec(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public KlangFunctionRefType getFunctionRefType() {
    return findChildByClass(KlangFunctionRefType.class);
  }

  @Override
  @Nullable
  public KlangFundamentalTypeSpec getFundamentalTypeSpec() {
    return findChildByClass(KlangFundamentalTypeSpec.class);
  }

  @Override
  @Nullable
  public KlangQualifiedIdentifier getQualifiedIdentifier() {
    return findChildByClass(KlangQualifiedIdentifier.class);
  }

  @Override
  @NotNull
  public List<KlangTypeSuffix> getTypeSuffixList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, KlangTypeSuffix.class);
  }

}
