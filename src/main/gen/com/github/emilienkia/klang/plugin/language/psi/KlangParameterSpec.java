// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangParameterSpec extends KlangNamedElement {

  @NotNull
  List<KlangAnnotationDef> getAnnotationDefList();

  @Nullable
  KlangConditionalExpr getConditionalExpr();

  @NotNull
  List<KlangSpecifier> getSpecifierList();

  @NotNull
  KlangTypeSpec getTypeSpec();

}
