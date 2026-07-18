package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.inspection.KlangFinalOnCtorDtorInspection;
import org.junit.jupiter.api.Test;

/**
 * {@link KlangFinalOnCtorDtorInspection}: flags the {@code final} specifier on a constructor
 * or destructor, mirroring the klangc compiler's {@code ERR_FINAL_ON_CTOR_DTOR} (0x0195)
 * diagnostic added alongside the existing 'override'/'default' restrictions.
 */
class KlangFinalOnCtorDtorInspectionTest extends KlangFixtureTestBase {

    private void check(String source) {
        onEdt(() -> {
            fixture.enableInspections(KlangFinalOnCtorDtorInspection.class);
            fixture.configureByText("a.k", source);
            fixture.checkHighlighting(true, false, false);
        });
    }

    @Test
    void finalOnClassConstructorIsFlagged() {
        check("""
                module demo;
                class Widget {
                    <error descr="Specifier 'final' is not allowed on a constructor or destructor">final</error> Widget() { }
                }
                """);
    }

    @Test
    void finalOnClassDestructorIsFlagged() {
        check("""
                module demo;
                class Widget {
                    <error descr="Specifier 'final' is not allowed on a constructor or destructor">final</error> ~Widget() { }
                }
                """);
    }

    @Test
    void finalOnStructConstructorIsFlagged() {
        check("""
                module demo;
                struct Point {
                    <error descr="Specifier 'final' is not allowed on a constructor or destructor">final</error> Point() { }
                }
                """);
    }

    @Test
    void finalOnRegularMethodIsAccepted() {
        check("""
                module demo;
                class Widget {
                    final draw() : void { }
                }
                """);
    }

    @Test
    void noFinalSpecifierIsNeverFlagged() {
        check("""
                module demo;
                class Widget {
                    Widget() { }
                    ~Widget() { }
                }
                """);
    }
}
