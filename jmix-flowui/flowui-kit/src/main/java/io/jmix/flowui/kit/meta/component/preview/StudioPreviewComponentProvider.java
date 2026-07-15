/*
 * Copyright 2026 Haulmont.
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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.meta.StudioAPI;
import org.jspecify.annotations.Nullable;
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
 * Do not rename or refactor.
 * <p>
 *     Finds a suitable {@link StudioPreviewComponentLoader loader} for component tag
 *     and invoke {@link StudioPreviewComponentLoader#load(Element, Element) load method}
 * </p>
 */
@StudioAPI
@SuppressWarnings("unused")
final class StudioPreviewComponentProvider {

    private static final Set<StudioPreviewComponentLoader> loaders = new LinkedHashSet<>();
    private static final Lock loaderInitializationLock = new ReentrantLock();
    private static final Condition lockCondition = loaderInitializationLock.newCondition();

    private static final Set<StudioPreviewComponentProcessor> processors = new LinkedHashSet<>();
    private static final Lock processorInitializationLock = new ReentrantLock();
    private static final Condition processorLockCondition = processorInitializationLock.newCondition();

    /**
     * Used in Studio.
     *
     * @param tagLocalName xml tag name without namespace.
     */
    static boolean canCreateComponent(String tagLocalName, String namespaceUri) {
        final Element element = new BaseElement(tagLocalName, Namespace.get(namespaceUri));
        return findComponentLoader(element).isPresent();
    }

    /**
     * Used in Studio. Attaches {@code child} to {@code parent} using the first registered
     * {@link StudioPreviewChildProcessor} that supports {@code parent}'s type.
     *
     * @param index target index, or a negative value to append
     * @return {@code true} if a processor handled the attachment; {@code false} if none did
     */
    public static boolean addPreviewChild(Component parent, Component child, int index) {
        return findChildProcessor(parent)
                .map(processor -> processor.addChild(parent, child, index))
                .orElse(false);
    }

    /**
     * Used in Studio. Detaches {@code child} from {@code parent} using the first registered
     * {@link StudioPreviewChildProcessor} that supports {@code parent}'s type.
     *
     * @return {@code true} if a processor handled the detachment; {@code false} if none did
     */
    public static boolean removePreviewChild(Component parent, Component child) {
        return findChildProcessor(parent)
                .map(processor -> processor.removeChild(parent, child))
                .orElse(false);
    }

    /**
     * Used in Studio. Attaches {@code child} to {@code parent}'s {@code slotHint} slot (e.g.
     * {@code "prefix"}, {@code "navbar"}); distinct-arity overload of
     * {@link #addPreviewChild(Component, Component, int)} for slots a parent+child type pair alone
     * cannot disambiguate.
     *
     * @return {@code true} if a processor handled the attachment; {@code false} if none did
     */
    public static boolean addPreviewChild(Component parent, Component child, int index, String slotHint) {
        return dispatch(StudioPreviewSlotProcessor.class,
                processor -> processor.addToSlot(parent, child, index, slotHint));
    }

    /**
     * Used in Studio. Detaches {@code child} from {@code parent}'s {@code slotHint} slot;
     * distinct-arity overload of {@link #removePreviewChild(Component, Component)}.
     *
     * @return {@code true} if a processor handled the detachment; {@code false} if none did
     */
    public static boolean removePreviewChild(Component parent, Component child, String slotHint) {
        return dispatch(StudioPreviewSlotProcessor.class,
                processor -> processor.removeFromSlot(parent, child, slotHint));
    }

    /**
     * Used in Studio. Attaches {@code action} to a keyed action container (e.g. {@code HasActions})
     * at {@code index}. {@code action} is declared as {@code Object} because this frozen
     * reflection-ABI static may only declare JDK/Vaadin-typed parameters.
     *
     * @return {@code true} if a processor handled the attachment; {@code false} if none did
     *         (including when {@code action} is not actually a kit {@code Action})
     */
    public static boolean addPreviewAction(Component parent, Object action, int index) {
        return action instanceof Action realAction
                && dispatch(StudioPreviewActionProcessor.class,
                        processor -> processor.addAction(parent, realAction, index));
    }

    /**
     * Used in Studio. Detaches {@code action} from a keyed action container.
     *
     * @return {@code true} if a processor handled the detachment; {@code false} if none did
     */
    public static boolean removePreviewAction(Component parent, Object action) {
        return action instanceof Action realAction
                && dispatch(StudioPreviewActionProcessor.class,
                        processor -> processor.removeAction(parent, realAction));
    }

    /**
     * Used in Studio. Attaches {@code tab} (paired with its {@code content}) to a
     * {@code TabSheet}-like container at {@code index}.
     *
     * @return {@code true} if a processor handled the attachment; {@code false} if none did
     */
    public static boolean addPreviewTab(Component parent, Component tab, Component content, int index) {
        return dispatch(StudioPreviewTabProcessor.class,
                processor -> processor.addTab(parent, tab, content, index));
    }

    /**
     * Used in Studio. Detaches {@code tab} (and its paired content) from a {@code TabSheet}-like
     * container.
     *
     * @return {@code true} if a processor handled the detachment; {@code false} if none did
     */
    public static boolean removePreviewTab(Component parent, Component tab) {
        return dispatch(StudioPreviewTabProcessor.class, processor -> processor.removeTab(parent, tab));
    }

    /**
     * Used in Studio. Creates (or reuses, if already materialized at load time) the column
     * identified by {@code key} on a {@code Grid}-like container, placed at {@code index}. Studio
     * re-resolves the resulting column afterwards via {@code Grid#getColumnByKey(String)}.
     *
     * @return {@code true} if a processor handled the creation; {@code false} if none did
     */
    public static boolean addPreviewColumn(Component parent, String key, int index) {
        return dispatch(StudioPreviewColumnProcessor.class,
                processor -> processor.addColumn(parent, key, index));
    }

    /**
     * Used in Studio. Removes the column identified by {@code key} from a {@code Grid}-like
     * container.
     *
     * @return {@code true} if a processor handled the removal; {@code false} if none did
     */
    public static boolean removePreviewColumn(Component parent, String key) {
        return dispatch(StudioPreviewColumnProcessor.class, processor -> processor.removeColumn(parent, key));
    }

    /**
     * True when this framework builds full preview content (structure + placeholders) itself,
     * so Studio skips its own structural postInit. Absent on pre-capability frameworks.
     */
    public static boolean buildsFullPreviewContent() {
        return true;
    }

    /**
     * Used in Studio.
     * <p>
     *     Creates a preview component from {@link ComponentCreationContext creationContext}.
     * </p>
     */
    @Nullable
    static Component createComponent(ComponentCreationContext creationContext) {
        Element viewElement = getElement(creationContext.viewXml());
        if (hasQualifiedName(viewElement)) {
            Element componentElement = getComponentElement(viewElement, creationContext.componentPath());
            Optional<StudioPreviewComponentLoader> loader = findComponentLoader(componentElement);
            if (loader.isPresent()) {
                StudioPreviewEnvironment environment = unwrapEnvironment(creationContext.environment());
                return loader.get().load(componentElement, viewElement, environment);
            }
        }
        return null;
    }

    private static StudioPreviewEnvironment unwrapEnvironment(@Nullable Object environment) {
        return environment instanceof StudioPreviewEnvironment studioPreviewEnvironment
                ? studioPreviewEnvironment
                : StudioPreviewEnvironment.NOOP;
    }

    @Nullable
    private static Element getComponentElement(Element viewElement, String componentPath) {
        return (Element) viewElement.selectSingleNode(componentPath);
    }

    private static Optional<StudioPreviewComponentLoader> findComponentLoader(final Element element) {
        return getLoaderServices().stream().filter(loader -> loader.isSupported(element)).findFirst();
    }

    private static Optional<StudioPreviewChildProcessor> findChildProcessor(final Component parent) {
        return getProcessorServices().stream()
                .filter(StudioPreviewChildProcessor.class::isInstance)
                .map(StudioPreviewChildProcessor.class::cast)
                .filter(processor -> processor.isSupported(parent))
                .findFirst();
    }

    /**
     * Tries {@code action} on every registered processor implementing {@code role}, first {@code true} wins.
     */
    private static <P extends StudioPreviewComponentProcessor> boolean dispatch(Class<P> role, Predicate<P> action) {
        for (StudioPreviewComponentProcessor processor : getProcessorServices()) {
            if (role.isInstance(processor) && action.test(role.cast(processor))) {
                return true;
            }
        }
        return false;
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
                        .map(StudioPreviewComponentProvider::instantiateSafely)
                        .filter(Objects::nonNull)
                        .forEach(loaders::add);
                loaders.add(new StudioStandardComponentsPreviewLoader());
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

    private static Collection<StudioPreviewComponentProcessor> getProcessorServices() {
        if (processors.isEmpty()) {
            initProcessorServices();
        }
        return processors;
    }

    private static void initProcessorServices() {
        if (processorInitializationLock.tryLock()) {
            try {
                processors.clear();
                ClassLoader classLoader = StudioPreviewComponentProvider.class.getClassLoader();
                ServiceLoader.load(StudioPreviewComponentProcessor.class, classLoader).stream()
                        .map(StudioPreviewComponentProvider::instantiateSafely)
                        .filter(Objects::nonNull)
                        .forEach(processors::add);
            } finally {
                processorLockCondition.signalAll();
                processorInitializationLock.unlock();
            }
        } else {
            try {
                processorLockCondition.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                console("Exception when waiting processors initialization", e);
            }
        }
    }

    // A preview loader/processor from an outdated or incompatible add-on on the preview classloader
    // must not abort the whole preview; skip it (logged) and keep the rest.
    @Nullable
    static <T> T instantiateSafely(ServiceLoader.Provider<T> provider) {
        try {
            return provider.get();
        } catch (Throwable t) {
            console("Skipping preview SPI service that could not be instantiated", t);
            return null;
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
        private final Object environment;

        public ComponentCreationContext(String viewXml, String componentPath) {
            this(viewXml, componentPath, null);
        }

        public ComponentCreationContext(String viewXml, String componentPath, @Nullable Object environment) {
            this.viewXml = viewXml;
            this.componentPath = componentPath;
            this.environment = environment;
        }

        /**
         * XML of View descriptor.
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

        /**
         * Studio-side environment, expected to be a {@link StudioPreviewEnvironment}
         * (typed as {@code Object} so this constructor's lookup never depends on that interface class).
         */
        @Nullable
        public Object environment() {
            return environment;
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