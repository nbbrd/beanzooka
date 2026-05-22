package beanzooka.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JdkTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void ofDesktopSearch() {
        assertThat(new Jdk.DesktopSearch(JdkTest::fakeSearch).findResources())
                .hasSize(1)
                .element(0)
                .returns(Paths.get("C:\\some\\path\\jdk-21+35").toFile(), Jdk::getJavaHome);
    }

    private static File[] fakeSearch(String ignore) {
        return new File[]{Paths.get("C:\\some\\path\\jdk-21+35\\bin\\javaw.exe").toFile()};
    }

    @Test
    void javaHomeProperty_returnsCurrentJvm() {
        // SystemProperties.DEFAULT.getJavaHome() is always set while a JVM is running
        assertThat(new Jdk.JavaHomeProperty().findResources())
                .isNotEmpty();
    }

    @Test
    void javaHomeEnv_doesNotThrow() {
        // JAVA_HOME may or may not be set; the finder must not throw in either case
        assertThatCode(() -> new Jdk.JavaHomeEnv().findResources())
                .doesNotThrowAnyException();
    }

    @Test
    void testWhere() {
        assertThatCode(() -> new Jdk.WhereSearch().findResources())
                .doesNotThrowAnyException();
    }
}