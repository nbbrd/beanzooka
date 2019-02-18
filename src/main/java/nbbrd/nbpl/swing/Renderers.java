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

import ec.util.various.swing.FontAwesome;
import java.awt.Color;
import java.io.File;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import nbbrd.nbpl.core.App;
import nbbrd.nbpl.core.Config;
import nbbrd.nbpl.core.Plugin;
import nbbrd.nbpl.core.UserDir;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Renderers {

    void renderApp(JLabel label, App value) {
        setText(label, value, App::getLabel);
        if (value != null) {
            label.setToolTipText(value.getFile().toString());
            setIfInvalidFile(label, value.getFile());
        }
    }

    void renderConfig(JLabel label, Config value) {
        setText(label, value, Config::getLabel);
        if (value != null) {
            label.setToolTipText(value.getJavaFile().toString());
            setIfInvalidFile(label, value.getJavaFile());
        }
    }

    void renderUserDir(JLabel label, UserDir value) {
        setText(label, value, UserDir::getLabel);
        if (value != null) {
            label.setToolTipText(value.getFolder().toString());
            if (value.isClone()) {
                setIfInvalidFolder(label, value.getFolder());
            }
        }
    }

    void renderFolder(JLabel label, File value) {
        if (value != null) {
            label.setText(value.getPath());
            label.setToolTipText(value.toString());
            setIfInvalidFolder(label, value);
        }
    }

    void renderPlugin(JLabel label, Plugin value) {
        setText(label, value, Plugin::getLabel);
        if (value != null) {
            label.setToolTipText(value.getFile().toString());
            setIfInvalidFile(label, value.getFile());
        }
    }

    void renderState(JLabel label, SwingWorker.StateValue value) {
        label.setText(value.name());
    }

    private <T> void setText(JLabel label, T value, Function<T, String> toLabel) {
        if (value != null) {
            label.setText(toLabel.apply(value));
        }
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
}
