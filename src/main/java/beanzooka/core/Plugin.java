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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
@lombok.With
public class Plugin {

    @lombok.NonNull
    private String label;

    @lombok.NonNull
    private File file;

    public void extract(File folder) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(file.toPath(), (ClassLoader) null)) {
            Util.copyAll(fs.getPath("/netbeans"), folder.toPath());
        }
    }

    public static List<Plugin> ofDesktopSearch(Function<String, File[]> engine) {
        return Stream.of(engine.apply(".nbm"))
                .filter(Plugin::isNbm)
                .map(Plugin::ofNbm)
                .collect(toList());
    }

    private static boolean isNbm(File file) {
        return file.getName().endsWith(".nbm");
    }

    private static Plugin ofNbm(File file) {
        return Plugin.builder().label(file.getName()).file(file).build();
    }
}
