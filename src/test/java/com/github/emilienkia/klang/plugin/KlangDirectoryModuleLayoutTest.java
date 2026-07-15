package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.index.KlangModuleModel;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Directory-aware module layout (per-directory decision + ancestor inheritance). Verifies that
 * {@link KlangModuleModel} computes the right effective module for files spread across
 * sub-directories, propagates a single declared module downward, isolates multi-module
 * directories, and flags mixed-directory / ambiguous-inheritance problems.
 */
class KlangDirectoryModuleLayoutTest extends KlangFixtureTestBase {

    private KlangModuleModel model() {
        return KlangModuleModel.getInstance(fixture.getProject());
    }

    private Set<String> moduleFileNames(KlangFile anchor) {
        return model().filesInModuleOf(anchor).stream()
                .map(VirtualFile::getName)
                .collect(Collectors.toSet());
    }

    private KlangModuleModel.LayoutProblem problemOf(KlangFile f) {
        return model().problemFor(f.getVirtualFile());
    }

    // ── Multi-module directory: every file declares its own module ────────────

    @Test
    void allFilesDeclaredAllowsSeveralModulesInOneDirectory() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module foo;\nf() : void { }");
            KlangFile b = addFile("b.k", "module bar;\ng() : void { }");

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.PER_FILE_MODULES);
            assertThat(model().moduleNameOf(a)).isEqualTo("foo");
            assertThat(model().moduleNameOf(b)).isEqualTo("bar");
            assertThat(moduleFileNames(a)).containsExactly("a.k");
            assertThat(moduleFileNames(b)).containsExactly("b.k");
            assertThat(problemOf(a)).isNull();
        });
    }

    // ── Mixed directory: anonymous file + several modules → error ─────────────

    @Test
    void mixedDirectoryWithSeveralModulesIsFlagged() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module foo;\nf() : void { }");
            KlangFile b = addFile("b.k", "module bar;\ng() : void { }");
            KlangFile c = addFile("c.k", "h() : void { }"); // anonymous

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.UNSUPPORTED);
            // Best-effort: declared files keep their own module, the anonymous file stays anonymous.
            assertThat(model().moduleNameOf(a)).isEqualTo("foo");
            assertThat(model().moduleNameOf(c)).isEmpty();
            // The conflict is reported on the directory's files.
            assertThat(problemOf(a)).isNotNull();
            assertThat(problemOf(a).kind()).isEqualTo(KlangModuleModel.ProblemKind.MIXED_DIRECTORY);
            assertThat(problemOf(c)).isNotNull();
        });
    }

    // ── Inheritance: a single declared module propagates to a sub-directory ───

    @Test
    void singleModulePropagatesToSubdirectory() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module foo;\nf() : void { }");
            KlangFile b = addFile("sub/b.k", "g() : void { }"); // anonymous → inherits foo

            assertThat(model().moduleNameOf(a)).isEqualTo("foo");
            assertThat(model().moduleNameOf(b)).isEqualTo("foo");
            assertThat(moduleFileNames(b)).containsExactlyInAnyOrder("a.k", "b.k");
            assertThat(problemOf(b)).isNull();
        });
    }

    @Test
    void singleModulePropagatesAcrossTwoDirectoryLevels() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module foo;\nf() : void { }");
            KlangFile d = addFile("sub/sub2/d.k", "g() : void { }");

            assertThat(model().moduleNameOf(d)).isEqualTo("foo");
            assertThat(moduleFileNames(d)).containsExactlyInAnyOrder("a.k", "d.k");
        });
    }

    // ── Override: a sub-directory declaring its own module wins ───────────────

    @Test
    void subdirectoryModuleOverridesInheritedOne() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "module foo;\nf() : void { }");
            KlangFile b = addFile("sub/b.k", "module bar;\ng() : void { }");

            assertThat(model().moduleNameOf(a)).isEqualTo("foo");
            assertThat(model().moduleNameOf(b)).isEqualTo("bar");
            assertThat(moduleFileNames(a)).containsExactly("a.k");
            assertThat(moduleFileNames(b)).containsExactly("b.k");
        });
    }

    // ── Ambiguous inheritance: ancestor declares several modules → error ──────

    @Test
    void ambiguousInheritedModuleIsFlagged() {
        onEdt(() -> {
            addFile("a.k", "module foo;\nf() : void { }");
            addFile("x.k", "module bar;\ng() : void { }");
            KlangFile c = addFile("sub/c.k", "h() : void { }"); // anonymous → ambiguous inherit

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.UNSUPPORTED);
            assertThat(model().moduleNameOf(c)).isEmpty(); // best-effort: anonymous
            assertThat(problemOf(c)).isNotNull();
            assertThat(problemOf(c).kind())
                    .isEqualTo(KlangModuleModel.ProblemKind.AMBIGUOUS_INHERITANCE);
        });
    }

    // ── Anonymous everywhere across directories: one global anonymous module ──

    @Test
    void anonymousFilesAcrossDirectoriesFormOneModule() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "f() : void { }");
            KlangFile b = addFile("sub/b.k", "g() : void { }");

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.ANONYMOUS_SINGLE);
            assertThat(model().moduleNameOf(a)).isEmpty();
            assertThat(model().moduleNameOf(b)).isEmpty();
            assertThat(moduleFileNames(a)).containsExactlyInAnyOrder("a.k", "b.k");
        });
    }

    // ── libk never participates in the directory layout ───────────────────────

    @Test
    void standardLibraryDoesNotAffectDirectoryLayout() {
        onEdt(() -> {
            KlangFile a = addFile("a.k", "f() : void { }"); // anonymous app
            addFile("kmath.k", "module k::math;\nabs(x: int) : int { return x; }");

            assertThat(model().policy()).isEqualTo(KlangModuleModel.Policy.ANONYMOUS_SINGLE);
            assertThat(model().moduleNameOf(a)).isEmpty();
            // The libk file is never part of the app's anonymous module root.
            assertThat(moduleFileNames(a)).containsExactly("a.k");
            assertThat(model().allModuleNames()).contains("k::math");
        });
    }
}

