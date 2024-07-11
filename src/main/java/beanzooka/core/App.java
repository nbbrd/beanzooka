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
package beanzooka.core;

import nbbrd.io.sys.OS;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
@lombok.With
public class App {

    @lombok.NonNull
    String label;

    @lombok.NonNull
    File file;

    public String getBranding() {
        return getBranding(file);
    }

    private static String getBranding(File file) {
        return file.getName().replace("64.exe", "").replace(".exe", "");
    }

    public static List<App> ofDesktopSearch(Function<String, File[]> engine) {
        return Stream.of(engine.apply("64.exe"))
                .filter(App::isNetBeansPlatform)
                .map(App::ofNetBeansPlatform)
                .collect(toList());
    }

    private static boolean isNetBeansPlatform(File file) {
        return file.getName().endsWith("64.exe")
                && file.getParentFile() != null
                && file.getParentFile().getName().equals("bin")
                && file.getParentFile().getParentFile() != null;
    }

    private static App ofNetBeansPlatform(File file) {
        if (!OS.NAME.equals(OS.Name.WINDOWS)) {
            file = new File(file.toString().replace(".exe", ""));
        }
        return App.builder().label(getBranding(file)).file(file).build();
    }
}
