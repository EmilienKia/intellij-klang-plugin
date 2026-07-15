package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.inspection.KlangUnresolvedReferenceInspection;
import com.intellij.codeInsight.intention.IntentionAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 4 — {@link KlangUnresolvedReferenceInspection}: flags references whose {@code multiResolve}
 * is empty (the spec's {@code Undefined symbol}, §16), while staying fail-soft for fundamentals,
 * keywords and the §16 <em>deferred resolution</em> rule (a call callee may be a unified-call
 * candidate). Also covers the {@code Add import} quick fix.
 */
class KlangUnresolvedReferenceInspectionTest extends KlangFixtureTestBase {

    /** Enables the inspection and asserts the highlighting markup embedded in {@code source}. */
    private void check(String source) {
        onEdt(() -> {
            fixture.enableInspections(KlangUnresolvedReferenceInspection.class);
            fixture.configureByText("a.k", source);
            fixture.checkHighlighting(false, false, false); // errors only
        });
    }

    // ── Flagged: genuinely unresolved references ──────────────────────────────

    @Test
    void unresolvedExpressionNameIsFlagged() {
        check("""
                module demo;
                f() {
                    y : int = <error descr="Cannot resolve symbol 'mystery'">mystery</error>;
                }
                """);
    }

    @Test
    void unresolvedTypeIsFlagged() {
        check("""
                module demo;
                f() {
                    v : <error descr="Cannot resolve symbol 'Wibble'">Wibble</error>;
                }
                """);
    }

    @Test
    void unresolvedBaseTypeIsFlagged() {
        check("""
                module demo;
                class C : <error descr="Cannot resolve symbol 'Nope'">Nope</error> {}
                """);
    }

    @Test
    void unresolvedMemberAccessIsFlagged() {
        check("""
                module demo;
                struct Point { x : int; }
                f() {
                    p : Point;
                    q : int = p.<error descr="Cannot resolve symbol 'nope'">nope</error>;
                }
                """);
    }

    @Test
    void qualifiedUnresolvedTailIsFlagged() {
        check("""
                module demo;
                namespace ns {}
                f() {
                    v : ns::<error descr="Cannot resolve symbol 'Missing'">Missing</error>;
                }
                """);
    }

    // ── Not flagged: resolved references ──────────────────────────────────────

    @Test
    void resolvedNamesAreAccepted() {
        check("""
                module demo;
                struct Point { x : int; }
                f() {
                    a : int = 0;
                    b : int = a;
                    p : Point;
                    q : int = p.x;
                }
                """);
    }

    @Test
    void fundamentalTypeIsNotFlagged() {
        check("""
                module demo;
                f() {
                    v : int;
                    w : float;
                }
                """);
    }

    // ── Not flagged: array virtual member 'size' (§9.8) ───────────────────────

    @Test
    void arraySizeMemberIsNotFlagged() {
        // 'size' is the array virtual member — it has no declaration but must not be flagged.
        check("""
                module demo;
                f(a : int[5]&) : unsigned int {
                    return a.size;
                }
                """);
    }

    @Test
    void arraySizeThroughUnionChainIsNotFlagged() {
        // other._store.utf16.size — receiver is an array alternative of a nested union.
        // Regression for the libk String copy-constructor case.
        check("""
                module demo;
                class String {
                    union CharStore {
                        utf16 : unsigned short[]!;
                        utf32 : char[]!;
                    }
                    _store : CharStore;
                    copy(other : const String&) : unsigned int {
                        return other._store.utf16.size;
                    }
                }
                """);
    }

    @Test
    void nonArraySizeMemberIsStillFlagged() {
        // 'size' on a non-array aggregate has no such virtual member → still flagged.
        check("""
                module demo;
                struct Point { x : int; }
                f() {
                    p : Point;
                    n : int = p.<error descr="Cannot resolve symbol 'size'">size</error>;
                }
                """);
    }

    // ── Not flagged: deferred resolution (§16) ────────────────────────────────

    @Test
    void unresolvedCallCalleeIsNotFlagged() {
        // 'mysteryFn' is the callee of a call → may be a unified-call/overload candidate (§16).
        check("""
                module demo;
                f() {
                    mysteryFn();
                }
                """);
    }

    @Test
    void unresolvedMemberCallCalleeIsNotFlagged() {
        // 'doStuff' is the callee of a method-style call on a known receiver → deferred (§16).
        check("""
                module demo;
                struct Point { x : int; }
                f() {
                    p : Point;
                    p.doStuff();
                }
                """);
    }

    // ── Add-import quick fix ──────────────────────────────────────────────────

    @Test
    void addImportQuickFixInsertsTheDeclaringModule() {
        onEdt(() -> {
            addFile("lib.k", "module mylib;\nclass Widget {}\n");
            fixture.enableInspections(KlangUnresolvedReferenceInspection.class);
            fixture.configureByText("a.k", "module app;\nf() { w : <caret>Widget; }\n");

            IntentionAction fix = fixture.findSingleIntention("Add import for 'Widget'");
            fixture.launchAction(fix);

            String result = fixture.getFile().getText();
            assertThat(result).contains("import mylib;");
            assertThat(result).contains("module app;");
        });
    }

    @Test
    void addImportQuickFixIsAbsentWhenNoModuleDeclaresTheSymbol() {
        onEdt(() -> {
            fixture.enableInspections(KlangUnresolvedReferenceInspection.class);
            fixture.configureByText("a.k", "module app;\nf() { w : <caret>Widget; }\n");

            assertThat(fixture.filterAvailableIntentions("Add import for 'Widget'")).isEmpty();
        });
    }
}


