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

public class KlangDeclarationImpl extends ASTWrapperPsiElement implements KlangDeclaration {

  public KlangDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull KlangVisitor visitor) {
    visitor.visitDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KlangVisitor) accept((KlangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public KlangAggregateDecl getAggregateDecl() {
    return findChildByClass(KlangAggregateDecl.class);
  }

  @Override
  @Nullable
  public KlangEnumDecl getEnumDecl() {
    return findChildByClass(KlangEnumDecl.class);
  }

  @Override
  @Nullable
  public KlangFriendDecl getFriendDecl() {
    return findChildByClass(KlangFriendDecl.class);
  }

  @Override
  @Nullable
  public KlangFunctionDecl getFunctionDecl() {
    return findChildByClass(KlangFunctionDecl.class);
  }

  @Override
  @Nullable
  public KlangNamespaceDecl getNamespaceDecl() {
    return findChildByClass(KlangNamespaceDecl.class);
  }

  @Override
  @Nullable
  public KlangUnionDecl getUnionDecl() {
    return findChildByClass(KlangUnionDecl.class);
  }

  @Override
  @Nullable
  public KlangUsingDecl getUsingDecl() {
    return findChildByClass(KlangUsingDecl.class);
  }

  @Override
  @Nullable
  public KlangVariableDecl getVariableDecl() {
    return findChildByClass(KlangVariableDecl.class);
  }

  @Override
  @Nullable
  public KlangVisibilityDecl getVisibilityDecl() {
    return findChildByClass(KlangVisibilityDecl.class);
  }

}
