package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.index.KdiLibraryService;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>KDI library support — Settings UI.</b>
 *
 * <p>Project configurable (Settings → K-lang → KDI Libraries) that lets the user add or
 * remove {@code .kdi} file / directory paths for the project. The list is persisted by
 * {@link KdiLibraryService}; changes take effect immediately after {@code Apply}.</p>
 *
 * <p>Registered in {@code plugin.xml} as a {@code projectConfigurable} under the
 * {@code com.intellij} namespace with {@code parentId="language"} so it appears under the
 * K-lang language settings group.</p>
 */
public final class KdiLibraryConfigurable implements Configurable {

    private final Project project;

    /** List model backing the UI list — initialised lazily in {@link #createComponent()}. */
    private @Nullable DefaultListModel<String> listModel;

    /** The root panel returned to IntelliJ — kept for reference; not used directly. */
    private @Nullable JPanel panel;

    public KdiLibraryConfigurable(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @Nls String getDisplayName() {
        return "KDI Libraries";
    }

    @Override
    public @Nullable JComponent createComponent() {
        listModel = new DefaultListModel<>();

        JBList<String> list = new JBList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.getEmptyText().setText("No KDI files or directories configured");

        panel = ToolbarDecorator.createDecorator(list)
                .setAddAction(button -> {
                    FileChooserDescriptor descriptor =
                            FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                                    .withTitle("Select KDI File or Directory")
                                    .withDescription("Choose a .kdi file or a directory that contains .kdi files");
                    var chosen = FileChooser.chooseFiles(descriptor, project, null);
                    for (var vf : chosen) {
                        String path = vf.getPath();
                        if (!listModel.contains(path)) {
                            listModel.addElement(path);
                        }
                    }
                })
                .setRemoveAction(button -> {
                    int idx = list.getSelectedIndex();
                    if (idx >= 0) {
                        listModel.remove(idx);
                    }
                })
                .disableUpDownActions()
                .createPanel();

        // Populate from the current service state.
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        if (listModel == null) return false;
        List<String> current = KdiLibraryService.getInstance(project).getPaths();
        List<String> ui = toList(listModel);
        return !current.equals(ui);
    }

    @Override
    public void apply() {
        if (listModel == null) return;
        KdiLibraryService.getInstance(project).setPaths(toList(listModel));
    }

    @Override
    public void reset() {
        if (listModel == null) return;
        listModel.clear();
        for (String path : KdiLibraryService.getInstance(project).getPaths()) {
            listModel.addElement(path);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private static @NotNull List<String> toList(@NotNull DefaultListModel<String> model) {
        List<String> list = new ArrayList<>(model.size());
        for (int i = 0; i < model.size(); i++) {
            list.add(model.get(i));
        }
        return list;
    }
}

