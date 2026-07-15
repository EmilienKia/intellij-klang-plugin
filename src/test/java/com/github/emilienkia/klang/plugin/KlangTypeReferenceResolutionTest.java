package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 1 — verifies that {@code qualifiedIdentifier}s in <em>type</em> and
 * <em>directive</em> positions carry a resolvable reference (Go-to-Declaration).
 * Each fragment places a single {@code <caret>} on the name under test; the comment
 * explains the position being exercised.
 */
class KlangTypeReferenceResolutionTest extends KlangFixtureTestBase {

    /** Configures the source and resolves the reference under the (single) caret. */
    private PsiElement resolveAtCaret(String source) {
        fixture.configureByText("a.k", source);
        PsiReference ref = fixture.getReferenceAtCaretPosition();
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        return ref.resolve();
    }

    @Test
    void variableTypeResolvesToAggregate() {
        onEdt(() -> {
            // The type in a variable declaration resolves to its struct.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    main() : int {
                        p: Po<caret>int;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void qualifiedParameterTypeResolvesThroughNamespace() {
        onEdt(() -> {
            // 'shapes::Circle' in a parameter type descends into the namespace.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace shapes {
                        struct Circle { r: float; }
                    }
                    area(c: shapes::Cir<caret>cle) : float {
                        return 0.0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Circle");
        });
    }

    @Test
    void baseClassReferenceResolves() {
        onEdt(() -> {
            // The base-class name in an inheritance list resolves to the base aggregate.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    class Animal { name: int; }
                    class Dog : public Ani<caret>mal {
                        bark() : void { }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Animal");
        });
    }

    @Test
    void annotationReferenceResolvesToAnnotationType() {
        onEdt(() -> {
            // '@Deprecated' resolves to the annotation aggregate.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    annotation Deprecated { }
                    @Depre<caret>cated
                    struct Widget { x: int; }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Deprecated");
        });
    }

    @Test
    void newExpressionTypeResolves() {
        onEdt(() -> {
            // 'new Point()' resolves the constructed type.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    make() : void {
                        new Po<caret>int();
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void annotationArgumentResolvesInnerEnumEntryOfAppliedType() {
        onEdt(() -> {
            // §13 — an inner-enum entry referenced from another annotation's application
            // ('@Retention(Policy::RUNTIME)' on a type that does NOT declare 'Policy')
            // resolves against the applied annotation type's own scope (Retention::Policy).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace annotations {
                        @Retention(Policy::RUNTIME)
                        annotation Retention {
                            enum Policy { SOURCE; RUNTIME; }
                            policy : Policy = Policy::RUNTIME;
                        }
                        @Retention(Policy::RUN<caret>TIME)
                        annotation Inherited { }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangEnumEntry.class);
            assertThat(target.getText()).startsWith("RUNTIME");
        });
    }

    @Test
    void annotationArgumentResolvesInnerEnumEntryInBraceList() {
        onEdt(() -> {
            // §13 — the brace-list form '@Target({ElementType::ANNOTATION})' resolves the
            // inner enum entry against the applied annotation type's scope (Target::ElementType).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace annotations {
                        @Target({ElementType::ANNO<caret>TATION})
                        annotation Target {
                            enum ElementType { CLASS; ANNOTATION; FUNCTION; }
                            value : ElementType[];
                        }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangEnumEntry.class);
            assertThat(target.getText()).startsWith("ANNOTATION");
        });
    }

    @Test
    void annotationArgumentResolvesInnerEnumTypeSegment() {
        onEdt(() -> {
            // The intermediate segment 'Policy' in '@Retention(Policy::RUNTIME)' navigates to
            // the inner enum type of the applied annotation (Retention::Policy).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace annotations {
                        @Retention(Policy::RUNTIME)
                        annotation Retention {
                            enum Policy { SOURCE; RUNTIME; }
                            policy : Policy = Policy::RUNTIME;
                        }
                        @Retention(Pol<caret>icy::RUNTIME)
                        annotation Inherited { }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangEnumDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Policy");
        });
    }

    @Test
    void annotationArgumentResolvesInnerEnumTypeSegmentInBraceList() {
        onEdt(() -> {
            // The intermediate segment 'ElementType' in '@Target({ElementType::ANNOTATION})'
            // navigates to the inner enum type (Target::ElementType).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace annotations {
                        @Target({Elem<caret>entType::ANNOTATION})
                        annotation Target {
                            enum ElementType { CLASS; ANNOTATION; FUNCTION; }
                            value : ElementType[];
                        }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangEnumDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("ElementType");
        });
    }

    @Test
    void qualifiedTypeFirstSegmentNavigatesToNamespace() {
        onEdt(() -> {
            // The leading segment of a qualified type name navigates to the namespace.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace shapes {
                        struct Circle { r: float; }
                    }
                    area(c: shap<caret>es::Circle) : float {
                        return 0.0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangNamespaceDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("shapes");
        });
    }

    @Test
    void usingNamespaceTargetResolves() {
        onEdt(() -> {
            // The target of a 'using namespace' directive resolves to the namespace.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace util {
                        count: int = 0;
                    }
                    foo() : void {
                        using namespace ut<caret>il;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangNamespaceDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("util");
        });
    }

    @Test
    void friendTargetResolves() {
        onEdt(() -> {
            // 'friend class Inspector;' resolves the friend target aggregate.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    class Inspector { check() : void { } }
                    class Safe {
                        friend class Inspe<caret>ctor;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Inspector");
        });
    }

    @Test
    void redirectTargetResolvesToFunction() {
        onEdt(() -> {
            // The '-> target' redirect body resolves to the target function (§14).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    target() : int { return 1; }
                    alias() : int -> tar<caret>get;
                    """);
            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("target");
        });
    }

    @Test
    void staticDependencyResolvesToFunction() {
        onEdt(() -> {
            // A static-dependency entry 'deps::init()' resolves to the function.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace deps {
                        init() : void { }
                    }
                    build() : deps::in<caret>it() {
                    }
                    """);
            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("init");
        });
    }
}



