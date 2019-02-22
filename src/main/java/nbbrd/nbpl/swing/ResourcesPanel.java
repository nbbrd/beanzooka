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
package nbbrd.nbpl.swing;

import internal.swing.SwingUtil;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import internal.swing.ListTableEdition;
import internal.swing.EventShield;
import internal.swing.ShowInFolderCommand;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import nbbrd.nbpl.core.App;
import nbbrd.nbpl.core.Jdk;
import nbbrd.nbpl.core.Configuration;
import nbbrd.nbpl.core.Plugin;
import nbbrd.nbpl.core.Resources;
import nbbrd.nbpl.core.UserDir;

/**
 *
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

    private final EventShield shield;
    private Resources resources;
    private Configuration configuration;

    public ResourcesPanel() {
        this.shield = new EventShield();
        initComponents();
        initComponents2();
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        firePropertyChange(RESOURCES_PROPERTY, this.resources, this.resources = resources);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        firePropertyChange(CONFIGURATION_PROPERTY, this.configuration, this.configuration = configuration);
    }

    private void initComponents2() {
        OpenPluginLocation openPlugin = new OpenPluginLocation();
        SwingUtil.onDoubleClick(plugins, openPlugin);
        getActionMap().put(OPEN_PLUGIN_ACTION, openPlugin.toAction(plugins));
        getActionMap().put(EDIT_APPS_ACTION, APPS.toAction(ListTableEdition.Bridge.comboBox(), apps));
        getActionMap().put(EDIT_JDKS_ACTION, JDKS.toAction(ListTableEdition.Bridge.comboBox(), jdks));
        getActionMap().put(EDIT_USER_DIRS_ACTION, USER_DIRS.toAction(ListTableEdition.Bridge.comboBox(), userDirs));
        getActionMap().put(EDIT_PLUGINS_ACTION, PLUGINS.toAction(ListTableEdition.Bridge.list(), plugins));

        apps.setRenderer(JLists.cellRendererOf(Renderers::renderApp));
        apps.addItemListener(shield.wrap(this::onAppsChange));
        apps.setComponentPopupMenu(getPopupMenu(EDIT_APPS_ACTION));

        jdks.setRenderer(JLists.cellRendererOf(Renderers::renderJdk));
        jdks.addItemListener(shield.wrap(this::onJdkChange));
        jdks.setComponentPopupMenu(getPopupMenu(EDIT_JDKS_ACTION));

        userDirs.setRenderer(JLists.cellRendererOf(Renderers::renderUserDir));
        userDirs.addItemListener(shield.wrap(this::onUserDirsChange));
        userDirs.setComponentPopupMenu(getPopupMenu(EDIT_USER_DIRS_ACTION));

        plugins.setCellRenderer(JLists.cellRendererOf(Renderers::renderPlugin));
        plugins.addListSelectionListener(shield.wrap(this::onPluginsChange));
        plugins.setComponentPopupMenu(getPopupMenu(EDIT_PLUGINS_ACTION, OPEN_PLUGIN_ACTION));

        addPropertyChangeListener(RESOURCES_PROPERTY, shield.wrap(this::onResourcesChange));
        addPropertyChangeListener(CONFIGURATION_PROPERTY, shield.wrap(this::onConfigurationChange));
    }

    private JPopupMenu getPopupMenu(String... actionKeys) {
        JMenu result = new JMenu();
        Stream.of(actionKeys)
                .map(getActionMap()::get)
                .forEach(result::add);
        return result.getPopupMenu();
    }

    private void onAppsChange(ItemEvent event) {
        updateConfiguration();
    }

    private void onJdkChange(ItemEvent event) {
        updateConfiguration();
    }

    private void onUserDirsChange(ItemEvent event) {
        updateConfiguration();
    }

    private void onPluginsChange(ListSelectionEvent event) {
        updateConfiguration();
    }

    private void onResourcesChange(PropertyChangeEvent event) {
        if (resources != null) {
            apps.setModel(SwingUtil.modelOf(resources.getApps()));
            jdks.setModel(SwingUtil.modelOf(resources.getJdks()));
            userDirs.setModel(SwingUtil.modelOf(SwingUtil.concat(UserDir.TEMP, resources.getUserDirs())));
            plugins.setModel(SwingUtil.modelOf(resources.getPlugins()));
        } else {
            apps.setModel(new DefaultComboBoxModel());
            jdks.setModel(new DefaultComboBoxModel());
            userDirs.setModel(new DefaultComboBoxModel());
            plugins.setModel(new DefaultComboBoxModel());
        }
        updateConfiguration();
    }

    private void onConfigurationChange(PropertyChangeEvent event) {
        if (configuration != null) {
            apps.setSelectedItem(configuration.getApp());
            jdks.setSelectedItem(configuration.getJdk());
            userDirs.setSelectedItem(configuration.getUserDir());
//        plugins.setSelectedIndices(job.getPlugins().stream().mapToInt(plugins.get));
        } else {
            apps.setSelectedItem(null);
            jdks.setSelectedItem(null);
            userDirs.setSelectedItem(null);
            plugins.setSelectedIndex(-1);
        }
    }

    private void updateConfiguration() {
        if (apps.getSelectedIndex() != -1
                && jdks.getSelectedIndex() != -1
                && userDirs.getSelectedIndex() != -1) {
            setConfiguration(Configuration
                    .builder()
                    .app((App) apps.getSelectedItem())
                    .jdk((Jdk) jdks.getSelectedItem())
                    .userDir((UserDir) userDirs.getSelectedItem())
                    .plugins(plugins.getSelectedValuesList())
                    .build());
        } else {
            setConfiguration(null);
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
        public JCommand.ActionAdapter toAction(JList<Plugin> c) {
            ActionAdapter result = super.toAction(c).withWeakListSelectionListener(c.getSelectionModel());
            result.putValue(Action.NAME, "Open plugin location");
            return result;
        }
    }

    private static final ListTableEdition<App> APPS
            = ListTableEdition.<App>builder()
                    .name("Edit applications")
                    .valueFactory(Renderers::newApp)
                    .column("Label", String.class, App::getLabel, App::withLabel, Renderers.LABEL_DESCRIPTOR)
                    .column("File", File.class, App::getFile, App::withFile, Renderers.FILE_DESCRIPTOR)
                    .build();

    private static final ListTableEdition<Jdk> JDKS
            = ListTableEdition.<Jdk>builder()
                    .name("Edit JDKs")
                    .valueFactory(Renderers::newJdk)
                    .column("Label", String.class, Jdk::getLabel, Jdk::withLabel, Renderers.LABEL_DESCRIPTOR)
                    .column("Java home", File.class, Jdk::getJavaHome, Jdk::withJavaHome, Renderers.FOLDER_DESCRIPTOR)
                    .column("Clusters", String.class, Jdk::getClusters, Jdk::withClusters, Renderers.TEXT_DESCRIPTOR)
                    .column("Options", String.class, Jdk::getOptions, Jdk::withOptions, Renderers.TEXT_DESCRIPTOR)
                    .build();

    private static final ListTableEdition<UserDir> USER_DIRS
            = ListTableEdition.<UserDir>builder()
                    .name("Edit userdirs")
                    .valueFactory(Renderers::newUserDir)
                    .column("Label", String.class, UserDir::getLabel, UserDir::withLabel, Renderers.LABEL_DESCRIPTOR)
                    .column("Folder", File.class, UserDir::getFolder, UserDir::withFolder, Renderers.FOLDER_DESCRIPTOR)
                    .build();

    private static final ListTableEdition<Plugin> PLUGINS
            = ListTableEdition.<Plugin>builder()
                    .name("Edit plugins")
                    .valueFactory(Renderers::newPlugin)
                    .column("Label", String.class, Plugin::getLabel, Plugin::withLabel, Renderers.LABEL_DESCRIPTOR)
                    .column("File", File.class, Plugin::getFile, Plugin::withFile, Renderers.FILE_DESCRIPTOR)
                    .build();

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        userDirs = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        plugins = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        apps = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jdks = new javax.swing.JComboBox<>();

        jLabel4.setText("User dir:");

        jScrollPane1.setViewportView(plugins);

        jLabel1.setText("Application:");

        jLabel5.setText("Plugins:");

        jLabel2.setText("JDK:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(apps, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jdks, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userDirs, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
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
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userDirs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<App> apps;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<nbbrd.nbpl.core.Jdk> jdks;
    private javax.swing.JList<Plugin> plugins;
    private javax.swing.JComboBox<UserDir> userDirs;
    // End of variables declaration//GEN-END:variables
}
