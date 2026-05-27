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
