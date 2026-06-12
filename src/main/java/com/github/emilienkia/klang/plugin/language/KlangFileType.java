package com.github.emilienkia.klang.plugin.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class KlangFileType extends LanguageFileType {

    public static final KlangFileType INSTANCE = new KlangFileType();

    private KlangFileType() {
        super(KlangLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "K-lang source file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "K language source file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "k";
    }

    @Override
    public Icon getIcon() {
        return KlangIcons.FILE;
    }

}
