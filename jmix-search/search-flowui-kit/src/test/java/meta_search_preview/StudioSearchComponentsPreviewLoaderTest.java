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

package meta_search_preview;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.searchflowui.kit.meta.loader.StudioSearchComponentsPreviewLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioSearchComponentsPreviewLoaderTest {

    static final Namespace SEARCH_NS = Namespace.get("http://jmix.io/schema/search/ui");
    static final Namespace OTHER_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioSearchComponentsPreviewLoader loader = new StudioSearchComponentsPreviewLoader();

    /** Fake env backed by a message map, per the dropdown/charts loader tests' FakeEnv pattern. */
    static class FakeEnv implements StudioPreviewEnvironment {
        final Map<String, String> messages = new HashMap<>();

        @Override
        public String resolveMessage(String messageKey) {
            return messages.get(messageKey);
        }

        @Override
        public String propertyCaption(String dataContainerId, String metaClass, String propertyPath) {
            return null;
        }
    }

    BaseElement element(String name, String... nameValuePairs) {
        BaseElement element = new BaseElement(name, SEARCH_NS);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            element.addAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return element;
    }

    @Test
    void testIsSupportedForSearchFieldElement() {
        assertTrue(loader.isSupported(element("searchField")));
    }

    @Test
    void testIsSupportedForFullTextFilterElement() {
        assertTrue(loader.isSupported(element("fullTextFilter")));
    }

    @Test
    void testIsSupportedFalseForWrongNamespace() {
        assertFalse(loader.isSupported(new BaseElement("searchField", OTHER_NS)));
    }

    @Test
    void testIsSupportedFalseForWrongElementName() {
        assertFalse(loader.isSupported(element("someOtherElement")));
    }

    @Test
    void testLoadSearchFieldReturnsTextField() {
        Element el = element("searchField");
        Component component = loader.load(el, el);

        assertInstanceOf(TextField.class, component);
    }

    @Test
    void testLoadFullTextFilterReturnsHorizontalLayout() {
        Element el = element("fullTextFilter");
        Component component = loader.load(el, el);

        assertInstanceOf(HorizontalLayout.class, component);
    }

    @Test
    void testSearchFieldBaseAttributesApplied() {
        Element el = element("searchField", "width", "100%", "visible", "false", "classNames", "foo bar");
        TextField field = (TextField) loader.load(el, el);

        assertEquals("100%", field.getWidth());
        assertFalse(field.isVisible());
        assertTrue(field.getClassNames().contains("foo"));
        assertTrue(field.getClassNames().contains("bar"));
    }

    @Test
    void testSearchFieldPureXmlAttributesApplied() {
        Element el = element("searchField",
                "value", "abc",
                "autofocus", "true",
                "ariaLabel", "aria label raw");
        TextField field = (TextField) loader.load(el, el);

        assertEquals("abc", field.getValue());
        assertTrue(field.isAutofocus());
        assertEquals("aria label raw", field.getAriaLabel().orElse(null));
    }

    @Test
    void testSearchFieldMessageReferencedTextRawFallback() {
        Element el = element("searchField",
                "placeholder", "msg://unresolved.placeholder",
                "label", "msg://unresolved.label",
                "helperText", "msg://unresolved.helper",
                "title", "msg://unresolved.title");

        // 2-arg load: routes through StudioPreviewEnvironment.NOOP.
        TextField field = (TextField) loader.load(el, el);

        assertEquals("msg://unresolved.placeholder", field.getPlaceholder());
        assertEquals("msg://unresolved.label", field.getLabel());
        assertEquals("msg://unresolved.helper", field.getHelperText());
        assertEquals("msg://unresolved.title", field.getTitle());
    }

    @Test
    void testSearchFieldMessageReferenceResolvedWithEnv() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://search.placeholder", "Search everywhere");

        Element el = element("searchField", "placeholder", "msg://search.placeholder");
        TextField field = (TextField) loader.load(el, el, env);

        assertEquals("Search everywhere", field.getPlaceholder());
    }

    @Test
    void testFullTextFilterBaseAttributesApplied() {
        Element el = element("fullTextFilter", "width", "50%", "visible", "false");
        HorizontalLayout root = (HorizontalLayout) loader.load(el, el);

        assertEquals("50%", root.getWidth());
        assertFalse(root.isVisible());
    }

    @Test
    void testFullTextFilterLabelApproximationRendersSpanAndField() {
        Element el = element("fullTextFilter", "label", "Search");
        HorizontalLayout root = (HorizontalLayout) loader.load(el, el);

        assertEquals(2, root.getComponentCount());
        assertInstanceOf(Span.class, root.getComponentAt(0));
        assertEquals("Search", ((Span) root.getComponentAt(0)).getText());
        assertInstanceOf(TextField.class, root.getComponentAt(1));
    }

    @Test
    void testFullTextFilterWithoutLabelStillRendersField() {
        Element el = element("fullTextFilter");
        HorizontalLayout root = (HorizontalLayout) loader.load(el, el);

        assertEquals(1, root.getComponentCount());
        assertInstanceOf(TextField.class, root.getComponentAt(0));
    }

    @Test
    void testDiscoverableViaServiceLoader() {
        var loaded = ServiceLoader.load(StudioPreviewComponentLoader.class).stream()
                .map(ServiceLoader.Provider::type)
                .toList();

        assertTrue(loaded.contains(StudioSearchComponentsPreviewLoader.class));
    }
}
