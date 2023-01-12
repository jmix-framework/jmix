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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

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
     * @param buildDir          the used build directory
     */
    TaskGeneratePackageJson(File npmFolder, File studioFolder, File generatedPath,
                            String buildDir, FeatureFlags featureFlags) {
        super(null, null, npmFolder, studioFolder, generatedPath, buildDir, featureFlags);
    }

    @Override
    public void execute() {
        try {
            modified = false;
            File studioJsonFile = getStudioJsonFile();
            if (!studioJsonFile.exists()) {
                studioJsonFile.createNewFile();
                FileUtils.copyFile(getProjectJsonFile(), studioJsonFile);
            }
            JsonObject mainContent = getPackageJson(getStudioJsonFile());
            modified = updateDefaultDependencies(mainContent);
            if (modified) {
                writePackageFile(mainContent);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
