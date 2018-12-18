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

import nbbrd.nbpl.core.Job;
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
    private final Job job;

    @lombok.Getter
    private final File workingDir;

    private final PropertyChangeSupport propertyChangeSupport;
    private final SwingWorker<Void, Void> worker;

    public Session(Job job, File workingDir) {
        this.job = job;
        this.workingDir = workingDir;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.worker = new SwingWorker() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    job.launch(workingDir);
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
    }

    public void execute() {
        worker.execute();
    }

    public SwingWorker.StateValue getState() {
        return worker.getState();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static Session of(Job job) throws IOException {
        return new Session(job, job.init());
    }
}
