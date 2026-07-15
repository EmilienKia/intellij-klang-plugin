package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.KlangNavigationPresentation;
import com.github.emilienkia.klang.plugin.language.KlangUi;
import com.github.emilienkia.klang.plugin.language.index.KlangModuleModel;
import com.github.emilienkia.klang.plugin.language.index.KlangSymbolNameIndex;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangImportDeclaration;
import com.github.emilienkia.klang.plugin.language.psi.KlangModuleDeclaration;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Quick fix offered by {@link KlangUnresolvedReferenceInspection}: <em>add the {@code import}</em>
 * for an unresolved simple name.
 *
 * <p>It looks the name up in {@link KlangSymbolNameIndex} (files that declare a matching importable
 * symbol), maps those files to their effective module ({@link KlangModuleModel}), drops the anonymous
 * module and the current file's own module, and offers the remaining module names. A single candidate
 * is inserted directly; several candidates pop up a chooser.</p>
 *
 * <p><b>Fail-soft:</b> the fix is silently unavailable while indexes are still building (dumb mode)
 * and never proposes a module that is already imported.</p>
 *
 * <p><b>Known limitation (cross-module / external symbols):</b> candidate discovery is bounded by what
 * {@link KlangSymbolNameIndex} sees in the project. True external-module resolution
 * ({@code EXTERNAL_LOOKUP}, §5.8 — narrowing via the not-yet-built {@code KlangFqnIndex}) is still
 * deferred, so symbols that live outside the indexed project sources are not offered yet. See the
 * note in {@code TODO.md} → <i>Unresolved-reference inspection</i>.</p>
 */
final class KlangAddImportQuickFix implements LocalQuickFix {

    private final @NotNull String simpleName;

    KlangAddImportQuickFix(@NotNull String simpleName) {
        this.simpleName = simpleName;
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Add import for '" + simpleName + "'";
    }

    /**
     * Whether at least one importable module declares {@code simpleName} (so the fix is worth
     * attaching). Returns {@code false} fail-soft in dumb mode.
     */
    static boolean hasCandidates(@NotNull PsiElement element, @NotNull String simpleName) {
        return !candidateChoices(element, simpleName).isEmpty();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        if (element == null) return;
        PsiFile file = element.getContainingFile();
        if (!(file instanceof KlangFile klangFile)) return;

        List<ImportChoice> choices = candidateChoices(element, simpleName);
        if (choices.isEmpty()) return;
        if (choices.size() == 1) {
            insertImport(project, klangFile, choices.get(0).moduleName());
            return;
        }
        chooseModule(project, klangFile, choices);
    }

    // ── Candidate discovery ───────────────────────────────────────────────────

    /** Import choices (module + representative symbol) for {@code simpleName}, minus already-visible modules. */
    private static @NotNull List<ImportChoice> candidateChoices(@NotNull PsiElement element,
                                                                 @NotNull String simpleName) {
        PsiFile file = element.getContainingFile();
        if (!(file instanceof KlangFile klangFile)) return List.of();
        Project project = element.getProject();

        Map<String, PsiElement> byModule = new LinkedHashMap<>();
        try {
            KlangModuleModel model = KlangModuleModel.getInstance(project);
            String ownModule = model.moduleNameOf(klangFile);
            Set<String> alreadyImported = importedModules(klangFile);
            PsiManager psiManager = PsiManager.getInstance(project);

            for (VirtualFile vf : KlangSymbolNameIndex.filesDeclaring(project, simpleName)) {
                PsiFile declaring = psiManager.findFile(vf);
                if (!(declaring instanceof KlangFile declaringKlang)) continue;
                String module = model.moduleNameOf(declaringKlang);
                if (module.isEmpty()) continue;                 // anonymous — nothing to import
                if (module.equals(ownModule)) continue;          // same module — already visible
                if (alreadyImported.contains(module)) continue;  // already imported
                byModule.computeIfAbsent(module, m -> firstDeclarationByName(declaringKlang, simpleName));
            }
        } catch (IndexNotReadyException ignored) {
            return List.of(); // dumb mode — fail soft
        }
        return byModule.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ImportChoice(e.getKey(), e.getValue()))
                .toList();
    }

    /** The module names already imported by {@code file}. */
    private static @NotNull Set<String> importedModules(@NotNull KlangFile file) {
        Set<String> imported = new TreeSet<>();
        for (KlangImportDeclaration imp : PsiTreeUtil.getChildrenOfTypeAsList(file, KlangImportDeclaration.class)) {
            String text = imp.getQualifiedIdentifier().getText().trim();
            if (text.startsWith("::")) text = text.substring(2);
            if (!text.isEmpty()) imported.add(text);
        }
        return imported;
    }

    // ── Insertion ─────────────────────────────────────────────────────────────

    private static void chooseModule(@NotNull Project project,
                                     @NotNull KlangFile file,
                                     @NotNull List<ImportChoice> choices) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) { // headless / no editor — pick the first deterministically
            insertImport(project, file, choices.get(0).moduleName());
            return;
        }
        JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>(KlangUi.Text.POPUP_IMPORT_MODULE, choices) {
            @Override
            public @NotNull String getTextFor(ImportChoice value) {
                PsiElement symbol = value.symbol();
                if (symbol == null) {
                    return "import " + value.moduleName() + ";";
                }
                return "import " + value.moduleName() + ";  —  " + KlangNavigationPresentation.elementText(symbol);
            }

            @Override
            public Icon getIconFor(ImportChoice value) {
                PsiElement symbol = value.symbol();
                if (symbol == null) return KlangUi.Icons.IMPORT_MODULE;
                Icon icon = KlangNavigationPresentation.icon(symbol);
                return icon != null ? icon : KlangUi.Icons.IMPORT_MODULE;
            }

            @Override
            public PopupStep<?> onChosen(ImportChoice selected, boolean finalChoice) {
                return doFinalStep(() -> insertImport(project, file, selected.moduleName()));
            }
        }).showInBestPositionFor(editor);
    }

    /** Inserts {@code import <module>;} after the last existing import (or the module decl, or at top). */
    private static void insertImport(@NotNull Project project,
                                     @NotNull KlangFile file,
                                     @NotNull String moduleName) {
        if (importedModules(file).contains(moduleName)) return; // idempotent

        PsiDocumentManager docManager = PsiDocumentManager.getInstance(project);
        Document doc = docManager.getDocument(file);
        if (doc == null) return;

        // Editing the document (then committing) reparses the whole file, which always yields a
        // consistent PSI — unlike manual addAfter/whitespace surgery, which can produce a tree
        // that fails the platform's post-action PSI-structure check.
        PsiElement anchor = lastImport(file);
        if (anchor == null) anchor = PsiTreeUtil.getChildOfType(file, KlangModuleDeclaration.class);

        if (anchor != null) {
            int offset = anchor.getTextRange().getEndOffset();
            doc.insertString(offset, "\nimport " + moduleName + ";");
        } else {
            doc.insertString(0, "import " + moduleName + ";\n");
        }
        docManager.commitDocument(doc);
    }

    private static @Nullable KlangImportDeclaration lastImport(@NotNull KlangFile file) {
        List<KlangImportDeclaration> imports =
                PsiTreeUtil.getChildrenOfTypeAsList(file, KlangImportDeclaration.class);
        return imports.isEmpty() ? null : imports.get(imports.size() - 1);
    }

    private static @Nullable PsiElement firstDeclarationByName(@NotNull KlangFile file,
                                                               @NotNull String simpleName) {
        for (PsiElement e : PsiTreeUtil.collectElements(file, el ->
                el instanceof KlangNamedElement named
                        && simpleName.equals(named.getName()))) {
            return e;
        }
        return null;
    }

    private record ImportChoice(@NotNull String moduleName, @Nullable PsiElement symbol) {
    }
}


