package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.KlangOperatorGotoDeclarationHandler;
import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Operator-overload navigation: Go-to-Declaration on an operator token in an expression
 * ({@code a == b}, {@code -a}, {@code a += b}) jumps to the matching {@code operator}
 * overload definition. Driven through {@link KlangOperatorGotoDeclarationHandler}.
 */
class KlangOperatorNavigationTest extends KlangFixtureTestBase {

    /** Resolves the goto-declaration target for the operator token under the (single) caret. */
    private PsiElement gotoAtCaret(String source) {
        fixture.configureByText("a.k", source);
        int offset = fixture.getCaretOffset();
        PsiElement token = fixture.getFile().findElementAt(offset);
        assertThat(token).as("a leaf token under the caret").isNotNull();
        PsiElement[] targets = new KlangOperatorGotoDeclarationHandler()
                .getGotoDeclarationTargets(token, offset, fixture.getEditor());
        return targets == null || targets.length != 1 ? null : targets[0];
    }

    /** Asserts the target is the {@code operator <symbol>} overload of the named aggregate. */
    private void assertIsOperatorOf(PsiElement target, String typeName) {
        assertThat(target).isInstanceOf(KlangFunctionDecl.class);
        assertThat(((KlangFunctionDecl) target).getOperatorFunctionHead()).isNotNull();
        KlangAggregateDecl owner = PsiTreeUtil.getParentOfType(target, KlangAggregateDecl.class);
        assertThat(owner).isNotNull();
        assertThat(owner.getName()).isEqualTo(typeName);
    }

    /** Asserts the resolved overload's operator symbol (whitespace-stripped) equals {@code expected}. */
    private void assertOperatorSymbol(PsiElement target, String expected) {
        var head = ((KlangFunctionDecl) target).getOperatorFunctionHead();
        assertThat(head).isNotNull();
        assertThat(head.getOperatorSymbol()).isNotNull();
        assertThat(head.getOperatorSymbol().getText().replaceAll("\\s+", "")).isEqualTo(expected);
    }

    @Test
    void binaryOperatorResolvesToMemberOverload() {
        onEdt(() -> {
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        operator ==(other: Point&) : bool { return x == other.x; }
                    }
                    main() : int {
                        p: Point;
                        q: Point;
                        b: bool = p =<caret>= q;
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Point");
        });
    }

    @Test
    void prefixOperatorResolvesToMemberOverload() {
        onEdt(() -> {
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Vec {
                        x: float;
                        operator -() : Vec { return this; }
                    }
                    main() : int {
                        v: Vec;
                        w: Vec = <caret>-v;
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Vec");
        });
    }

    @Test
    void freeOperatorResolvesByFirstParameter() {
        onEdt(() -> {
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Vec { x: float; }
                    operator +(a: Vec&, b: Vec&) : Vec { return a; }
                    main() : int {
                        u: Vec;
                        v: Vec;
                        w: Vec = u <caret>+ v;
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangFunctionDecl) target).getOperatorFunctionHead()).isNotNull();
            // free operator: not a member of any aggregate
            assertThat(PsiTreeUtil.getParentOfType(target, KlangAggregateDecl.class)).isNull();
        });
    }

    @Test
    void inheritedOperatorResolves() {
        onEdt(() -> {
            PsiElement target = gotoAtCaret("""
                    module demo;
                    class Base {
                        operator ==(o: Base&) : bool { return true; }
                    }
                    class Derived : public Base { }
                    main() : int {
                        d: Derived;
                        e: Derived;
                        b: bool = d =<caret>= e;
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Base");
        });
    }

    @Test
    void operatorOnPrimitiveResolvesToNothing() {
        onEdt(() -> {
            // No overload for built-in int '+': fail soft (no target).
            PsiElement target = gotoAtCaret("""
                    module demo;
                    main() : int {
                        a: int = 1;
                        b: int = 2;
                        c: int = a <caret>+ b;
                        return 0;
                    }
                    """);
            assertThat(target).isNull();
        });
    }

    @Test
    void subscriptOperatorResolvesToMemberOverload() {
        onEdt(() -> {
            // Ctrl-Click on the '[' of an indexing expression jumps to the member operator[].
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Buffer {
                        operator [](i : int) : int& { return i; }
                    }
                    main() : int {
                        b: Buffer;
                        x: int = b<caret>[0];
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Buffer");
        });
    }

    @Test
    void subscriptOperatorResolvesFromClosingBracket() {
        onEdt(() -> {
            // Clicking the ']' resolves the same overload as clicking the '['.
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Buffer {
                        operator [](i : int) : int& { return i; }
                    }
                    main() : int {
                        b: Buffer;
                        x: int = b[0<caret>];
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Buffer");
        });
    }

    @Test
    void inheritedSubscriptOperatorResolves() {
        onEdt(() -> {
            PsiElement target = gotoAtCaret("""
                    module demo;
                    class Base {
                        operator [](i : int) : int& { return i; }
                    }
                    class Derived : public Base { }
                    main() : int {
                        d: Derived;
                        x: int = d<caret>[0];
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Base");
        });
    }

    @Test
    void freeSubscriptOperatorResolvesByFirstParameter() {
        onEdt(() -> {
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Vec { x: float; }
                    operator [](v: Vec&, i: int) : float { return v.x; }
                    main() : int {
                        u: Vec;
                        f: float = u<caret>[0];
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangFunctionDecl) target).getOperatorFunctionHead()).isNotNull();
            // free operator: not a member of any aggregate
            assertThat(PsiTreeUtil.getParentOfType(target, KlangAggregateDecl.class)).isNull();
        });
    }

    @Test
    void subscriptOnPrimitiveResolvesToNothing() {
        onEdt(() -> {
            // Indexing something with no aggregate type / no operator[] overload: fail soft.
            PsiElement target = gotoAtCaret("""
                    module demo;
                    main() : int {
                        a: int = 1;
                        b: int = a<caret>[0];
                        return 0;
                    }
                    """);
            assertThat(target).isNull();
        });
    }

    @Test
    void postfixIncrementResolvesToPostfixOverload() {
        onEdt(() -> {
            // 'c++' must select 'operator _++' (postfix), not 'operator ++_' (prefix).
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Counter {
                        n: int;
                        operator ++_() : Counter { return this; }
                        operator _++() : Counter { return this; }
                    }
                    main() : int {
                        c: Counter;
                        c+<caret>+;
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Counter");
            assertOperatorSymbol(target, "_++");
        });
    }

    @Test
    void prefixIncrementResolvesToPrefixOverload() {
        onEdt(() -> {
            // '++c' must select 'operator ++_' (prefix), not 'operator _++' (postfix).
            PsiElement target = gotoAtCaret("""
                    module demo;
                    struct Counter {
                        n: int;
                        operator ++_() : Counter { return this; }
                        operator _++() : Counter { return this; }
                    }
                    main() : int {
                        c: Counter;
                        d: Counter = +<caret>+c;
                        return 0;
                    }
                    """);
            assertIsOperatorOf(target, "Counter");
            assertOperatorSymbol(target, "++_");
        });
    }
}




