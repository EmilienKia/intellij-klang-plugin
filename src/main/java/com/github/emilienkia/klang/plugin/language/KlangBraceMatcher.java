package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KlangBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = {
            new BracePair(KlangTypes.PUNC_LPAREN,   KlangTypes.PUNC_RPAREN,   false),
            new BracePair(KlangTypes.PUNC_LBRACE,   KlangTypes.PUNC_RBRACE,   true),
            new BracePair(KlangTypes.PUNC_LBRACKET, KlangTypes.PUNC_RBRACKET, false),
            new BracePair(KlangTypes.OP_LT,         KlangTypes.OP_GT,         false),
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType,
                                                   @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
