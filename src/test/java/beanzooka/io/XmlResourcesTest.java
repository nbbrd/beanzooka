package beanzooka.io;

import beanzooka.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class XmlResourcesTest {

    @Test
    void roundTrip(@TempDir Path dir) throws IOException {
        Resources original = Resources.builder()
                .app(App.builder().label("myApp").file(Paths.get("/app/myapp64.exe").toFile()).build())
                .jdk(Jdk.builder().label("myJdk").javaHome(Paths.get("/jdk").toFile()).build())
                .userDir(UserDir.builder().label("dir1").folder(Paths.get("/userdir").toFile()).build())
                .plugin(Plugin.builder().label("plugin1").file(Paths.get("/plugins/p.nbm").toFile()).build())
                .selectedAppIndex(0)
                .selectedJdkIndex(0)
                .tempUserDirSelected(false)
                .selectedUserDirIndex(0)
                .selectedPluginIndex(0)
                .build();

        Path file = dir.resolve("resources.xml");
        XmlResources.write(file, original);
        Resources loaded = XmlResources.read(file);

        assertThat(loaded).isEqualTo(original);
    }

    @Test
    void backwardCompatible_noSelection(@TempDir Path dir) throws IOException {
        // XML produced by an old version of Beanzooka — no <selection> block
        String xml = "<?xml version=\"1.0\" ?>"
                + "<resources>"
                + "<jdks/><apps/><userDirs/><plugins/>"
                + "</resources>";

        Path file = dir.resolve("old.xml");
        Files.write(file, xml.getBytes(StandardCharsets.UTF_8));
        Resources loaded = XmlResources.read(file);

        assertThat(loaded.getSelectedAppIndex()).isNull();
        assertThat(loaded.getSelectedJdkIndex()).isNull();
        assertThat(loaded.isTempUserDirSelected()).isTrue(); // Builder.Default = true
        assertThat(loaded.getSelectedUserDirIndex()).isNull();
        assertThat(loaded.getSelectedPluginIndices()).isEmpty();
    }

    @Test
    void selection_multiplePlugins(@TempDir Path dir) throws IOException {
        Resources original = Resources.builder()
                .selectedAppIndex(1)
                .selectedJdkIndex(2)
                .tempUserDirSelected(false)
                .selectedUserDirIndex(3)
                .selectedPluginIndex(0)
                .selectedPluginIndex(1)
                .build();

        Path file = dir.resolve("sel.xml");
        XmlResources.write(file, original);
        Resources loaded = XmlResources.read(file);

        assertThat(loaded.getSelectedAppIndex()).isEqualTo(1);
        assertThat(loaded.getSelectedJdkIndex()).isEqualTo(2);
        assertThat(loaded.isTempUserDirSelected()).isFalse();
        assertThat(loaded.getSelectedUserDirIndex()).isEqualTo(3);
        assertThat(loaded.getSelectedPluginIndices()).containsExactly(0, 1);
    }

    @Test
    void selection_defaults_whenEmpty(@TempDir Path dir) throws IOException {
        // Empty Resources: selection block is written but contains only tempUserDir=true
        Resources original = Resources.builder().build();

        Path file = dir.resolve("empty.xml");
        XmlResources.write(file, original);
        Resources loaded = XmlResources.read(file);

        assertThat(loaded.getSelectedAppIndex()).isNull();
        assertThat(loaded.getSelectedJdkIndex()).isNull();
        assertThat(loaded.isTempUserDirSelected()).isTrue();
        assertThat(loaded.getSelectedUserDirIndex()).isNull();
        assertThat(loaded.getSelectedPluginIndices()).isEmpty();
    }
}

