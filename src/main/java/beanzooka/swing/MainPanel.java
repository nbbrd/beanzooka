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

import beanzooka.core.Resources;
import beanzooka.io.XmlResources;
import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.JCommand;
import internal.swing.PersistantFileChooser;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public final class MainPanel extends javax.swing.JPanel {

    /**
     * Creates new form NetBeansLauncher
     */
    public MainPanel() {
        initComponents();
        initCommands();
    }

    private void initCommands() {
        new NewCmd().init(newButton, this, FontAwesome.FA_FILE, "New", "F1");
        new OpenCmd().init(openButton, this, FontAwesome.FA_FOLDER_OPEN, "Open", "F2");
        new SaveAsCmd().init(saveAsButton, this, FontAwesome.FA_UPLOAD, "Save as", "F3");
        new LaunchCmd().init(launchButton, this, FontAwesome.FA_PLAY_CIRCLE, "Launch", "F5");
    }

    private static abstract class CustomCommand extends JCommand<MainPanel> {

        public CustomCommand init(JButton button, MainPanel c, FontAwesome icon, String toolTip, String key) {
            Action action = toAction(c);

            button.setAction(action);
            button.setIcon(icon.getIcon(Color.DARK_GRAY, 14f));
            button.setToolTipText(toolTip + " (" + key + ")");

            String id = getClass().getName();
            c.getActionMap().put(id, action);
            c.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), id);

            return this;
        }

        @Override
        public ActionAdapter toAction(MainPanel component) {
            return new ActionAdapterWithExceptionDialog(component);
        }

        private class ActionAdapterWithExceptionDialog extends ActionAdapter {

            public ActionAdapterWithExceptionDialog(MainPanel c) {
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
        public void execute(MainPanel c) throws Exception {
            c.resources.setResources(Resources.builder().build());
        }
    }

    private static final class OpenCmd extends CustomCommand {

        private final JFileChooser fileChooser;

        private OpenCmd() {
            this.fileChooser = new PersistantFileChooser(MainPanel.class);
            fileChooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        }

        @Override
        public void execute(MainPanel c) throws Exception {
            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(c)) {
                c.resources.setResources(XmlResources.read(fileChooser.getSelectedFile().toPath()));
            }
        }
    }

    private static final class SaveAsCmd extends CustomCommand {

        private final JFileChooser fileChooser;

        public SaveAsCmd() {
            this.fileChooser = new PersistantFileChooser(MainPanel.class);
            fileChooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        }

        @Override
        public void execute(MainPanel c) throws Exception {
            if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(c)) {
                File target = fileChooser.getSelectedFile();
                if (!target.exists() || confirm(c)) {
                    XmlResources.write(target.toPath(), c.resources.getResources());
                }
            }
        }

        private boolean confirm(MainPanel c) {
            return JOptionPane.showConfirmDialog(c, "File exists already. Delete it anyway?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
    }

    private static final class LaunchCmd extends CustomCommand {

        @Override
        public void execute(MainPanel c) throws Exception {
            Session session = Session.of(c.resources.getConfiguration().get());
            c.sessions.add(session);
            session.execute();
        }

        @Override
        public boolean isEnabled(MainPanel c) {
            return c.resources.getConfiguration().isPresent();
        }

        @Override
        public ActionAdapter toAction(MainPanel c) {
            return super.toAction(c)
                    .withWeakPropertyChangeListener(c.resources, ResourcesPanel.CONFIGURATION_PROPERTY);
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
        resources = new beanzooka.swing.ResourcesPanel();
        sessions = new beanzooka.swing.SessionsPanel();

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
                    .addComponent(resources, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resources, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private beanzooka.swing.ResourcesPanel resources;
    private javax.swing.JButton saveAsButton;
    private beanzooka.swing.SessionsPanel sessions;
    // End of variables declaration//GEN-END:variables
}
