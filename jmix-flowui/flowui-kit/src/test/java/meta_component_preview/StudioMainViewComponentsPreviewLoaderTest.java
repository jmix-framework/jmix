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
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioMainViewComponentsPreviewLoader;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioMainViewComponentsPreviewLoaderTest {

    static final Namespace MAIN_VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/main-view");
    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioMainViewComponentsPreviewLoader loader = new StudioMainViewComponentsPreviewLoader();

    BaseElement element(String name, Namespace ns) {
        return new BaseElement(name, ns);
    }

    @Test
    void testSupportsMainViewStructuralElements() {
        assertTrue(loader.isSupported(element("appLayout", MAIN_VIEW_NS)));
        assertTrue(loader.isSupported(element("initialLayout", MAIN_VIEW_NS)));
        assertTrue(loader.isSupported(element("navigationBar", MAIN_VIEW_NS)));
        assertTrue(loader.isSupported(element("drawerLayout", MAIN_VIEW_NS)));
        assertTrue(loader.isSupported(element("layout", VIEW_NS)));
    }

    @Test
    void testDoesNotSupportRootsOrOtherNamespaces() {
        assertFalse(loader.isSupported(element("mainView", MAIN_VIEW_NS)));
        assertFalse(loader.isSupported(element("view", VIEW_NS)));
        assertFalse(loader.isSupported(element("appLayout", Namespace.get("http://other"))));
    }

    @Test
    void testLoadsAppLayout() {
        Component component = loader.load(element("appLayout", MAIN_VIEW_NS), element("mainView", MAIN_VIEW_NS));
        assertInstanceOf(AppLayout.class, component);
    }

    @Test
    void testLoadsInitialLayoutAndLayoutAsVerticalLayout() {
        assertInstanceOf(VerticalLayout.class,
                loader.load(element("initialLayout", MAIN_VIEW_NS), element("mainView", MAIN_VIEW_NS)));
        assertInstanceOf(VerticalLayout.class,
                loader.load(element("layout", VIEW_NS), element("view", VIEW_NS)));
    }

    @Test
    void testLoadsNavigationBarAndDrawerLayoutAsDiv() {
        assertInstanceOf(Div.class,
                loader.load(element("navigationBar", MAIN_VIEW_NS), element("mainView", MAIN_VIEW_NS)));
        assertInstanceOf(Div.class,
                loader.load(element("drawerLayout", MAIN_VIEW_NS), element("mainView", MAIN_VIEW_NS)));
    }

    @Test
    void testLoadsComponentWithBaseAttributes() {
        BaseElement element = element("layout", VIEW_NS);
        element.addAttribute("width", "100%");
        element.addAttribute("classNames", "styled");

        Component component = loader.load(element, element("view", VIEW_NS));

        assertInstanceOf(VerticalLayout.class, component);
        assertEquals("100%", ((VerticalLayout) component).getWidth());
        assertTrue(((VerticalLayout) component).hasClassName("styled"));
    }

    @Test
    void testOwnedAspectsIsEmpty() {
        assertTrue(loader.ownedAspects(element("appLayout", MAIN_VIEW_NS)).isEmpty());
    }
}
