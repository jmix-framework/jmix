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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.meta.StudioAPI;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import org.jspecify.annotations.Nullable;
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
    private static boolean loadersInitialized;

    private static final Set<StudioPreviewComponentProcessor> processors = new LinkedHashSet<>();
    private static final Lock processorInitializationLock = new ReentrantLock();
    private static boolean processorsInitialized;

    // The protocol Studio speaks (see perform). Studio holds the same literals on its side - it can't
    // share these classes - so an operation is only ever added, never renamed.
    private static final String OP_CAN_CREATE_COMPONENT = "canCreateComponent";
    private static final String OP_CREATE_COMPONENT = "createComponent";
    private static final String OP_CAPABILITIES = "capabilities";
    private static final String OP_COMPONENT_ADDED = "componentAdded";
    private static final String OP_COMPONENT_REMOVED = "componentRemoved";

    private static final String PARAM_TARGET = "target";
    private static final String PARAM_CHILD = "child";
    private static final String PARAM_CONTENT = "content";
    private static final String PARAM_TAG = "tag";
    private static final String PARAM_ATTRIBUTES = "attributes";
    private static final String PARAM_INDEX = "index";
    private static final String PARAM_NAMESPACE = "namespace";
    private static final String PARAM_VIEW_XML = "viewXml";
    private static final String PARAM_COMPONENT_PATH = "componentPath";
    private static final String PARAM_ENVIRONMENT = "environment";

    private static final String CAPABILITY_FULL_CONTENT = "fullContent";

    // Attribute names the mutation classifier understands; the tag names it matches come from
    // StudioXmlElements and the slot names it returns from StudioPreviewSlotProcessor.
    private static final String ATTRIBUTE_KEY = "key";
    private static final String ATTRIBUTE_PROPERTY = "property";
    private static final String ATTRIBUTE_TOUCH_OPTIMIZED = "touchOptimized";

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
     * Used in Studio. The single entry point for the whole preview protocol: Studio names the
     * {@code operation} and passes its arguments by key in {@code params}, so a new operation, a new
     * argument or a new capability never changes this signature. This method and the operation
     * vocabulary below are the only things the two sides must agree on.
     * <p>
     * All {@code params} values are JDK- or Vaadin-typed (plus the kit {@code Action}) so they cross
     * the preview classloader boundary; a missing or wrongly-typed value counts as absent.
     * <ul>
     *   <li>{@code "canCreateComponent"} — {@code tag}, {@code namespace} ({@link String}); returns
     *       {@link Boolean}: some loader claims that element</li>
     *   <li>{@code "createComponent"} — {@code viewXml}, {@code componentPath} ({@link String}),
     *       optional {@code environment} (a {@link StudioPreviewEnvironment}); returns the built
     *       {@link Component}, or {@code null} when no loader claimed it</li>
     *   <li>{@code "capabilities"} — no arguments; returns a {@link Map} describing this
     *       implementation. Key {@code "fullContent"} ({@link Boolean}): loaders build all live
     *       preview content (supported XML properties, real structure and placeholders) themselves,
     *       so Studio does not mutate the created Vaadin components. An older Studio ignores keys
     *       it doesn't know</li>
     *   <li>{@code "componentAdded"} / {@code "componentRemoved"} — Studio reports the designer
     *       change without classifying it; this side decides whether it is a plain child, a slot, an
     *       action, a tab or a column (see {@link #classifyAdded}). Keys: {@code target}
     *       ({@link Component}, the mutated container), {@code child} (the live added/removed object
     *       — a {@link Component} or a kit {@code Action} — absent for columns), {@code content}
     *       ({@link Component}, a tab's content, present only for tabs), {@code tag} ({@link String},
     *       the child element's XML local name, when Studio knows it), {@code attributes}
     *       ({@link Map}{@code <String,String>}, the child element's XML attributes), {@code index}
     *       ({@link Integer}, negative or absent appends)</li>
     * </ul>
     *
     * @return the operation's result. {@code null} — the operation is unknown, an argument it cannot
     *         work without is missing, or no processor claimed the target; Studio then runs its own
     *         reflective path. A handled {@code "componentAdded"}/{@code "componentRemoved"} returns
     *         {@link Boolean#TRUE}, except a handled column add, which returns the created
     *         {@code Grid.Column} so Studio binds to it directly.
     */
    @Nullable
    public static Object perform(@Nullable String operation, @Nullable Map<String, Object> params) {
        if (operation == null) {
            return null;
        }
        return switch (operation) {
            case OP_CAN_CREATE_COMPONENT -> canCreate(params);
            case OP_CREATE_COMPONENT -> create(params);
            case OP_CAPABILITIES -> Map.of(CAPABILITY_FULL_CONTENT, true);
            case OP_COMPONENT_ADDED -> componentAdded(params);
            case OP_COMPONENT_REMOVED -> componentRemoved(params);
            default -> null;
        };
    }

    private static boolean canCreate(@Nullable Map<String, Object> params) {
        String tag = param(params, PARAM_TAG, String.class);
        String namespace = param(params, PARAM_NAMESPACE, String.class);
        return tag != null && namespace != null && canCreateComponent(tag, namespace);
    }

    @Nullable
    private static Component create(@Nullable Map<String, Object> params) {
        String viewXml = param(params, PARAM_VIEW_XML, String.class);
        String componentPath = param(params, PARAM_COMPONENT_PATH, String.class);
        if (viewXml == null || componentPath == null) {
            return null;
        }
        Object environment = params == null ? null : params.get(PARAM_ENVIRONMENT);
        return createComponent(new ComponentCreationContext(viewXml, componentPath, environment));
    }

    @Nullable
    private static Object componentAdded(@Nullable Map<String, Object> params) {
        Mutation mutation = Mutation.of(params);
        if (mutation == null) {
            return null;
        }
        return switch (classifyAdded(mutation)) {
            case ACTION -> dispatch(StudioPreviewActionProcessor.class,
                    processor -> processor.addAction(mutation.target, (Action) mutation.child, mutation.index))
                    ? Boolean.TRUE : null;
            case COLUMN -> dispatch(StudioPreviewColumnProcessor.class,
                    processor -> processor.addColumn(mutation.target, mutation.columnKey(), mutation.index))
                    ? resolveColumn(mutation.target, mutation.columnKey()) : null;
            case TAB -> dispatch(StudioPreviewTabProcessor.class,
                    processor -> processor.addTab(mutation.target, (Component) mutation.child,
                            mutation.content, mutation.index))
                    ? Boolean.TRUE : null;
            case SLOT -> dispatch(StudioPreviewSlotProcessor.class,
                    processor -> processor.addToSlot(mutation.target, (Component) mutation.child,
                            mutation.index, mutation.slot()))
                    ? Boolean.TRUE : null;
            case CHILD -> findChildProcessor(mutation.target)
                    .map(processor -> processor.addChild(mutation.target, (Component) mutation.child, mutation.index))
                    .orElse(false)
                    ? Boolean.TRUE : null;
            case NONE -> null;
        };
    }

    @Nullable
    private static Object componentRemoved(@Nullable Map<String, Object> params) {
        Mutation mutation = Mutation.of(params);
        if (mutation == null) {
            return null;
        }
        boolean handled = switch (classifyRemoved(mutation)) {
            case ACTION -> dispatch(StudioPreviewActionProcessor.class,
                    processor -> processor.removeAction(mutation.target, (Action) mutation.child));
            case COLUMN -> dispatch(StudioPreviewColumnProcessor.class,
                    processor -> processor.removeColumn(mutation.target, mutation.columnKey()));
            case TAB -> dispatch(StudioPreviewTabProcessor.class,
                    processor -> processor.removeTab(mutation.target, (Component) mutation.child));
            case SLOT -> dispatch(StudioPreviewSlotProcessor.class,
                    processor -> processor.removeFromSlot(mutation.target, (Component) mutation.child,
                            mutation.removalSlot()));
            case CHILD -> findChildProcessor(mutation.target)
                    .map(processor -> processor.removeChild(mutation.target, (Component) mutation.child))
                    .orElse(false);
            case NONE -> false;
        };
        return handled ? Boolean.TRUE : null;
    }

    private enum MutationKind {ACTION, COLUMN, TAB, SLOT, CHILD, NONE}

    /**
     * Decides what a reported designer change actually is — the classification Studio used to do.
     * Order matters: an {@code Action} is never a {@code Component}; a column carries no live child
     * at all (only its identity attributes); only tabs carry {@code content}; slots are recognized
     * by the child element's tag ({@code prefix}/{@code suffix}, AppLayout's parts).
     */
    private static MutationKind classifyAdded(Mutation mutation) {
        if (mutation.child instanceof Action) {
            return MutationKind.ACTION;
        }
        if (mutation.child == null) {
            return mutation.columnKey() != null ? MutationKind.COLUMN : MutationKind.NONE;
        }
        if (!(mutation.child instanceof Component)) {
            return MutationKind.NONE;
        }
        if (mutation.content != null) {
            return MutationKind.TAB;
        }
        return mutation.slot() != null ? MutationKind.SLOT : MutationKind.CHILD;
    }

    private static MutationKind classifyRemoved(Mutation mutation) {
        if (mutation.child instanceof Action) {
            return MutationKind.ACTION;
        }
        if (mutation.columnKey() != null && !(mutation.child instanceof Component)) {
            return MutationKind.COLUMN;
        }
        if (!(mutation.child instanceof Component)) {
            return MutationKind.NONE;
        }
        if (StudioXmlElements.TAB.equals(mutation.tag)) {
            return MutationKind.TAB;
        }
        return mutation.removalSlot() != null ? MutationKind.SLOT : MutationKind.CHILD;
    }

    /**
     * The arguments of a {@code componentAdded}/{@code componentRemoved} report, as far as Studio
     * knows them: the live objects it holds plus the child element's XML identity.
     */
    private static final class Mutation {
        final Component target;
        @Nullable
        final Object child;
        @Nullable
        final Component content;
        @Nullable
        final String tag;
        final Map<?, ?> attributes;
        final int index;

        private Mutation(Component target, @Nullable Object child, @Nullable Component content,
                         @Nullable String tag, Map<?, ?> attributes, int index) {
            this.target = target;
            this.child = child;
            this.content = content;
            this.tag = tag;
            this.attributes = attributes;
            this.index = index;
        }

        @Nullable
        static Mutation of(@Nullable Map<String, Object> params) {
            Component target = param(params, PARAM_TARGET, Component.class);
            if (params == null || target == null) {
                return null;
            }

            Map<?, ?> attributes = param(params, PARAM_ATTRIBUTES, Map.class);

            return new Mutation(
                    target,
                    params.get(PARAM_CHILD),
                    param(params, PARAM_CONTENT, Component.class),
                    param(params, PARAM_TAG, String.class),
                    attributes == null ? Map.of() : attributes,
                    index(params));
        }

        /**
         * The column identity, derived the same way the preview loaders do: {@code key} attribute,
         * else {@code property}, else the tag name for a bare {@code <editorActionsColumn/>}.
         */
        @Nullable
        String columnKey() {
            Object key = attributes.get(ATTRIBUTE_KEY);
            if (key instanceof String stringKey && !stringKey.isBlank()) {
                return stringKey;
            }
            Object property = attributes.get(ATTRIBUTE_PROPERTY);
            if (property instanceof String stringProperty && !stringProperty.isBlank()) {
                return stringProperty;
            }
            return StudioXmlElements.EDITOR_ACTIONS_COLUMN.equals(tag)
                    ? StudioXmlElements.EDITOR_ACTIONS_COLUMN : null;
        }

        /**
         * The slot the child element's tag names, or {@code null} for a plain child. A
         * touch-optimized navigation bar stays {@code null}: its Vaadin attach needs the
         * {@code touchOptimized} flag this SPI's slot processors don't model, so Studio's own
         * reflective path handles it.
         */
        @Nullable
        String slot() {
            if (tag == null) {
                return null;
            }
            return switch (tag) {
                case StudioXmlElements.PREFIX -> StudioPreviewSlotProcessor.PREFIX_SLOT;
                case StudioXmlElements.SUFFIX -> StudioPreviewSlotProcessor.SUFFIX_SLOT;
                case StudioXmlElements.NAVIGATION_BAR ->
                        Boolean.parseBoolean(String.valueOf(attributes.get(ATTRIBUTE_TOUCH_OPTIMIZED)))
                                ? null : StudioPreviewSlotProcessor.NAVBAR_SLOT;
                case StudioXmlElements.DRAWER_LAYOUT -> StudioPreviewSlotProcessor.DRAWER_SLOT;
                case StudioXmlElements.INITIAL_LAYOUT, StudioXmlElements.LAYOUT, StudioXmlElements.WORK_AREA ->
                        target instanceof AppLayout ? StudioPreviewSlotProcessor.CONTENT_SLOT : null;
                default -> null;
            };
        }

        /**
         * [slot()] plus the remove-only default: Studio's AppLayout removal reports no tag, and the
         * pre-classifier protocol always removed through the {@code content} slot. Never applied to
         * adds — a freshly dropped component has no XML element yet either, and defaulting an add
         * to {@code content} would replace the AppLayout's work area.
         */
        @Nullable
        String removalSlot() {
            String slot = slot();
            if (slot != null) {
                return slot;
            }
            return tag == null && target instanceof AppLayout ? StudioPreviewSlotProcessor.CONTENT_SLOT : null;
        }
    }

    /**
     * Re-resolves the column the processor just created (or reused) so Studio can bind to it
     * directly instead of doing its own {@code getColumnByKey} round-trip.
     */
    @Nullable
    private static Object resolveColumn(Component target, String key) {
        return target instanceof Grid<?> grid ? grid.getColumnByKey(key) : Boolean.TRUE;
    }

    @Nullable
    private static <T> T param(@Nullable Map<String, Object> params, String key, Class<T> type) {
        Object value = params == null ? null : params.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /**
     * The {@code index} param, defaulting to append when absent or not an {@link Integer}.
     */
    private static int index(@Nullable Map<String, Object> params) {
        Integer index = param(params, PARAM_INDEX, Integer.class);
        return index != null ? index : -1;
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
        if (viewElement != null && hasQualifiedName(viewElement)) {
            Element componentElement = getComponentElement(viewElement, creationContext.componentPath());
            Optional<StudioPreviewComponentLoader> loaderOpt = findComponentLoader(componentElement);
            if (loaderOpt.isPresent()) {
                StudioPreviewEnvironment environment = unwrapEnvironment(creationContext.environment());
                StudioPreviewComponentLoader loader = loaderOpt.get();
                return loader.load(componentElement, viewElement, environment);
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
        loaderInitializationLock.lock();
        try {
            if (!loadersInitialized) {
                addServicesResiliently(ServiceLoader.load(StudioPreviewComponentLoader.class,
                        StudioPreviewComponentProvider.class.getClassLoader()).iterator(), loaders);
                loaders.add(new StudioStandardComponentsPreviewLoader());
                loadersInitialized = true;
            }
            return loaders;
        } finally {
            loaderInitializationLock.unlock();
        }
    }

    private static Collection<StudioPreviewComponentProcessor> getProcessorServices() {
        processorInitializationLock.lock();
        try {
            if (!processorsInitialized) {
                addServicesResiliently(ServiceLoader.load(StudioPreviewComponentProcessor.class,
                        StudioPreviewComponentProvider.class.getClassLoader()).iterator(), processors);
                processorsInitialized = true;
            }
            return processors;
        } finally {
            processorInitializationLock.unlock();
        }
    }

    // A loader/processor from an outdated or incompatible add-on on the preview classloader must not
    // abort the whole preview. ServiceLoader throws ServiceConfigurationError (wrapping e.g.
    // NoClassDefFoundError) when a service can't be loaded or instantiated - during either advancement
    // (hasNext) or instantiation (next); log it, skip it, and keep going (the iterator makes a best
    // effort to reach the next provider).
    static <T> void addServicesResiliently(Iterator<T> serviceIterator, Set<? super T> target) {
        while (true) {
            try {
                if (!serviceIterator.hasNext()) {
                    break;
                }
                target.add(serviceIterator.next());
            } catch (ServiceConfigurationError e) {
                console("Skipping preview SPI service that could not be loaded", e);
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

    /**
     * These features are best-effort hardening: a parser that doesn't recognise one is fine, and a
     * reader is built per parsed component, so logging each miss floods the Studio log with hundreds
     * of identical lines per preview render.
     */
    private static void setParserFeature(final XMLReader reader,
                                         final String featureName,
                                         final boolean value) {
        try {
            reader.setFeature(featureName, value);
        } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
            // ignored on purpose
        }
    }

    private static void console(String text, @Nullable Throwable throwable) {
        // toString() carries the exception class and message: for a skipped SPI service that is the
        // only place naming the failing loader and the class it couldn't link against.
        System.out.println(text + ": " + throwable + " " + Arrays.toString(throwable.getStackTrace()));
    }
}
