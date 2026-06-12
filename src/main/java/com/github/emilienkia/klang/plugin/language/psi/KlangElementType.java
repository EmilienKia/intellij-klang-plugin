package com.github.emilienkia.klang.plugin.language.psi;

import com.github.emilienkia.klang.plugin.language.KlangLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class KlangElementType extends IElementType {
    public KlangElementType(@NotNull @NonNls String debugName) {
        super(debugName, KlangLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "Element '" + super.toString() + "'";
    }
}
