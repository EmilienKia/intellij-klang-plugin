package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.usageView.UsageViewLongNameLocation;
import com.intellij.usageView.UsageViewShortNameLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides human-readable element descriptions for K-lang declarations, used by refactorings
 * and Find Usages. Without it, IntelliJ falls back to the PSI class name, producing cryptic
 * labels such as <em>"Rename Klang Function Decl Impl 'distance' …"</em>; with it the label
 * reads <em>"Rename function 'distance' …"</em>.
 */
public class KlangElementDescriptionProvider implements ElementDescriptionProvider {

    @Override
    public @Nullable String getElementDescription(@NotNull PsiElement element,
                                                  @NotNull ElementDescriptionLocation location) {
        if (location instanceof UsageViewTypeLocation) {
            return typeName(element);
        }
        if (location instanceof UsageViewShortNameLocation) {
            return element instanceof PsiNamedElement named ? named.getName() : null;
        }
        if (location instanceof UsageViewLongNameLocation) {
            if (element instanceof KlangNamedElement) {
                return KlangNavigationPresentation.elementText(element);
            }
            return element instanceof PsiNamedElement named ? named.getName() : null;
        }
        return null;
    }

    /** A friendly, lower-case type noun for a K-lang declaration (or {@code null} if unknown). */
    private static @Nullable String typeName(@NotNull PsiElement element) {
        if (element instanceof KlangAggregateDecl agg)        return aggregateKind(agg);
        if (element instanceof KlangFunctionDecl)             return "function";
        if (element instanceof KlangEnumDecl)                 return "enum";
        if (element instanceof KlangUnionDecl)                return "union";
        if (element instanceof KlangNamespaceDecl)            return "namespace";
        if (element instanceof KlangEnumEntry)                return "enum entry";
        if (element instanceof KlangUnionMemberDecl)          return "union member";
        if (element instanceof KlangParameterSpec)            return "parameter";
        if (element instanceof KlangNamedReturnVar)           return "return variable";
        if (element instanceof KlangTemplateParameter)        return "type parameter";
        if (element instanceof KlangIfCondVarDecl)            return "variable";
        if (element instanceof KlangCatchParameterDecl)       return "catch parameter";
        if (element instanceof KlangVariableDecl)             return "variable";
        return null;
    }

    /** Distinguishes struct / class / interface / annotation by the aggregate's keyword. */
    private static String aggregateKind(@NotNull KlangAggregateDecl agg) {
        var node = agg.getNode();
        if (node.findChildByType(KlangTypes.KW_CLASS) != null)      return "class";
        if (node.findChildByType(KlangTypes.KW_INTERFACE) != null)  return "interface";
        if (node.findChildByType(KlangTypes.KW_ANNOTATION) != null) return "annotation";
        return "struct";
    }
}

