package beanzooka.core;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    @Test
    void ofDesktopSearch() {
        assertThat(App.ofDesktopSearch(ignore -> new File[]{new File("C:\\some\\path\\jdemetra-3.2.2-windows-bin\\bin\\nbdemetra64.exe")}))
                .hasSize(1)
                .element(0)
                .returns(new File("C:\\some\\path\\jdemetra-3.2.2-windows-bin\\bin\\nbdemetra64.exe"), App::getFile);
    }
}