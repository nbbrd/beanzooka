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
import ec.util.completion.FileAutoCompletionSource;
import ec.util.completion.swing.FileListCellRenderer;
import ec.util.completion.swing.JAutoCompletion;
import ec.util.table.swing.JTables;
import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.StandardSwingColor;
import ec.util.various.swing.TextPrompt;
import internal.swing.PersistantFileChooser;
import internal.swing.TableColumnDescriptor;
import internal.swing.Converter;
import internal.swing.SwingUtil;
import internal.swing.TextCellEditor;
import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Renderers {

    final TableColumnDescriptor LABEL_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderLabel))
                    .preferedWidth(100)
                    .build();

    final TableColumnDescriptor OPTIONS_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderText))
                    .cellEditor(() -> new TextCellEditor<>(Converter.identity(), newOptionsField(), Renderers::onMoreOptions))
                    .build();

    private final String OPTIONS_PROMPT = "options used by the launcher";

    private JTextField newOptionsField() {
        JTextField result = new JTextField();
        withPrompt(OPTIONS_PROMPT, result);
        return result;
    }

    private void onMoreOptions(JTextField textField) {
        JTextArea result = new JTextArea(textField.getText());
        result.setLineWrap(true);
        withPrompt(OPTIONS_PROMPT, result);
        if (SwingUtil.showOkCancelDialog(textField, new JScrollPane(result), "Options")) {
            textField.setText(result.getText());
        }
    }

    final TableColumnDescriptor FILE_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFile))
                    .cellEditor(() -> new TextCellEditor<>(Converter.of(File::getPath, File::new), newFileField(), Renderers::onMoreFile))
                    .preferedWidth(300)
                    .build();

    private JTextField newFileField() {
        JTextField result = new JTextField();
        withPrompt("file path", result);
        JAutoCompletion completion = new JAutoCompletion(result);
        completion.setSource(new FileAutoCompletionSource());
        completion.getList().setCellRenderer(new FileListCellRenderer(Executors.newSingleThreadExecutor()));
        return result;
    }

    private void onMoreFile(JTextField textField) {
        JFileChooser result = new JFileChooser();
        result.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (result.showOpenDialog(textField) == JFileChooser.APPROVE_OPTION) {
            textField.setText(result.getSelectedFile().toString());
        }
    }

    final TableColumnDescriptor CLUSTERS_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderClusters))
                    .cellEditor(() -> new TextCellEditor<>(Converter.of(Jdk::fromFiles, Jdk::toFiles), newClustersField(), null))
                    .build();

    private JTextField newClustersField() {
        JTextField result = new JTextField();
        withPrompt("cluster paths separated by '" + File.pathSeparator + "'", result);
        JAutoCompletion completion = new JAutoCompletion(result);
        completion.setSource(new FileAutoCompletionSource());
        completion.getList().setCellRenderer(new FileListCellRenderer(Executors.newSingleThreadExecutor()));
        completion.setSeparator(File.pathSeparator);
        return result;
    }

    final TableColumnDescriptor FOLDER_DESCRIPTOR
            = TableColumnDescriptor.builder()
                    .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFolder))
                    .cellEditor(() -> new TextCellEditor<>(Converter.of(File::getPath, File::new), newFolderField(), Renderers::onMoreFile))
                    .preferedWidth(300)
                    .build();

    private JTextField newFolderField() {
        JTextField result = new JTextField();
        withPrompt("folder path", result);
        JAutoCompletion completion = new JAutoCompletion(result);
        completion.setSource(new FileAutoCompletionSource());
        completion.getList().setCellRenderer(new FileListCellRenderer(Executors.newSingleThreadExecutor()));
        return result;
    }

    void renderApp(JLabel label, App value) {
        label.setIcon(null);
        label.setToolTipText(null);
        if (value != null) {
            label.setText(value.getLabel());
            label.setToolTipText(value.getFile().toString());
            setIfInvalidFile(label, value.getFile());
        }
    }

    void renderJdk(JLabel label, Jdk value) {
        label.setIcon(null);
        label.setToolTipText(null);
        if (value != null) {
            label.setText(value.getLabel());
            label.setToolTipText(value.getJavaHome().toString());
            setIfInvalidFolder(label, value.getJavaHome());
        }
    }

    void renderUserDir(JLabel label, UserDir value) {
        label.setIcon(null);
        label.setToolTipText(null);
        if (value != null) {
            label.setText(value.getLabel());
            label.setToolTipText(value.getFolder().toString());
            if (value.isClone()) {
                setIfInvalidFolder(label, value.getFolder());
            }
        }
    }

    void renderFile(JLabel label, File value) {
        label.setIcon(null);
        label.setToolTipText(null);
        if (value != null) {
            label.setText(value.getPath());
            label.setToolTipText(value.toString());
            setIfInvalidFile(label, value);
        }
    }

    void renderClusters(JLabel label, List<File> value) {
        label.setIcon(null);
        label.setToolTipText(null);
        if (value != null) {
            label.setText(Jdk.fromFiles(value));
            label.setToolTipText("<html>" + label.getText().replace(File.pathSeparator, "<br>"));
        }
    }

    void renderFolder(JLabel label, File value) {
        label.setIcon(null);
        label.setToolTipText(null);
        if (value != null) {
            label.setText(value.getPath());
            label.setToolTipText(value.toString());
            setIfInvalidFolder(label, value);
        }
    }

    void renderPlugin(JLabel label, Plugin value) {
        label.setIcon(null);
        label.setToolTipText(null);
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
        if (!folder.exists()) {
            label.setIcon(getErrorIcon(label));
            label.setToolTipText("Error: folder doesn't exist");
        } else if (!folder.isDirectory()) {
            label.setIcon(getErrorIcon(label));
            label.setToolTipText("Error: not a folder");
        }
    }

    private void setIfInvalidFile(JLabel label, File folder) {
        if (!folder.exists()) {
            label.setIcon(getErrorIcon(label));
            label.setToolTipText("Error: file doesn't exist");
        } else if (folder.isDirectory()) {
            label.setIcon(getErrorIcon(label));
            label.setToolTipText("Error: not a file");
        }
    }

    private Icon getErrorIcon(JLabel label) {
        return FontAwesome.FA_EXCLAMATION_CIRCLE.getIcon(Color.RED, label.getFont().getSize2D() * 1.2f);
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

    private void withPrompt(String text, JTextComponent component) {
        TextPrompt prompt = new TextPrompt(text, component);
        StandardSwingColor.TEXT_FIELD_INACTIVE_FOREGROUND.lookup().ifPresent(prompt::setForeground);
        prompt.setVerticalAlignment(JLabel.CENTER);
        prompt.setHorizontalAlignment(JLabel.CENTER);
    }
}
