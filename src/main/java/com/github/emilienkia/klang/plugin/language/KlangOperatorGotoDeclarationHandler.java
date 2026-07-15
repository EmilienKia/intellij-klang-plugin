package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.impl.KlangOperatorUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Go-to-Declaration for <strong>operator usages</strong>: pressing Ctrl/Cmd-Click (or
 * <em>Navigate → Declaration</em>) on an operator token in an expression (e.g. the {@code ==} in
 * {@code a == b}, the {@code -} in {@code -a}, or the {@code [}/{@code ]} in {@code a[i]}) jumps to
 * the matching {@code operator} overload definition.
 *
 * <p>All the logic lives in {@link KlangOperatorUtil#resolveOperatorUsage}, shared with the reverse
 * usage gutter marker so the two navigation directions stay exact inverses. The operand's aggregate
 * type is inferred, then the overload is resolved against it — member operators (incl. inherited)
 * first, then visible free operator functions. Operator tokens that belong to an {@code operator}
 * <em>declaration</em> are ignored, and unresolved cases fail soft (return {@code null}), so
 * built-in operators on primitives are unaffected.</p>
 */
public class KlangOperatorGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement source,
                                                             int offset,
                                                             Editor editor) {
        List<PsiElement> targets = KlangOperatorUtil.resolveOperatorUsage(source);
        return targets.isEmpty() ? null : targets.toArray(PsiElement.EMPTY_ARRAY);
    }
}

