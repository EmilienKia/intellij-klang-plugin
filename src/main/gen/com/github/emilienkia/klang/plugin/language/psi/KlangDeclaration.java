// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangDeclaration extends PsiElement {

  @Nullable
  KlangAggregateDecl getAggregateDecl();

  @Nullable
  KlangEnumDecl getEnumDecl();

  @Nullable
  KlangFriendDecl getFriendDecl();

  @Nullable
  KlangFunctionDecl getFunctionDecl();

  @Nullable
  KlangNamespaceDecl getNamespaceDecl();

  @Nullable
  KlangUnionDecl getUnionDecl();

  @Nullable
  KlangUsingDecl getUsingDecl();

  @Nullable
  KlangVariableDecl getVariableDecl();

  @Nullable
  KlangVisibilityDecl getVisibilityDecl();

}
