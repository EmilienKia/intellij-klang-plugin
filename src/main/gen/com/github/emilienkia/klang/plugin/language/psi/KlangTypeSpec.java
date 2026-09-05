// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangTypeSpec extends PsiElement {

  @Nullable
  KlangCallableTypeSpec getCallableTypeSpec();

  @Nullable
  KlangFundamentalTypeSpec getFundamentalTypeSpec();

  @Nullable
  KlangMemberFnRefType getMemberFnRefType();

  @Nullable
  KlangQualifiedIdentifier getQualifiedIdentifier();

  @NotNull
  List<KlangTypeSuffix> getTypeSuffixList();

}
