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

import nbbrd.io.sys.SystemProperties;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public class About {

    public static About lookup() {
        SystemProperties props = SystemProperties.DEFAULT;
        return builder()
                .application(lookupApplication())
                .java(lookupJava(props))
                .runtime(lookupRuntime(props))
                .system(lookupSystem(props))
                .build();
    }

    String application;
    String java;
    String runtime;
    String system;

    private static String lookupApplication() {
        return "Beanzooka " + ManifestVersionProvider.get().orElse("...");
    }

    private static String lookupJava(SystemProperties p) {
        return p.getJavaVersion()
                + "; " + p.getJavaVmName()
                + " " + p.getJavaVmVersion();
    }

    private static String lookupRuntime(SystemProperties p) {
        return System.getProperty("java.runtime.name")
                + " " + System.getProperty("java.runtime.version");
    }

    private static String lookupSystem(SystemProperties p) {
        return p.getOsName()
                + " version " + p.getOsVersion()
                + " running on " + p.getOsArch()
                + "; " + System.getProperty("file.encoding")
                + "; " + System.getProperty("user.language") + "_" + System.getProperty("user.country");
    }
}
