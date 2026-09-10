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
package io.jmix.flowui.xml.layout;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StudioPreviewCoverageTest {

    static final String VIEW_NS = "http://jmix.io/schema/flowui/view";

    // Keep this test beside the runtime registry so a newly registered component cannot silently
    // rely on Studio's reflection fallback. No Spring context or application data is needed.
    @TestFactory
    Stream<DynamicTest> everyRuntimeComponentHasFrameworkPreview() throws Exception {
        var perform = Class.forName("io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentProvider")
                .getDeclaredMethod("perform", String.class, Map.class);
        perform.setAccessible(true);
        StudioPreviewEnvironment environment = new StudioPreviewEnvironment() {
            @Override
            public String resolveMessage(String key) {
                return null;
            }

            @Override
            public String propertyCaption(String container, String metaClass, String property) {
                return null;
            }
        };
        return new BaseLoaderConfig() {}.loaders.keySet().stream().sorted()
                .map(tag -> DynamicTest.dynamicTest(tag, () -> {
                    assertEquals(true, perform.invoke(null, "canCreateComponent",
                            Map.of("tag", tag, "namespace", VIEW_NS)));
                    String xml = "<view xmlns='" + VIEW_NS + "'><layout><" + tag
                            + " id='probe'/></layout></view>";
                    Component component = assertInstanceOf(Component.class,
                            perform.invoke(null, "createComponent", Map.of(
                                    "viewXml", xml, "componentPath", "//*[@id='probe']",
                                    "environment", environment)));
                    assertEquals("probe", component.getId().orElse(null));
                }));
    }
}
