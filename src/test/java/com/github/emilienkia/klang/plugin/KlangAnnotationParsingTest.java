package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangAnnotationDef;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for annotation parsing.
 *
 * <p>The three {@code AnnotationDef} forms in {@code klang.ebnf} share the common prefix
 * {@code '@' QualifiedIdentifier}. GrammarKit emits an <em>ordered-choice</em> PEG parser
 * with no backtracking over an already-matched prefix, so listing the bare
 * {@code '@' QualifiedIdentifier} alternative first used to "win" for {@code @Foo(...)} and
 * {@code @Foo{...}}, leaving the {@code (...)} / {@code {...}} part dangling and breaking the
 * enclosing declaration. The grammar now left-factors the optional argument group, which
 * these tests pin down.</p>
 */
class KlangAnnotationParsingTest extends KlangFixtureTestBase {

    private static void assertNoParseErrors(KlangFile file) {
        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        assertThat(errors)
                .withFailMessage(() -> "Unexpected parse errors: "
                        + errors.stream().map(PsiErrorElement::getErrorDescription).toList())
                .isEmpty();
    }

    @Test
    void annotationWithCallArgumentsOnSelfReferencingType() {
        onEdt(() -> {
            // Exact shape of the standard-library annotations.k that used to fail:
            // an annotation type annotated with itself, using call-style arguments.
            KlangFile file = parse("""
                    module k;

                    namespace annotations {

                    @Retention(Policy::RUNTIME)
                    @Target({ElementType::ANNOTATION})
                    annotation Retention {
                        enum Policy { SOURCE; RUNTIME; };
                        policy : Policy = Policy::RUNTIME;
                    }

                    }
                    """);

            assertNoParseErrors(file);

            // Both annotation usages are recognised as annotation definitions.
            assertThat(PsiTreeUtil.findChildrenOfType(file, KlangAnnotationDef.class)).hasSize(2);
        });
    }

    @Test
    void allThreeAnnotationFormsParse() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    @Marker                       // '@' QualifiedIdentifier
                    @WithArgs(1, "two", Color::RED) // '@' QualifiedIdentifier '(' ExpressionList ')'
                    @WithEmptyArgs()              // empty argument list
                    @WithBraces({1, 2, 3})        // '@' QualifiedIdentifier BraceInitList
                    struct S {
                    }
                    """);

            assertNoParseErrors(file);
            assertThat(PsiTreeUtil.findChildrenOfType(file, KlangAnnotationDef.class)).hasSize(4);
        });
    }
}

