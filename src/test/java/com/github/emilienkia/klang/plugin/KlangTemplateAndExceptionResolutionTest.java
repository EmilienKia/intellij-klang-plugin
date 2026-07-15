package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 3 — exception-type references, template parameters as type names, template
 * arguments, and template-qualified scope expressions. Cross-module ({@code import})
 * resolution is deliberately out of scope (it needs a project-wide symbol index).
 */
class KlangTemplateAndExceptionResolutionTest extends KlangFixtureTestBase {

    private PsiElement resolveAtCaret(String source) {
        fixture.configureByText("a.k", source);
        PsiReference ref = fixture.getReferenceAtCaretPosition();
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        return ref.resolve();
    }

    @Test
    void throwsClauseTypeResolves() {
        onEdt(() -> {
            // 'throws MyError' resolves the exception type.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct MyError { code: int; }
                    risky() throws MyEr<caret>ror {
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("MyError");
        });
    }

    @Test
    void catchParameterTypeResolves() {
        onEdt(() -> {
            // The type in 'catch (e: MyError)' resolves to the exception struct.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct MyError { code: int; }
                    handler() : int {
                        try { return 0; }
                        catch (e: MyEr<caret>ror) { return 1; }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("MyError");
        });
    }

    @Test
    void templateParameterInFunctionResolves() {
        onEdt(() -> {
            // The type parameter 'T' used in a parameter type resolves to its declaration.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    template<typename T>
                    identity(x: <caret>T) : T {
                        return x;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangTemplateParameter.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("T");
        });
    }

    @Test
    void templateParameterInAggregateResolves() {
        onEdt(() -> {
            // The type parameter 'T' used in a member type resolves to its declaration.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    template<typename T>
                    struct Box {
                        item : <caret>T;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangTemplateParameter.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("T");
        });
    }

    @Test
    void templateArgumentTypeResolves() {
        onEdt(() -> {
            // The argument 'Point' inside 'Box<Point>' resolves to the struct.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    template<typename T> struct Box { item : T; }
                    main() : int {
                        b : Box<Po<caret>int>;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void templatedTypeNameResolvesIgnoringArguments() {
        onEdt(() -> {
            // 'Box' in 'Box<Point>' resolves to the template aggregate (args stripped).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    template<typename T> struct Box { item : T; }
                    main() : int {
                        b : Bo<caret>x<Point>;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Box");
        });
    }

    @Test
    void qualifiedTemplateArgumentTypeResolves() {
        onEdt(() -> {
            // The qualified argument 'shapes::Point' inside 'Box<shapes::Point>' resolves
            // to the struct (the inner argument keeps its own dedicated reference, and the
            // outer 'Box<...>' reference no longer collides with it on the 'Point' segment).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace shapes { struct Point { x: float; } }
                    template<typename T> struct Box { item : T; }
                    main() : int {
                        b : Box<shapes::Po<caret>int>;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void outerTemplatedNameResolvesWhenArgumentIsQualified() {
        onEdt(() -> {
            // Clicking the outer 'Box' of 'Box<shapes::Point>' navigates to 'Box', even
            // though the argument contains a '::' (the outer range stops before the '<').
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace shapes { struct Point { x: float; } }
                    template<typename T> struct Box { item : T; }
                    main() : int {
                        b : Bo<caret>x<shapes::Point>;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Box");
        });
    }

    @Test
    void secondTemplateArgumentTypeResolves() {
        onEdt(() -> {
            // The second argument 'Val' of 'Map<Key, Val>' resolves independently.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Key { x: float; }
                    struct Val { y: float; }
                    template<typename A, typename B> struct Map { }
                    main() : int {
                        m : Map<Key, Va<caret>l>;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Val");
        });
    }

    @Test
    void nestedTemplateArgumentTypeResolves() {
        onEdt(() -> {
            // The innermost argument 'Point' of 'Box<Vec<Point> >' resolves to the struct.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    template<typename T> struct Vec { item : T; }
                    template<typename T> struct Box { item : T; }
                    main() : int {
                        b : Box<Vec<Po<caret>int> >;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void templateQualifiedScopeExprResolves() {
        onEdt(() -> {
            // 'Box<int>::count' resolves through the (untemplated) Box scope to 'count'.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    template<typename T> struct Box {
                        static count : int = 0;
                    }
                    main() : int {
                        n : int = Box<int>::cou<caret>nt;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("count");
        });
    }
}

