package com.github.emilienkia.klang.plugin.language.refactoring.changesignature;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Plain data holder describing the user's requested edits for a K-lang
 * "Change Signature" refactoring: new name, new return-type text, new
 * (raw-text) parameter list, new specifier set, plus propagation choices.
 */
public final class KlangChangeSignatureModel {

    /** Canonical order specifiers are re-emitted in, mirroring klang.bnf's {@code specifier} rule. */
    public static final String[] SPECIFIER_ORDER = {
            "public", "protected", "private", "static", "const", "abstract", "final", "override", "default"
    };

    private final String name;
    private final String returnType;
    private final List<String> parameters;
    private final List<String> specifiers;
    private final boolean propagateDown;
    private final boolean propagateUp;

    public KlangChangeSignatureModel(@NotNull String name,
                                      @NotNull String returnType,
                                      @NotNull List<String> parameters,
                                      @NotNull List<String> specifiers,
                                      boolean propagateDown,
                                      boolean propagateUp) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.specifiers = specifiers;
        this.propagateDown = propagateDown;
        this.propagateUp = propagateUp;
    }

    public @NotNull String name() { return name; }
    public @NotNull String returnType() { return returnType; }
    public @NotNull List<String> parameters() { return parameters; }
    public @NotNull List<String> specifiers() { return specifiers; }
    public boolean propagateDown() { return propagateDown; }
    public boolean propagateUp() { return propagateUp; }
}

