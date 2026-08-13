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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioHtmlPreviewLoader;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioHtmlPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioHtmlPreviewLoader loader = new StudioHtmlPreviewLoader();

    BaseElement element(String name) {
        return new BaseElement(name, VIEW_NS);
    }

    StudioPreviewEnvironment environment(String resolvedMessage) {
        return new StudioPreviewEnvironment() {
            @Override
            public String resolveMessage(String messageKey) {
                return resolvedMessage;
            }

            @Override
            public String propertyCaption(String dataContainerId, String metaClass, String propertyPath) {
                return null;
            }
        };
    }

    @Test
    void testSupportsHtmlElementsInViewNamespace() {
        assertTrue(loader.isSupported(element("div")));
        assertTrue(loader.isSupported(element("h1")));
        assertFalse(loader.isSupported(element("button")));
        assertFalse(loader.isSupported(new BaseElement("div", Namespace.get("http://other"))));
    }

    @Test
    void testLoadsComponentWithBaseAttributes() {
        BaseElement element = element("div");
        element.addAttribute("width", "100px");
        element.addAttribute("classNames", "styled");

        Component component = loader.load(element, element("view"));

        assertInstanceOf(Div.class, component);
        assertEquals("100px", ((Div) component).getWidth());
        assertTrue(((Div) component).hasClassName("styled"));
    }

    @Test
    void testLoadsHeadings() {
        assertInstanceOf(H1.class, loader.load(element("h1"), element("view")));
    }

    @Test
    void testLoadsLiteralText() {
        BaseElement element = element("p");
        element.addAttribute("text", "About Jmix");

        Component component = loader.load(element, element("view"));

        assertInstanceOf(Paragraph.class, component);
        assertEquals("About Jmix", ((Paragraph) component).getText());
    }

    @Test
    void testLoadsLocalizedText() {
        BaseElement element = element("h1");
        element.addAttribute("text", "msg://about.title");

        Component component = loader.load(element, element("view"), environment("About application"));

        assertInstanceOf(H1.class, component);
        assertEquals("About application", ((H1) component).getText());
    }

    @Test
    void testLoadsLocalizedSpanText() {
        BaseElement element = element("span");
        element.addAttribute("text", "msg://product.name");

        Component component = loader.load(element, element("view"), environment("B2B CRM"));

        assertInstanceOf(Span.class, component);
        assertEquals("B2B CRM", ((Span) component).getText());
    }

    @Test
    void testHtmlElementUsesInlineContent() {
        BaseElement element = element("html");
        BaseElement content = new BaseElement("content", VIEW_NS);
        content.setText("<b>bold</b>");
        element.add(content);

        Component component = loader.load(element, element("view"));
        assertNotNull(component);
        assertEquals("b", component.getElement().getTag());
    }

    @Test
    void testHtmlElementUsesContentAttribute() {
        BaseElement element = element("html");
        element.addAttribute("content", "<i>italic</i>");

        Component component = loader.load(element, element("view"));
        assertNotNull(component);
        assertEquals("i", component.getElement().getTag());
    }

    @Test
    void testHtmlElementWithoutContentUsesDefault() {
        Component component = loader.load(element("html"), element("view"));
        assertNotNull(component);
        assertEquals("span", component.getElement().getTag());
    }

    @Test
    void testHtmlElementWithNonMarkupContentUsesDefault() {
        BaseElement element = element("html");
        element.addAttribute("content", "plain text");

        Component component = loader.load(element, element("view"));
        assertNotNull(component);
        assertEquals("span", component.getElement().getTag());
    }

    @Test
    void testHtmlElementWithFileAttributeDeclinesSoStudioResolvesTheFile() {
        BaseElement element = element("html");
        element.addAttribute("file", "content/page.html");

        // A spring-free kit loader can't read the project file: decline (null) so Studio's fallback resolves it.
        assertNull(loader.load(element, element("view")));
    }

    @Test
    void testHtmlElementWithMultiRootContentDoesNotThrow() {
        BaseElement element = element("html");
        // Raw `new Html(...)` requires a single root; two siblings would throw.
        assertThrows(IllegalArgumentException.class, () -> new com.vaadin.flow.component.Html("<p>a</p><p>b</p>"));
        element.addAttribute("content", "<p>a</p><p>b</p>");

        Component component = loader.load(element, element("view"));
        assertNotNull(component);
    }

    @Test
    void testHtmlElementWithInlineContentAndFileUsesInlineContent() {
        BaseElement element = element("html");
        element.addAttribute("file", "content/page.html");
        BaseElement content = new BaseElement("content", VIEW_NS);
        content.setText("<b>bold</b>");
        element.add(content);

        Component component = loader.load(element, element("view"));
        assertNotNull(component);
        assertEquals("b", component.getElement().getTag());
    }
}
