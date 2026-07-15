package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.github.emilienkia.klang.plugin.language.psi.KlangVariableDecl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multi-file indexing — milestone M2. The module root is now a multi-file aggregation, so a
 * reference in one {@code .k} file resolves onto a declaration in another file of the same
 * module (including re-opened namespaces). Covers the supported policies A/B/C.
 */
class KlangCrossFileResolutionTest extends KlangFixtureTestBase {

    /** Adds {@code dependencies}, then configures the caret file and resolves the reference under it. */
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

    // ── Policy A — free function in a sibling file ────────────────────────────

    @Test
    void freeFunctionResolvesToSiblingFile() {
        onEdt(() -> {
            // 'foo' is declared in lib.k; called from main.k — same module 'demo'.
            PsiElement target = resolveAcrossFiles("""
                            module demo;
                            bar() : void { fo<caret>o(); }
                            """,
                    "lib.k", "module demo;\nfoo() : void { }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("foo");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    // ── Policy A — qualified type through a namespace declared in another file ──

    @Test
    void qualifiedTypeResolvesAcrossFiles() {
        onEdt(() -> {
            // 'shapes::Point' — the namespace and struct live in lib.k.
            PsiElement target = resolveAcrossFiles("""
                            module demo;
                            main() : void { p : shapes::Po<caret>int; }
                            """,
                    "lib.k", "module demo;\nnamespace shapes { struct Point { x: int; } }");

            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    // ── Policy A — re-opened namespace: simple name across files ──────────────

    @Test
    void reopenedNamespaceMergesMembersAcrossFiles() {
        onEdt(() -> {
            // 'count' is declared in lib.k's 'util'; referenced from main.k's re-opened 'util'.
            PsiElement target = resolveAcrossFiles("""
                            module demo;
                            namespace util {
                                reset() : void { co<caret>unt = 0; }
                            }
                            """,
                    "lib.k", "module demo;\nnamespace util { count: int = 0; }");

            assertThat(target).isInstanceOf(KlangVariableDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("count");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    // ── Policy A — absolute name across files ─────────────────────────────────

    @Test
    void absoluteNameResolvesAcrossFiles() {
        onEdt(() -> {
            // '::demo::run' with explicit module prefix resolves to run() declared in lib.k.
            PsiElement target = resolveAcrossFiles("""
                            module demo;
                            main() : void { ::demo::ru<caret>n(); }
                            """,
                    "lib.k", "module demo;\nrun() : void { }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("run");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    // ── Policy C — one file declares the module, others inherit it ────────────

    @Test
    void policyCFileWithoutModuleResolvesIntoTheDeclaredModule() {
        onEdt(() -> {
            // main.k has NO module declaration; lib.k declares 'the::game' (policy C → applies
            // to all). 'helper' must still resolve cross-file.
            PsiElement target = resolveAcrossFiles("""
                            caller() : void { hel<caret>per(); }
                            """,
                    "lib.k", "module the::game;\nhelper() : void { }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("helper");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    // ── Policy A — different modules stay isolated ────────────────────────────

    @Test
    void differentModulesDoNotLeakDeclarations() {
        onEdt(() -> {
            // main.k is module 'app'; 'secret' lives in module 'other' → must NOT resolve.
            PsiElement target = resolveAcrossFiles("""
                            module app;
                            use() : void { secr<caret>et(); }
                            """,
                    "lib.k", "module other;\nsecret() : void { }");

            assertThat(target).as("symbols from a different module must not resolve").isNull();
        });
    }

    // ── Policy B — no file declares a module: one anonymous module ────────────

    @Test
    void anonymousModuleResolvesFreeFunctionAcrossFiles() {
        onEdt(() -> {
            // Neither file declares a module (policy B). All files form one anonymous module, so
            // 'foo' declared in lib.k is visible from main.k without any import.
            PsiElement target = resolveAcrossFiles("""
                            bar() : void { fo<caret>o(); }
                            """,
                    "lib.k", "foo() : void { }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("foo");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    @Test
    void anonymousModuleResolvesQualifiedTypeAcrossFiles() {
        onEdt(() -> {
            // 'shapes::Point' — the namespace and struct live in lib.k; no module declared.
            PsiElement target = resolveAcrossFiles("""
                            main() : void { p : shapes::Po<caret>int; }
                            """,
                    "lib.k", "namespace shapes { struct Point { x: int; } }");

            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Point");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    @Test
    void anonymousModuleResolvesAbsoluteNameAcrossFiles() {
        onEdt(() -> {
            // '::run' — absolute name from the (empty-named) anonymous root resolves into lib.k.
            PsiElement target = resolveAcrossFiles("""
                            main() : void { ::ru<caret>n(); }
                            """,
                    "lib.k", "run() : void { }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("run");
            assertThat(fileName(target)).isEqualTo("lib.k");
        });
    }

    @Test
    void anonymousModuleCoexistsWithAutoImportedLibk() {
        onEdt(() -> {
            // Policy B app (no module) with the 'k' stdlib present: a sibling symbol still
            // resolves cross-file (anonymous root intact), and 'k::math::abs' resolves via the
            // auto-imported libk — even though a libk file declares a module.
            addFile("lib.k", "helper() : int { return 0; }");
            addFile("kmath.k", "module k::math;\nabs(x: int) : int { return x; }");

            fixture.configureByText("main.k", """
                            run() : int { return help<caret>er(); }
                            """);
            PsiReference siblingRef = fixture.getReferenceAtCaretPosition();
            assertThat(siblingRef).isNotNull();
            PsiElement sibling = siblingRef.resolve();
            assertThat(sibling).isInstanceOf(KlangFunctionDecl.class);
            assertThat(fileName(sibling)).isEqualTo("lib.k");

            fixture.configureByText("main2.k", """
                            calc() : int { return k::math::ab<caret>s(5); }
                            """);
            PsiReference libkRef = fixture.getReferenceAtCaretPosition();
            assertThat(libkRef).isNotNull();
            PsiElement libk = libkRef.resolve();
            assertThat(libk).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) libk).getName()).isEqualTo("abs");
            assertThat(fileName(libk)).isEqualTo("kmath.k");
        });
    }
}

