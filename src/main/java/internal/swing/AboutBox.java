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

import nbbrd.io.sys.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author Philippe Charles
 */
public final class AboutBox {

    private AboutBox() {
        // static class
    }

    public static void showDialog(Component parent, String name, String version, Icon icon) throws HeadlessException {
        JTextPane html = new JTextPane();
        html.setContentType("text/html");
        html.setText(toHtml(name, version));
        html.setEditable(false);
        html.setBorder(BorderFactory.createEmptyBorder());

        if (JOptionPane.showOptionDialog(parent, html, "About " + name, JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, icon, new Object[]{"Copy and Close", "Close"}, "Copy and Close") == 0) {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(toReport(name, version)), null);
        }
    }

    private static String toHtml(String name, String version) {
        SystemProperties props = SystemProperties.DEFAULT;
        return "<h2>" + name + " " + version + "</h2>" +
                "<b>Java:</b> " + lookupJava(props) + "<br>" +
                "<b>Runtime:</b> " + lookupRuntime(props) + "<br>" +
                "<b>System:</b> " + lookupSystem(props) + "<br><br>";
    }

    private static String toReport(String name, String version) {
        SystemProperties props = SystemProperties.DEFAULT;
        return name + " " + version + System.lineSeparator() +
                "  Java: " + lookupJava(props) + System.lineSeparator() +
                "  Runtime: " + lookupRuntime(props) + System.lineSeparator() +
                "  System: " + lookupSystem(props) + System.lineSeparator();
    }

    private static String lookupJava(SystemProperties p) {
        return p.getJavaVersion()
                + "; " + p.getJavaVmName()
                + " " + p.getJavaVmVersion();
    }

    private static String lookupRuntime(SystemProperties p) {
        return System.getProperty("java.runtime.name")
                + " " + System.getProperty("java.runtime.version");
    }

    private static String lookupSystem(SystemProperties p) {
        return p.getOsName()
                + " version " + p.getOsVersion()
                + " running on " + p.getOsArch()
                + "; " + System.getProperty("file.encoding")
                + "; " + System.getProperty("user.language") + "_" + System.getProperty("user.country");
    }
}
