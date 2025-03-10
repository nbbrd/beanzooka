package beanzooka;

import lombok.NonNull;
import nbbrd.io.sys.SystemProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static nbbrd.io.function.IOFunction.unchecked;
import static nbbrd.io.sys.ProcessReader.readToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

class BeanzookaIT {

    @Test
    void testVersion() throws IOException {
        Path uberjar = Paths.get("target").resolve(About.getName() + "-" + About.getVersion() + "-bin.jar");
        assertThat(uberjar).existsNoFollowLinks().isRegularFile();

        JavaRuntime javaRuntime = JavaRuntime.ofDefault();

        assertThat(uberjar)
                .existsNoFollowLinks().isRegularFile()
                .extracting(unchecked(javaRuntime::getVersion), STRING)
                .contains("Beanzooka", javaRuntime.getJavaVersion());

    }

    @lombok.AllArgsConstructor
    private static final class JavaRuntime {

        public static JavaRuntime ofDefault() {
            return new JavaRuntime(requireNonNull(SystemProperties.DEFAULT.getJavaHome()));
        }

        private final @NonNull Path javaHome;

        public String getVersion(Path uberjar) throws IOException {
            return readToString(UTF_8, new ProcessBuilder(
                    javaHome.resolve("bin").resolve("java").toString(),
                    "-splash:",
                    "-jar", uberjar.toString(),
                    "--version").start()
            );
        }

        public String getJavaVersion() throws IOException {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(javaHome.resolve("release")));
            return properties.getProperty("JAVA_VERSION", "").replace("\"", "");
        }
    }
}