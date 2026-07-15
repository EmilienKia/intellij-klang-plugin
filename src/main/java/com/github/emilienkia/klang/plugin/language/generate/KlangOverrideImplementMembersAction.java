package com.github.emilienkia.klang.plugin.language.generate;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Alt+Insert "Generate…" action: <b>Override/Implement Members…</b>.
 *
 * <p>Lets the user pick, from the whole inherited surface of the {@link KlangAggregateDecl} at the
 * caret (transitive bases — interfaces and classes, inheritance is always virtual), which methods
 * to generate an {@code override} stub for. Still-unimplemented abstract methods
 * ({@link KlangResolveUtil#collectAbstractSlots}) come pre-checked, exactly like Java's own
 * "Implement Methods" dialog; already-concrete/virtual inherited methods
 * ({@link KlangResolveUtil#collectOverridableSlots}) are offered unchecked, as optional overrides —
 * the C++/Java-style "override a non-abstract base method" case.</p>
 *
 * <p>Stub synthesis is shared with {@code KlangImplementMissingMethodsQuickFix} via
 * {@link KlangMemberStubGenerator}, so both entry points produce identical code.</p>
 *
 * <p>Design: {@code docs/klang/abstract-implementation-plan.md} §7.</p>
 */
public final class KlangOverrideImplementMembersAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        KlangAggregateDecl agg = aggregateAt(e);
        e.getPresentation().setEnabledAndVisible(agg != null && !candidates(agg).isEmpty());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        KlangAggregateDecl agg = aggregateAt(e);
        if (project == null || agg == null) return;

        Map<String, KlangFunctionDecl> candidates = candidates(agg);
        if (candidates.isEmpty()) return;

        java.util.Set<String> requiredNames = KlangResolveUtil.collectAbstractSlots(agg).keySet();
        List<KlangOverridableMember> items = new ArrayList<>();
        List<KlangOverridableMember> preChecked = new ArrayList<>();
        for (Map.Entry<String, KlangFunctionDecl> entry : candidates.entrySet()) {
            boolean required = requiredNames.contains(entry.getKey())
                    && KlangResolveUtil.requiresImplementation(entry.getValue());
            KlangOverridableMember member = new KlangOverridableMember(entry.getValue(), required);
            items.add(member);
            if (required) preChecked.add(member);
        }

        MemberChooser<KlangOverridableMember> chooser = new MemberChooser<>(
                items.toArray(new KlangOverridableMember[0]), false, true, project);
        chooser.setTitle("Select Methods to Override/Implement");
        chooser.selectElements(preChecked.toArray(new ClassMember[0]));
        if (!chooser.showAndGet()) return;

        List<KlangFunctionDecl> selected = new ArrayList<>();
        List<KlangOverridableMember> chosen = chooser.getSelectedElements();
        if (chosen == null || chosen.isEmpty()) return;
        for (KlangOverridableMember member : chosen) {
            selected.add(member.method());
        }
        if (selected.isEmpty()) return;

        WriteCommandAction.runWriteCommandAction(project, "Override/Implement Members", null,
                () -> KlangMemberStubGenerator.insertStubs(project, agg, selected));
    }

    /** The full override-candidate set (abstract-required ∪ concrete/virtual-optional), by name. */
    private static @NotNull Map<String, KlangFunctionDecl> candidates(@NotNull KlangAggregateDecl agg) {
        if (KlangResolveUtil.isInterface(agg)) return java.util.Collections.emptyMap();
        return KlangResolveUtil.collectOverridableSlots(agg);
    }

    private static @Nullable KlangAggregateDecl aggregateAt(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null || !(file instanceof KlangFile)) return null;

        int offset = editor.getCaretModel().getOffset();
        var element = file.findElementAt(offset);
        if (element == null && offset > 0) element = file.findElementAt(offset - 1);
        return element != null ? PsiTreeUtil.getParentOfType(element, KlangAggregateDecl.class) : null;
    }
}




