package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the split between <em>type</em> references and <em>constructor</em> references:
 * a name in a pure type position (variable / parameter / return type, cast, throw, base,
 * {@code new}) resolves to the aggregate <em>type</em>, while a constructor call in an
 * expression ({@code Name(args)}, including a variable's parenthesised initializer)
 * resolves to the aggregate's <em>constructor</em>.
 */
class KlangConstructorReferenceResolutionTest extends KlangFixtureTestBase {

    private PsiElement resolveAtCaret(String source) {
        fixture.configureByText("a.k", source);
        PsiReference ref = fixture.getReferenceAtCaretPosition();
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        return ref.resolve();
    }

    /** Returns every declaration the reference at the caret resolves to (poly-variant). */
    private List<PsiElement> multiResolveAtCaret(String source) {
        fixture.configureByText("a.k", source);
        PsiReference ref = fixture.getReferenceAtCaretPosition();
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        assertThat(ref).isInstanceOf(PsiPolyVariantReference.class);
        return Arrays.stream(((PsiPolyVariantReference) ref).multiResolve(false))
                .map(ResolveResult::getElement)
                .toList();
    }

    private static boolean isConstructorOf(PsiElement el, String typeName) {
        if (!(el instanceof KlangFunctionDecl fn) || !typeName.equals(fn.getName())) return false;
        KlangAggregateDecl owner = PsiTreeUtil.getParentOfType(fn, KlangAggregateDecl.class);
        return owner != null && typeName.equals(owner.getName());
    }

    private static boolean isFreeFunctionNamed(PsiElement el, String name) {
        return el instanceof KlangFunctionDecl fn
                && name.equals(fn.getName())
                && PsiTreeUtil.getParentOfType(fn, KlangAggregateDecl.class) == null;
    }

    /** Asserts the target is the constructor function of the named aggregate. */
    private void assertIsConstructorOf(PsiElement target, String typeName) {
        assertThat(target).isInstanceOf(KlangFunctionDecl.class);
        assertThat(((KlangNamedElement) target).getName()).isEqualTo(typeName);
        KlangAggregateDecl owner = PsiTreeUtil.getParentOfType(target, KlangAggregateDecl.class);
        assertThat(owner).as("constructor is a member of its aggregate").isNotNull();
        assertThat(owner.getName()).isEqualTo(typeName);
    }

    @Test
    void constructorCallInInitializerExpressionResolvesToConstructor() {
        onEdt(() -> {
            // 'p : Point = Point(1.0f, 2.0f)' — the RHS Point(args) is a constructor call.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        y: float;
                        Point(px: float, py: float) : x(px), y(py) {}
                    }
                    main() : int {
                        p: Point = Po<caret>int(1.0f, 2.0f);
                        return 0;
                    }
                    """);
            assertIsConstructorOf(target, "Point");
        });
    }

    @Test
    void variableTypeStillResolvesToAggregate() {
        onEdt(() -> {
            // The declared type (left of '=') stays a type reference → the aggregate.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(px: float) : x(px) {}
                    }
                    main() : int {
                        p: Po<caret>int = Point(1.0f);
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void parenthesisedVariableInitResolvesToConstructor() {
        onEdt(() -> {
            // 'p : Point(1.0f)' — the type name is initialised through a constructor call.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(px: float) : x(px) {}
                    }
                    main() : int {
                        p: Po<caret>int(1.0f);
                        return 0;
                    }
                    """);
            assertIsConstructorOf(target, "Point");
        });
    }

    @Test
    void constructorCallAsArgumentResolvesToConstructor() {
        onEdt(() -> {
            // A temporary built inline as a call argument is a constructor call.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(px: float) : x(px) {}
                    }
                    use(p: Point&) : void { }
                    main() : int {
                        use(Po<caret>int(1.0f));
                        return 0;
                    }
                    """);
            assertIsConstructorOf(target, "Point");
        });
    }

    @Test
    void constructorCallWithoutExplicitConstructorFallsBackToType() {
        onEdt(() -> {
            // No explicit constructor declared → the call falls back to the type (so
            // navigation is never lost).
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    main() : int {
                        p: Point = Po<caret>int();
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void parameterTypeStillResolvesToAggregate() {
        onEdt(() -> {
            // A parameter type is a pure type position → the aggregate.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(px: float) : x(px) {}
                    }
                    use(p: Po<caret>int&) : void { }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void newExpressionResolvesToConstructor() {
        onEdt(() -> {
            // 'new Point(args)' is a constructor call → the constructor.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(px: float) : x(px) {}
                    }
                    main() : int {
                        p: Point = new Po<caret>int(1.0f);
                        return 0;
                    }
                    """);
            assertIsConstructorOf(target, "Point");
        });
    }

    @Test
    void newExpressionWithoutConstructorFallsBackToType() {
        onEdt(() -> {
            // No explicit constructor → 'new Point()' falls back to the aggregate type.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    main() : int {
                        p: Point = new Po<caret>int();
                        return 0;
                    }
                    """);
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
        });
    }

    @Test
    void baseInitializerResolvesToBaseConstructor() {
        onEdt(() -> {
            // 'Derived(...) : Base(v)' — the base initializer calls the base constructor.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    class Base {
                        v: int;
                        Base(iv: int) : v(iv) {}
                    }
                    class Derived : public Base {
                        Derived(dv: int) : Ba<caret>se(dv) {}
                    }
                    """);
            assertIsConstructorOf(target, "Base");
        });
    }

    @Test
    void fieldInitializerStillResolvesToField() {
        onEdt(() -> {
            // 'Point(...) : x(px)' — a field initializer references the field, not a constructor.
            PsiElement target = resolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(px: float) : x<caret>(px) {}
                    }
                    """);
            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("x");
        });
    }

    // ── Same-named free function vs. constructor (best-effort arbitration) ─────────

    @Test
    void expressionCallOffersBothConstructorAndSameNamedFreeFunction() {
        onEdt(() -> {
            // A bare 'Point(args)' expression call is ambiguous between the constructor and a
            // same-named free function: without true overload resolution, BOTH are offered.
            List<PsiElement> targets = multiResolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(v: float) {}
                    }
                    Point(a: int) : int { return a; }
                    main() : int {
                        Po<caret>int(1);
                        return 0;
                    }
                    """);
            assertThat(targets).hasSize(2);
            assertThat(targets).anyMatch(t -> isConstructorOf(t, "Point"));
            assertThat(targets).anyMatch(t -> isFreeFunctionNamed(t, "Point"));
        });
    }

    @Test
    void expressionCallWithoutConstructorOffersTypeAndFreeFunction() {
        onEdt(() -> {
            // No explicit constructor: the call still offers the aggregate type (implicit ctor)
            // and the same-named free function.
            List<PsiElement> targets = multiResolveAtCaret("""
                    module demo;
                    struct Point { x: float; }
                    Point(a: int) : int { return a; }
                    main() : int {
                        Po<caret>int(1);
                        return 0;
                    }
                    """);
            assertThat(targets).hasSize(2);
            assertThat(targets).anyMatch(t -> t instanceof KlangAggregateDecl agg && "Point".equals(agg.getName()));
            assertThat(targets).anyMatch(t -> isFreeFunctionNamed(t, "Point"));
        });
    }

    @Test
    void newExpressionExcludesSameNamedFreeFunction() {
        onEdt(() -> {
            // 'new Point(...)' is an unambiguous type position: a free function cannot be
            // allocated, so only the constructor is offered.
            List<PsiElement> targets = multiResolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(v: float) {}
                    }
                    Point(a: int) : int { return a; }
                    main() : int {
                        q : Point = new Po<caret>int(1.0f);
                        return 0;
                    }
                    """);
            assertThat(targets).hasSize(1);
            assertThat(isConstructorOf(targets.get(0), "Point")).isTrue();
        });
    }

    @Test
    void parenthesisedTypeInitExcludesSameNamedFreeFunction() {
        onEdt(() -> {
            // 'p : Point(args)' — 'Point' is the declared *type* of p, so the same-named free
            // function is not a candidate; only the constructor is offered.
            List<PsiElement> targets = multiResolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(v: float) {}
                    }
                    Point(a: int) : int { return a; }
                    main() : int {
                        p : Po<caret>int(1.0f);
                        return 0;
                    }
                    """);
            assertThat(targets).hasSize(1);
            assertThat(isConstructorOf(targets.get(0), "Point")).isTrue();
        });
    }

    @Test
    void pureTypePositionExcludesSameNamedFreeFunction() {
        onEdt(() -> {
            // A pure type position (no call) resolves to the aggregate type only — never the
            // same-named free function.
            List<PsiElement> targets = multiResolveAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        Point(v: float) {}
                    }
                    Point(a: int) : int { return a; }
                    main() : int {
                        p : Po<caret>int;
                        return 0;
                    }
                    """);
            assertThat(targets).hasSize(1);
            assertThat(targets.get(0)).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) targets.get(0)).getName()).isEqualTo("Point");
        });
    }
}



