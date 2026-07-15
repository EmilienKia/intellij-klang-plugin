// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangTemplateParameter extends KlangNamedElement {

  @Nullable
  KlangConditionalExpr getConditionalExpr();

  @NotNull
  KlangTemplateParameterKind getTemplateParameterKind();

  @Nullable
  KlangTypeSpec getTypeSpec();

  @NotNull
  PsiElement getIdentifier();

}
