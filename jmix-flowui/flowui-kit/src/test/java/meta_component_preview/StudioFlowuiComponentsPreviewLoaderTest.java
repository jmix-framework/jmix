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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioFlowuiComponentsPreviewLoader;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioFlowuiComponentsPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioFlowuiComponentsPreviewLoader loader = new StudioFlowuiComponentsPreviewLoader();

    BaseElement element(String name) {
        return new BaseElement(name, VIEW_NS);
    }

    @Test
    void testDoesNotClaimStandardOrHtmlElements() {
        assertFalse(loader.isSupported(element("fragment")));
        assertFalse(loader.isSupported(element("userMenu")));
        assertFalse(loader.isSupported(element("div")));
    }

    @Test
    void testEveryFactoryElementInstantiates() {
        for (String name : StudioFlowuiComponentsPreviewLoader.supportedElements()) {
            BaseElement element = element(name);
            assertTrue(loader.isSupported(element), name);
            assertNotNull(loader.load(element, element("view")), name);
        }
    }

    @Test
    void testButtonWithAttributes() {
        BaseElement element = element("button");
        element.addAttribute("width", "12em");
        element.addAttribute("enabled", "false");

        Component component = loader.load(element, element("view"));

        assertInstanceOf(JmixButton.class, component);
        assertEquals("12em", ((JmixButton) component).getWidth());
        assertFalse(((JmixButton) component).isEnabled());
    }

    @Test
    void testLayoutFlexAttributes() {
        BaseElement element = element("hbox");
        element.addAttribute("alignItems", "CENTER");

        Component component = loader.load(element, element("view"));
        assertInstanceOf(HorizontalLayout.class, component);
    }

    @Test
    void testFilterPlaceholders() {
        assertInstanceOf(TextField.class, loader.load(element("propertyFilter"), element("view")));
    }

    @Test
    void testIconUsesIconAttribute() {
        BaseElement element = element("icon");
        element.addAttribute("icon", "CHECK");
        assertInstanceOf(Icon.class, loader.load(element, element("view")));
    }

    @Test
    void testNoLongerSupportsDropdownButtonOrComboButton() {
        assertFalse(loader.isSupported(element("dropdownButton")));
        assertFalse(loader.isSupported(element("comboButton")));
    }

    @Test
    void testNoLongerSupportsGridColumnVisibility() {
        assertFalse(loader.isSupported(element("gridColumnVisibility")));
    }
}
