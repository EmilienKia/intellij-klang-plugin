package com.github.emilienkia.klang.plugin.language.index;

import com.github.emilienkia.klang.plugin.language.KlangFileType;
import com.github.emilienkia.klang.plugin.language.psi.KlangBaseSpec;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>Multi-file indexing — milestone M4.</b>
 *
 * <p>A {@link FileBasedIndex} mapping the <em>simple name</em> of every {@code baseSpec}
 * (the type named in a {@code class Dog : public Animal} base clause → key {@code "Animal"})
 * to the files that contain it. This is the reverse of inheritance: from a base aggregate it
 * narrows the project-wide search for derived types / overriding methods to the handful of
 * files that actually mention the base name, instead of scanning every {@code .k} file.</p>
 *
 * <p>Backs the project-wide downward inheritance markers (TODO B3) via
 * {@code KlangResolveUtil.findSubAggregates} / {@code findOverridingMethods}.</p>
 */
public final class KlangBaseNameIndex extends ScalarIndexExtension<String> {

    /** Index id. */
    public static final ID<String, Void> NAME = ID.create("klang.base.name");

    /** Bump when the indexer output shape changes. */
    private static final int VERSION = 1;

    @Override
    public @NotNull ID<String, Void> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            PsiFile psi = inputData.getPsiFile();
            if (!(psi instanceof KlangFile)) return Map.of();
            Map<String, Void> keys = new HashMap<>();
            for (KlangBaseSpec base : PsiTreeUtil.collectElementsOfType(psi, KlangBaseSpec.class)) {
                String simple = simpleName(base.getQualifiedIdentifier().getText());
                if (!simple.isEmpty()) keys.put(simple, null);
            }
            return keys;
        };
    }

    /** The simple (last) name of a possibly-qualified, possibly-templated base type text. */
    private static @NotNull String simpleName(@NotNull String text) {
        String s = text.trim();
        int lt = s.indexOf('<');
        if (lt >= 0) s = s.substring(0, lt); // drop template arguments
        int idx = s.lastIndexOf("::");
        if (idx >= 0) s = s.substring(idx + 2);
        return s.trim();
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(KlangFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    // ── Query helper ──────────────────────────────────────────────────────────

    /**
     * The files that contain at least one {@code baseSpec} whose simple name is
     * {@code simpleName}. May throw {@code IndexNotReadyException} in dumb mode — callers
     * should fall back to a file-local scan.
     */
    public static @NotNull Collection<VirtualFile> filesContainingBase(@NotNull Project project,
                                                                       @NotNull String simpleName) {
        return FileBasedIndex.getInstance()
                .getContainingFiles(NAME, simpleName, GlobalSearchScope.allScope(project));
    }
}

