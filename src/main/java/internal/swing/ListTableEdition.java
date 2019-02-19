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
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

/**
 *
 * @author Philippe Charles
 * @param <ROW>
 */
@lombok.Getter
@lombok.Builder(builderClassName = "Builder")
public class ListTableEdition<ROW> {

    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final Supplier<ROW> valueFactory;

    @lombok.Singular
    private final List<ListTableModel.Column<ROW, Object>> columnHandlers;

    @lombok.Singular
    private final Map<Object, TableColumnDescriptor> columnDescriptors;

    public <C extends Component> Action toAction(Bridge<ROW, C> bridge, C component) {
        return new EditCommand<>(this, bridge).toAction(component);
    }

    public static class Builder<ROW> {

        public <CELL> Builder<ROW> column(String name, Class<CELL> type, Function<ROW, CELL> extractor, BiFunction<ROW, CELL, ROW> updater, TableColumnDescriptor descriptor) {
            columnHandler(new ListTableModel.Column(name, type, extractor, updater));
            columnDescriptor(name, descriptor);
            return this;
        }
    }

    public interface Bridge<ROW, C extends Component> {

        void push(C source, JTable target);

        void pull(JTable source, C target);

        static <ROW, C extends Component> Bridge<ROW, C> of(BiConsumer<C, JTable> push, BiConsumer<JTable, C> pull) {
            return new Bridge<ROW, C>() {
                @Override
                public void push(C source, JTable target) {
                    push.accept(source, target);
                }

                @Override
                public void pull(JTable source, C target) {
                    pull.accept(source, target);
                }
            };
        }

        static <ROW> Bridge<ROW, JComboBox<ROW>> comboBox() {
            return of(Bridges::pushComboBox, Bridges::pullComboBox);
        }

        static <ROW> Bridge<ROW, JList<ROW>> list() {
            return of(Bridges::pushList, Bridges::pullList);
        }
    }

    private static class Bridges {

        private static final int NO_SELECTION = -1;

        private static void pushComboBox(JComboBox source, JTable target) {
            ListTableModel model = ((ListTableModel) target.getModel());
            model.getRows().clear();
            model.copyFrom(source.getModel());

            int selectedIndex = source.getSelectedIndex();
            if (selectedIndex != NO_SELECTION) {
                target.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
            } else {
                target.getSelectionModel().clearSelection();
            }
        }

        private static void pullComboBox(JTable source, JComboBox target) {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            model.addAll(((ListTableModel) source.getModel()).getRows());
            target.setModel(model);

            if (!source.getSelectionModel().isSelectionEmpty()) {
                target.setSelectedIndex(source.getSelectionModel().getSelectedIndices()[0]);
            } else {
                target.setSelectedIndex(NO_SELECTION);
            }
        }

        private static void pushList(JList source, JTable target) {
            ListTableModel model = ((ListTableModel) target.getModel());
            model.getRows().clear();
            model.copyFrom(source.getModel());

            JLists.setSelectionIndexStream(target.getSelectionModel(), JLists.getSelectionIndexStream(source.getSelectionModel()));
        }

        private static void pullList(JTable source, JList target) {
            DefaultListModel model = new DefaultListModel();
            model.addAll(((ListTableModel) source.getModel()).getRows());
            target.setModel(model);

            JLists.setSelectionIndexStream(target.getSelectionModel(), JLists.getSelectionIndexStream(source.getSelectionModel()));
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class EditCommand<ROW, C extends Component> extends JCommand<C> {

        @lombok.NonNull
        private final ListTableEdition<ROW> edition;

        @lombok.NonNull
        private final Bridge<ROW, C> bridge;

        @Override
        public void execute(C component) throws Exception {
            XTable table = new XTable();
            table.setNoDataRenderer(new XTable.DefaultNoDataRenderer(""));

            ListTableModel<ROW> model = new ListTableModel<>();
            table.setModel(model);
            edition.getColumnHandlers().forEach(model.getColumns()::add);
            TableColumnDescriptor.applyAll(edition.getColumnDescriptors(), table.getColumnModel());

            bridge.push(component, table);

            if (show(component, table, edition.getValueFactory(), edition.getName())) {
                bridge.pull(table, component);
            }
        }

        @Override
        public JCommand.ActionAdapter toAction(C component) {
            JCommand.ActionAdapter result = super.toAction(component);
            result.putValue(Action.NAME, edition.getName());
            return result;
        }

        private static boolean show(Component parent, JTable table, Supplier<?> valueFactory, String title) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.getActionMap().put(ListTable.ADD_ACTION, ListTable.newAddAction(table, valueFactory));
            panel.getActionMap().put(ListTable.REMOVE_ACTION, ListTable.newRemoveAction(table));
            panel.getActionMap().put(ListTable.CLEAR_ACTION, ListTable.newClearAction(table));
            panel.getActionMap().put(ListTable.MOVE_UP_ACTION, ListTable.newMoveUpAction(table));
            panel.getActionMap().put(ListTable.MOVE_DOWN_ACTION, ListTable.newMoveDownAction(table));

            table.setComponentPopupMenu(createMenu(panel.getActionMap()).getPopupMenu());

            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(createToolBar(panel.getActionMap()), BorderLayout.NORTH);

            return SwingUtil.showOkCancelDialog(parent, panel, title);
        }

        private static JToolBar createToolBar(ActionMap am) {
            JToolBar result = new JToolBar();
            result.setFloatable(false);

            JButton item;

            item = result.add(am.get(ListTable.ADD_ACTION));
            item.setText(null);
            item.setToolTipText("Add");
            item.setIcon(FontAwesome.FA_PLUS.getIcon(Color.DARK_GRAY, 14f));

            item = result.add(am.get(ListTable.REMOVE_ACTION));
            item.setText(null);
            item.setToolTipText("Remove");
            item.setIcon(FontAwesome.FA_MINUS.getIcon(Color.DARK_GRAY, 14f));

            item = result.add(am.get(ListTable.CLEAR_ACTION));
            item.setText(null);
            item.setToolTipText("Clear");
            item.setIcon(FontAwesome.FA_TIMES.getIcon(Color.DARK_GRAY, 14f));

            item = result.add(am.get(ListTable.MOVE_UP_ACTION));
            item.setText(null);
            item.setToolTipText("Move up");
            item.setIcon(FontAwesome.FA_ARROW_UP.getIcon(Color.DARK_GRAY, 14f));

            item = result.add(am.get(ListTable.MOVE_DOWN_ACTION));
            item.setText(null);
            item.setToolTipText("Move down");
            item.setIcon(FontAwesome.FA_ARROW_DOWN.getIcon(Color.DARK_GRAY, 14f));

            return result;
        }

        private static JMenu createMenu(ActionMap am) {
            JMenu result = new JMenu();

            JMenuItem item;

            item = result.add(am.get(ListTable.ADD_ACTION));
            item.setText("Add");

            item = result.add(am.get(ListTable.REMOVE_ACTION));
            item.setText("Remove");

            item = result.add(am.get(ListTable.CLEAR_ACTION));
            item.setText("Clear");

            return result;
        }
    }
}
