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
package nbbrd.nbpl.io;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import nbbrd.nbpl.core.App;
import nbbrd.nbpl.core.Jdk;
import nbbrd.nbpl.core.Plugin;
import nbbrd.nbpl.core.Resources;
import nbbrd.nbpl.core.UserDir;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class XmlResources {

    @Nonnull
    public Resources parse(@Nonnull Path file) throws IOException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        try (Reader reader = Files.newBufferedReader(file)) {
            XMLStreamReader xml = factory.createXMLStreamReader(reader);
            try {
                return parseResources(xml);
            } finally {
                xml.close();
            }
        }
    }

    private Resources parseResources(XMLStreamReader xml) throws XMLStreamException {
        Resources.Builder result = Resources.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case "jdk":
                            result.jdk(parseJdk(xml));
                            break;
                        case "app":
                            result.app(parseApp(xml));
                            break;
                        case "userDir":
                            result.userDir(parseUserDir(xml));
                            break;
                        case "plugin":
                            result.plugin(parsePlugin(xml));
                            break;
                    }
                    break;
            }
        }
        return result.build();
    }

    private Jdk parseJdk(XMLStreamReader xml) throws XMLStreamException {
        Jdk.Builder result = Jdk.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case "label":
                            result.label(xml.getElementText());
                            break;
                        case "javaHome":
                            result.javaHome(new File(xml.getElementText()));
                            break;
                        case "options":
                            result.options(xml.getElementText());
                            break;
                        case "clusters":
                            result.clusters(xml.getElementText());
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals("jdk")) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private App parseApp(XMLStreamReader xml) throws XMLStreamException {
        App.Builder result = App.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case "label":
                            result.label(xml.getElementText());
                            break;
                        case "file":
                            result.file(new File(xml.getElementText()));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals("app")) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private UserDir parseUserDir(XMLStreamReader xml) throws XMLStreamException {
        UserDir.Builder result = UserDir.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case "label":
                            result.label(xml.getElementText());
                            break;
                        case "folder":
                            result.folder(new File(xml.getElementText()));
                            break;
                        case "clone":
                            result.clone(Boolean.parseBoolean(xml.getElementText()));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals("userDir")) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private Plugin parsePlugin(XMLStreamReader xml) throws XMLStreamException {
        Plugin.Builder result = Plugin.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case "label":
                            result.label(xml.getElementText());
                            break;
                        case "file":
                            result.file(new File(xml.getElementText()));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals("plugin")) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }
}
