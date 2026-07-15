package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.github.emilienkia.klang.plugin.language.refactoring.changesignature.KlangChangeSignatureModel;
import com.github.emilienkia.klang.plugin.language.refactoring.changesignature.KlangChangeSignatureProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Change Signature refactoring tests — direct calls to {@link KlangChangeSignatureProcessor}
 * (bypassing the dialog UI) to exercise rename + parameter/return-type/specifier changes and
 * their propagation across the override hierarchy.
 */
class KlangChangeSignatureTest extends KlangFixtureTestBase {

    private KlangFunctionDecl functionAtCaret(String source) {
        fixture.configureByText("a.k", source);
        int offset = fixture.getCaretOffset();
        var element = fixture.getFile().findElementAt(offset);
        if (element == null && offset > 0) element = fixture.getFile().findElementAt(offset - 1);
        return PsiTreeUtil.getParentOfType(element, KlangFunctionDecl.class);
    }

    @Test
    void changesNameParametersReturnTypeAndSpecifiers() {
        onEdt(() -> {
            KlangFunctionDecl fn = functionAtCaret("""
                    module demo;
                    struct Point {
                        dist<caret>ance(other: Point&) : float { return 0.0; }
                    }
                    """);
            assertThat(fn).isNotNull();

            KlangChangeSignatureModel model = new KlangChangeSignatureModel(
                    "distanceTo",
                    "double",
                    List.of("other: Point&", "epsilon: float = 0.001"),
                    List.of("public", "final"),
                    true, false);
            KlangChangeSignatureProcessor.execute(fixture.getProject(), fn, model);

            String result = fixture.getFile().getText();
            assertThat(result).contains(
                    "public final distanceTo(other: Point&, epsilon: float = 0.001) : double");
            assertThat(result).doesNotContain("distance(");
        });
    }

    @Test
    void propagatesPrototypeDownToOverridingMethodsButKeepsTheirSpecifiers() {
        onEdt(() -> {
            KlangFunctionDecl fn = functionAtCaret("""
                    module demo;
                    interface Shape {
                        ar<caret>ea() : float;
                    }
                    struct Circle : Shape {
                        override area() : float { return 1.0; }
                    }
                    struct Square : Circle {
                        override area() : float { return 2.0; }
                    }
                    """);
            assertThat(fn).isNotNull();

            KlangChangeSignatureModel model = new KlangChangeSignatureModel(
                    "surface", "double", List.of(), List.of(), true, false);
            KlangChangeSignatureProcessor.execute(fixture.getProject(), fn, model);

            String result = fixture.getFile().getText();
            assertThat(result).contains("surface() : double;");
            assertThat(result).contains("override surface() : double { return 1.0; }");
            assertThat(result).contains("override surface() : double { return 2.0; }");
            assertThat(result).doesNotContain("area");
        });
    }

    @Test
    void propagatesPrototypeUpToBaseMethodOnlyWhenRequested() {
        onEdt(() -> {
            KlangFunctionDecl fn = functionAtCaret("""
                    module demo;
                    struct Base {
                        compute() : int { return 1; }
                    }
                    struct Child : Base {
                        comp<caret>ute() : int { return 2; }
                    }
                    """);
            assertThat(fn).isNotNull();
            assertThat(KlangResolveUtil.findOverriddenMethods(fn)).isNotEmpty();

            KlangChangeSignatureModel model = new KlangChangeSignatureModel(
                    "evaluate", "int", List.of(), List.of(), true, true);
            KlangChangeSignatureProcessor.execute(fixture.getProject(), fn, model);

            String result = fixture.getFile().getText();
            assertThat(result).contains("struct Base {\n    evaluate() : int");
            assertThat(result).contains("struct Child : Base {\n    evaluate() : int");
            assertThat(result).doesNotContain("compute");
        });
    }

    @Test
    void doesNotChangeBaseMethodWhenPropagateUpIsFalse() {
        onEdt(() -> {
            KlangFunctionDecl fn = functionAtCaret("""
                    module demo;
                    struct Base {
                        compute() : int { return 1; }
                    }
                    struct Child : Base {
                        comp<caret>ute() : int { return 2; }
                    }
                    """);
            assertThat(fn).isNotNull();

            KlangChangeSignatureModel model = new KlangChangeSignatureModel(
                    "evaluate", "int", List.of(), List.of(), false, false);
            KlangChangeSignatureProcessor.execute(fixture.getProject(), fn, model);

            String result = fixture.getFile().getText();
            assertThat(result).contains("struct Base {\n    compute() : int");
            assertThat(result).contains("struct Child : Base {\n    evaluate() : int");
        });
    }
}

