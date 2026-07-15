package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.inspection.KlangModuleLayoutInspection;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Diagnostics for the directory-aware module layout ({@link KlangModuleLayoutInspection}): a mixed
 * directory (a module-less file plus several declared modules) and an ambiguous inherited module
 * (a module-less directory whose nearest declaring ancestor declares several modules) are both
 * reported as errors.
 */
class KlangModuleLayoutInspectionTest extends KlangFixtureTestBase {

    private List<HighlightInfo> highlight(VirtualFile caretFile) {
        fixture.configureFromExistingVirtualFile(caretFile);
        fixture.enableInspections(KlangModuleLayoutInspection.class);
        return fixture.doHighlighting();
    }

    private static boolean hasError(List<HighlightInfo> infos, String needle) {
        return infos.stream().anyMatch(i -> i.getSeverity() == HighlightSeverity.ERROR
                && i.getDescription() != null && i.getDescription().contains(needle));
    }

    @Test
    void mixedDirectoryIsReported() {
        onEdt(() -> {
            VirtualFile a = addFile("a.k", "module foo;\nf() : void { }").getVirtualFile();
            addFile("b.k", "module bar;\ng() : void { }");
            addFile("c.k", "h() : void { }"); // module-less → directory is mixed

            assertThat(hasError(highlight(a), "mixes")).isTrue();
        });
    }

    @Test
    void ambiguousInheritedModuleIsReported() {
        onEdt(() -> {
            addFile("a.k", "module foo;\nf() : void { }");
            addFile("x.k", "module bar;\ng() : void { }");
            // sub/ declares nothing → inherits from the root, which declares two modules → ambiguous.
            VirtualFile c = addFile("sub/c.k", "h() : void { }").getVirtualFile();

            assertThat(hasError(highlight(c), "ambiguous")).isTrue();
        });
    }

    @Test
    void wellFormedLayoutHasNoModuleLayoutError() {
        onEdt(() -> {
            // Policy C: one declared module + a module-less file → no error.
            VirtualFile a = addFile("a.k", "module foo;\nf() : void { }").getVirtualFile();
            addFile("b.k", "g() : void { }");

            List<HighlightInfo> infos = highlight(a);
            assertThat(hasError(infos, "mixes")).isFalse();
            assertThat(hasError(infos, "ambiguous")).isFalse();
        });
    }
}

