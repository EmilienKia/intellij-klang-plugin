package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.KlangFileType;
import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangAssignmentOperator;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangOperatorFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangOperatorSymbol;
import com.github.emilienkia.klang.plugin.language.psi.KlangPostfixOp;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bidirectional navigation for {@code operator} overloads, shared by
 * {@code KlangOperatorGotoDeclarationHandler} (usage → definition) and
 * {@code KlangOperatorUsageLineMarkerProvider} (definition → usages). Both directions go through
 * {@link #resolveOperatorUsage} so they stay exact inverses of each other: an {@code a op b} /
 * {@code op a} / {@code a[i]} site is a usage of an overload iff the forward resolution of its
 * operator token lands on that overload.
 *
 * <p>Covered: binary, prefix-unary and <em>postfix</em> {@code ++}/{@code --} operators (the
 * {@link #OPERATOR_TOKENS} set) and the subscript {@code []}. The prefix/postfix {@code ++}/{@code --}
 * forms are disambiguated by position into the {@code "++_"}/{@code "_++"} symbols the grammar uses.
 * Out of scope: the cast/conversion {@code operator ()} (implicit at conversion sites).</p>
 */
public final class KlangOperatorUtil {

    private KlangOperatorUtil() {}

    /** Expression operator tokens that may be overloaded as {@code operator <sym>}. */
    public static final TokenSet OPERATOR_TOKENS = TokenSet.create(
            KlangTypes.OP_EQ, KlangTypes.OP_NEQ, KlangTypes.OP_LT, KlangTypes.OP_GT,
            KlangTypes.OP_LE, KlangTypes.OP_GE, KlangTypes.OP_SPACESHIP,
            KlangTypes.OP_PLUS, KlangTypes.OP_MINUS, KlangTypes.OP_STAR, KlangTypes.OP_DIV,
            KlangTypes.OP_PERCENT, KlangTypes.OP_POW,
            KlangTypes.OP_AMP, KlangTypes.OP_PIPE, KlangTypes.OP_CARET, KlangTypes.OP_TILDE,
            KlangTypes.OP_LSHIFT, KlangTypes.OP_RSHIFT,
            KlangTypes.OP_AND, KlangTypes.OP_OR, KlangTypes.OP_NOT,
            KlangTypes.OP_INC, KlangTypes.OP_DEC,
            KlangTypes.OP_ASSIGN, KlangTypes.OP_PLUS_ASSIGN, KlangTypes.OP_MINUS_ASSIGN,
            KlangTypes.OP_STAR_ASSIGN, KlangTypes.OP_DIV_ASSIGN, KlangTypes.OP_MOD_ASSIGN,
            KlangTypes.OP_AND_ASSIGN, KlangTypes.OP_OR_ASSIGN, KlangTypes.OP_XOR_ASSIGN,
            KlangTypes.OP_LSHIFT_ASSIGN, KlangTypes.OP_RSHIFT_ASSIGN);

    // ── Forward: an operator usage token → its overload declaration(s) ────────────

    /**
     * Resolves an operator token used in an expression to the {@code operator} overload(s) it
     * binds to: member operators of the operand's aggregate type (including inherited), then
     * visible free operator functions. Handles binary/unary operator tokens and the
     * {@code [}/{@code ]} of a subscript {@code a[i]}. Returns an empty list when {@code token}
     * is not an (overloadable) operator usage, belongs to an {@code operator} <em>declaration</em>,
     * or its operand type / overload cannot be inferred (fail soft).
     */
    public static @NotNull List<PsiElement> resolveOperatorUsage(@Nullable PsiElement token) {
        if (token == null || token.getFirstChild() != null || token.getNode() == null) {
            return Collections.emptyList(); // leaf tokens only
        }
        if (!(token.getContainingFile() instanceof KlangFile)) return Collections.emptyList();
        // Don't treat the operator symbol/brackets of an 'operator …' declaration head as a usage.
        if (PsiTreeUtil.getParentOfType(token, KlangOperatorFunctionHead.class) != null) {
            return Collections.emptyList();
        }

        IElementType type = token.getNode().getElementType();
        if (type == KlangTypes.PUNC_LBRACKET || type == KlangTypes.PUNC_RBRACKET) {
            return subscriptUsageTargets(token);
        }
        if (type == KlangTypes.OP_INC || type == KlangTypes.OP_DEC) {
            return incDecUsageTargets(token, type);
        }
        if (!OPERATOR_TOKENS.contains(type)) return Collections.emptyList();

        PsiElement operand = operandOf(token);
        if (operand == null) return Collections.emptyList();
        KlangAggregateDecl operandType = KlangTypeUtil.aggregateOfExpression(operand);
        if (operandType == null) return Collections.emptyList();
        return KlangResolveUtil.resolveOperator(token.getText(), operandType, token);
    }

    /** Resolves the {@code [}/{@code ]} of an indexing expression {@code a[i]} to its {@code operator[]}. */
    private static @NotNull List<PsiElement> subscriptUsageTargets(@NotNull PsiElement bracket) {
        KlangPostfixOp op = PsiTreeUtil.getParentOfType(bracket, KlangPostfixOp.class);
        if (op == null || bracket.getParent() != op) return Collections.emptyList(); // the op's own bracket
        if (op.getExpression() == null) return Collections.emptyList();               // only 'primary[index]'
        KlangAggregateDecl receiver = KlangTypeUtil.receiverTypeOfPostfixOp(op);
        if (receiver == null) return Collections.emptyList();
        return KlangResolveUtil.resolveOperator("[]", receiver, bracket);
    }

    /**
     * Resolves a {@code ++}/{@code --} usage to its overload, disambiguating prefix from postfix
     * by the token's position: a <em>postfix</em> {@code a++} ({@code KlangPostfixOp}) uses the
     * overload declared {@code operator _++} (symbol {@code "_++"}) on the type to its left; a
     * <em>prefix</em> {@code ++a} uses {@code operator ++_} (symbol {@code "++_"}) on its operand.
     */
    private static @NotNull List<PsiElement> incDecUsageTargets(@NotNull PsiElement token,
                                                                @NotNull IElementType type) {
        String base = type == KlangTypes.OP_INC ? "++" : "--";
        if (token.getParent() instanceof KlangPostfixOp op) {
            KlangAggregateDecl operandType = KlangTypeUtil.receiverTypeOfPostfixOp(op);
            if (operandType == null) return Collections.emptyList();
            return KlangResolveUtil.resolveOperator("_" + base, operandType, token); // postfix '_++' / '_--'
        }
        PsiElement operand = operandOf(token);
        if (operand == null) return Collections.emptyList();
        KlangAggregateDecl operandType = KlangTypeUtil.aggregateOfExpression(operand);
        if (operandType == null) return Collections.emptyList();
        return KlangResolveUtil.resolveOperator(base + "_", operandType, token); // prefix '++_' / '--_'
    }

    /**
     * The operand whose type selects the overload: the binary operator's <em>left</em> operand,
     * or (for a prefix unary operator) its single operand. Returns {@code null} for postfix
     * {@code ++}/{@code --} and other unsupported shapes.
     */
    private static @Nullable PsiElement operandOf(@NotNull PsiElement opToken) {
        PsiElement opNode = opToken;
        PsiElement container = opToken.getParent();
        if (container instanceof KlangAssignmentOperator) {
            // 'a += b' wraps the token in an assignmentOperator; operands surround that node.
            opNode = container;
            container = container.getParent();
        }
        if (container == null || container instanceof KlangPostfixOp) return null;

        int opStart = opNode.getTextRange().getStartOffset();
        int opEnd = opNode.getTextRange().getEndOffset();
        PsiElement left = null, right = null;
        for (PsiElement kid : container.getChildren()) { // getChildren() excludes leaf tokens
            if (kid.getTextRange().getEndOffset() <= opStart) {
                left = kid; // keep the nearest operand to the left
            } else if (right == null && kid.getTextRange().getStartOffset() >= opEnd) {
                right = kid;
            }
        }
        return left != null ? left : right;
    }

    // ── Reverse: an operator declaration → its usage tokens ───────────────────────

    /**
     * <b>Reverse navigation.</b> Finds every operator usage across the project that resolves back
     * to {@code operatorDecl}: each candidate token whose symbol matches the overload's is resolved
     * forward (via {@link #resolveOperatorUsage}) and kept when {@code operatorDecl} is among its
     * targets — so reverse is the exact inverse of forward. Project-wide via {@link FileTypeIndex};
     * falls back to the declaration's own file when the index is not ready (dumb mode). Returns the
     * usage tokens (binary/unary operator leaf, or the {@code [} of a subscript), suitable as
     * navigation targets.
     */
    public static @NotNull List<PsiElement> findOperatorUsages(@NotNull KlangFunctionDecl operatorDecl) {
        String want = operatorSymbolOf(operatorDecl);
        if (want == null) return Collections.emptyList();

        Project project = operatorDecl.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        List<PsiElement> result = new ArrayList<>();
        try {
            for (VirtualFile vf : FileTypeIndex.getFiles(KlangFileType.INSTANCE, GlobalSearchScope.allScope(project))) {
                collectUsagesInFile(psiManager.findFile(vf), operatorDecl, want, result);
            }
        } catch (IndexNotReadyException ignored) {
            collectUsagesInFile(operatorDecl.getContainingFile(), operatorDecl, want, result);
        }
        return result;
    }

    private static void collectUsagesInFile(@Nullable PsiFile file,
                                            @NotNull KlangFunctionDecl operatorDecl,
                                            @NotNull String want,
                                            @NotNull List<PsiElement> out) {
        if (!(file instanceof KlangFile)) return;
        for (PsiElement leaf : PsiTreeUtil.collectElements(file, e -> isUsageCandidate(e, want))) {
            if (resolveOperatorUsage(leaf).contains(operatorDecl)) out.add(leaf);
        }
    }

    /**
     * A leaf token that could be a usage of the operator with symbol {@code want}. For the
     * prefix/postfix {@code ++}/{@code --} forms (symbols {@code "++_"}/{@code "_++"}/…), the
     * usage token is a plain {@code ++}/{@code --}; the prefix-vs-postfix disambiguation is left
     * to {@link #resolveOperatorUsage}, so any {@code ++}/{@code --} of the right kind qualifies.
     */
    private static boolean isUsageCandidate(@NotNull PsiElement e, @NotNull String want) {
        if (e.getFirstChild() != null || e.getNode() == null) return false; // leaves only
        IElementType type = e.getNode().getElementType();
        if ("[]".equals(want)) return type == KlangTypes.PUNC_LBRACKET; // navigate from the opening bracket
        if (want.contains("++")) return type == KlangTypes.OP_INC;      // '++_' / '_++'
        if (want.contains("--")) return type == KlangTypes.OP_DEC;      // '--_' / '_--'
        return OPERATOR_TOKENS.contains(type) && normalize(e.getText()).equals(want);
    }

    /**
     * The operator symbol an {@code operator} overload declares: the normalized symbol text
     * (e.g. {@code "=="}, {@code "+="}) or {@code "[]"} for the subscript form; {@code null} when
     * {@code fn} is not an {@code operatorFunctionHead} overload (a plain function, destructor, or
     * the cast {@code operator ()}).
     */
    public static @Nullable String operatorSymbolOf(@NotNull KlangFunctionDecl fn) {
        KlangOperatorFunctionHead head = fn.getOperatorFunctionHead();
        if (head == null) return null;
        KlangOperatorSymbol sym = head.getOperatorSymbol();
        return sym != null ? normalize(sym.getText()) : "[]";
    }

    private static @NotNull String normalize(@NotNull String s) {
        return s.replaceAll("\\s+", "");
    }
}

