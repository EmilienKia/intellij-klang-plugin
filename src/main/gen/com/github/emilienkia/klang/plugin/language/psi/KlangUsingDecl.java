// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangUsingDecl extends PsiElement {

  @Nullable
  KlangQualifiedIdentifier getQualifiedIdentifier();

  @Nullable
  KlangUsingFilter getUsingFilter();

  @Nullable
  PsiElement getIdentifier();

}
