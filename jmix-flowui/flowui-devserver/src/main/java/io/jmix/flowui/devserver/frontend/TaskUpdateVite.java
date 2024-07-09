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

package io.jmix.flowui.devserver.frontend;

import com.vaadin.flow.server.frontend.FallibleCommand;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Updates the Vite configuration files according with current project settings.
 */
public class TaskUpdateVite implements FallibleCommand, Serializable {

    private final Options options;

    private final Set<String> webComponentTags;
    private static final String[] reactPluginTemplatesUsedInStarters = new String[] {
            getSimplifiedTemplate("vite.config-react.ts"),
            getSimplifiedTemplate("vite.config-react-swc.ts") };

    static final String FILE_SYSTEM_ROUTER_DEPENDENCY = "@vaadin/hilla-file-router/vite-plugin.js";

    TaskUpdateVite(Options options, Set<String> webComponentTags) {
        this.options = options;
        this.webComponentTags = webComponentTags;
    }

    private static String getSimplifiedTemplate(String string) {
        return simplifyTemplate(getTemplate(string));
    }

    private static String getTemplate(String string) {
        try {
            return IOUtils.toString(
                    FrontendUtils.getResourceAsStream(string),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String simplifyTemplate(String text) {
        return text.replace("\n", "").replace("\r", "").replace("\t", "")
                .replace(" ", "");
    }
    @Override
    public void execute() {
        try {
            createConfig();
            createGeneratedConfig();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createConfig() throws IOException {
        File configFile = new File(options.getStudioFolder(),
                FrontendUtils.VITE_CONFIG);
        if (configFile.exists()) {
            if (!replaceWithDefault(configFile)) {
                return;
            }
            log().info(
                    "Replacing vite.config.ts with the default version as the React plugin is now automatically included");
        }

        try (InputStream resource = FrontendUtils.getResourceAsStream(FrontendUtils.VITE_CONFIG)) {
            String template = IOUtils.toString(resource, StandardCharsets.UTF_8);

            // --- Special Studio logic start ---
            int freePort;
            try {
                freePort = FrontendUtils.findFreePort(60_000, 65_000);
            } catch (Exception e) {
                freePort = new Random().nextInt(60_000, 65_000);
            }

            template = template.replace("60001", String.valueOf(freePort));

            if (!configFile.exists()) {
                configFile.createNewFile();
                FileUtils.write(configFile, template, StandardCharsets.UTF_8);
                String message = String.format("Created vite configuration file: '%s'", configFile);
                log().info(message);
            } else {
                List<String> viteConfigLines = FileUtils.readLines(configFile, StandardCharsets.UTF_8);
                final String hmrPortConstDeclaration = "let hmrPort = " + freePort + ";";
                for (String line : viteConfigLines) {
                    if (line.trim().contains("let hmrPort")) {
                        viteConfigLines.set(
                                viteConfigLines.indexOf(line),
                                hmrPortConstDeclaration
                        );
                    }
                }
                FileUtils.writeLines(configFile, viteConfigLines);
                log().info(String.format("Vite configuration '%s' has been updated", configFile));
            }
            // --- Special Studio logic end ---
        }
    }

    private boolean replaceWithDefault(File configFile) throws IOException {
        String text = simplifyTemplate(
                IOUtils.toString(configFile.toURI(), StandardCharsets.UTF_8));
        for (String template : reactPluginTemplatesUsedInStarters) {
            if (text.equals(template)) {
                return true;
            }
        }
        return false;
    }

    private void createGeneratedConfig() throws IOException {
        // Always overwrite this
        File generatedConfigFile = new File(options.getStudioFolder(), FrontendUtils.VITE_GENERATED_CONFIG);
        try (InputStream resource = FrontendUtils.getResourceAsStream(
                FrontendUtils.VITE_GENERATED_CONFIG)) {
            String buildFolder = options.getBuildDirectoryName();
            String template = IOUtils.toString(resource, StandardCharsets.UTF_8);
            String relativePath = "./" + buildFolder.substring(buildFolder.indexOf("/build") + 1);
            template = template
                    .replace("#settingsImport#", relativePath + "/" + TaskUpdateSettingsFile.DEV_SETTINGS_FILE)
                    .replace("#buildFolder#", relativePath)
                    .replace("#webComponentTags#",
                            webComponentTags == null || webComponentTags.isEmpty()
                                    ? ""
                                    : String.join(";", webComponentTags));
        template = updateFileSystemRouterVitePlugin(template);
            FileIOUtils.writeIfChanged(generatedConfigFile, template);
            log().debug("Created vite generated configuration file: '{}'",
                    generatedConfigFile);
        }
    }

    private String updateFileSystemRouterVitePlugin(String template) {
        if (options.isReactEnabled() && FrontendUtils.isHillaUsed(
                options.getFrontendDirectory(), options.getClassFinder())) {
            return template
                    .replace("//#vitePluginFileSystemRouterImport#",
                            "import vitePluginFileSystemRouter from '"
                                    + FILE_SYSTEM_ROUTER_DEPENDENCY + "';")
                    .replace("//#vitePluginFileSystemRouter#",
                            ", vitePluginFileSystemRouter({isDevMode: devMode})");
        }
        return template.replace("//#vitePluginFileSystemRouterImport#", "")
                .replace("//#vitePluginFileSystemRouter#", "");
    }

    private Logger log() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
