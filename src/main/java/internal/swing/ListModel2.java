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

import java.util.List;
import javax.swing.ListModel;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface ListModel2<E> extends ListModel<E>, List<E> {

    @Override
    default int getSize() {
        return size();
    }

    @Override
    default E getElementAt(int index) {
        return get(index);
    }
}
