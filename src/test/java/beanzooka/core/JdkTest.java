package beanzooka.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class JdkTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void ofDesktopSearch() {
        assertThat(Jdk.ofDesktopSearch(ignore -> new File[]{new File("C:\\some\\path\\jdk-21+35\\bin\\javaw.exe")}))
                .hasSize(1)
                .element(0)
                .returns(new File("C:\\some\\path\\jdk-21+35"), Jdk::getJavaHome);;
    }
}