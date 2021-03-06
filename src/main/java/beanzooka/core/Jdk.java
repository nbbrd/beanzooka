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
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
@lombok.With
public class Jdk {

    @lombok.NonNull
    private String label;

    @lombok.NonNull
    private File javaHome;

    private String options;

    @lombok.Singular
    private List<File> clusters;

    public Map<String, String> getConfigFileEntries() {
        Map<String, String> result = new HashMap<>();
        result.put("jdkhome", javaHome.getPath());
        if (options != null && !options.isEmpty()) {
            result.put("default_options", options);
        }
        if (clusters != null && !clusters.isEmpty()) {
            result.put("extra_clusters", fromFiles(clusters));
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

    public static String fromFiles(List<File> files) {
        return files.stream().map(File::getPath).collect(Collectors.joining(File.pathSeparator));
    }

    public static List<File> toFiles(String files) {
        return Stream.of(files.split(File.pathSeparator, -1)).map(File::new).collect(Collectors.toList());
    }
}
