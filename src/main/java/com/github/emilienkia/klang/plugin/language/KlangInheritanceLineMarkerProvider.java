package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * <b>B1 — upward inheritance gutter markers (file-local).</b>
 *
 * <p>Adds <em>"overrides / implements method"</em> gutter icons on a method that redefines a
 * base-class method, and a <em>"go to super type(s)"</em> icon on an aggregate that derives
 * from one or more base aggregates. Navigation is usage → definition (upward), relying on the
 * existing resolution engine ({@link KlangResolveUtil#findOverriddenMethods} /
 * {@link KlangResolveUtil#directBases}) — no project-wide index is required.</p>
 *
 * <p><b>B3 — downward inheritance gutter markers (project-wide).</b> Adds <em>"overridden /
 * implemented by"</em> icons on a base method and a <em>"subclassed / implemented by"</em> icon
 * on a base aggregate, by searching the <em>whole project</em> for derived types / overriding
 * methods ({@link KlangResolveUtil#findOverridingMethods} / {@link KlangResolveUtil#findSubAggregates},
 * narrowed by {@code KlangBaseNameIndex}, transitively over the derivation relation). Degrades to
 * a file-local scan when the index is not ready.</p>
 *
 * <p>Markers are attached to the leaf name {@code IDENTIFIER} of the declaration, as required by
 * the platform's "line marker must target a leaf element" contract.</p>
 */
public final class KlangInheritanceLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public @NotNull String getName() {
        return KlangUi.Text.MARKER_PROVIDER_INHERITANCE;
    }

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Only react on leaf identifier tokens (the declaration's name carrier).
        if (element.getNode() == null || element.getNode().getElementType() != KlangTypes.IDENTIFIER) {
            return;
        }

        // Method name → overridden base method(s) (up, B1) and overriding method(s) (down, B2).
        KlangFunctionDecl func = PsiTreeUtil.getParentOfType(element, KlangFunctionDecl.class);
        if (func != null && element.equals(func.getNameIdentifier())) {
            // Constructors/destructors are currently out of inheritance/override markers scope.
            // They have their own dedicated gutter marker provider.
            if (isConstructorOrDestructor(func)) {
                return;
            }

            List<KlangFunctionDecl> overridden = KlangResolveUtil.findOverriddenMethods(func);
            List<KlangFunctionDecl> overriders = KlangResolveUtil.findOverridingMethods(func);

            addVirtualOrAbstractMarker(func, overridden, overriders, element, result);
            addMethodMarker(overridden, element, result);
            addOverriddenByMarker(func, overriders, element, result);
            return;
        }

        // Aggregate name → base aggregate(s) (up, B1) and derived aggregate(s) (down, B2).
        KlangAggregateDecl agg = PsiTreeUtil.getParentOfType(element, KlangAggregateDecl.class);
        if (agg != null && element.equals(agg.getNameIdentifier())) {
            addAggregateMarker(agg, element, result);
            addSubtypesMarker(agg, element, result);
        }
    }

    private static void addMethodMarker(@NotNull List<KlangFunctionDecl> overridden,
                                        @NotNull PsiElement anchor,
                                        @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (overridden.isEmpty()) return;

        boolean implementing = overridden.stream().anyMatch(KlangInheritanceLineMarkerProvider::isAbstractInterfaceMethod);
        RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                .create(implementing
                        ? KlangUi.Icons.GUTTER_INHERITANCE_IMPLEMENTING_METHOD
                        : KlangUi.Icons.GUTTER_INHERITANCE_OVERRIDING_METHOD)
                .setTargets(overridden)
                .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                .setTooltipText(implementing
                        ? KlangUi.Text.TOOLTIP_IMPLEMENTS_BASE_METHOD
                        : KlangUi.Text.TOOLTIP_OVERRIDES_BASE_METHOD)
                .createLineMarkerInfo(anchor);
        result.add(info);
    }

    private static void addOverriddenByMarker(@NotNull KlangFunctionDecl func,
                                              @NotNull List<KlangFunctionDecl> overriders,
                                              @NotNull PsiElement anchor,
                                              @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (overriders.isEmpty()) return;

        boolean inInterface = isAbstractInterfaceMethod(func);
        RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                .create(inInterface
                        ? KlangUi.Icons.GUTTER_INHERITANCE_IMPLEMENTED_METHOD
                        : KlangUi.Icons.GUTTER_INHERITANCE_OVERRIDDEN_METHOD)
                .setTargets(overriders)
                .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                .setTooltipText(inInterface
                        ? KlangUi.Text.TOOLTIP_IMPLEMENTED_IN_SUBTYPES
                        : KlangUi.Text.TOOLTIP_OVERRIDDEN_IN_SUBTYPES)
                .createLineMarkerInfo(anchor);
        result.add(info);
    }

    /**
     * Adds semantic method markers for declarations that are abstract/virtual by language rules.
     * These markers are informative (their target is the declaration itself).
     */
    private static void addVirtualOrAbstractMarker(@NotNull KlangFunctionDecl func,
                                                   @NotNull List<KlangFunctionDecl> overridden,
                                                   @NotNull List<KlangFunctionDecl> overriders,
                                                   @NotNull PsiElement anchor,
                                                   @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // If there are real implementation/override navigation targets, avoid adding a
        // self-target marker on top: it creates a confusing first popup entry that points back
        // to the same declaration before the useful "implemented/overridden in subtype(s)" popup.
        if (!overriders.isEmpty()) return;

        if (isAbstractMethod(func)) {
            RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                    .create(KlangUi.Icons.GUTTER_METHOD_ABSTRACT)
                    .setTargets(List.of(func))
                    .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                    .setTooltipText(KlangUi.Text.TOOLTIP_ABSTRACT_METHOD)
                    .createLineMarkerInfo(anchor);
            result.add(info);
            return;
        }
        // Keep overridden methods on the dedicated override/implements marker to avoid duplicate icons.
        if (!overridden.isEmpty()) return;
        if (isVirtualMethod(func, overridden)) {
            RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                    .create(KlangUi.Icons.GUTTER_METHOD_VIRTUAL)
                    .setTargets(List.of(func))
                    .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                    .setTooltipText(KlangUi.Text.TOOLTIP_VIRTUAL_METHOD)
                    .createLineMarkerInfo(anchor);
            result.add(info);
        }
    }

    private static boolean isConstructorOrDestructor(@NotNull KlangFunctionDecl fn) {
        if (fn.getNode().findChildByType(KlangTypes.DESTRUCTOR_HEAD) != null) return true;
        if (fn.getNode().findChildByType(KlangTypes.FUNCTION_HEAD) == null) return false;
        String name = fn.getName();
        if (name == null) return false;
        KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(fn);
        return owner != null && name.equals(owner.getName());
    }

    private static void addAggregateMarker(@NotNull KlangAggregateDecl agg,
                                           @NotNull PsiElement anchor,
                                           @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        List<KlangAggregateDecl> bases = KlangResolveUtil.directBases(agg);
        if (bases.isEmpty()) return;

        RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                .create(KlangUi.Icons.GUTTER_INHERITANCE_OVERRIDING_METHOD)
                .setTargets(bases)
                .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                .setTooltipText(bases.size() > 1
                        ? KlangUi.Text.TOOLTIP_GO_TO_SUPER_TYPES
                        : KlangUi.Text.TOOLTIP_GO_TO_SUPER_TYPE)
                .createLineMarkerInfo(anchor);
        result.add(info);
    }

    private static void addSubtypesMarker(@NotNull KlangAggregateDecl agg,
                                          @NotNull PsiElement anchor,
                                          @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        List<KlangAggregateDecl> subtypes = KlangResolveUtil.findSubAggregates(agg);
        if (subtypes.isEmpty()) return;

        boolean isInterface = isInterface(agg);
        RelatedItemLineMarkerInfo<PsiElement> info = NavigationGutterIconBuilder
                .create(isInterface
                        ? KlangUi.Icons.GUTTER_INHERITANCE_IMPLEMENTED_METHOD
                        : KlangUi.Icons.GUTTER_INHERITANCE_OVERRIDDEN_METHOD)
                .setTargets(subtypes)
                .setCellRenderer(KlangNavigationTargetRenderer.INSTANCE)
                .setTooltipText(isInterface
                        ? KlangUi.Text.TOOLTIP_IMPLEMENTED_BY_SUBTYPES
                        : KlangUi.Text.TOOLTIP_SUBCLASSED_BY_SUBTYPES)
                .createLineMarkerInfo(anchor);
        result.add(info);
    }

    /**
     * Whether the method is a (non-static) interface member — whether purely abstract or
     * carrying a {@code default} implementation. Both are treated the same way for gutter
     * marker purposes: no marker until an override/implementation exists, then a navigable
     * "implemented by" marker pointing to it (with a chooser when there are several).
     */
    private static boolean isAbstractInterfaceMethod(@NotNull KlangFunctionDecl method) {
        KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(method);
        return owner != null
                && isInterface(owner)
                && !hasSpecifier(method, "static");
    }

    /** Abstract if explicitly marked, or implied by interface non-static non-default method rules. */
    private static boolean isAbstractMethod(@NotNull KlangFunctionDecl method) {
        if (hasSpecifier(method, "abstract")) return true;
        return isAbstractInterfaceMethod(method);
    }

    /** Virtual by default in classes (non-static), with language-specific exceptions. */
    private static boolean isVirtualMethod(@NotNull KlangFunctionDecl method,
                                           @NotNull List<KlangFunctionDecl> overridden) {
        KlangAggregateDecl owner = KlangResolveUtil.owningAggregate(method);
        if (owner == null) return false; // free function
        if (hasSpecifier(method, "static")) return false;
        if (isStruct(owner)) return false;
        // Interface members (abstract or default) are handled by isAbstractMethod/
        // isAbstractInterfaceMethod above; they are never "virtual" markers.
        if (isInterface(owner)) return false;
        if (isClass(owner)) {
            // Class non-static methods are virtual by default, except a first-definition final method.
            return !(hasSpecifier(method, "final") && overridden.isEmpty());
        }
        return false;
    }

    private static boolean hasSpecifier(@NotNull KlangFunctionDecl method, @NotNull String keyword) {
        return method.getSpecifierList().stream().anyMatch(s -> keyword.equals(s.getText()));
    }

    /** Whether {@code agg} is declared with the {@code interface} keyword. */
    private static boolean isInterface(@NotNull KlangAggregateDecl agg) {
        return KlangResolveUtil.isInterface(agg);
    }

    private static boolean isClass(@NotNull KlangAggregateDecl agg) {
        return agg.getNode().findChildByType(KlangTypes.KW_CLASS) != null;
    }

    private static boolean isStruct(@NotNull KlangAggregateDecl agg) {
        return agg.getNode().findChildByType(KlangTypes.KW_STRUCT) != null;
    }
}
