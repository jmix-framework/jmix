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
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.component.dropdownbutton.TextItem;
import io.jmix.flowui.kit.meta.component.preview.ComponentCreationResult;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioDropdownButtonPreviewLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioDropdownButtonPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioDropdownButtonPreviewLoader loader = new StudioDropdownButtonPreviewLoader();

    /** Fake env backed by a message map, per the grid loader test's FakeEnv pattern. */
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

    @Test
    void testDropdownButtonInstantiatesDropdownButton() {
        assertInstanceOf(DropdownButton.class, loader.load(element("dropdownButton"), element("view")));
    }

    @Test
    void testComboButtonInstantiatesComboButton() {
        assertInstanceOf(ComboButton.class, loader.load(element("comboButton"), element("view")));
    }

    @Test
    void testTextItemRawText() {
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "Hello"));
        AbstractDropdownButton button = (AbstractDropdownButton)
                loader.load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        TextItem item = (TextItem) button.getItem("item1");
        assertEquals("Hello", item.getText());
    }

    @Test
    void testTextItemResolvesMessageReference() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://some.key", "Resolved Text");
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "msg://some.key"));
        AbstractDropdownButton button =
                (AbstractDropdownButton) loader.load(buttonElement("dropdownButton", items), element("view"), env);

        TextItem item = (TextItem) button.getItem("item1");
        assertEquals("Resolved Text", item.getText());
    }

    @Test
    void testTextItemMessageReferenceFallsBackToRawKeyWithNoopEnv() {
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "item1", "text", "msg://unresolved.key"));
        // 2-arg load: routes through StudioPreviewEnvironment.NOOP.
        Component component = loader.load(buttonElement("dropdownButton", items), element("view"));

        TextItem item = (TextItem) ((AbstractDropdownButton) component).getItem("item1");
        assertEquals("msg://unresolved.key", item.getText());
    }

    @Test
    void testActionItemInlineActionTextAndIcon() {
        Element actionElement =
                withAttributes(element("action"), "id", "act1", "text", "msg://act.text", "icon", "CHECK");
        Element actionItem = withAttributes(element("actionItem"), "id", "item1");
        actionItem.add(actionElement);
        Element items = itemsElement(actionItem);

        FakeEnv env = new FakeEnv();
        env.messages.put("msg://act.text", "Action Text");

        AbstractDropdownButton button =
                (AbstractDropdownButton) loader.load(buttonElement("dropdownButton", items), element("view"), env);

        ActionItem item = (ActionItem) button.getItem("item1");
        assertEquals("Action Text", item.getAction().getText());
        assertInstanceOf(Icon.class, item.getAction().getIcon());
    }

    @Test
    void testActionItemRefResolvedAgainstViewLevelAction() {
        Element viewElement = element("view");
        Element actionsElement = element("actions");
        actionsElement.add(withAttributes(element("action"), "id", "refAction", "text", "Ref Text"));
        viewElement.add(actionsElement);

        Element items = itemsElement(withAttributes(element("actionItem"), "id", "item1", "ref", "refAction"));
        AbstractDropdownButton button = (AbstractDropdownButton)
                loader.load(buttonElement("dropdownButton", items), viewElement, new FakeEnv());

        ActionItem item = (ActionItem) button.getItem("item1");
        assertEquals("Ref Text", item.getAction().getText());
    }

    @Test
    void testActionItemRefMissFallsBackToIdAsText() {
        Element items = itemsElement(
                withAttributes(element("actionItem"), "id", "item1", "ref", "missingAction"));
        AbstractDropdownButton button = (AbstractDropdownButton)
                loader.load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        TextItem item = (TextItem) button.getItem("item1");
        assertEquals("item1", item.getText());
    }

    @Test
    void testActionItemWithoutIdIsSkipped() {
        Element items = itemsElement(element("actionItem"));
        AbstractDropdownButton button = (AbstractDropdownButton)
                loader.load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        assertEquals(0, button.getItems().size());
    }

    @Test
    void testSeparatorDoesNotThrowAndAddsNoItem() {
        Element items = itemsElement(element("separator"));
        AbstractDropdownButton button = (AbstractDropdownButton)
                loader.load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        assertEquals(0, button.getItems().size());
    }

    @Test
    void testComponentItemIsSkipped() {
        Element componentItem = withAttributes(element("componentItem"), "id", "item1");
        componentItem.add(element("button"));
        Element items = itemsElement(componentItem);
        AbstractDropdownButton button = (AbstractDropdownButton)
                loader.load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        assertEquals(0, button.getItems().size());
        assertNull(button.getItem("item1"));
    }

    @Test
    void testItemCountAndOrder() {
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "first", "text", "First"),
                element("separator"),
                withAttributes(element("textItem"), "id", "second", "text", "Second"));
        AbstractDropdownButton button = (AbstractDropdownButton)
                loader.load(buttonElement("dropdownButton", items), element("view"), new FakeEnv());

        List<DropdownButtonItem> buttonItems = button.getItems();
        assertEquals(2, buttonItems.size());
        assertEquals("first", buttonItems.get(0).getId());
        assertEquals("second", buttonItems.get(1).getId());
    }

    @Test
    void testButtonIconAttributeApplied() {
        Element button = withAttributes(element("dropdownButton"), "icon", "CHECK");
        AbstractDropdownButton result = (AbstractDropdownButton) loader.load(button, element("view"));
        assertInstanceOf(Icon.class, result.getIcon());
    }

    @Test
    void testDropdownIconAttributeAppliedToComboButtonOnly() {
        Element comboElement = withAttributes(element("comboButton"), "dropdownIcon", "CHECK");
        ComboButton comboButton = (ComboButton) loader.load(comboElement, element("view"));
        assertInstanceOf(Icon.class, comboButton.getDropdownIcon());

        Element dropdownElement = withAttributes(element("dropdownButton"), "dropdownIcon", "CHECK");
        AbstractDropdownButton dropdownButton =
                (AbstractDropdownButton) loader.load(dropdownElement, element("view"));
        assertNull(dropdownButton.getIcon());
    }

    @Test
    void testOpenOnHoverApplied() {
        Element button = withAttributes(element("dropdownButton"), "openOnHover", "true");
        AbstractDropdownButton result = (AbstractDropdownButton) loader.load(button, element("view"));
        assertTrue(result.isOpenOnHover());
    }

    @Test
    void testOwnedAspectsWithItemsElement() {
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "Text"));
        assertEquals(Set.of(ComponentCreationResult.ITEMS),
                loader.ownedAspects(buttonElement("dropdownButton", items)));
    }

    @Test
    void testOwnedAspectsWithoutItemsElement() {
        assertEquals(Set.of(), loader.ownedAspects(element("dropdownButton")));
    }
}
