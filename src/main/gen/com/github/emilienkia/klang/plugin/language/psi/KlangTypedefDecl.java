// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangTypedefDecl extends KlangNamedElement {

  @Nullable
  KlangGenericDeclaration getGenericDeclaration();

  @NotNull
  List<KlangSpecifier> getSpecifierList();

  @Nullable
  KlangTemplateDeclaration getTemplateDeclaration();

  @Nullable
  KlangTypeSpec getTypeSpec();

  @Nullable
  PsiElement getIdentifier();

}
