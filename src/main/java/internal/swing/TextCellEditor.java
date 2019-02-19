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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.LineBorder;

/**
 *
 * @author Philippe Charles
 */
public final class TextCellEditor extends DefaultCellEditor {

    private final JPanel customComponent;

    public TextCellEditor() {
        super(createTextField());
        this.customComponent = new JPanel(new BorderLayout());
        customComponent.add(super.getComponent(), BorderLayout.CENTER);
        customComponent.add(createButton(), BorderLayout.EAST);
    }

    private static JTextField createTextField() {
        JTextField result = new JTextField();
        result.setBorder(new LineBorder(Color.black));
        return result;
    }

    private JButton createButton() {
        JButton result = new JButton();
        result.setAction(new AbstractAction() {
            final JTextArea fileChooser = new JTextArea();

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea textArea = new JTextArea(((JTextField) TextCellEditor.super.getComponent()).getText());
                if (SwingUtil.showOkCancelDialog(null, new JScrollPane(textArea), "")) {
                    ((JTextField) TextCellEditor.super.getComponent()).setText(textArea.getText());
                }
            }
        });
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
