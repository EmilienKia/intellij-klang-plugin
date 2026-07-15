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

public class KlangEnumDeclImpl extends KlangNamedDeclMixin implements KlangEnumDecl {

  public KlangEnumDeclImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitEnumDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<KlangEnumEntry> getEnumEntryList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, KlangEnumEntry.class);
  }

  @Override
  @NotNull
  public List<KlangSpecifier> getSpecifierList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, KlangSpecifier.class);
  }

  @Override
  @Nullable
  public KlangTypeSpec getTypeSpec() {
    return findChildByClass(KlangTypeSpec.class);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

}
