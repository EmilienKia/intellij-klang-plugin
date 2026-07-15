package com.github.emilienkia.klang.plugin.language.generate;

import com.github.emilienkia.klang.plugin.language.KlangNavigationPresentation;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.MemberChooserObjectBase;
import org.jetbrains.annotations.NotNull;

/**
 * {@link ClassMember} wrapper around a base {@link KlangFunctionDecl}, for the
 * {@code com.intellij.ide.util.MemberChooser} dialog used by
 * {@link KlangOverrideImplementMembersAction}. Rendering (text + icon) reuses
 * {@link KlangNavigationPresentation}, the same presentation used by other navigation popups
 * (gutter markers, add-import chooser) so the entry looks consistent across the plugin.
 */
final class KlangOverridableMember extends MemberChooserObjectBase implements ClassMember {

    private final KlangFunctionDecl method;
    private final boolean required;

    KlangOverridableMember(@NotNull KlangFunctionDecl method, boolean required) {
        super(KlangNavigationPresentation.elementText(method), KlangNavigationPresentation.icon(method));
        this.method = method;
        this.required = required;
    }

    @NotNull KlangFunctionDecl method() {
        return method;
    }

    /** Whether this member is a still-unimplemented abstract method (pre-checked in the dialog). */
    boolean isRequired() {
        return required;
    }

    @Override
    public MemberChooserObject getParentNodeDelegate() {
        return this;
    }
}



