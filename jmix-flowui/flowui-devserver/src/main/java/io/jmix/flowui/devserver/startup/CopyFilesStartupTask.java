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
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import io.jmix.flowui.devserver.AppShell;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.PACKAGE_LOCK_JSON;

public class CopyFilesStartupTask implements StartupTask {

    private static final Logger log = LoggerFactory.getLogger(CopyFilesStartupTask.class);

    private static final Set<String> FRONTEND_COPY_EXCLUDES = Set.of(
            "node_modules", "generated", "dist", "build", "target"
    );

    private static final FileFilter FRONTEND_COPY_FILTER =
            file -> !FRONTEND_COPY_EXCLUDES.contains(file.getName());

    @Override
    public void execute(StartupContext context) {
        copyProjectResources(context);
        copyNpmrcFile(context);
        copyPackageLockFile(context);
    }

    public static void copyProjectResources(StartupContext context) {
        copyProjectFrontend(context);
        copyProjectMetaInfResources(context);
        createPreviewThemeBridge(context);
    }

    public static void copyProjectFrontend(StartupContext context) {
        File projectFrontend = context.getProjectFrontendFolder();
        File designerFrontend = context.getDesignerFrontendFolder();

        if (!projectFrontend.exists() || !projectFrontend.isDirectory()) {
            log.info("Project frontend folder {} does not exist, skipping mirror", projectFrontend);
            return;
        }

        logFileCopying("project frontend folder");
        try {
            FileUtils.copyDirectory(projectFrontend, designerFrontend, FRONTEND_COPY_FILTER);
            log.info("Project frontend folder has been copied successfully from {} to {}",
                    projectFrontend, designerFrontend);
        } catch (IOException e) {
            log.warn("Cannot copy project frontend folder from {} to {}", projectFrontend, designerFrontend, e);
        }
    }

    public static void copyProjectMetaInfResources(StartupContext context) {
        File projectClasspathFrontend = context.getProjectMetaInfResourcesFolder();
        File designerClasspathFrontend = context.getDesignerFrontendFolder();

        if (!projectClasspathFrontend.exists() || !projectClasspathFrontend.isDirectory()) {
            log.info("Project classpath frontend folder {} does not exist, skipping mirror",
                    projectClasspathFrontend);
            return;
        }

        logFileCopying("project META-INF/resources/frontend folder");
        try {
            FileUtils.copyDirectory(projectClasspathFrontend, designerClasspathFrontend);
            log.info("Project classpath frontend folder has been copied successfully from {} to {}",
                    projectClasspathFrontend, designerClasspathFrontend);
        } catch (IOException e) {
            log.warn("Cannot copy project classpath frontend folder from {} to {}",
                    projectClasspathFrontend, designerClasspathFrontend, e);
        }
    }

    public static void createPreviewThemeBridge(StartupContext context) {
        createLegacyThemeFolder(context);
    }

    private static void createLegacyThemeFolder(StartupContext context) {
        File designerThemesFolder = context.getDesignerLegacyThemesFolder();
        File projectLegacyThemesFolder = context.getProjectLegacyThemesFolder();
        File previewThemeFolder = new File(designerThemesFolder, AppShell.PREVIEW_THEME_NAME);

        try {
            log.info("Creating empty preview theme folder {}...", previewThemeFolder);
            FileUtils.forceMkdir(previewThemeFolder);

            if (projectLegacyThemesFolder.exists() && projectLegacyThemesFolder.isDirectory()) {
                logFileCopying("project themes folder '%s'".formatted(projectLegacyThemesFolder.getName()));
                FileUtils.copyDirectory(projectLegacyThemesFolder, designerThemesFolder);

                String themeName = context.themeName();
                log.info("Project theme name is {}", themeName);
                if (StringUtils.isNotBlank(themeName)) {
                    File themeDir = new File(designerThemesFolder, themeName);
                    if (themeDir.exists() && themeDir.isDirectory()) {
                        FileUtils.copyDirectory(themeDir, previewThemeFolder);
                        FileUtils.deleteDirectory(themeDir);
                        log.info("Project theme folder '{}' has been successfully copied to '{}'", themeName, previewThemeFolder);
                    }
                }
            }

            File stylesCss = new File(previewThemeFolder, "styles.css");
            if (!stylesCss.exists()) {
                log.info("Creating empty styles.css file...");
                FileUtils.write(stylesCss, "", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Can not create preview theme folder {}", previewThemeFolder);
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
