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
import ec.util.various.swing.FontAwesome;
import java.awt.Color;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Beanzooka {

    public static void main(String args[]) {
        new BasicSwingLauncher()
                .content(MainPanel.class)
                .title("Beanzooka")
                .icons(() -> FontAwesome.FA_ROCKET.getImages(Color.DARK_GRAY, 16f, 32f, 64f))
                .size(600, 400)
                .launch();
    }
}
