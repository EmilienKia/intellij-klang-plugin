package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.KlangProblemFileHighlightFilter;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards {@link KlangProblemFileHighlightFilter}: the {@code com.intellij.problemFileHighlightFilter}
 * condition that opts {@code .k} files into the platform's <em>WolfTheProblemSolver</em>.
 *
 * <p>{@code WolfTheProblemSolverImpl.isToBeHighlighted(file)} (the gate that decides whether a file
 * may be marked as "contains errors" — red name in the Project view and editor tab) returns
 * {@code true} only when at least one registered filter matches the file. This filter must
 * therefore match K-lang files and ignore everything else.</p>
 */
class KlangProblemFileHighlightFilterTest extends KlangFixtureTestBase {

    private final KlangProblemFileHighlightFilter filter = new KlangProblemFileHighlightFilter();

    @Test
    void klangFileIsTracked() {
        onEdt(() -> {
            VirtualFile vf = parse("module demo;\n").getVirtualFile();
            assertThat(filter.value(vf)).isTrue();
        });
    }

    @Test
    void nonKlangFileIsIgnored() {
        onEdt(() -> {
            VirtualFile vf = fixture.addFileToProject("note.txt", "hello").getVirtualFile();
            assertThat(filter.value(vf)).isFalse();
        });
    }

    @Test
    void nullFileIsIgnored() {
        assertThat(filter.value(null)).isFalse();
    }
}

