package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cross-<em>directory</em> resolution on top of the directory-aware module model (phase 2):
 * a file that inherits its module from a parent directory shares one compilation unit with the
 * declaring files, an {@code import} sees the imported module's inherited sub-directory files, and
 * unrelated modules in sibling directories stay isolated.
 */
class KlangDirectoryResolutionTest extends KlangFixtureTestBase {

    /**
     * Adds {@code deps} (name/source pairs), then creates {@code path} from {@code sourceWithCaret}
     * (a single {@code <caret>} marker) and resolves the reference under the caret — directly on
     * the PSI, so the caret file may live in any sub-directory.
     */
    private PsiElement resolveAt(String path, String sourceWithCaret, String... deps) {
        for (int i = 0; i + 1 < deps.length; i += 2) {
            addFile(deps[i], deps[i + 1]);
        }
        int caret = sourceWithCaret.indexOf("<caret>");
        assertThat(caret).as("the source must contain a <caret> marker").isGreaterThanOrEqualTo(0);
        String src = sourceWithCaret.substring(0, caret)
                + sourceWithCaret.substring(caret + "<caret>".length());
        KlangFile file = addFile(path, src);
        PsiReference ref = file.findReferenceAt(caret);
        assertThat(ref).as("a reference is present at the caret").isNotNull();
        return ref.resolve();
    }

    private static String fileName(PsiElement el) {
        return el.getContainingFile().getViewProvider().getVirtualFile().getName();
    }

    // ── A sub-directory file inherits its parent's module (same compilation unit) ──

    @Test
    void inheritedSubdirectoryFileResolvesIntoParentModule() {
        onEdt(() -> {
            // 'a.k' declares module 'foo' at the root; 'sub/b.k' declares nothing, so it inherits
            // 'foo' and shares its module root → 'target' (in a.k) resolves from sub/b.k.
            PsiElement target = resolveAt("sub/b.k",
                    "caller() : void { tar<caret>get(); }",
                    "a.k", "module foo;\ntarget() : void { }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("target");
            assertThat(fileName(target)).isEqualTo("a.k");
        });
    }

    // ── An import sees the imported module's inherited sub-directory files ─────

    @Test
    void importSeesInheritedSubdirectoryFileOfImportedModule() {
        onEdt(() -> {
            // module 'geometry' is declared in geo/g.k; geo/sub/w.k inherits 'geometry'. From
            // module 'app', 'import geometry;' must see the inherited 'widget' in geo/sub/w.k.
            PsiElement target = resolveAt("main.k",
                    "module app;\nimport geometry;\nuse() : void { geometry::wid<caret>get(); }",
                    "geo/g.k", "module geometry;\nbase() : void { }",
                    "geo/sub/w.k", "widget() : void { }");

            assertThat(target).isInstanceOf(KlangFunctionDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("widget");
            assertThat(fileName(target)).isEqualTo("w.k");
        });
    }

    // ── Sibling directories with different modules stay isolated ──────────────

    @Test
    void siblingDirectoryModuleStaysIsolatedWithoutImport() {
        onEdt(() -> {
            // 'secret' lives in module 'bar' (other/o.k); module 'app' does not import it → no leak.
            PsiElement target = resolveAt("main.k",
                    "module app;\nuse() : void { secr<caret>et(); }",
                    "other/o.k", "module bar;\nsecret() : void { }");

            assertThat(target).as("a non-imported sibling module must not resolve").isNull();
        });
    }
}

