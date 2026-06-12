package com.github.emilienkia.klang.plugin.language.psi;

import com.github.emilienkia.klang.plugin.language.KlangFileType;
import com.github.emilienkia.klang.plugin.language.KlangLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class KlangFile extends PsiFileBase {

    public KlangFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, KlangLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return KlangFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Klang File";
    }

}
