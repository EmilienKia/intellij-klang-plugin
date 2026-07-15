package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.psi.KlangIdentifierExpr;
import com.github.emilienkia.klang.plugin.language.psi.KlangQualifiedIdentifier;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypeSpec;
import com.github.emilienkia.klang.plugin.language.psi.KlangVisitor;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangTypeUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Reports references that resolve to nothing — the spec's {@code Undefined symbol} (§16).
 *
 * <p>It visits the two reference-bearing nodes ({@code identifierExpr} and
 * {@code qualifiedIdentifier}), so it transparently covers
 * {@link com.github.emilienkia.klang.plugin.language.psi.impl.KlangReference},
 * {@link com.github.emilienkia.klang.plugin.language.psi.impl.KlangQualifiedReference} and
 * {@link com.github.emilienkia.klang.plugin.language.psi.impl.KlangMemberReference}, and flags
 * the ones whose {@code multiResolve} is empty.</p>
 *
 * <p><b>Fail-soft by design</b> — it deliberately stays silent for:</p>
 * <ul>
 *   <li><b>Fundamental types / keywords</b>: they carry no reference (a fundamental
 *       {@code typeSpec} has no {@code qualifiedIdentifier}), so they are never visited; a
 *       defensive guard skips fundamental type specs anyway.</li>
 *   <li><b>Deferred resolution (§16)</b>: a name in <em>callee</em> position
 *       ({@code Name(args)} or {@code recv.member(args)}) may legitimately stay unresolved as a
 *       unified-call / overload candidate — see {@link KlangResolveUtil#isCalleeOfCall}.</li>
 *   <li><b>Dumb mode</b>: the inspection is not {@code DumbAware} (the platform skips it), and any
 *       {@code IndexNotReadyException} during resolution is swallowed.</li>
 * </ul>
 *
 * <p>When a simple name is unresolved, an <em>add-import</em> quick fix
 * ({@link KlangAddImportQuickFix}) proposes the modules that declare it.</p>
 *
 * <p><b>Known limitation:</b> cross-module / external-symbol resolution ({@code EXTERNAL_LOOKUP},
 * §5.8) is only partially modelled (no {@code KlangFqnIndex} narrowing yet), so a symbol that lives
 * in an un-imported external module is reported as unresolved and relies on the add-import fix to
 * surface its module. See {@code TODO.md} → <i>Unresolved-reference inspection</i>.</p>
 */
public final class KlangUnresolvedReferenceInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new KlangVisitor() {
            @Override
            public void visitIdentifierExpr(@NotNull KlangIdentifierExpr o) {
                // The callee of a call may be a deferred unified-call candidate (§16) — never flag it.
                if (KlangResolveUtil.isCalleeOfCall(o)) return;
                // The array virtual member 'size' (§9.8) has no declaration — recognise, don't flag.
                if (KlangTypeUtil.isArraySizeMember(o)) return;
                checkReference(o, holder);
            }

            @Override
            public void visitQualifiedIdentifier(@NotNull KlangQualifiedIdentifier o) {
                if (isFundamentalTypePosition(o)) return; // defensive — fundamentals are never Throwable/refs
                checkReference(o, holder);
            }
        };
    }

    /** Flags {@code element} when it carries a poly-variant reference that resolves to nothing. */
    private static void checkReference(@NotNull PsiElement element, @NotNull ProblemsHolder holder) {
        PsiReference ref = element.getReference();
        if (!(ref instanceof PsiPolyVariantReference poly)) return;

        ResolveResult[] results;
        try {
            results = poly.multiResolve(false);
        } catch (IndexNotReadyException ignored) {
            return; // index building — fail soft
        }
        if (results.length > 0) return; // resolved (possibly to overloads) — fine

        TextRange range = ref.getRangeInElement();
        String segment = range.substring(element.getText());
        if (segment.isBlank()) return;

        holder.registerProblem(element,
                "Cannot resolve symbol '" + segment + "'",
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                range,
                quickFixes(element, segment));
    }

    /** The add-import quick fix, attached only when the index knows a module declaring {@code name}. */
    private static LocalQuickFix @NotNull [] quickFixes(@NotNull PsiElement element,
                                                        @NotNull String name) {
        if (KlangAddImportQuickFix.hasCandidates(element, name)) {
            return new LocalQuickFix[]{new KlangAddImportQuickFix(name)};
        }
        return LocalQuickFix.EMPTY_ARRAY;
    }

    /** True when this qualified identifier sits in a {@code typeSpec} that is a fundamental type. */
    private static boolean isFundamentalTypePosition(@NotNull KlangQualifiedIdentifier qid) {
        KlangTypeSpec typeSpec = PsiTreeUtil.getParentOfType(qid, KlangTypeSpec.class);
        return typeSpec != null && typeSpec.getFundamentalTypeSpec() != null;
    }
}



