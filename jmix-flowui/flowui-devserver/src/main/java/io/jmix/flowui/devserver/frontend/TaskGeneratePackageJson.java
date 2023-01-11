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

import com.vaadin.experimental.FeatureFlags;
import elemental.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static io.jmix.flowui.devserver.frontend.FrontendUtils.FLOW_NPM_PACKAGE_NAME;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.NODE_MODULES;

/**
 * Creates the <code>package.json</code> if missing.
 */
public class TaskGeneratePackageJson extends NodeUpdater {

    /**
     * Create an instance of the updater given all configurable parameters.
     *
     * @param npmFolder         folder with the `package.json` file.
     * @param studioFolder      folder with generated `package.json` file.
     * @param generatedPath     folder where flow generated files will be placed.
     * @param flowResourcesPath folder where flow resources taken from jars will be placed.
     *                          default)
     * @param buildDir          the used build directory
     */
    TaskGeneratePackageJson(File npmFolder, File studioFolder, File generatedPath,
                            File flowResourcesPath, String buildDir,
                            FeatureFlags featureFlags) {
        super(null, null, npmFolder, studioFolder, generatedPath,
                flowResourcesPath, buildDir, featureFlags);
    }

    @Override
    public void execute() {
        try {
            modified = false;
            JsonObject mainContent = getPackageJson(getPackageJsonFile());
            modified = updateDefaultDependencies(mainContent);
            writePackageFile(mainContent);

            if (flowResourcesFolder == null) {
                return;
            }

            if (!new File(studioFolder, NODE_MODULES + FLOW_NPM_PACKAGE_NAME)
                    .equals(flowResourcesFolder)) {
                writeResourcesPackageFile(getResourcesPackageJson());
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
