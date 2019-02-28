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
package beanzooka.swing;

import beanzooka.core.App;
import beanzooka.core.Jdk;
import beanzooka.core.Plugin;
import beanzooka.core.UserDir;
import ec.util.table.swing.JTables;
import ec.util.various.swing.FontAwesome;
import internal.swing.FileCellEditor;
import internal.swing.FilesCellEditor;
import internal.swing.PersistantFileChooser;
import internal.swing.TableColumnDescriptor;
import internal.swing.TextCellEditor;
import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Renderers {

    static final TableColumnDescriptor LABEL_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderLabel))
                    .preferedWidth(100)
                    .build();

    static final TableColumnDescriptor TEXT_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderText))
                    .cellEditor(TextCellEditor::new)
                    .build();

    static final TableColumnDescriptor FILE_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFile))
                    .cellEditor(FileCellEditor::new)
                    .preferedWidth(300)
                    .build();

    static final TableColumnDescriptor FILES_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFiles))
                    .cellEditor(FilesCellEditor::new)
                    .build();

    static final TableColumnDescriptor FOLDER_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFolder))
                    .cellEditor(FileCellEditor::new)
                    .preferedWidth(300)
                    .build();

    void renderApp(JLabel label, App value) {
        if (value != null) {
            label.setText(value.getLabel());
            label.setToolTipText(value.getFile().toString());
            setIfInvalidFile(label, value.getFile());
        } else {
            label.setToolTipText(null);
        }
    }

    void renderJdk(JLabel label, Jdk value) {
        if (value != null) {
            label.setText(value.getLabel());
            label.setToolTipText(value.getJavaHome().toString());
            setIfInvalidFolder(label, value.getJavaHome());
        } else {
            label.setToolTipText(null);
        }
    }

    void renderUserDir(JLabel label, UserDir value) {
        if (value != null) {
            label.setText(value.getLabel());
            label.setToolTipText(value.getFolder().toString());
            if (value.isClone()) {
                setIfInvalidFolder(label, value.getFolder());
            }
        } else {
            label.setToolTipText(null);
        }
    }

    void renderFile(JLabel label, File value) {
        if (value != null) {
            label.setText(value.getPath());
            label.setToolTipText(value.toString());
            setIfInvalidFile(label, value);
        } else {
            label.setToolTipText(null);
        }
    }

    void renderFiles(JLabel label, List<File> value) {
        if (value != null) {
            label.setText(Jdk.fromFiles(value));
            label.setToolTipText("<html>" + label.getText().replace(File.pathSeparator, "<br>"));
        } else {
            label.setToolTipText(null);
        }
    }

    void renderFolder(JLabel label, File value) {
        if (value != null) {
            label.setText(value.getPath());
            label.setToolTipText(value.toString());
            setIfInvalidFolder(label, value);
        } else {
            label.setToolTipText(null);
        }
    }

    void renderPlugin(JLabel label, Plugin value) {
        if (value != null) {
            label.setText(value.getLabel());
            label.setToolTipText(value.getFile().toString());
            setIfInvalidFile(label, value.getFile());
        }
    }

    void renderState(JLabel label, SwingWorker.StateValue value) {
        label.setText(value.name());
    }

    void renderLabel(JLabel label, String value) {
        label.setText(value);
        label.setToolTipText(value);
    }

    void renderText(JLabel label, String value) {
        label.setText(value);
        label.setToolTipText(value);
    }

    private void setIfInvalidFolder(JLabel label, File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            label.setIcon(FontAwesome.FA_EXCLAMATION_CIRCLE.getIcon(Color.RED, label.getFont().getSize2D() * 1.2f));
        } else {
            label.setIcon(null);
        }
    }

    private void setIfInvalidFile(JLabel label, File folder) {
        if (!folder.exists() || folder.isDirectory()) {
            label.setIcon(FontAwesome.FA_EXCLAMATION_CIRCLE.getIcon(Color.RED, label.getFont().getSize2D() * 1.2f));
        } else {
            label.setIcon(null);
        }
    }

    private File openFile(Class<?> id) {
        JFileChooser fileChooser = new PersistantFileChooser(id);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
                ? fileChooser.getSelectedFile()
                : null;
    }

    private final File EMPTY_FILE = new File("");

    private String randomLabel() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public App newApp() {
        File file = openFile(App.class);
        return file != null
                ? App.builder().label(file.getName()).file(file).build()
                : App.builder().label(randomLabel()).file(EMPTY_FILE).build();
    }

    public Jdk newJdk() {
        File file = openFile(Jdk.class);
        return file != null
                ? Jdk.builder().label(file.getName()).javaHome(file).build()
                : Jdk.builder().label(randomLabel()).javaHome(EMPTY_FILE).build();
    }

    public UserDir newUserDir() {
        File file = openFile(UserDir.class);
        return file != null
                ? UserDir.builder().label(file.getName()).folder(file).build()
                : UserDir.builder().label(randomLabel()).folder(EMPTY_FILE).build();
    }

    public Plugin newPlugin() {
        File file = openFile(Plugin.class);
        return file != null
                ? Plugin.builder().label(file.getName()).file(file).build()
                : Plugin.builder().label(randomLabel()).file(EMPTY_FILE).build();
    }
}
