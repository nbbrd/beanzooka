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

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import nbbrd.nbpl.swing.NetBeansLauncher;

/**
 *
 * @author Philippe Charles
 */
public final class PersistantFileChooser extends JFileChooser {

    private final static String LAST_USED_FILE = "lastUsedFile";

    private final Preferences prefs;

    public PersistantFileChooser(Class<?> id) {
        this.prefs = Preferences.userNodeForPackage(NetBeansLauncher.class);
        loadCurrentDir();
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        int result = super.showOpenDialog(parent);
        if (result == APPROVE_OPTION) {
            storeCurrentDir();
        }
        return result;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        int result = super.showSaveDialog(parent);
        if (result == APPROVE_OPTION) {
            storeCurrentDir();
        }
        return result;
    }

    private void loadCurrentDir() {
        String dir = prefs.get(LAST_USED_FILE, null);
        if (dir != null) {
            setCurrentDirectory(new File(dir));
        }
    }

    private void storeCurrentDir() {
        prefs.put(LAST_USED_FILE, getCurrentDirectory().toString());
    }
}
