/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.devserver.frontend;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.FallibleCommand;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * Copy themes files from {projectFolder}/frontend/themes
 * to {projectFolder}/.jmix/screen-designer/frontend/themes
 */
public class TaskCopyThemes implements FallibleCommand, Serializable {

    private final Options options;

    public TaskCopyThemes(Options options) {
        this.options = options;
    }

    @Override
    public void execute() throws ExecutionFailedException {
        File projectFolder = options.getNpmFolder();
        if (projectFolder.exists()) {
            File projectThemesFolder = projectFolder.toPath().resolve("frontend").resolve("themes").toFile();
            if (projectThemesFolder.exists() && projectThemesFolder.isDirectory()) {
                try {
                    Path studioThemesFolder = options.getFrontendDirectory().toPath().resolve("themes");
                    FileUtils.copyDirectory(projectThemesFolder, studioThemesFolder.toFile());
                } catch (IOException e) {
                    FrontendUtils.console("Cannot find 'themes' folder in " + projectFolder);
                }
            }
        }
    }
}
