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

import nbbrd.nbpl.core.Settings;
import nbbrd.nbpl.core.SettingsParser;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.JCommand;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JButton;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public final class NetBeansLauncher extends javax.swing.JPanel {

    public static void main(String args[]) {
        new BasicSwingLauncher()
                .content(NetBeansLauncher.class)
                .title("NetBeans Platform launcher")
                .icons(() -> FontAwesome.FA_ROCKET.getImages(Color.DARK_GRAY, 16f, 32f, 64f))
                .size(600, 400)
                .launch();
    }

    /**
     * Creates new form NetBeansLauncher
     */
    public NetBeansLauncher() {
        initComponents();
        initCommands();
    }

    private void initCommands() {
        new NewCmd().init(newButton, this, FontAwesome.FA_FILE, "New", "F1");
        new OpenCmd().init(openButton, this, FontAwesome.FA_FOLDER_OPEN, "Open", "F2");
        new SaveAsCmd().init(saveAsButton, this, FontAwesome.FA_UPLOAD, "Save as", "F3");
        new LaunchCmd().init(launchButton, this, FontAwesome.FA_PLAY_CIRCLE, "Launch", "F5");
    }

    private static abstract class CustomCommand extends JCommand<NetBeansLauncher> {

        public CustomCommand init(JButton button, NetBeansLauncher c, FontAwesome icon, String toolTip, String key) {
            Action action = toAction(c);

            button.setAction(action);
            button.setIcon(FontAwesomeUtils.getIcon(icon, BeanInfo.ICON_MONO_16x16));
            button.setToolTipText(toolTip + " (" + key + ")");

            String id = getClass().getName();
            c.getActionMap().put(id, action);
            c.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), id);

            return this;
        }

        @Override
        public ActionAdapter toAction(NetBeansLauncher component) {
            return new ActionAdapterWithExceptionDialog(component);
        }

        private class ActionAdapterWithExceptionDialog extends ActionAdapter {

            public ActionAdapterWithExceptionDialog(NetBeansLauncher c) {
                super(c);
            }

            @Override
            public void handleException(ActionEvent event, Exception ex) {
                StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                JScrollPane message = new JScrollPane(new JTextArea(writer.toString()));
                message.setPreferredSize(new Dimension(500, 300));
                JOptionPane.showMessageDialog(null, message, ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static final class NewCmd extends CustomCommand {

        @Override
        public void execute(NetBeansLauncher c) throws Exception {
            c.settings.setSettings(null);
        }
    }

    private static final class OpenCmd extends CustomCommand {

        private final static String LAST_USED_FILE = "lastUsedFile";

        private final Preferences prefs;
        private final JFileChooser fileChooser;

        private OpenCmd() {
            this.prefs = Preferences.userNodeForPackage(NetBeansLauncher.class);
            this.fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
            loadCurrentDir();
        }

        private void loadCurrentDir() {
            String dir = prefs.get(LAST_USED_FILE, null);
            if (dir != null) {
                fileChooser.setCurrentDirectory(new File(dir));
            }
        }

        private void storeCurrentDir() {
            prefs.put(LAST_USED_FILE, fileChooser.getCurrentDirectory().toString());
        }

        @Override
        public void execute(NetBeansLauncher c) throws Exception {
            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(c)) {
                Settings settings = SettingsParser.parse(fileChooser.getSelectedFile().toPath());
                c.settings.setSettings(settings);
                storeCurrentDir();
            }
        }
    }

    private static final class SaveAsCmd extends CustomCommand {

        @Override
        public void execute(NetBeansLauncher c) throws Exception {
        }

        @Override
        public boolean isEnabled(NetBeansLauncher c) {
            return false;
        }
    }

    private static final class LaunchCmd extends CustomCommand {

        @Override
        public void execute(NetBeansLauncher c) throws Exception {
            Session session = Session.of(c.settings.getJob());
            c.sessions.add(session);
            session.execute();
        }

        @Override
        public boolean isEnabled(NetBeansLauncher c) {
            return c.settings.getJob() != null;
        }

        @Override
        public ActionAdapter toAction(NetBeansLauncher c) {
            return super.toAction(c).withWeakPropertyChangeListener(c.settings, SettingsPanel.JOB_PROPERTY);
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

        jToolBar1 = new javax.swing.JToolBar();
        newButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        launchButton = new javax.swing.JButton();
        settings = new nbbrd.nbpl.swing.SettingsPanel();
        sessions = new nbbrd.nbpl.swing.SessionsPanel();

        setPreferredSize(new java.awt.Dimension(600, 400));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        newButton.setText("new");
        newButton.setToolTipText("New");
        newButton.setFocusable(false);
        newButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(newButton);

        openButton.setText("open");
        openButton.setToolTipText("Open");
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(openButton);

        saveAsButton.setText("save");
        saveAsButton.setToolTipText("Save as");
        saveAsButton.setFocusable(false);
        saveAsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveAsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(saveAsButton);
        jToolBar1.add(jSeparator1);

        launchButton.setText("launch");
        launchButton.setToolTipText("Launch job");
        launchButton.setFocusable(false);
        launchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        launchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(launchButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sessions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settings, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settings, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sessions, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton launchButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton openButton;
    private javax.swing.JButton saveAsButton;
    private nbbrd.nbpl.swing.SessionsPanel sessions;
    private nbbrd.nbpl.swing.SettingsPanel settings;
    // End of variables declaration//GEN-END:variables
}
