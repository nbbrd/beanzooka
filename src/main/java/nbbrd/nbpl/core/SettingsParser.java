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
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
public class SettingsParser {

    public static Settings parse(Path file) throws IOException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        try (Reader reader = Files.newBufferedReader(file)) {
            XMLStreamReader xml = factory.createXMLStreamReader(reader);
            try {
                return parseSettings(xml);
            } finally {
                xml.close();
            }
        }
    }

    private static Settings parseSettings(XMLStreamReader xml) throws XMLStreamException {
        Settings.Builder result = Settings.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case "config":
                            result.config(parseConfig(xml));
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

    private static Config parseConfig(XMLStreamReader xml) throws XMLStreamException {
        Config.Builder result = Config.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case "label":
                            result.label(xml.getElementText());
                            break;
                        case "javaFile":
                            result.javaFile(new File(xml.getElementText()));
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
                    if (xml.getLocalName().equals("config")) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private static App parseApp(XMLStreamReader xml) throws XMLStreamException {
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

    private static UserDir parseUserDir(XMLStreamReader xml) throws XMLStreamException {
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

    private static Plugin parsePlugin(XMLStreamReader xml) throws XMLStreamException {
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
