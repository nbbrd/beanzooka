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

import beanzooka.core.*;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import internal.swing.*;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public final class ResourcesPanel extends javax.swing.JPanel {

    public static final String RESOURCES_PROPERTY = "resources";
    public static final String CONFIGURATION_PROPERTY = "configuration";

    public static final String OPEN_PLUGIN_ACTION = "openPlugin";
    public static final String EDIT_APPS_ACTION = "editApps";
    public static final String EDIT_JDKS_ACTION = "editJdks";
    public static final String EDIT_USER_DIRS_ACTION = "editUserDirs";
    public static final String EDIT_PLUGINS_ACTION = "editPlugins";
    public static final String COPY_PATH_APPS_ACTION = "copyPathApps";
    public static final String COPY_PATH_JDKS_ACTION = "copyPathJdks";
    public static final String COPY_PATH_USER_DIRS_ACTION = "copyPathUserDirs";
    public static final String COPY_PATH_PLUGINS_ACTION = "copyPathPlugins";
    public static final String FILL_ACTION = "fill";

    private final EventShield shield;
    private Resources resources;
    private Optional<Configuration> configuration;

    public ResourcesPanel() {
        this.shield = new EventShield();
        this.resources = Resources.builder().build();
        this.configuration = Optional.empty();
        initComponents();
        initComponents2();
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        Objects.requireNonNull(resources);
        firePropertyChange(RESOURCES_PROPERTY, this.resources, this.resources = resources);
    }

    public Optional<Configuration> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Optional<Configuration> configuration) {
        Objects.requireNonNull(configuration);
        firePropertyChange(CONFIGURATION_PROPERTY, this.configuration, this.configuration = configuration);
    }

    private void initComponents2() {
        OpenPluginLocation openPlugin = new OpenPluginLocation();
        SwingUtil.onDoubleClick(plugins, openPlugin);

        getActionMap().put(OPEN_PLUGIN_ACTION, openPlugin.toAction(plugins));

        getActionMap().put(EDIT_APPS_ACTION, ListTableEdition.ofComboBox("Edit applications", APPS).toAction(apps));
        getActionMap().put(EDIT_JDKS_ACTION, ListTableEdition.ofComboBox("Edit JDKs", JDKS).toAction(jdks));
        getActionMap().put(EDIT_USER_DIRS_ACTION, ListTableEdition.ofComboBox("Edit user dirs", USER_DIRS).toAction(userDirs));
        getActionMap().put(EDIT_PLUGINS_ACTION, ListTableEdition.ofList("Edit plugins", PLUGINS).toAction(plugins));

        getActionMap().put(COPY_PATH_APPS_ACTION, new ComboCopyPath<>(App::getFile).toAction(apps));
        getActionMap().put(COPY_PATH_JDKS_ACTION, new ComboCopyPath<>(Jdk::getJavaHome).toAction(jdks));
        getActionMap().put(COPY_PATH_USER_DIRS_ACTION, new ComboCopyPath<>(UserDir::getFolder).toAction(userDirs));
        getActionMap().put(COPY_PATH_PLUGINS_ACTION, new ListCopyPath<>(Plugin::getFile).toAction(plugins));

        getActionMap().put(FILL_ACTION, new Fill().toAction(this));

        apps.setRenderer(JLists.cellRendererOf(Renderers::renderApp));
        apps.addItemListener(shield.wrap(this::onAppsSelectionChange));
        apps.setComponentPopupMenu(getPopupMenu(EDIT_APPS_ACTION, COPY_PATH_APPS_ACTION));
        SwingUtil.addListDataListener(apps, SwingUtil.listDataListenerOf(this::onAppsDataChange));

        jdks.setRenderer(JLists.cellRendererOf(Renderers::renderJdk));
        jdks.addItemListener(shield.wrap(this::onJdksSelectionChange));
        jdks.setComponentPopupMenu(getPopupMenu(EDIT_JDKS_ACTION, COPY_PATH_JDKS_ACTION));
        SwingUtil.addListDataListener(jdks, SwingUtil.listDataListenerOf(this::onJdksDataChange));

        userDirs.setRenderer(JLists.cellRendererOf(Renderers::renderUserDir));
        userDirs.addItemListener(shield.wrap(this::onUserDirsSelectionChange));
        userDirs.setComponentPopupMenu(getPopupMenu(EDIT_USER_DIRS_ACTION, COPY_PATH_USER_DIRS_ACTION));
        SwingUtil.addListDataListener(userDirs, SwingUtil.listDataListenerOf(this::onUserDirsDataChange));

        plugins.setCellRenderer(JLists.cellRendererOf(Renderers::renderPlugin));
        plugins.addListSelectionListener(shield.wrap(this::onPluginsSelectionChange));
        plugins.setComponentPopupMenu(getPopupMenu(EDIT_PLUGINS_ACTION, OPEN_PLUGIN_ACTION, COPY_PATH_PLUGINS_ACTION));
        SwingUtil.addListDataListener(plugins, SwingUtil.listDataListenerOf(this::onPluginsDataChange));

        tempUserDir.addPropertyChangeListener("BUTTON.BP_CHECKBOX", event -> updateConfiguration());

        addPropertyChangeListener(RESOURCES_PROPERTY, shield.wrap(this::onResourcesChange));
        addPropertyChangeListener(CONFIGURATION_PROPERTY, shield.wrap(this::onConfigurationChange));
        addPropertyChangeListener("enabled", shield.wrap(this::onEnabledChange));
    }

    private JPopupMenu getPopupMenu(String... actionKeys) {
        JMenu result = new JMenu();
        Stream.of(actionKeys)
                .map(getActionMap()::get)
                .forEach(result::add);
        return result.getPopupMenu();
    }

    private void onAppsSelectionChange(ItemEvent event) {
        updateConfiguration();
    }

    private void onAppsDataChange(ListDataEvent event) {
        setResources(getResources().withApps(SwingUtil.listOf(apps.getModel())));
    }

    private void onJdksSelectionChange(ItemEvent event) {
        updateConfiguration();
    }

    private void onJdksDataChange(ListDataEvent event) {
        setResources(getResources().withJdks(SwingUtil.listOf(jdks.getModel())));
    }

    private void onUserDirsSelectionChange(ItemEvent event) {
        updateConfiguration();
    }

    private void onUserDirsDataChange(ListDataEvent event) {
        setResources(getResources().withUserDirs(SwingUtil.listOf(userDirs.getModel())));
    }

    private void onPluginsSelectionChange(ListSelectionEvent event) {
        updateConfiguration();
    }

    private void onPluginsDataChange(ListDataEvent event) {
        setResources(getResources().withPlugins(SwingUtil.listOf(plugins.getModel())));
    }

    private void onResourcesChange(PropertyChangeEvent event) {
        if (resources != null) {
            apps.setModel(SwingUtil.modelOf(resources.getApps()));
            jdks.setModel(SwingUtil.modelOf(resources.getJdks()));
            userDirs.setModel(SwingUtil.modelOf(resources.getUserDirs()));
            plugins.setModel(SwingUtil.modelOf(resources.getPlugins()));
        } else {
            apps.setModel(new DefaultComboBoxModel<>());
            jdks.setModel(new DefaultComboBoxModel<>());
            userDirs.setModel(new DefaultComboBoxModel<>());
            plugins.setModel(new DefaultComboBoxModel<>());
        }
        updateConfiguration();
    }

    private void onConfigurationChange(PropertyChangeEvent event) {
        if (configuration.isPresent()) {
            apps.setSelectedItem(configuration.get().getApp());
            jdks.setSelectedItem(configuration.get().getJdk());
            userDirs.setSelectedItem(configuration.get().getUserDir());
//        plugins.setSelectedIndices(job.getPlugins().stream().mapToInt(plugins.get));
        }
    }

    private void onEnabledChange(PropertyChangeEvent event) {
        boolean enabled = (boolean) event.getNewValue();
        jdks.setEnabled(enabled);
        apps.setEnabled(enabled);
        userDirs.setEnabled(enabled);
        tempUserDir.setEnabled(enabled);
        plugins.setEnabled(enabled);
    }

    private void updateConfiguration() {
        if (apps.getSelectedIndex() != -1
                && jdks.getSelectedIndex() != -1
                && (tempUserDir.isSelected() || userDirs.getSelectedIndex() != -1)) {
            setConfiguration(Optional.of(
                    Configuration
                            .builder()
                            .app((App) apps.getSelectedItem())
                            .jdk((Jdk) jdks.getSelectedItem())
                            .userDir(tempUserDir.isSelected() ? Optional.empty() : Optional.ofNullable((UserDir) userDirs.getSelectedItem()))
                            .plugins(plugins.getSelectedValuesList())
                            .build())
            );
        } else {
            setConfiguration(Optional.empty());
        }
    }

    private static final class OpenPluginLocation extends ShowInFolderCommand<JList<Plugin>> {

        @Override
        protected Optional<File> getFile(JList<Plugin> component) {
            return JLists.isSingleSelectionIndex(component.getSelectionModel())
                    ? Optional.of(component.getSelectedValue().getFile())
                    : Optional.empty();
        }

        @Override
        public JCommand.ActionAdapter toAction(@NonNull JList<Plugin> c) {
            ActionAdapter result = super.toAction(c).withWeakListSelectionListener(c.getSelectionModel());
            result.putValue(Action.NAME, "Open plugin location");
            return result;
        }
    }

    @lombok.AllArgsConstructor
    private static final class ComboCopyPath<T> extends CopyPathCommand<JComboBox<T>> {

        private final Function<T, File> toFile;

        @Override
        protected Optional<File> getFile(JComboBox<T> component) {
            return component.getSelectedIndex() != -1
                    ? Optional.ofNullable(toFile.apply((T) component.getSelectedItem()))
                    : Optional.empty();
        }

        @Override
        public JCommand.ActionAdapter toAction(@NonNull JComboBox<T> c) {
            ActionAdapter result = super.toAction(c);
            SwingUtil.addListDataListener(c, SwingUtil.listDataListenerOf(o -> result.refreshActionState()));
            result.putValue(Action.NAME, "Copy path");
            return result;
        }
    }

    @lombok.AllArgsConstructor
    private static final class ListCopyPath<T> extends CopyPathCommand<JList<T>> {

        private final Function<T, File> toFile;

        @Override
        protected Optional<File> getFile(JList<T> component) {
            return JLists.isSingleSelectionIndex(component.getSelectionModel())
                    ? Optional.ofNullable(toFile.apply(component.getSelectedValue()))
                    : Optional.empty();
        }

        @Override
        public JCommand.ActionAdapter toAction(@NonNull JList<T> c) {
            ActionAdapter result = super.toAction(c).withWeakListSelectionListener(c.getSelectionModel());
            result.putValue(Action.NAME, "Copy path");
            return result;
        }
    }

    private static final class Fill extends JCommand<ResourcesPanel> {

        @Override
        public void execute(@NonNull ResourcesPanel c) {
            c.setEnabled(false);
            Resources previous = c.getResources();
            new SwingWorker<Resources, Void>() {
                @Override
                protected Resources doInBackground() {
                    ExecutorService executor = Executors.newCachedThreadPool();
                    try {
                        Future<List<Jdk>> jdks = executor.submit(() -> fillList(JDKS, previous.getJdks()));
                        Future<List<App>> apps = executor.submit(() -> fillList(APPS, previous.getApps()));
                        Future<List<UserDir>> userDirs = executor.submit(() -> fillList(USER_DIRS, previous.getUserDirs()));
                        Future<List<Plugin>> plugins = executor.submit(() -> fillList(PLUGINS, previous.getPlugins()));
                        return Resources
                                .builder()
                                .jdks(jdks.get())
                                .apps(apps.get())
                                .userDirs(userDirs.get())
                                .plugins(plugins.get())
                                .build();
                    } catch (ExecutionException | InterruptedException ex) {
                        ex.printStackTrace();
                        return previous;
                    } finally {
                        executor.shutdown();
                    }
                }

                @Override
                protected void done() {
                    try {
                        c.setResources(get());
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                    }
                    c.setEnabled(true);
                }
            }.execute();
        }

        @MightBePromoted
        private static <T> List<T> fillList(ListTableDescriptor<T> descriptor, List<T> list) {
            if (descriptor.isEnableFiller()) {
                List<T> result = new ArrayList<>(list);
                descriptor.getValueFiller().accept(result);
                return result;
            }
            return list;
        }
    }

    private static final ListTableDescriptor<App> APPS
            = ListTableDescriptor
            .builder(Renderers::newApp)
            .valueFiller(Renderers::fillApp)
            .enableFiller(true)
            .column("Label", String.class, App::getLabel, App::withLabel, Renderers.LABEL_DESCRIPTOR)
            .column("File", File.class, App::getFile, App::withFile, Renderers.FILE_DESCRIPTOR)
            .build();

    private static final ListTableDescriptor<Jdk> JDKS
            = ListTableDescriptor
            .builder(Renderers::newJdk)
            .valueFiller(Renderers::fillJdk)
            .enableFiller(true)
            .column("Label", String.class, Jdk::getLabel, Jdk::withLabel, Renderers.LABEL_DESCRIPTOR)
            .column("Java home", File.class, Jdk::getJavaHome, Jdk::withJavaHome, Renderers.FOLDER_DESCRIPTOR)
            .column("Options", String.class, Jdk::getOptions, Jdk::withOptions, Renderers.OPTIONS_DESCRIPTOR)
            .column("Clusters", List.class, Jdk::getClusters, Jdk::withClusters, Renderers.CLUSTERS_DESCRIPTOR)
            .build();

    private static final ListTableDescriptor<UserDir> USER_DIRS
            = ListTableDescriptor
            .builder(Renderers::newUserDir)
            .column("Label", String.class, UserDir::getLabel, UserDir::withLabel, Renderers.LABEL_DESCRIPTOR)
            .column("Folder", File.class, UserDir::getFolder, UserDir::withFolder, Renderers.FOLDER_DESCRIPTOR)
            .column("Clone", Boolean.class, UserDir::isClone, UserDir::withClone, TableColumnDescriptor.EMPTY)
            .build();

    private static final ListTableDescriptor<Plugin> PLUGINS
            = ListTableDescriptor
            .builder(Renderers::newPlugin)
            .valueFiller(Renderers::fillPlugin)
            .enableFiller(true)
            .column("Label", String.class, Plugin::getLabel, Plugin::withLabel, Renderers.LABEL_DESCRIPTOR)
            .column("File", File.class, Plugin::getFile, Plugin::withFile, Renderers.PLUGIN_DESCRIPTOR)
            .build();

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        userDirs = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        plugins = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        apps = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jdks = new javax.swing.JComboBox<>();
        tempUserDir = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();

        jScrollPane1.setViewportView(plugins);

        jLabel1.setText("Application:");

        jLabel5.setText("Plugins:");

        jLabel2.setText("JDK:");

        tempUserDir.setSelected(true);
        tempUserDir.setText("temp");
        tempUserDir.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        tempUserDir.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel3.setText("User dir:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel2)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 175, Short.MAX_VALUE)
                                                .addComponent(tempUserDir))
                                        .addComponent(userDirs, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jdks, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(apps, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(apps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jdks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel3)
                                                        .addComponent(tempUserDir))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(userDirs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<App> apps;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<beanzooka.core.Jdk> jdks;
    private javax.swing.JList<Plugin> plugins;
    private javax.swing.JCheckBox tempUserDir;
    private javax.swing.JComboBox<UserDir> userDirs;
    // End of variables declaration//GEN-END:variables
}
