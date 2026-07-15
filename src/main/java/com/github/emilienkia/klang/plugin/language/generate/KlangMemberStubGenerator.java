package com.github.emilienkia.klang.plugin.language.generate;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangParameterList;
import com.github.emilienkia.klang.plugin.language.psi.KlangReturnTypeOrMemberInitList;
import com.github.emilienkia.klang.plugin.language.psi.KlangThrowsClause;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypeSpec;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Shared stub-synthesis helper for K-lang "override an inherited method" generation, used by both
 * {@code KlangImplementMissingMethodsQuickFix} (fixes the missing-implementation inspection) and
 * {@link KlangOverrideImplementMembersAction} (the Alt+Insert "Generate…" action) — see
 * {@code docs/klang/abstract-implementation-plan.md} §5 / §7.
 *
 * <p>The stub signature (return type, parameter list, throws clause) is copied verbatim from the
 * base declaration's PSI text — robust to any type-spec shape (arrays, pointers, function-ref
 * types, generics) without re-deriving it. The body defaults to an empty block, or
 * {@code throw ::k::NotImplementedError();} when {@code ::k::Throwable} resolves from the target
 * file (fail-soft otherwise: never emit code referencing a symbol that might not resolve).</p>
 */
public final class KlangMemberStubGenerator {

    private KlangMemberStubGenerator() {}

    /**
     * Inserts an {@code override} stub for every method in {@code baseMethods} into {@code agg},
     * right before its closing {@code '}'}, then reformats the inserted range so indentation
     * matches the file's style. Must be called from within a write action (both call sites — the
     * quick fix's {@code applyFix} and the Generate action's {@code WriteCommandAction} — already
     * satisfy this).
     */
    public static void insertStubs(@NotNull Project project,
                                   @NotNull KlangAggregateDecl agg,
                                   @NotNull List<KlangFunctionDecl> baseMethods) {
        if (baseMethods.isEmpty()) return;

        PsiFile file = agg.getContainingFile();
        if (file == null) return;
        PsiDocumentManager docManager = PsiDocumentManager.getInstance(project);
        Document doc = docManager.getDocument(file);
        if (doc == null) return;

        ASTNode rbrace = agg.getNode().findChildByType(KlangTypes.PUNC_RBRACE);
        if (rbrace == null) return;
        int insertOffset = rbrace.getStartOffset();

        Set<KlangAggregateDecl> throwables = KlangResolveUtil.resolveThrowable(agg);

        StringBuilder sb = new StringBuilder();
        for (KlangFunctionDecl baseMethod : baseMethods) {
            sb.append('\n').append(generateStub(baseMethod, throwables)).append('\n');
        }

        doc.insertString(insertOffset, sb.toString());
        docManager.commitDocument(doc);

        // Reformat the freshly inserted range so indentation matches the file's style.
        PsiFile committed = docManager.getPsiFile(doc);
        if (committed != null) {
            int end = insertOffset + sb.length();
            CodeStyleManager.getInstance(project).reformatText(committed, insertOffset, Math.min(end, doc.getTextLength()));
        }
    }

    /**
     * Renders a concrete {@code override} stub for {@code baseMethod}. Works whether the base
     * method is still abstract (must-implement) or already concrete/virtual (optional override) —
     * both cases copy the exact signature so the override is well-formed either way.
     */
    public static @NotNull String generateStub(@NotNull KlangFunctionDecl baseMethod,
                                                @NotNull Set<KlangAggregateDecl> throwables) {
        String name = baseMethod.getName();
        KlangTypeSpec returnType = returnTypeOf(baseMethod);
        KlangParameterList params = baseMethod.getParameterList();
        KlangThrowsClause throwsClause = baseMethod.getThrowsClause();

        // K's function syntax is 'name(params) : ReturnType', unlike Java/C++'s
        // 'ReturnType name(params)' — the return type trails the parameter list.
        StringBuilder sb = new StringBuilder("override ");
        sb.append(name).append('(');
        if (params != null) sb.append(params.getText().trim());
        sb.append(')');
        if (returnType != null) sb.append(" : ").append(returnType.getText().trim());
        if (throwsClause != null) sb.append(' ').append(throwsClause.getText().trim());

        boolean hasReturnValue = returnType != null && !"void".equals(returnType.getText().trim());
        String ownerName = ownerNameOf(baseMethod);
        sb.append(" {\n")
          .append("    // TODO: auto-generated stub — implement inherited abstract member")
          .append(ownerName != null ? " from " + ownerName : "").append('\n');
        if (!throwables.isEmpty()) {
            sb.append("    throw ::k::NotImplementedError();\n");
        } else if (hasReturnValue) {
            sb.append("    // TODO: return a value\n");
        }
        sb.append('}');
        return sb.toString();
    }

    private static @Nullable String ownerNameOf(@NotNull KlangFunctionDecl fn) {
        KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(fn);
        return owner != null ? owner.getName() : null;
    }

    /**
     * The declared return type of a regular (non-cast-operator) method: nested inside
     * {@code returnTypeOrMemberInitList} (only meaningful when that alternative parsed as a plain
     * type, not a member-init/static-dep list — irrelevant here since constructors are excluded).
     * Falls back to {@link KlangFunctionDecl#getTypeSpec()}, which only ever carries a value for
     * the cast-operator ({@code operator () : Type}) head shape.
     */
    private static @Nullable KlangTypeSpec returnTypeOf(@NotNull KlangFunctionDecl fn) {
        KlangReturnTypeOrMemberInitList tail = fn.getReturnTypeOrMemberInitList();
        if (tail != null && tail.getTypeSpec() != null) return tail.getTypeSpec();
        return fn.getTypeSpec();
    }
}

