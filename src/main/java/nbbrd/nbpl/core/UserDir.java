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
package nbbrd.nbpl.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class UserDir {

    public static final UserDir TEMP = UserDir.builder().label("---").folder(new File("")).build();

    @lombok.NonNull
    private String label;

    @lombok.NonNull
    private File folder;

    private boolean clone;

    public static File resolveLogFile(File workingDir) {
        return workingDir.toPath().resolve("var").resolve("log").resolve("messages.log").toFile();
    }

    public static File resolveConfigFile(File workingDir, String branding) {
        return workingDir.toPath().resolve("etc").resolve(branding + ".conf").toFile();
    }

    public File createWorkingDir() throws IOException {
        if (UserDir.TEMP.equals(this)) {
            return createTempUserDir();
        }
        if (clone) {
            File cloned = createTempUserDir();
            Util.copyAll(folder.toPath(), cloned.toPath());
            return cloned;
        }
        return folder;
    }

    private static File createTempUserDir() throws IOException {
        File result = Files.createTempDirectory("userdir").toFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteDirectory(result)));
        return result;
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
