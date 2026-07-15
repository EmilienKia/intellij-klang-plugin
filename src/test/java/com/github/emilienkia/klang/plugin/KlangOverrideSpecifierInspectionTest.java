package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.inspection.KlangOverrideSpecifierInspection;
import org.junit.jupiter.api.Test;

/**
 * {@link KlangOverrideSpecifierInspection}: flags a method marked {@code override} that does not
 * actually override/implement any base member. Design: {@code docs/klang/abstract-implementation-plan.md} §8.
 */
class KlangOverrideSpecifierInspectionTest extends KlangFixtureTestBase {

    private void check(String source) {
        onEdt(() -> {
            fixture.enableInspections(KlangOverrideSpecifierInspection.class);
            fixture.configureByText("a.k", source);
            fixture.checkHighlighting(true, false, false);
        });
    }

    @Test
    void overrideOfExistingBaseMethodIsAccepted() {
        check("""
                module demo;
                class Animal { sound() : void { } }
                class Dog : public Animal { override sound() : void { } }
                """);
    }

    @Test
    void overrideOfNothingIsFlagged() {
        check("""
                module demo;
                class Animal { sound() : void { } }
                class Dog : public Animal {
                    <warning descr="Method marked 'override' does not override or implement a base member">override</warning> bark() : void { }
                }
                """);
    }

    @Test
    void overrideImplementingInterfaceMethodIsAccepted() {
        check("""
                module demo;
                interface Drawable { draw() : void; }
                class Sprite : public Drawable { override draw() : void { } }
                """);
    }

    @Test
    void noOverrideSpecifierIsNeverFlagged() {
        check("""
                module demo;
                class Animal { sound() : void { } }
                class Dog : public Animal { bark() : void { } }
                """);
    }
}

