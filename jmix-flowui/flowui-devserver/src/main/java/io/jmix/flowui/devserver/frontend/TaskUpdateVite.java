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

/**
 * Updates the Vite configuration files according to current project settings.
 */
public class TaskUpdateVite implements FallibleCommand, Serializable {

    private final File configFolder;
    private final String buildFolder;

    TaskUpdateVite(File configFolder, String buildFolder) {
        this.configFolder = configFolder;
        this.buildFolder = buildFolder;
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
        // Only create it if it does not exist
        File configFile = new File(configFolder, FrontendUtils.VITE_CONFIG);

        InputStream resource;
        resource = FrontendUtils.getResourceAsStream(
                FrontendUtils.VITE_CONFIG, TaskUpdateVite.class.getClassLoader()
        );

        String template = IOUtils.toString(resource, StandardCharsets.UTF_8);

        if (configFile.exists()
                && template.hashCode() == IOUtils.toString(
                        configFile.toURI(), StandardCharsets.UTF_8).hashCode()
        ) {
            return;
        }

        FileUtils.write(configFile, template, StandardCharsets.UTF_8);
        log().debug("Created vite configuration file: '{}'", configFile);

    }

    private void createGeneratedConfig() throws IOException {
        // Always overwrite this
        File generatedConfigFile = new File(configFolder, FrontendUtils.VITE_GENERATED_CONFIG);
        InputStream resource;
        resource = FrontendUtils.getResourceAsStream(
                FrontendUtils.VITE_GENERATED_CONFIG, TaskUpdateVite.class.getClassLoader()
        );
        String template = IOUtils.toString(resource, StandardCharsets.UTF_8);
        String relativePath = "./" + buildFolder.substring(buildFolder.indexOf("/build") + 1);
        template = template
                .replace("#settingsImport#", relativePath + "/" + TaskUpdateSettingsFile.DEV_SETTINGS_FILE)
                .replace("#buildFolder#", relativePath);
        FileUtils.write(generatedConfigFile, template, StandardCharsets.UTF_8);
        log().debug("Created vite generated configuration file: '{}'", generatedConfigFile);
    }

    private Logger log() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
