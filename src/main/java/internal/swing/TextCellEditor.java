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
import java.util.function.Consumer;
import java.util.function.Function;
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
public final class TextCellEditor extends DefaultCellEditor {

    public static <T> TextCellEditor of(Function<T, String> forward, Function<String, T> backward, JTextField textField) {
        return new TextCellEditor(forward, backward, textField, null);
    }

    public static <T> TextCellEditor of(Function<T, String> forward, Function<String, T> backward, JTextField textField, Consumer<JTextField> onMoreAction) {
        return new TextCellEditor(forward, backward, textField, onMoreAction);
    }

    private final JPanel customComponent;

    private TextCellEditor(Function forward, Function backward, JTextField textField, Consumer<JTextField> onMoreAction) {
        super(textField);
        this.customComponent = new JPanel(new BorderLayout());
        this.delegate = new EditorDelegate() {
            @Override
            public void setValue(Object value) {
                textField.setText(value != null ? (String) forward.apply(value) : "");
            }

            @Override
            public Object getCellEditorValue() {
                return backward.apply(textField.getText());
            }
        };

        textField.setBorder(new LineBorder(Color.black));
        customComponent.add(textField, BorderLayout.CENTER);

        if (onMoreAction != null) {
            JButton moreButton = new JButton();
            moreButton.addActionListener(event -> onMoreAction.accept(getTextField()));
            moreButton.setText("…");
            moreButton.setMargin(new Insets(0, 1, 0, 1));
            customComponent.add(moreButton, BorderLayout.EAST);
        }
    }

    private JTextField getTextField() {
        return (JTextField) super.getComponent();
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
