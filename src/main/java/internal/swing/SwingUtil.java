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
package internal.swing;

import ec.util.various.swing.JCommand;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.CLOSED_OPTION;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SwingUtil {

    public <X extends JComponent> void onDoubleClick(X component, JCommand<X> command) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && command.isEnabled(component)) {
                    command.executeSafely(component);
                }
            }
        });
    }

    public <X> List<X> concat(X element, List<X> list) {
        List<X> result = new ArrayList<>(list);
        result.add(0, element);
        return result;
    }

    public <E> ComboBoxModel<E> modelOf(List<E> list) {
        return new DefaultComboBoxModel(list.toArray());
    }

    public <E> List<E> listOf(ComboBoxModel<E> model) {
        return IntStream
                .range(0, model.getSize())
                .mapToObj(model::getElementAt)
                .collect(Collectors.toList());
    }

    public boolean showOkCancelDialog(Component parent, JComponent component, String title) {
        JOptionPane pane = new JOptionPane(component, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, null);

        JDialog dialog = pane.createDialog(title);
        dialog.setResizable(true);
        dialog.setSize(new Dimension(600, 400));
        dialog.setVisible(true);
        dialog.dispose();

        Object selectedValue = pane.getValue();
        int result = selectedValue instanceof Integer ? (Integer) selectedValue : CLOSED_OPTION;
        return result == JOptionPane.OK_OPTION;
    }
}
