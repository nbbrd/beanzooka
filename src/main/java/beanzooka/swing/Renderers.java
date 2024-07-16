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
import ec.util.desktop.Desktop;
import ec.util.desktop.DesktopManager;
import ec.util.table.swing.JTables;
import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.StandardSwingColor;
import ec.util.various.swing.TextPrompt;
import internal.swing.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
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
            .cellEditor(() -> TextCellEditor.of(o -> o, o -> o, newOptionsField(), Renderers::onMoreOptions))
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

    private final FileFilter PLUGIN_FILTER = new FileNameExtensionFilter("NetBeans plugin", "nbm");

    final TableColumnDescriptor FILE_DESCRIPTOR
            = TableColumnDescriptor.builder()
            .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFile))
            .cellEditor(() -> TextCellEditor.of(File::getPath, File::new, newFileField(null), o -> Renderers.onMoreFile(o, null)))
            .preferedWidth(300)
            .build();

    final TableColumnDescriptor PLUGIN_DESCRIPTOR
            = TableColumnDescriptor.builder()
            .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFile))
            .cellEditor(() -> TextCellEditor.of(File::getPath, File::new, newFileField(PLUGIN_FILTER::accept), o -> Renderers.onMoreFile(o, PLUGIN_FILTER)))
            .preferedWidth(300)
            .build();

    private JTextField newFileField(java.io.FileFilter optionalFileFilter) {
        JTextField result = new JTextField();
        withPrompt("file path", result);
        JAutoCompletion completion = new JAutoCompletion(result);
        completion.setSource(new FileAutoCompletionSource(false, optionalFileFilter, new File[0]));
        completion.getList().setCellRenderer(new FileListCellRenderer(Executors.newSingleThreadExecutor()));
        return result;
    }

    private void onMoreFile(JTextField textField, FileFilter optionalFileFilter) {
        JFileChooser result = new JFileChooser();
        if (optionalFileFilter != null) {
            result.setFileFilter(optionalFileFilter);
        }
        result.setFileSelectionMode(JFileChooser.FILES_ONLY);
        result.setSelectedFile(new File(textField.getText()));
        JFileChoosers.getOpenFile(result, textField).map(File::getPath).ifPresent(textField::setText);
    }

    final TableColumnDescriptor CLUSTERS_DESCRIPTOR
            = TableColumnDescriptor.builder()
            .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderClusters))
            .cellEditor(() -> TextCellEditor.of(Jdk::fromFiles, Jdk::toFiles, newClustersField(), Renderers::onMoreClusters))
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

    private void onMoreClusters(JTextField textField) {
        ListTableDescriptor<File> listTable = ListTableDescriptor
                .builder(Renderers::newCluster)
                .column("File", File.class, o -> o, (x, y) -> y, FOLDER_DESCRIPTOR)
                .build();
        ListTableEdition.ofText("Edit clusters", listTable, Jdk::fromFiles, Jdk::toFiles).edit(textField);
    }

    final TableColumnDescriptor FOLDER_DESCRIPTOR
            = TableColumnDescriptor.builder()
            .cellRenderer(() -> JTables.cellRendererOf(Renderers::renderFolder))
            .cellEditor(() -> TextCellEditor.of(File::getPath, File::new, newFolderField(), Renderers::onMoreFolder))
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

    private void onMoreFolder(JTextField textField) {
        JFileChooser result = new JFileChooser();
        result.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        result.setSelectedFile(new File(textField.getText()));
        JFileChoosers.getOpenFile(result, textField).map(File::getPath).ifPresent(textField::setText);
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

    private File open(Class<?> id, int fileSelectionMode, FileFilter optionalFileFilter) {
        JFileChooser fileChooser = new JFileChooser();
        if (optionalFileFilter != null) {
            fileChooser.setFileFilter(optionalFileFilter);
        }
        JFileChoosers.autoPersist(fileChooser, Preferences.userNodeForPackage(id).node(id.getSimpleName()));
        fileChooser.setFileSelectionMode(fileSelectionMode);
        return JFileChoosers.getOpenFile(fileChooser, null).orElse(null);
    }

    private final File EMPTY_FILE = new File("");

    private String randomLabel() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public App newApp() {
        File file = open(App.class, JFileChooser.FILES_ONLY, null);
        return file != null
                ? App.builder().label(file.getName()).file(file).build()
                : App.builder().label(randomLabel()).file(EMPTY_FILE).build();
    }

    public void fillApp(List<App> list) {
        App.ofDesktopSearch(Renderers::search).forEach(e -> appendAppIfAbsent(list, e));
    }

    private void appendAppIfAbsent(List<App> list, App item) {
        if (item != null && list.stream().noneMatch(o -> o.getFile().equals(item.getFile()))) {
            list.add(item);
        }
    }

    public Jdk newJdk() {
        File folder = open(Jdk.class, JFileChooser.DIRECTORIES_ONLY, null);
        return folder != null
                ? Jdk.builder().label(folder.getName()).javaHome(folder).build()
                : Jdk.builder().label(randomLabel()).javaHome(EMPTY_FILE).build();
    }

    public void fillJdk(List<Jdk> list) {
        Jdk.ofSystemProperty().ifPresent(e -> appendJdkIfAbsent(list, e));
        Jdk.ofEnvironmentVariable().ifPresent(e -> appendJdkIfAbsent(list, e));
        Jdk.ofDesktopSearch(Renderers::search).forEach(e -> appendJdkIfAbsent(list, e));
    }

    private void appendJdkIfAbsent(List<Jdk> list, Jdk item) {
        if (item != null && list.stream().noneMatch(o -> o.getJavaHome().equals(item.getJavaHome()))) {
            list.add(item);
        }
    }

    public UserDir newUserDir() {
        File folder = open(UserDir.class, JFileChooser.DIRECTORIES_ONLY, null);
        return folder != null
                ? UserDir.builder().label(folder.getName()).folder(folder).build()
                : UserDir.builder().label(randomLabel()).folder(EMPTY_FILE).build();
    }

    public Plugin newPlugin() {
        File file = open(Plugin.class, JFileChooser.FILES_ONLY, PLUGIN_FILTER);
        return file != null
                ? Plugin.builder().label(file.getName()).file(file).build()
                : Plugin.builder().label(randomLabel()).file(EMPTY_FILE).build();
    }

    public void fillPlugin(List<Plugin> list) {
        Plugin.ofDesktopSearch(Renderers::search).forEach(e -> appendPluginIfAbsent(list, e));
    }

    private void appendPluginIfAbsent(List<Plugin> list, Plugin item) {
        if (item != null && list.stream().noneMatch(o -> o.getFile().equals(item.getFile()))) {
            list.add(item);
        }
    }

    public File newCluster() {
        File folder = open(ClusterFile.class, JFileChooser.DIRECTORIES_ONLY, null);
        return folder != null ? folder : EMPTY_FILE;
    }

    private void withPrompt(String text, JTextComponent component) {
        TextPrompt prompt = new TextPrompt(text, component);
        StandardSwingColor.TEXT_FIELD_INACTIVE_FOREGROUND.lookup().ifPresent(prompt::setForeground);
        prompt.setVerticalAlignment(JLabel.CENTER);
        prompt.setHorizontalAlignment(JLabel.CENTER);
    }

    private static final class ClusterFile {
    }

    private File[] search(String query) {
        Desktop desktop = DesktopManager.get();
        if (desktop.isSupported(Desktop.Action.SEARCH)) {
            try {
                return desktop.search(query);
            } catch (IOException ignore) {
            }
        }
        return new File[0];
    }
}
