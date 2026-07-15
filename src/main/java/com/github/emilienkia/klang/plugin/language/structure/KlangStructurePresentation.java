package com.github.emilienkia.klang.plugin.language.structure;

import com.github.emilienkia.klang.plugin.language.KlangDeclarationLabels;
import com.github.emilienkia.klang.plugin.language.KlangUi;
import com.github.emilienkia.klang.plugin.language.psi.*;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.RowIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Produces the label text and icon shown for a K-lang declaration in the structure view.
 *
 * <p>Unlike the breadcrumbs (which prefix a keyword, e.g. {@code struct Point}), the outline
 * shows the bare name and conveys the declaration kind through the icon — matching the usual
 * IntelliJ structure-view convention. Variables and union members add their type
 * ({@code name : type}). Function labels reuse {@link KlangDeclarationLabels#functionLabel}.</p>
 */
final class KlangStructurePresentation {

    private KlangStructurePresentation() {
    }

    static @NotNull String text(@NotNull PsiElement element) {
        if (element instanceof KlangNamespaceDecl ns) {
            String name = ns.getName();
            return name != null ? name : "<namespace>";
        }
        if (element instanceof KlangAggregateDecl agg) {
            return orAnonymous(agg.getName());
        }
        if (element instanceof KlangEnumDecl en) {
            return orAnonymous(en.getName());
        }
        if (element instanceof KlangUnionDecl un) {
            return orAnonymous(un.getName());
        }
        if (element instanceof KlangFunctionDecl) {
            return KlangDeclarationLabels.functionLabel(element);
        }
        if (element instanceof KlangVariableDecl var) {
            return field(var.getName(), var.getTypeSpec());
        }
        if (element instanceof KlangEnumEntry e) {
            return orAnonymous(e.getName());
        }
        if (element instanceof KlangUnionMemberDecl m) {
            return field(m.getName(), m.getTypeSpec());
        }
        return element.getText();
    }

    static @Nullable String location(@NotNull PsiElement element) {
        if (element instanceof KlangAggregateDecl agg) {
            LinkedHashSet<String> roots = rootAggregateNames(agg);
            return roots.isEmpty() ? null : "base: " + String.join(", ", roots);
        }
        if (element instanceof KlangFunctionDecl fn) {
            LinkedHashSet<String> roots = rootMethodOwnerNames(fn);
            return roots.isEmpty() ? null : "base: " + String.join(", ", roots);
        }
        return null;
    }

    static @NotNull Icon icon(@NotNull PsiElement element) {
        Icon base = baseIcon(element);
        List<Icon> decorations = new ArrayList<>();

        Icon visibility = visibilityIcon(element);
        if (visibility != null) decorations.add(visibility);
        if (hasSpecifier(element, "static")) decorations.add(KlangUi.Icons.STATIC_MARK);
        if (hasSpecifier(element, "final")) decorations.add(KlangUi.Icons.FINAL_MARK);
        if (!(element instanceof KlangFunctionDecl) && hasSpecifier(element, "override")) {
            decorations.add(KlangUi.Icons.OVERRIDE_MARK);
        }

        if (decorations.isEmpty()) return base;

        RowIcon row = new RowIcon(1 + decorations.size());
        row.setIcon(base, 0);
        for (int i = 0; i < decorations.size(); i++) {
            row.setIcon(decorations.get(i), i + 1);
        }
        return row;
    }

    private static @NotNull Icon baseIcon(@NotNull PsiElement element) {
        if (element instanceof KlangNamespaceDecl)   return KlangUi.Icons.NAMESPACE;
        if (element instanceof KlangAggregateDecl agg) return aggregateIcon(agg);
        if (element instanceof KlangEnumDecl)        return KlangUi.Icons.ENUM;
        if (element instanceof KlangUnionDecl)       return KlangUi.Icons.AGGREGATE_CLASS;
        if (element instanceof KlangFunctionDecl fn) return functionIcon(fn);
        if (element instanceof KlangVariableDecl)    return KlangUi.Icons.FIELD;
        if (element instanceof KlangEnumEntry)       return KlangUi.Icons.FIELD;
        if (element instanceof KlangUnionMemberDecl) return KlangUi.Icons.FIELD;
        return KlangUi.Icons.UNKNOWN;
    }

    private static @Nullable Icon visibilityIcon(@NotNull PsiElement element) {
        return switch (effectiveVisibility(element)) {
            case PUBLIC -> KlangUi.Icons.VISIBILITY_PUBLIC;
            case PROTECTED -> KlangUi.Icons.VISIBILITY_PROTECTED;
            case PRIVATE -> KlangUi.Icons.VISIBILITY_PRIVATE;
            case NONE -> null;
        };
    }

    private static @NotNull Visibility effectiveVisibility(@NotNull PsiElement element) {
        Visibility explicit = explicitVisibility(element);
        if (explicit != Visibility.NONE) return explicit;

        PsiElement parent = element.getParent();
        if (!(parent instanceof KlangDeclaration currentDecl)) return Visibility.NONE;

        for (KlangDeclaration decl = PsiTreeUtil.getPrevSiblingOfType(currentDecl, KlangDeclaration.class);
             decl != null;
             decl = PsiTreeUtil.getPrevSiblingOfType(decl, KlangDeclaration.class)) {
            KlangVisibilityDecl visibilityDecl = decl.getVisibilityDecl();
            if (visibilityDecl != null) {
                return parseVisibilityKeyword(visibilityDecl.getText());
            }
        }
        return Visibility.NONE;
    }

    private static @NotNull Visibility explicitVisibility(@NotNull PsiElement element) {
        if (hasSpecifier(element, "public")) return Visibility.PUBLIC;
        if (hasSpecifier(element, "protected")) return Visibility.PROTECTED;
        if (hasSpecifier(element, "private")) return Visibility.PRIVATE;
        return Visibility.NONE;
    }

    private static boolean hasSpecifier(@NotNull PsiElement element, @NotNull String keyword) {
        for (KlangSpecifier specifier : specifiers(element)) {
            if (keyword.equals(specifier.getText())) return true;
        }
        return false;
    }

    private static @NotNull List<KlangSpecifier> specifiers(@NotNull PsiElement element) {
        if (element instanceof KlangAggregateDecl agg) return agg.getSpecifierList();
        if (element instanceof KlangEnumDecl en) return en.getSpecifierList();
        if (element instanceof KlangUnionDecl un) return un.getSpecifierList();
        if (element instanceof KlangFunctionDecl fn) return fn.getSpecifierList();
        if (element instanceof KlangVariableDecl var) return var.getSpecifierList();
        return List.of();
    }

    private static @NotNull LinkedHashSet<String> rootAggregateNames(@NotNull KlangAggregateDecl agg) {
        List<KlangAggregateDecl> directBases = KlangResolveUtil.directBases(agg);
        if (directBases.isEmpty()) return new LinkedHashSet<>();

        LinkedHashSet<KlangAggregateDecl> roots = new LinkedHashSet<>();
        Set<KlangAggregateDecl> seen = new LinkedHashSet<>();
        Deque<KlangAggregateDecl> queue = new ArrayDeque<>(directBases);
        seen.add(agg);
        while (!queue.isEmpty()) {
            KlangAggregateDecl current = queue.poll();
            if (!seen.add(current)) continue;
            List<KlangAggregateDecl> parents = KlangResolveUtil.directBases(current);
            if (parents.isEmpty()) {
                roots.add(current);
            } else {
                queue.addAll(parents);
            }
        }
        if (roots.isEmpty()) {
            roots.addAll(directBases);
        }
        return toNames(roots);
    }

    private static @NotNull LinkedHashSet<String> rootMethodOwnerNames(@NotNull KlangFunctionDecl method) {
        List<KlangFunctionDecl> directBases = KlangResolveUtil.findOverriddenMethods(method);
        if (directBases.isEmpty()) return new LinkedHashSet<>();

        LinkedHashSet<KlangFunctionDecl> roots = new LinkedHashSet<>();
        Set<KlangFunctionDecl> seen = new LinkedHashSet<>();
        Deque<KlangFunctionDecl> queue = new ArrayDeque<>(directBases);
        seen.add(method);
        while (!queue.isEmpty()) {
            KlangFunctionDecl current = queue.poll();
            if (!seen.add(current)) continue;
            List<KlangFunctionDecl> parents = KlangResolveUtil.findOverriddenMethods(current);
            if (parents.isEmpty()) {
                roots.add(current);
            } else {
                queue.addAll(parents);
            }
        }
        if (roots.isEmpty()) {
            roots.addAll(directBases);
        }

        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (KlangFunctionDecl root : roots) {
            KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(root);
            if (owner != null) names.add(orAnonymous(owner.getName()));
        }
        return names;
    }

    private static @NotNull LinkedHashSet<String> toNames(@NotNull Iterable<KlangAggregateDecl> aggregates) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (KlangAggregateDecl agg : aggregates) {
            names.add(orAnonymous(agg.getName()));
        }
        return names;
    }

    private static @NotNull Visibility parseVisibilityKeyword(@Nullable String text) {
        if (text == null) return Visibility.NONE;
        if (text.startsWith("public")) return Visibility.PUBLIC;
        if (text.startsWith("protected")) return Visibility.PROTECTED;
        if (text.startsWith("private")) return Visibility.PRIVATE;
        return Visibility.NONE;
    }

    private enum Visibility { PUBLIC, PROTECTED, PRIVATE, NONE }

    private static @NotNull Icon aggregateIcon(@NotNull KlangAggregateDecl agg) {
        String kw = KlangDeclarationLabels.aggregateKeyword(agg);
        if ("interface".equals(kw))  return KlangUi.Icons.AGGREGATE_INTERFACE;
        if ("annotation".equals(kw)) return KlangUi.Icons.AGGREGATE_ANNOTATION;
        // struct / class (and any fallback)
        return KlangUi.Icons.AGGREGATE_CLASS;
    }

    private static @NotNull Icon functionIcon(@NotNull KlangFunctionDecl fn) {
        if (isConstructorOrDestructor(fn)) return KlangUi.Icons.FUNCTION_CTOR_DTOR;

        List<KlangFunctionDecl> overridden = KlangResolveUtil.findOverriddenMethods(fn);
        if (!overridden.isEmpty()) {
            boolean implementing = overridden.stream().anyMatch(KlangStructurePresentation::isInterfaceMethod);
            return implementing
                    ? KlangUi.Icons.GUTTER_INHERITANCE_IMPLEMENTING_METHOD
                    : KlangUi.Icons.GUTTER_INHERITANCE_OVERRIDING_METHOD;
        }
        return KlangUi.Icons.FUNCTION_METHOD;
    }

    private static boolean isConstructorOrDestructor(@NotNull KlangFunctionDecl fn) {
        if (fn.getNode().findChildByType(KlangTypes.DESTRUCTOR_HEAD) != null) return true;
        var head = fn.getNode().findChildByType(KlangTypes.FUNCTION_HEAD);
        if (head == null) return false;
        var id = head.findChildByType(KlangTypes.IDENTIFIER);
        if (id == null) return false;
        KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(fn);
        return owner != null && id.getText().equals(owner.getName());
    }

    private static boolean isInterfaceMethod(@NotNull KlangFunctionDecl method) {
        KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(method);
        return owner != null && owner.getNode().findChildByType(KlangTypes.KW_INTERFACE) != null;
    }

    /** {@code name : type}, gracefully degrading when either part is missing. */
    private static @NotNull String field(@Nullable String name, @Nullable KlangTypeSpec type) {
        String n = orAnonymous(name);
        String t = type != null ? normalizeSpace(type.getText()) : null;
        return (t != null && !t.isEmpty()) ? n + " : " + t : n;
    }

    private static @NotNull String orAnonymous(@Nullable String name) {
        return name != null ? name : "<anonymous>";
    }

    /** Collapses internal whitespace runs to single spaces so multi-line types stay on one line. */
    private static @NotNull String normalizeSpace(@NotNull String text) {
        return text.trim().replaceAll("\\s+", " ");
    }
}
