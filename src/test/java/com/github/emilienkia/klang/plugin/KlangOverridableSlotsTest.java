package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link KlangResolveUtil#collectOverridableSlots}: the candidate-collection logic backing the
 * Alt+Insert "Override/Implement Members…" Generate action
 * ({@code KlangOverrideImplementMembersAction}). The action's UI (a modal
 * {@code com.intellij.ide.util.MemberChooser}) isn't exercised here — this covers the underlying,
 * UI-independent candidate set. Design: {@code docs/klang/abstract-implementation-plan.md} §7.
 */
class KlangOverridableSlotsTest extends KlangFixtureTestBase {

    private KlangAggregateDecl aggregate(KlangFile file, String name) {
        for (KlangAggregateDecl agg : PsiTreeUtil.findChildrenOfType(file, KlangAggregateDecl.class)) {
            if (name.equals(agg.getName())) return agg;
        }
        throw new AssertionError("No aggregate named " + name);
    }

    @Test
    void includesBothAbstractAndConcreteInheritedMethods() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    interface Drawable { draw() : void; }
                    class Shape : public Drawable { area() : int { return 0; } }
                    class Circle : public Shape { }
                    """);
            Map<String, KlangFunctionDecl> slots =
                    KlangResolveUtil.collectOverridableSlots(aggregate(file, "Circle"));

            assertThat(slots).containsKeys("draw", "area");
            assertThat(KlangResolveUtil.requiresImplementation(slots.get("draw"))).isTrue();
            assertThat(KlangResolveUtil.requiresImplementation(slots.get("area"))).isFalse();
        });
    }

    @Test
    void excludesMethodsAlreadyDeclaredLocally() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    interface Drawable { draw() : void; }
                    class Sprite : public Drawable { draw() : void { } }
                    """);
            Map<String, KlangFunctionDecl> slots =
                    KlangResolveUtil.collectOverridableSlots(aggregate(file, "Sprite"));

            assertThat(slots).doesNotContainKey("draw");
        });
    }

    @Test
    void excludesConstructorsDestructorsAndStaticMembers() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    class Base {
                        Base() { }
                        ~Base() { }
                        static util() : void { }
                        greet() : void { }
                    }
                    class Derived : public Base { }
                    """);
            Map<String, KlangFunctionDecl> slots =
                    KlangResolveUtil.collectOverridableSlots(aggregate(file, "Derived"));

            assertThat(slots).containsKey("greet");
            assertThat(slots).doesNotContainKeys("Base", "~Base", "util");
        });
    }

    @Test
    void emptyForAggregateWithoutBases() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    class Standalone { greet() : void { } }
                    """);
            assertThat(KlangResolveUtil.collectOverridableSlots(aggregate(file, "Standalone"))).isEmpty();
        });
    }
}

