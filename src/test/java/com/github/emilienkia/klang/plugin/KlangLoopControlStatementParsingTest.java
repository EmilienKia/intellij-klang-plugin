package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangBreakStatement;
import com.github.emilienkia.klang.plugin.language.psi.KlangContinueStatement;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for the loop-control statements {@code break} and {@code continue}
 * (both implemented by the {@code klangc} compiler). Ensures each parses into its dedicated
 * statement node and emits the matching keyword leaf token.
 */
class KlangLoopControlStatementParsingTest extends KlangFixtureTestBase {

    private static void assertNoParseErrors(KlangFile file) {
        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        assertThat(errors)
                .withFailMessage(() -> "Unexpected parse errors: "
                        + errors.stream().map(PsiErrorElement::getErrorDescription).toList())
                .isEmpty();
    }

    private static boolean hasLeafOfType(KlangFile file, IElementType type) {
        for (PsiElement leaf : PsiTreeUtil.collectElements(file,
                e -> e.getFirstChild() == null && e.getNode() != null
                        && e.getNode().getElementType() == type)) {
            if (leaf != null) return true;
        }
        return false;
    }

    @Test
    void breakAndContinueInLoopParse() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    scan(values : int[], n : int) : int {
                        i : int = 0;
                        while (i < n) {
                            if (values[i] < 0) { continue; }
                            if (values[i] > 1000) { break; }
                            i++;
                        }
                        return i;
                    }
                    """);

            assertNoParseErrors(file);

            assertThat(PsiTreeUtil.findChildOfType(file, KlangBreakStatement.class))
                    .as("break; builds a breakStatement node").isNotNull();
            assertThat(PsiTreeUtil.findChildOfType(file, KlangContinueStatement.class))
                    .as("continue; builds a continueStatement node").isNotNull();

            assertThat(hasLeafOfType(file, KlangTypes.KW_BREAK))
                    .as("'break' is lexed as the KW_BREAK keyword").isTrue();
            assertThat(hasLeafOfType(file, KlangTypes.KW_CONTINUE))
                    .as("'continue' is lexed as the KW_CONTINUE keyword").isTrue();
        });
    }
}
