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
import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class JFileChoosers {

    private final String IS_CLOSING_PROPERTY = "JFileChooserDialogIsClosingProperty";

    public void autoPersistUserNodeForClass(@NonNull JFileChooser fileChooser, @NonNull Class<?> type) {
        autoPersist(fileChooser, Preferences.userNodeForPackage(type).node(type.getSimpleName()));
    }

    public void autoPersist(@NonNull JFileChooser fileChooser, @NonNull Preferences prefs) {
        Objects.requireNonNull(fileChooser);
        Objects.requireNonNull(prefs);
        loadCurrentDir(fileChooser, prefs);
        fileChooser.addPropertyChangeListener(IS_CLOSING_PROPERTY, event -> storeCurrentDir(fileChooser, prefs));
    }

    public File getSelectedFileWithExtension(@NonNull JFileChooser fileChooser) {
        File file = fileChooser.getSelectedFile();
        if (file != null) {
            FileFilter filter = fileChooser.getFileFilter();
            if (filter instanceof FileNameExtensionFilter) {
                String[] exts = ((FileNameExtensionFilter) filter).getExtensions();
                if (exts.length > 0 && !anyMatch(file, exts)) {
                    return Paths.get(file.getPath() + "." + exts[0]).toFile();
                }
            }
        }
        return file;
    }

    public boolean canOverride(@NonNull File file, @Nullable Component parent) {
        return !file.exists() || JOptionPane.showConfirmDialog(parent, "File exists already. Delete it anyway?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public Optional<File> getOpenFile(@NonNull JFileChooser fileChooser, @Nullable Component parent) {
        return JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(parent)
                ? Optional.of(fileChooser.getSelectedFile())
                : Optional.empty();
    }

    public Optional<File> getSaveFile(@NonNull JFileChooser fileChooser, @Nullable Component parent) {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(parent)) {
            File result = getSelectedFileWithExtension(fileChooser);
            if (canOverride(result, parent)) {
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private final static String CURRENT_DIRECTORY_KEY = "currentDirectory";

    private void loadCurrentDir(JFileChooser fileChooser, Preferences prefs) {
        Optional.ofNullable(prefs.get(CURRENT_DIRECTORY_KEY, null))
                .map(File::new)
                .ifPresent(fileChooser::setCurrentDirectory);
    }

    private void storeCurrentDir(JFileChooser fileChooser, Preferences prefs) {
        prefs.put(CURRENT_DIRECTORY_KEY, fileChooser.getCurrentDirectory().toString());
    }

    private boolean anyMatch(File file, String[] exts) {
        String normalizedFile = file.getName().toLowerCase(Locale.ROOT);
        return Stream.of(exts)
                .map(ext -> "." + ext.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedFile::endsWith);
    }
}
