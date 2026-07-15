package com.github.emilienkia.klang.plugin;

import com.intellij.codeInsight.daemon.GutterMark;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reverse navigation for operator overloads: a gutter marker on an {@code operator <sym>} /
 * {@code operator[]} declaration navigates to the usages that resolve back to it
 * ({@code KlangOperatorUsageLineMarkerProvider}). Mirror of the usage → definition jump in
 * {@code KlangOperatorGotoDeclarationHandler}; both go through {@code KlangOperatorUtil} so they
 * stay exact inverses.
 */
class KlangOperatorUsageLineMarkerTest extends KlangFixtureTestBase {

    /** Tooltips of all gutter marks at the (single) caret, after running highlighting. */
    private List<String> gutterTooltipsAtCaret(String source) {
        fixture.configureByText("a.k", source);
        List<GutterMark> gutters = fixture.findGuttersAtCaret();
        return gutters.stream().map(GutterMark::getTooltipText).toList();
    }

    // ── Subscript operator ────────────────────────────────────────────────────

    @Test
    void usedSubscriptOperatorGetsUsagesMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Buffer {
                        op<caret>erator [](i : int) : int& { return i; }
                    }
                    main() : int {
                        b: Buffer;
                        x: int = b[0];
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator [] usages"));
        });
    }

    @Test
    void inheritedSubscriptOperatorUsageIsFound() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class Base {
                        op<caret>erator [](i : int) : int& { return i; }
                    }
                    class Derived : public Base { }
                    main() : int {
                        d: Derived;
                        x: int = d[0];
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator [] usages"));
        });
    }

    // ── Binary operator ───────────────────────────────────────────────────────

    @Test
    void usedBinaryOperatorGetsUsagesMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        op<caret>erator ==(o: Point&) : bool { return true; }
                    }
                    main() : int {
                        p: Point;
                        q: Point;
                        b: bool = p == q;
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator == usages"));
        });
    }

    @Test
    void inheritedBinaryOperatorUsageIsFound() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class Base {
                        op<caret>erator ==(o: Base&) : bool { return true; }
                    }
                    class Derived : public Base { }
                    main() : int {
                        d: Derived;
                        e: Derived;
                        b: bool = d == e;
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator == usages"));
        });
    }

    @Test
    void freeBinaryOperatorUsageIsFound() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Vec { x: float; }
                    op<caret>erator +(a: Vec&, b: Vec&) : Vec { return a; }
                    main() : int {
                        u: Vec;
                        v: Vec;
                        w: Vec = u + v;
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator + usages"));
        });
    }

    // ── Prefix unary operator ─────────────────────────────────────────────────

    @Test
    void usedPrefixUnaryOperatorGetsUsagesMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Vec {
                        x: float;
                        op<caret>erator -() : Vec { return this; }
                    }
                    main() : int {
                        v: Vec;
                        w: Vec = -v;
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator - usages"));
        });
    }

    // ── Postfix / prefix ++ operators ─────────────────────────────────────────

    @Test
    void usedPostfixIncrementOperatorGetsUsagesMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Counter {
                        n: int;
                        op<caret>erator _++() : Counter { return this; }
                    }
                    main() : int {
                        c: Counter;
                        c++;
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator _++ usages"));
        });
    }

    @Test
    void usedPrefixIncrementOperatorGetsUsagesMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Counter {
                        n: int;
                        op<caret>erator ++_() : Counter { return this; }
                    }
                    main() : int {
                        c: Counter;
                        d: Counter = ++c;
                        return 0;
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("operator ++_ usages"));
        });
    }

    // ── Negative cases ────────────────────────────────────────────────────────

    @Test
    void unusedOperatorHasNoUsagesMarker() {
        onEdt(() -> {
            // operator declared but never used → no reverse-navigation marker.
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        op<caret>erator ==(o: Point&) : bool { return true; }
                    }
                    """);
            assertThat(tooltips).noneSatisfy(t ->
                    assertThat(t).isNotNull().contains("usages"));
        });
    }

    @Test
    void castOperatorHasNoUsagesMarker() {
        onEdt(() -> {
            // The cast/conversion 'operator ()' is out of scope for reverse navigation.
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    struct Wrapper {
                        value : int;
                        op<caret>erator () : int { return value; }
                    }
                    main() : int {
                        w: Wrapper;
                        return 0;
                    }
                    """);
            assertThat(tooltips).noneSatisfy(t ->
                    assertThat(t).isNotNull().contains("usages"));
        });
    }
}


