/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import io.jmix.core.*;
import io.jmix.core.common.util.Dom4j;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.Window;
import io.jmix.ui.navigation.RouteDefinition;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.*;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GenericUI class holding information about all registered in <code>screens.xml</code> screens.
 */
@Component("ui_WindowConfig")
public class WindowConfig {

    public static final String WINDOW_CONFIG_XML_PROP = "jmix.ui.window-config";

    public static final Pattern ENTITY_SCREEN_PATTERN = Pattern.compile("([A-Za-z0-9]+[$_][A-Z][_A-Za-z0-9]*)\\..+");

    protected static final List<String> LOGIN_SCREEN_IDS = ImmutableList.of("login", "loginWindow");
    protected static final List<String> MAIN_SCREEN_IDS = ImmutableList.of("main", "mainWindow");

    private final Logger log = LoggerFactory.getLogger(WindowConfig.class);

    protected Map<String, WindowInfo> screens = new HashMap<>();
    // route -> screen id
    protected BiMap<String, String> routes = HashBiMap.create();

    protected Map<Class, WindowInfo> primaryEditors = new HashMap<>();
    protected Map<Class, WindowInfo> primaryLookups = new HashMap<>();

    protected List<UiControllersConfiguration> configurations;

    @Autowired
    protected Resources resources;
    @Autowired
    protected ClassManager classManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected ScreenXmlLoader screenXmlLoader;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected Environment environment;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected AnnotationScanMetadataReaderFactory metadataReaderFactory;
    @Autowired
    protected JmixModules modules;
    @Autowired
    protected UiControllersConfigurationSorter uiControllersConfigurationSorter;

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    protected WindowAttributesProvider windowAttributesProvider = new WindowAttributesProvider() {
        @Override
        public WindowInfo.Type getType(WindowInfo windowInfo) {
            return resolveWindowInfo(windowInfo).getType();
        }

        @Nullable
        @Override
        public String getTemplate(WindowInfo windowInfo) {
            return resolveWindowInfo(windowInfo).getTemplate();
        }

        @Override
        public Class<? extends FrameOwner> getControllerClass(WindowInfo windowInfo) {
            return resolveWindowInfo(windowInfo).getControllerClass();
        }

        @Override
        public WindowInfo resolve(WindowInfo windowInfo) {
            return resolveWindowInfo(windowInfo);
        }
    };

    @Autowired
    public void setConfigurations(List<UiControllersConfiguration> configurations) {
        this.configurations = configurations;
    }

    @PostConstruct
    protected void postConstruct() {
        //sort UiControllersConfiguration list in the same order as Jmix modules. In this case screens overridden
        //in add-ons or application will replace original screen definitions
        this.configurations = uiControllersConfigurationSorter.sort(this.configurations);
    }

    protected WindowInfo resolveWindowInfo(WindowInfo windowInfo) {
        Class<? extends FrameOwner> controllerClass;
        String template;

        if (windowInfo.getDescriptor() != null) {
            String className = windowInfo.getDescriptor().attributeValue("class");

            if (Strings.isNullOrEmpty(className)) {
                template = windowInfo.getDescriptor().attributeValue("template");

                Element screenXml = screenXmlLoader.load(template,
                        windowInfo.getId(), Collections.emptyMap());
                className = screenXml.attributeValue("class");
            } else {
                template = null;
            }

            controllerClass = loadDefinedScreenClass(className);

        } else if (windowInfo.getControllerClassName() != null) {
            controllerClass = loadDefinedScreenClass(windowInfo.getControllerClassName());

            UiDescriptor annotation = controllerClass.getAnnotation(UiDescriptor.class);
            if (annotation == null) {
                template = null;
            } else {
                String templatePath = UiDescriptorUtils.getInferredTemplate(annotation, controllerClass);
                if (!templatePath.startsWith("/")) {
                    String packageName = UiControllerUtils.getPackage(controllerClass);
                    if (StringUtils.isNotEmpty(packageName)) {
                        String relativePath = packageName.replace('.', '/');
                        templatePath = "/" + relativePath + "/" + templatePath;
                    }
                }

                template = templatePath;
            }
        } else {
            throw new IllegalStateException("Neither screen class nor descriptor is set for WindowInfo " + windowInfo.getId());
        }

        WindowInfo.Type type = extractWindowInfoType(windowInfo, controllerClass);

        return new ResolvedWindowInfo(windowInfo, type, controllerClass, template);
    }

    protected MetadataReaderFactory getMetadataReaderFactory() {
        return metadataReaderFactory;
    }

    protected ResourceLoader getResourceLoader() {
        return resources;
    }

    protected WindowInfo.Type extractWindowInfoType(WindowInfo windowInfo, Class<? extends FrameOwner> controllerClass) {
        if (Screen.class.isAssignableFrom(controllerClass)) {
            return WindowInfo.Type.SCREEN;
        }

        if (ScreenFragment.class.isAssignableFrom(controllerClass)) {
            return WindowInfo.Type.FRAGMENT;
        }

        throw new IllegalStateException("Unknown type of screen " + windowInfo.getId());
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends FrameOwner> loadDefinedScreenClass(String className) {
        Preconditions.checkNotEmptyString(className, "class name is empty");
        return (Class<? extends FrameOwner>) classManager.loadClass(className);
    }

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        long startTime = System.currentTimeMillis();

        screens.clear();
        primaryEditors.clear();
        primaryLookups.clear();

        routes.clear();

        loadScreenConfigurations();
        loadScreensXml();

        log.info("WindowConfig initialized in {} ms", System.currentTimeMillis() - startTime);
    }

    protected void loadScreenConfigurations() {
        for (UiControllersConfiguration provider : configurations) {
            List<UiControllerDefinition> uiControllers = provider.getUiControllers();

            Map<String, String> projectScreens = new HashMap<>(uiControllers.size());

            for (UiControllerDefinition definition : uiControllers) {
                String existingScreenController = projectScreens.get(definition.getId());
                if (existingScreenController != null
                        && !Objects.equals(existingScreenController, definition.getControllerClass())) {
                    throw new RuntimeException(
                            String.format("Project contains screens with the same id: '%s'. See '%s' and '%s'",
                                    definition.getId(),
                                    definition.getControllerClass(),
                                    existingScreenController));
                } else {
                    projectScreens.put(definition.getId(), definition.getControllerClass());
                }

                WindowInfo windowInfo = new WindowInfo(definition.getId(), windowAttributesProvider,
                        definition.getControllerClass(), definition.getRouteDefinition());
                registerScreen(definition.getId(), windowInfo);
            }

            projectScreens.clear();
        }
    }

    protected void loadScreensXml() {
        for (String location : modules.getPropertyValues(WINDOW_CONFIG_XML_PROP)) {
            Resource resource = resources.getResource(location);
            if (resource.exists()) {
                try (InputStream stream = resource.getInputStream()) {
                    loadConfig(Dom4j.readDocument(stream).getRootElement());
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read window config from " + location, e);
                }
            } else {
                log.warn("Resource {} not found, ignore it", location);
            }
        }
    }

    protected void loadConfig(Element rootElem) {
        for (Element element : rootElem.elements("include")) {
            String fileName = element.attributeValue("file");
            if (!StringUtils.isBlank(fileName)) {
                String incXml = resources.getResourceAsString(fileName);
                if (incXml == null) {
                    log.warn("File {} not found, ignore it", fileName);
                    continue;
                }
                loadConfig(Dom4j.readDocument(incXml).getRootElement());
            }
        }
        for (Element element : rootElem.elements("screen")) {
            String id = element.attributeValue("id");
            if (StringUtils.isBlank(id)) {
                log.warn("Invalid window config: 'id' attribute not defined");
                continue;
            }

            RouteDefinition routeDef = loadRouteDefinition(element);

            WindowInfo windowInfo = new WindowInfo(id, windowAttributesProvider, element, routeDef);
            registerScreen(id, windowInfo);
        }
    }

    @Nullable
    protected RouteDefinition loadRouteDefinition(Element screenElement) {
        String screenId = screenElement.attributeValue("id");
        String route = screenElement.attributeValue("route");
        String parentPrefix = screenElement.attributeValue("routeParentPrefix");
        boolean rootRoute = Boolean.parseBoolean(screenElement.attributeValue("rootRoute"));

        RouteDefinition routeDefinition;

        WindowInfo superScreen = screens.get(screenId);
        RouteDefinition superScreenRouteDefinition = superScreen != null
                ? superScreen.getRouteDefinition()
                : null;

        if (route != null && !route.isEmpty()) {
            if (superScreenRouteDefinition != null) {
                String superScreenRoute = superScreenRouteDefinition.getPath();
                String superScreenParentPrefix = superScreenRouteDefinition.getParentPrefix();

                if (!route.equals(superScreenRoute)) {
                    log.debug("Route for screen '{}' is redefined from '{}' to '{}'",
                            screenId, superScreenRoute, rootRoute);

                    routes.remove(superScreenRoute);
                }

                if (parentPrefix == null || parentPrefix.isEmpty()) {
                    parentPrefix = superScreenParentPrefix;
                }
            }
            routeDefinition = new RouteDefinition(route, parentPrefix, rootRoute);
        } else {
            routeDefinition = superScreenRouteDefinition;
        }

        return routeDefinition;
    }

    protected void registerScreen(String id, WindowInfo windowInfo) {
        String controllerClassName = windowInfo.getControllerClassName();
        if (controllerClassName != null) {
            MetadataReader classMetadata = loadClassMetadata(controllerClassName);
            AnnotationMetadata annotationMetadata = classMetadata.getAnnotationMetadata();

            registerPrimaryEditor(windowInfo, annotationMetadata);
            registerPrimaryLookup(windowInfo, annotationMetadata);
        }

        screens.put(id, windowInfo);

        registerScreenRoute(id, windowInfo);
    }

    protected void registerScreenRoute(String screenId, WindowInfo windowInfo) {
        RouteDefinition routeDef = windowInfo.getRouteDefinition();
        if (routeDef != null) {
            String route = routeDef.getPath();
            String registeredScreenId = routes.get(route);
            if (registeredScreenId != null
                    && !Objects.equals(screenId, registeredScreenId)) {

                if (!routeOverrideAllowed(screenId)) {
                    return;
                }

                log.debug("Multiple use of the route '{}' for different screens is detected: '{}' and '{}'. " +
                                "The screen '{}' will be opened during navigation as the last registered screen",
                        route, screenId, registeredScreenId, screenId);
            }

            String registeredRoute = routes.inverse().get(screenId);
            if (StringUtils.isNotEmpty(registeredRoute)
                    && !Objects.equals(registeredRoute, route)) {
                log.debug("Route for screen '{}' is redefined from '{}' to '{}'",
                        screenId, registeredRoute, route);

                routes.remove(registeredRoute);
            }

            routes.put(route, screenId);
        }
    }

    /**
     * Have to do this check due to Login/Main Screen are registered
     * before legacy LoginWindow / AppMainWindow.
     */
    protected boolean routeOverrideAllowed(String newScreenId) {
        if (LOGIN_SCREEN_IDS.contains(newScreenId)) {
            return StringUtils.equals(uiProperties.getLoginScreenId(), newScreenId);
        }

        if (MAIN_SCREEN_IDS.contains(newScreenId)) {
            return StringUtils.equals(uiProperties.getMainScreenId(), newScreenId);
        }

        return true;
    }

    protected void registerPrimaryEditor(WindowInfo windowInfo, AnnotationMetadata annotationMetadata) {
        Map<String, Object> primaryEditorAnnotation =
                annotationMetadata.getAnnotationAttributes(PrimaryEditorScreen.class.getName());
        if (primaryEditorAnnotation != null) {
            Class entityClass = (Class) primaryEditorAnnotation.get("value");
            if (entityClass != null) {
                MetaClass metaClass = metadata.findClass(entityClass);
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                primaryEditors.put(originalMetaClass.getJavaClass(), windowInfo);
            }
        }
    }

    protected void registerPrimaryLookup(WindowInfo windowInfo, AnnotationMetadata annotationMetadata) {
        Map<String, Object> primaryEditorAnnotation =
                annotationMetadata.getAnnotationAttributes(PrimaryLookupScreen.class.getName());
        if (primaryEditorAnnotation != null) {
            Class entityClass = (Class) primaryEditorAnnotation.get("value");
            if (entityClass != null) {
                MetaClass metaClass = metadata.findClass(entityClass);
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                primaryLookups.put(originalMetaClass.getJavaClass(), windowInfo);
            }
        }
    }

    protected MetadataReader loadClassMetadata(String className) {
        Resource resource = getResourceLoader().getResource("/" + className.replace(".", "/") + ".class");
        if (!resource.isReadable()) {
            throw new RuntimeException(String.format("Resource %s is not readable for class %s", resource, className));
        }
        try {
            return getMetadataReaderFactory().getMetadataReader(resource);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read resource " + resource, e);
        }
    }

    /**
     * Loads hot-deployed {@link UiController} screens and registers
     * {@link UiControllersConfiguration} containing new {@link UiControllerDefinition}.
     *
     * @param className the fully qualified name of the screen class to load
     */
    public void loadScreenClass(String className) {
        Class screenClass = classManager.loadClass(className);

        UiControllerMeta controllerMeta = new UiControllerMeta(metadataReaderFactory, screenClass);

        UiControllerDefinition uiControllerDefinition = new UiControllerDefinition(
                controllerMeta.getId(), controllerMeta.getControllerClass(), controllerMeta.getRouteDefinition());

        UiControllersConfiguration controllersConfiguration =
                new UiControllersConfiguration(applicationContext, metadataReaderFactory);

        controllersConfiguration.setExplicitDefinitions(Collections.singletonList(uiControllerDefinition));

        configurations.add(controllersConfiguration);

        reset();
    }

    /**
     * Make the config to reload screens on next request.
     */
    public void reset() {
        initialized = false;
    }

    /**
     * Get screen information by screen ID.
     *
     * @param id screen ID as set up in <code>screens.xml</code>
     * @return screen's registration information or null if not found
     */
    @Nullable
    public WindowInfo findWindowInfo(String id) {
        lock.readLock().lock();
        try {
            checkInitialized();

            WindowInfo windowInfo = screens.get(id);
            if (windowInfo == null) {
                Matcher matcher = ENTITY_SCREEN_PATTERN.matcher(id);
                if (matcher.matches()) {
                    MetaClass metaClass = metadata.findClass(matcher.group(1));
                    if (metaClass == null)
                        return null;
                    MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
                    if (originalMetaClass != null) {
                        String originalId = new StringBuilder(id)
                                .replace(matcher.start(1), matcher.end(1), originalMetaClass.getName()).toString();
                        windowInfo = screens.get(originalId);
                    }
                }
            }
            return windowInfo;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get screen information by screen ID.
     *
     * @param id screen ID as set up in <code>screens.xml</code>
     * @return screen's registration information
     * @throws NoSuchScreenException if the screen with specified ID is not registered
     */
    public WindowInfo getWindowInfo(String id) {
        WindowInfo windowInfo = findWindowInfo(id);
        if (windowInfo == null) {
            throw new NoSuchScreenException(id);
        }
        return windowInfo;
    }

    /**
     * Get screen information by route.
     *
     * @param route route
     * @return screen's registration information or null if not found
     */
    @Nullable
    public WindowInfo findWindowInfoByRoute(String route) {
        String screenId = routes.get(route);
        return screenId != null
                ? findWindowInfo(screenId)
                : null;
    }

    /**
     * Find route by screen id.
     *
     * @param id screen id
     * @return registered route or null if no route for screen
     */
    @Nullable
    public String findRoute(String id) {
        return routes.inverse().get(id);
    }

    /**
     * @return true if the configuration contains a screen with provided ID
     */
    public boolean hasWindow(String id) {
        return findWindowInfo(id) != null;
    }

    /**
     * All registered screens
     */
    public Collection<WindowInfo> getWindows() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return screens.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getMetaClassScreenId(MetaClass metaClass, String suffix) {
        MetaClass screenMetaClass = metaClass;
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        if (originalMetaClass != null) {
            screenMetaClass = originalMetaClass;
        }

        return screenMetaClass.getName() + suffix;
    }

    public String getBrowseScreenId(MetaClass metaClass) {
        return getMetaClassScreenId(metaClass, Window.BROWSE_WINDOW_SUFFIX);
    }

    public String getLookupScreenId(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        WindowInfo windowInfo = primaryLookups.get(originalMetaClass.getJavaClass());
        if (windowInfo != null) {
            return windowInfo.getId();
        }

        return getMetaClassScreenId(metaClass, Window.LOOKUP_WINDOW_SUFFIX);
    }

    public String getEditorScreenId(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        WindowInfo windowInfo = primaryEditors.get(originalMetaClass.getJavaClass());
        if (windowInfo != null) {
            return windowInfo.getId();
        }

        return getMetaClassScreenId(metaClass, Window.EDITOR_WINDOW_SUFFIX);
    }

    public WindowInfo getEditorScreen(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        WindowInfo windowInfo = primaryEditors.get(originalMetaClass.getJavaClass());
        if (windowInfo != null) {
            return windowInfo;
        }

        String editorScreenId = getEditorScreenId(metaClass);
        return getWindowInfo(editorScreenId);
    }

    /**
     * Get available lookup screen by class of entity
     *
     * @param entityClass entity class
     * @return id of lookup screen
     * @throws NoSuchScreenException if the screen with specified ID is not registered
     */
    public WindowInfo getLookupScreen(Class<?> entityClass) {
        MetaClass metaClass = metadata.getSession().findClass(entityClass);

        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        WindowInfo windowInfo = primaryLookups.get(originalMetaClass.getJavaClass());
        if (windowInfo != null) {
            return windowInfo;
        }

        String lookupScreenId = getAvailableLookupScreenId(metaClass);
        return getWindowInfo(lookupScreenId);
    }

    public String getAvailableLookupScreenId(MetaClass metaClass) {
        String id = getLookupScreenId(metaClass);
        if (!hasWindow(id)) {
            id = getBrowseScreenId(metaClass);
        }
        return id;
    }

    public static class ResolvedWindowInfo extends WindowInfo {

        protected final String template;
        protected final Class<? extends FrameOwner> controllerClass;
        protected final Type type;

        public ResolvedWindowInfo(WindowInfo windowInfo, Type type, Class<? extends FrameOwner> controllerClass,
                                  @Nullable String template) {
            super(windowInfo.getId(), null, windowInfo.getDescriptor(),
                    windowInfo.getControllerClassName(), windowInfo.getRouteDefinition());

            this.template = template;

            this.controllerClass = controllerClass;
            this.type = type;
        }

        @Nullable
        @Override
        public String getTemplate() {
            return template;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Class<? extends FrameOwner> getControllerClass() {
            return controllerClass;
        }

        @Override
        public WindowInfo resolve() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ResolvedWindowInfo that = (ResolvedWindowInfo) o;
            return Objects.equals(template, that.template) &&
                    Objects.equals(controllerClass, that.controllerClass) &&
                    type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(template, controllerClass, type);
        }
    }
}
