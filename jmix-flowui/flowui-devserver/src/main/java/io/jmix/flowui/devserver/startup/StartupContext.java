/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.devserver.startup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.vaadin.flow.server.Constants;
import org.apache.commons.io.FileUtils;

public record StartupContext(
        String themeName,
        File projectFolder,
        File designerFolder
) {
    public File findFileInProjectFolder(String fileName) {
        return new File(projectFolder, fileName);
    }

    public File findFileInDesignerFolder(String fileName) {
        return new File(designerFolder, fileName);
    }

    public File getProjectFrontendFolder() {
        return getFrontendFolder(projectFolder, false);
    }

    public File getDesignerFrontendFolder() {
        return getFrontendFolder(designerFolder, true);
    }

    public File getProjectLegacyThemesFolder() {
        return getLegacyThemesFolder(projectFolder);
    }

    public File getDesignerLegacyThemesFolder() {
        return getLegacyThemesFolder(designerFolder);
    }

    public File getProjectMetaInfResourcesFolder() {
        return getMetaInfResourcesFolder(projectFolder);
    }

    private File getLegacyThemesFolder(File baseFolder) {
        return getFrontendFolder(baseFolder, false).toPath().resolve(Constants.APPLICATION_THEME_ROOT).toFile();
    }

    private File getFrontendFolder(File baseFolder, boolean createIfNotExist) {
        File frontendFolder = baseFolder.toPath()
                .resolve("src")
                .resolve("main")
                .resolve("frontend")
                .toFile();

        File legacyFrontendFolder = baseFolder.toPath().resolve("frontend").toFile();

        if (createIfNotExist && !frontendFolder.exists()) {
            try {
                FileUtils.forceMkdir(frontendFolder);
                return frontendFolder;
            } catch (IOException ignored) {
            }
        }

        return frontendFolder.exists() ? frontendFolder : legacyFrontendFolder;
    }

    private File getMetaInfResourcesFolder(File baseFolder) {
        Path classpathResources = baseFolder.toPath()
                .resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("META-INF")
                .resolve("resources");
        return classpathResources.toFile();
    }
}
