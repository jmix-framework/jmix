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

import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.server.PwaConfiguration;
import com.vaadin.flow.server.frontend.FallibleCommand;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.vaadin.flow.shared.ApplicationConstants.VAADIN_STATIC_FILES_PATH;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.BOOTSTRAP_FILE_NAME;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.GENERATED;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.INDEX_HTML;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.SERVICE_WORKER_SRC;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.SERVICE_WORKER_SRC_JS;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.WEBPACK_CONFIG;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.WEBPACK_GENERATED;

public class TaskUpdateWebpack implements FallibleCommand {
    private final Path webpackOutputPath;
    private final Path resourceOutputPath;
    private final Path flowImportsFilePath;
    private final Path studioPath;
    private final Path frontendDirectory;
    private final boolean useV14Bootstrapping;
    private final PwaConfiguration pwaConfiguration;
    private final Path resourceFolder;
    private final Path frontendGeneratedFolder;
    private final String buildFolder;

    /**
     * Create an instance of the updater given all configurable parameters.
     *
     * @param frontendDirectory
     *            the directory used for {@code Frontend} alias
     * @param studioFolder
     *            folder with the `webpack.config.js` file.
     * @param webpackOutputDirectory
     *            the directory to set for webpack to output its build results.
     * @param resourceOutputDirectory
     *            the directory for generated non-served resources.
     * @param generatedFlowImports
     *            name of the JS file to update with the Flow project imports
     * @param useV14Bootstrapping
     *            whether the application running with deprecated V14
     *            bootstrapping
     * @param buildFolder
     *            build target folder
     */
    @SuppressWarnings("squid:S00107")
    TaskUpdateWebpack(File frontendDirectory, File studioFolder,
                      File webpackOutputDirectory, File resourceOutputDirectory,
                      File generatedFlowImports, boolean useV14Bootstrapping,
            PwaConfiguration pwaConfiguration, String buildFolder) {
        this.frontendDirectory = frontendDirectory.toPath();
        this.webpackOutputPath = webpackOutputDirectory.toPath();
        this.resourceOutputPath = resourceOutputDirectory.toPath();
        this.flowImportsFilePath = generatedFlowImports.toPath();
        this.studioPath = studioFolder.toPath();
        this.useV14Bootstrapping = useV14Bootstrapping;
        this.pwaConfiguration = pwaConfiguration;
        this.resourceFolder = new File(webpackOutputDirectory,
                VAADIN_STATIC_FILES_PATH).toPath();
        this.frontendGeneratedFolder = this.frontendDirectory
                .resolve(GENERATED);
        this.buildFolder = buildFolder;
    }

    @Override
    public void execute() {
        try {
            createWebpackConfig();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createWebpackConfig() throws IOException {
        File configFile = new File(studioPath.toFile(), WEBPACK_CONFIG);

        // If we have an old config file we remove it and create the new one
        // using the webpack.generated.js
        if (configFile.exists()) {
            if (!FrontendUtils.isWebpackConfigFile(configFile)) {
                log().warn(
                        "Flow generated webpack configuration was not mentioned "
                                + "in the configuration file: {}."
                                + "Please verify that './webpack.generated.js' is used "
                                + "in the merge or remove the file to generate a new one.",
                        configFile);
        }
        } else {
            InputStream webpackConfig = FrontendUtils.getResourceAsStream(
                    FrontendUtils.WEBPACK_CONFIG, TaskUpdateWebpack.class.getClassLoader()
            );
            FileUtils.copyInputStreamToFile(webpackConfig, configFile);
            log().debug("Created webpack configuration file: '{}'", configFile);
    }

        // Generated file is always re-written
        File generatedFile = new File(studioPath.toFile(),
                WEBPACK_GENERATED);

        URL resource = this.getClass().getClassLoader()
                .getResource(FrontendUtils.WEBPACK_GENERATED);
        FileUtils.copyURLToFile(resource, generatedFile);
        List<String> lines = modifyWebpackConfig(generatedFile);
        FileUtils.writeLines(generatedFile, lines);
    }

    private List<String> modifyWebpackConfig(File generatedFile)
            throws IOException {
        List<String> lines = FileUtils.readLines(generatedFile,
                StandardCharsets.UTF_8);
        List<Pair<String, String>> replacements = getReplacements();
        String declaration = "%s = %s;";

        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < replacements.size(); j++) {
                Pair<String, String> pair = replacements.get(j);
                if (lines.get(i).startsWith(pair.getFirst() + " ")) {
                    lines.set(i, String.format(declaration, pair.getFirst(),
                            pair.getSecond()));
                }
            }
        }
        return lines;
    }

    private List<Pair<String, String>> getReplacements() {
        return Arrays.asList(
                new Pair<>("const frontendFolder",
                        formatPathResolve(getEscapedRelativeWebpackPath(
                                frontendDirectory))),
                new Pair<>("const frontendGeneratedFolder",
                        formatPathResolve(getEscapedRelativeWebpackPath(
                                frontendGeneratedFolder))),
                new Pair<>("const mavenOutputFolderForFlowBundledFiles",
                        formatPathResolve(getEscapedRelativeWebpackPath(
                                webpackOutputPath))),
                new Pair<>("const mavenOutputFolderForResourceFiles",
                        formatPathResolve(getEscapedRelativeWebpackPath(
                                resourceOutputPath))),
                new Pair<>("const fileNameOfTheFlowGeneratedMainEntryPoint",
                        formatPathResolve(getEscapedRelativeWebpackPath(
                                flowImportsFilePath))),
                new Pair<>("const useClientSideIndexFileForBootstrapping",
                        Boolean.toString(!useV14Bootstrapping)),
                new Pair<>("const clientSideIndexHTML",
                        "'./" + INDEX_HTML + "'"),
                new Pair<>("const clientSideIndexEntryPoint",
                        getClientEntryPoint()),
                new Pair<>("const pwaEnabled",
                        Boolean.toString(pwaConfiguration.isEnabled())),
                new Pair<>("const offlineEnabled",
                        Boolean.toString(pwaConfiguration.isOfflineEnabled())),
                new Pair<>("const offlinePath", getOfflinePath()),
                new Pair<>("const clientServiceWorkerEntryPoint",
                        getClientServiceWorker()),
                new Pair<>("const flowFrontendFolder",
                        formatPathResolve(getEscapedRelativeWebpackPath(
                                getJarFrontendResourcesFolder()))),
                new Pair<>("const projectStaticAssetsOutputFolder",
                        formatPathResolve(
                                getEscapedRelativeWebpackPath(resourceFolder))),
                new Pair<>("const buildDirectory",
                        formatPathResolve(getEscapedRelativeWebpackPath(
                                Paths.get(buildFolder)))));
    }

    private Path getJarFrontendResourcesFolder() {
        return frontendGeneratedFolder
                .resolve(FrontendUtils.JAR_RESOURCES_FOLDER);
    }

    private String getClientEntryPoint() {
        return String.format("path.resolve(__dirname, '%s', '%s', '%s');",
                getEscapedRelativeWebpackPath(frontendDirectory), GENERATED,
                BOOTSTRAP_FILE_NAME);
    }

    private String getClientServiceWorker() {
        String outputDirectory = Paths.get(studioPath.toString(), "build").toString();
        boolean exists = new File(outputDirectory, SERVICE_WORKER_SRC).exists()
                || new File(outputDirectory, SERVICE_WORKER_SRC_JS).exists();
        if (!exists) {
            Path path = Paths.get(
                    getEscapedRelativeWebpackPath(studioPath),
                    buildFolder, SERVICE_WORKER_SRC);
            return formatPathResolve(path.toString().replaceFirst("\\.[tj]s$", ""));
        } else {
            return formatPathResolve("build/sw");
        }
    }

    private String getEscapedRelativeWebpackPath(Path path) {
        if (path.isAbsolute()) {
            return FrontendUtils.getUnixRelativePath(studioPath, path);
        } else {
            return FrontendUtils.getUnixPath(path);
        }
    }

    private String getOfflinePath() {
        if (pwaConfiguration.isOfflinePathEnabled()) {
            return "'" + getEscapedRelativeWebpackPath(
                    Paths.get(pwaConfiguration.getOfflinePath())) + "'";
        }
        return "'.'";
    }

    private String formatPathResolve(String path) {
        return String.format("path.resolve(__dirname, '%s')", path);
    }

    private Logger log() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
