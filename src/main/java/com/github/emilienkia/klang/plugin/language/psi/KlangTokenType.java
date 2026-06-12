package com.github.emilienkia.klang.plugin.language.psi;

import com.github.emilienkia.klang.plugin.language.KlangLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class KlangTokenType extends IElementType {
    public KlangTokenType(@NotNull @NonNls String debugName) {
        super(debugName, KlangLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "token '" + super.toString() + "'";
    }
}
