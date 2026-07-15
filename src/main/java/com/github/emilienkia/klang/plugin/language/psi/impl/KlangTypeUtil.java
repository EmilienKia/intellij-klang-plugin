package com.github.emilienkia.klang.plugin.language.psi.impl;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Minimal, best-effort static type inference for K-lang expressions — just enough to
 * support member-access resolution (§10). It maps an expression to the
 * {@link KlangAggregateDecl} that is its static (aggregate) type, or {@code null} when
 * the type is not an aggregate or cannot be determined.
 *
 * <p>This is intentionally partial: it fails soft (returns {@code null}) rather than
 * guessing, so navigation is never wrong — at worst it is absent. Full type checking is
 * out of scope.</p>
 */
public final class KlangTypeUtil {

    private KlangTypeUtil() {}

    /**
     * The aggregate type of the <em>receiver</em> of a member access, i.e. the type of the
     * expression to the left of the {@code .}/{@code ->} whose right-hand side is
     * {@code member}. Returns {@code null} if the receiver is not (or cannot be inferred
     * to be) an aggregate.
     */
    public static @Nullable KlangAggregateDecl receiverTypeOfMember(@NotNull PsiElement member) {
        return asAggregate(receiverNominalTypeOfMember(member));
    }

    /**
     * Like {@link #receiverTypeOfMember} but returns the receiver's nominal type as a raw
     * container — a {@link KlangAggregateDecl} <em>or</em> a {@link KlangUnionDecl}. This lets
     * member access on a <em>union</em> value (e.g. {@code _storage.result} where
     * {@code _storage : Storage} is a union) be resolved against the union's members. Returns
     * {@code null} when the receiver type is not a nominal aggregate/union.
     */
    public static @Nullable PsiElement receiverNominalTypeOfMember(@NotNull PsiElement member) {
        PsiElement op = member.getParent();
        if (!(op instanceof KlangPostfixOp postfixOp)) return null;
        PsiElement pe = postfixOp.getParent();
        if (!(pe instanceof KlangPostfixExpr postfix)) return null;
        int idx = postfix.getPostfixOpList().indexOf(postfixOp);
        if (idx < 0) return null;
        return foldPrefixNominalType(postfix, idx);
    }

    /**
     * The written type spec of the <em>receiver</em> of a member access — the type of the
     * expression to the left of the {@code .}/{@code ->}. Unlike
     * {@link #receiverNominalTypeOfMember}, this preserves non-nominal traits such as array-ness
     * (e.g. {@code unsigned short[]!}), so callers can recognise the array virtual member
     * {@code size} (§9.8). Returns {@code null} when the receiver type cannot be inferred.
     */
    public static @Nullable KlangTypeSpec receiverTypeSpecOfMember(@NotNull PsiElement member) {
        PsiElement op = member.getParent();
        if (!(op instanceof KlangPostfixOp postfixOp)) return null;
        PsiElement pe = postfixOp.getParent();
        if (!(pe instanceof KlangPostfixExpr postfix)) return null;
        int idx = postfix.getPostfixOpList().indexOf(postfixOp);
        if (idx < 0) return null;
        return foldPrefixTypeSpec(postfix, idx);
    }

    /**
     * Whether {@code member} is the array virtual member {@code size} (§9.8) — i.e. the member
     * name is {@code size} and the receiver's inferred type is an array ({@code T[]} / {@code
     * T[N]}, possibly behind an owner/pointer/link/view suffix). Arrays expose {@code size} as
     * their only (read-only) member; it has no declaration to navigate to, so the unresolved
     * inspection must not flag it.
     */
    public static boolean isArraySizeMember(@NotNull PsiElement member) {
        if (!"size".equals(KlangResolveUtil.lastSegment(member.getText().trim()))) return false;
        return isArrayTypeSpec(receiverTypeSpecOfMember(member));
    }

    /** True when {@code ts} carries an array suffix ({@code [ ]} / {@code [ N ]}). */
    static boolean isArrayTypeSpec(@Nullable KlangTypeSpec ts) {
        if (ts == null) return false;
        for (KlangTypeSuffix suffix : ts.getTypeSuffixList()) {
            ASTNode first = suffix.getNode().getFirstChildNode();
            if (first != null && first.getElementType() == KlangTypes.PUNC_LBRACKET) return true;
        }
        return false;
    }

    /**
     * The aggregate type of the prefix that a {@code postfixOp} applies to — i.e. the type of
     * everything to the left of this operation in its {@code postfixExpr} chain. Used both for
     * member access ({@code a.b} → type of {@code a}) and for the subscript operator
     * ({@code a[i]} → type of {@code a}, the object being indexed). Returns {@code null} when
     * the prefix is not (or cannot be inferred to be) an aggregate.
     */
    public static @Nullable KlangAggregateDecl receiverTypeOfPostfixOp(@NotNull KlangPostfixOp postfixOp) {
        PsiElement pe = postfixOp.getParent();
        if (!(pe instanceof KlangPostfixExpr postfix)) return null;
        int idx = postfix.getPostfixOpList().indexOf(postfixOp);
        if (idx < 0) return null;
        return foldPrefixType(postfix, idx);
    }

    /**
     * Folds {@code primaryExpr} followed by {@code postfixOps[0..uptoExclusive)} into the
     * aggregate type they evaluate to. (Aggregate-only view of {@link #foldPrefixNominalType}.)
     */
    static @Nullable KlangAggregateDecl foldPrefixType(@NotNull KlangPostfixExpr postfix, int uptoExclusive) {
        return asAggregate(foldPrefixNominalType(postfix, uptoExclusive));
    }

    /**
     * Folds {@code primaryExpr} followed by {@code postfixOps[0..uptoExclusive)} into the nominal
     * type they evaluate to — a {@link KlangAggregateDecl} or a {@link KlangUnionDecl}.
     */
    static @Nullable PsiElement foldPrefixNominalType(@NotNull KlangPostfixExpr postfix, int uptoExclusive) {
        return foldPrefix(postfix, uptoExclusive).nominal;
    }

    /**
     * Like {@link #foldPrefixNominalType} but returns the receiver value's declared
     * {@link KlangTypeSpec} (e.g. {@code unsigned short[]!}), so callers can inspect non-nominal
     * traits such as array-ness. {@code null} when the type cannot be inferred (or is {@code this},
     * which has no written type spec).
     */
    static @Nullable KlangTypeSpec foldPrefixTypeSpec(@NotNull KlangPostfixExpr postfix, int uptoExclusive) {
        return foldPrefix(postfix, uptoExclusive).typeSpec;
    }

    /** The inferred type of a folded prefix: its nominal container and/or its written type spec. */
    private record PrefixType(@Nullable PsiElement nominal, @Nullable KlangTypeSpec typeSpec) {
        static final PrefixType NONE = new PrefixType(null, null);
    }

    private static @NotNull PrefixType foldPrefix(@NotNull KlangPostfixExpr postfix, int uptoExclusive) {
        KlangPrimaryExpr primary = postfix.getPrimaryExpr();
        List<KlangPostfixOp> ops = postfix.getPostfixOpList();

        PsiElement cur = null;                  // current nominal type (aggregate or union)
        KlangTypeSpec curTs = null;             // current value's written type spec (for array, …)
        KlangFunctionDecl pendingCallable = null;

        if (isThis(primary)) {
            cur = enclosingAggregate(primary);  // 'this' has a nominal type but no written type spec
        } else if (primary.getIdentifierExpr() != null) {
            PsiElement decl = firstResolved(primary.getIdentifierExpr());
            if (decl instanceof KlangFunctionDecl fn) pendingCallable = fn;
            else if (decl != null) { curTs = typeSpecOf(decl); cur = nominalTypeOfTypeSpec(curTs, decl); }
        }

        for (int i = 0; i < uptoExclusive && i < ops.size(); i++) {
            KlangPostfixOp op = ops.get(i);
            if (op.getIdentifierExpr() != null) {
                // member access: '.' / '->' name
                if (cur == null) return PrefixType.NONE;   // can't descend (e.g. array / fundamental)
                String name = KlangResolveUtil.lastSegment(op.getIdentifierExpr().getText());
                PsiElement member = firstMember(cur, name, op);
                if (member instanceof KlangFunctionDecl fn) {
                    pendingCallable = fn;
                    cur = null;
                    curTs = null;
                } else if (member != null) {
                    // aggregate field or union member — its declared type drives the next step
                    curTs = typeSpecOf(member);
                    cur = nominalTypeOfTypeSpec(curTs, member);
                    pendingCallable = null;
                } else {
                    return PrefixType.NONE;
                }
            } else if (firstTokenType(op) == KlangTypes.PUNC_LPAREN) {
                // call: applies to a pending callable (method / free function)
                if (pendingCallable == null) return PrefixType.NONE;
                curTs = returnTypeSpecOf(pendingCallable);
                cur = curTs == null ? null : nominalTypeOfTypeSpec(curTs, pendingCallable);
                pendingCallable = null;
            }
            // '[...]' index, '++'/'--'/'...' and brace-init postfix ops leave cur unchanged (best effort).
        }
        return new PrefixType(cur, curTs);
    }

    // ── Type of declarations / type specs ─────────────────────────────────────

    /**
     * The aggregate type an arbitrary expression evaluates to, when it can be inferred —
     * just enough to support operator-overload navigation. Only a single {@code postfixExpr}
     * "chain" is understood (a variable, {@code this}, field/method access, calls); compound
     * sub-expressions (binary operators, casts, parenthesised groups) fail soft and return
     * {@code null}.
     */
    public static @Nullable KlangAggregateDecl aggregateOfExpression(@Nullable PsiElement expr) {
        KlangPostfixExpr pe = wholePostfix(expr);
        return pe == null ? null : asAggregate(foldPrefixNominalType(pe, pe.getPostfixOpList().size()));
    }

    /**
     * Descends a single-child expression chain ({@code assignmentExpr → … → postfixExpr})
     * down to its {@code postfixExpr}. Returns {@code null} as soon as a level has more than
     * one composite child (i.e. an actual operator/cast is present), so only "atomic"
     * operands are resolved.
     */
    static @Nullable KlangPostfixExpr wholePostfix(@Nullable PsiElement expr) {
        PsiElement cur = expr;
        while (cur != null && !(cur instanceof KlangPostfixExpr)) {
            PsiElement[] kids = cur.getChildren();
            if (kids.length != 1) return null;
            cur = kids[0];
        }
        return (KlangPostfixExpr) cur;
    }

    /** Resolves the aggregate named by a {@code typeSpec}'s qualified identifier, if any. */
    public static @Nullable KlangAggregateDecl aggregateOfTypeSpec(@Nullable KlangTypeSpec typeSpec,
                                                                   @NotNull PsiElement anchor) {
        return asAggregate(nominalTypeOfTypeSpec(typeSpec, anchor));
    }

    /**
     * Resolves the nominal type named by a {@code typeSpec}'s qualified identifier — a
     * {@link KlangAggregateDecl} or a {@link KlangUnionDecl} — or {@code null} for a fundamental
     * type / unresolvable name.
     */
    public static @Nullable PsiElement nominalTypeOfTypeSpec(@Nullable KlangTypeSpec typeSpec,
                                                             @NotNull PsiElement anchor) {
        if (typeSpec == null) return null;
        KlangQualifiedIdentifier qid = typeSpec.getQualifiedIdentifier();
        if (qid == null) return null; // fundamental type (int, float, …) — not a nominal type
        KlangAggregateDecl viaConstructor = null;
        for (PsiElement el : KlangResolveUtil.resolve(anchor, qid.getText().trim())) {
            if (el instanceof KlangAggregateDecl agg) return agg;
            if (el instanceof KlangUnionDecl union) return union;
            // A type name may resolve to a *constructor* rather than the type itself when the
            // lexical climb passes through the aggregate's own scope: constructors are member
            // functions that share the aggregate's simple name (e.g. `Shared()` inside
            // `struct Shared`), so they shadow the enclosing type declaration. Treat such a
            // constructor result as denoting its owning aggregate — this is what enables member
            // access on a value of the enclosing type (e.g. `other._obs` where
            // `other : Shared<T>&` inside `Shared`).
            if (viaConstructor == null && el instanceof KlangFunctionDecl fn) {
                KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(fn);
                if (owner != null && fn.getName() != null && fn.getName().equals(owner.getName())) {
                    viaConstructor = owner;
                }
            }
        }
        return viaConstructor;
    }

    private static @Nullable KlangTypeSpec typeSpecOf(@NotNull PsiElement decl) {
        if (decl instanceof KlangVariableDecl v)        return v.getTypeSpec();
        if (decl instanceof KlangParameterSpec p)       return p.getTypeSpec();
        if (decl instanceof KlangNamedReturnVar r)      return r.getTypeSpec();
        if (decl instanceof KlangIfCondVarDecl c)       return c.getTypeSpec();
        if (decl instanceof KlangCatchParameterDecl c)  return c.getTypeSpec();
        if (decl instanceof KlangUnionMemberDecl u)     return u.getTypeSpec();
        return null;
    }

    /** The written return type spec of a function (its return type or named return variable). */
    private static @Nullable KlangTypeSpec returnTypeSpecOf(@NotNull KlangFunctionDecl fn) {
        KlangReturnTypeOrMemberInitList rt = fn.getReturnTypeOrMemberInitList();
        if (rt != null && rt.getTypeSpec() != null) return rt.getTypeSpec();
        KlangNamedReturnVar nrv = fn.getNamedReturnVar();
        return nrv != null ? nrv.getTypeSpec() : null;
    }

    /** The aggregate that lexically encloses {@code from} (the owner of {@code this}). */
    static @Nullable KlangAggregateDecl enclosingAggregate(@NotNull PsiElement from) {
        return PsiTreeUtil.getParentOfType(from, KlangAggregateDecl.class);
    }

    /**
     * The aggregate type that a {@code braceInitList} initialises, for designated
     * initializers ({@code .field = …}). Climbs to the nearest initialization context —
     * a variable/if-condition-variable initializer or a {@code new Type {…}} — and returns
     * its declared type. Best-effort: returns {@code null} when the context is not a plain
     * aggregate initialization (e.g. the brace list is a function argument).
     */
    public static @Nullable KlangAggregateDecl braceInitTargetAggregate(@Nullable PsiElement braceInitList) {
        if (braceInitList == null) return null;
        for (PsiElement cur = braceInitList.getParent();
             cur != null && !(cur instanceof KlangFile);
             cur = cur.getParent()) {
            if (cur instanceof KlangInitialiser ini && ini.getParent() instanceof KlangVariableDecl v) {
                return aggregateOfTypeSpec(v.getTypeSpec(), v);
            }
            if (cur instanceof KlangCondVarInitialiser cvi && cvi.getParent() instanceof KlangIfCondVarDecl c) {
                return aggregateOfTypeSpec(c.getTypeSpec(), c);
            }
            if (cur instanceof KlangNewExpr ne) {
                KlangTypeName tn = ne.getTypeName();
                if (tn != null && tn.getQualifiedIdentifier() != null) {
                    for (PsiElement el : KlangResolveUtil.resolve(tn, tn.getQualifiedIdentifier().getText().trim())) {
                        if (el instanceof KlangAggregateDecl agg) return agg;
                    }
                }
                return null;
            }
        }
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static @Nullable PsiElement firstResolved(@NotNull KlangIdentifierExpr idExpr) {
        List<PsiElement> r = KlangResolveUtil.resolve(idExpr, idExpr.getText().trim());
        return r.isEmpty() ? null : r.get(0);
    }

    private static @Nullable PsiElement firstMember(@NotNull PsiElement container,
                                                    @NotNull String name,
                                                    @NotNull PsiElement anchor) {
        if (container instanceof KlangAggregateDecl owner) {
            List<PsiElement> m = KlangResolveUtil.resolveMember(owner, name, anchor);
            return m.isEmpty() ? null : m.get(0);
        }
        if (container instanceof KlangUnionDecl union) {
            return KlangResolveUtil.resolveUnionMember(union, name);
        }
        return null;
    }

    /** Narrows a nominal type to a {@link KlangAggregateDecl}, or {@code null} (e.g. a union). */
    private static @Nullable KlangAggregateDecl asAggregate(@Nullable PsiElement nominal) {
        return nominal instanceof KlangAggregateDecl agg ? agg : null;
    }

    private static boolean isThis(@NotNull KlangPrimaryExpr primary) {
        return primary.getNode().findChildByType(KlangTypes.KW_THIS) != null;
    }

    private static @Nullable com.intellij.psi.tree.IElementType firstTokenType(@NotNull PsiElement el) {
        ASTNode n = el.getNode().getFirstChildNode();
        while (n != null && n.getElementType() == TokenType.WHITE_SPACE) n = n.getTreeNext();
        return n == null ? null : n.getElementType();
    }
}



