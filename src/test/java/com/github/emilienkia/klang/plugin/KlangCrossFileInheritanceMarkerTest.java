package com.github.emilienkia.klang.plugin;

import com.intellij.codeInsight.daemon.GutterMark;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multi-file indexing — milestone M4 (TODO B3). Inheritance gutter markers now span the whole
 * project: a base type / method in one file shows downward (<em>subclassed / overridden by</em>)
 * markers for subtypes declared in <em>other</em> files of the module, and upward markers
 * resolve across files too. Backed by {@code KlangBaseNameIndex}.
 */
class KlangCrossFileInheritanceMarkerTest extends KlangFixtureTestBase {

    /** Adds {@code deps}, configures the caret file, runs highlighting and returns gutter tooltips. */
    private List<String> gutterTooltipsAtCaret(String caretSource, String... deps) {
        for (int i = 0; i + 1 < deps.length; i += 2) {
            addFile(deps[i], deps[i + 1]);
        }
        fixture.configureByText("main.k", caretSource);
        return fixture.findGuttersAtCaret().stream().map(GutterMark::getTooltipText).toList();
    }

    // ── Downward markers across files ─────────────────────────────────────────

    @Test
    void baseClassSubclassedInAnotherFileGetsSubtypeMarker() {
        onEdt(() -> {
            // 'Animal' is in main.k; 'Dog : public Animal' is declared in sub.k (same module).
            List<String> tooltips = gutterTooltipsAtCaret("""
                            module demo;
                            class An<caret>imal { name: int; }
                            """,
                    "sub.k", "module demo;\nclass Dog : public Animal { bark() : void { } }");

            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Subclassed by subtype"));
        });
    }

    @Test
    void baseMethodOverriddenInAnotherFileGetsOverriddenByMarker() {
        onEdt(() -> {
            // 'Animal.sound' is overridden by 'Dog.sound' in sub.k.
            List<String> tooltips = gutterTooltipsAtCaret("""
                            module demo;
                            class Animal { so<caret>und() : void { } }
                            """,
                    "sub.k", "module demo;\nclass Dog : public Animal { sound() : void { } }");

            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Overridden in subtype"));
        });
    }

    @Test
    void interfaceImplementedInAnotherFileGetsImplementedByMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                            module demo;
                            interface Dr<caret>awable { draw() : void; }
                            """,
                    "sub.k", "module demo;\nclass Sprite : public Drawable { draw() : void { } }");

            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Implemented by subtype"));
        });
    }

    @Test
    void transitiveSubtypeAcrossThreeFilesIsMarked() {
        onEdt(() -> {
            // A (main.k) ← B (b.k) ← C (c.k): the marker on A must reflect the transitive subtype.
            List<String> tooltips = gutterTooltipsAtCaret("""
                            module demo;
                            class <caret>A { x: int; }
                            """,
                    "b.k", "module demo;\nclass B : public A { }",
                    "c.k", "module demo;\nclass C : public B { }");

            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Subclassed by subtype"));
        });
    }

    // ── Upward marker across files ────────────────────────────────────────────

    @Test
    void methodOverridingBaseInAnotherFileGetsOverrideMarker() {
        onEdt(() -> {
            // The subclass and its overriding method are in main.k; the base is in lib.k.
            List<String> tooltips = gutterTooltipsAtCaret("""
                            module demo;
                            class Dog : public Animal {
                                so<caret>und() : void { }
                            }
                            """,
                    "lib.k", "module demo;\nclass Animal { sound() : void { } }");

            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Overrides method"));
        });
    }

    // ── Isolation — a different module does not produce markers ───────────────

    @Test
    void subtypeInDifferentModuleDoesNotMark() {
        onEdt(() -> {
            // The other-module class names 'Animal' as a base, but without importing module
            // 'demo' it does not resolve to this Animal → no downward marker here.
            List<String> tooltips = gutterTooltipsAtCaret("""
                            module demo;
                            class An<caret>imal { name: int; }
                            """,
                    "other.k", "module other;\nclass Beast : public Animal { }");

            assertThat(tooltips).noneSatisfy(t ->
                    assertThat(t).isNotNull().containsAnyOf("Subclassed by subtype", "Implemented by subtype"));
        });
    }
}

