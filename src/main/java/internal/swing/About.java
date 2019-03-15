/*
 * Copyright 2019 National Bank of Belgium
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
package internal.swing;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public class About {

    public static About lookup() {
        return builder()
                .application(lookupApplication())
                .java(lookupJava())
                .runtime(lookupRuntime())
                .system(lookupSystem())
                .build();
    }

    private String application;
    private String java;
    private String runtime;
    private String system;

    private static String lookupApplication() {
        return "Beanzooka " + ManifestVersionProvider.get().orElse("...");
    }

    private static String lookupJava() {
        return System.getProperty("java.version")
                + "; " + System.getProperty("java.vm.name")
                + " " + System.getProperty("java.vm.version");
    }

    private static String lookupRuntime() {
        return System.getProperty("java.runtime.name")
                + " " + System.getProperty("java.runtime.version");
    }

    private static String lookupSystem() {
        return System.getProperty("os.name")
                + " version " + System.getProperty("os.version")
                + " running on " + System.getProperty("os.arch")
                + "; " + System.getProperty("file.encoding")
                + "; " + System.getProperty("user.language") + "_" + System.getProperty("user.country");
    }
}
