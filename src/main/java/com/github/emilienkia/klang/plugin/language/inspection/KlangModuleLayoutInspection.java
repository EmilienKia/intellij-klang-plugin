package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.index.KlangModuleModel;
import com.github.emilienkia.klang.plugin.language.psi.KlangDeclaration;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangModuleDeclaration;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Flags the two error shapes of the directory-aware module layout (see {@link KlangModuleModel}):
 *
 * <ul>
 *   <li>{@link KlangModuleModel.ProblemKind#MIXED_DIRECTORY} — a directory mixes a module-less
 *       file with several distinct declared modules (a module-less file requires that the others
 *       agree on a single module);</li>
 *   <li>{@link KlangModuleModel.ProblemKind#AMBIGUOUS_INHERITANCE} — a module-less directory
 *       inherits from an ancestor that declares several modules, so the module is ambiguous.</li>
 * </ul>
 *
 * <p>The diagnostic is anchored on the file's {@code module} declaration when present, otherwise
 * on its first top-level declaration. Resolution itself degrades gracefully regardless (the model
 * picks a deterministic best-effort module); this inspection only surfaces the problem.</p>
 */
public final class KlangModuleLayoutInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file,
                                                    @NotNull InspectionManager manager,
                                                    boolean isOnTheFly) {
        if (!(file instanceof KlangFile klangFile)) return null;
        VirtualFile vf = file.getVirtualFile();
        if (vf == null) return null;

        KlangModuleModel.LayoutProblem problem =
                KlangModuleModel.getInstance(file.getProject()).problemFor(vf);
        if (problem == null) return null;

        PsiElement anchor = anchorFor(klangFile);
        if (anchor == null) return null;

        ProblemDescriptor descriptor = manager.createProblemDescriptor(
                anchor, problem.message(), isOnTheFly, LocalQuickFix.EMPTY_ARRAY,
                ProblemHighlightType.GENERIC_ERROR);
        return new ProblemDescriptor[]{descriptor};
    }

    /** The element to underline: the {@code module} declaration if any, else the first declaration. */
    private static @Nullable PsiElement anchorFor(@NotNull KlangFile file) {
        KlangModuleDeclaration moduleDecl = PsiTreeUtil.getChildOfType(file, KlangModuleDeclaration.class);
        if (moduleDecl != null) return moduleDecl;
        return PsiTreeUtil.getChildOfType(file, KlangDeclaration.class);
    }
}


