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
package beanzooka.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
@lombok.experimental.Wither
public class Jdk {

    @lombok.NonNull
    private String label;

    @lombok.NonNull
    private File javaHome;

    private String options;

    private String clusters;

    public Map<String, String> getConfigFileEntries() {
        Map<String, String> result = new HashMap<>();
        result.put("jdkhome", getJavaHome().getPath());
        if (getOptions() != null) {
            result.put("default_options", options);
        }
        if (getClusters() != null) {
            result.put("extra_clusters", clusters);
        }
        return result;
    }

    public void writeConfigFile(File file) throws IOException {
        Path etc = file.toPath().getParent();
        if (!Files.exists(etc)) {
            Files.createDirectories(etc);
        }
        List<String> content = getConfigFileEntries()
                .entrySet()
                .stream()
                .map(o -> o.getKey() + "=\"" + o.getValue() + "\"")
                .collect(Collectors.toList());
        Files.write(file.toPath(), content, StandardOpenOption.CREATE);
    }
}
