package com.github.emilienkia.klang.plugin.language;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformIcons;

import javax.swing.*;

/**
 * Shared UI constants (icons + short labels/tooltips) for the K-lang plugin.
 */
public abstract class KlangUi {

    private KlangUi() {
    }

    public static final class Icons {
        private Icons() {
        }

        public static final Icon FILE = IconLoader.getIcon("/icons/klang.png", KlangUi.class);

        // Nodes
        public static final Icon NAMESPACE = AllIcons.Nodes.Package;
        public static final Icon AGGREGATE_CLASS = AllIcons.Nodes.Class;
        public static final Icon AGGREGATE_INTERFACE = AllIcons.Nodes.Interface;
        public static final Icon AGGREGATE_ANNOTATION = AllIcons.Nodes.Annotationtype;
        public static final Icon ENUM = AllIcons.Nodes.Enum;
        public static final Icon FIELD = AllIcons.Nodes.Field;
        public static final Icon FUNCTION_METHOD = AllIcons.Nodes.Method;
        public static final Icon FUNCTION_CTOR_DTOR = AllIcons.Nodes.Constructor;
        public static final Icon UNKNOWN = AllIcons.Nodes.Unknown;
        public static final Icon IMPORT_MODULE = AllIcons.Nodes.Package;

        // Decorations
        public static final Icon STATIC_MARK = AllIcons.Nodes.StaticMark;
        public static final Icon FINAL_MARK = AllIcons.Nodes.FinalMark;
        public static final Icon OVERRIDE_MARK = AllIcons.Gutter.OverridingMethod;

        // Visibility
        public static final Icon VISIBILITY_PUBLIC = PlatformIcons.PUBLIC_ICON;
        public static final Icon VISIBILITY_PROTECTED = PlatformIcons.PROTECTED_ICON;
        public static final Icon VISIBILITY_PRIVATE = PlatformIcons.PRIVATE_ICON;

        // Gutter navigation
        public static final Icon GUTTER_INHERITANCE_IMPLEMENTING_METHOD = AllIcons.Gutter.ImplementingMethod;
        public static final Icon GUTTER_INHERITANCE_OVERRIDING_METHOD = AllIcons.Gutter.OverridingMethod;
        public static final Icon GUTTER_INHERITANCE_IMPLEMENTED_METHOD = AllIcons.Gutter.ImplementedMethod;
        public static final Icon GUTTER_INHERITANCE_OVERRIDDEN_METHOD = AllIcons.Gutter.OverridenMethod;
        public static final Icon GUTTER_METHOD_ABSTRACT = AllIcons.Gutter.ImplementedMethod;
        public static final Icon GUTTER_METHOD_VIRTUAL = AllIcons.Gutter.ImplementingMethod;
        public static final Icon GUTTER_OPERATOR_USAGES = AllIcons.Gutter.ReadAccess;
        public static final Icon GUTTER_CTOR_DTOR = AllIcons.Nodes.Constructor;
    }

    public static final class Text {
        private Text() {
        }

        public static final String MARKER_PROVIDER_INHERITANCE = "K-lang inheritance markers";
        public static final String MARKER_PROVIDER_OPERATOR_USAGES = "K-lang operator usage markers";
        public static final String MARKER_PROVIDER_CTOR_DTOR = "K-lang constructor/destructor markers";

        public static final String TOOLTIP_CONSTRUCTOR = "Constructor";
        public static final String TOOLTIP_DESTRUCTOR = "Destructor";
        public static final String TOOLTIP_IMPLEMENTS_BASE_METHOD = "Implements method in base type";
        public static final String TOOLTIP_OVERRIDES_BASE_METHOD = "Overrides method in base type";
        public static final String TOOLTIP_ABSTRACT_METHOD = "Abstract method";
        public static final String TOOLTIP_VIRTUAL_METHOD = "Virtual method";
        public static final String TOOLTIP_IMPLEMENTED_IN_SUBTYPES = "Implemented in subtype(s)";
        public static final String TOOLTIP_OVERRIDDEN_IN_SUBTYPES = "Overridden in subtype(s)";
        public static final String TOOLTIP_GO_TO_SUPER_TYPE = "Go to super type";
        public static final String TOOLTIP_GO_TO_SUPER_TYPES = "Go to super types";
        public static final String TOOLTIP_IMPLEMENTED_BY_SUBTYPES = "Implemented by subtype(s)";
        public static final String TOOLTIP_SUBCLASSED_BY_SUBTYPES = "Subclassed by subtype(s)";

        public static final String POPUP_IMPORT_MODULE = "Import Module";
        public static final String POPUP_OPERATOR_USAGES_PREFIX = "Operator ";
        public static final String POPUP_OPERATOR_USAGES_SUFFIX = " usages";
        public static final String TOOLTIP_GO_TO_OPERATOR_USAGES_PREFIX = "Go to operator ";
        public static final String TOOLTIP_GO_TO_OPERATOR_USAGES_SUFFIX = " usages";
    }
}
