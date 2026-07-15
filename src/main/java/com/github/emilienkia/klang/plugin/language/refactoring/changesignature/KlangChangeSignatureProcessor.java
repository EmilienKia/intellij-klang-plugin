package com.github.emilienkia.klang.plugin.language.refactoring.changesignature;

import com.github.emilienkia.klang.plugin.language.psi.KlangAnnotationDef;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedReturnVar;
import com.github.emilienkia.klang.plugin.language.psi.KlangReturnTypeOrMemberInitList;
import com.github.emilienkia.klang.plugin.language.psi.KlangSpecifier;
import com.github.emilienkia.klang.plugin.language.psi.KlangThrowsClause;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypeSpec;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangPsiElementFactory;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Applies a {@link KlangChangeSignatureModel} to a {@link KlangFunctionDecl}: rebuilds the
 * declaration header (specifiers, name, parameter list, return type) as text, re-parses it via
 * {@link KlangPsiElementFactory#createFunctionDecl}, and swaps it in via {@link
 * com.intellij.psi.PsiElement#replace}. Optionally propagates the <em>prototype</em> (name,
 * parameters, return type — never the edited method's own specifiers) to methods it overrides
 * (up the hierarchy) and/or methods that override it (down the hierarchy), each keeping its own
 * specifiers, annotations, throws clause and body untouched.
 */
public final class KlangChangeSignatureProcessor {

    private KlangChangeSignatureProcessor() {}

    public static void execute(@NotNull Project project,
                                @NotNull KlangFunctionDecl target,
                                @NotNull KlangChangeSignatureModel model) {
        // Collect companions BEFORE any mutation — findOverriddenMethods/findOverridingMethods
        // match by current name, so they must run against the still-unmodified tree.
        List<KlangFunctionDecl> upChain = model.propagateUp()
                ? collectTransitive(target, KlangResolveUtil::findOverriddenMethods)
                : List.of();
        List<KlangFunctionDecl> downChain = model.propagateDown()
                ? collectTransitive(target, KlangResolveUtil::findOverridingMethods)
                : List.of();

        WriteCommandAction.runWriteCommandAction(project, "Change Signature", null, () -> {
            for (KlangFunctionDecl companion : upChain) {
                applyPrototype(companion, model);
            }
            for (KlangFunctionDecl companion : downChain) {
                applyPrototype(companion, model);
            }
            // Apply full change (incl. specifiers) to the primary target last.
            String text = buildText(target, model.specifiers(), model.name(),
                    String.join(", ", model.parameters()), model.returnType());
            target.replace(KlangPsiElementFactory.createFunctionDecl(project, text));
        });
    }

    /** Renames + re-parameterizes {@code fn} while keeping its own specifiers untouched. */
    private static void applyPrototype(@NotNull KlangFunctionDecl fn, @NotNull KlangChangeSignatureModel model) {
        List<String> ownSpecifiers = fn.getSpecifierList().stream()
                .map(KlangSpecifier::getText).collect(Collectors.toList());
        String text = buildText(fn, ownSpecifiers, model.name(),
                String.join(", ", model.parameters()), model.returnType());
        fn.replace(KlangPsiElementFactory.createFunctionDecl(fn.getProject(), text));
    }

    /**
     * Renders a full {@code functionDecl} source fragment for {@code fn}, substituting
     * {@code specifiers}/{@code name}/{@code paramsText}/{@code returnTypeText} but preserving
     * everything else (leading annotations, template/generic declaration, named-return-var,
     * constructor member-init list when no return type is given, throws clause, body) verbatim.
     */
    private static @NotNull String buildText(@NotNull KlangFunctionDecl fn,
                                              @NotNull List<String> specifiers,
                                              @NotNull String name,
                                              @NotNull String paramsText,
                                              @NotNull String returnTypeText) {
        StringBuilder sb = new StringBuilder();
        for (KlangAnnotationDef ann : fn.getAnnotationDefList()) sb.append(ann.getText()).append(' ');
        if (fn.getTemplateDeclaration() != null) sb.append(fn.getTemplateDeclaration().getText()).append(' ');
        if (fn.getGenericDeclaration() != null) sb.append(fn.getGenericDeclaration().getText()).append(' ');
        for (String s : specifiers) sb.append(s).append(' ');
        sb.append(name).append('(').append(paramsText).append(')');

        KlangNamedReturnVar nrv = fn.getNamedReturnVar();
        if (nrv != null) sb.append(' ').append(nrv.getText());

        if (!returnTypeText.isBlank()) {
            sb.append(" : ").append(returnTypeText);
        } else {
            // No return-type text supplied: preserve a constructor member-init / static-dep list
            // verbatim (returnTypeOrMemberInitList only carries a typeSpec for ordinary methods).
            KlangReturnTypeOrMemberInitList tail = fn.getReturnTypeOrMemberInitList();
            if (tail != null && tail.getTypeSpec() == null) {
                sb.append(" : ").append(tail.getText());
            }
        }

        KlangThrowsClause throwsClause = fn.getThrowsClause();
        if (throwsClause != null) sb.append(' ').append(throwsClause.getText());

        // No space before a bodiless ';' (e.g. abstract/interface method declarations).
        String bodyText = fn.getFunctionBody().getText();
        if (!bodyText.startsWith(";")) sb.append(' ');
        sb.append(bodyText);
        return sb.toString();
    }

    /** The declared return type text of {@code fn}, or {@code ""} when it declares none. */
    public static @NotNull String initialReturnType(@NotNull KlangFunctionDecl fn) {
        KlangReturnTypeOrMemberInitList tail = fn.getReturnTypeOrMemberInitList();
        KlangTypeSpec type = tail != null ? tail.getTypeSpec() : null;
        return type != null ? type.getText().trim() : "";
    }

    private static @NotNull List<KlangFunctionDecl> collectTransitive(
            @NotNull KlangFunctionDecl start,
            @NotNull Function<KlangFunctionDecl, List<KlangFunctionDecl>> step) {
        LinkedHashSet<KlangFunctionDecl> result = new LinkedHashSet<>();
        Set<KlangFunctionDecl> seen = new LinkedHashSet<>();
        Deque<KlangFunctionDecl> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            KlangFunctionDecl current = queue.poll();
            for (KlangFunctionDecl next : step.apply(current)) {
                if (seen.add(next)) {
                    result.add(next);
                    queue.add(next);
                }
            }
        }
        return new ArrayList<>(result);
    }
}


