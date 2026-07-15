// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangEnumDecl extends KlangNamedElement {

  @NotNull
  List<KlangEnumEntry> getEnumEntryList();

  @NotNull
  List<KlangSpecifier> getSpecifierList();

  @Nullable
  KlangTypeSpec getTypeSpec();

  @Nullable
  PsiElement getIdentifier();

}
