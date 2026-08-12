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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.component.dropdownbutton.TextItem;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioDropdownButtonPreviewLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioDropdownButtonPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");
    static final Namespace OTHER_NS = Namespace.get("http://example.com/schema/other");

    final StudioDropdownButtonPreviewLoader loader = new StudioDropdownButtonPreviewLoader();

    /** Fake env backed by a message map, per the standard-components/grid loader tests' FakeEnv pattern. */
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

    BaseElement element(String name) {
        return new BaseElement(name, VIEW_NS);
    }

    BaseElement element(String name, Namespace namespace) {
        return new BaseElement(name, namespace);
    }

    BaseElement withAttributes(BaseElement element, String... nameValuePairs) {
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            element.addAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return element;
    }

    BaseElement itemsElement(Element... children) {
        BaseElement items = element("items");
        for (Element child : children) {
            items.add(child);
        }
        return items;
    }

    BaseElement buttonElement(String tag, Element items) {
        BaseElement button = element(tag);
        button.add(items);
        return button;
    }

    AbstractDropdownButton load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        return (AbstractDropdownButton) loader.load(componentElement, viewElement, environment);
    }

    /** The popup hangs off the last root menu-bar item: {@code comboButton} puts its action button first. */
    JmixSubMenu dropdownSubMenu(AbstractDropdownButton button) {
        List<MenuItem> rootItems = button.getContent().getItems();
        return ((JmixMenuItem) rootItems.get(rootItems.size() - 1)).getSubMenu();
    }

    void assertPlaceholderItems(AbstractDropdownButton button) {
        List<DropdownButtonItem> items = button.getItems();
        assertEquals(5, items.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("menuItem" + i, items.get(i).getId());
            assertEquals("Menu item " + i, ((TextItem) items.get(i)).getText());
        }
    }

    @Test
    void testIsSupportedForDropdownButtonAndComboButtonOnly() {
        assertTrue(loader.isSupported(element("dropdownButton")));
        assertTrue(loader.isSupported(element("comboButton")));
        assertFalse(loader.isSupported(element("button")));
        assertFalse(loader.isSupported(element("dropdownButton", OTHER_NS)));
    }

    @Test
    void testInstantiatesDropdownButtonAndComboButtonPerTag() {
        assertInstanceOf(DropdownButton.class, loader.load(element("dropdownButton"), element("view")));
        assertInstanceOf(ComboButton.class, loader.load(element("comboButton"), element("view")));
    }

    @Test
    void testItemsBuiltInDocumentOrderWithSeparator() {
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "item1", "text", "Hello"),
                element("separator"));

        AbstractDropdownButton button = load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        List<Component> popupChildren = dropdownSubMenu(button).getChildren().toList();
        assertEquals(2, popupChildren.size());
        assertEquals("Hello", ((MenuItem) popupChildren.get(0)).getText());
        assertInstanceOf(Hr.class, popupChildren.get(1));
        // getItems() filters separators out, so only the text item is addressable.
        assertEquals(1, button.getItems().size());
        assertEquals("item1", button.getItems().get(0).getId());
    }

    @Test
    void testTextItemResolvesMessageReference() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://some.key", "Resolved Text");
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "msg://some.key"));

        AbstractDropdownButton button = load(buttonElement("dropdownButton", items), element("view"), env);

        assertEquals("Resolved Text", ((TextItem) button.getItem("item1")).getText());
    }

    @Test
    void testTextItemWithoutIdIsSkipped() {
        Element items = itemsElement(withAttributes(element("textItem"), "text", "Hello"));

        AbstractDropdownButton button = load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        assertTrue(button.getItems().isEmpty());
        assertEquals(0, dropdownSubMenu(button).getChildren().count());
    }

    @Test
    void testActionItemWithInlineActionBuildsActionItem() {
        Element actionItem = withAttributes(element("actionItem"), "id", "item1");
        actionItem.add(withAttributes(element("action"), "id", "act1", "text", "Action Text"));
        Element items = itemsElement(actionItem);

        AbstractDropdownButton button = load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        ActionItem item = (ActionItem) button.getItem("item1");
        assertEquals("act1", item.getAction().getId());
        assertEquals("Action Text", item.getAction().getText());
    }

    @Test
    void testActionItemRefResolvedAgainstViewLevelAction() {
        Element view = element("view");
        Element actions = element("actions");
        actions.add(withAttributes(element("action"), "id", "refAction", "text", "Ref Text"));
        view.add(actions);
        Element items = itemsElement(withAttributes(element("actionItem"), "id", "item1", "ref", "refAction"));

        AbstractDropdownButton button = load(buttonElement("dropdownButton", items), view, new FakeEnv());

        ActionItem item = (ActionItem) button.getItem("item1");
        assertEquals("refAction", item.getAction().getId());
        assertEquals("Ref Text", item.getAction().getText());
    }

    /**
     * An {@code <items>} block holding nothing renderable must preview like an absent one:
     * {@code componentItem} needs the runtime {@code LayoutLoader} to build its nested content, so
     * the preview can't render it, and an empty popup would misrepresent a component that does show
     * items at runtime.
     */
    @Test
    void testItemsWithOnlyNonRenderableChildrenFallsBackToPlaceholderItems() {
        Element componentItem = withAttributes(element("componentItem"), "id", "item1");
        componentItem.add(element("button"));
        Element items = itemsElement(componentItem, element("unknownItem"));

        AbstractDropdownButton button = load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        assertPlaceholderItems(button);
        assertNull(button.getItem("item1"));
    }

    @Test
    void testAbsentItemsElementBuildsPlaceholderItemsWithRealEnv() {
        AbstractDropdownButton button = load(element("dropdownButton"), element("view"), new FakeEnv());

        assertPlaceholderItems(button);
    }

    @Test
    void testNoopEnvironmentSkipsPlaceholdersButStillBuildsDeclaredItems() {
        // 2-arg load: routes through StudioPreviewEnvironment.NOOP.
        AbstractDropdownButton bare = (AbstractDropdownButton) loader.load(element("dropdownButton"), element("view"));
        assertTrue(bare.getItems().isEmpty());

        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "Hello"));
        AbstractDropdownButton declared =
                (AbstractDropdownButton) loader.load(buttonElement("dropdownButton", items), element("view"));

        assertEquals(1, declared.getItems().size());
        assertEquals("Hello", ((TextItem) declared.getItem("item1")).getText());
    }

    @Test
    void testOpenOnHoverAttributeApplied() {
        Element component = withAttributes(element("dropdownButton"), "openOnHover", "true");

        assertTrue(load(component, element("view"), new FakeEnv()).isOpenOnHover());
    }

    @Test
    void testComboButtonDropdownIconAttributeApplied() {
        Element component = withAttributes(element("comboButton"), "dropdownIcon", "CHECK");

        ComboButton button = (ComboButton) load(component, element("view"), new FakeEnv());

        Component dropdownIcon = button.getDropdownIcon();
        assertInstanceOf(Icon.class, dropdownIcon);
        assertEquals("vaadin:check", ((Icon) dropdownIcon).getIcon());
    }

}
