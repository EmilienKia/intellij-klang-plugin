package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.index.KlangModuleModel;
import com.github.emilienkia.klang.plugin.language.index.KlangModuleScope;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multi-file indexing — milestone M1. Verifies that {@link KlangModuleModel} detects the
 * correct project policy (A/B/C/UNSUPPORTED) from the {@code KlangModuleNameIndex} and groups
 * files into the right module roots, and that {@link KlangModuleScope} surfaces those files.
 */
class KlangModuleModelTest extends KlangFixtureTestBase {

    private KlangModuleModel model() {
        return KlangModuleModel.getInstance(fixture.getProject());
    }

    private Set<String> rootFileNames(KlangFile anchor) {
        return KlangModuleScope.moduleRootFiles(anchor).stream()
                .map(f -> f.getVirtualFile().getName())
                .collect(Collectors.toSet());
    }

    private Set<String> moduleFileNames(KlangFile anchor) {
        return model().filesInModuleOf(anchor).stream()
                .map(VirtualFile::getName)
                .collect(Collectors.toSet());
    }

    // ── Policy B — no file declares a module ──────────────────────────────────

    @Test
    void anonymousSinglePolicyGroupsAllFilesTogether() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "foo() : void { }");
            KlangFile b = addFile("b.k", "bar() : void { }");

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.ANONYMOUS_SINGLE);
            assertThat(model().allModuleNames()).isEmpty();
            assertThat(model().moduleNameOf(a)).isEmpty();
            assertThat(model().moduleNameOf(b)).isEmpty();

            assertThat(moduleFileNames(a)).containsExactlyInAnyOrder("a.k", "b.k");
            assertThat(rootFileNames(a)).containsExactlyInAnyOrder("a.k", "b.k");
        });
    }

    @Test
    void standardLibraryDoesNotBreakAnonymousSinglePolicy() {
        onEdt(() -> {
            // The app files declare no module (policy B). The 'k' stdlib sources DO declare a
            // module, but as a library they are excluded from policy detection, so the project
            // is still ANONYMOUS_SINGLE and the anonymous root excludes the libk file.
            KlangFile a = addFile("a.k", "foo() : void { }");
            KlangFile b = addFile("b.k", "bar() : void { }");
            addFile("kmath.k", "module k::math;\nabs(x: int) : int { return x; }");

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.ANONYMOUS_SINGLE);
            assertThat(model().moduleNameOf(a)).isEmpty();

            // The anonymous module root is the two app files only — never the libk file.
            assertThat(moduleFileNames(a)).containsExactlyInAnyOrder("a.k", "b.k");
            assertThat(rootFileNames(b)).containsExactlyInAnyOrder("a.k", "b.k");
        });
    }

    // ── Policy C — exactly one file declares a module ─────────────────────────

    @Test
    void singleDeclaresAllAppliesTheModuleToEveryFile() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module the::game;\nfoo() : void { }");
            KlangFile b = addFile("b.k", "bar() : void { }");

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.SINGLE_DECLARES_ALL);
            assertThat(model().allModuleNames()).containsExactly("the::game");
            // The single declared module applies to both files.
            assertThat(model().moduleNameOf(a)).isEqualTo("the::game");
            assertThat(model().moduleNameOf(b)).isEqualTo("the::game");

            assertThat(moduleFileNames(b)).containsExactlyInAnyOrder("a.k", "b.k");
            assertThat(rootFileNames(b)).containsExactlyInAnyOrder("a.k", "b.k");
        });
    }

    // ── Policy A — every file declares a module ───────────────────────────────

    @Test
    void perFileModulesGroupFilesByDeclaredName() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module the::game;\nfoo() : void { }");
            KlangFile b = addFile("b.k", "module the::game;\nbar() : void { }");
            KlangFile c = addFile("c.k", "module other;\nbaz() : void { }");

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.PER_FILE_MODULES);
            assertThat(model().allModuleNames()).containsExactlyInAnyOrder("the::game", "other");

            assertThat(model().moduleNameOf(a)).isEqualTo("the::game");
            assertThat(model().moduleNameOf(c)).isEqualTo("other");

            // 'the::game' bundles a.k + b.k; 'other' is alone.
            assertThat(moduleFileNames(a)).containsExactlyInAnyOrder("a.k", "b.k");
            assertThat(rootFileNames(b)).containsExactlyInAnyOrder("a.k", "b.k");
            assertThat(moduleFileNames(c)).containsExactly("c.k");
        });
    }

    // ── UNSUPPORTED — mixed declarations (best-effort fallback) ───────────────

    @Test
    void mixedDeclarationsFallBackToUnsupportedWithoutThrowing() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module the::game;\nfoo() : void { }");
            KlangFile b = addFile("b.k", "module other;\nbar() : void { }");
            KlangFile c = addFile("c.k", "baz() : void { }"); // no module → mixed

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.UNSUPPORTED);

            // Declared files still group by their own name…
            assertThat(model().moduleNameOf(a)).isEqualTo("the::game");
            assertThat(model().moduleNameOf(b)).isEqualTo("other");
            assertThat(moduleFileNames(a)).containsExactly("a.k");
            assertThat(moduleFileNames(b)).containsExactly("b.k");
            // …and the undeclared file is grouped with other undeclared files.
            assertThat(model().moduleNameOf(c)).isEmpty();
            assertThat(moduleFileNames(c)).containsExactly("c.k");
        });
    }
}


