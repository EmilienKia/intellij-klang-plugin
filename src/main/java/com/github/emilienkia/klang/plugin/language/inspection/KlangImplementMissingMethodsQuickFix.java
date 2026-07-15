package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.generate.KlangMemberStubGenerator;
import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Quick fix offered by {@link KlangMissingImplementationInspection}: generate an
 * {@code override} stub for every abstract method the class inherits but does not implement.
 *
 * <p>Stub synthesis is delegated to {@link KlangMemberStubGenerator}, shared with the Alt+Insert
 * "Override/Implement Members…" Generate action
 * ({@code com.github.emilienkia.klang.plugin.language.generate.KlangOverrideImplementMembersAction}).</p>
 *
 * <p>Does not cache PSI in the fix instance: it re-resolves the aggregate and recomputes the
 * missing-method set from the (possibly stale) {@link ProblemDescriptor} at {@link #applyFix} time,
 * mirroring {@code KlangAddImportQuickFix}'s defensive re-resolution.</p>
 */
final class KlangImplementMissingMethodsQuickFix implements LocalQuickFix {

    @Override
    public @NotNull String getFamilyName() {
        return "Implement missing methods";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement name = descriptor.getPsiElement();
        if (name == null) return;
        KlangAggregateDecl agg = PsiTreeUtil.getParentOfType(name, KlangAggregateDecl.class, false);
        if (agg == null) return;

        List<KlangFunctionDecl> missing = KlangResolveUtil.findMissingImplementations(agg);
        KlangMemberStubGenerator.insertStubs(project, agg, missing);
    }
}
