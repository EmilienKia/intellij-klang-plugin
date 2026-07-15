package com.github.emilienkia.klang.plugin.language.index;

import com.github.emilienkia.klang.plugin.language.KlangFileType;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * <b>Multi-file indexing — milestone M1.</b>
 *
 * <p>A {@link FileBasedIndex} mapping every {@code .k} file to the name of its declared
 * {@code module} (e.g. {@code "the::game"}), or to {@link #ANONYMOUS} when the file declares
 * no module. One key per file.</p>
 *
 * <p>This is the persistent, incrementally-maintained cache that backs {@code KlangModuleModel}:
 * it lets the plugin enumerate every module name in the project, count how many files declare a
 * module (to pick the project's module policy A/B/C) and list the files belonging to a given
 * module — without re-parsing the whole project on every lookup.</p>
 *
 * <p>See {@code docs/klang/multifile-indexing-plan.md}.</p>
 */
public final class KlangModuleNameIndex extends ScalarIndexExtension<String> {

    /** Index id. */
    public static final ID<String, Void> NAME = ID.create("klang.module.name");

    /**
     * Sentinel key for a file that declares <em>no</em> {@code module} (the anonymous module).
     * A real module name is always a non-empty {@code ::}-joined identifier, so the empty
     * string can never collide with one.
     */
    public static final String ANONYMOUS = "";

    /** Bump when the indexer output shape changes so the platform rebuilds the index. */
    private static final int VERSION = 1;

    @Override
    public @NotNull ID<String, Void> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            String moduleName = ANONYMOUS;
            PsiFile psi = inputData.getPsiFile();
            if (psi instanceof KlangFile) {
                String[] segments = KlangResolveUtil.getModuleName(psi);
                if (segments.length > 0) {
                    moduleName = String.join("::", segments);
                }
            }
            return Collections.singletonMap(moduleName, null);
        };
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
}


