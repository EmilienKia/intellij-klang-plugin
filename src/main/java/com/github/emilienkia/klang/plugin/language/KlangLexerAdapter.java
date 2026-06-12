package com.github.emilienkia.klang.plugin.language;

import com.intellij.lexer.FlexAdapter;

public class KlangLexerAdapter extends FlexAdapter {
    public KlangLexerAdapter() {
        super(new KlangLexer(null));
    }
}
