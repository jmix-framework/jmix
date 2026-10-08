/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview;

/**
 * Test-only bridge to the package-private last-resort loader. Loader-package tests assert which of
 * the two loaders claims a tag - a tag claimed by both would make the outcome depend on loader
 * order - and cannot name the class from their own package.
 */
public final class StandardComponentsLoaderAccess {

    private StandardComponentsLoaderAccess() {
    }

    public static StudioPreviewComponentLoader loader() {
        return new StudioStandardComponentsPreviewLoader();
    }
}
