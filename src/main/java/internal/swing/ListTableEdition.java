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

import ec.util.grid.swing.XTable;
import ec.util.list.swing.JLists;
import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.JCommand;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Philippe Charles
 * @param <ROW>
 * @param <C>
 */
@lombok.RequiredArgsConstructor
public class ListTableEdition<ROW, C extends Component> {

    public static <ROW> ListTableEdition<ROW, JList<ROW>> ofList(String name, ListTableDescriptor<ROW> listTable) {
        return new ListTableEdition<>(name, listTable, ListTableEdition::pushList, ListTableEdition::pullList);
    }

    public static <ROW> ListTableEdition<ROW, JComboBox<ROW>> ofComboBox(String name, ListTableDescriptor<ROW> listTable) {
        return new ListTableEdition<>(name, listTable, ListTableEdition::pushComboBox, ListTableEdition::pullComboBox);
    }

    public static <ROW> ListTableEdition<ROW, JTextComponent> ofText(String name, ListTableDescriptor<ROW> listTable, Function<List<ROW>, String> forward, Function<String, List<ROW>> backward) {
        return new ListTableEdition<>(name, listTable, (s, t) -> pushText(s, t, backward), (s, t) -> pullText(s, t, forward));
    }

    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final ListTableDescriptor<ROW> listTable;

    @lombok.NonNull
    private final BiConsumer<C, JTable> push;

    @lombok.NonNull
    private final BiConsumer<JTable, C> pull;

    public void edit(C component) {
        XTable table = new XTable();
        table.setNoDataRenderer(new XTable.DefaultNoDataRenderer(""));

        listTable.apply(table);

        push.accept(component, table);
        if (show(component, table, name)) {
            pull.accept(table, component);
        }
    }

    public Action toAction(C component) {
        Action result = JCommand.of(this::edit).toAction(component);
        result.putValue(Action.NAME, name);
        return result;
    }

    private static boolean show(Component parent, JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(createToolBar(table.getActionMap()), BorderLayout.NORTH);

        return SwingUtil.showOkCancelDialog(parent, panel, title);
    }

    private static JToolBar createToolBar(ActionMap am) {
        JToolBar result = new JToolBar();
        result.setFloatable(false);

        JButton item;

        item = result.add(am.get(ListTableActions.ADD_ACTION));
        item.setText(null);
        item.setToolTipText("Add");
        item.setIcon(FontAwesome.FA_PLUS.getIcon(Color.DARK_GRAY, 14f));

        item = result.add(am.get(ListTableActions.DUPLICATE_ACTION));
        item.setText(null);
        item.setToolTipText("Duplicate");
        item.setIcon(FontAwesome.FA_PLUS_SQUARE.getIcon(Color.DARK_GRAY, 14f));

        item = result.add(am.get(ListTableActions.FILL_ACTION));
        item.setText(null);
        item.setToolTipText("Auto fill");
        item.setIcon(FontAwesome.FA_MAGIC.getIcon(Color.DARK_GRAY, 14f));

        item = result.add(am.get(ListTableActions.REMOVE_ACTION));
        item.setText(null);
        item.setToolTipText("Remove");
        item.setIcon(FontAwesome.FA_MINUS.getIcon(Color.DARK_GRAY, 14f));

        item = result.add(am.get(ListTableActions.CLEAR_ACTION));
        item.setText(null);
        item.setToolTipText("Clear");
        item.setIcon(FontAwesome.FA_TIMES.getIcon(Color.DARK_GRAY, 14f));

        item = result.add(am.get(ListTableActions.MOVE_UP_ACTION));
        item.setText(null);
        item.setToolTipText("Move up");
        item.setIcon(FontAwesome.FA_ARROW_UP.getIcon(Color.DARK_GRAY, 14f));

        item = result.add(am.get(ListTableActions.MOVE_DOWN_ACTION));
        item.setText(null);
        item.setToolTipText("Move down");
        item.setIcon(FontAwesome.FA_ARROW_DOWN.getIcon(Color.DARK_GRAY, 14f));

        return result;
    }

    private static final int NO_SELECTION = -1;

    private static <ROW> void pushComboBox(JComboBox<ROW> source, JTable target) {
        ComboBoxModel<ROW> sourceModel = source.getModel();

        ListTableModel<ROW> targetModel = (ListTableModel<ROW>) target.getModel();
        targetModel.getRows().clear();
        targetModel.copyFrom(sourceModel);

        int selectedIndex = source.getSelectedIndex();
        if (selectedIndex != NO_SELECTION) {
            target.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
        } else {
            target.getSelectionModel().clearSelection();
        }
    }

    private static <ROW> void pullComboBox(JTable source, JComboBox<ROW> target) {
        ListTableModel<ROW> sourceModel = (ListTableModel<ROW>) source.getModel();

        DefaultComboBoxModel<ROW> targetModel = new DefaultComboBoxModel<>();
        sourceModel.getRows().forEach(targetModel::addElement);
        target.setModel(targetModel);

        target.setSelectedIndex(JLists.getSelectionIndexStream(source.getSelectionModel()).findFirst().orElse(NO_SELECTION));
    }

    private static <ROW> void pushList(JList<ROW> source, JTable target) {
        ListModel<ROW> sourceModel = source.getModel();

        ListTableModel<ROW> targetModel = (ListTableModel<ROW>) target.getModel();
        targetModel.getRows().clear();
        targetModel.copyFrom(sourceModel);

        JLists.setSelectionIndexStream(target.getSelectionModel(), JLists.getSelectionIndexStream(source.getSelectionModel()));
    }

    private static <ROW> void pullList(JTable source, JList<ROW> target) {
        ListTableModel<ROW> sourceModel = (ListTableModel<ROW>) source.getModel();

        DefaultListModel<ROW> targetModel = new DefaultListModel<>();
        sourceModel.getRows().forEach(targetModel::addElement);
        target.setModel(targetModel);

        JLists.setSelectionIndexStream(target.getSelectionModel(), JLists.getSelectionIndexStream(source.getSelectionModel()));
    }

    private static <ROW> void pushText(JTextComponent source, JTable target, Function<String, List<ROW>> backward) {
        String sourceModel = source.getText();

        ListTableModel<ROW> targetModel = (ListTableModel<ROW>) target.getModel();
        targetModel.getRows().clear();
        targetModel.getRows().addAll(backward.apply(sourceModel));
    }

    private static <ROW> void pullText(JTable source, JTextComponent target, Function<List<ROW>, String> forward) {
        ListTableModel<ROW> sourceModel = (ListTableModel<ROW>) source.getModel();

        String targetModel = forward.apply(sourceModel.getRows());
        target.setText(targetModel);
    }
}
