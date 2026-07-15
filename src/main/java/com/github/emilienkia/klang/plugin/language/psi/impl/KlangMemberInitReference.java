package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference for a constructor member/base initializer ({@code Name(args)} in a
 * {@code memberInitList}, §10.1).
 *
 * <ul>
 *   <li>When {@code Name} is a <strong>field</strong> of the enclosing aggregate, it resolves
 *       to that field (the initializer sets the member).</li>
 *   <li>When {@code Name} is a <strong>base type</strong>, the initializer is a call to the
 *       base's constructor, so it resolves to the base's constructor overloads (falling back
 *       to the base type when none is declared) — mirroring the type/constructor split applied
 *       to ordinary constructor calls.</li>
 * </ul>
 */
public class KlangMemberInitReference extends PsiReferenceBase.Poly<PsiElement>
        implements PsiPolyVariantReference {

    public KlangMemberInitReference(@NotNull PsiElement element) {
        super(element, identifierRange(element), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        ASTNode id = myElement.getNode().findChildByType(KlangTypes.IDENTIFIER);
        if (id == null) return ResolveResult.EMPTY_ARRAY;
        String name = id.getText();

        KlangAggregateDecl owner = PsiTreeUtil.getParentOfType(myElement, KlangAggregateDecl.class);
        if (owner == null) return ResolveResult.EMPTY_ARRAY;

        List<PsiElement> found = new ArrayList<>(KlangResolveUtil.resolveMember(owner, name, myElement));
        if (found.isEmpty()) found.addAll(basesNamed(owner, name));

        return found.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] r = multiResolve(false);
        return r.length == 1 ? r[0].getElement() : null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) {
        return KlangPsiElementFactory.renameIdentifierAt(
                myElement, getRangeInElement().getStartOffset(), newName);
    }

    /** Constructors of base aggregates of {@code owner} whose (last) name segment equals
     *  {@code name} (falling back to the base type itself when it declares no constructor). */
    private List<PsiElement> basesNamed(@NotNull KlangAggregateDecl owner, @NotNull String name) {
        List<PsiElement> bases = new ArrayList<>();
        KlangBaseClause clause = owner.getBaseClause();
        if (clause == null) return bases;
        for (KlangBaseSpec spec : clause.getBaseSpecList()) {
            String baseText = spec.getQualifiedIdentifier().getText().trim();
            if (!name.equals(KlangResolveUtil.lastSegment(baseText))) continue;
            for (PsiElement el : KlangResolveUtil.resolve(spec, baseText)) {
                if (el instanceof KlangAggregateDecl) bases.add(el);
            }
        }
        // A base initializer is a call to the base constructor → reference the constructor.
        return KlangResolveUtil.preferConstructors(bases);
    }

    private static TextRange identifierRange(@NotNull PsiElement element) {
        ASTNode id = element.getNode().findChildByType(KlangTypes.IDENTIFIER);
        if (id == null) return TextRange.EMPTY_RANGE;
        int start = id.getStartOffset() - element.getTextRange().getStartOffset();
        return new TextRange(start, start + id.getTextLength());
    }
}

