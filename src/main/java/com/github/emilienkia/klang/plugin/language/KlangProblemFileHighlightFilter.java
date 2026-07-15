package com.github.emilienkia.klang.plugin.language;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

/**
 * Opts K-lang files into the platform's <em>WolfTheProblemSolver</em> problem tracking.
 *
 * <p>The Project view and editor tab paint a file name in red ("contains errors") when
 * {@code WolfTheProblemSolver.isProblemFile(file)} is {@code true}. The solver only records
 * error-severity highlights for a file when {@code isToBeHighlighted(file)} returns
 * {@code true}, and that method returns {@code true} <strong>only</strong> if at least one
 * registered {@code com.intellij.problemFileHighlightFilter} condition matches the file
 * (see {@code WolfTheProblemSolverImpl.isToBeHighlighted} / {@code FILTER_EP_NAME}).
 *
 * <p>The platform ships such filters for its built-in content (e.g. Java), but a third-party
 * language has none by default — so without this filter the errors produced by
 * {@link KlangAnnotator} (parser/{@code PsiErrorElement}) and the K-lang ERROR-level
 * inspections are highlighted in the editor yet never mark the file as "problematic".
 * Registering this condition makes a {@code .k} file with at least one error appear with a
 * red underlined name in the Project tree and in its editor tab, like Java.
 */
public final class KlangProblemFileHighlightFilter implements Condition<VirtualFile> {

    @Override
    public boolean value(@Nullable VirtualFile file) {
        return file != null && FileTypeRegistry.getInstance().isFileOfType(file, KlangFileType.INSTANCE);
    }
}

