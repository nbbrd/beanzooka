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
package beanzooka.io;

import beanzooka.core.*;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class XmlResources {

    public static final Xml.Parser<Resources> PARSER = Stax.StreamParser.valueOf(XmlResources::readResources);
    public static final Xml.Formatter<Resources> FORMATTER = Stax.StreamFormatter.valueOf(XmlResources::writeResources);

    @NonNull
    public Resources read(@NonNull Path file) throws IOException {
        return PARSER.parsePath(file);
    }

    public void write(@NonNull Path file, @NonNull Resources resources) throws IOException, XMLStreamException {
        FORMATTER.formatPath(resources, file);
    }

    private Resources readResources(XMLStreamReader xml) throws XMLStreamException {
        Resources.Builder result = Resources.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case JDK_TAG:
                            result.jdk(readJdk(xml));
                            break;
                        case APP_TAG:
                            result.app(readApp(xml));
                            break;
                        case USER_DIR_TAG:
                            result.userDir(readUserDir(xml));
                            break;
                        case PLUGIN_TAG:
                            result.plugin(readPlugin(xml));
                            break;
                    }
                    break;
            }
        }
        return result.build();
    }

    private Jdk readJdk(XMLStreamReader xml) throws XMLStreamException {
        Jdk.Builder result = Jdk.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case LABEL_TAG:
                            result.label(xml.getElementText());
                            break;
                        case JAVA_HOME_TAG:
                            result.javaHome(new File(xml.getElementText()));
                            break;
                        case OPTIONS_TAG:
                            result.options(xml.getElementText());
                            break;
                        case CLUSTERS_TAG:
                            result.clusters(readClusters(xml));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals(JDK_TAG)) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private List<File> readClusters(XMLStreamReader xml) throws XMLStreamException {
        List<File> result = new ArrayList<>();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case CLUSTER_TAG:
                            result.add(new File(xml.getElementText()));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals(CLUSTERS_TAG)) {
                        return result;
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private App readApp(XMLStreamReader xml) throws XMLStreamException {
        App.Builder result = App.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case LABEL_TAG:
                            result.label(xml.getElementText());
                            break;
                        case FILE_TAG:
                            result.file(new File(xml.getElementText()));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals(APP_TAG)) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private UserDir readUserDir(XMLStreamReader xml) throws XMLStreamException {
        UserDir.Builder result = UserDir.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case LABEL_TAG:
                            result.label(xml.getElementText());
                            break;
                        case FOLDER_TAG:
                            result.folder(new File(xml.getElementText()));
                            break;
                        case CLONE_TAG:
                            result.clone(Boolean.parseBoolean(xml.getElementText()));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals(USER_DIR_TAG)) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private Plugin readPlugin(XMLStreamReader xml) throws XMLStreamException {
        Plugin.Builder result = Plugin.builder();
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (xml.getLocalName()) {
                        case LABEL_TAG:
                            result.label(xml.getElementText());
                            break;
                        case FILE_TAG:
                            result.file(new File(xml.getElementText()));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (xml.getLocalName().equals(PLUGIN_TAG)) {
                        return result.build();
                    }
                    break;
            }
        }
        throw new RuntimeException();
    }

    private void writeResources(Resources resources, XMLStreamWriter xml) throws XMLStreamException {
        xml.writeStartElement(RESOURCES_TAG);
        writeList(xml, JDKS_TAG, resources.getJdks(), XmlResources::writeJdk);
        writeList(xml, APPS_TAG, resources.getApps(), XmlResources::writeApp);
        writeList(xml, USER_DIRS_TAG, resources.getUserDirs(), XmlResources::writeUserDir);
        writeList(xml, PLUGINS_TAG, resources.getPlugins(), XmlResources::writePlugin);
        xml.writeEndElement();
    }

    private void writeJdk(XMLStreamWriter xml, Jdk item) throws XMLStreamException {
        xml.writeStartElement(JDK_TAG);
        writeValue(xml, LABEL_TAG, item.getLabel());
        writeValue(xml, JAVA_HOME_TAG, item.getJavaHome().toString());
        writeValue(xml, OPTIONS_TAG, item.getOptions());
        writeList(xml, CLUSTERS_TAG, item.getClusters(), XmlResources::writeCluster);
        xml.writeEndElement();
    }

    private void writeCluster(XMLStreamWriter xml, File item) throws XMLStreamException {
        writeValue(xml, CLUSTER_TAG, item.getPath());
    }

    private void writeApp(XMLStreamWriter xml, App item) throws XMLStreamException {
        xml.writeStartElement(APP_TAG);
        writeValue(xml, LABEL_TAG, item.getLabel());
        writeValue(xml, FILE_TAG, item.getFile().toString());
        xml.writeEndElement();
    }

    private void writeUserDir(XMLStreamWriter xml, UserDir item) throws XMLStreamException {
        xml.writeStartElement(USER_DIR_TAG);
        writeValue(xml, LABEL_TAG, item.getLabel());
        writeValue(xml, FOLDER_TAG, item.getFolder().toString());
        writeValue(xml, CLONE_TAG, Boolean.toString(item.isClone()));
        xml.writeEndElement();
    }

    private void writePlugin(XMLStreamWriter xml, Plugin item) throws XMLStreamException {
        xml.writeStartElement(PLUGIN_TAG);
        writeValue(xml, LABEL_TAG, item.getLabel());
        writeValue(xml, FILE_TAG, item.getFile().toString());
        xml.writeEndElement();
    }

    private <T> void writeList(XMLStreamWriter xml, String name, List<T> list, XmlWriter<T> writer) throws XMLStreamException {
        xml.writeStartElement(name);
        for (T item : list) {
            writer.write(xml, item);
        }
        xml.writeEndElement();
    }

    private void writeValue(XMLStreamWriter xml, String name, String value) throws XMLStreamException {
        xml.writeStartElement(name);
        xml.writeCharacters(value);
        xml.writeEndElement();
    }

    private interface XmlWriter<T> {

        void write(XMLStreamWriter writer, T item) throws XMLStreamException;
    }

    private static final String PLUGINS_TAG = "plugins";
    private static final String USER_DIRS_TAG = "userDirs";
    private static final String APPS_TAG = "apps";
    private static final String JDKS_TAG = "jdks";
    private static final String RESOURCES_TAG = "resources";
    private static final String CLUSTERS_TAG = "clusters";
    private static final String CLUSTER_TAG = "cluster";
    private static final String OPTIONS_TAG = "options";
    private static final String JAVA_HOME_TAG = "javaHome";
    private static final String LABEL_TAG = "label";
    private static final String JDK_TAG = "jdk";
    private static final String FILE_TAG = "file";
    private static final String CLONE_TAG = "clone";
    private static final String USER_DIR_TAG = "userDir";
    private static final String FOLDER_TAG = "folder";
    private static final String PLUGIN_TAG = "plugin";
    private static final String APP_TAG = "app";
}
