package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangForStatement;
import com.github.emilienkia.klang.plugin.language.psi.KlangForeachStatement;
import com.github.emilienkia.klang.plugin.language.psi.KlangForeachVarDecl;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for the {@code foreach} statement added to the klangc compiler
 * ('for ( [specifiers] name : type = source_expr ) nested_stmt'), covering the ARRAY,
 * ITERATOR and SEQUENCE source forms — all three share the exact same syntax and are only
 * told apart later by type resolution, so a single grammar rule covers them — as well as the
 * disambiguation against the classic C-style 'for' statement, and primitive array-literal
 * temporaries (e.g. 'int[]{1, 2, 3}') used as a foreach source.
 */
class KlangForeachStatementParsingTest extends KlangFixtureTestBase {

    private static void assertNoParseErrors(KlangFile file) {
        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        assertThat(errors)
                .withFailMessage(() -> "Unexpected parse errors: "
                        + errors.stream().map(PsiErrorElement::getErrorDescription).toList())
                .isEmpty();
    }

    @Test
    void foreachOverArrayParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    scan(values : int[]) : void {
                        for(x : int = values) { }
                    }
                    """);
            assertNoParseErrors(file);

            List<KlangForeachStatement> stmts = PsiTreeUtil.collectElementsOfType(file, KlangForeachStatement.class).stream().toList();
            assertThat(stmts).hasSize(1);
            KlangForeachVarDecl var = stmts.get(0).getForeachVarDecl();
            assertThat(var.getName()).isEqualTo("x");
        });
    }

    @Test
    void foreachWithReferenceAndSpecifierParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    scan(values : int[]) : void {
                        for(const x : int& = values) { }
                    }
                    """);
            assertNoParseErrors(file);
            assertThat(PsiTreeUtil.findChildOfType(file, KlangForeachStatement.class)).isNotNull();
        });
    }

    @Test
    void foreachOverPrimitiveArrayLiteralParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    run() : void {
                        for(x : int = int[]{1, 2, 30}) { }
                    }
                    """);
            assertNoParseErrors(file);
            assertThat(PsiTreeUtil.findChildOfType(file, KlangForeachStatement.class)).isNotNull();
        });
    }

    @Test
    void foreachOverIteratorOrSequenceSourceParses() {
        onEdt(() -> {
            // Syntactically identical to the ARRAY form: 'seq' being a Sequence<T>/Iterator<T>
            // is only established once type resolution kicks in, not by the grammar.
            KlangFile file = parse("""
                    module demo;
                    scan(seq : ::k::Sequence<int>&) : void {
                        for(x : int = seq) { }
                    }
                    """);
            assertNoParseErrors(file);
            assertThat(PsiTreeUtil.findChildOfType(file, KlangForeachStatement.class)).isNotNull();
        });
    }

    @Test
    void classicForStatementStillParsesAsForStatement() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    run() : void {
                        for(i : int = 0; i < 5; i++) { }
                    }
                    """);
            assertNoParseErrors(file);

            assertThat(PsiTreeUtil.findChildOfType(file, KlangForStatement.class))
                    .as("trailing ';' after the init expression disambiguates to the classic for")
                    .isNotNull();
            assertThat(PsiTreeUtil.findChildOfType(file, KlangForeachStatement.class))
                    .as("must not be mis-parsed as a foreach statement")
                    .isNull();
        });
    }
}
