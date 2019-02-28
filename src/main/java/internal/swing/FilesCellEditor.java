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

import beanzooka.core.Jdk;
import ec.util.completion.FileAutoCompletionSource;
import ec.util.completion.swing.FileListCellRenderer;
import ec.util.completion.swing.JAutoCompletion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.LineBorder;

/**
 *
 * @author Philippe Charles
 */
public final class FilesCellEditor extends DefaultCellEditor {

    private final JPanel customComponent;

    public FilesCellEditor() {
        this(new JTextField());
    }

    private FilesCellEditor(JTextField textField) {
        super(textField);
        this.customComponent = new JPanel(new BorderLayout());
        this.delegate = new EditorDelegate() {
            @Override
            public void setValue(Object value) {
                textField.setText((value != null) ? Jdk.fromFiles((List<File>) value) : "");
            }

            @Override
            public Object getCellEditorValue() {
                return Jdk.toFiles(textField.getText());
            }
        };

        JAutoCompletion completion = new JAutoCompletion(textField);
        completion.setSource(new FileAutoCompletionSource());
        completion.getList().setCellRenderer(new FileListCellRenderer(Executors.newSingleThreadExecutor()));
        completion.setSeparator(File.pathSeparator);
        textField.setBorder(new LineBorder(Color.black));

        customComponent.add(textField, BorderLayout.CENTER);
//        customComponent.add(createMoreButton(), BorderLayout.EAST);
    }

    private void onMoreAction(ActionEvent e) {
    }

    private JButton createMoreButton() {
        JButton result = new JButton();
        result.addActionListener(this::onMoreAction);
        result.setText("\u2026");
        result.setMargin(new Insets(0, 1, 0, 1));
        return result;
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
