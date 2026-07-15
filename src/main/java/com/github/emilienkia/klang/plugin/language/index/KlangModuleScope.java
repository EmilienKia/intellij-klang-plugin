package com.github.emilienkia.klang.plugin.language.index;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <b>Multi-file indexing — milestone M1.</b>
 *
 * <p>Thin helper that turns the {@link KlangModuleModel} into the concrete PSI inputs the
 * resolver will consume in later milestones: the list of {@link KlangFile}s that form an
 * anchor's module root, and the matching search scope.</p>
 *
 * <p>In M1 this only exposes the module-root file set (and is unit-tested); wiring it into
 * {@code KlangResolveUtil} so the module root spans several files is milestone M2.</p>
 */
public final class KlangModuleScope {

    private KlangModuleScope() {}

    /**
     * The files that form {@code anchor}'s module root. The anchor's own file is returned
     * <b>first</b> so callers can preserve same-file shadowing precedence (§15).
     * Returns an empty list when the anchor is not inside a {@link KlangFile}.
     */
    public static @NotNull List<KlangFile> moduleRootFiles(@NotNull PsiElement anchor) {
        PsiFile containing = anchor.getContainingFile();
        if (!(containing instanceof KlangFile current)) return Collections.emptyList();

        Project project = anchor.getProject();
        KlangModuleModel model = KlangModuleModel.getInstance(project);
        Set<VirtualFile> files = model.filesInModuleOf(current);

        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile currentVf = current.getVirtualFile();

        List<KlangFile> result = new ArrayList<>();
        result.add(current); // current file first (shadowing precedence)
        for (VirtualFile vf : files) {
            if (vf.equals(currentVf)) continue;
            PsiFile file = psiManager.findFile(vf);
            if (file instanceof KlangFile kf) result.add(kf);
        }
        return result;
    }

    /** The search scope spanning {@code anchor}'s module root. */
    public static @NotNull GlobalSearchScope moduleScope(@NotNull PsiElement anchor) {
        PsiFile containing = anchor.getContainingFile();
        if (!(containing instanceof KlangFile current)) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }
        return KlangModuleModel.getInstance(anchor.getProject()).moduleScope(current);
    }

    /**
     * The {@link KlangFile}s that explicitly declare {@code moduleName} (the raw, policy-independent
     * grouping). Used to resolve {@code import} targets / the standard library to another module's
     * files. Returns an empty list for an unknown module.
     */
    public static @NotNull List<KlangFile> filesOfModule(@NotNull Project project,
                                                         @NotNull String moduleName) {
        Set<VirtualFile> files = KlangModuleModel.getInstance(project).filesDeclaringModule(moduleName);
        if (files.isEmpty()) return Collections.emptyList();
        PsiManager psiManager = PsiManager.getInstance(project);
        List<KlangFile> result = new ArrayList<>();
        for (VirtualFile vf : files) {
            PsiFile file = psiManager.findFile(vf);
            if (file instanceof KlangFile kf) result.add(kf);
        }
        return result;
    }
}

