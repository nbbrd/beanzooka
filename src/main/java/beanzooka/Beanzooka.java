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
package beanzooka;

import beanzooka.swing.MainPanel;
import ec.util.various.swing.BasicSwingLauncher;
import internal.swing.ManifestVersionProvider;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ImageIcon;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Beanzooka {

    public static void main(String args[]) {
        File resources = args.length == 1 ? new File(args[0]) : null;

        new BasicSwingLauncher()
                .content(() -> createContent(resources))
                .title("Beanzooka " + ManifestVersionProvider.get().orElse("..."))
                .icons(Beanzooka::getIcons)
                .size(600, 400)
                .launch();
    }

    private Component createContent(File resources) {
        MainPanel result = new MainPanel();
        if (resources != null) {
            result.open(resources);
        }
        return result;
    }

    private List<? extends Image> getIcons() {
        return IntStream.of(256, 128, 64, 48, 32, 16)
                .mapToObj(size -> "beanzooka_redux_" + size + ".png")
                .map(Beanzooka.class::getResource)
                .map(ImageIcon::new)
                .map(ImageIcon::getImage)
                .collect(Collectors.toList());
    }
}
