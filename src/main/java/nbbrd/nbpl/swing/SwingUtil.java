/*
 * Copyright 2018 National Bank of Belgium
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
package nbbrd.nbpl.swing;

import ec.util.various.swing.JCommand;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class SwingUtil {

    <X extends JComponent> void onDoubleClick(X component, JCommand<X> command) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && command.isEnabled(component)) {
                    command.executeSafely(component);
                }
            }
        });
    }

    <X> List<X> concat(X element, List<X> list) {
        List<X> result = new ArrayList<>(list);
        result.add(0, element);
        return result;
    }

    <E> ComboBoxModel<E> modelOf(List<E> list) {
        return new DefaultComboBoxModel(list.toArray());
    }
}
