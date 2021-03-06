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
import internal.swing.About;
import internal.swing.FixedImageIcon;
import internal.swing.JFileChoosers;
import internal.swing.SwingUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

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
        preventClosing();
    }

    private void initCommands() {
        new NewCmd().init(newButton, this, FontAwesome.FA_FILE, "New", "F1");
        new OpenCmd().init(openButton, this, FontAwesome.FA_FOLDER_OPEN, "Open", "F2");
        new SaveAsCmd().init(saveAsButton, this, FontAwesome.FA_UPLOAD, "Save as", "F3");
        new LaunchCmd().init(launchButton, this, FontAwesome.FA_PLAY_CIRCLE, "Launch", "F5");
        new AboutCmd().init(aboutButton, this, FontAwesome.FA_INFO_CIRCLE, "About", "F7");
    }

    private void preventClosing() {
        SwingUtil.preventClosing(this, this::canClose);
    }

    private boolean canClose() {
        return !sessions.isRunning() || JOptionPane.showConfirmDialog(MainPanel.this, "Some sessions are still running.\nDo you want to close the application anyway?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void open(File file) {
        try {
            resources.setResources(XmlResources.read(file.toPath()));
        } catch (IOException | XMLStreamException ex) {
            reportException(ex);
        }
    }

    private static void reportException(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        JScrollPane message = new JScrollPane(new JTextArea(writer.toString()));
        message.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(null, message, ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
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
                reportException(ex);
            }
        }
    }

    private static final class NewCmd extends CustomCommand {

        @Override
        public void execute(MainPanel c) throws Exception {
            c.resources.setResources(Resources.builder().build());
        }
    }

    private static JFileChooser newResourcesFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        JFileChoosers.autoPersistUserNodeForClass(fileChooser, Resources.class);
        fileChooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        return fileChooser;
    }

    private static final class OpenCmd extends CustomCommand {

        @Override
        public void execute(MainPanel c) throws Exception {
            JFileChooser fileChooser = newResourcesFileChooser();
            Optional<File> file = JFileChoosers.getOpenFile(fileChooser, c);
            if (file.isPresent()) {
                c.resources.setResources(XmlResources.read(file.get().toPath()));
            }
        }
    }

    private static final class SaveAsCmd extends CustomCommand {

        @Override
        public void execute(MainPanel c) throws Exception {
            JFileChooser fileChooser = newResourcesFileChooser();
            Optional<File> file = JFileChoosers.getSaveFile(fileChooser, c);
            if (file.isPresent()) {
                XmlResources.write(file.get().toPath(), c.resources.getResources());
            }
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

    private static final class AboutCmd extends CustomCommand {

        @Override
        public void execute(MainPanel c) throws Exception {
            JTextPane html = new JTextPane();
            html.setContentType("text/html");
            html.setText(toHtml(About.lookup()));
            html.setEditable(false);

            Icon icon = new FixedImageIcon(new ImageIcon(MainPanel.class.getResource("/beanzooka/beanzooka_redux_32.png")));
            JOptionPane.showMessageDialog(c, new JScrollPane(html), "About", JOptionPane.INFORMATION_MESSAGE, icon);
        }

        private String toHtml(About about) {
            return new StringBuilder()
                    .append("<html>")
                    .append("<b>Application:</b> ").append(about.getApplication()).append("<br>")
                    .append("<b>Java:</b> ").append(about.getJava()).append("<br>")
                    .append("<b>Runtime:</b> ").append(about.getRuntime()).append("<br>")
                    .append("<b>System:</b> ").append(about.getSystem()).append("<br>")
                    .toString();
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
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        aboutButton = new javax.swing.JButton();
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
        jToolBar1.add(filler1);

        aboutButton.setText("about");
        aboutButton.setFocusable(false);
        aboutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        aboutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(aboutButton);

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
    private javax.swing.JButton aboutButton;
    private javax.swing.Box.Filler filler1;
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
