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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.jmix.flowui.devserver.frontend.FrontendUtils.FEATURE_FLAGS_FILE_NAME;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.GENERATED;

/**
 * A task for generating the feature flags file
 * {@link FrontendUtils#FEATURE_FLAGS_FILE_NAME} during `package` Maven goal.
 */
public class TaskGenerateFeatureFlags extends AbstractTaskClientGenerator {

    private final File frontendGeneratedDirectory;
    private final FeatureFlags featureFlags;

    TaskGenerateFeatureFlags(File frontendDirectory,
                             FeatureFlags featureFlags) {
        this.frontendGeneratedDirectory = new File(frontendDirectory,
                GENERATED);
        this.featureFlags = featureFlags;
    }

    @Override
    protected String getFileContent() {
        List<String> lines = new ArrayList<>();
        lines.add("// @ts-nocheck");
        lines.add("window.Vaadin = window.Vaadin || {};");
        lines.add(
                "window.Vaadin.featureFlags = window.Vaadin.featureFlags || {};");

        featureFlags.getFeatures().forEach(feature -> {
            lines.add(String.format("window.Vaadin.featureFlags.%s = %s;",
                    feature.getId(), featureFlags.isEnabled(feature)));
        });

        // See https://github.com/vaadin/flow/issues/14184
        lines.add("export {};");
        return String.join(System.lineSeparator(), lines);
    }

    @Override
    protected File getGeneratedFile() {
        return new File(frontendGeneratedDirectory, FEATURE_FLAGS_FILE_NAME);
    }

    @Override
    protected boolean shouldGenerate() {
        return true;
    }
}

