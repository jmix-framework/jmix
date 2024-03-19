/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.kit.meta.component.preview;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.vaadin.flow.component.Component;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.BaseElement;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;

/**
 * Used in Studio.
 * <p>
 *     Finds a suitable {@link StudioPreviewComponentLoader loader} for component tag
 *     and invoke {@link StudioPreviewComponentLoader#load(Element, Element) load method}
 * </p>
 */
@SuppressWarnings("unused")
final class StudioPreviewComponentProvider {

    private static final Set<StudioPreviewComponentLoader> loaders = new HashSet<>();
    private static final Lock loaderInitializationLock = new ReentrantLock();
    private static final Condition lockCondition = loaderInitializationLock.newCondition();

    /**
     * Used in Studio.
     */
    static boolean canCreateComponent(String tagLocaleName, String namespaceUri) {
        final Element element = new BaseElement(tagLocaleName, Namespace.get(namespaceUri));
        return findComponentLoader(element).isPresent();
    }

    /**
     * Used in Studio.
     * <p>
     *     Creates a preview component from {@link ComponentCreationContext creationContext}.
     * </p>
     */
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    static Component createComponent(ComponentCreationContext creationContext) {
        Element viewElement = getElement(creationContext.viewXml());
        if (hasQualifiedName(viewElement)) {
            Element componentElement = getComponentElement(viewElement, creationContext.componentPath());
            Optional<StudioPreviewComponentLoader> loader = findComponentLoader(componentElement);
            if (loader.isPresent()) {
                return loader.get().load(componentElement, viewElement);
            }
        }
        return null;
    }

    @Nullable
    private static Element getComponentElement(Element viewElement, String componentPath) {
        return (Element) viewElement.selectSingleNode(componentPath);
    }

    private static Optional<StudioPreviewComponentLoader> findComponentLoader(final Element element) {
        return getLoaderServices().stream().filter(loader -> loader.isSuitable(element)).findFirst();
    }

    private static Collection<StudioPreviewComponentLoader> getLoaderServices() {
        if (loaders.isEmpty()) {
            initLoaderServices();
        }
        return loaders;
    }

    private static void initLoaderServices() {
        if (loaderInitializationLock.tryLock()) {
            try {
                loaders.clear();
                ClassLoader classLoader = StudioPreviewComponentProvider.class.getClassLoader();
                ServiceLoader.load(StudioPreviewComponentLoader.class, classLoader).stream()
                        .map(ServiceLoader.Provider::get)
                        .forEach(loaders::add);
            } finally {
                lockCondition.signalAll();
                loaderInitializationLock.unlock();
            }
        } else {
            try {
                lockCondition.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                console("Exception when waiting loaders initialization", e);
            }
        }
    }

    @Nullable
    private static Element getElement(@Nullable final String xml) {
        Document document = readDocument(xml);
        if (document != null) {
            return document.getRootElement();
        } else {
            return null;
        }
    }

    @Nullable
    private static Document readDocument(@Nullable final String xml) {
        try {
            SAXReader reader = getSaxReader();
            if (xml != null && reader != null && isNoneBlank(xml)) {
                return reader.read(new StringReader(xml));
            }
        } catch (DocumentException e) {
            console("Can not read document", e);
        }
        return null;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class ComponentCreationContext {
        private final String viewXml;
        private final String componentPath;

        public ComponentCreationContext(String viewXml, String componentPath) {
            this.viewXml = viewXml;
            this.componentPath = componentPath;
        }

        /**
         * Xml of View descriptor.
         */
        public String viewXml() {
            return viewXml;
        }

        /**
         * Component's unique XPath.
         */
        public String componentPath() {
            return componentPath;
        }
    }

    private static boolean hasQualifiedName(final Element... elements) {
        return Arrays.stream(elements)
                .allMatch(e -> e != null && isNoneBlank(e.getQualifiedName()));
    }

    private static SAXReader getSaxReader() {
        try {
            SAXParser parser = getParser();
            if (parser != null) {
                return new SAXReader(parser.getXMLReader());
            }
        } catch (SAXException e) {
            console("Can not create SAXReader", e);
        }
        return null;
    }

    private static SAXParser getParser() {
        SAXParser parser;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        XMLReader xmlReader;
        try {
            parser = factory.newSAXParser();
            xmlReader = parser.getXMLReader();
        } catch (ParserConfigurationException | SAXException e) {
            console("Can not create SAXParser", e);
            return null;
        }

        setParserFeature(xmlReader, "http://xml.org/sax/features/namespaces", true);
        setParserFeature(xmlReader, "http://xml.org/sax/features/namespace-prefixes", false);

        // external entites
        setParserFeature(xmlReader, "http://xml.org/sax/properties/external-general-entities", false);
        setParserFeature(xmlReader, "http://xml.org/sax/properties/external-parameter-entities", false);

        // external DTD
        setParserFeature(xmlReader, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        // use Locator2 if possible
        setParserFeature(xmlReader, "http://xml.org/sax/features/use-locator2", true);

        return parser;
    }

    private static void setParserFeature(final XMLReader reader,
                                         final String featureName,
                                         final boolean value) {
        try {
            reader.setFeature(featureName, value);
        } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
            console("Can not set feature for XMLReader:\n", e);
        }
    }

    private static void console(String text) {
        console(text, null);
    }

    private static void console(String text, @Nullable Throwable throwable) {
        final String stacktrace;
        if (throwable != null) {
            stacktrace = Arrays.toString(throwable.getStackTrace());
        } else {
            stacktrace = "";
        }
        System.out.print(text + StringUtils.defaultString(stacktrace));
    }
}