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

import com.vaadin.flow.server.Constants;

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
        return getFrontendFolder(projectFolder);
    }

    public File getDesignerFrontendFolder() {
        return getFrontendFolder(designerFolder);
    }

    public File getProjectThemeFolder() {
        return getThemeFolder(projectFolder);
    }

    public File getDesignerThemeFolder() {
        return getThemeFolder(designerFolder);
    }

    private File getThemeFolder(File baseFolder) {
        return getFrontendFolder(baseFolder).toPath().resolve(Constants.APPLICATION_THEME_ROOT).toFile();
    }

    private File getFrontendFolder(File baseFolder) {
        File frontendFolder = baseFolder.toPath()
                .resolve("src")
                .resolve("main")
                .resolve("frontend")
                .toFile();

        File legacyFrontendFolder = baseFolder.toPath().resolve("frontend").toFile();

        return frontendFolder.exists() ? frontendFolder : legacyFrontendFolder;
    }
}
