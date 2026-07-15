package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangBraceInitList;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reference for a designated initializer member name ({@code .field = …} in a brace
 * initializer list, §10.1). The (last) identifier of the {@code designatedMemberName}
 * resolves to a field of the aggregate that the enclosing {@code braceInitList} initializes
 * ({@link KlangTypeUtil#braceInitTargetAggregate}).
 */
public class KlangDesignatedMemberReference extends PsiReferenceBase.Poly<PsiElement>
        implements PsiPolyVariantReference {

    public KlangDesignatedMemberReference(@NotNull PsiElement element) {
        super(element, lastIdentifierRange(element), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        ASTNode last = lastIdentifier(myElement);
        if (last == null) return ResolveResult.EMPTY_ARRAY;
        String name = last.getText();

        KlangBraceInitList brace = PsiTreeUtil.getParentOfType(myElement, KlangBraceInitList.class);
        KlangAggregateDecl target = KlangTypeUtil.braceInitTargetAggregate(brace);
        if (target == null) return ResolveResult.EMPTY_ARRAY;

        List<PsiElement> found = KlangResolveUtil.resolveMember(target, name, myElement);
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

    /** The last IDENTIFIER token of {@code (IDENTIFIER '::')* IDENTIFIER}. */
    private static @Nullable ASTNode lastIdentifier(@NotNull PsiElement element) {
        ASTNode result = null;
        for (ASTNode n = element.getNode().getFirstChildNode(); n != null; n = n.getTreeNext()) {
            if (n.getElementType() == KlangTypes.IDENTIFIER) result = n;
        }
        return result;
    }

    private static TextRange lastIdentifierRange(@NotNull PsiElement element) {
        ASTNode id = lastIdentifier(element);
        if (id == null) return TextRange.EMPTY_RANGE;
        int start = id.getStartOffset() - element.getTextRange().getStartOffset();
        return new TextRange(start, start + id.getTextLength());
    }
}

