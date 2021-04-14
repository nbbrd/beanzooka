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

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Builder
public final class TableColumnDescriptor {

    public static final TableColumnDescriptor EMPTY = builder().build();
    
    private final Supplier<TableCellRenderer> cellRenderer;
    private final Supplier<TableCellEditor> cellEditor;
    private final int preferedWidth;

    public void apply(TableColumn column) {
        Optional.ofNullable(cellRenderer.get()).ifPresent(column::setCellRenderer);
        Optional.ofNullable(cellEditor.get()).ifPresent(column::setCellEditor);
        if (preferedWidth != -1) {
            column.setPreferredWidth(preferedWidth);
        }
    }

    public static void applyAll(Map<Object, TableColumnDescriptor> descriptors, TableColumnModel model) {
        descriptors.forEach((id, descriptor) -> getColumnById(model, id).ifPresent(descriptor::apply));
    }

    private static Optional<TableColumn> getColumnById(TableColumnModel model, Object id) {
        return Optional.ofNullable(model.getColumn(model.getColumnIndex(id)));
    }

    public static Builder builder() {
        return new Builder()
                .cellRenderer(() -> null)
                .cellEditor(() -> null)
                .preferedWidth(-1);
    }
}
