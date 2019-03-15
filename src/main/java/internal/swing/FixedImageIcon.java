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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class FixedImageIcon implements Icon {

    @lombok.NonNull
    private final ImageIcon delegate;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        delegate.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return delegate.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return delegate.getIconHeight();
    }
}
