package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangSpecifier;
import com.github.emilienkia.klang.plugin.language.psi.KlangVisitor;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Flags the {@code final} specifier on a constructor or destructor: the klangc compiler
 * rejects it unconditionally (diagnostic {@code ERR_FINAL_ON_CTOR_DTOR}, 0x0195) since
 * neither kind of member can be inherited/overridden, so marking one 'final' is meaningless.
 *
 * <p>Mirrors {@link KlangOverrideSpecifierInspection}'s structure, but the check here is
 * purely syntactic (no resolution needed): 'final' is invalid on a constructor/destructor
 * regardless of the enclosing struct/class or its hierarchy.</p>
 */
public final class KlangFinalOnCtorDtorInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new KlangVisitor() {
            @Override
            public void visitFunctionDecl(@NotNull KlangFunctionDecl fn) {
                if (!KlangResolveUtil.isConstructorOrDestructor(fn)) return;

                for (KlangSpecifier s : fn.getSpecifierList()) {
                    if ("final".equals(s.getText())) {
                        holder.registerProblem(s,
                                "Specifier 'final' is not allowed on a constructor or destructor");
                    }
                }
            }
        };
    }
}
