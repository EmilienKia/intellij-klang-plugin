// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangFunctionDecl extends KlangNamedElement {

  @NotNull
  List<KlangAnnotationDef> getAnnotationDefList();

  @Nullable
  KlangCastOperatorFunctionHead getCastOperatorFunctionHead();

  @Nullable
  KlangDestructorHead getDestructorHead();

  @NotNull
  KlangFunctionBody getFunctionBody();

  @Nullable
  KlangFunctionHead getFunctionHead();

  @Nullable
  KlangGenericDeclaration getGenericDeclaration();

  @Nullable
  KlangNamedReturnVar getNamedReturnVar();

  @Nullable
  KlangOperatorFunctionHead getOperatorFunctionHead();

  @Nullable
  KlangParameterList getParameterList();

  @Nullable
  KlangReturnTypeOrMemberInitList getReturnTypeOrMemberInitList();

  @NotNull
  List<KlangSpecifier> getSpecifierList();

  @Nullable
  KlangTemplateDeclaration getTemplateDeclaration();

  @Nullable
  KlangThrowsClause getThrowsClause();

  @Nullable
  KlangTypeSpec getTypeSpec();

}
