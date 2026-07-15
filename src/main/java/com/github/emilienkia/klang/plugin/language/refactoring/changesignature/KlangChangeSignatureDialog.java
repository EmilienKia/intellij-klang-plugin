package com.github.emilienkia.klang.plugin.language.refactoring.changesignature;

import com.github.emilienkia.klang.plugin.language.psi.KlangFunctionDecl;
import com.github.emilienkia.klang.plugin.language.psi.KlangParameterList;
import com.github.emilienkia.klang.plugin.language.psi.KlangParameterSpec;
import com.github.emilienkia.klang.plugin.language.psi.KlangSpecifier;
import com.github.emilienkia.klang.plugin.language.psi.impl.KlangResolveUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Change Signature" dialog for a K-lang function/method: name, return type, specifier flags
 * (public/protected/private/static/const/abstract/final/override/default) and a free-text-per-row
 * parameter list (add/edit/remove/reorder), plus propagation checkboxes for the override chain.
 */
public final class KlangChangeSignatureDialog extends DialogWrapper {

    private final KlangFunctionDecl function;
    private final boolean nameEditable;
    private final boolean returnTypeEditable;
    private final boolean hasBase;

    private JBTextField nameField;
    private JBTextField returnTypeField;
    private final Map<String, JBCheckBox> specifierBoxes = new LinkedHashMap<>();
    private DefaultListModel<String> paramsModel;
    private JBCheckBox propagateDownBox;
    private JBCheckBox propagateUpBox;

    public KlangChangeSignatureDialog(@NotNull KlangFunctionDecl function) {
        super(function.getProject());
        this.function = function;
        boolean ctorOrDtor = KlangResolveUtil.isConstructorOrDestructor(function);
        this.nameEditable = !ctorOrDtor && function.getName() != null;
        this.returnTypeEditable = !ctorOrDtor;
        this.hasBase = !KlangResolveUtil.findOverriddenMethods(function).isEmpty();
        setTitle("Change Signature");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 8));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = JBUI.insets(2, 4);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        top.add(new JBLabel("Name:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JBTextField(function.getName() != null ? function.getName() : "");
        nameField.setEnabled(nameEditable);
        top.add(nameField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        top.add(new JBLabel("Return type:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        returnTypeField = new JBTextField(KlangChangeSignatureProcessor.initialReturnType(function));
        returnTypeField.setEnabled(returnTypeEditable);
        top.add(returnTypeField, c);
        root.add(top, BorderLayout.NORTH);

        JPanel specPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        specPanel.setBorder(BorderFactory.createTitledBorder("Specifiers"));
        Set<String> current = currentSpecifiers(function);
        for (String s : KlangChangeSignatureModel.SPECIFIER_ORDER) {
            JBCheckBox box = new JBCheckBox(s, current.contains(s));
            specifierBoxes.put(s, box);
            specPanel.add(box);
        }

        paramsModel = new DefaultListModel<>();
        for (KlangParameterSpec p : parameterSpecs(function)) paramsModel.addElement(p.getText().trim());
        JBList<String> paramsListUi = new JBList<>(paramsModel);
        JPanel paramsPanel = ToolbarDecorator.createDecorator(paramsListUi)
                .setAddAction(btn -> addOrEditParam(paramsListUi, -1))
                .setEditAction(btn -> addOrEditParam(paramsListUi, paramsListUi.getSelectedIndex()))
                .setRemoveAction(btn -> {
                    int idx = paramsListUi.getSelectedIndex();
                    if (idx >= 0) paramsModel.remove(idx);
                })
                .setMoveUpAction(btn -> moveParam(paramsListUi, -1))
                .setMoveDownAction(btn -> moveParam(paramsListUi, 1))
                .createPanel();
        paramsPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
        paramsPanel.setPreferredSize(new Dimension(420, 160));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.add(specPanel, BorderLayout.NORTH);
        center.add(paramsPanel, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        propagateDownBox = new JBCheckBox("Propagate to overriding methods (subclasses)", true);
        south.add(propagateDownBox);
        if (hasBase) {
            propagateUpBox = new JBCheckBox("Also apply to overridden base method(s)", false);
            south.add(propagateUpBox);
        }
        root.add(south, BorderLayout.SOUTH);

        root.setPreferredSize(new Dimension(480, 400));
        return root;
    }

    private void addOrEditParam(@NotNull JBList<String> list, int index) {
        String initial = index >= 0 ? paramsModel.get(index) : "";
        String text = Messages.showInputDialog(function.getProject(),
                "Parameter (e.g. \"x : int\" or \"x : int = 0\"):",
                index >= 0 ? "Edit Parameter" : "Add Parameter",
                Messages.getQuestionIcon(), initial, null);
        if (text == null) return;
        text = text.trim();
        if (text.isEmpty()) return;
        if (index >= 0) {
            paramsModel.set(index, text);
        } else {
            paramsModel.addElement(text);
            list.setSelectedIndex(paramsModel.size() - 1);
        }
    }

    private void moveParam(@NotNull JBList<String> list, int delta) {
        int idx = list.getSelectedIndex();
        int target = idx + delta;
        if (idx < 0 || target < 0 || target >= paramsModel.size()) return;
        String value = paramsModel.remove(idx);
        paramsModel.add(target, value);
        list.setSelectedIndex(target);
    }

    private static @NotNull Set<String> currentSpecifiers(@NotNull KlangFunctionDecl fn) {
        return fn.getSpecifierList().stream().map(KlangSpecifier::getText).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static @NotNull List<KlangParameterSpec> parameterSpecs(@NotNull KlangFunctionDecl fn) {
        KlangParameterList list = fn.getParameterList();
        return list != null ? list.getParameterSpecList() : Collections.emptyList();
    }

    public @NotNull KlangChangeSignatureModel buildModel() {
        List<String> specifiers = new ArrayList<>();
        for (String s : KlangChangeSignatureModel.SPECIFIER_ORDER) {
            if (specifierBoxes.get(s).isSelected()) specifiers.add(s);
        }
        List<String> params = new ArrayList<>();
        for (int i = 0; i < paramsModel.size(); i++) params.add(paramsModel.get(i));
        String name = nameEditable ? nameField.getText().trim() : function.getName();
        String returnType = returnTypeEditable ? returnTypeField.getText().trim() : "";
        boolean propagateDown = propagateDownBox.isSelected();
        boolean propagateUp = propagateUpBox != null && propagateUpBox.isSelected();
        return new KlangChangeSignatureModel(
                name != null ? name : "",
                returnType,
                params,
                specifiers,
                propagateDown,
                propagateUp);
    }
}

