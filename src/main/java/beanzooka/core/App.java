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
import nbbrd.io.sys.OS;

import java.io.File;
import java.nio.file.Paths;
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
public class App {

    @StaticFactoryMethod
    public static App ofNetBeansRuntime(File runtime) {
        if (!OS.NAME.equals(OS.Name.WINDOWS)) {
            runtime = Paths.get(runtime.toString().replace(".exe", "")).toFile();
        }
        return App
                .builder()
                .label(runtime.getParentFile().getParentFile().getName())
                .file(runtime)
                .build();
    }

    @lombok.NonNull
    String label;

    @lombok.NonNull
    File file;

    public String getBranding() {
        return getBranding(file);
    }

    private static String getBranding(File file) {
        return file.getName().replace("64.exe", "").replace(".exe", "");
    }

    public static List<App> findApps(Function<String, File[]> engine) {
        return ResourceFinder.findResources(List.of(new DesktopSearch(engine)));
    }

    @VisibleForTesting
    @lombok.RequiredArgsConstructor
    static final class DesktopSearch implements ResourceFinder<App> {

        @lombok.Getter(AccessLevel.PRIVATE)
        @lombok.NonNull
        private final Function<String, File[]> engine;

        @lombok.experimental.Delegate
        private final NetBeansRuntimeSupport delegate = NetBeansRuntimeSupport
                .builder()
                .runtime(() -> getEngine().apply("64.exe"))
                .build();
    }

    @lombok.Builder
    private static final class NetBeansRuntimeSupport implements ResourceFinder<App> {

        private final Supplier<File[]> runtime;

        @Override
        public void addResourcesTo(@NonNull Consumer<? super App> consumer) {
            Stream.of(runtime.get())
                    .filter(NetBeansRuntimeSupport::isNetBeansRuntime)
                    .map(App::ofNetBeansRuntime)
                    .forEach(consumer);
        }

        static boolean isNetBeansRuntime(@NonNull File file) {
            return file.getName().endsWith("64.exe")
                    && file.getParentFile() != null
                    && file.getParentFile().getName().equals("bin")
                    && file.getParentFile().getParentFile() != null;
        }
    }
}
