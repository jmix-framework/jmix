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

package xml_layout_support;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseComponentLoaderSupportTest {

    @Test
    void testLoadSizeAttributes() {
        BaseElement element = new BaseElement("button");
        element.addAttribute("width", "10em");
        element.addAttribute("maxHeight", "5em");

        Button button = new Button();
        BaseComponentLoaderSupport.loadSizeAttributes(button, element);

        assertEquals("10em", button.getWidth());
        assertEquals("5em", button.getMaxHeight());
        assertNull(button.getMinWidth());
    }

    @Test
    void testLoadEnabledAndClassAndThemeNames() {
        BaseElement element = new BaseElement("button");
        element.addAttribute("enabled", "false");
        element.addAttribute("classNames", "cls-a, cls-b");
        element.addAttribute("themeNames", "primary small");

        Button button = new Button();
        BaseComponentLoaderSupport.loadEnabled(button, element);
        BaseComponentLoaderSupport.loadClassNames(button, element);
        BaseComponentLoaderSupport.loadThemeNames(button, element);

        assertFalse(button.isEnabled());
        assertTrue(button.hasClassName("cls-a"));
        assertTrue(button.hasClassName("cls-b"));
        assertTrue(button.getThemeNames().containsAll(java.util.List.of("primary", "small")));
    }

    @Test
    void testLoadThemableAttributes() {
        BaseElement element = new BaseElement("vbox");
        element.addAttribute("margin", "true");
        element.addAttribute("padding", "false");

        VerticalLayout layout = new VerticalLayout();
        BaseComponentLoaderSupport.loadThemableAttributes(layout, element);

        assertTrue(layout.isMargin());
        assertFalse(layout.isPadding());
    }

    @Test
    void testLoadIconSetIcon() {
        BaseElement element = new BaseElement("button");
        element.addAttribute("icon", "CHECK");

        java.util.Optional<Icon> icon = BaseComponentLoaderSupport.loadIconSetIcon(element);
        assertTrue(icon.isPresent());
    }
}
