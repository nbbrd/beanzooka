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
package nbbrd.nbpl.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class Configuration {

    private App app;

    private Jdk jdk;

    private UserDir userDir;

    private List<Plugin> plugins;

    public File init() throws IOException {
        File workingDir = userDir.createWorkingDir();

        jdk.writeConfigFile(UserDir.resolveConfigFile(workingDir, app.getBranding()));

        for (Plugin plugin : plugins) {
            plugin.extract(workingDir);
        }

        return workingDir;
    }

    public void launch(File workingDir) throws Exception {
        new ProcessBuilder(app.getFile().toString(), "--userdir", workingDir.toString())
                .start()
                .waitFor();
    }
}
