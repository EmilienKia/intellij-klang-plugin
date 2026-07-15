package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.inspection.KlangExceptionTypeInspection;
import org.junit.jupiter.api.Test;

/**
 * Phase 4 — {@link KlangExceptionTypeInspection}: validates that {@code throw} / {@code catch} /
 * {@code throws} exception types derive from {@code ::k::Throwable}. Each fixture declares a local
 * {@code namespace k { class Throwable {} }} so the inspection's base type is resolvable (it is
 * otherwise gated off and silent — fail soft).
 */
class KlangExceptionTypeInspectionTest extends KlangFixtureTestBase {

    /** Common preamble: a resolvable {@code ::k::Throwable} plus derived / unrelated types. */
    private static final String PREAMBLE = """
            module demo;
            namespace k {
                class Throwable {}
                class Exception : k::Throwable {}
            }
            class MyError : k::Exception {}
            class NotAnError {}
            """;

    private void check(String body) {
        onEdt(() -> {
            fixture.enableInspections(KlangExceptionTypeInspection.class);
            fixture.configureByText("a.k", PREAMBLE + body);
            fixture.checkHighlighting(true, false, false);
        });
    }

    @Test
    void throwsClauseDerivedTypeIsAccepted() {
        check("risky() throws MyError {}\n");
    }

    @Test
    void throwsClauseNonThrowableIsFlagged() {
        check("risky() throws <warning descr=\"Exception type 'NotAnError' does not derive from '::k::Throwable'\">NotAnError</warning> {}\n");
    }

    @Test
    void throwsClauseFundamentalTypeIsFlagged() {
        check("risky() throws <warning descr=\"Exception type 'int' does not derive from '::k::Throwable'\">int</warning> {}\n");
    }

    @Test
    void catchDerivedTypeIsAccepted() {
        check("""
                f() {
                    try {} catch (e : MyError&) {}
                }
                """);
    }

    @Test
    void catchNonThrowableIsFlagged() {
        check("""
                f() {
                    try {} catch (e : <warning descr="Exception type 'NotAnError' does not derive from '::k::Throwable'">NotAnError</warning>&) {}
                }
                """);
    }

    @Test
    void throwDerivedVariableIsAccepted() {
        check("""
                f() {
                    e : MyError;
                    throw e;
                }
                """);
    }

    @Test
    void throwNonThrowableVariableIsFlagged() {
        check("""
                f() {
                    e : NotAnError;
                    throw <warning descr="Exception type 'NotAnError' does not derive from '::k::Throwable'">e</warning>;
                }
                """);
    }

    @Test
    void bareRethrowIsAccepted() {
        check("""
                f() {
                    try {} catch (e : MyError&) { throw; }
                }
                """);
    }
}

