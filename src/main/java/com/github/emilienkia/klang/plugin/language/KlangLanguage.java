package com.github.emilienkia.klang.plugin.language;

import com.intellij.lang.Language;

public class KlangLanguage extends Language {
    public static final KlangLanguage INSTANCE = new KlangLanguage();

    private KlangLanguage() {
        super("K-lang");
    }
}
