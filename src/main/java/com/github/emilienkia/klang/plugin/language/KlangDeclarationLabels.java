package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared, presentation-agnostic label helpers for K-lang declarations.
 *
 * <p>Extracted from {@link KlangBreadcrumbsProvider} so the breadcrumbs and the
 * structure view derive their labels from a single source of truth (function /
 * operator / destructor / cast-operator naming, aggregate keyword detection).</p>
 */
public final class KlangDeclarationLabels {

    private KlangDeclarationLabels() {
    }

    /**
     * Builds a short label for a function declaration: {@code name(…)} for a regular
     * function, {@code operator==(…)} / {@code operator[](…)} for an operator overload,
     * {@code ~Name(…)} for a destructor, and {@code operator ()} for a cast operator.
     */
    public static @NotNull String functionLabel(@NotNull PsiElement functionDecl) {
        var node = functionDecl.getNode();

        // Destructor: ~Name
        var destructor = node.findChildByType(KlangTypes.DESTRUCTOR_HEAD);
        if (destructor != null) return destructor.getText() + "(…)";

        // Operator overload
        var opHead = node.findChildByType(KlangTypes.OPERATOR_FUNCTION_HEAD);
        if (opHead != null) return opHead.getText() + "(…)";

        // Cast / conversion operator: 'operator ()' — the '()' is the operator symbol,
        // so there is no parameter list to elide.
        var castHead = node.findChildByType(KlangTypes.CAST_OPERATOR_FUNCTION_HEAD);
        if (castHead != null) return castHead.getText();

        // Regular function: FUNCTION_HEAD > IDENTIFIER
        var head = node.findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head != null) {
            var id = head.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return id.getText() + "(…)";
        }

        return "<function>";
    }

    /** Returns the aggregate keyword (struct / class / interface / annotation), or {@code null}. */
    public static @Nullable String aggregateKeyword(@NotNull PsiElement aggregateDecl) {
        var node = aggregateDecl.getNode().getFirstChildNode();
        while (node != null) {
            IElementType t = node.getElementType();
            if (t == KlangTypes.KW_STRUCT)      return "struct";
            if (t == KlangTypes.KW_CLASS)       return "class";
            if (t == KlangTypes.KW_INTERFACE)   return "interface";
            if (t == KlangTypes.KW_ANNOTATION)  return "annotation";
            node = node.getTreeNext();
        }
        return null;
    }

    /**
     * Returns the text of the first direct child with the given element type,
     * or {@code null} if no such child exists.
     */
    public static @Nullable String childText(@NotNull PsiElement element, @NotNull IElementType childType) {
        var child = element.getNode().findChildByType(childType);
        return child != null ? child.getText() : null;
    }
}

