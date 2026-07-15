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

public class KlangFunctionDeclImpl extends KlangFunctionDeclMixin implements KlangFunctionDecl {

  public KlangFunctionDeclImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitFunctionDecl(this);
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
  public KlangCastOperatorFunctionHead getCastOperatorFunctionHead() {
    return findChildByClass(KlangCastOperatorFunctionHead.class);
  }

  @Override
  @Nullable
  public KlangDestructorHead getDestructorHead() {
    return findChildByClass(KlangDestructorHead.class);
  }

  @Override
  @NotNull
  public KlangFunctionBody getFunctionBody() {
    return findNotNullChildByClass(KlangFunctionBody.class);
  }

  @Override
  @Nullable
  public KlangFunctionHead getFunctionHead() {
    return findChildByClass(KlangFunctionHead.class);
  }

  @Override
  @Nullable
  public KlangGenericDeclaration getGenericDeclaration() {
    return findChildByClass(KlangGenericDeclaration.class);
  }

  @Override
  @Nullable
  public KlangNamedReturnVar getNamedReturnVar() {
    return findChildByClass(KlangNamedReturnVar.class);
  }

  @Override
  @Nullable
  public KlangOperatorFunctionHead getOperatorFunctionHead() {
    return findChildByClass(KlangOperatorFunctionHead.class);
  }

  @Override
  @Nullable
  public KlangParameterList getParameterList() {
    return findChildByClass(KlangParameterList.class);
  }

  @Override
  @Nullable
  public KlangReturnTypeOrMemberInitList getReturnTypeOrMemberInitList() {
    return findChildByClass(KlangReturnTypeOrMemberInitList.class);
  }

  @Override
  @NotNull
  public List<KlangSpecifier> getSpecifierList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, KlangSpecifier.class);
  }

  @Override
  @Nullable
  public KlangTemplateDeclaration getTemplateDeclaration() {
    return findChildByClass(KlangTemplateDeclaration.class);
  }

  @Override
  @Nullable
  public KlangThrowsClause getThrowsClause() {
    return findChildByClass(KlangThrowsClause.class);
  }

  @Override
  @Nullable
  public KlangTypeSpec getTypeSpec() {
    return findChildByClass(KlangTypeSpec.class);
  }

}
