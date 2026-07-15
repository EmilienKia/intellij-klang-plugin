package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.KlangSemanticHighlighter;
import com.github.emilienkia.klang.plugin.language.KlangSyntaxHighlighter;
import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link KlangSemanticHighlighter#keyForResolved} — verifies that identifier usages
 * are mapped to the correct {@link TextAttributesKey} based on what their reference resolves to.
 *
 * <p>Each test follows the pattern established by {@link KlangReferenceResolutionTest}:
 * <ol>
 *   <li>Configure an in-memory K source with a {@code <caret>} on the usage under test</li>
 *   <li>Retrieve the {@link PsiReference} at the caret via
 *       {@code fixture.getReferenceAtCaretPosition()}</li>
 *   <li>Resolve it and call
 *       {@link KlangSemanticHighlighter#keyForResolved(PsiElement, PsiElement)} with the
 *       resolved target and the usage-site element</li>
 *   <li>Assert the returned key matches the expected constant</li>
 * </ol>
 * </p>
 *
 * <p>This approach is robust against IntelliJ test-framework changes in how INFORMATION-level
 * silent annotations are stored, and directly tests the semantic-highlight dispatch logic.</p>
 */
public class KlangSemanticHighlightingTest extends KlangFixtureTestBase {

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Configures the source, resolves the reference at the caret, and asserts both that the
     * resolved target is an instance of {@code expectedTargetClass} and that
     * {@link KlangSemanticHighlighter#keyForResolved} returns {@code expectedKey}.
     */
    private void assertSemanticKey(String source,
                                   Class<? extends PsiElement> expectedTargetClass,
                                   TextAttributesKey expectedKey) {
        onEdt(() -> {
            fixture.configureByText("a.k", source);
            PsiReference ref = fixture.getReferenceAtCaretPosition();
            assertThat(ref).as("reference at caret").isNotNull();
            PsiElement usageSite = ref.getElement();
            PsiElement resolved  = ref.resolve();
            assertThat(resolved).as("resolved target").isNotNull();
            assertThat(resolved).isInstanceOf(expectedTargetClass);
            TextAttributesKey actual = KlangSemanticHighlighter.keyForResolved(resolved, usageSite);
            assertEquals(expectedKey, actual,
                    "Wrong key for " + resolved.getClass().getSimpleName()
                    + " resolved from " + usageSite.getClass().getSimpleName());
        });
    }

    // ── T1 — Variable reference ───────────────────────────────────────────────

    @Test
    void variableReferenceYieldsVarRefKey() {
        assertSemanticKey("""
                module demo;
                f() : int {
                    x : int = 0;
                    return <caret>x;
                }
                """, KlangVariableDecl.class, KlangSyntaxHighlighter.IDENTIFIER_VAR_REF);
    }

    // ── T2 — Parameter reference ─────────────────────────────────────────────

    @Test
    void parameterReferenceYieldsParamRefKey() {
        assertSemanticKey("""
                module demo;
                add(a : int, b : int) : int { return <caret>a + b; }
                """, KlangParameterSpec.class, KlangSyntaxHighlighter.IDENTIFIER_PARAM_REF);
    }

    // ── T3 — Function call ────────────────────────────────────────────────────

    @Test
    void functionCallYieldsFunCallKey() {
        onEdt(() -> {
            fixture.configureByText("a.k", """
                    module demo;
                    helper() : int { return 1; }
                    main() : int { return <caret>helper(); }
                    """);
            PsiReference ref = fixture.getReferenceAtCaretPosition();
            assertThat(ref).as("reference at caret").isNotNull();
            PsiElement usageSite = ref.getElement();
            PsiElement resolved  = ref.resolve();
            assertThat(resolved).as("resolved target").isNotNull();
            assertThat(resolved).isInstanceOf(KlangFunctionDecl.class);
            // The usage site is in call position → IDENTIFIER_FUN_CALL
            TextAttributesKey key = KlangSemanticHighlighter.keyForResolved(resolved, usageSite);
            assertEquals(KlangSyntaxHighlighter.IDENTIFIER_FUN_CALL, key,
                    "Function call must yield IDENTIFIER_FUN_CALL");
        });
    }

    // ── T4 — Type reference in variable declaration ───────────────────────────

    @Test
    void typeReferenceYieldsTypeRefKey() {
        assertSemanticKey("""
                module demo;
                struct Point { x : float = 0.0f; }
                f() : int {
                    p : <caret>Point;
                    return 0;
                }
                """, KlangAggregateDecl.class, KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF);
    }

    // ── T5 — Enum type reference ──────────────────────────────────────────────

    @Test
    void enumTypeReferenceYieldsTypeRefKey() {
        assertSemanticKey("""
                module demo;
                enum Color : int { RED = 0; }
                f() : int {
                    c : <caret>Color;
                    return 0;
                }
                """, KlangEnumDecl.class, KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF);
    }

    // ── T6 — Type reference in base spec ──────────────────────────────────────

    @Test
    void baseSpecTypeReferenceYieldsTypeRefKey() {
        assertSemanticKey("""
                module demo;
                struct Base {}
                struct Derived : <caret>Base {}
                """, KlangAggregateDecl.class, KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF);
    }

    // ── T7 — Enum entry reference ─────────────────────────────────────────────

    @Test
    void enumEntryReferenceYieldsEnumEntryRefKey() {
        assertSemanticKey("""
                module demo;
                enum Color : int { RED = 0; GREEN = 1; }
                f() : int {
                    c : Color = Color::<caret>RED;
                    return 0;
                }
                """, KlangEnumEntry.class, KlangSyntaxHighlighter.IDENTIFIER_ENUM_ENTRY_REF);
    }

    // ── T8 — Template type parameter reference ────────────────────────────────

    @Test
    void templateParamReferenceYieldsTemplateParamRefKey() {
        assertSemanticKey("""
                module demo;
                template<typename T>
                identity(v : <caret>T) : int { return 0; }
                """, KlangTemplateParameter.class, KlangSyntaxHighlighter.IDENTIFIER_TEMPLATE_PARAM_REF);
    }

    // ── T9 — Named return variable reference ─────────────────────────────────

    @Test
    void namedReturnVarReferenceYieldsVarRefKey() {
        assertSemanticKey("""
                module demo;
                compute() result : int {
                    result = 42;
                    return <caret>result;
                }
                """, KlangNamedReturnVar.class, KlangSyntaxHighlighter.IDENTIFIER_VAR_REF);
    }

    // ── T10 — If-condition variable reference ─────────────────────────────────

    @Test
    void ifCondVarReferenceYieldsVarRefKey() {
        assertSemanticKey("""
                module demo;
                run(p : int*) : int {
                    if (v : int* = p) {
                        return *<caret>v;
                    }
                    return 0;
                }
                """, KlangIfCondVarDecl.class, KlangSyntaxHighlighter.IDENTIFIER_VAR_REF);
    }

    // ── T11 — Namespace reference ─────────────────────────────────────────────

    @Test
    void namespaceReferenceYieldsNamespaceRefKey() {
        assertSemanticKey("""
                module demo;
                namespace ns { x : int = 0; }
                f() : int { return <caret>ns::x; }
                """, KlangNamespaceDecl.class, KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF);
    }

    // ── Per-segment coloring tests ────────────────────────────────────────────
    //
    // KlangSegmentReference (already in the codebase) attaches a separate PsiReference
    // to each intermediate segment, so placing the caret ON that segment lets us resolve
    // it via getReferenceAtCaretPosition() and verify the key independently.

    @Test
    void twoSegmentQualifiedName_namespaceSegmentIsNamespaceRef() {
        // <caret>ns::Point — caret on 'ns' → KlangSegmentReference → KlangNamespaceDecl
        assertSemanticKey("""
                module demo;
                namespace ns {
                    struct Point {}
                }
                f() {
                    p : <caret>ns::Point;
                }
                """, KlangNamespaceDecl.class, KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF);
    }

    @Test
    void twoSegmentQualifiedName_typeSegmentIsTypeRef() {
        // ns::<caret>Point — caret on 'Point' → KlangQualifiedReference → KlangAggregateDecl
        assertSemanticKey("""
                module demo;
                namespace ns {
                    struct Point {}
                }
                f() {
                    p : ns::<caret>Point;
                }
                """, KlangAggregateDecl.class, KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF);
    }

    @Test
    void threeSegmentQualifiedName_outerNamespaceIsNamespaceRef() {
        // <caret>outer::inner::Value — caret on 'outer' → KlangSegmentReference → KlangNamespaceDecl
        assertSemanticKey("""
                module demo;
                namespace outer {
                    namespace inner {
                        struct Value {}
                    }
                }
                f() {
                    v : <caret>outer::inner::Value;
                }
                """, KlangNamespaceDecl.class, KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF);
    }

    @Test
    void threeSegmentQualifiedName_innerNamespaceIsNamespaceRef() {
        // outer::<caret>inner::Value — caret on 'inner' → KlangSegmentReference → KlangNamespaceDecl
        assertSemanticKey("""
                module demo;
                namespace outer {
                    namespace inner {
                        struct Value {}
                    }
                }
                f() {
                    v : outer::<caret>inner::Value;
                }
                """, KlangNamespaceDecl.class, KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF);
    }

    @Test
    void threeSegmentQualifiedName_lastSegmentIsTypeRef() {
        // outer::inner::<caret>Value — caret on 'Value' → KlangQualifiedReference → KlangAggregateDecl
        assertSemanticKey("""
                module demo;
                namespace outer {
                    namespace inner {
                        struct Value {}
                    }
                }
                f() {
                    v : outer::inner::<caret>Value;
                }
                """, KlangAggregateDecl.class, KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF);
    }

    @Test
    void enumQualifiedEntry_enumNameIsTypeRef() {
        // <caret>Color::RED — caret on 'Color' → KlangSegmentReference → KlangEnumDecl → TYPE_REF
        assertSemanticKey("""
                module demo;
                enum Color : int { RED = 0; GREEN = 1; }
                f() {
                    c : Color = <caret>Color::RED;
                }
                """, KlangEnumDecl.class, KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF);
    }

    @Test
    void enumQualifiedEntry_entryIsEnumEntryRef() {
        // Color::<caret>RED — caret on 'RED' → KlangQualifiedReference → KlangEnumEntry
        assertSemanticKey("""
                module demo;
                enum Color : int { RED = 0; GREEN = 1; }
                f() {
                    c : Color = Color::<caret>RED;
                }
                """, KlangEnumEntry.class, KlangSyntaxHighlighter.IDENTIFIER_ENUM_ENTRY_REF);
    }

    // ── T12 — keyForResolved returns null for unknown target ──────────────────

    @Test
    void keyForResolvedReturnsNullForUnknownTarget() {
        onEdt(() -> {
            parse("x : int = 0;");
            // A KlangFile is not a recognized resolved target → null
            KlangFile file = (KlangFile) fixture.getFile();
            assertNull(KlangSemanticHighlighter.keyForResolved(file, file),
                    "KlangFile is not a recognized target — should return null");
        });
    }

    // ── T13 — External names key constant smoke test ─────────────────────────

    @Test
    void semanticKeyExternalNames() {
        // Ensures each TextAttributesKey has the expected external name used as the
        // persistent color-scheme settings key.  Compile-time constants only.
        onEdt(() -> {
            parse("x : int = 0;"); // need to be on EDT for fixture setup
            assertEquals("KLANG_IDENTIFIER_VAR_REF",
                    KlangSyntaxHighlighter.IDENTIFIER_VAR_REF.getExternalName());
            assertEquals("KLANG_IDENTIFIER_PARAM_REF",
                    KlangSyntaxHighlighter.IDENTIFIER_PARAM_REF.getExternalName());
            assertEquals("KLANG_IDENTIFIER_FUN_CALL",
                    KlangSyntaxHighlighter.IDENTIFIER_FUN_CALL.getExternalName());
            assertEquals("KLANG_IDENTIFIER_FUN_REF",
                    KlangSyntaxHighlighter.IDENTIFIER_FUN_REF.getExternalName());
            assertEquals("KLANG_IDENTIFIER_TYPE_REF",
                    KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF.getExternalName());
            assertEquals("KLANG_IDENTIFIER_NAMESPACE_REF",
                    KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF.getExternalName());
            assertEquals("KLANG_IDENTIFIER_ENUM_ENTRY_REF",
                    KlangSyntaxHighlighter.IDENTIFIER_ENUM_ENTRY_REF.getExternalName());
            assertEquals("KLANG_IDENTIFIER_TEMPLATE_PARAM_REF",
                    KlangSyntaxHighlighter.IDENTIFIER_TEMPLATE_PARAM_REF.getExternalName());
        });
    }
}





