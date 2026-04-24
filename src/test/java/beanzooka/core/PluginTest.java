package beanzooka.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class PluginTest {

    @Test
    void desktopSearch_findsNbm() {
        assertThat(new Plugin.DesktopSearch(PluginTest::fakeSearch).findResources())
                .hasSize(1)
                .element(0)
                .returns(Paths.get("/plugins/my-plugin.nbm").toFile(), Plugin::getFile)
                .returns("my-plugin", Plugin::getLabel);
    }

    @Test
    void desktopSearch_ignoresNonNbm() {
        assertThat(new Plugin.DesktopSearch(ignore -> new File[]{Paths.get("/file.jar").toFile()}).findResources())
                .isEmpty();
    }

    @Test
    void desktopSearch_empty() {
        assertThat(new Plugin.DesktopSearch(ignore -> new File[0]).findResources())
                .isEmpty();
    }

    private static File[] fakeSearch(String ignore) {
        return new File[]{Paths.get("/plugins/my-plugin.nbm").toFile()};
    }
}

