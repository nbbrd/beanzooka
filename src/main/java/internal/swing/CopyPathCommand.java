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

import ec.util.various.swing.JCommand;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.Optional;

/**
 *
 * @author Philippe Charles
 */
public abstract class CopyPathCommand<T> extends JCommand<T> {

    @Override
    public boolean isEnabled(T component) {
        return getFile(component).isPresent();
    }

    @Override
    public void execute(T component) throws Exception {
        String path = getFile(component).get().toString();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(path), null);
    }

    abstract protected Optional<File> getFile(T component);
}
