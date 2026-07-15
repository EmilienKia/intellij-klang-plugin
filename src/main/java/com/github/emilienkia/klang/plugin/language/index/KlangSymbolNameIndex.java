package com.github.emilienkia.klang.plugin.language.index;

import com.github.emilienkia.klang.plugin.language.KlangFileType;
import com.github.emilienkia.klang.plugin.language.psi.KlangAggregateDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangEnumDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionBody;
import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangNamedElement;
import com.github.emilienkia.klang.plugin.language.psi.KlangUnionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangVariableDecl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
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
 * <b>Multi-file indexing — milestone M5.</b>
 *
 * <p>A {@link FileBasedIndex} mapping the <em>simple name</em> of every <b>importable</b>
 * top-level / namespace-level declaration (aggregate, enum, union, free function or global
 * variable) to the files that declare it. Members of an aggregate and locals are excluded:
 * they are reached through their owner, not through an {@code import}.</p>
 *
 * <p>This is the lightweight precursor to the planned {@code KlangFqnIndex}. Its only consumer
 * today is the <em>add-import</em> quick fix of the unresolved-reference inspection: from an
 * unresolved simple name it lists the files (hence modules, via {@link KlangModuleModel}) that
 * declare a matching symbol, so the user can import the right module.</p>
 *
 * <p>See {@code docs/klang/multifile-indexing-plan.md} and {@code docs/klang/navigation-plan.md}
 * (Phase 4 — diagnostics).</p>
 */
public final class KlangSymbolNameIndex extends ScalarIndexExtension<String> {

    /** Index id. */
    public static final ID<String, Void> NAME = ID.create("klang.symbol.name");

    /** Bump when the indexer output shape changes so the platform rebuilds the index. */
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
            collect(psi, KlangAggregateDecl.class, keys);
            collect(psi, KlangEnumDecl.class, keys);
            collect(psi, KlangUnionDecl.class, keys);
            collect(psi, KlangFunctionDecl.class, keys);
            collect(psi, KlangVariableDecl.class, keys);
            return keys;
        };
    }

    /** Collects the simple names of importable {@code type} declarations found in {@code psi}. */
    private static <T extends PsiElement & KlangNamedElement> void collect(
            @NotNull PsiFile psi, @NotNull Class<T> type, @NotNull Map<String, Void> keys) {
        for (T decl : PsiTreeUtil.collectElementsOfType(psi, type)) {
            if (!isImportable(decl)) continue;
            String name = decl.getName();
            if (name != null && !name.isBlank()) keys.put(name, null);
        }
    }

    /**
     * A declaration is importable when it lives at module/namespace scope — i.e. it is not a
     * member of an aggregate nor a local inside a function body. Such symbols are reached by
     * importing their module, whereas members/locals are reached through their owner.
     */
    private static boolean isImportable(@NotNull PsiElement decl) {
        return PsiTreeUtil.getParentOfType(decl, KlangAggregateDecl.class, KlangFunctionBody.class) == null;
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
     * The files that declare at least one importable symbol whose simple name is {@code simpleName}.
     * May throw {@code IndexNotReadyException} in dumb mode — callers should fail soft.
     */
    public static @NotNull Collection<VirtualFile> filesDeclaring(@NotNull Project project,
                                                                  @NotNull String simpleName) {
        return FileBasedIndex.getInstance()
                .getContainingFiles(NAME, simpleName, GlobalSearchScope.allScope(project));
    }
}

