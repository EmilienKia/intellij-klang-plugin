// This is a generated file. Not intended for manual editing.
package com.github.emilienkia.klang.plugin.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface KlangReturnTypeOrMemberInitList extends PsiElement {

  @Nullable
  KlangMemberInitList getMemberInitList();

  @Nullable
  KlangStaticDepList getStaticDepList();

  @Nullable
  KlangTypeSpec getTypeSpec();

}
