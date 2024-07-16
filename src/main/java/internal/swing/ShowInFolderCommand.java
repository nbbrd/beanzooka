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

import ec.util.desktop.Desktop;
import ec.util.desktop.DesktopManager;
import ec.util.various.swing.JCommand;
import lombok.NonNull;

import java.io.File;
import java.util.Optional;

/**
 *
 * @author Philippe Charles
 */
public abstract class ShowInFolderCommand<T> extends JCommand<T> {

    private final Desktop desktop = DesktopManager.get();

    @Override
    public boolean isEnabled(@NonNull T component) {
        return desktop.isSupported(Desktop.Action.SHOW_IN_FOLDER)
                && getFile(component).filter(File::exists).isPresent();
    }

    @Override
    public void execute(@NonNull T component) throws Exception {
        desktop.showInFolder(getFile(component).orElseThrow(RuntimeException::new));
    }

    abstract protected Optional<File> getFile(T component);
}
