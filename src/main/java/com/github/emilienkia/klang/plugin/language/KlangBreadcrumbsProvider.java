package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

/**
 * Provides breadcrumb items for K-lang files.
 * Shows the chain of enclosing namespaces, structs/classes/interfaces, and functions.
 *
 * <p>Example: {@code demo :: Point :: distance}</p>
 */
public class KlangBreadcrumbsProvider implements BreadcrumbsProvider {

    /** PSI composite node types that constitute a breadcrumb level. */
    private static final Set<IElementType> CRUMB_TYPES = Set.of(
            KlangTypes.NAMESPACE_DECL,
            KlangTypes.AGGREGATE_DECL,
            KlangTypes.FUNCTION_DECL,
            KlangTypes.ENUM_DECL,
            KlangTypes.UNION_DECL
    );

    // ── BreadcrumbsProvider ──────────────────────────────────────────────────

    @Override
    public Language[] getLanguages() {
        return new Language[]{ KlangLanguage.INSTANCE };
    }

    @Override
    public boolean acceptElement(@NotNull PsiElement element) {
        return CRUMB_TYPES.contains(element.getNode().getElementType());
    }

    @Override
    public @NotNull String getElementInfo(@NotNull PsiElement element) {
        return labelFor(element);
    }

    @Override
    public @Nullable String getElementTooltip(@NotNull PsiElement element) {
        return tooltipFor(element);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String labelFor(PsiElement element) {
        IElementType type = element.getNode().getElementType();

        if (type == KlangTypes.NAMESPACE_DECL) {
            String name = childText(element, KlangTypes.IDENTIFIER);
            return name != null ? name : "<namespace>";
        }

        if (type == KlangTypes.AGGREGATE_DECL) {
            // keyword (struct/class/interface/annotation) + name
            String kw   = aggregateKeyword(element);
            String name = childText(element, KlangTypes.IDENTIFIER);
            return (kw != null ? kw + " " : "") + (name != null ? name : "<type>");
        }

        if (type == KlangTypes.ENUM_DECL) {
            String name = childText(element, KlangTypes.IDENTIFIER);
            return "enum " + (name != null ? name : "<enum>");
        }

        if (type == KlangTypes.UNION_DECL) {
            String name = childText(element, KlangTypes.IDENTIFIER);
            return "union " + (name != null ? name : "<union>");
        }

        if (type == KlangTypes.FUNCTION_DECL) {
            return functionLabel(element);
        }

        return element.getText();
    }

    private static String tooltipFor(PsiElement element) {
        IElementType type = element.getNode().getElementType();
        if (type == KlangTypes.FUNCTION_DECL) {
            // Full signature: first line of the declaration text (before the body)
            String text = element.getText();
            int brace = text.indexOf('{');
            String sig = (brace > 0 ? text.substring(0, brace) : text).trim();
            // Trim to a reasonable length
            return sig.length() > 120 ? sig.substring(0, 117) + "…" : sig;
        }
        return labelFor(element);
    }

    /** Returns the aggregate keyword (struct / class / interface / annotation). */
    private static @Nullable String aggregateKeyword(PsiElement element) {
        var node = element.getNode().getFirstChildNode();
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

    /** Builds a short label for a function: name + (…) */
    private static String functionLabel(PsiElement element) {
        var node = element.getNode();

        // Destructor: ~Name
        var destructor = node.findChildByType(KlangTypes.DESTRUCTOR_HEAD);
        if (destructor != null) return destructor.getText() + "(…)";

        // Operator overload
        var opHead = node.findChildByType(KlangTypes.OPERATOR_FUNCTION_HEAD);
        if (opHead != null) return opHead.getText() + "(…)";

        // Regular function: FUNCTION_HEAD > IDENTIFIER
        var head = node.findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head != null) {
            var id = head.findChildByType(KlangTypes.IDENTIFIER);
            if (id != null) return id.getText() + "(…)";
        }

        return "<function>";
    }

    /**
     * Returns the text of the first direct child with the given element type,
     * or {@code null} if no such child exists.
     */
    private static @Nullable String childText(PsiElement element, IElementType childType) {
        var child = element.getNode().findChildByType(childType);
        return child != null ? child.getText() : null;
    }
}

