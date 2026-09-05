package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 0 — verifies that name references resolve onto the newly-promoted declarations.
 * Each fragment uses a single {@code <caret>} on the reference under test; the comment
 * explains the resolution path exercised (see {@code docs/klang/name-resolution.md}).
 */
class KlangReferenceResolutionTest extends KlangFixtureTestBase {

    /** Configures the source and resolves the reference under the (single) caret. */
    private PsiElement resolveAtCaret(String source) {
        fixture.configureByText("a.k", source);
        PsiReference ref = fixture.getReferenceAtCaretPosition();
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        return ref.resolve();
    }

    @Test
    void referenceToParameterResolvesToParameterSpec() {
        onEdt(() -> {
            // 'a' in the body resolves to the parameter declared in the function head.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    add(a: int, b: int) : int {
                        return <caret>a + b;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangParameterSpec.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("a");
        });
    }

    @Test
    void referenceToNamedReturnVarResolves() {
        onEdt(() -> {
            // The named return variable 'result' is visible throughout the body.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    compute() result: int = 0 {
                        return <caret>result;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangNamedReturnVar.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("result");
        });
    }

    @Test
    void referenceToIfConditionVariableResolves() {
        onEdt(() -> {
            // 'v' declared in the if-condition is visible inside the then-branch.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    run() : int {
                        if (v: int = 1) {
                            return <caret>v;
                        }
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangIfCondVarDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("v");
        });
    }

    @Test
    void referenceToCatchParameterResolves() {
        onEdt(() -> {
            // The catch binding 'e' is visible inside the catch block (new scope handling).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    run() : int {
                        try {
                            return 0;
                        } catch (e: int) {
                            return <caret>e;
                        }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangCatchParameterDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("e");
        });
    }

    @Test
    void enumEntryReferenceResolvesToEnumEntry() {
        onEdt(() -> {
            // Qualified 'Color::Blue' resolves to the enum entry (§11.1).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    enum Color { Red; Green; Blue; };
                    paint(c: Color) : void { }
                    main() : int {
                        paint(Color::Bl<caret>ue);
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangEnumEntry.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Blue");
        });
    }

    @Test
    void unionKindEntryReferenceResolvesToUnionMember() {
        onEdt(() -> {
            // 'Shape::Kind::circle' resolves to the union alternative (§11.2).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Circle { r: float; }
                    union Shape { circle: Circle; rect: Circle; }
                    classify(s: Shape) : int {
                        if (s == Shape::Kind::cir<caret>cle) {
                            return 1;
                        }
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangUnionMemberDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("circle");
        });
    }

    @Test
    void qualifiedNameResolvesThroughNamespaceContainer() {
        onEdt(() -> {
            // 'util::count' descends into the (now-named) namespace 'util' to its variable.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    namespace util {
                        count: int = 0;
                    }
                    main() : int {
                        return util::cou<caret>nt;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("count");
        });
    }

    @Test
    void aliasReferenceResolvesToSoftAliasDecl() {
        onEdt(() -> {
            PsiElement target = resolveAtCaret("""
                    module demo;
                    alias MyInt : int;
                    main() : int {
                        x: My<caret>Int = 42;
                        return x;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangSoftAliasDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("MyInt");
        });
    }

    @Test
    void typedefReferenceResolvesToTypedefDecl() {
        onEdt(() -> {
            PsiElement target = resolveAtCaret("""
                    module demo;
                    typedef Distance : float;
                    main() : int {
                        d: Dis<caret>tance = 10.0f;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangTypedefDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Distance");
        });
    }

    @Test
    void blockLocalAliasResolves() {
        onEdt(() -> {
            PsiElement target = resolveAtCaret("""
                    module demo;
                    main() : int {
                        alias LocalAlias : int;
                        x: Local<caret>Alias = 1;
                        return x;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangSoftAliasDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("LocalAlias");
        });
    }

    @Test
    void lambdaParameterReferenceResolves() {
        onEdt(() -> {
            PsiElement target = resolveAtCaret("""
                    module demo;
                    main() : int {
                        f = (val: int) : int {
                            return <caret>val * 2;
                        };
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangParameterSpec.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("val");
        });
    }

    @Test
    void lambdaCaptureReferenceResolves() {
        onEdt(() -> {
            PsiElement target = resolveAtCaret("""
                    module demo;
                    main() : int {
                        f = [bonus = 5] (val: int) : int {
                            return val + <caret>bonus;
                        };
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangCapture.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("bonus");
        });
    }
}

