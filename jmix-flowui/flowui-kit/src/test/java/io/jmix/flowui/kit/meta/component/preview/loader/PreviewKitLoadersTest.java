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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The kit's own loaders are package-private, so {@link java.util.ServiceLoader} cannot instantiate
 * them and they are no longer registered in {@code META-INF/services}: {@link StudioPreviewKitLoaders}
 * is the single place that lists them. Regression net so none gets forgotten there.
 */
class PreviewKitLoadersTest {

    @Test
    void testEveryKitLoaderIsRegistered() {
        List<Class<?>> registered = StudioPreviewKitLoaders.loaders().stream()
                .<Class<?>>map(Object::getClass)
                .toList();

        // StudioStandardComponentsPreviewLoader is not listed here: the provider instantiates it
        // directly as the last-resort loader.
        assertEquals(List.of(
                StudioHtmlPreviewLoader.class,
                StudioGridPreviewLoader.class,
                StudioDropdownButtonPreviewLoader.class,
                StudioGridColumnVisibilityPreviewLoader.class,
                StudioMainViewComponentsPreviewLoader.class,
                StudioNonVisualElementsPreviewLoader.class), registered);
    }
}
