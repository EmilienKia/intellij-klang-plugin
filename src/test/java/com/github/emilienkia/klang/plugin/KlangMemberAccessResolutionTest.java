package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 2 — member access (the dot operator, §10) resolved against the receiver's type,
 * unified call syntax, and constructor / designated initializers. Each fragment places a
 * single {@code <caret>} on the member name under test.
 */
class KlangMemberAccessResolutionTest extends KlangFixtureTestBase {

    private PsiElement resolveAtCaret(String source) {
        fixture.configureByText("a.k", source);
        PsiReference ref = fixture.getReferenceAtCaretPosition();
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        return ref.resolve();
    }

    @Test
    void fieldAccessResolvesAgainstReceiverType() {
        onEdt(() -> {
            // p.coord : 'coord' resolves to the field of Point (the type of p), not via scope chain.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { coord: float; other: float; }
                    main() : int {
                        p: Point;
                        f: float = p.co<caret>ord;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("coord");
        });
    }

    @Test
    void methodAccessResolvesToMethod() {
        onEdt(() -> {
            // p.dist(q) : 'dist' resolves to the method of Point.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        dist(other: Point&) : float { return other.x; }
                    }
                    main() : int {
                        p: Point;
                        q: Point;
                        d: float = p.di<caret>st(q);
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("dist");
        });
    }

    @Test
    void thisFieldAccessResolves() {
        onEdt(() -> {
            // this.value : resolves to the field of the enclosing aggregate.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Counter {
                        value: int;
                        inc() : void { this.val<caret>ue = 1; }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("value");
        });
    }

    @Test
    void inheritedMemberAccessResolves() {
        onEdt(() -> {
            // this.val : 'val' is inherited from base class A (BFS over bases, §10.4).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    class A { val: int; }
                    class B : public A {
                        show() : void { this.va<caret>l = 0; }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("val");
        });
    }

    @Test
    void unifiedCallResolvesToFreeFunction() {
        onEdt(() -> {
            // v.length() : no member 'length' on Vec2 → free function length(v: Vec2&) (§10.3).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Vec2 { x: float; y: float; }
                    length(v: Vec2&) : float { return v.x; }
                    main() : int {
                        v: Vec2;
                        l: float = v.len<caret>gth();
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("length");
        });
    }

    @Test
    void constructorMemberInitResolvesToField() {
        onEdt(() -> {
            // Point(...) : x(px) — the 'x' member initializer resolves to the field x.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        y: float;
                        Point(px: float, py: float) : x<caret>(px), y(py) {}
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("x");
        });
    }

    @Test
    void fieldAccessThroughParamOfEnclosingTypeResolves() {
        onEdt(() -> {
            // other._obs : 'other' is typed as the *enclosing* struct that also declares a
            // constructor sharing the struct's name. The type-name lookup must yield the
            // struct (not its constructor), so the field access resolves. Regression for the
            // libk Shared<T>::share(other: Shared<T>&) case.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    template<class T>
                    struct Shared {
                        _obs: T*;
                        Shared() { _obs = null; }
                        share(other: Shared<T>&) {
                            if (other._o<caret>bs == null) { return; }
                        }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("_obs");
        });
    }

    @Test
    void unionMemberAccessResolves() {
        onEdt(() -> {
            // _storage.result : '_storage' is a union-typed field; 'result' resolves to the
            // union's alternative. Regression for the libk Expected<R,E>::setResult case.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Expected {
                        union Storage {
                            result: int;
                            error: int;
                        }
                        _storage: Storage;
                        setResult(value: int&) {
                            _storage.res<caret>ult = value;
                        }
                    }
                    """);
            assertThat(target).isInstanceOf(KlangUnionMemberDecl.class);
            assertThat(((KlangUnionMemberDecl) target).getIdentifier().getText()).isEqualTo("result");
        });
    }

    @Test
    void designatedInitializerResolvesToField() {
        onEdt(() -> {
            // p: Point = { .coord = … } — the '.coord' designator resolves to the field of Point.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { coord: float; other: float; }
                    main() : int {
                        p: Point = { .co<caret>ord = 1.0f, .other = 2.0f };
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("coord");
        });
    }
}



