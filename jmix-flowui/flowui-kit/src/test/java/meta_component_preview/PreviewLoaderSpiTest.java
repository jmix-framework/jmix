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

package meta_component_preview;

import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioDropdownButtonPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioFlowuiComponentsPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioGridColumnVisibilityPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioGridPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioHtmlPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioMainViewComponentsPreviewLoader;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewLoaderSpiTest {

    @Test
    void testKitLoadersAreDiscoverableViaServiceLoader() {
        var loader = ServiceLoader.load(StudioPreviewComponentLoader.class);
        var loaded = loader.stream()
                .map(ServiceLoader.Provider::type)
                .toList();

        assertTrue(loaded.contains(StudioHtmlPreviewLoader.class));
        assertTrue(loaded.contains(StudioFlowuiComponentsPreviewLoader.class));
        assertTrue(loaded.contains(StudioGridPreviewLoader.class));
        assertTrue(loaded.contains(StudioDropdownButtonPreviewLoader.class));
        assertTrue(loaded.contains(StudioGridColumnVisibilityPreviewLoader.class));
        assertTrue(loaded.contains(StudioMainViewComponentsPreviewLoader.class));
        assertEquals(6, loaded.size());
    }
}
