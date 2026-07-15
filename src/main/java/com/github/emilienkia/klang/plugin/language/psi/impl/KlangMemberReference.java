package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangUnionDecl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Member-access reference (§10): the right-hand side of {@code expr.member} /
 * {@code expr->member}. Unlike {@link KlangReference}, it does <em>not</em> use the lexical
 * scope chain — it resolves {@code member} against the inferred <strong>type of the
 * receiver</strong> ({@link KlangTypeUtil}):
 *
 * <ol>
 *   <li>field of the receiver aggregate (§10.1),</li>
 *   <li>method of the aggregate, incl. inherited members (§10.2 / §10.4),</li>
 *   <li>otherwise unified call syntax — a free function whose first parameter is
 *       {@code ref&lt;receiverType&gt;} (§10.3).</li>
 * </ol>
 *
 * <p>If the receiver type cannot be inferred, the reference resolves to nothing (fail soft).</p>
 */
public class KlangMemberReference extends PsiReferenceBase.Poly<PsiElement>
        implements PsiPolyVariantReference {

    public KlangMemberReference(@NotNull PsiElement element) {
        super(element, computeRange(element), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        String name = KlangResolveUtil.lastSegment(myElement.getText().trim());
        if (name.isEmpty()) return ResolveResult.EMPTY_ARRAY;

        // The receiver's nominal type may be an aggregate (struct/class) or a union — member
        // access is resolved differently for each (§10).
        PsiElement receiver = KlangTypeUtil.receiverNominalTypeOfMember(myElement);

        List<PsiElement> found = new ArrayList<>();
        if (receiver instanceof KlangAggregateDecl agg) {
            found.addAll(KlangResolveUtil.resolveMember(agg, name, myElement));
            if (found.isEmpty()) {
                found.addAll(KlangResolveUtil.resolveUnifiedCall(name, agg, myElement));
            }
        } else if (receiver instanceof KlangUnionDecl union) {
            // Union value member access (e.g. _storage.result) — resolve against the union's
            // declared alternatives. Unions have no methods or unified-call syntax.
            PsiElement member = KlangResolveUtil.resolveUnionMember(union, name);
            if (member != null) found.add(member);
        }
        return found.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) {
        return KlangPsiElementFactory.renameIdentifierAt(
                myElement, getRangeInElement().getStartOffset(), newName);
    }

    private static TextRange computeRange(@NotNull PsiElement element) {
        String text = element.getText();
        if (text == null) return TextRange.EMPTY_RANGE;
        int idx = text.lastIndexOf("::");
        int start = idx >= 0 ? idx + 2 : 0;
        return new TextRange(start, text.length());
    }
}

