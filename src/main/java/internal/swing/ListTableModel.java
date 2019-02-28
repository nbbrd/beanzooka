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

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Philippe Charles
 * @param <ROW>
 */
public final class ListTableModel<ROW> extends AbstractTableModel {

    private final ListModel2<ROW> rows;
    private final ListModel2<Column<ROW, Object>> columns;

    public ListTableModel() {
        this.rows = new DefaultListModel2<>(new ArrayList<>());
        this.columns = new DefaultListModel2<>(new ArrayList<>());
        rows.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                fireTableRowsInserted(e.getIndex0(), e.getIndex1());
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                fireTableRowsUpdated(e.getIndex0(), e.getIndex1());
            }
        });
        columns.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                fireTableStructureChanged();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                fireTableStructureChanged();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                fireTableStructureChanged();
            }
        });
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).getType();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getName();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Accessor<ROW, Object> accessor = columns.get(columnIndex).getAccessor();
        ROW row = rows.get(rowIndex);
        return accessor.canRead(row) ? accessor.read(row) : null;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Accessor<ROW, Object> accessor = columns.get(columnIndex).getAccessor();
        ROW row = rows.get(rowIndex);
        if (accessor.canWrite(row)) {
            rows.set(rowIndex, accessor.write(row, value));
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        Accessor<ROW, Object> accessor = columns.get(columnIndex).getAccessor();
        ROW row = rows.get(rowIndex);
        return accessor.canWrite(row);
    }

    public List<ROW> getRows() {
        return rows;
    }

    public List<Column<ROW, Object>> getColumns() {
        return columns;
    }

    public void copyFrom(ListModel<ROW> model) {
        for (int i = 0; i < model.getSize(); i++) {
            rows.add(model.getElementAt(i));
        }
    }

    public void copyTo(MutableComboBoxModel<ROW> model) {
        rows.forEach(model::addElement);
    }

    public void copyTo(DefaultListModel<ROW> model) {
        rows.forEach(model::addElement);
    }

    @lombok.Value
    public static final class Column<ROW, CELL> {

        @lombok.NonNull
        private String name;

        @lombok.NonNull
        private Class<CELL> type;

        @lombok.NonNull
        private Accessor<ROW, CELL> accessor;
    }
}
