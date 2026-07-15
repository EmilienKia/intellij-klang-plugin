package com.github.emilienkia.klang.plugin.language.refactoring.changesignature;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * "Change Signature…" action for K-lang functions/methods: rename + change parameters, return
 * type and specifiers (public/abstract/override/…), propagating the prototype (name, parameters,
 * return type) to overriding methods and, on request, to overridden base method(s).
 *
 * <p>Only enabled on regular named functions/methods (a plain {@code functionHead}) — operator
 * overloads, cast operators and destructors are excluded since they either have no independent
 * name or a fixed parameter shape.</p>
 */
public final class KlangChangeSignatureAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(functionAt(e) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        KlangFunctionDecl function = functionAt(e);
        if (project == null || function == null) return;

        KlangChangeSignatureDialog dialog = new KlangChangeSignatureDialog(function);
        if (!dialog.showAndGet()) return;

        KlangChangeSignatureProcessor.execute(project, function, dialog.buildModel());
    }

    private static @Nullable KlangFunctionDecl functionAt(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null || !(file instanceof KlangFile)) return null;

        int offset = editor.getCaretModel().getOffset();
        var element = file.findElementAt(offset);
        if (element == null && offset > 0) element = file.findElementAt(offset - 1);
        KlangFunctionDecl fn = element != null ? PsiTreeUtil.getParentOfType(element, KlangFunctionDecl.class) : null;
        return fn != null && fn.getFunctionHead() != null ? fn : null;
    }
}

