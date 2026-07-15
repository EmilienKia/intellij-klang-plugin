package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Quick fix offered by {@link KlangMissingImplementationInspection}: mark the class {@code abstract}
 * instead of implementing the missing methods — mirrors Java/C++'s "leave it abstract" option.
 *
 * <p>Inserts the {@code abstract} specifier right before the aggregate's type keyword
 * ({@code class}/{@code struct}/{@code annotation}) — so {@code public class Foo} becomes
 * {@code public abstract class Foo}, keeping any existing specifiers untouched and in place.
 * Idempotent no-op if {@code abstract} is already present.</p>
 */
final class KlangMakeAbstractQuickFix implements LocalQuickFix {

    private static final TokenSet AGGREGATE_KEYWORDS = TokenSet.create(
            KlangTypes.KW_CLASS, KlangTypes.KW_STRUCT, KlangTypes.KW_INTERFACE, KlangTypes.KW_ANNOTATION);

    @Override
    public @NotNull String getFamilyName() {
        return "Make class abstract";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement name = descriptor.getPsiElement();
        if (name == null) return;
        KlangAggregateDecl agg = PsiTreeUtil.getParentOfType(name, KlangAggregateDecl.class, false);
        if (agg == null || KlangResolveUtil.isAbstractAggregate(agg)) return; // idempotent

        PsiFile file = agg.getContainingFile();
        if (file == null) return;
        PsiDocumentManager docManager = PsiDocumentManager.getInstance(project);
        Document doc = docManager.getDocument(file);
        if (doc == null) return;

        ASTNode keyword = agg.getNode().findChildByType(AGGREGATE_KEYWORDS);
        if (keyword == null) return;

        doc.insertString(keyword.getStartOffset(), "abstract ");
        docManager.commitDocument(doc);
    }
}


