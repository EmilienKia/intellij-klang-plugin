package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.index.KlangModuleModel;
import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamespaceDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class KlangNavigationPresentation {

    private KlangNavigationPresentation() {
    }

    public static @NotNull String elementText(@NotNull PsiElement element) {
        if (element instanceof KlangFunctionDecl fn) return qualifiedFunctionLabel(fn);
        if (element instanceof KlangAggregateDecl agg) return qualifiedAggregateLabel(agg);
        String text = element.getText();
        return text != null ? text : "<element>";
    }

    public static @Nullable String containerText(@NotNull PsiElement element) {
        String kind = declarationKind(element);
        PsiFile file = element.getContainingFile();
        String fileName = file != null ? file.getName() : null;
        if (kind != null && fileName != null) return kind + " • " + fileName;
        return kind != null ? kind : fileName;
    }

    public static @Nullable Icon icon(@NotNull PsiElement element) {
        if (element instanceof KlangAggregateDecl agg) return aggregateIcon(agg);
        if (element instanceof KlangFunctionDecl fn) return functionIcon(fn);
        return null;
    }

    private static @NotNull String qualifiedFunctionLabel(@NotNull KlangFunctionDecl fn) {
        String label = KlangDeclarationLabels.functionLabel(fn);
        List<String> prefix = qualifiedPrefix(fn);
        return prefix.isEmpty() ? label : String.join("::", prefix) + "::" + label;
    }

    private static @NotNull String qualifiedAggregateLabel(@NotNull KlangAggregateDecl agg) {
        String name = agg.getName() != null ? agg.getName() : "<type>";
        List<String> prefix = qualifiedPrefix(agg.getParent());
        return prefix.isEmpty() ? name : String.join("::", prefix) + "::" + name;
    }

    private static @NotNull List<String> qualifiedPrefix(@Nullable PsiElement anchor) {
        List<String> parts = new ArrayList<>();

        PsiFile file = anchor != null ? anchor.getContainingFile() : null;
        if (file instanceof KlangFile kf) {
            String module = KlangModuleModel.getInstance(kf.getProject()).moduleNameOf(kf);
            if (!module.isEmpty()) {
                for (String seg : module.split("::")) {
                    if (!seg.isEmpty()) parts.add(seg);
                }
            }
        }

        Deque<String> lexical = new ArrayDeque<>();
        PsiElement current = anchor;
        while (current != null) {
            if (current instanceof KlangNamespaceDecl ns && ns.getName() != null) {
                lexical.addFirst(ns.getName());
            } else if (current instanceof KlangAggregateDecl agg && agg.getName() != null) {
                lexical.addFirst(agg.getName());
            }
            current = current.getParent();
        }
        parts.addAll(lexical);
        return parts;
    }

    private static @Nullable String declarationKind(@NotNull PsiElement element) {
        if (element instanceof KlangAggregateDecl agg) {
            String kw = KlangDeclarationLabels.aggregateKeyword(agg);
            return kw != null ? kw : "struct";
        }
        if (element instanceof KlangFunctionDecl fn) {
            KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(fn);
            if (owner != null) {
                String kw = KlangDeclarationLabels.aggregateKeyword(owner);
                return (kw != null ? kw : "struct") + " member";
            }
            return "function";
        }
        return null;
    }

    private static @NotNull Icon aggregateIcon(@NotNull KlangAggregateDecl agg) {
        String kw = KlangDeclarationLabels.aggregateKeyword(agg);
        if ("interface".equals(kw)) return KlangUi.Icons.AGGREGATE_INTERFACE;
        if ("annotation".equals(kw)) return KlangUi.Icons.AGGREGATE_ANNOTATION;
        return KlangUi.Icons.AGGREGATE_CLASS;
    }

    private static @NotNull Icon functionIcon(@NotNull KlangFunctionDecl fn) {
        if (isConstructorOrDestructor(fn)) return KlangUi.Icons.FUNCTION_CTOR_DTOR;
        return KlangUi.Icons.FUNCTION_METHOD;
    }

    private static boolean isConstructorOrDestructor(@NotNull KlangFunctionDecl fn) {
        if (fn.getNode().findChildByType(KlangTypes.DESTRUCTOR_HEAD) != null) {
            return true;
        }
        var head = fn.getNode().findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head == null) return false;
        var id = head.findChildByType(KlangTypes.IDENTIFIER);
        if (id == null) return false;
        KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(fn);
        return owner != null && id.getText().equals(owner.getName());
    }
}
