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

package meta_dynattr_preview;

import java.util.ServiceLoader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.dynattrflowui.kit.meta.loader.StudioDynamicAttributesPanelPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioDynamicAttributesPanelPreviewLoaderTest {

    static final Namespace DYNATTR_NS = Namespace.get("http://jmix.io/schema/dynattr/flowui");
    static final Namespace OTHER_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioDynamicAttributesPanelPreviewLoader loader = new StudioDynamicAttributesPanelPreviewLoader();

    BaseElement element(String name, String... nameValuePairs) {
        BaseElement element = new BaseElement(name, DYNATTR_NS);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            element.addAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return element;
    }

    @Test
    void testIsSupportedForDynamicAttributesPanelElement() {
        assertTrue(loader.isSupported(element("dynamicAttributesPanel")));
    }

    @Test
    void testIsSupportedFalseForWrongNamespace() {
        assertFalse(loader.isSupported(new BaseElement("dynamicAttributesPanel", OTHER_NS)));
    }

    @Test
    void testIsSupportedFalseForWrongElementName() {
        assertFalse(loader.isSupported(element("someOtherElement")));
    }

    @Test
    void testLoadReturnsFormLayout() {
        Element el = element("dynamicAttributesPanel");
        Component component = loader.load(el, el);

        assertInstanceOf(FormLayout.class, component);
    }

    @Test
    void testBaseAttributesApplied() {
        Element el = element("dynamicAttributesPanel",
                "width", "100%", "visible", "false", "classNames", "foo bar");
        FormLayout panel = (FormLayout) loader.load(el, el);

        assertEquals("100%", panel.getWidth());
        assertFalse(panel.isVisible());
        assertTrue(panel.getClassNames().contains("foo"));
        assertTrue(panel.getClassNames().contains("bar"));
    }

    @Test
    void testDiscoverableViaServiceLoader() {
        var loaded = ServiceLoader.load(StudioPreviewComponentLoader.class).stream()
                .map(ServiceLoader.Provider::type)
                .toList();

        assertTrue(loaded.contains(StudioDynamicAttributesPanelPreviewLoader.class));
    }
}
