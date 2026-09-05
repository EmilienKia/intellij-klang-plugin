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

public class KlangCallableTypeSpecImpl extends ASTWrapperPsiElement implements KlangCallableTypeSpec {

  public KlangCallableTypeSpecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitCallableTypeSpec(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public KlangCallableAddresser getCallableAddresser() {
    return findChildByClass(KlangCallableAddresser.class);
  }

  @Override
  @Nullable
  public KlangThrowsClause getThrowsClause() {
    return findChildByClass(KlangThrowsClause.class);
  }

  @Override
  @Nullable
  public KlangTypeList getTypeList() {
    return findChildByClass(KlangTypeList.class);
  }

  @Override
  @Nullable
  public KlangTypeSpec getTypeSpec() {
    return findChildByClass(KlangTypeSpec.class);
  }

}
