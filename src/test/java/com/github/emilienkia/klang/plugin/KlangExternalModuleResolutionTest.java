package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangModuleDeclaration;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multi-file indexing — milestone M3, cross-<em>module</em> resolution via {@code import}
 * declarations (EXTERNAL_LOOKUP, §5.8), refined to the K module semantics:
 *
 * <ul>
 *   <li>Modules are <b>airtight</b>: a symbol of another module is invisible without an
 *       explicit {@code import}.</li>
 *   <li>An {@code import M;} grants <b>qualified access only</b> ({@code M::x} / {@code ::M::x});
 *       it does <b>not</b> inject {@code M}'s symbols unqualified — that needs a {@code using}
 *       directive (§9).</li>
 *   <li>The standard library {@code k} is the <b>sole auto-imported</b> module: names targeting
 *       the {@code k} namespace resolve (qualified / absolute) without an explicit import.</li>
 * </ul>
 */
class KlangExternalModuleResolutionTest extends KlangFixtureTestBase {

    private PsiElement resolveAcrossFiles(String caretSource, String... namedDeps) {
        for (int i = 0; i + 1 < namedDeps.length; i += 2) {
            addFile(namedDeps[i], namedDeps[i + 1]);
        }
        fixture.configureByText("main.k", caretSource);
        PsiReference ref = fixture.getReferenceAtCaretPosition();
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        return ref.resolve();
    }

    private static String fileName(PsiElement el) {
        return el.getContainingFile().getViewProvider().getVirtualFile().getName();
    }

    // ── Explicit import — qualified access ────────────────────────────────────

    @Test
    void importedQualifiedNameResolves() {
        onEdt(() -> {
            // 'geometry::dist' with the module-name prefix resolves into the imported module.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            import geometry;
                            compute() : int { return geometry::di<caret>st(); }
                            """,
                    "geo.k", "module geometry;\ndist() : int { return 0; }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("dist");
            assertThat(fileName(target)).isEqualTo("geo.k");
        });
    }

    @Test
    void importedAbsoluteNameResolves() {
        onEdt(() -> {
            // '::geometry::dist' absolute form with the module prefix resolves through the import.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            import geometry;
                            compute() : int { return ::geometry::di<caret>st(); }
                            """,
                    "geo.k", "module geometry;\ndist() : int { return 0; }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("dist");
            assertThat(fileName(target)).isEqualTo("geo.k");
        });
    }

    @Test
    void importedTypeResolvesInQualifiedTypePosition() {
        onEdt(() -> {
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            import geometry;
                            main() : void { v : geometry::Po<caret>int; }
                            """,
                    "geo.k", "module geometry;\nstruct Point { x: int; }");

            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
            assertThat(fileName(target)).isEqualTo("geo.k");
        });
    }

    // ── Explicit import does NOT grant unqualified access ─────────────────────

    @Test
    void importedUnqualifiedNameDoesNotResolve() {
        onEdt(() -> {
            // 'import geometry;' makes 'geometry::dist' reachable, but a bare 'dist()' must NOT
            // resolve — importing a module never injects its symbols unqualified.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            import geometry;
                            compute() : int { return di<caret>st(); }
                            """,
                    "geo.k", "module geometry;\ndist() : int { return 0; }");

            assertThat(target).as("import grants qualified access only").isNull();
        });
    }

    // ── Without import, another module stays invisible ────────────────────────

    @Test
    void nonImportedQualifiedNameDoesNotResolve() {
        onEdt(() -> {
            // No 'import geometry;' → even the fully-qualified 'geometry::dist' is invisible.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            compute() : int { return geometry::di<caret>st(); }
                            """,
                    "geo.k", "module geometry;\ndist() : int { return 0; }");

            assertThat(target).as("a non-imported module is airtight").isNull();
        });
    }

    // ── 'using' enables the unqualified short form ────────────────────────────

    @Test
    void usingNamespaceEnablesUnqualifiedAccess() {
        onEdt(() -> {
            // 'using namespace geometry::shapes;' injects the sub-namespace's members unqualified.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            import geometry;
                            compute() : int {
                                using namespace geometry::shapes;
                                return ar<caret>ea();
                            }
                            """,
                    "geo.k", "module geometry;\nnamespace shapes { area() : int { return 0; } }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("area");
            assertThat(fileName(target)).isEqualTo("geo.k");
        });
    }

    // ── import target reference → the imported module's declaration ───────────

    @Test
    void importTargetResolvesToModuleDeclaration() {
        onEdt(() -> {
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            import geome<caret>try;
                            compute() : int { return 0; }
                            """,
                    "geo.k", "module geometry;\ndist() : int { return 0; }");

            assertThat(target).isInstanceOf(KlangModuleDeclaration.class);
            assertThat(fileName(target)).isEqualTo("geo.k");
        });
    }

    // ── Standard library 'k' — auto-imported (no 'import' needed) ─────────────

    @Test
    void libkQualifiedNameResolvesWithoutImport() {
        onEdt(() -> {
            // No 'import' — 'k::math::abs' resolves because libk is auto-imported.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            compute() : int { return k::math::ab<caret>s(5); }
                            """,
                    "kmath.k", "module k::math;\nabs(x: int) : int { return x; }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("abs");
            assertThat(fileName(target)).isEqualTo("kmath.k");
        });
    }

    @Test
    void libkAbsoluteNameResolvesWithoutImport() {
        onEdt(() -> {
            // No 'import' — '::k::math::abs' (absolute) resolves through the auto-imported libk.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            compute() : int { return ::k::math::ab<caret>s(5); }
                            """,
                    "kmath.k", "module k::math;\nabs(x: int) : int { return x; }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("abs");
            assertThat(fileName(target)).isEqualTo("kmath.k");
        });
    }

    @Test
    void libkUnqualifiedNameStillRequiresQualificationOrUsing() {
        onEdt(() -> {
            // libk needs no import, but qualification is still required: bare 'abs(5)' is unresolved.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            compute() : int { return ab<caret>s(5); }
                            """,
                    "kmath.k", "module k::math;\nabs(x: int) : int { return x; }");

            assertThat(target).as("unqualified libk name needs a 'using' directive").isNull();
        });
    }

    // ── fail soft — unknown module never crashes ──────────────────────────────

    @Test
    void importOfAbsentModuleFailsSoft() {
        onEdt(() -> {
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            import nowhere;
                            compute() : int { return nowhere::ghos<caret>t(5); }
                            """);

            assertThat(target).as("unknown module → unresolved").isNull();
        });
    }
}

