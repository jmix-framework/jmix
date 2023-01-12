/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package io.jmix.flowui.devserver.frontend;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.PwaConfiguration;
import com.vaadin.flow.server.frontend.FallibleCommand;
import com.vaadin.flow.server.frontend.installer.NodeInstaller;
import com.vaadin.flow.server.frontend.installer.Platform;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.FrontendDependenciesScanner;
import com.vaadin.flow.theme.AbstractTheme;
import com.vaadin.flow.theme.ThemeDefinition;
import elemental.json.JsonObject;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.jmix.flowui.devserver.frontend.FrontendUtils.DEFAULT_FRONTEND_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.DEFAULT_GENERATED_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.IMPORTS_NAME;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_FRONTEND_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_GENERATED_DIR;

/**
 * An executor that it's run when the servlet context is initialised in dev-mode
 * or when flow-maven-plugin goals are run. It can chain a set of task to run.
 */
public class NodeTasks implements FallibleCommand {

    /**
     * Build a <code>NodeExecutor</code> instance.
     */
    public static class Options implements Serializable {

        private final String buildDirectory;

        private final ClassFinder classFinder;

        private final File frontendDirectory;

        private File webappResourcesDirectory = null;

        private File resourceOutputDirectory = null;

        private boolean enablePackagesUpdate = false;

        private boolean createMissingPackageJson = false;

        private boolean enableImportsUpdate = false;

        private boolean enableWebpackConfigUpdate = false;

        private boolean runNpmInstall = false;

        private Set<File> jarFiles = null;

        private boolean generateEmbeddableWebComponents = true;

        private boolean cleanNpmFiles = false;

        private File jarFrontendResourcesFolder = null;

        private File flowResourcesFolder = null;

        private File localResourcesFolder = null;

        private File localThemeFolder = null;

        private boolean useByteCodeScanner = false;

        private JsonObject tokenFileData;

        private File tokenFile;

        private boolean enablePnpm = Constants.ENABLE_PNPM_DEFAULT;

        private boolean useGlobalPnpm = false;

        private File endpointSourceFolder;

        private File endpointGeneratedOpenAPIFile;

        private File applicationProperties;

        private File frontendGeneratedFolder;

        private boolean requireHomeNodeExec;

        private boolean copyTemplates = false;

        /**
         * Directory for npm and folders and files.
         */
        private final File npmFolder;

        /**
         * Directory where generated files are written.
         */
        private final File generatedFolder;

        /**
         * Is in client-side bootstrapping mode.
         */
        private boolean useLegacyV14Bootstrap;

        /**
         * The node.js version to be used when node.js is installed
         * automatically by Vaadin, for example <code>"v16.0.0"</code>. Defaults
         * to {@value FrontendTools#DEFAULT_NODE_VERSION}.
         */
        private String nodeVersion = FrontendTools.DEFAULT_NODE_VERSION;

        /**
         * Download node.js from this URL. Handy in heavily firewalled corporate
         * environments where the node.js download can be provided from an
         * intranet mirror. Defaults to
         * {@link NodeInstaller#DEFAULT_NODEJS_DOWNLOAD_ROOT}.
         */
        private URI nodeDownloadRoot = URI
                .create(Platform.guess().getNodeDownloadRoot());

        private boolean nodeAutoUpdate = false;

        private Lookup lookup;

        /**
         * Default is true here so we do not accidentally include development
         * stuff into production.
         */
        private boolean productionMode = true;

        /**
         * The resource folder for java resources.
         */
        private File javaResourceFolder;

        /**
         * Additional npm packages to run postinstall for.
         */
        private List<String> postinstallPackages;

        private String themeValue;
        private String themeVariant;
        private String themeClass;

        private File studioFolder;

        /**
         * Create a builder instance given an specific npm folder.
         *
         * @param lookup         a {@link Lookup} to discover services used by Flow (SPI)
         * @param npmFolder      folder with the `package.json` file
         * @param buildDirectory project build directory
         */
        public Options(Lookup lookup, File npmFolder, String buildDirectory) {
            this(lookup, npmFolder,
                    new File(npmFolder,
                            System.getProperty(PARAM_GENERATED_DIR,
                                    Paths.get(buildDirectory,
                                            DEFAULT_GENERATED_DIR).toString())),
                    buildDirectory);
        }

        /**
         * Create a builder instance with custom npmFolder and generatedPath
         *
         * @param lookup         a {@link Lookup} to discover services used by Flow (SPI)
         * @param npmFolder      folder with the `package.json` file
         * @param generatedPath  folder where flow generated files will be placed.
         * @param buildDirectory project build directory
         */
        public Options(Lookup lookup, File npmFolder, File generatedPath,
                       String buildDirectory) {
            this(lookup, npmFolder, generatedPath, new File(npmFolder, System
                            .getProperty(PARAM_FRONTEND_DIR, DEFAULT_FRONTEND_DIR)),
                    buildDirectory);
        }

        /**
         * Create a builder instance with all parameters.
         *
         * @param lookup            a {@link Lookup} to discover services used by Flow (SPI)
         * @param npmFolder         folder with the `package.json` file
         * @param generatedPath     folder where flow generated files will be placed.
         * @param frontendDirectory a directory with project's frontend files
         * @param buildDirectory    project build directory
         */
        public Options(Lookup lookup, File npmFolder, File generatedPath,
                       File frontendDirectory, String buildDirectory) {
            this.lookup = lookup;
            this.classFinder = lookup.lookup(ClassFinder.class);
            this.npmFolder = npmFolder;
            this.generatedFolder = generatedPath.isAbsolute() ? generatedPath
                    : new File(npmFolder, generatedPath.getPath());
            this.frontendDirectory = frontendDirectory.isAbsolute()
                    ? frontendDirectory
                    : new File(npmFolder, frontendDirectory.getPath());
            this.buildDirectory = buildDirectory;
        }

        /**
         * Creates a <code>NodeExecutor</code> using this configuration.
         *
         * @return a <code>NodeExecutor</code> instance
         */
        public NodeTasks build() {
            return new NodeTasks(this);
        }

        /**
         * Sets the webpack related properties.
         *
         * @param webappResourcesDirectory the directory to set for webpack to output its build
         *                                 results, meant for serving from context root.
         * @param resourceOutputDirectory  the directory to output generated non-served resources,
         *                                 such as the "config/stats.json" stats file, and the
         *                                 "config/flow-build-info.json" token file.
         * @return this builder
         */
        public Options withWebpack(File webappResourcesDirectory,
                                   File resourceOutputDirectory) {
            this.enableWebpackConfigUpdate = true;
            this.webappResourcesDirectory = webappResourcesDirectory;
            this.resourceOutputDirectory = resourceOutputDirectory;
            return this;
        }

        /**
         * Sets whether to enable packages and webpack file updates. Default is
         * <code>true</code>.
         *
         * @param enablePackagesUpdate <code>true</code> to enable packages and webpack update,
         *                             otherwise <code>false</code>
         * @return this builder
         */
        public Options enablePackagesUpdate(boolean enablePackagesUpdate) {
            this.enablePackagesUpdate = enablePackagesUpdate;
            return this;
        }

        /**
         * Sets whether to perform always perform clean up procedure. Default is
         * <code>false</code>. When the value is false, npm related files will
         * only be removed when a platform version update is detected.
         *
         * @param forceClean <code>true</code> to clean npm files always, otherwise
         *                   <code>false</code>
         * @return this builder
         */
        // This method is only used in tests ...
        Options enableNpmFileCleaning(boolean forceClean) {
            this.cleanNpmFiles = forceClean;
            return this;
        }

        /**
         * Sets whether to enable imports file update. Default is
         * <code>false</code>. This will also enable creation of missing package
         * files if set to true.
         *
         * @param enableImportsUpdate <code>true</code> to enable imports file update, otherwise
         *                            <code>false</code>
         * @return this builder
         */
        public Options enableImportsUpdate(boolean enableImportsUpdate) {
            this.enableImportsUpdate = enableImportsUpdate;
            this.createMissingPackageJson = enableImportsUpdate
                    || createMissingPackageJson;
            return this;
        }

        /**
         * Sets whether run <code>npm install</code> after updating
         * dependencies.
         *
         * @param runNpmInstall run npm install. Default is <code>false</code>
         * @return the builder
         */
        public Options runNpmInstall(boolean runNpmInstall) {
            this.runNpmInstall = runNpmInstall;
            return this;
        }

        /**
         * Sets the appropriate npm package folder for copying flow resources in
         * jars.
         *
         * @param jarFrontendResourcesFolder
         *            target folder
         * @return the builder
         */
        public Options withJarFrontendResourcesFolder(
                File jarFrontendResourcesFolder) {
            this.jarFrontendResourcesFolder = jarFrontendResourcesFolder
                    .isAbsolute() ? jarFrontendResourcesFolder
                    : new File(npmFolder,
                    jarFrontendResourcesFolder.getPath());
            return this;
        }

        /**
         * Sets whether copy resources from classpath to the appropriate npm
         * package folder so as they are available for webpack build.
         *
         * @param jars set of class nodes to be visited. Not {@code null}
         * @return the builder
         */
        public Options copyResources(Set<File> jars) {
            Objects.requireNonNull(jars, "Parameter 'jars' must not be null!");
            this.jarFiles = jars;
            return this;
        }

        /**
         * Sets whether copy templates to
         * {@code META-INF/VAADIN/config/templates}.
         *
         * @param copyTemplates whether to copy templates
         * @return the builder
         */
        public Options copyTemplates(boolean copyTemplates) {
            this.copyTemplates = copyTemplates;
            return this;
        }

        /**
         * Sets whether to collect and package
         * {@link com.vaadin.flow.component.WebComponentExporter} dependencies.
         *
         * @param generateEmbeddableWebComponents collect dependencies. Default is {@code true}
         * @return the builder
         */
        public Options withEmbeddableWebComponents(
                boolean generateEmbeddableWebComponents) {
            this.generateEmbeddableWebComponents = generateEmbeddableWebComponents;
            return this;
        }

        /**
         * Sets whether to create the package file if missing.
         *
         * @param create create the package
         * @return the builder
         */
        public Options createMissingPackageJson(boolean create) {
            this.createMissingPackageJson = create;
            return this;
        }

        /**
         * Set local frontend files to be copied from given folder.
         *
         * @param localResourcesFolder folder to copy local frontend files from
         * @return the builder, for chaining
         */
        public Options copyLocalResources(File localResourcesFolder) {
            this.localResourcesFolder = localResourcesFolder;
            return this;
        }

        /**
         * Use V14 bootstrapping that disables index.html entry point.
         *
         * @param useDeprecatedV14Bootstrapping <code>true</code> to use legacy V14 bootstrapping
         * @return the builder, for chaining
         */
        public Options useV14Bootstrap(boolean useDeprecatedV14Bootstrapping) {
            this.useLegacyV14Bootstrap = useDeprecatedV14Bootstrapping;
            return this;
        }

        /**
         * Set the folder where frontend files should be generated.
         *
         * @param frontendGeneratedFolder folder to generate frontend files in.
         * @return the builder, for chaining
         */
        public Options withFrontendGeneratedFolder(
                File frontendGeneratedFolder) {
            this.frontendGeneratedFolder = frontendGeneratedFolder;
            return this;
        }

        /**
         * Set application properties file for Spring project.
         *
         * @param applicationProperties application properties file.
         * @return this builder, for chaining
         */
        public Options withApplicationProperties(File applicationProperties) {
            this.applicationProperties = applicationProperties;
            return this;
        }

        /**
         * Set output location for the generated OpenAPI file.
         *
         * @param endpointGeneratedOpenAPIFile the generated output file.
         * @return the builder, for chaining
         */
        public Options withEndpointGeneratedOpenAPIFile(
                File endpointGeneratedOpenAPIFile) {
            this.endpointGeneratedOpenAPIFile = endpointGeneratedOpenAPIFile;
            return this;
        }

        /**
         * Set source paths that OpenAPI generator searches for endpoints.
         *
         * @param endpointSourceFolder java source folder
         * @return the builder, for chaining
         */
        public Options withEndpointSourceFolder(File endpointSourceFolder) {
            this.endpointSourceFolder = endpointSourceFolder;
            return this;
        }

        /**
         * Sets frontend scanner strategy: byte code scanning strategy is used
         * if {@code byteCodeScanner} is {@code true}, full classpath scanner
         * strategy is used otherwise (by default).
         *
         * @param byteCodeScanner if {@code true} then byte code scanner is used, full
         *                        scanner is used otherwise (by default).
         * @return the builder, for chaining
         */
        public Options useByteCodeScanner(boolean byteCodeScanner) {
            this.useByteCodeScanner = byteCodeScanner;
            return this;
        }

        /**
         * Fill token file data into the provided {@code object}.
         *
         * @param object the object to fill with token file data
         * @return the builder, for chaining
         */
        public Options populateTokenFileData(JsonObject object) {
            tokenFileData = object;
            return this;
        }

        /**
         * Sets the token file (flow-build-info.json) path.
         *
         * @param tokenFile token file path
         * @return the builder, for chaining
         */
        public Options withTokenFile(File tokenFile) {
            this.tokenFile = tokenFile;
            return this;
        }

        /**
         * Enables pnpm tool.
         * <p>
         * "pnpm" will be used instead of "npm".
         *
         * @param enable enables pnpm.
         * @return the builder, for chaining
         */
        public Options enablePnpm(boolean enable) {
            enablePnpm = enable;
            return this;
        }

        /**
         * Uses globally installed pnpm tool for frontend packages installation.
         *
         * @param useGlobalPnpm uses globally installed pnpm instead of default one, see
         *                      {@link FrontendTools#DEFAULT_PNPM_VERSION}.
         * @return the builder, for chaining
         */
        public Options useGlobalPnpm(boolean useGlobalPnpm) {
            this.useGlobalPnpm = useGlobalPnpm;
            return this;
        }

        /**
         * Requires node executable to be installed in vaadin home folder.
         *
         * @param requireHomeNodeExec requires vaadin home node exec
         * @return the builder, for chaining
         */
        public Options withHomeNodeExecRequired(boolean requireHomeNodeExec) {
            this.requireHomeNodeExec = requireHomeNodeExec;
            return this;
        }

        /**
         * Sets the node.js version to be used when node.js is installed
         * automatically by Vaadin, for example <code>"v16.0.0"</code>. Defaults
         * to {@value FrontendTools#DEFAULT_NODE_VERSION}.
         *
         * @param nodeVersion the new node version to download, not null.
         * @return the builder, for chaining
         */
        public Options withNodeVersion(String nodeVersion) {
            this.nodeVersion = Objects.requireNonNull(nodeVersion);
            return this;
        }

        /**
         * Sets the download node.js URL. Handy in heavily firewalled corporate
         * environments where the node.js download can be provided from an
         * intranet mirror. Defaults to
         * {@link NodeInstaller#DEFAULT_NODEJS_DOWNLOAD_ROOT}.
         *
         * @param nodeDownloadRoot the new download URL to set, not null.
         * @return the builder, for chaining
         */
        public Options withNodeDownloadRoot(URI nodeDownloadRoot) {
            this.nodeDownloadRoot = Objects.requireNonNull(nodeDownloadRoot);
            return this;
        }

        /**
         * Sets the production mode.
         *
         * @param productionMode <code>true</code> to enable production mode, otherwise
         *                       <code>false</code>
         * @return this builder
         */
        public Options withProductionMode(boolean productionMode) {
            this.productionMode = productionMode;
            return this;
        }

        /**
         * Sets whether it is fine to automatically update the alternate node
         * installation if installed version is older than the current default.
         *
         * @param update true to update alternate node when used
         * @return the builder
         */
        public Options setNodeAutoUpdate(boolean update) {
            this.nodeAutoUpdate = update;
            return this;
        }

        public Options withThemeValue(String themeValue) {
            this.themeValue = themeValue;
            return this;
        }

        public String getThemeValue() {
            return themeValue;
        }

        public Options withThemeVariant(String themeVariant) {
            this.themeVariant = themeVariant;
            return this;
        }

        public String getThemeVariant() {
            return themeVariant;
        }

        public Options withThemeClass(String themeClass) {
            this.themeClass = themeClass;
            return this;
        }

        public String getThemeClass() {
            return themeClass;
        }

        public Options withStudioFolder(File studioFolder) {
            this.studioFolder = studioFolder;
            return this;
        }

        /**
         * Set local theme files to be copied from given folder.
         *
         * @param localThemeFolder folder to copy local theme files from
         * @return the builder, for chaining
         */
        public Options copyLocalThemes(File localThemeFolder) {
            this.localThemeFolder = localThemeFolder;
            return this;
        }

        public File getLocalThemeFolder() {
            return localThemeFolder;
        }

        public File getStudioFolder() {
            return studioFolder;
        }

        /**
         * Get the npm folder used for this build.
         *
         * @return npmFolder
         */
        public File getNpmFolder() {
            return npmFolder;
        }

        /**
         * Get the generated folder for this build.
         *
         * @return generatedFolder
         */
        public File getGeneratedFolder() {
            return generatedFolder;
        }

        /**
         * Get the output directory for webpack output.
         *
         * @return webpackOutputDirectory
         */
        public File getWebappResourcesDirectory() {
            return webappResourcesDirectory;
        }

        /**
         * Get the defined frontend directory.
         *
         * @return frontendDirectory
         */
        public File getFrontendDirectory() {
            return frontendDirectory;
        }

        /**
         * Get the name of the used build directory.
         * <p>
         * By default this will be {@code target} for maven and {@code build}
         * for gradle.
         *
         * @return buildDirectory
         */
        public String getBuildDirectory() {
            return buildDirectory;
        }

        /**
         * Set the java resources folder to be checked for feature file.
         * <p>
         * Needed for plugin execution.
         *
         * @param javaResourceFolder java resources folder
         * @return this builder
         */
        public Options setJavaResourceFolder(File javaResourceFolder) {
            this.javaResourceFolder = javaResourceFolder;
            return this;
        }

        protected FeatureFlags getFeatureFlags() {
            final FeatureFlags featureFlags = new FeatureFlags(lookup);
            if (javaResourceFolder != null) {
                featureFlags.setPropertiesLocation(javaResourceFolder);
            }
            return featureFlags;
        }

        /**
         * Sets the additional npm packages to run {@code postinstall} for.
         * <p>
         * By default, postinstall is only run for internal dependencies which
         * rely on post install scripts to work, e.g. esbuild
         *
         * @param postinstallPackages the additional npm packages to run postinstall for
         * @return the builder, for chaining
         */
        public Options withPostinstallPackages(
                List<String> postinstallPackages) {
            this.postinstallPackages = postinstallPackages;
            return this;
        }

        public File getJarFrontendResourcesFolder() {
            return jarFrontendResourcesFolder;
        }
    }

    // @formatter:off
    // This list keeps the tasks in order so that they are executed
    // without depending on when they are added.
    private static final List<Class<? extends FallibleCommand>> commandOrder =
            Collections.unmodifiableList(Arrays.asList(
            TaskNotifyWebpackConfExistenceWhileUsingVite.class,
                    TaskGeneratePackageJson.class,
                    TaskGenerateIndexHtml.class,
                    TaskGenerateIndexTs.class,
                    TaskGenerateViteDevMode.class,
                    TaskGenerateTsConfig.class,
                    TaskGenerateTsDefinitions.class,
                    TaskGenerateServiceWorker.class,
                    TaskGenerateHilla.class,
                    TaskGenerateOpenAPI.class,
                    TaskGenerateEndpoint.class,
                    TaskGenerateBootstrap.class,
                    TaskGenerateFeatureFlags.class,
                    TaskInstallWebpackPlugins.class,
                    TaskUpdatePackages.class,
                    TaskRunNpmInstall.class,
                    TaskCopyFrontendFiles.class,
                    TaskCopyLocalFrontendFiles.class,
                    TaskCopyLocalThemeFiles.class,
                    TaskUpdateSettingsFile.class,
                    TaskUpdateWebpack.class,
                    TaskUpdateVite.class,
                    TaskUpdateImports.class,
                    TaskUpdateThemeImport.class,
                    TaskCopyTemplateFiles.class
            ));
    // @formatter:on

    private final List<FallibleCommand> commands = new ArrayList<>();

    private ThemeDefinition createThemeDefinition(Options options, ClassFinder classFinder) {
        if (options.themeClass == null) {
            return null;
        }

        Class<? extends AbstractTheme> themeClass = getThemeClass(classFinder, options.themeClass);
        return new ThemeDefinition(themeClass, options.themeVariant, options.themeValue);
    }

    private Class<? extends AbstractTheme> getThemeClass(ClassFinder classFinder, String className) {
        try {
            return classFinder.loadClass(className);
        } catch (ClassNotFoundException ignore) { // NOSONAR
            return null;
        }
    }

    private NodeTasks(Options options) {

        ClassFinder classFinder = new ClassFinder.CachedClassFinder(options.classFinder);
        FrontendDependenciesScanner frontendDependencies = null;
        ThemeDefinition themeDefinition = createThemeDefinition(options, classFinder);

        final FeatureFlags featureFlags = options.getFeatureFlags();

        if (options.enablePackagesUpdate || options.enableImportsUpdate
                || options.enableWebpackConfigUpdate) {
            frontendDependencies = new FrontendDependenciesScanner.FrontendDependenciesScannerFactory()
                    .createScanner(!options.useByteCodeScanner, classFinder,
                            options.generateEmbeddableWebComponents,
                            options.useLegacyV14Bootstrap, featureFlags);

            if (options.generateEmbeddableWebComponents) {
                FrontendWebComponentGenerator generator = new FrontendWebComponentGenerator(classFinder);
                generator.generateWebComponents(options.generatedFolder, themeDefinition);
            }

            TaskUpdatePackages packageUpdater = null;
            if (options.enablePackagesUpdate
                    && options.jarFrontendResourcesFolder != null) {
                packageUpdater = new TaskUpdatePackages(classFinder,
                        frontendDependencies, options.npmFolder, options.studioFolder,
                        options.generatedFolder, options.jarFrontendResourcesFolder,
                        options.cleanNpmFiles, options.enablePnpm,
                        options.buildDirectory, featureFlags);
                commands.add(packageUpdater);
            }

            if (packageUpdater != null && options.runNpmInstall) {
                commands.add(new TaskRunNpmInstall(packageUpdater,
                        options.enablePnpm, options.requireHomeNodeExec,
                        options.nodeVersion, options.nodeDownloadRoot,
                        options.useGlobalPnpm, options.nodeAutoUpdate,
                        options.postinstallPackages));

                commands.add(new TaskInstallWebpackPlugins(
                        new File(options.npmFolder, options.buildDirectory)));
            }

        }

        if (options.createMissingPackageJson) {
            TaskGeneratePackageJson packageCreator = new TaskGeneratePackageJson(
                    options.npmFolder, options.studioFolder,
                    options.generatedFolder, options.buildDirectory, featureFlags);
            commands.add(packageCreator);
        }

        if (frontendDependencies != null) {
            addGenerateServiceWorkerTask(options);
            addGenerateTsConfigTask(options);
        }

        if (options.useLegacyV14Bootstrap) {
            if (!featureFlags.isEnabled(FeatureFlags.WEBPACK)) {
                throw new IllegalStateException("V14_BOOTSTRAPPING_VITE_ERROR_MESSAGE");
            }
        } else {
            addBootstrapTasks(options);

            // use the new Hilla generator if enabled, otherwise use the old
            // generator.
            TaskGenerateHilla hillaTask;
            if (options.endpointGeneratedOpenAPIFile != null
                    && featureFlags.isEnabled(FeatureFlags.HILLA_ENGINE)
                    && (hillaTask = options.lookup
                    .lookup(TaskGenerateHilla.class)) != null) {
                hillaTask.configure(options.getNpmFolder(),
                        options.getBuildDirectory());
                commands.add(hillaTask);
            } else if (options.endpointGeneratedOpenAPIFile != null
                    && options.endpointSourceFolder != null
                    && options.endpointSourceFolder.exists()) {
                addEndpointServicesTasks(options);
            }

            commands.add(new TaskGenerateBootstrap(frontendDependencies,
                    options.frontendDirectory, options.productionMode, themeDefinition));

            commands.add(new TaskGenerateFeatureFlags(options.frontendDirectory,
                    featureFlags));
        }

        if (options.jarFiles != null
                && options.jarFrontendResourcesFolder != null) {
            commands.add(new TaskCopyFrontendFiles(
                    options.jarFrontendResourcesFolder, options.jarFiles));
        }

        if (options.localResourcesFolder != null
                && options.jarFrontendResourcesFolder != null) {
            commands.add(new TaskCopyLocalFrontendFiles(
                    options.jarFrontendResourcesFolder,
                    options.localResourcesFolder));
        }

        if (options.frontendDirectory != null
                && options.localThemeFolder != null) {
            commands.add(new TaskCopyLocalThemeFiles(
                    new File(options.frontendDirectory, "themes"), options.localThemeFolder));
        }

        if (!featureFlags.isEnabled(FeatureFlags.WEBPACK)) {
            String themeName = "";
            PwaConfiguration pwa;
            if (frontendDependencies != null) {
                if (frontendDependencies.getThemeDefinition() != null) {
                    themeName = frontendDependencies.getThemeDefinition().getName();
                }
                pwa = frontendDependencies.getPwaConfiguration();
            } else {
                pwa = new PwaConfiguration();
            }
            commands.add(new TaskNotifyWebpackConfExistenceWhileUsingVite(
                    options.npmFolder));
            commands.add(new TaskUpdateSettingsFile(options, themeName, pwa));
            commands.add(new TaskUpdateVite(options.studioFolder, options.buildDirectory));
        } else if (options.enableWebpackConfigUpdate) {
            PwaConfiguration pwaConfiguration = frontendDependencies.getPwaConfiguration();
            commands.add(new TaskUpdateWebpack(options.frontendDirectory,
                    options.studioFolder, options.webappResourcesDirectory,
                    options.resourceOutputDirectory,
                    new File(options.generatedFolder, IMPORTS_NAME),
                    options.useLegacyV14Bootstrap, pwaConfiguration, options.buildDirectory));
        }

        if (options.enableImportsUpdate) {
            commands.add(new TaskUpdateImports(classFinder,
                    frontendDependencies,
                    finder -> getFallbackScanner(options, finder, featureFlags),
                    options.npmFolder, options.studioFolder, options.generatedFolder,
                    options.frontendDirectory, options.tokenFile,
                    options.tokenFileData, options.enablePnpm,
                    options.buildDirectory, options.productionMode,
                    options.useLegacyV14Bootstrap, featureFlags, themeDefinition));

            commands.add(new TaskUpdateThemeImport(options.studioFolder,
                    themeDefinition,
                    options.frontendDirectory));
        }

        if (options.copyTemplates) {
            commands.add(new TaskCopyTemplateFiles(classFinder,
                    options.npmFolder, options.resourceOutputDirectory,
                    options.frontendDirectory));
        }
    }

    private void addBootstrapTasks(Options builder) {
        TaskGenerateIndexHtml taskGenerateIndexHtml = new TaskGenerateIndexHtml(
                builder.frontendDirectory);
        commands.add(taskGenerateIndexHtml);
        File buildDirectory = new File(builder.npmFolder,
                builder.buildDirectory);
        TaskGenerateIndexTs taskGenerateIndexTs = new TaskGenerateIndexTs(
                builder.frontendDirectory,
                new File(builder.generatedFolder, IMPORTS_NAME),
                buildDirectory);
        commands.add(taskGenerateIndexTs);
        if (!builder.getFeatureFlags().isEnabled(FeatureFlags.WEBPACK)
                && !builder.productionMode) {
            commands.add(
                    new TaskGenerateViteDevMode(builder.frontendDirectory));
        }
    }

    private void addGenerateTsConfigTask(Options builder) {
        TaskGenerateTsConfig taskGenerateTsConfig = new TaskGenerateTsConfig(
                builder.studioFolder, builder.getFeatureFlags());
        commands.add(taskGenerateTsConfig);

        TaskGenerateTsDefinitions taskGenerateTsDefinitions = new TaskGenerateTsDefinitions(
                builder.studioFolder);
        commands.add(taskGenerateTsDefinitions);

    }

    private void addGenerateServiceWorkerTask(Options builder) {
        File outputDirectory = new File(builder.npmFolder, builder.buildDirectory);
        commands.add(new TaskGenerateServiceWorker(
                builder.frontendDirectory, outputDirectory));
    }

    private void addEndpointServicesTasks(Options builder) {
        Lookup lookup = builder.lookup;
        EndpointGeneratorTaskFactory endpointGeneratorTaskFactory = lookup
                .lookup(EndpointGeneratorTaskFactory.class);

        if (endpointGeneratorTaskFactory != null) {
            TaskGenerateOpenAPI taskGenerateOpenAPI = endpointGeneratorTaskFactory
                    .createTaskGenerateOpenAPI(builder.applicationProperties,
                            builder.endpointSourceFolder,
                            builder.classFinder.getClassLoader(),
                            builder.endpointGeneratedOpenAPIFile);
            commands.add(taskGenerateOpenAPI);

            if (builder.frontendGeneratedFolder != null) {
                TaskGenerateEndpoint taskGenerateEndpoint = endpointGeneratorTaskFactory
                        .createTaskGenerateEndpoint(
                                builder.applicationProperties,
                                builder.endpointGeneratedOpenAPIFile,
                                builder.frontendGeneratedFolder,
                                builder.frontendDirectory);
                commands.add(taskGenerateEndpoint);
            }
        }
    }

    private FrontendDependenciesScanner getFallbackScanner(Options builder,
                                                           ClassFinder finder, FeatureFlags featureFlags) {
        if (builder.useByteCodeScanner) {
            return new FrontendDependenciesScanner.FrontendDependenciesScannerFactory()
                    .createScanner(true, finder,
                            builder.generateEmbeddableWebComponents,
                            builder.useLegacyV14Bootstrap, featureFlags, true);
        } else {
            return null;
        }
    }

    @Override
    public void execute() throws ExecutionFailedException {
        sortCommands(commands);

        for (FallibleCommand command : commands) {
            FrontendUtils.logInFile("Executing task: " + command.getClass().getSimpleName());
            command.execute();
        }
    }

    /**
     * Sort command list so we always execute commands in a pre-defined order.
     *
     * @param commandList list of FallibleCommands to sort
     */
    private void sortCommands(List<FallibleCommand> commandList) {
        commandList.sort((c1, c2) -> {
            final int indexOf1 = getIndex(c1);
            final int indexOf2 = getIndex(c2);
            if (indexOf1 == -1 || indexOf2 == -1) {
                return 0;
            }
            return indexOf1 - indexOf2;
        });
    }

    /**
     * Find index of command for which it is assignable to.
     *
     * @param command command to find execution index for
     * @return index of command or -1 if not available
     */
    private int getIndex(FallibleCommand command) {
        int index = commandOrder.indexOf(command.getClass());
        if (index != -1) {
            return index;
        }
        for (int i = 0; i < commandOrder.size(); i++) {
            if (commandOrder.get(i).isAssignableFrom(command.getClass())) {
                return i;
            }
        }
        throw new UnknownTaskException(command);
    }
}
