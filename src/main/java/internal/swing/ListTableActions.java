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

import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.swing.Action;
import javax.swing.JTable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ListTableActions {

    public final String ADD_ACTION = "add";
    public final String DUPLICATE_ACTION = "duplicate";
    public final String REMOVE_ACTION = "remove";
    public final String CLEAR_ACTION = "clear";
    public final String MOVE_UP_ACTION = "moveUp";
    public final String MOVE_DOWN_ACTION = "moveDown";

    public <ROW> Action newAddAction(JTable table, Supplier<ROW> valueFactory) {
        return new AddCommand<>(valueFactory).toAction(table);
    }

    public <ROW> Action newDuplicateAction(JTable table, UnaryOperator<ROW> valueDuplicator) {
        return new DuplicateCommand<>(valueDuplicator).toAction(table);
    }

    public Action newRemoveAction(JTable table) {
        return new RemoveCommand().toAction(table);
    }

    public Action newClearAction(JTable table) {
        return new ClearCommand().toAction(table);
    }

    public Action newMoveUpAction(JTable table) {
        return new MoveUpCommand().toAction(table);
    }

    public Action newMoveDownAction(JTable table) {
        return new MoveDownCommand().toAction(table);
    }

    private static abstract class ListTableCommand<ROW> extends JCommand<JTable> {

        @Override
        public boolean isEnabled(JTable component) {
            return component.getModel() instanceof ListTableModel;
        }

        @Override
        public ActionAdapter toAction(JTable component) {
            return super.toAction(component)
                    .withWeakPropertyChangeListener(component, "model");
        }

        protected ListTableModel<ROW> getModel(JTable component) {
            return ((ListTableModel<ROW>) component.getModel());
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class AddCommand<ROW> extends ListTableCommand<ROW> {

        @lombok.NonNull
        private final Supplier<ROW> valueSupplier;

        @Override
        public void execute(JTable component) throws Exception {
            getModel(component).getRows().add(valueSupplier.get());
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class DuplicateCommand<ROW> extends ListTableCommand<ROW> {

        @lombok.NonNull
        private final UnaryOperator<ROW> valueDuplicator;

        @Override
        public boolean isEnabled(JTable component) {
            return super.isEnabled(component)
                    && !component.getSelectionModel().isSelectionEmpty();
        }

        @Override
        public void execute(JTable component) throws Exception {
            int[] selection = JLists.getSelectionIndexStream(component.getSelectionModel()).toArray();
            for (int i = 0; i < selection.length; i++) {
                List<ROW> rows = getModel(component).getRows();
                rows.add(valueDuplicator.apply(rows.get(selection[i])));
            }
        }

        @Override
        public ActionAdapter toAction(JTable component) {
            return super.toAction(component)
                    .withWeakListSelectionListener(component.getSelectionModel());
        }
    }

    private static final class RemoveCommand extends ListTableCommand<Object> {

        @Override
        public boolean isEnabled(JTable component) {
            return super.isEnabled(component)
                    && !component.getSelectionModel().isSelectionEmpty();
        }

        @Override
        public void execute(JTable component) throws Exception {
            int[] selection = JLists.getSelectionIndexStream(component.getSelectionModel()).toArray();
            for (int i = 0; i < selection.length; i++) {
                getModel(component).getRows().remove(selection[selection.length - i - 1]);
            }
        }

        @Override
        public ActionAdapter toAction(JTable component) {
            return super.toAction(component)
                    .withWeakListSelectionListener(component.getSelectionModel());
        }
    }

    private static final class ClearCommand extends ListTableCommand<Object> {

        @Override
        public boolean isEnabled(JTable component) {
            return super.isEnabled(component)
                    && !getModel(component).getRows().isEmpty();
        }

        @Override
        public void execute(JTable component) throws Exception {
            getModel(component).getRows().clear();
            component.getSelectionModel().clearSelection();
        }

        @Override
        public ActionAdapter toAction(JTable component) {
            return super.toAction(component)
                    .withWeakListSelectionListener(component.getSelectionModel());
        }
    }

    private static final class MoveUpCommand extends ListTableCommand<Object> {

        @Override
        public boolean isEnabled(JTable component) {
            return super.isEnabled(component)
                    && JLists.isSingleSelectionIndex(component.getSelectionModel())
                    && component.getSelectedRow() > 0;
        }

        @Override
        public void execute(JTable component) throws Exception {
            ListTableModel model = getModel(component);
            int index = component.getSelectedRow();
            Object value = model.getRows().remove(index);
            model.getRows().add(index - 1, value);
            component.getSelectionModel().setSelectionInterval(index - 1, index - 1);
        }

        @Override
        public ActionAdapter toAction(JTable component) {
            return super.toAction(component)
                    .withWeakListSelectionListener(component.getSelectionModel());
        }
    }

    private static final class MoveDownCommand extends ListTableCommand<Object> {

        @Override
        public boolean isEnabled(JTable component) {
            return super.isEnabled(component)
                    && JLists.isSingleSelectionIndex(component.getSelectionModel())
                    && component.getSelectedRow() < getModel(component).getRows().size() - 1;
        }

        @Override
        public void execute(JTable component) throws Exception {
            ListTableModel model = getModel(component);
            int index = component.getSelectedRow();
            Object value = model.getRows().remove(index);
            model.getRows().add(index + 1, value);
            component.getSelectionModel().setSelectionInterval(index + 1, index + 1);
        }

        @Override
        public ActionAdapter toAction(JTable component) {
            return super.toAction(component)
                    .withWeakListSelectionListener(component.getSelectionModel());
        }
    }
}
