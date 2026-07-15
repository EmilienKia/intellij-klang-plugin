package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangVisitor;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Flags a concrete {@code class}/{@code struct}/{@code annotation} that does not implement every
 * abstract method it inherits (transitively — inheritance is always virtual in K, so there is a
 * single logical slot per method name, see {@link KlangResolveUtil#collectAbstractSlots}) from its
 * base {@code interface}s / {@code abstract} classes.
 *
 * <p>Design: {@code docs/klang/abstract-implementation-plan.md}.</p>
 *
 * <p>Skipped (fail-soft / out of scope):</p>
 * <ul>
 *   <li>{@code interface} declarations — implicitly abstract, never instantiated.</li>
 *   <li>Aggregates themselves marked {@code abstract} — allowed to leave abstract members
 *       unimplemented, exactly like Java/C++.</li>
 *   <li>Template / generic aggregate <em>definitions</em> — their member set is parametrised and
 *       not meaningful to check before instantiation.</li>
 *   <li>{@code IndexNotReadyException} (dumb mode) — silently skipped, like the other inspections.</li>
 * </ul>
 */
public final class KlangMissingImplementationInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new KlangVisitor() {
            @Override
            public void visitAggregateDecl(@NotNull KlangAggregateDecl agg) {
                check(agg, holder);
            }
        };
    }

    private static void check(@NotNull KlangAggregateDecl agg, @NotNull ProblemsHolder holder) {
        if (KlangResolveUtil.isInterface(agg)) return;
        if (KlangResolveUtil.isAbstractAggregate(agg)) return;
        if (agg.getTemplateDeclaration() != null || agg.getGenericDeclaration() != null) return;

        PsiElement name = agg.getNameIdentifier();
        if (name == null) return;

        List<KlangFunctionDecl> missing;
        try {
            missing = KlangResolveUtil.findMissingImplementations(agg);
        } catch (IndexNotReadyException ignored) {
            return; // dumb mode — fail soft
        }
        if (missing.isEmpty()) return;

        holder.registerProblem(name, message(missing),
                new KlangImplementMissingMethodsQuickFix(),
                new KlangMakeAbstractQuickFix());
    }

    private static @NotNull String message(@NotNull List<KlangFunctionDecl> missing) {
        String names = missing.stream()
                .map(KlangFunctionDecl::getName)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .limit(5)
                .collect(Collectors.joining("', '"));
        String suffix = missing.size() > 5 ? ", …" : "";
        String plural = missing.size() > 1 ? "s" : "";
        return "Class does not implement inherited abstract method" + plural + " '" + names + "'" + suffix;
    }
}

