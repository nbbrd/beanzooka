package beanzooka.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void ofDesktopSearch() {
        assertThat(App.ofDesktopSearch(ignore -> new File[]{Paths.get("C:\\some\\path\\jdemetra-3.2.2-windows-bin\\bin\\nbdemetra64.exe").toFile()}))
                .hasSize(1)
                .element(0)
                .returns(Paths.get("C:\\some\\path\\jdemetra-3.2.2-windows-bin\\bin\\nbdemetra64.exe").toFile(), App::getFile);
    }
}