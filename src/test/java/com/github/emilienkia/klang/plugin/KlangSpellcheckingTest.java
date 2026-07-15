package com.github.emilienkia.klang.plugin;

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link com.github.emilienkia.klang.plugin.language.KlangSpellcheckingStrategy}.
 *
 * <p>Verifies that:</p>
 * <ul>
 *   <li>Typos in <b>line / block / doc comments</b> are flagged and correct text is not.</li>
 *   <li>Typos in <b>string literals</b> are flagged; the surrounding quotes and escape sequences
 *       are excluded.</li>
 *   <li>Typos in <b>identifiers</b> — single-word, camelCase-split and snake_case-split — are
 *       flagged; correctly spelled identifiers are not.</li>
 *   <li><b>K-lang keywords</b>, numeric literals and character literals are never flagged.</li>
 * </ul>
 *
 * <p>Test markup uses {@code <TYPO descr="Typo: In word 'word'">word</TYPO>} (the standard
 * IntelliJ spellchecker format). {@link org.assertj.core.api.Assertions#checkHighlighting} is
 * called with {@code checkWeakWarnings=true} since TYPO severity maps to weak-warning level.</p>
 */
class KlangSpellcheckingTest extends KlangFixtureTestBase {

    @BeforeEach
    void enableSpellchecking() {
        // GrazieSpellCheckingInspection is the concrete implementation of the abstract
        // SpellCheckingInspection, registered by the 'tanvd.grazi' bundled plugin with
        // shortName="SpellCheckingInspection". Adding bundledPlugin("tanvd.grazi") to
        // build.gradle.kts makes it available and registered in the test environment.
        onEdt(() -> fixture.enableInspections(GrazieSpellCheckingInspection.class));
    }

    /**
     * Configures {@code a.k} with the given source (which may contain
     * {@code <TYPO>…</TYPO>} markers) and checks that the actual TYPO highlights match exactly.
     */
    private void check(String source) {
        onEdt(() -> {
            fixture.configureByText("a.k", source);
            fixture.checkHighlighting(false, false, true);
        });
    }

    // ── Comments ──────────────────────────────────────────────────────────────────────────────

    @Test
    void lineCommentTypoFlagged() {
        // "writting" is a reliable misspelling (confirmed by singleWordIdentifierTypoFlagged)
        check("// <TYPO descr=\"Typo: In word 'writting'\">writting</TYPO> world\n");
    }

    @Test
    void blockCommentTypoFlagged() {
        // "blokk" is not a dictionary word
        check("/* <TYPO descr=\"Typo: In word 'blokk'\">blokk</TYPO> */\n");
    }

    @Test
    void lineDocCommentTypoFlagged() {
        // "documantation" is a misspelling of "documentation"
        check("/// <TYPO descr=\"Typo: In word 'documantation'\">documantation</TYPO>\n");
    }

    @Test
    void blockDocCommentTypoFlagged() {
        // Same misspelling in a Javadoc-style block comment
        check("/** <TYPO descr=\"Typo: In word 'documantation'\">documantation</TYPO> */\n");
    }

    @Test
    void correctCommentNotFlagged() {
        // No typo markers expected — checkHighlighting fails if any TYPO annotation appears
        check("// hello world, this is correct text\n");
    }

    // ── String literals ───────────────────────────────────────────────────────────────────────

    @Test
    void stringLiteralTypoFlagged() {
        // "wrold" is a misspelling of "world"; sits inside "…" which our tokenizer strips
        check("x : const char* = \"<TYPO descr=\"Typo: In word 'wrold'\">wrold</TYPO>\";\n");
    }

    @Test
    void correctStringNotFlagged() {
        check("x : const char* = \"hello world\";\n");
    }

    @Test
    void emptyStringNotFlagged() {
        check("x : const char* = \"\";\n");
    }

    @Test
    void stringEscapeSequencesNotFlagged() {
        // Escape sequences (\n, \t, \\, \") are not dictionary words — PlainTextSplitter ignores them
        check("x : const char* = \"\\n\\t\\\\\";\n");
    }

    // ── Identifiers ───────────────────────────────────────────────────────────────────────────

    @Test
    void singleWordIdentifierTypoFlagged() {
        // "writting" is a misspelling of "writing" — one-word function name
        check("<TYPO descr=\"Typo: In word 'writting'\">writting</TYPO>() {}\n");
    }

    @Test
    void camelCaseSegmentTypoFlagged() {
        // "getDistence": IdentifierSplitter splits to "get" (3 chars → below MINIMAL_TYPO_LENGTH,
        // skipped) and "Distence" (misspelling of "Distance" → flagged).
        check("get<TYPO descr=\"Typo: In word 'Distence'\">Distence</TYPO>() {}\n");
    }

    @Test
    void snakeCaseSegmentTypoFlagged() {
        // "bad_writting": splits to "bad" (3 chars → skipped) and "writting" (flagged).
        check("bad_<TYPO descr=\"Typo: In word 'writting'\">writting</TYPO> : int;\n");
    }

    @Test
    void correctIdentifierNotFlagged() {
        // "calculateDistance" → "calculate" + "Distance" — both in the dictionary
        check("calculateDistance() {}\n");
    }

    @Test
    void shortIdentifierNotFlagged() {
        // Single-letter params are below IdentifierSplitter.MINIMAL_TYPO_LENGTH (4) — never flagged
        check("f(x : int, y : int) : int { return x + y; }\n");
    }

    // ── Keywords, numeric & char literals ────────────────────────────────────────────────────

    @Test
    void keywordsNotFlagged() {
        // K-lang keywords use dedicated token types (KW_STRUCT, KW_CLASS, …), not IDENTIFIER,
        // so they never reach our tokenizer — no TYPO regardless of spelling.
        check("struct Foo {}\n");
    }

    @Test
    void numericLiteralNotFlagged() {
        check("x : int = 42;\n");
    }

    @Test
    void charLiteralNotFlagged() {
        // LIT_CHAR returns EMPTY_TOKENIZER — single character, no spell-check
        check("c : char = 'z';\n");
    }
}






