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

package io.jmix.flowui.kit.meta.component.preview.loader;

import java.util.List;

import io.jmix.flowui.kit.meta.StudioAPI;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;

/**
 * Internal registry of this module's preview loaders. For internal use only — not part of the
 * public API and not an extension point.
 * <p>
 * The kit's own loaders are package-private and registered here instead of
 * {@code META-INF/services}: the classpath {@link java.util.ServiceLoader} cannot instantiate
 * non-public classes, and keeping them non-public keeps them out of application developers' way.
 * Loaders shipped by <b>other</b> modules (add-on kits) still register via
 * {@code META-INF/services} and therefore must stay public.
 */
@StudioAPI
public final class StudioPreviewKitLoaders {

    private StudioPreviewKitLoaders() {
    }

    public static List<StudioPreviewComponentLoader> loaders() {
        return List.of(
                new StudioHtmlPreviewLoader(),
                new StudioGridPreviewLoader(),
                new StudioDropdownButtonPreviewLoader(),
                new StudioGridColumnVisibilityPreviewLoader(),
                new StudioMainViewComponentsPreviewLoader(),
                new StudioNonVisualElementsPreviewLoader());
    }
}
