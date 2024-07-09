package internal.swing;

import ec.util.various.swing.JCommand;
import lombok.NonNull;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public abstract class JCommand2<C> extends JCommand<C> {

    @Override
    public @NonNull ActionAdapter2 toAction(@NonNull C component) {
        return new ActionAdapter2(component);
    }

    public class ActionAdapter2 extends ActionAdapter {

        public ActionAdapter2(@NonNull C component) {
            super(component);
        }

        @NonNull
        public ActionAdapter withWeakTableModelListener(@NonNull TableModel source) {
            TableModelListener realListener = evt -> refreshActionState();
            putValue("TableModelListener", realListener);
            source.addTableModelListener(new WeakTableModelListener(realListener) {
                @Override
                protected void unregister(@NonNull Object source) {
                    ((TableModel) source).removeTableModelListener(this);
                }
            });
            return this;
        }
    }

    private abstract static class WeakTableModelListener extends WeakEventListener<TableModelListener> implements TableModelListener {

        public WeakTableModelListener(@NonNull TableModelListener delegate) {
            super(delegate);
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            TableModelListener listener = delegate.get();
            if (listener != null) {
                listener.tableChanged(e);
            } else {
                unregister(e.getSource());
            }
        }
    }
}
