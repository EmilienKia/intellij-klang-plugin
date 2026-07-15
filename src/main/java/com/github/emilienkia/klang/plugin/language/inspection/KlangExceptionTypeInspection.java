package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangCatchParameterDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangExpression;
import com.github.emilienkia.klang.plugin.language.psi.KlangQualifiedIdentifier;
import com.github.emilienkia.klang.plugin.language.psi.KlangThrowStatement;
import com.github.emilienkia.klang.plugin.language.psi.KlangThrowsClause;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypeSpec;
import com.github.emilienkia.klang.plugin.language.psi.KlangVisitor;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangTypeUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Validates that exception types are {@code ::k::Throwable}-derived (the K spec's
 * {@code 0x01C0} rule). Three sites are checked:
 *
 * <ul>
 *   <li>a {@code throws T, …} clause type,</li>
 *   <li>a {@code catch (e : T&)} parameter type,</li>
 *   <li>the operand of a {@code throw expr;} statement (best-effort type inference).</li>
 * </ul>
 *
 * <p><b>Fail-soft by design.</b> The check is gated on {@code ::k::Throwable} being
 * resolvable from the current file (standard library present/imported, or a local
 * declaration). When it cannot be resolved the inspection does nothing, so projects
 * without the {@code k} library never see false positives. Likewise, an exception
 * <em>type</em> that cannot be resolved at all is left to the (future) unresolved-reference
 * inspection, and a {@code throw} operand whose type cannot be inferred is skipped.</p>
 */
public final class KlangExceptionTypeInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new KlangVisitor() {
            @Override
            public void visitThrowsClause(@NotNull KlangThrowsClause clause) {
                for (KlangTypeSpec typeSpec : clause.getTypeSpecList()) {
                    checkType(typeSpec, holder);
                }
            }

            @Override
            public void visitCatchParameterDecl(@NotNull KlangCatchParameterDecl decl) {
                checkType(decl.getTypeSpec(), holder);
            }

            @Override
            public void visitThrowStatement(@NotNull KlangThrowStatement statement) {
                KlangExpression operand = statement.getExpression();
                if (operand == null) return; // bare 'throw;' rethrow — not a type check
                checkExpression(operand, holder);
            }
        };
    }

    /** Validates an explicit exception type ({@code throws}/{@code catch} position). */
    private static void checkType(@NotNull KlangTypeSpec typeSpec, @NotNull ProblemsHolder holder) {
        Set<KlangAggregateDecl> throwables = KlangResolveUtil.resolveThrowable(typeSpec);
        if (throwables.isEmpty()) return; // cannot validate without the base type — fail soft

        // Highlight (and name) the qualified identifier when present (drops any '&'/'*' suffix);
        // otherwise the whole type spec (e.g. a fundamental type).
        KlangQualifiedIdentifier qid = typeSpec.getQualifiedIdentifier();
        PsiElement reportOn = qid != null ? qid : typeSpec;

        KlangAggregateDecl agg = KlangTypeUtil.aggregateOfTypeSpec(typeSpec, typeSpec);
        if (agg != null) {
            if (!KlangResolveUtil.derivesFromAny(agg, throwables)) {
                holder.registerProblem(reportOn, message(reportOn.getText().trim()));
            }
            return;
        }

        // Not an aggregate. A fundamental type (int, float, …) can never be Throwable.
        if (typeSpec.getFundamentalTypeSpec() != null) {
            holder.registerProblem(reportOn, message(reportOn.getText().trim()));
            return;
        }

        // A qualified name that resolves to a non-class declaration (enum/union/variable/…)
        // is also not Throwable. Unresolved names are deliberately left untouched.
        if (qid != null) {
            List<PsiElement> targets = KlangResolveUtil.resolve(qid, qid.getText().trim());
            if (!targets.isEmpty() && targets.stream().noneMatch(t -> t instanceof KlangAggregateDecl)) {
                holder.registerProblem(reportOn, message(reportOn.getText().trim()));
            }
        }
    }

    /** Validates the operand of a {@code throw} statement via best-effort type inference. */
    private static void checkExpression(@NotNull KlangExpression operand, @NotNull ProblemsHolder holder) {
        KlangAggregateDecl agg = KlangTypeUtil.aggregateOfExpression(operand);
        if (agg == null) return; // type could not be inferred — fail soft

        Set<KlangAggregateDecl> throwables = KlangResolveUtil.resolveThrowable(operand);
        if (throwables.isEmpty()) return;

        if (!KlangResolveUtil.derivesFromAny(agg, throwables)) {
            String name = agg.getName() != null ? agg.getName() : operand.getText().trim();
            holder.registerProblem(operand, message(name));
        }
    }

    private static @NotNull String message(@NotNull String typeText) {
        return "Exception type '" + typeText + "' does not derive from '::k::Throwable'";
    }
}


