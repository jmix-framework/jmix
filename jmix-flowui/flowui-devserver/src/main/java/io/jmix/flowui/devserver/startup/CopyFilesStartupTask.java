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

import io.jmix.flowui.devserver.AppShell;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.PACKAGE_LOCK_JSON;

public class CopyFilesStartupTask implements StartupTask {

    private static final Logger log = LoggerFactory.getLogger(CopyFilesStartupTask.class);

    @Override
    public void execute(StartupContext context) {
        copyThemes(context);
        copyNpmrcFile(context);
        copyPackageLockFile(context);
    }

    public static void copyThemes(StartupContext context) {
        logFileCopying("project themes");

        File projectThemeFolder = context.getProjectThemeFolder();
        File designerThemeFolder = context.getDesignerThemeFolder();

        if (projectThemeFolder.exists() && projectThemeFolder.isDirectory()) {
            try {
                FileUtils.copyDirectory(projectThemeFolder, designerThemeFolder);
                log.info("Themes folder has been copied successfully from {} to {}",
                        projectThemeFolder, designerThemeFolder);

                String themeName = context.themeName();
                if (StringUtils.isNotBlank(themeName)) {
                    File themeDir = new File(designerThemeFolder, themeName);
                    if (themeDir.exists() && themeDir.isDirectory()) {
                        File newThemeDir = new File(designerThemeFolder, AppShell.PREVIEW_THEME_NAME);
                        FileUtils.copyDirectory(themeDir, newThemeDir);
                        FileUtils.deleteDirectory(themeDir);
                        log.info("Theme folder '{}' has been successfully copied to '{}'", themeName, newThemeDir);
                    }
                }
            } catch (IOException e) {
                log.warn("Cannot copy project themes from {} to {}", projectThemeFolder, designerThemeFolder);
            }
        }
    }

    private void copyNpmrcFile(StartupContext context) {
        String npmrcFileName = ".npmrc";
        logFileCopying(npmrcFileName);

        File npmrcFile = context.findFileInProjectFolder(npmrcFileName);
        if (npmrcFile.exists()) {
            try {
                FileUtils.copyFile(npmrcFile, context.findFileInDesignerFolder(npmrcFileName));
                logFileCopiedSuccessfully(npmrcFileName);
            } catch (IOException e) {
                log.warn("Exception when copying {} file", npmrcFileName);
            }
        }
    }

    private void copyPackageLockFile(StartupContext context) {
        logFileCopying(PACKAGE_LOCK_JSON);

        File packageLockFile = context.findFileInProjectFolder(PACKAGE_LOCK_JSON);
        if (packageLockFile.exists()) {
            File studioPackageLockFile = context.findFileInDesignerFolder(PACKAGE_LOCK_JSON);
            try {
                FileUtils.copyFile(packageLockFile, studioPackageLockFile);
                logFileCopiedSuccessfully(PACKAGE_LOCK_JSON);
            } catch (IOException e) {
                log.warn("Exception when copying " + PACKAGE_LOCK_JSON + " file", e);
            }
        }
    }

    private static void logFileCopying(String fileName) {
        log.info("Copying {}...", fileName);
    }

    private static void logFileCopiedSuccessfully(String fileName) {
        log.info("{} has been copied successfully", fileName);
    }
}
