package io.jmix.flowui.screen;

import io.jmix.core.ClassManager;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.Resources;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.exception.NoSuchScreenException;
import io.jmix.flowui.sys.UiControllerDefinition;
import io.jmix.flowui.sys.UiControllersConfiguration;
import io.jmix.flowui.sys.UiDescriptorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;

@Component("flowui_ScreenRegistry")
public class ScreenRegistry {

    private static final Logger log = LoggerFactory.getLogger(ScreenRegistry.class);

    public static final Pattern ENTITY_SCREEN_PATTERN = Pattern.compile("([A-Za-z0-9]+[$_][A-Z][_A-Za-z0-9]*)\\..+");

    public static final String BROWSE_SCREEN_SUFFIX = ".browse";
    public static final String LOOKUP_SCREEN_SUFFIX = ".lookup";
    public static final String EDITOR_SCREEN_SUFFIX = ".edit";

    protected Metadata metadata;
    protected Resources resources;
    protected ClassManager classManager;
    protected ExtendedEntities extendedEntities;
    protected AnnotationScanMetadataReaderFactory metadataReaderFactory;

    protected Map<String, ScreenInfo> screens = new HashMap<>();

    protected Map<Class<?>, ScreenInfo> primaryEditors = new HashMap<>();
    protected Map<Class<?>, ScreenInfo> primaryLookups = new HashMap<>();

    protected List<UiControllersConfiguration> configurations = Collections.emptyList();

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();


    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Autowired
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    @Autowired
    public void setExtendedEntities(ExtendedEntities extendedEntities) {
        this.extendedEntities = extendedEntities;
    }

    @Autowired
    public void setMetadataReaderFactory(AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        this.metadataReaderFactory = metadataReaderFactory;
    }

    @Autowired(required = false)
    public void setConfigurations(List<UiControllersConfiguration> configurations) {
        this.configurations = configurations;
    }

    /**
     * Make the registry to reload screens on next request.
     */
    public void reset() {
        initialized = false;
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

        loadScreenConfigurations();

        log.info("{} initialized in {} ms", getClass().getSimpleName(), System.currentTimeMillis() - startTime);
    }

    protected void loadScreenConfigurations() {
        for (UiControllersConfiguration provider : configurations) {
            List<UiControllerDefinition> uiControllers = provider.getUiControllers();

            Map<String, String> projectScreens = new HashMap<>(uiControllers.size());

            for (UiControllerDefinition definition : uiControllers) {
                String screenId = definition.getId();
                String controllerClassName = definition.getControllerClassName();

                String existingScreenController = projectScreens.get(screenId);
                if (existingScreenController != null
                        && !Objects.equals(existingScreenController, controllerClassName)) {
                    throw new RuntimeException(
                            String.format("Project contains screens with the same id: '%s'. See '%s' and '%s'",
                                    screenId,
                                    controllerClassName,
                                    existingScreenController));
                } else {
                    projectScreens.put(screenId, controllerClassName);
                }

                Class<? extends Screen> controllerClass = loadDefinedScreenClass(controllerClassName);
                String templatePath = resolveTemplatePath(controllerClass);
                ScreenInfo screenInfo = new ScreenInfo(screenId, controllerClassName, controllerClass, templatePath);

                registerScreen(screenId, screenInfo);
            }

            projectScreens.clear();
        }
    }

    protected void registerScreen(String id, ScreenInfo screenInfo) {
        String controllerClassName = screenInfo.getControllerClassName();
        MetadataReader classMetadata = loadClassMetadata(controllerClassName);
        AnnotationMetadata annotationMetadata = classMetadata.getAnnotationMetadata();

        registerPrimaryEditor(screenInfo, annotationMetadata);
        registerPrimaryLookup(screenInfo, annotationMetadata);

        screens.put(id, screenInfo);
    }

    protected void registerPrimaryEditor(ScreenInfo screenInfo, AnnotationMetadata annotationMetadata) {
        Map<String, Object> primaryEditorAnnotation =
                annotationMetadata.getAnnotationAttributes(PrimaryEditorScreen.class.getName());
        if (primaryEditorAnnotation != null) {
            Class<?> entityClass = (Class<?>) primaryEditorAnnotation.get("value");
            if (entityClass != null) {
                MetaClass metaClass = metadata.findClass(entityClass);
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                primaryEditors.put(originalMetaClass.getJavaClass(), screenInfo);
            }
        }
    }

    protected void registerPrimaryLookup(ScreenInfo screenInfo, AnnotationMetadata annotationMetadata) {
        Map<String, Object> primaryEditorAnnotation =
                annotationMetadata.getAnnotationAttributes(PrimaryLookupScreen.class.getName());
        if (primaryEditorAnnotation != null) {
            Class<?> entityClass = (Class<?>) primaryEditorAnnotation.get("value");
            if (entityClass != null) {
                MetaClass metaClass = metadata.findClass(entityClass);
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                primaryLookups.put(originalMetaClass.getJavaClass(), screenInfo);
            }
        }
    }

    protected MetadataReader loadClassMetadata(String className) {
        Resource resource = resources.getResource("/" + className.replace(".", "/") + ".class");
        if (!resource.isReadable()) {
            throw new RuntimeException(String.format("Resource %s is not readable for class %s", resource, className));
        }
        try {
            return metadataReaderFactory.getMetadataReader(resource);
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
    // TODO: gg, implement?
    /*public void loadScreenClass(String className) {
        Class screenClass = classManager.loadClass(className);

        UiControllerMeta controllerMeta = new UiControllerMeta(metadataReaderFactory, screenClass);

        UiControllerDefinition uiControllerDefinition = new UiControllerDefinition(
                controllerMeta.getId(), controllerMeta.getControllerClass(), controllerMeta.getRouteDefinition());

        UiControllersConfiguration controllersConfiguration =
                new UiControllersConfiguration(applicationContext, metadataReaderFactory);

        controllersConfiguration.setExplicitDefinitions(Collections.singletonList(uiControllerDefinition));

        configurations.add(controllersConfiguration);

        reset();
    }*/
    @SuppressWarnings("unchecked")
    protected Class<? extends Screen> loadDefinedScreenClass(String className) {
        checkNotEmptyString(className, "class name is empty");
        return (Class<? extends Screen>) classManager.loadClass(className);
    }

    // TODO: gg, move to utils?
    @Nullable
    protected String resolveTemplatePath(Class<? extends Screen> controllerClass) {
        UiDescriptor annotation = controllerClass.getAnnotation(UiDescriptor.class);
        if (annotation == null) {
            return null;
        } else {
            String templatePath = UiDescriptorUtils.getInferredTemplate(annotation, controllerClass);
            if (!templatePath.startsWith("/")) {
                String packageName = UiControllerUtils.getPackage(controllerClass);
                if (StringUtils.isNotEmpty(packageName)) {
                    String relativePath = packageName.replace('.', '/');
                    templatePath = "/" + relativePath + "/" + templatePath;
                }
            }

            return templatePath;
        }
    }

    /**
     * Returns screen information by screen id.
     *
     * @param id screen id as set in the {@link UiController} annotation
     * @return screen's registration information
     */
    public Optional<ScreenInfo> findScreenInfo(String id) {
        lock.readLock().lock();
        try {
            checkInitialized();

            ScreenInfo screenInfo = screens.get(id);
            if (screenInfo != null) {
                return Optional.of(screenInfo);
            }

            Matcher matcher = ENTITY_SCREEN_PATTERN.matcher(id);
            if (matcher.matches()) {
                MetaClass metaClass = metadata.findClass(matcher.group(1));
                if (metaClass == null) {
                    return Optional.empty();
                }

                MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
                if (originalMetaClass != null) {
                    String originalId = new StringBuilder(id)
                            .replace(matcher.start(1), matcher.end(1), originalMetaClass.getName()).toString();
                    screenInfo = screens.get(originalId);
                }
            }

            return Optional.ofNullable(screenInfo);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns screen information by screen id.
     *
     * @param id screen id as set in the {@link UiController} annotation
     * @return screen's registration information
     * @throws NoSuchScreenException if the screen with specified id is not registered
     */
    public ScreenInfo getScreenInfo(String id) {
        Optional<ScreenInfo> screenInfo = findScreenInfo(id);
        if (screenInfo.isPresent()) {
            return screenInfo.get();
        }

        throw new NoSuchScreenException(id);
    }

    /**
     * @return {@code true} if the registry contains a screen with provided id
     */
    public boolean hasScreen(String id) {
        return findScreenInfo(id).isPresent();
    }

    public Collection<ScreenInfo> getScreens() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return screens.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    // TODO: gg, API for specific screens, e.g. browse, lookup, edit, etc.

    public String getMetaClassScreenId(MetaClass metaClass, String suffix) {
        MetaClass screenMetaClass = metaClass;
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        if (originalMetaClass != null) {
            screenMetaClass = originalMetaClass;
        }

        return screenMetaClass.getName() + suffix;
    }

    public String getBrowseScreenId(MetaClass metaClass) {
        return getMetaClassScreenId(metaClass, BROWSE_SCREEN_SUFFIX);
    }

    public String getLookupScreenId(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        ScreenInfo screenInfo = primaryLookups.get(originalMetaClass.getJavaClass());
        if (screenInfo != null) {
            return screenInfo.getId();
        }

        return getMetaClassScreenId(metaClass, LOOKUP_SCREEN_SUFFIX);
    }

    public String getEditorScreenId(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        ScreenInfo screenInfo = primaryEditors.get(originalMetaClass.getJavaClass());
        if (screenInfo != null) {
            return screenInfo.getId();
        }

        return getMetaClassScreenId(metaClass, EDITOR_SCREEN_SUFFIX);
    }

    public ScreenInfo getEditorScreen(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        ScreenInfo screenInfo = primaryEditors.get(originalMetaClass.getJavaClass());
        if (screenInfo != null) {
            return screenInfo;
        }

        String editorScreenId = getEditorScreenId(metaClass);
        return getScreenInfo(editorScreenId);
    }

    // TODO: gg, rename?
    public ScreenInfo getEditorScreen(Class<?> entityClass) {
        MetaClass metaClass = metadata.getSession().findClass(entityClass);
        return getEditorScreen(metaClass);
    }

    // TODO: gg, rename?
    public ScreenInfo getEditorScreen(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        return getEditorScreen(metaClass);
    }

    public ScreenInfo getLookupScreen(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        ScreenInfo screenInfo = primaryLookups.get(originalMetaClass.getJavaClass());
        if (screenInfo != null) {
            return screenInfo;
        }

        String lookupScreenId = getAvailableLookupScreenId(metaClass);
        return getScreenInfo(lookupScreenId);
    }

    /**
     * Get available lookup screen by class of entity
     *
     * @param entityClass entity class
     * @return id of lookup screen
     * @throws NoSuchScreenException if the screen with specified ID is not registered
     */
    // TODO: gg, rename?
    public ScreenInfo getLookupScreen(Class<?> entityClass) {
        MetaClass metaClass = metadata.getSession().findClass(entityClass);
        return getLookupScreen(metaClass);
    }

    public ScreenInfo getLookupScreen(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        return getLookupScreen(metaClass);
    }

    // TODO: gg, test if neither .lookup not .browse screen exists
    public String getAvailableLookupScreenId(MetaClass metaClass) {
        String id = getLookupScreenId(metaClass);
        if (!hasScreen(id)) {
            id = getBrowseScreenId(metaClass);
        }
        return id;
    }
}
