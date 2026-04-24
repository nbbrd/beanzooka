package beanzooka.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

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
}