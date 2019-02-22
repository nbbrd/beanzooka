/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.swing;

import ec.util.completion.FileAutoCompletionSource;
import ec.util.completion.swing.FileListCellRenderer;
import ec.util.completion.swing.JAutoCompletion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.LineBorder;

/**
 *
 * @author Philippe Charles
 */
public final class FileCellEditor extends DefaultCellEditor {

    private File value;
    private final JPanel customComponent;

    public FileCellEditor() {
        super(createTextField());
        this.customComponent = new JPanel(new BorderLayout());
        customComponent.add(super.getComponent(), BorderLayout.CENTER);
        customComponent.add(createButton(), BorderLayout.EAST);
    }

    private static JTextField createTextField() {
        JTextField result = new JTextField();
        JAutoCompletion completion = new JAutoCompletion(result);
        completion.setSource(new FileAutoCompletionSource());
        completion.getList().setCellRenderer(new FileListCellRenderer(Executors.newSingleThreadExecutor()));
        result.setBorder(new LineBorder(Color.black));
        return result;
    }

    private JButton createButton() {
        JButton result = new JButton();
        result.setAction(new AbstractAction() {
            final JFileChooser fileChooser = new JFileChooser();

            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (fileChooser.showOpenDialog(getComponent()) == JFileChooser.APPROVE_OPTION) {
                    ((JTextField) FileCellEditor.super.getComponent()).setText(fileChooser.getSelectedFile().toString());
                };
            }
        });
        result.setText("\u2026");
        result.setMargin(new Insets(0, 1, 0, 1));
        return result;
    }

    @Override
    public boolean stopCellEditing() {
        String text = (String) super.getCellEditorValue();
        value = new File(text);
        return super.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    @Override
    public Component getComponent() {
        return customComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        return customComponent;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        return customComponent;
    }
}
