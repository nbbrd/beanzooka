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

import ec.util.desktop.Desktop;
import ec.util.desktop.DesktopManager;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JMenu;
import nbbrd.nbpl.core.App;
import nbbrd.nbpl.core.Config;
import nbbrd.nbpl.core.Job;
import nbbrd.nbpl.core.Plugin;
import nbbrd.nbpl.core.Settings;
import nbbrd.nbpl.core.UserDir;

/**
 *
 * @author Philippe Charles
 */
public final class SettingsPanel extends javax.swing.JPanel {

    public static final String SETTINGS_PROPERTY = "settings";
    public static final String JOB_PROPERTY = "job";

    private final OpenPluginLocation openPlugin;

    private Settings settings;
    private Job job;

    public SettingsPanel() {
        initComponents();

        this.openPlugin = new OpenPluginLocation();

        apps.setRenderer(JLists.cellRendererOf(Renderers::renderApp));
        apps.addItemListener(o -> computeJob());

        configs.setRenderer(JLists.cellRendererOf(Renderers::renderConfig));
        configs.addItemListener(o -> computeJob());

        userDirs.setRenderer(JLists.cellRendererOf(Renderers::renderUserDir));
        userDirs.addItemListener(o -> computeJob());

        plugins.setCellRenderer(JLists.cellRendererOf(Renderers::renderPlugin));
        plugins.setComponentPopupMenu(getPluginsMenu().getPopupMenu());
        plugins.addListSelectionListener(o -> computeJob());
        SwingUtil.onDoubleClick(plugins, openPlugin);

        addPropertyChangeListener(o -> {
            switch (o.getPropertyName()) {
                case SETTINGS_PROPERTY:
                    onSettingsChange();
                    break;
                case JOB_PROPERTY:
                    onJobChange();
                    break;
            }
        });
    }

    private JMenu getPluginsMenu() {
        JMenu result = new JMenu();
        result.add(openPlugin.toAction(plugins)).setText("Open plugin location");
        return result;
    }

    private void onSettingsChange() {
        if (settings != null) {
            apps.setModel(SwingUtil.modelOf(settings.getApps()));
            configs.setModel(SwingUtil.modelOf(settings.getConfigs()));
            userDirs.setModel(SwingUtil.modelOf(SwingUtil.concat(UserDir.TEMP, settings.getUserDirs())));
            plugins.setModel(SwingUtil.modelOf(settings.getPlugins()));
        } else {
            apps.setModel(new DefaultComboBoxModel());
            configs.setModel(new DefaultComboBoxModel());
            userDirs.setModel(new DefaultComboBoxModel());
            plugins.setModel(new DefaultComboBoxModel());
        }
        computeJob();
    }

    private void onJobChange() {
        if (job != null) {
            apps.setSelectedItem(job.getApp());
            configs.setSelectedItem(job.getConfig());
            userDirs.setSelectedItem(job.getUserDir());
//        plugins.setSelectedIndices(job.getPlugins().stream().mapToInt(plugins.get));
        } else {
            apps.setSelectedItem(null);
            configs.setSelectedItem(null);
            userDirs.setSelectedItem(null);
            plugins.setSelectedIndex(-1);;
        }
    }

    private void computeJob() {
        if (apps.getSelectedIndex() != -1
                && configs.getSelectedIndex() != -1
                && userDirs.getSelectedIndex() != -1) {
            setJob(Job
                    .builder()
                    .app((App) apps.getSelectedItem())
                    .config((Config) configs.getSelectedItem())
                    .userDir((UserDir) userDirs.getSelectedItem())
                    .plugins(plugins.getSelectedValuesList())
                    .build());
        } else {
            setJob(null);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        Settings old = this.settings;
        this.settings = settings;
        firePropertyChange(SETTINGS_PROPERTY, old, this.settings);
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        Job old = this.job;
        this.job = job;
        firePropertyChange(JOB_PROPERTY, old, this.job);
    }

    private static final class OpenPluginLocation extends JCommand<JList<Plugin>> {

        private final Desktop desktop = DesktopManager.get();

        @Override
        public void execute(JList<Plugin> c) throws Exception {
            desktop.showInFolder(c.getSelectedValue().getFile());
        }

        @Override
        public boolean isEnabled(JList<Plugin> c) {
            return JLists.isSingleSelectionIndex(c.getSelectionModel())
                    && desktop.isSupported(Desktop.Action.SHOW_IN_FOLDER);
        }

        @Override
        public JCommand.ActionAdapter toAction(JList<Plugin> c) {
            return super.toAction(c).withWeakListSelectionListener(c.getSelectionModel());
        }
    }

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
        configs = new javax.swing.JComboBox<>();

        jLabel4.setText("User dir:");

        jScrollPane1.setViewportView(plugins);

        jLabel1.setText("Application:");

        jLabel5.setText("Plugins:");

        jLabel2.setText("Config:");

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
                    .addComponent(configs, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                        .addComponent(configs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userDirs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<App> apps;
    private javax.swing.JComboBox<nbbrd.nbpl.core.Config> configs;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<Plugin> plugins;
    private javax.swing.JComboBox<UserDir> userDirs;
    // End of variables declaration//GEN-END:variables
}
