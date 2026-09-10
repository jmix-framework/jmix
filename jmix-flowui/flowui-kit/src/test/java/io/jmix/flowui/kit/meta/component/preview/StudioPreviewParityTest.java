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
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.gridlayout.JmixGridLayout;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioActionComponentProcessor;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioLayoutComponentProcessor;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StudioPreviewParityTest {

    final StudioPreviewEnvironment environment = new StudioPreviewEnvironment() {
        @Override
        public String resolveMessage(String key) {
            return "msg://custom".equals(key) ? "Custom" : null;
        }

        @Override
        public String propertyCaption(String container, String metaClass, String property) {
            return null;
        }
    };

    Element view(String content) {
        return StudioPreviewComponentProvider.parseXmlRoot(
                "<view xmlns='http://jmix.io/schema/flowui/view'>" + content + "</view>");
    }

    Component load(Element element, Element view) {
        return StudioPreviewComponentProvider.loadSingleComponent(element, view, environment);
    }

    @Test
    void badge_hasFrameworkLoaderAndRuntimeAttributes() {
        Element view = view("<badge id='badge' text='msg://custom' number='7' role='status' "
                + "icon='CHECK' themeNames='success pill' whiteSpace='PRE'/>");
        Badge badge = assertInstanceOf(Badge.class, load(view.element("badge"), view));
        assertEquals("badge", badge.getId().orElseThrow());
        assertEquals("Custom", badge.getText());
        assertEquals(7, badge.getNumber());
        assertEquals("status", badge.getRole());
        assertTrue(badge.getThemeNames().containsAll(List.of("success", "pill")));
        assertEquals("pre", badge.getStyle().get("white-space"));
        assertEquals("vaadin:check", badge.getIcon().getElement().getAttribute("icon"));
    }

    @Test
    void viewRoot_isNeutralAndLayoutUsesRuntimeDefaultsAndAttributes() {
        Element view = view("<layout/>");
        VerticalLayout root = assertInstanceOf(VerticalLayout.class, load(view, view));
        assertFalse(root.isPadding());
        assertFalse(root.isSpacing());
        VerticalLayout layout = assertInstanceOf(VerticalLayout.class, load(view.element("layout"), view));
        assertTrue(layout.isPadding());
        assertTrue(layout.isSpacing());

        view = view("<layout padding='false' spacing='false' margin='true' width='30em' "
                + "classNames='custom' alignItems='END' css='color: red'/>");
        layout = assertInstanceOf(VerticalLayout.class, load(view.element("layout"), view));
        assertFalse(layout.isPadding());
        assertFalse(layout.isSpacing());
        assertTrue(layout.isMargin());
        assertEquals("30em", layout.getWidth());
        assertEquals(VerticalLayout.Alignment.END, layout.getAlignItems());
        assertTrue(layout.hasClassName("custom"));
        assertEquals("red", layout.getStyle().get("color"));
    }

    @Test
    void orderedLayout_wrapAndThemeSpacingMatchRuntime() {
        Element view = view("<hbox wrap='true' spacing='true' themeNames='spacing-xl'/>");
        HorizontalLayout layout = assertInstanceOf(HorizontalLayout.class, load(view.element("hbox"), view));
        assertTrue(layout.isWrap());
        assertFalse(layout.getThemeNames().contains("spacing"));
        assertTrue(layout.getThemeNames().contains("spacing-xl"));
    }

    @Test
    void gridLayout_loadsColumnWidthAndGap() {
        Element view = view("<gridLayout columnMinWidth='10em' gap='1rem'/>");
        JmixGridLayout<?> grid = assertInstanceOf(JmixGridLayout.class, load(view.element("gridLayout"), view));
        assertEquals("10em", grid.getColumnMinWidth());
        assertEquals("1rem", grid.getGap());
    }

    @Test
    void splitAndFilter_loadRuntimeSizingDefaultsAndOverrides() {
        Element view = view("<split splitterPosition='40' orientation='VERTICAL'/><genericFilter/>");
        SplitLayout split = assertInstanceOf(SplitLayout.class, load(view.element("split"), view));
        assertEquals(40.0, split.getSplitterPosition());
        assertEquals(SplitLayout.Orientation.VERTICAL, split.getOrientation());
        Details filter = assertInstanceOf(Details.class, load(view.element("genericFilter"), view));
        assertEquals("100%", filter.getWidth());
        view.element("genericFilter").addAttribute("width", "25em");
        filter = assertInstanceOf(Details.class, load(view.element("genericFilter"), view));
        assertEquals("25em", filter.getWidth());
    }

    @Test
    void formItem_valueFillsResponsiveStepCellRegardlessOfAttachmentOrder() {
        var processor = new StudioLayoutComponentProcessor();
        for (boolean childFirst : List.of(false, true)) {
            FormLayout form = new FormLayout();
            FormLayout.FormItem item = new FormLayout.FormItem();
            TextField field = new TextField();
            if (childFirst) {
                processor.addChild(item, field, -1);
                processor.addChild(form, item, -1);
            } else {
                processor.addChild(form, item, -1);
                processor.addChild(item, field, -1);
            }
            assertEquals("100%", field.getWidth());
        }
        FormLayout autoResponsive = new FormLayout();
        autoResponsive.setAutoResponsive(true);
        TextField field = new TextField();
        field.setWidth("10em");
        processor.addChild(autoResponsive, field, -1);
        assertEquals("10em", field.getWidth());
    }

    @Test
    void markdown_preservesRuntimeWhitespaceSemantics() {
        String body = "\n    ## Indented markdown\n    first line\n";
        Element view = view("<markdown><content>" + body + "</content></markdown>");
        Markdown markdown = assertInstanceOf(Markdown.class, load(view.element("markdown"), view));
        assertEquals(body, markdown.getContent());
    }

    @Test
    void unresolvedPlaceholders_ownDefaultSizeAndRespectXmlOverrides() {
        for (String tag : List.of("fragment", "component")) {
            Element view = view("<" + tag + " class='unavailable.Component' width='25em'/>");
            Image placeholder = assertInstanceOf(Image.class, load(view.element(tag), view));
            assertEquals("25em", placeholder.getWidth());
            assertEquals("200px", placeholder.getHeight());
        }
    }

    @Test
    void actionButton_appliesStandardVariants() {
        for (var entry : Map.of("list_create", "primary", "list_remove", "error", "list_exclude", "error",
                "detail_saveClose", "primary", "lookup_select", "primary").entrySet()) {
            Element view = view("<actions><action id='test' type='" + entry.getKey()
                    + "'/></actions><button action='test'/>");
            JmixButton button = assertInstanceOf(JmixButton.class, load(view.element("button"), view));
            assertTrue(button.getThemeNames().contains(entry.getValue()), entry.getKey());
            assertNotNull(button.getIcon());
        }
        Element view = view("<button action='saveAndCloseAction'/>");
        JmixButton button = assertInstanceOf(JmixButton.class, load(view.element("button"), view));
        assertTrue(button.getThemeNames().contains("primary"));
    }

    @Test
    void actionButton_xmlOverridesActionDefaultsAndProperties() {
        Element view = view("""
                <actions>
                    <action id="test" type="list_create" actionVariant="DANGER"
                            text="Action text" description="Action title" enabled="false" visible="false"/>
                </actions>
                <button action="test" text="msg://custom" title="" icon="STAR"
                        enabled="true" visible="true" themeNames="small"/>
                """);
        JmixButton button = assertInstanceOf(JmixButton.class, load(view.element("button"), view));
        assertEquals("Custom", button.getText());
        assertEquals("", button.getTitle());
        assertTrue(button.isEnabled());
        assertTrue(button.isVisible());
        assertTrue(button.getThemeNames().containsAll(List.of("error", "small")));
        assertFalse(button.getThemeNames().contains("primary"));
        assertEquals("vaadin:star", button.getIcon().getElement().getAttribute("icon"));
    }

    @Test
    void actionButton_emptyTextAndDefaultVariantOverrideTypeDefaults() {
        Element view = view("<actions><action id='test' type='list_create' actionVariant='DEFAULT'/></actions>"
                + "<button action='test' text=''/>");
        JmixButton button = assertInstanceOf(JmixButton.class, load(view.element("button"), view));
        assertEquals("", button.getText());
        assertFalse(button.getThemeNames().contains("primary"));
    }

    @Test
    void pickerActions_renderWithoutStudioMetadataAndCanBeReplaced() {
        Element view = view("<comboBoxPicker><actions><action id='clear' type='value_clear'/>"
                + "<action id='incomplete'/></actions></comboBoxPicker>");
        Element element = view.element("comboBoxPicker");
        ComboBoxPicker<?> picker = assertInstanceOf(ComboBoxPicker.class, load(element, view));
        assertEquals(1, picker.getActions().size());
        var clearAction = picker.getAction("clear");
        assertNotNull(clearAction);
        assertNotNull(clearAction.getIcon());
        assertEquals(1, picker.getChildren().count());
        assertTrue(new StudioActionComponentProcessor().addAction(picker,
                new BaseAction<>("clear").withIcon(clearAction.getIcon()), 0));
        assertEquals(1, picker.getActions().size());

        picker = assertInstanceOf(ComboBoxPicker.class,
                StudioPreviewComponentProvider.loadSingleComponent(element, view, StudioPreviewEnvironment.NOOP));
        assertNotNull(picker);
        assertTrue(picker.getActions().isEmpty());
    }
}
