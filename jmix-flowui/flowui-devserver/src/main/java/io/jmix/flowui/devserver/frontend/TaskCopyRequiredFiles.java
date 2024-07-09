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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.FrontendDependenciesScanner;
import org.apache.commons.io.FileUtils;

/**
 * Special for Studio designer.
 * <p>
 * Copy <code>package-lock.json</code>, <code>.npmrc</code> from project root folder
 * and themes files from <code>{project}/src/main/frontend/themes</code>
 * or <code>{project}/frontend/themes</code>
 * to <code>{project}/.jmix/screen-designer/frontend/themes</code>
 */
public class TaskCopyRequiredFiles extends NodeUpdater {

    protected TaskCopyRequiredFiles(FrontendDependenciesScanner frontendDependencies, Options options) {
        super(frontendDependencies, options);
    }

    @Override
    public void execute() throws ExecutionFailedException {
        copyPackageLockFile();
        copyNpmrcFile();
        copyThemes();
    }

    private void copyPackageLockFile() {
        File packageLockFile = getProjectPackageLockFile();
        if (packageLockFile.exists()) {
            File studioPackageLockFile = getStudioPackageLockFile();
            try {
                FileUtils.copyFile(packageLockFile, studioPackageLockFile);
            } catch (IOException e) {
                log().warn("Exception when copying " + Constants.PACKAGE_LOCK_JSON + " file");
            }
        }
    }

    private void copyNpmrcFile() {
        String npmrcFileName = ".npmrc";
        File npmrcFile = options.getNpmFolder().toPath().resolve(npmrcFileName).toFile();
        if (npmrcFile.exists()) {
            try {
                FileUtils.copyFile(npmrcFile, options.getStudioFolder().toPath().resolve(npmrcFileName).toFile());
            } catch (IOException e) {
                log().warn("Exception when copying {} file", npmrcFileName);
            }
        }
    }

    private void copyThemes() {
        File projectFolder = options.getNpmFolder();
        if (projectFolder.exists()) {
            File projectFrontendThemesFolder = projectFolder.toPath()
                    .resolve("src")
                    .resolve("main")
                    .resolve("frontend")
                    .resolve("themes")
                    .toFile();
            File legacyProjectThemesFolder = projectFolder.toPath()
                    .resolve("frontend")
                    .resolve("themes")
                    .toFile();
            File actualThemesFolder = projectFrontendThemesFolder.exists() ? projectFrontendThemesFolder : legacyProjectThemesFolder;
            if (actualThemesFolder.exists() && actualThemesFolder.isDirectory()) {
                try {
                    Path studioThemesFolder = options.getFrontendDirectory().toPath().resolve("themes");
                    FileUtils.copyDirectory(actualThemesFolder, studioThemesFolder.toFile());
                } catch (IOException e) {
                    log().warn("Cannot find 'themes' folder in {}", projectFolder);
                }
            }
        }
    }
}

