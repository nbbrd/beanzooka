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
package internal.swing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
@lombok.extern.java.Log
public final class ManifestVersionProvider {

    public Optional<String> get() {
        try (InputStream stream = ManifestVersionProvider.class.getResourceAsStream(MANIFEST_PATH)) {
            Manifest manifest = new Manifest(stream);
            Attributes attr = manifest.getMainAttributes();
            return Optional.ofNullable(attr.getValue(IMPL_VERSION_HEADER));
        } catch (IOException ex) {
            log.log(Level.INFO, "Unable to read manifest", ex);
        } catch (NullPointerException ex) {
            log.log(Level.INFO, "Missing manifest");
        }
        return Optional.empty();
    }

    private final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";
    private final String IMPL_VERSION_HEADER = "Implementation-Version";
}
