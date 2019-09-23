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
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.swing.ActionMap;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;

/**
 *
 * @author Philippe Charles
 * @param <ROW>
 */
@lombok.Getter
@lombok.Builder(builderClassName = "Builder")
public class ListTableDescriptor<ROW> {

    @lombok.NonNull
    private final Supplier<ROW> valueFactory;

    @lombok.NonNull
    private final UnaryOperator<ROW> valueDuplicator;

    @lombok.Singular
    private final List<ListTableModel.Column<ROW, Object>> columnHandlers;

    @lombok.Singular
    private final Map<Object, TableColumnDescriptor> columnDescriptors;

    public void apply(JTable table) {
        table.setTransferHandler(new ListTableItemTransferHandler());
        table.setDropMode(DropMode.INSERT);
        table.setDragEnabled(true);

        ListTableModel<ROW> model = new ListTableModel<>();
        table.setModel(model);
        columnHandlers.forEach(model.getColumns()::add);
        TableColumnDescriptor.applyAll(columnDescriptors, table.getColumnModel());

        ActionMap am = table.getActionMap();
        am.put(ListTableActions.ADD_ACTION, ListTableActions.newAddAction(table, valueFactory));
        am.put(ListTableActions.DUPLICATE_ACTION, ListTableActions.newDuplicateAction(table, valueDuplicator));
        am.put(ListTableActions.REMOVE_ACTION, ListTableActions.newRemoveAction(table));
        am.put(ListTableActions.CLEAR_ACTION, ListTableActions.newClearAction(table));
        am.put(ListTableActions.MOVE_UP_ACTION, ListTableActions.newMoveUpAction(table));
        am.put(ListTableActions.MOVE_DOWN_ACTION, ListTableActions.newMoveDownAction(table));

        table.setComponentPopupMenu(createMenu(am).getPopupMenu());
    }

    public static <ROW> Builder<ROW> builder(Supplier<ROW> valueFactory) {
        return ListTableDescriptor.<ROW>builder().valueFactory(valueFactory);
    }

    // Set default values and enforce non-null items
    private static <ROW> Builder<ROW> builder() {
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
