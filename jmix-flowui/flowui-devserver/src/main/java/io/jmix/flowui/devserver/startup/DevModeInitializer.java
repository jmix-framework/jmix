/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.flowui.devserver.startup;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.frontend.FallbackChunk;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.ClassFinder.DefaultClassFinder;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.flow.server.startup.VaadinInitializerException;
import elemental.json.Json;
import elemental.json.JsonObject;
import io.jmix.flowui.devserver.BrowserLiveReloadAccessorImpl;
import io.jmix.flowui.devserver.ViteHandler;
import io.jmix.flowui.devserver.ViteWebsocketEndpoint;
import io.jmix.flowui.devserver.frontend.EndpointGeneratorTaskFactory;
import io.jmix.flowui.devserver.frontend.FrontendUtils;
import io.jmix.flowui.devserver.frontend.NodeTasks;
import io.jmix.flowui.devserver.frontend.Options;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.HandlesTypes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.vaadin.flow.server.Constants.PROJECT_FRONTEND_GENERATED_DIR_TOKEN;
import static com.vaadin.flow.server.Constants.VAADIN_SERVLET_RESOURCES;
import static com.vaadin.flow.server.Constants.VAADIN_WEBAPP_RESOURCES;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_DEVMODE_OPTIMIZE_BUNDLE;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.DEFAULT_FRONTEND_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.DEFAULT_GENERATED_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_FRONTEND_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_GENERATED_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_STUDIO_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_THEME_CLASS;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_THEME_VALUE;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_THEME_VARIANT;

public class DevModeInitializer implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(DevModeInitializer.class);

    static class DevModeClassFinder extends DefaultClassFinder {

        private static final Set<String> APPLICABLE_CLASS_NAMES = Collections
                .unmodifiableSet(calculateApplicableClassNames());

        public DevModeClassFinder(ClassLoader classLoader, Class<?>... classes) {
            super(classLoader, classes);
        }

        @Override
        public Set<Class<?>> getAnnotatedClasses(
                Class<? extends Annotation> annotation) {
            ensureImplementation(annotation);
            return super.getAnnotatedClasses(annotation);
        }

        @Override
        public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
            ensureImplementation(type);
            return super.getSubTypesOf(type);
        }

        private void ensureImplementation(Class<?> clazz) {
            if (!APPLICABLE_CLASS_NAMES.contains(clazz.getName())) {
                throw new IllegalArgumentException("Unexpected class name "
                        + clazz + ". Implementation error: the class finder "
                        + "instance is not aware of this class. "
                        + "Fix @HandlesTypes annotation value for "
                        + DevModeStartupListener.class.getName());
            }
        }

        private static Set<String> calculateApplicableClassNames() {
            HandlesTypes handlesTypes = DevModeStartupListener.class
                    .getAnnotation(HandlesTypes.class);
            return Stream.of(handlesTypes.value()).map(Class::getName)
                    .collect(Collectors.toSet());
        }
    }

    private static final Pattern JAR_FILE_REGEX = Pattern
            .compile(".*file:(.+\\.jar).*");

    // Path of jar files in a URL with zip protocol doesn't start with
    // "zip:"
    // nor "file:". It contains only the path of the file.
    // Weblogic uses zip protocol.
    private static final Pattern ZIP_PROTOCOL_JAR_FILE_REGEX = Pattern
            .compile("(.+\\.jar).*");

    private static final Pattern VFS_FILE_REGEX = Pattern
            .compile("(vfs:/.+\\.jar).*");

    private static final Pattern VFS_DIRECTORY_REGEX = Pattern
            .compile("vfs:/.+");

    // allow trailing slash
    private static final Pattern DIR_REGEX_FRONTEND_DEFAULT = Pattern.compile(
            "^(?:file:0)?(.+)" + Constants.RESOURCES_FRONTEND_DEFAULT + "/?$");

    // allow trailing slash
    private static final Pattern DIR_REGEX_RESOURCES_JAR_DEFAULT = Pattern
            .compile("^(?:file:0)?(.+)" + Constants.RESOURCES_THEME_JAR_DEFAULT
                    + "/?$");

    // allow trailing slash
    private static final Pattern DIR_REGEX_COMPATIBILITY_FRONTEND_DEFAULT = Pattern
            .compile("^(?:file:)?(.+)"
                    + Constants.COMPATIBILITY_RESOURCES_FRONTEND_DEFAULT
                    + "/?$");

    /**
     * Initialize the devmode server if not in production mode or compatibility
     * mode.
     *
     * @param classes classes to check for npm- and js modules
     * @param context VaadinContext we are running in
     * @return the initialized dev mode handler or {@code null} if none was
     * created
     * @throws VaadinInitializerException if dev mode can't be initialized
     */
    public static DevModeHandler initDevModeHandler(Set<Class<?>> classes,
                                                    VaadinContext context) throws VaadinInitializerException {
        File studioFolder = new File(System.getProperty(PARAM_STUDIO_DIR));

        ApplicationConfiguration config = ApplicationConfiguration.get(context);
        if (config.isProductionMode()) {
            log.debug("Skipping DEV MODE because PRODUCTION MODE is set.");
            return null;
        }
        // This needs to be set as there is no "current service" available in
        // this call
        FeatureFlags.get(context).setPropertiesLocation(config.getJavaResourceFolder());

        String baseDir = config.getProjectFolder().getAbsolutePath();

//        // Initialize the usage statistics if enabled
//        if (config.isUsageStatisticsEnabled()) {
//            StatisticsStorage storage = new StatisticsStorage();
//            DevModeUsageStatistics.init(baseDir, storage,
//                    new StatisticsSender(storage));
//        }

        String generatedDir = System.getProperty(PARAM_GENERATED_DIR,
                Paths.get(config.getBuildFolder(), DEFAULT_GENERATED_DIR).toString());
        String frontendFolder = config.getStringProperty(PARAM_FRONTEND_DIR,
                System.getProperty(PARAM_FRONTEND_DIR, DEFAULT_FRONTEND_DIR));

        ServletContext servletContext = ((VaadinServletContext) context).getContext();
        ClassLoader contextClassLoader = servletContext.getClassLoader();
        Lookup lookup = configureAndGetLookup(config, context, classes);

        Options options = new Options(lookup, new File(baseDir))
                .withGeneratedFolder(new File(generatedDir))
                .withFrontendDirectory(new File(frontendFolder))
                .withBuildDirectory(config.getBuildFolder());

        log.info("Starting dev-mode updaters in {} folder.", studioFolder);
        FrontendUtils.logInFile("Starting dev-mode updaters in folder (" + studioFolder + ")", true);

        if (!options.getGeneratedFolder().exists()) {
            try {
                FileUtils.forceMkdir(options.getGeneratedFolder());
            } catch (IOException e) {
                throw new UncheckedIOException(
                        String.format("Failed to create directory '%s'",
                                options.getGeneratedFolder()),
                        e);
            }
        }

        // Regenerate Vite configuration, as it may be necessary to
        // update it
        // TODO: make sure target directories are aligned with build
        // config,
        // see https://github.com/vaadin/flow/issues/9082

        File target = new File(baseDir, config.getBuildFolder());
        options.withWebpack(
                Paths.get(target.getPath(), "classes", VAADIN_WEBAPP_RESOURCES)
                        .toFile(),
                Paths.get(target.getPath(), "classes", VAADIN_SERVLET_RESOURCES)
                        .toFile());

        options.createMissingPackageJson(true);

        Set<File> frontendLocations = getFrontendLocationsFromClassloader(contextClassLoader);

        boolean useByteCodeScanner = config.getBooleanProperty(
                SERVLET_PARAMETER_DEVMODE_OPTIMIZE_BUNDLE,
                Boolean.parseBoolean(System.getProperty(
                        SERVLET_PARAMETER_DEVMODE_OPTIMIZE_BUNDLE,
                        Boolean.FALSE.toString())));

        boolean enablePnpm = config.isPnpmEnabled();

        boolean useGlobalPnpm = config.isGlobalPnpm();

        boolean useHomeNodeExec = config.getBooleanProperty(
                InitParameters.REQUIRE_HOME_NODE_EXECUTABLE, false);

        String[] additionalPostinstallPackages = config
                .getStringProperty(
                        InitParameters.ADDITIONAL_POSTINSTALL_PACKAGES, "")
                .split(",");

        String frontendGeneratedFolderName = config.getStringProperty(
                PROJECT_FRONTEND_GENERATED_DIR_TOKEN,
                Paths.get(baseDir, DEFAULT_PROJECT_FRONTEND_GENERATED_DIR)
                        .toString());

        File frontendGeneratedFolder = new File(frontendGeneratedFolderName);
        File jarFrontendResourcesFolder = new File(frontendGeneratedFolder,
                FrontendUtils.JAR_RESOURCES_FOLDER);
        JsonObject tokenFileData = Json.createObject();
        options.enablePackagesUpdate(true)
                .useByteCodeScanner(useByteCodeScanner)
                .withFrontendGeneratedFolder(frontendGeneratedFolder)
                .withJarFrontendResourcesFolder(jarFrontendResourcesFolder)
                .copyResources(frontendLocations)
                .copyLocalResources(new File(baseDir,
                        Constants.LOCAL_FRONTEND_RESOURCES_PATH))
                .enableImportsUpdate(true)
                .withRunNpmInstall(true)
                .populateTokenFileData(tokenFileData)
                .withEmbeddableWebComponents(true)
                .withEnablePnpm(enablePnpm)
                .useGlobalPnpm(useGlobalPnpm)
                .withHomeNodeExecRequired(useHomeNodeExec)
                .withProductionMode(config.isProductionMode())
                // TODO: add link to download Node from Jmix sources
                //.withNodeDownloadRoot(URI.create("https://www."))
                .withPostinstallPackages(
                        Arrays.asList(additionalPostinstallPackages))
                .withDevBundleBuild(true)
                .withThemeValue(System.getProperty(PARAM_THEME_VALUE))
                .withThemeVariant(System.getProperty(PARAM_THEME_VARIANT))
                .withThemeClass(System.getProperty(PARAM_THEME_CLASS))
                .withFrontendHotdeploy(true)
                .withStudioFolder(studioFolder);

        NodeTasks tasks = new NodeTasks(options);

        Runnable runnable = () -> {
            runNodeTasks(context, tokenFileData, tasks);
            // For Vite, wait until a VaadinServlet is deployed so we know
            // which frontend servlet path to use
            if (VaadinServlet.getFrontendMapping() == null) {
                String waitingServletMessage = "Waiting for a VaadinServlet to be deployed";
                FrontendUtils.logInFile(waitingServletMessage);
                log().debug(waitingServletMessage);
                while (VaadinServlet.getFrontendMapping() == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };

        CompletableFuture<Void> nodeTasksFuture = CompletableFuture.runAsync(runnable);

        Lookup devServerLookup = Lookup.compose(lookup, Lookup.of(config, ApplicationConfiguration.class));
        ViteHandler handler = new ViteHandler(devServerLookup, 0,
                options.getNpmFolder(), options.getStudioFolder(), nodeTasksFuture);

        VaadinServlet.whenFrontendMappingAvailable(
                () -> ViteWebsocketEndpoint.init(context, handler));

        return handler;
    }

    private static Lookup configureAndGetLookup(ApplicationConfiguration config,
                                                VaadinContext context,
                                                Set<Class<?>> classes) {
        return composeLookups(
                context.getAttribute(Lookup.class),
                getServiceBindings(config, context, classes)
        );
    }

    private static <T> Lookup composeLookups(Lookup lookup, Map<T, Class<?>> serviceBindings) {
        final Lookup[] resultLookup = new Lookup[1];
        resultLookup[0] = lookup;

        //noinspection unchecked
        serviceBindings.forEach((service, serviceInterface) ->
                resultLookup[0] = Lookup.compose(
                        resultLookup[0],
                        Lookup.of(
                                service,
                                (Class<T>) serviceInterface
                        )
                ));

        return resultLookup[0];
    }

    private static Map<Object, Class<?>> getServiceBindings(ApplicationConfiguration config,
                                                            VaadinContext context,
                                                            Set<Class<?>> classes) {
        ServletContext servletContext = ((VaadinServletContext) context).getContext();
        ClassLoader contextClassLoader = servletContext.getClassLoader();

        final Map<Object, Class<?>> serviceBindings = new HashMap<>();
        serviceBindings.put(
                new DevModeClassFinder(contextClassLoader, classes.toArray(new Class[0])),
                ClassFinder.class
        );
        serviceBindings.put(
                config,
                ApplicationConfiguration.class
        );
        serviceBindings.put(
                new BrowserLiveReloadAccessorImpl(),
                BrowserLiveReloadAccessor.class
        );

        return serviceBindings;
    }

    private static boolean isEndpointServiceAvailable(Lookup lookup) {
        if (lookup == null) {
            return false;
        }
        return lookup.lookup(EndpointGeneratorTaskFactory.class) != null;
    }

    private static Logger log() {
        return LoggerFactory.getLogger(DevModeStartupListener.class);
    }


    /*
     * This method returns all folders of jar files having files in the
     * META-INF/resources/frontend and META-INF/resources/themes folder. We
     * don't use URLClassLoader because will fail in Java 9+
     */
    static Set<File> getFrontendLocationsFromClassloader(
            ClassLoader classLoader) throws VaadinInitializerException {
        Set<File> frontendFiles = new HashSet<>();

        frontendFiles.addAll(getFrontendLocationsFromClassloader(classLoader,
                Constants.RESOURCES_FRONTEND_DEFAULT));
        frontendFiles.addAll(getFrontendLocationsFromClassloader(classLoader,
                Constants.COMPATIBILITY_RESOURCES_FRONTEND_DEFAULT));
        frontendFiles.addAll(getFrontendLocationsFromClassloader(classLoader,
                Constants.RESOURCES_THEME_JAR_DEFAULT));

        return frontendFiles;
    }

    private static void runNodeTasks(VaadinContext vaadinContext,
                                     JsonObject tokenFileData, NodeTasks tasks) {
        try {
            tasks.execute();

            FallbackChunk chunk = FrontendUtils.readFallbackChunk(tokenFileData);
            if (chunk != null) {
                vaadinContext.setAttribute(chunk);
            }
        } catch (ExecutionFailedException exception) {
            String errorMessage = "Could not initialize dev mode handler. One of the node tasks failed. ";
            FrontendUtils.logInFile(errorMessage + exception.getMessage());
            log().debug(errorMessage, exception);
            throw new CompletionException(exception);
        }
    }

    private static Set<File> getFrontendLocationsFromClassloader(
            ClassLoader classLoader, String resourcesFolder)
            throws VaadinInitializerException {
        Set<File> frontendFiles = new HashSet<>();
        try {
            Enumeration<URL> en = classLoader.getResources(resourcesFolder);
            if (en == null) {
                return frontendFiles;
            }
            Set<String> vfsJars = new HashSet<>();
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                String urlString = url.toString();

                String path = URLDecoder.decode(url.getPath(),
                        StandardCharsets.UTF_8);
                Matcher jarMatcher = JAR_FILE_REGEX.matcher(path);
                Matcher zipProtocolJarMatcher = ZIP_PROTOCOL_JAR_FILE_REGEX
                        .matcher(path);
                Matcher dirMatcher = DIR_REGEX_FRONTEND_DEFAULT.matcher(path);
                Matcher dirResourcesMatcher = DIR_REGEX_RESOURCES_JAR_DEFAULT
                        .matcher(path);
                Matcher dirCompatibilityMatcher = DIR_REGEX_COMPATIBILITY_FRONTEND_DEFAULT
                        .matcher(path);
                Matcher jarVfsMatcher = VFS_FILE_REGEX.matcher(urlString);
                Matcher dirVfsMatcher = VFS_DIRECTORY_REGEX.matcher(urlString);
                if (jarVfsMatcher.find()) {
                    String vfsJar = jarVfsMatcher.group(1);
                    if (vfsJars.add(vfsJar)) { // NOSONAR
                        frontendFiles.add(
                                getPhysicalFileOfJBossVfsJar(new URL(vfsJar)));
                    }
                } else if (dirVfsMatcher.find()) {
                    URL vfsDirUrl = new URL(urlString.substring(0,
                            urlString.lastIndexOf(resourcesFolder)));
                    frontendFiles
                            .add(getPhysicalFileOfJBossVfsDirectory(vfsDirUrl));
                } else if (jarMatcher.find()) {
                    frontendFiles.add(new File(jarMatcher.group(1)));
                } else if ("zip".equalsIgnoreCase(url.getProtocol())
                        && zipProtocolJarMatcher.find()) {
                    frontendFiles.add(new File(zipProtocolJarMatcher.group(1)));
                } else if (dirMatcher.find()) {
                    frontendFiles.add(new File(dirMatcher.group(1)));
                } else if (dirResourcesMatcher.find()) {
                    frontendFiles.add(new File(dirResourcesMatcher.group(1)));
                } else if (dirCompatibilityMatcher.find()) {
                    frontendFiles
                            .add(new File(dirCompatibilityMatcher.group(1)));
                } else {
                    String message = String.format("Resource %s not visited because does not meet supported formats.", url.getPath());
                    log().warn(message);
                    FrontendUtils.logInFile(message);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return frontendFiles;
    }

    private static File getPhysicalFileOfJBossVfsDirectory(URL url)
            throws IOException, VaadinInitializerException {
        try {
            Object virtualFile = url.openConnection().getContent();
            Class virtualFileClass = virtualFile.getClass();

            // Reflection as we cannot afford a dependency to
            // WildFly or JBoss
            Method getChildrenRecursivelyMethod = virtualFileClass
                    .getMethod("getChildrenRecursively");
            Method getPhysicalFileMethod = virtualFileClass
                    .getMethod("getPhysicalFile");

            // By calling getPhysicalFile, we make sure that the
            // corresponding
            // physical files/directories of the root directory and
            // its children
            // are created. Later, these physical files are scanned
            // to collect
            // their resources.
            List virtualFiles = (List) getChildrenRecursivelyMethod
                    .invoke(virtualFile);
            File rootDirectory = (File) getPhysicalFileMethod
                    .invoke(virtualFile);
            for (Object child : virtualFiles) {
                // side effect: create real-world files
                getPhysicalFileMethod.invoke(child);
            }
            return rootDirectory;
        } catch (NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException exc) {
            throw new VaadinInitializerException(
                    "Failed to invoke JBoss VFS API.", exc);
        }
    }

    private static File getPhysicalFileOfJBossVfsJar(URL url)
            throws IOException, VaadinInitializerException {
        try {
            Object jarVirtualFile = url.openConnection().getContent();

            // Creating a temporary jar file out of the vfs files
            String vfsJarPath = url.toString();
            String fileNamePrefix = vfsJarPath.substring(
                    vfsJarPath.lastIndexOf('/') + 1,
                    vfsJarPath.lastIndexOf(".jar"));
            Path tempJar = Files.createTempFile(fileNamePrefix, ".jar");

            generateJarFromJBossVfsFolder(jarVirtualFile, tempJar);

            File tempJarFile = tempJar.toFile();
            tempJarFile.deleteOnExit();
            return tempJarFile;
        } catch (NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException exc) {
            throw new VaadinInitializerException(
                    "Failed to invoke JBoss VFS API.", exc);
        }
    }

    private static void generateJarFromJBossVfsFolder(Object jarVirtualFile,
                                                      Path tempJar) throws IOException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        // We should use reflection to use JBoss VFS API as we cannot
        // afford a
        // dependency to WildFly or JBoss
        Class virtualFileClass = jarVirtualFile.getClass();
        Method getChildrenRecursivelyMethod = virtualFileClass
                .getMethod("getChildrenRecursively");
        Method openStreamMethod = virtualFileClass.getMethod("openStream");
        Method isFileMethod = virtualFileClass.getMethod("isFile");
        Method getPathNameRelativeToMethod = virtualFileClass
                .getMethod("getPathNameRelativeTo", virtualFileClass);

        List jarVirtualChildren = (List) getChildrenRecursivelyMethod
                .invoke(jarVirtualFile);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(
                Files.newOutputStream(tempJar))) {
            for (Object child : jarVirtualChildren) {
                if (!(Boolean) isFileMethod.invoke(child))
                    continue;

                String relativePath = (String) getPathNameRelativeToMethod
                        .invoke(child, jarVirtualFile);
                InputStream inputStream = (InputStream) openStreamMethod
                        .invoke(child);
                ZipEntry zipEntry = new ZipEntry(relativePath);
                zipOutputStream.putNextEntry(zipEntry);
                IOUtils.copy(inputStream, zipOutputStream);
                zipOutputStream.closeEntry();
            }
        }
    }
}