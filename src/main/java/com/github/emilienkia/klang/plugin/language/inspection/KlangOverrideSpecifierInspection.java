package com.github.emilienkia.klang.plugin.language.inspection;

import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangSpecifier;
import com.github.emilienkia.klang.plugin.language.psi.KlangVisitor;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Validates the (already-parsed, previously unchecked) {@code override} specifier: reports a
 * method marked {@code override} that does not actually override/implement any base member —
 * the K analogue of Java's {@code @Override}-on-nothing / C++'s
 * {@code marked 'override' but does not override} diagnostic.
 *
 * <p>Design: {@code docs/klang/abstract-implementation-plan.md} §8.</p>
 */
public final class KlangOverrideSpecifierInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new KlangVisitor() {
            @Override
            public void visitFunctionDecl(@NotNull KlangFunctionDecl fn) {
                if (KlangResolveUtil.isConstructorOrDestructor(fn)) return;

                KlangSpecifier overrideSpecifier = null;
                for (KlangSpecifier s : fn.getSpecifierList()) {
                    if ("override".equals(s.getText())) {
                        overrideSpecifier = s;
                        break;
                    }
                }
                if (overrideSpecifier == null) return;

                try {
                    if (!KlangResolveUtil.findOverriddenMethods(fn).isEmpty()) return;
                } catch (IndexNotReadyException ignored) {
                    return; // dumb mode — fail soft
                }

                holder.registerProblem(overrideSpecifier,
                        "Method marked 'override' does not override or implement a base member");
            }
        };
    }
}

