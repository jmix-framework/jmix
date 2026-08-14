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

package io.jmix.flowui.kit.meta.component.preview;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StudioStandardComponentsFactoryPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    /** Fake env; only needs to be non-NOOP to exercise the placeholder-fill gate. */
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

    final StudioStandardComponentsPreviewLoader loader = new StudioStandardComponentsPreviewLoader();

    BaseElement element(String name) {
        return new BaseElement(name, VIEW_NS);
    }

    @Test
    void testDoesNotClaimHtmlElements() {
        assertFalse(loader.isSupported(element("div")));
    }

    /**
     * {@code content} is an element rather than a component, but the fragment and card meanings of it
     * hold real children, so it must preview as a visible container - an invisible stand-in would
     * swallow them. {@code Div} is what Studio's own meta declares for the tag.
     */
    @Test
    void testContentElementPreviewsAsAVisibleDiv() {
        BaseElement element = element("content");

        // Claiming it matters as much as building it: without isSupported the provider never asks.
        assertTrue(loader.isSupported(element));

        Component component = loader.load(element, element("view"));

        assertInstanceOf(Div.class, component);
        assertTrue(component.isVisible());
    }

    /**
     * The generic {@code <component class=…>} names an application class the kit cannot instantiate,
     * so it previews as a labelled placeholder instead of being declined (which, with Studio's
     * reflection fallback disabled, left an empty hole in the preview).
     */
    @Test
    void testGenericComponentPreviewsAsALabelledPlaceholder() {
        BaseElement element = element("component");

        assertTrue(loader.isSupported(element));

        Component component = loader.load(element, element("view"));

        assertInstanceOf(Image.class, component);
        assertEquals("COMPONENT", ((Image) component).getAlt().orElse(null));
    }

    @Test
    void testEveryFactoryElementInstantiates() {
        for (String name : StudioStandardComponentsPreviewLoader.supportedElements()) {
            BaseElement element = element(name);
            assertTrue(loader.isSupported(element), name);
            assertNotNull(loader.load(element, element("view")), name);
        }
    }

    @Test
    void testButtonWithAttributes() {
        BaseElement element = element("button");
        element.addAttribute("id", "saveBtn");
        element.addAttribute("text", "msg://save");
        element.addAttribute("icon", "CHECK");
        element.addAttribute("width", "12em");
        element.addAttribute("enabled", "false");
        element.addAttribute("alignSelf", "CENTER");
        element.addAttribute("css", "color: red");

        FakeEnv environment = new FakeEnv();
        environment.messages.put("msg://save", "Save");

        Component component = loader.load(element, element("view"), environment);

        assertInstanceOf(JmixButton.class, component);
        JmixButton button = (JmixButton) component;
        assertEquals("saveBtn", button.getId().orElse(null));
        assertEquals("Save", button.getText());
        assertNotNull(button.getIcon());
        assertEquals("12em", button.getWidth());
        assertFalse(button.isEnabled());
        assertEquals("center", button.getStyle().get("align-self"));
        assertEquals("red", button.getStyle().get("color"));
    }

    @Test
    void testTextFieldAttributesAreLoadedExplicitly() {
        BaseElement element = element("textField");
        element.addAttribute("label", "Name");
        element.addAttribute("placeholder", "Enter a name");
        element.addAttribute("required", "true");
        element.addAttribute("clearButtonVisible", "true");
        element.addAttribute("maxLength", "40");

        TextField field = (TextField) loader.load(element, element("view"), new FakeEnv());

        assertEquals("Name", field.getLabel());
        assertEquals("Enter a name", field.getPlaceholder());
        assertTrue(field.isRequired());
        assertTrue(field.isClearButtonVisible());
        assertEquals(40, field.getMaxLength());
    }

    @Test
    void testTimePickerAndScrollerTypedAttributes() {
        BaseElement timePickerElement = element("timePicker");
        timePickerElement.addAttribute("step", "5m");
        TimePicker timePicker = (TimePicker) loader.load(timePickerElement, element("view"), new FakeEnv());
        assertEquals(Duration.ofMinutes(5), timePicker.getStep());

        BaseElement scrollerElement = element("scroller");
        scrollerElement.addAttribute("scrollBarsDirection", "BOTH");
        Scroller scroller = (Scroller) loader.load(scrollerElement, element("view"), new FakeEnv());
        assertEquals(Scroller.ScrollDirection.BOTH, scroller.getScrollDirection());
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

    /**
     * The source attribute is {@code resource} in layout.xsd (the runtime ImageLoader maps it onto
     * setSrc); there is no {@code src} attribute, so reading one would never match a valid view.
     */
    @Test
    void testImageReadsResourceAttributeForAbsoluteUrls() {
        BaseElement element = element("image");
        element.addAttribute("resource", "https://example.com/logo.png");

        Component component = loader.load(element, element("view"));

        assertInstanceOf(Image.class, component);
        assertEquals("https://example.com/logo.png", ((Image) component).getSrc());
    }

    /** A theme/classpath resource can't be resolved from the preview classloader, so it is skipped. */
    @Test
    void testImageIgnoresNonAbsoluteResource() {
        BaseElement element = element("image");
        element.addAttribute("resource", "icons/logo.png");

        Component component = loader.load(element, element("view"));

        assertInstanceOf(Image.class, component);
        assertTrue(((Image) component).getSrc() == null || ((Image) component).getSrc().isEmpty());
    }

    @Test
    void testSvgIconReadsSymbolAndResourceAttributes() {
        BaseElement element = element("svgIcon");
        element.addAttribute("symbol", "some-symbol");
        element.addAttribute("resource", "icons/icon.svg");

        Component component = loader.load(element, element("view"));

        assertInstanceOf(SvgIcon.class, component);
        assertEquals("some-symbol", ((SvgIcon) component).getSymbol());
        assertEquals("icons/icon.svg", ((SvgIcon) component).getSrc());
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

    @Test
    void testHasListDataViewComponentGetsThreePlaceholderItemsWithRealEnvironment() {
        ComboBox<?> comboBox = (ComboBox<?>) loader.load(element("comboBox"), element("view"), new FakeEnv());
        assertEquals(3, comboBox.getListDataView().getItemCount());

        ListBox<?> listBox = (ListBox<?>) loader.load(element("listBox"), element("view"), new FakeEnv());
        assertEquals(3, listBox.getListDataView().getItemCount());
    }

    @Test
    void testHasListDataViewComponentGetsNoPlaceholderItemsWithNoopEnvironment() {
        ComboBox<?> comboBox = (ComboBox<?>) loader.load(element("comboBox"), element("view"),
                StudioPreviewEnvironment.NOOP);
        assertEquals(0, comboBox.getListDataView().getItemCount());
    }

    @Test
    void testHorizontalMenuGetsFivePlaceholderItemsWithRealEnvironment() {
        JmixMenuBar menuBar = (JmixMenuBar) loader.load(element("horizontalMenu"), element("view"), new FakeEnv());
        assertEquals(5, menuBar.getItems().size());
    }

    @Test
    void testHorizontalMenuGetsNoPlaceholderItemsWithNoopEnvironment() {
        JmixMenuBar menuBar = (JmixMenuBar) loader.load(element("horizontalMenu"), element("view"),
                StudioPreviewEnvironment.NOOP);
        assertEquals(0, menuBar.getItems().size());
    }

    @Test
    void testPlainComponentUnaffectedByPlaceholderFill() {
        Component component = loader.load(element("textField"), element("view"), new FakeEnv());
        assertInstanceOf(TextField.class, component);
    }

    @Test
    void testMarkdownReadsContentAttribute() {
        BaseElement element = element("markdown");
        element.addAttribute("content", "# Title");

        Component component = loader.load(element, element("view"));
        assertInstanceOf(Markdown.class, component);
        assertEquals("# Title", ((Markdown) component).getContent());
    }
}
