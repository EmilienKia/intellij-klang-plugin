package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.inspection.KlangMissingImplementationInspection;
import com.intellij.codeInsight.intention.IntentionAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link KlangMissingImplementationInspection}: flags a concrete class/struct that does not
 * implement all abstract methods inherited (transitively, virtual-only) from base interfaces /
 * abstract classes. Design: {@code docs/klang/abstract-implementation-plan.md}.
 */
class KlangMissingImplementationInspectionTest extends KlangFixtureTestBase {

    private void check(String source) {
        onEdt(() -> {
            fixture.enableInspections(KlangMissingImplementationInspection.class);
            fixture.configureByText("a.k", source);
            fixture.checkHighlighting(true, false, false);
        });
    }

    @Test
    void interfaceMethodNotImplementedIsFlagged() {
        check("""
                module demo;
                interface Drawable { draw() : void; }
                class <error descr="Class does not implement inherited abstract method 'draw'">Sprite</error> : public Drawable { }
                """);
    }

    @Test
    void interfaceMethodImplementedIsAccepted() {
        check("""
                module demo;
                interface Drawable { draw() : void; }
                class Sprite : public Drawable { draw() : void { } }
                """);
    }

    @Test
    void abstractClassLeavesAbstractMethodUnimplementedButIsAccepted() {
        // The intermediate abstract class does not need to implement 'draw' itself.
        check("""
                module demo;
                interface Drawable { draw() : void; }
                abstract class Shape : public Drawable { area() : int { return 0; } }
                """);
    }

    @Test
    void concreteClassMustImplementAbstractBaseClassMethod() {
        check("""
                module demo;
                abstract class Shape { abstract area() : int; describe() : void { } }
                class <error descr="Class does not implement inherited abstract method 'area'">Circle</error> : public Shape { }
                """);
    }

    @Test
    void concreteClassImplementingAbstractBaseClassMethodIsAccepted() {
        check("""
                module demo;
                abstract class Shape { abstract area() : int; describe() : void { } }
                class Circle : public Shape { area() : int { return 1; } }
                """);
    }

    @Test
    void transitiveChainOfThreeLevelsIsCollected() {
        check("""
                module demo;
                interface Drawable { draw() : void; }
                abstract class Shape : public Drawable { abstract area() : int; }
                class <error descr="Class does not implement inherited abstract methods 'area', 'draw'">Circle</error> : public Shape { }
                """);
    }

    @Test
    void defaultInterfaceMethodIsNotRequired() {
        // A 'default' interface member already has a body: it does not need overriding —
        // implementing 'draw' alone (and leaving the default 'describe' untouched) is enough.
        check("""
                module demo;
                interface Drawable {
                    draw() : void;
                    default describe() : void { }
                }
                class Sprite : public Drawable { draw() : void { } }
                """);
    }

    @Test
    void interfaceDeclarationItselfIsNeverFlagged() {
        check("""
                module demo;
                interface Base { draw() : void; }
                interface Drawable : public Base { }
                """);
    }

    @Test
    void diamondSameNameFromTwoInterfacesIsOneSlot() {
        // K matching is name-based (§15): implementing 'tick' once discharges both obligations.
        check("""
                module demo;
                interface A { tick() : void; }
                interface B { tick() : void; }
                class Clock : public A, public B { tick() : void { } }
                """);
    }

    // ── Quick fixes ────────────────────────────────────────────────────────────

    @Test
    void makeAbstractQuickFixInsertsSpecifier() {
        onEdt(() -> {
            fixture.enableInspections(KlangMissingImplementationInspection.class);
            fixture.configureByText("a.k", """
                    module demo;
                    interface Drawable { draw() : void; }
                    class Spr<caret>ite : public Drawable { }
                    """);
            fixture.doHighlighting();
            IntentionAction fix = fixture.findSingleIntention("Make class abstract");
            fixture.launchAction(fix);
            fixture.checkResult("""
                    module demo;
                    interface Drawable { draw() : void; }
                    abstract class Sprite : public Drawable { }
                    """);
        });
    }

    @Test
    void makeAbstractQuickFixInsertsBeforeExistingSpecifiers() {
        onEdt(() -> {
            fixture.enableInspections(KlangMissingImplementationInspection.class);
            fixture.configureByText("a.k", """
                    module demo;
                    interface Drawable { draw() : void; }
                    public class Spr<caret>ite : public Drawable { }
                    """);
            fixture.doHighlighting();
            IntentionAction fix = fixture.findSingleIntention("Make class abstract");
            fixture.launchAction(fix);
            fixture.checkResult("""
                    module demo;
                    interface Drawable { draw() : void; }
                    public abstract class Sprite : public Drawable { }
                    """);
        });
    }

    @Test
    void implementMissingMethodsQuickFixGeneratesOverrideStub() {
        onEdt(() -> {
            fixture.enableInspections(KlangMissingImplementationInspection.class);
            fixture.configureByText("a.k", """
                    module demo;
                    interface Drawable { draw() : void; }
                    class Spr<caret>ite : public Drawable { }
                    """);
            fixture.doHighlighting();
            IntentionAction fix = fixture.findSingleIntention("Implement missing methods");
            fixture.launchAction(fix);

            String text = fixture.getFile().getText();
            assertThat(text).contains("override draw() : void");
            assertThat(text).contains("implement inherited abstract member from Drawable");
            // The class must no longer be flagged afterwards.
            fixture.checkHighlighting(true, false, false);
        });
    }

    @Test
    void implementMissingMethodsQuickFixCopiesParametersAndReturnType() {
        onEdt(() -> {
            fixture.enableInspections(KlangMissingImplementationInspection.class);
            fixture.configureByText("a.k", """
                    module demo;
                    interface Calculator { add(a : int, b : int) : int; }
                    class Spr<caret>ite : public Calculator { }
                    """);
            fixture.doHighlighting();
            IntentionAction fix = fixture.findSingleIntention("Implement missing methods");
            fixture.launchAction(fix);

            String text = fixture.getFile().getText();
            assertThat(text).contains("override add(a : int, b : int) : int");
        });
    }
}





