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
import nbbrd.io.sys.ProcessReader;
import nbbrd.io.sys.SystemProperties;
import nbbrd.io.win.WhereWrapper;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
@lombok.With
public class Jdk {

    @StaticFactoryMethod
    public static Jdk ofJavaHome(File javaHome) {
        return Jdk
                .builder()
                .label(javaHome.getName())
                .javaHome(javaHome)
                .build();
    }

    @lombok.NonNull
    String label;

    @lombok.NonNull
    File javaHome;

    String options;

    @lombok.Singular
    List<File> clusters;

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
                .collect(toList());
        Files.write(file.toPath(), content, StandardOpenOption.CREATE);
    }

    public static String fromFiles(List<File> files) {
        return files.stream().map(File::getPath).collect(Collectors.joining(File.pathSeparator));
    }

    public static List<File> toFiles(String files) {
        return Stream.of(files.split(File.pathSeparator, -1)).map(File::new).collect(toList());
    }

    public static List<Jdk> findJdks(Function<String, File[]> engine) {
        return ResourceFinder.findResources(List.of(
                new JavaHomeProperty(),
                new JavaHomeEnv(),
                new DesktopSearch(engine),
                new WhereSearch()
        ));
    }

    @VisibleForTesting
    static final class JavaHomeProperty implements ResourceFinder<Jdk> {

        @lombok.experimental.Delegate
        private final JavaHomeSupport delegate = JavaHomeSupport
                .builder()
                .homeOf(SystemProperties.DEFAULT::getJavaHome, Path::toFile)
                .build();
    }

    @VisibleForTesting
    static final class JavaHomeEnv implements ResourceFinder<Jdk> {

        @lombok.experimental.Delegate
        private final JavaHomeSupport delegate = JavaHomeSupport
                .builder()
                .homeOf(() -> System.getenv("JAVA_HOME"), File::new)
                .build();
    }

    @VisibleForTesting
    @lombok.RequiredArgsConstructor
    static final class DesktopSearch implements ResourceFinder<Jdk> {

        @lombok.Getter(AccessLevel.PRIVATE)
        @lombok.NonNull
        private final Function<String, File[]> engine;

        @lombok.experimental.Delegate
        private final JavaRuntimeSupport delegate = JavaRuntimeSupport
                .builder()
                .runtime(() -> getEngine().apply("javaw"))
                .build();
    }

    @VisibleForTesting
    static final class WhereSearch implements ResourceFinder<Jdk> {

        @lombok.experimental.Delegate
        private final JavaRuntimeSupport delegate = JavaRuntimeSupport
                .builder()
                .runtime(WhereSearch::where)
                .build();

        private static File[] where() {
            if (OS.NAME.equals(OS.Name.WINDOWS)) {
                try {
                    Process p = new ProcessBuilder(WhereWrapper.COMMAND, "javaw")
                            .redirectError(ProcessBuilder.Redirect.INHERIT)
                            .start();
                    try (BufferedReader reader = ProcessReader.newReader(Charset.defaultCharset(), p)) {
                        return reader.lines().map(File::new).toArray(File[]::new);
                    }
                } catch (IOException ignore) {
                }
            }
            return new File[0];
        }
    }

    @lombok.Builder
    private static final class JavaHomeSupport implements ResourceFinder<Jdk> {

        private final Supplier<Stream<File>> home;

        @Override
        public void addResourcesTo(@NonNull Consumer<? super Jdk> consumer) {
            home.get()
                    .map(Jdk::ofJavaHome)
                    .forEach(consumer);
        }

        public static final class Builder {

            public <X> Builder homeOf(Supplier<@Nullable X> supplier, Function<@NonNull X, File> converter) {
                return home(() -> Optional.ofNullable(supplier.get()).map(converter).stream());
            }
        }
    }

    @lombok.Builder
    private static final class JavaRuntimeSupport implements ResourceFinder<Jdk> {

        private final Supplier<File[]> runtime;

        @Override
        public void addResourcesTo(@NonNull Consumer<? super Jdk> consumer) {
            Stream.of(runtime.get())
                    .filter(JavaRuntimeSupport::isJavaRuntime)
                    .map(javaRuntime -> ofJavaHome(toJavaHome(javaRuntime)))
                    .forEach(consumer);
        }

        static boolean isJavaRuntime(@NonNull File javaRuntime) {
            return javaRuntime.getName().replace(".exe", "").equals("javaw")
                    && javaRuntime.getParentFile() != null
                    && javaRuntime.getParentFile().getName().equals("bin")
                    && toJavaHome(javaRuntime) != null;
        }

        static File toJavaHome(@NonNull File javaRuntime) {
            return javaRuntime.getParentFile().getParentFile();
        }
    }
}
