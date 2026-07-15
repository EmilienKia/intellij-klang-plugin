package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.spellchecker.inspections.IdentifierSplitter;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

/**
 * Spellchecking strategy for K-lang ({@code .k} files).
 *
 * <p>Three categories of text are spell-checked:</p>
 * <ul>
 *   <li><b>Comments</b> (line, block, line-doc and block-doc) -- delegated to the platform's
 *       built-in {@code CommentTokenizer} via {@link #super} ({@code myCommentTokenizer}):
 *       it strips comment markers, ignores @tags, URLs and inline code spans natively.</li>
 *   <li><b>Identifiers</b> -- every leaf {@code IDENTIFIER} token (declaration names, expression
 *       usages, member names, ...) is split on camelCase / snake_case / SCREAMING_SNAKE boundaries
 *       by {@link IdentifierSplitter}. Each resulting word is checked independently. The rename
 *       refactoring is offered as a fix ({@code useRename=true}) so a correction propagates to all
 *       usages automatically.</li>
 *   <li><b>String literals</b> -- the raw content between the surrounding {@code "..."} quotes is
 *       fed to {@link PlainTextSplitter}. Escape sequences ({@code \n}, {@code \t}, {@code \\},
 *       ...) are skipped silently: they are not dictionary words.</li>
 * </ul>
 *
 * <p>Everything else (keywords, operators, numeric/character literals, punctuators) receives
 * {@link #EMPTY_TOKENIZER} and is never spell-checked.</p>
 *
 * <p>This strategy is registered in {@code plugin.xml} as a {@code spellchecker.support}
 * extension for the K-lang language.</p>
 */
public final class KlangSpellcheckingStrategy extends SpellcheckingStrategy {

    // ── Identifier tokenizer ──────────────────────────────────────────────────────────────────

    /**
     * Spell-checks a single {@code IDENTIFIER} leaf token by splitting it on camelCase /
     * snake_case / SCREAMING_SNAKE boundaries.
     *
     * <p>{@code useRename=true}: when the user accepts a "Change to …" suggestion, the IDE fires
     * the full rename refactoring so every reference is updated, not just the caret location.</p>
     */
    private static final Tokenizer<PsiElement> IDENTIFIER_TOKENIZER = new Tokenizer<>() {
        @Override
        public void tokenize(@NotNull PsiElement element, @NotNull TokenConsumer consumer) {
            consumer.consumeToken(element, true, IdentifierSplitter.getInstance());
        }
    };

    // ── String literal tokenizer ──────────────────────────────────────────────────────────────

    /**
     * Spell-checks only the content inside a {@code "…"} string literal, skipping the
     * surrounding quote characters.
     *
     * <p>The {@code offset=1} argument passed to
     * {@link TokenConsumer#consumeToken(PsiElement, String, boolean, int, TextRange,
     * com.intellij.spellchecker.inspections.Splitter) consumeToken} maps each word range back to
     * the correct position in the source file, so squiggles appear under the right letters.</p>
     *
     * <p>Escape sequences ({@code \n}, {@code \t}, …) are treated as non-word characters by
     * {@link PlainTextSplitter} and therefore silently skipped — no false positives.</p>
     */
    private static final Tokenizer<PsiElement> STRING_TOKENIZER = new Tokenizer<>() {
        @Override
        public void tokenize(@NotNull PsiElement element, @NotNull TokenConsumer consumer) {
            String raw = element.getText();
            // Guard: must be at least two chars and start/end with a double-quote
            if (raw.length() < 2 || raw.charAt(0) != '"' || raw.charAt(raw.length() - 1) != '"') {
                return;
            }
            // Feed [1, len-1) to PlainTextSplitter with the correct source offset.
            // offset=0: the element text itself starts at position 0 within the element.
            // range=[1, len-1]: skips the leading and trailing quote characters.
            // PlainTextSplitter will extract "wrold" from "\"wrold\"", producing a TextRange
            // within the range — the framework adds element.getTextOffset()+0+rangeStart to
            // compute the file-level position, so the squiggle appears under the right letters.
            consumer.consumeToken(element, raw, false,
                    0, TextRange.create(1, raw.length() - 1),
                    PlainTextSplitter.getInstance());
        }
    };

    // ── Dispatch ──────────────────────────────────────────────────────────────────────────────

    @Override
    public @NotNull Tokenizer<?> getTokenizer(@NotNull PsiElement element) {
        // Comments — delegate to the platform's CommentTokenizer (handles //, /* */, ///, /** */)
        if (element instanceof PsiComment) {
            return super.getTokenizer(element);
        }

        if (element.getNode() == null) {
            return EMPTY_TOKENIZER;
        }

        IElementType type = element.getNode().getElementType();

        // Identifier leaves — split on camelCase / snake_case boundaries
        if (type == KlangTypes.IDENTIFIER) {
            return IDENTIFIER_TOKENIZER;
        }

        // String literals — check content between the quotes
        if (type == KlangTypes.LIT_STRING) {
            return STRING_TOKENIZER;
        }

        // Keywords, operators, numeric / char literals, punctuators — no spell-check
        return EMPTY_TOKENIZER;
    }
}

