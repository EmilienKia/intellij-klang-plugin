package com.github.emilienkia.klang.plugin;

import com.intellij.codeInsight.daemon.GutterMark;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B1 — verifies the upward inheritance gutter markers produced by
 * {@code KlangInheritanceLineMarkerProvider}: a method that redefines a base method gets an
 * <em>"overrides / implements"</em> icon, an aggregate with bases gets a <em>"go to super
 * type"</em> icon, and unrelated declarations get none.
 */
class KlangInheritanceLineMarkerTest extends KlangFixtureTestBase {

    /** Tooltips of all gutter marks at the (single) caret, after running highlighting. */
    private List<String> gutterTooltipsAtCaret(String source) {
        fixture.configureByText("a.k", source);
        List<GutterMark> gutters = fixture.findGuttersAtCaret();
        return gutters.stream().map(GutterMark::getTooltipText).toList();
    }

    @Test
    void overridingMethodGetsOverrideMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class Animal {
                        sound() : void { }
                    }
                    class Dog : public Animal {
                        so<caret>und() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Overrides method"));
        });
    }

    @Test
    void interfaceMethodImplementationGetsImplementsMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    interface Drawable {
                        draw() : void;
                    }
                    class Sprite : public Drawable {
                        dr<caret>aw() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Implements method"));
        });
    }

    @Test
    void unoverriddenDefaultInterfaceMethodHasNoOverrideOrVirtualMarker() {
        onEdt(() -> {
            // A default-implementation interface method with no overrides should look/behave
            // like a plain abstract interface method: no "overrides"/"virtual" style marker.
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    interface Drawable {
                        default dr<caret>aw() : void { }
                    }
                    """);
            assertThat(tooltips).noneSatisfy(t ->
                    assertThat(t).isNotNull().containsAnyOf("Overrides method", "Virtual method"));
        });
    }

    @Test
    void overriddenDefaultInterfaceMethodGetsImplementedByMarker() {
        onEdt(() -> {
            // Caret on the default interface method that a class overrides →
            // "Implemented in subtype(s)", navigating down to the override(s).
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    interface Drawable {
                        default dr<caret>aw() : void { }
                    }
                    class Sprite : public Drawable {
                        draw() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Implemented in subtype"));
        });
    }

    @Test
    void derivedAggregateGetsSuperTypeMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class Animal { name: int; }
                    class D<caret>og : public Animal {
                        bark() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("super type"));
        });
    }

    @Test
    void standaloneMethodHasNoInheritanceMarker() {
        onEdt(() -> {
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class Animal {
                        so<caret>und() : void { }
                    }
                    """);
            assertThat(tooltips).noneSatisfy(t ->
                    assertThat(t).isNotNull().containsAnyOf("Overrides method", "Implements method"));
        });
    }

    // ── B2 — downward markers (file-local) ────────────────────────────────────

    @Test
    void overriddenBaseMethodGetsOverriddenByMarker() {
        onEdt(() -> {
            // Caret on the base method that a subclass overrides → "Overridden in subtype(s)".
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class Animal {
                        so<caret>und() : void { }
                    }
                    class Dog : public Animal {
                        sound() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Overridden in subtype"));
        });
    }

    @Test
    void implementedInterfaceMethodGetsImplementedByMarker() {
        onEdt(() -> {
            // Caret on the interface method that a class implements → "Implemented in subtype(s)".
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    interface Drawable {
                        dr<caret>aw() : void;
                    }
                    class Sprite : public Drawable {
                        draw() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Implemented in subtype"));
        });
    }

    @Test
    void baseAggregateGetsSubtypesMarker() {
        onEdt(() -> {
            // Caret on the base class that another class derives from → "Subclassed by subtype(s)".
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class An<caret>imal { name: int; }
                    class Dog : public Animal {
                        bark() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Subclassed by subtype"));
        });
    }

    @Test
    void interfaceGetsImplementedByMarker() {
        onEdt(() -> {
            // Caret on the interface that a class implements → "Implemented by subtype(s)".
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    interface Dr<caret>awable { draw() : void; }
                    class Sprite : public Drawable {
                        draw() : void { }
                    }
                    """);
            assertThat(tooltips).anySatisfy(t ->
                    assertThat(t).isNotNull().contains("Implemented by subtype"));
        });
    }

    @Test
    void leafAggregateHasNoDownwardMarker() {
        onEdt(() -> {
            // The most-derived class has no subtypes → no downward marker.
            List<String> tooltips = gutterTooltipsAtCaret("""
                    module demo;
                    class Animal { name: int; }
                    class D<caret>og : public Animal {
                        bark() : void { }
                    }
                    """);
            assertThat(tooltips).noneSatisfy(t ->
                    assertThat(t).isNotNull().containsAnyOf("Subclassed by subtype", "Implemented by subtype"));
        });
    }
}

