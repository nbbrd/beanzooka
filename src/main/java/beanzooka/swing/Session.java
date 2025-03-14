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

import beanzooka.core.Configuration;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingWorker;

/**
 *
 * @author Philippe Charles
 */
public class Session {

    @lombok.Getter
    private final Configuration configuration;

    @lombok.Getter
    private final File workingDir;

    private final PropertyChangeSupport propertyChangeSupport;
    private SwingWorker<Void, Void> worker;

    public Session(Configuration configuration, File workingDir) {
        this.configuration = configuration;
        this.workingDir = workingDir;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void execute() {
        if (worker == null || worker.getState() == SwingWorker.StateValue.DONE) {
            this.worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        configuration.launch(workingDir);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
            worker.addPropertyChangeListener(o -> {
                switch (o.getPropertyName()) {
                    case "state":
                        propertyChangeSupport.firePropertyChange(o.getPropertyName(), o.getOldValue(), o.getNewValue());
                        break;
                }
            });
            worker.execute();
        }
    }

    public SwingWorker.StateValue getState() {
        return worker != null ? worker.getState() : SwingWorker.StateValue.PENDING;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static Session of(Configuration configuration) throws IOException {
        return new Session(configuration, configuration.init());
    }
}
