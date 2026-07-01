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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.flow.server.Constants;
import io.jmix.flowui.devserver.theme.LegacyThemeStyleSheets;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.PACKAGE_LOCK_JSON;

@NullMarked
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
        copyProjectLegacyThemes(context);
    }

    private static void copyProjectFrontend(StartupContext context) {
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

    private static void copyProjectMetaInfResources(StartupContext context) {
        File projectMetaInf = context.getProjectMetaInfResourcesFolder();
        File designerMetaInf = context.getDesignerMetaInfResourcesFolder();

        if (!projectMetaInf.exists() || !projectMetaInf.isDirectory()) {
            log.info("Project META-INF folder {} does not exist, skipping mirror", projectMetaInf);
            return;
        }

        logFileCopying("project META-INF/resources/frontend folder");

        try {
            FileUtils.copyDirectory(projectMetaInf, designerMetaInf);
            log.info("Project META-INFO folder has been copied successfully from {} to {}", projectMetaInf, designerMetaInf);
        } catch (IOException e) {
            log.warn("Cannot copy project META-INF folder from {} to {}", projectMetaInf, designerMetaInf, e);
        }
    }

    private static void copyProjectLegacyThemes(StartupContext context) {
        File projectLegacyThemes = context.getProjectLegacyThemesFolder();
        File designerMetaInf = context.getDesignerMetaInfResourcesFolder();

        if (!projectLegacyThemes.exists() || !projectLegacyThemes.isDirectory()) {
            log.info("Project legacy themes folder {} does not exist, skipping mirror", projectLegacyThemes);
            LegacyThemeStyleSheets.setStyleSheets(List.of());
            return;
        }

        logFileCopying("project legacy themes folder");

        try {
            FileUtils.copyDirectoryToDirectory(projectLegacyThemes, designerMetaInf);
            log.info("Project legacy themes folder has been copied successfully from {} to {}",
                    projectLegacyThemes, designerMetaInf);

            File copiedThemesRoot = new File(designerMetaInf, Constants.APPLICATION_THEME_ROOT);
            LegacyThemeStyleSheets.setStyleSheets(
                    collectLegacyThemeStyleSheets(copiedThemesRoot, designerMetaInf));
        } catch (IOException e) {
            log.warn("Cannot copy project legacy themes folder from {} to {}",
                    projectLegacyThemes, designerMetaInf, e);
            LegacyThemeStyleSheets.setStyleSheets(List.of());
        }
    }

    /**
     * Support for projects with {@code @Theme} annotation:
     * we will copy all theme files to the designer META-INF directory
     * and add theme {@code style.css} file to UI via {@code Page#addStyleSheet} in {@code MainLayout}
     * @see io.jmix.flowui.devserver.MainLayout
     */
    private static List<String> collectLegacyThemeStyleSheets(File copiedThemesRoot, File designerMetaInf) throws IOException {
        if (!copiedThemesRoot.exists() || !copiedThemesRoot.isDirectory()) {
            return List.of();
        }

        Path metaInfRoot = designerMetaInf.toPath();
        List<String> result = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(copiedThemesRoot.toPath())) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> "styles.css".equalsIgnoreCase(path.getFileName().toString()))
                    .forEach(path -> {
                        String relative = metaInfRoot.relativize(path).toString()
                                .replace(File.separatorChar, '/');
                        result.add(relative);
                    });
        }

        return result;
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
