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

import ec.util.datatransfer.LocalDataTransfer;
import ec.util.grid.swing.XTable;
import ec.util.list.swing.JLists;
import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.JCommand;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;
import javax.swing.text.JTextComponent;

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

    @lombok.NonNull
    private final UnaryOperator<ROW> valueDuplicator;

    @lombok.Singular
    private final List<ListTableModel.Column<ROW, Object>> columnHandlers;

    @lombok.Singular
    private final Map<Object, TableColumnDescriptor> columnDescriptors;

    public <C extends Component> Action toAction(Bridge<ROW, C> bridge, C component) {
        return new EditCommand<>(this, bridge).toAction(component);
    }

    public static <ROW> Builder<ROW> builder() {
        return new Builder<ROW>()
                .valueDuplicator(UnaryOperator.identity());
    }

    public static class Builder<ROW> {

        public <CELL> Builder<ROW> column(String name, Class<CELL> type, Function<ROW, CELL> extractor, BiFunction<ROW, CELL, ROW> updater, TableColumnDescriptor descriptor) {
            columnHandler(new ListTableModel.Column(name, type, Accessor.of(extractor, updater)));
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

        static <ROW> Bridge<ROW, JTextComponent> text(Function<List<ROW>, String> forward, Function<String, List<ROW>> backward) {
            return of((s, t) -> Bridges.pushText(s, t, backward), (s, t) -> Bridges.pullText(s, t, forward));
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
            ((ListTableModel) source.getModel()).getRows().forEach(model::addElement);
            target.setModel(model);

            target.setSelectedIndex(JLists.getSelectionIndexStream(source.getSelectionModel()).findFirst().orElse(NO_SELECTION));
        }

        private static void pushList(JList source, JTable target) {
            ListTableModel model = ((ListTableModel) target.getModel());
            model.getRows().clear();
            model.copyFrom(source.getModel());

            JLists.setSelectionIndexStream(target.getSelectionModel(), JLists.getSelectionIndexStream(source.getSelectionModel()));
        }

        private static void pullList(JTable source, JList target) {
            DefaultListModel model = new DefaultListModel();
            ((ListTableModel) source.getModel()).getRows().forEach(model::addElement);
            target.setModel(model);

            JLists.setSelectionIndexStream(target.getSelectionModel(), JLists.getSelectionIndexStream(source.getSelectionModel()));
        }

        private static <ROW> void pushText(JTextComponent source, JTable target, Function<String, List<ROW>> backward) {
            ListTableModel<ROW> model = ((ListTableModel<ROW>) target.getModel());
            model.getRows().clear();
            model.getRows().addAll(backward.apply(source.getText()));
        }

        private static <ROW> void pullText(JTable source, JTextComponent target, Function<List<ROW>, String> forward) {
            ListTableModel<ROW> model = ((ListTableModel<ROW>) source.getModel());
            target.setText(forward.apply(model.getRows()));
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

            table.setTransferHandler(new ListTableItemTransferHandler());
            table.setDropMode(DropMode.INSERT);
            table.setDragEnabled(true);

            ListTableModel<ROW> model = new ListTableModel<>();
            table.setModel(model);
            edition.getColumnHandlers().forEach(model.getColumns()::add);
            TableColumnDescriptor.applyAll(edition.getColumnDescriptors(), table.getColumnModel());

            bridge.push(component, table);

            if (show(component, table, edition.getValueFactory(), edition.getValueDuplicator(), edition.getName())) {
                bridge.pull(table, component);
            }
        }

        @Override
        public JCommand.ActionAdapter toAction(C component) {
            JCommand.ActionAdapter result = super.toAction(component);
            result.putValue(Action.NAME, edition.getName());
            return result;
        }

        private static boolean show(Component parent, JTable table, Supplier<?> valueFactory, UnaryOperator<?> valueDuplicator, String title) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.getActionMap().put(ListTableActions.ADD_ACTION, ListTableActions.newAddAction(table, valueFactory));
            panel.getActionMap().put(ListTableActions.DUPLICATE_ACTION, ListTableActions.newDuplicateAction(table, valueDuplicator));
            panel.getActionMap().put(ListTableActions.REMOVE_ACTION, ListTableActions.newRemoveAction(table));
            panel.getActionMap().put(ListTableActions.CLEAR_ACTION, ListTableActions.newClearAction(table));
            panel.getActionMap().put(ListTableActions.MOVE_UP_ACTION, ListTableActions.newMoveUpAction(table));
            panel.getActionMap().put(ListTableActions.MOVE_DOWN_ACTION, ListTableActions.newMoveDownAction(table));

            table.setComponentPopupMenu(createMenu(panel.getActionMap()).getPopupMenu());

            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(createToolBar(panel.getActionMap()), BorderLayout.NORTH);

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

        private static JMenu createMenu(ActionMap am) {
            JMenu result = new JMenu();

            JMenuItem item;

            item = result.add(am.get(ListTableActions.ADD_ACTION));
            item.setText("Add");

            item = result.add(am.get(ListTableActions.DUPLICATE_ACTION));
            item.setText("Duplicate");

            item = result.add(am.get(ListTableActions.REMOVE_ACTION));
            item.setText("Remove");

            item = result.add(am.get(ListTableActions.CLEAR_ACTION));
            item.setText("Clear");

            return result;
        }
    }

    private static final class ListTableItemTransferHandler extends TransferHandler {

        private static final LocalDataTransfer<int[]> INT_ARRAY = LocalDataTransfer.of(int[].class);

        @Override
        protected Transferable createTransferable(JComponent c) {
            return INT_ARRAY.createTransferable(((JTable) c).getSelectedRows());
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            return info.isDrop() && INT_ARRAY.canImport(info);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            INT_ARRAY.getData(support)
                    .ifPresent(o -> importData(o, (JTable) support.getComponent(), (JTable.DropLocation) support.getDropLocation()));
            return true;
        }

        private void importData(int[] indices, JTable target, JTable.DropLocation dl) {
            int index = dl.getRow();
            if (indices[0] < index) {
                index = index - indices.length;
            }
            move((ListTableModel) target.getModel(), (ListTableModel) target.getModel(), indices, index);
            target.getSelectionModel().setSelectionInterval(index, index + indices.length - 1);
        }

        private static void move(ListTableModel from, ListTableModel to, int[] selection, int dropIndex) {
            List reversedItems = new ArrayList(selection.length);
            for (int i = selection.length - 1; i >= 0; i--) {
                reversedItems.add(from.getRows().remove(selection[i]));
            }
            to.getRows().addAll(dropIndex, reversedItems);
        }
    }
}
