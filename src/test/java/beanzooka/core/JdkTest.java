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
        assertThat(Jdk.ofDesktopSearch(ignore -> new File[]{Paths.get("C:\\some\\path\\jdk-21+35\\bin\\javaw.exe").toFile()}))
                .hasSize(1)
                .element(0)
                .returns(Paths.get("C:\\some\\path\\jdk-21+35").toFile(), Jdk::getJavaHome);
    }
}