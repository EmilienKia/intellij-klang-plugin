package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 0 — verifies that every named K-lang entity is exposed as a
 * {@link KlangNamedElement} (the requirement for Go-to-Declaration, Find-Usages and
 * Rename to target it). Each fragment below is a small, self-contained K source whose
 * comments describe what is asserted.
 */
class KlangNamedDeclarationTest extends KlangFixtureTestBase {

    /** Collects the {@code getName()} of every PSI node of the given type, in document order. */
    private <T extends KlangNamedElement> List<String> namesOf(KlangFile file, Class<T> type) {
        Collection<T> elements = PsiTreeUtil.findChildrenOfType(file, type);
        return elements.stream().map(KlangNamedElement::getName).collect(Collectors.toList());
    }

    @Test
    void allTopLevelAndNestedDeclarationsAreNamedElements() {
        onEdt(() -> {
            // A compilation unit exercising every named construct at once.
            KlangFile file = parse("""
                    module demo;

                    namespace util {
                        count: int = 0;          // variable (already named pre-phase-0)
                        reset() : void { }       // free function
                    }

                    struct Point {
                        x: float;                // member variable
                        y: float;
                    }

                    enum Color : int {
                        Red;                     // enum entries
                        Green = 2;
                        Blue;
                    };

                    union Shape {
                        circle: Point;           // union alternatives
                        rect: Point;
                    }
                    """);

            // Namespaces are now navigation targets.
            assertThat(namesOf(file, KlangNamespaceDecl.class)).containsExactly("util");

            // Aggregates / enums / unions / functions / variables were already named.
            assertThat(namesOf(file, KlangAggregateDecl.class)).containsExactly("Point");
            assertThat(namesOf(file, KlangEnumDecl.class)).containsExactly("Color");
            assertThat(namesOf(file, KlangUnionDecl.class)).containsExactly("Shape");
            assertThat(namesOf(file, KlangFunctionDecl.class)).containsExactly("reset");
            assertThat(namesOf(file, KlangVariableDecl.class)).contains("count", "x", "y");

            // Newly promoted: enum entries and union members.
            assertThat(namesOf(file, KlangEnumEntry.class)).containsExactly("Red", "Green", "Blue");
            assertThat(namesOf(file, KlangUnionMemberDecl.class)).containsExactly("circle", "rect");
        });
    }

    @Test
    void functionParametersAndNamedReturnAreNamedElements() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    // Two parameters and a *named return variable* must all be navigable.
                    add(a: int, b: int) sum: int = 0 {
                        return a + b;
                    }
                    """);

            assertThat(namesOf(file, KlangParameterSpec.class)).containsExactly("a", "b");
            assertThat(namesOf(file, KlangNamedReturnVar.class)).containsExactly("sum");
        });
    }

    @Test
    void ifConditionAndCatchBindingsAreNamedElements() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    run() : int {
                        // if-condition variable declaration
                        if (v: int = 1) {
                            return v;
                        }
                        // catch parameter binding
                        try {
                            return 0;
                        } catch (e: int) {
                            return 1;
                        }
                    }
                    """);

            assertThat(namesOf(file, KlangIfCondVarDecl.class)).containsExactly("v");
            assertThat(namesOf(file, KlangCatchParameterDecl.class)).containsExactly("e");
        });
    }

    @Test
    void templateParametersAreNamedElements() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    // The template type parameter 'T' is a declaration in its own right.
                    template<typename T>
                    identity(x: T) : T {
                        return x;
                    }
                    """);

            assertThat(namesOf(file, KlangTemplateParameter.class)).containsExactly("T");
        });
    }

    @Test
    void anonymousNamespaceHasNoName() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    // An anonymous namespace declares a scope but has no name.
                    namespace {
                        hidden: int = 0;
                    }
                    """);

            KlangNamespaceDecl ns = PsiTreeUtil.findChildOfType(file, KlangNamespaceDecl.class);
            assertThat(ns).isNotNull();
            assertThat(ns.getName()).isNull();
            assertThat(ns.getNameIdentifier()).isNull();
        });
    }

    @Test
    void nameIdentifierTextMatchesDeclaredName() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    greet(who: int) : int { return who; }
                    """);

            KlangParameterSpec param = PsiTreeUtil.findChildOfType(file, KlangParameterSpec.class);
            assertThat(param).isNotNull();
            // The element is a real named element whose identifier token carries the name.
            assertThat(param).isInstanceOf(KlangNamedElement.class);
            assertThat(param.getName()).isEqualTo("who");
            assertThat(param.getNameIdentifier()).isNotNull();
            assertThat(param.getNameIdentifier().getText()).isEqualTo("who");
        });
    }
}

