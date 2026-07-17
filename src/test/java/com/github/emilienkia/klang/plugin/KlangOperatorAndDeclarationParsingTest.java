package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangCastOperatorFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangOperatorFunctionHead;
import com.github.emilienkia.klang.plugin.language.psi.KlangSpaceshipExpr;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for the operator-head / block-declaration grammar sync
 * (propagated from {@code klang.ebnf}):
 *
 * <ul>
 *   <li>the <strong>subscript</strong> operator {@code operator[]} (the originally reported
 *       failure in {@code collections.k}),</li>
 *   <li>the <strong>cast / conversion</strong> operator {@code operator ()} — now a distinct
 *       {@code castOperatorFunctionHead} with no parameter list and an optional {@code : type},</li>
 *   <li>block declarations carry <strong>no trailing {@code ;}</strong> (an enum here), and a
 *       stray {@code ;} is tolerated as an <strong>empty declaration</strong>,</li>
 *   <li>a {@code generic<…>} declaration prefixing an aggregate,</li>
 *   <li>the <strong>three-way comparison</strong> operator {@code operator <=>} and the
 *       {@code a <=> b} spaceship expression,</li>
 *   <li>{@code friend} declarations with optional <strong>template arguments</strong>
 *       ({@code friend Iterator<T>;}),</li>
 *   <li><strong>backward documentation comments</strong> {@code //!} and {@code /*! … *}{@code /}.</li>
 * </ul>
 */
class KlangOperatorAndDeclarationParsingTest extends KlangFixtureTestBase {

    private static void assertNoParseErrors(KlangFile file) {
        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        assertThat(errors)
                .withFailMessage(() -> "Unexpected parse errors: "
                        + errors.stream().map(PsiErrorElement::getErrorDescription).toList())
                .isEmpty();
    }

    /** True if any leaf token under {@code file} has the given element type. */
    private static boolean hasLeafOfType(KlangFile file, IElementType type) {
        for (PsiElement leaf : PsiTreeUtil.collectElements(file,
                e -> e.getFirstChild() == null && e.getNode() != null
                        && e.getNode().getElementType() == type)) {
            if (leaf != null) return true;
        }
        return false;
    }

    @Test
    void subscriptOperatorWithReferenceReturnTypeParses() {
        onEdt(() -> {
            // Exact shape of the collections.k overload that used to fail with
            // "<operator symbol> or token '(' expected, got '['".
            KlangFile file = parse("""
                    module demo;
                    template<typename T>
                    class LinkedList {
                        operator [](index : int) : T& {
                            return this;
                        }
                    }
                    """);

            assertNoParseErrors(file);

            KlangFunctionDecl fn = PsiTreeUtil.findChildOfType(file, KlangFunctionDecl.class);
            assertThat(fn).isNotNull();
            KlangOperatorFunctionHead head = fn.getOperatorFunctionHead();
            assertThat(head).as("operator[] is an operatorFunctionHead").isNotNull();
            // The subscript form has no operatorSymbol child (it is 'operator' '[' ']').
            assertThat(head.getOperatorSymbol()).isNull();
            assertThat(head.getText().replaceAll("\\s+", "")).isEqualTo("operator[]");
        });
    }

    @Test
    void castOperatorParsesAsDistinctHead() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    struct Wrapper {
                        value : int;
                        operator () : int { return value; }
                    }
                    """);

            assertNoParseErrors(file);

            KlangFunctionDecl fn = PsiTreeUtil.findChildOfType(file, KlangFunctionDecl.class);
            assertThat(fn).isNotNull();
            // The cast operator is NOT an operatorFunctionHead anymore…
            assertThat(fn.getOperatorFunctionHead()).isNull();
            // …it is a dedicated castOperatorFunctionHead (no parameter list, optional ': type').
            KlangCastOperatorFunctionHead cast =
                    PsiTreeUtil.findChildOfType(file, KlangCastOperatorFunctionHead.class);
            assertThat(cast).isNotNull();
            assertThat(cast.getText().replaceAll("\\s+", "")).isEqualTo("operator()");
        });
    }

    @Test
    void enumWithoutTrailingSemicolonAndStraySemicolonBothParse() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    enum Color { Red; Green; Blue; }
                    ;
                    struct S { }
                    """);

            // No PsiErrorElement: the missing trailing ';' after the enum's '}' is fine (block
            // declaration) and the stray ';' is absorbed as an empty declaration (only a
            // non-fatal annotator warning). Each enumEntry still carries its own ';'.
            assertNoParseErrors(file);
        });
    }

    @Test
    void genericPrefixedAggregateParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    generic<typename T>
                    class Box {
                        item : T;
                    }
                    """);

            assertNoParseErrors(file);
        });
    }

    @Test
    void interfaceDefaultMethodSpecifierParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    interface Sized {
                        /**
                         * Return the number of elements in the container.
                         * @return Element count.
                         */
                        const size() : unsigned int;
                        /**
                         * Tell whether the container is empty.
                         */
                        default const isEmpty() : bool {
                            return size() == 0;
                        }
                    }
                    """);

            assertNoParseErrors(file);
        });
    }

    @Test
    void spaceshipOperatorOverloadParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    struct Point {
                        x : int;
                        operator <=>(other : Point&) : int;
                    }
                    """);

            assertNoParseErrors(file);

            KlangFunctionDecl fn = PsiTreeUtil.findChildOfType(file, KlangFunctionDecl.class);
            assertThat(fn).isNotNull();
            KlangOperatorFunctionHead head = fn.getOperatorFunctionHead();
            assertThat(head).as("operator <=> is an operatorFunctionHead").isNotNull();
            assertThat(head.getOperatorSymbol()).isNotNull();
            assertThat(head.getText().replaceAll("\\s+", "")).isEqualTo("operator<=>");
        });
    }

    @Test
    void spaceshipExpressionParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    compare(a : int, b : int) : int {
                        return a <=> b;
                    }
                    """);

            assertNoParseErrors(file);

            KlangSpaceshipExpr expr = PsiTreeUtil.findChildOfType(file, KlangSpaceshipExpr.class);
            assertThat(expr).as("a <=> b builds a spaceshipExpr node").isNotNull();
            assertThat(hasLeafOfType(file, KlangTypes.OP_SPACESHIP)).isTrue();
        });
    }

    @Test
    void friendWithTemplateArgumentsParses() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    template<typename T>
                    class Container {
                        friend Iterator<T>;
                        friend struct Helper<int>;
                    }
                    """);

            assertNoParseErrors(file);
        });
    }

    @Test
    void backwardDocCommentsAreRecognisedAsDocTokens() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    struct Point {
                        x : int;
                        //! Backward line doc attached to the member above.
                        y : int;
                        /*! Backward block doc. */
                        z : int;
                    }
                    """);

            assertNoParseErrors(file);
            assertThat(hasLeafOfType(file, KlangTypes.LINE_DOC_COMMENT_BWD))
                    .as("//! is lexed as a backward line documentation comment").isTrue();
            assertThat(hasLeafOfType(file, KlangTypes.BLOCK_DOC_COMMENT_BWD))
                    .as("/*! … */ is lexed as a backward block documentation comment").isTrue();
        });
    }
}

