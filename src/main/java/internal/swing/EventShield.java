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

import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Philippe Charles
 */
public final class EventShield {

    private boolean updating = false;

    public PropertyChangeListener wrap(PropertyChangeListener listener) {
        return event -> {
            if (!updating) {
                updating = true;
                listener.propertyChange(event);
                updating = false;
            }
        };
    }

    public ItemListener wrap(ItemListener listener) {
        return event -> {
            if (!updating) {
                updating = true;
                listener.itemStateChanged(event);
                updating = false;
            }
        };
    }

    public ListSelectionListener wrap(ListSelectionListener listener) {
        return event -> {
            if (!updating) {
                updating = true;
                listener.valueChanged(event);
                updating = false;
            }
        };
    }
}
