// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangUnionDecl extends KlangNamedElement {

  @NotNull
  List<KlangAnnotationDef> getAnnotationDefList();

  @Nullable
  KlangQualifiedIdentifier getQualifiedIdentifier();

  @NotNull
  List<KlangSpecifier> getSpecifierList();

  @NotNull
  List<KlangUnionMemberDecl> getUnionMemberDeclList();

  @NotNull
  PsiElement getIdentifier();

}
