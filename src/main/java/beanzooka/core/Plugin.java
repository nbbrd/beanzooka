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

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
@lombok.With
public class Plugin {

    @StaticFactoryMethod
    public static Plugin ofNbm(File file) {
        return Plugin
                .builder()
                .label(file.getName().replace(".nbm", ""))
                .file(file)
                .build();
    }

    @lombok.NonNull
    String label;

    @lombok.NonNull
    File file;

    public void extract(File folder) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(file.toPath(), (ClassLoader) null)) {
            Util.copyAll(fs.getPath("/netbeans"), folder.toPath());
        }
    }

    public static List<Plugin> findPlugins(Function<String, File[]> engine) {
        return ResourceFinder.findResources(List.of(new DesktopSearch(engine)));
    }

    @VisibleForTesting
    @lombok.RequiredArgsConstructor
    static final class DesktopSearch implements ResourceFinder<Plugin> {

        @lombok.Getter(AccessLevel.PRIVATE)
        @lombok.NonNull
        private final Function<String, File[]> engine;

        @lombok.experimental.Delegate
        private final NbmSupport delegate = NbmSupport
                .builder()
                .runtime(() -> getEngine().apply(".nbm"))
                .build();
    }

    @lombok.Builder
    private static final class NbmSupport implements ResourceFinder<Plugin> {

        private final Supplier<File[]> runtime;

        @Override
        public void addResourcesTo(@NonNull Consumer<? super Plugin> consumer) {
            Stream.of(runtime.get())
                    .filter(NbmSupport::isNbm)
                    .map(Plugin::ofNbm)
                    .forEach(consumer);
        }

        static boolean isNbm(@NonNull File file) {
            return file.getName().endsWith(".nbm");
        }
    }
}
