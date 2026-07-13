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
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.TextUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.kit.meta.component.preview.ComponentCreationResult;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.StudioStandardComponentsPreviewLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioStandardComponentsPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");
    static final Namespace FRAGMENT_NS = Namespace.get("http://jmix.io/schema/flowui/fragment");

    final StudioStandardComponentsPreviewLoader loader = new StudioStandardComponentsPreviewLoader();

    /** Fake env backed by a message map, per the dropdown/grid loader tests' FakeEnv pattern. */
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

    BaseElement userMenuElement(Element items) {
        BaseElement userMenu = element("userMenu");
        userMenu.add(items);
        return userMenu;
    }

    @SuppressWarnings("unchecked")
    JmixUserMenu<String> loadUserMenu(Element componentElement, Element viewElement, StudioPreviewEnvironment env) {
        return (JmixUserMenu<String>) loader.load(componentElement, viewElement, env);
    }

    // ---- fragment: pin existing behavior across both namespaces ----

    @Test
    void testFragmentWithFragmentSchemaInstantiatesVerticalLayout() {
        Element fragment = element("fragment", FRAGMENT_NS);
        assertInstanceOf(VerticalLayout.class, loader.load(fragment, element("view")));
    }

    @Test
    void testFragmentWithViewSchemaInstantiatesPlaceholderImage() {
        Element fragment = element("fragment", VIEW_NS);
        assertInstanceOf(Image.class, loader.load(fragment, element("view")));
    }

    // ---- userMenu: fallback items / setUser ----

    @Test
    void testUserMenuWithoutItemsElementUsesHardcodedFallbackItems() {
        JmixUserMenu<String> userMenu = loadUserMenu(element("userMenu"), element("view"), new FakeEnv());

        List<UserMenuItem> items = userMenu.getItems();
        assertEquals(3, items.size());
        assertEquals("Item #1", ((TextUserMenuItem) items.get(0)).getText());
        assertEquals("Item #2", ((TextUserMenuItem) items.get(1)).getText());
        assertEquals("Item #3", ((TextUserMenuItem) items.get(2)).getText());
    }

    @Test
    void testUserMenuWithOnlyUnsupportedItemsChildUsesHardcodedFallbackItems() {
        // <items> present but its only child (componentItem) is never rendered in preview:
        // falling back to placeholders beats showing an empty menu.
        Element componentItem = withAttributes(element("componentItem"), "id", "item1");
        componentItem.add(element("button"));
        Element items = itemsElement(componentItem);

        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        assertEquals(3, userMenu.getItems().size());
    }

    @Test
    void testUserMenuSetsAdminUser() {
        JmixUserMenu<String> userMenu = loadUserMenu(element("userMenu"), element("view"), new FakeEnv());
        assertEquals("admin", userMenu.getUser());
    }

    // ---- textItem ----

    @Test
    void testTextItemRawText() {
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "Hello"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertEquals("Hello", item.getText());
    }

    @Test
    void testTextItemResolvesMessageReference() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://some.key", "Resolved Text");
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "msg://some.key"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), env);

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertEquals("Resolved Text", item.getText());
    }

    @Test
    void testTextItemMessageReferenceFallsBackToRawKeyWithNoopEnv() {
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "item1", "text", "msg://unresolved.key"));
        // 2-arg load: routes through StudioPreviewEnvironment.NOOP.
        Component component = loader.load(userMenuElement(items), element("view"));

        TextUserMenuItem item = (TextUserMenuItem) ((JmixUserMenu<?>) component).getItem("item1");
        assertEquals("msg://unresolved.key", item.getText());
    }

    @Test
    void testTextItemWithoutTextIsSkipped() {
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        assertTrue(userMenu.getItems().isEmpty());
    }

    @Test
    void testTextItemIconAttributeApplied() {
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "item1", "text", "Hello", "icon", "CHECK"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertInstanceOf(Icon.class, item.getIcon());
    }

    @Test
    void testTextItemNestedSubItemsBuildOneLevel() {
        Element subItems = itemsElement(withAttributes(element("textItem"), "id", "sub1", "text", "Sub Item"));
        Element parentTextItem = withAttributes(element("textItem"), "id", "parent1", "text", "Parent");
        parentTextItem.add(subItems);
        Element items = itemsElement(parentTextItem);

        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        TextUserMenuItem parent = (TextUserMenuItem) userMenu.getItem("parent1");
        List<UserMenuItem> subMenuItems = parent.getSubMenu().getItems();
        assertEquals(1, subMenuItems.size());
        assertEquals("Sub Item", ((TextUserMenuItem) subMenuItems.get(0)).getText());
    }

    // ---- actionItem ----

    @Test
    void testActionItemInlineActionTextAndIcon() {
        Element actionElement =
                withAttributes(element("action"), "id", "act1", "text", "msg://act.text", "icon", "CHECK");
        Element actionItem = withAttributes(element("actionItem"), "id", "item1");
        actionItem.add(actionElement);
        Element items = itemsElement(actionItem);

        FakeEnv env = new FakeEnv();
        env.messages.put("msg://act.text", "Action Text");

        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), env);

        ActionUserMenuItem item = (ActionUserMenuItem) userMenu.getItem("item1");
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
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), viewElement, new FakeEnv());

        ActionUserMenuItem item = (ActionUserMenuItem) userMenu.getItem("item1");
        assertEquals("Ref Text", item.getAction().getText());
    }

    @Test
    void testActionItemRefMissFallsBackToIdAsText() {
        Element items = itemsElement(
                withAttributes(element("actionItem"), "id", "item1", "ref", "missingAction"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertEquals("item1", item.getText());
    }

    @Test
    void testActionItemWithoutIdIsSkipped() {
        Element items = itemsElement(element("actionItem"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        assertTrue(userMenu.getItems().isEmpty());
    }

    // ---- viewItem: text-only approximation ----

    @Test
    void testViewItemUsesResolvedText() {
        Element items = itemsElement(
                withAttributes(element("viewItem"), "id", "item1", "text", "msg://view.text", "viewId", "someView"));
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://view.text", "View Text");

        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), env);

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertEquals("View Text", item.getText());
    }

    @Test
    void testViewItemFallsBackToViewIdWhenTextMissing() {
        Element items = itemsElement(withAttributes(element("viewItem"), "id", "item1", "viewId", "someView"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertEquals("someView", item.getText());
    }

    @Test
    void testViewItemFallsBackToIdWhenTextAndViewIdMissing() {
        Element items = itemsElement(withAttributes(element("viewItem"), "id", "item1"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertEquals("item1", item.getText());
    }

    @Test
    void testViewItemIconAttributeApplied() {
        Element items = itemsElement(
                withAttributes(element("viewItem"), "id", "item1", "viewId", "someView", "icon", "CHECK"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        TextUserMenuItem item = (TextUserMenuItem) userMenu.getItem("item1");
        assertInstanceOf(Icon.class, item.getIcon());
    }

    // ---- separator / componentItem / order ----

    @Test
    void testSeparatorDoesNotThrowAndAddsNoItem() {
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "item1", "text", "Hello"),
                element("separator"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        assertEquals(1, userMenu.getItems().size());
    }

    @Test
    void testComponentItemIsSkipped() {
        Element componentItem = withAttributes(element("componentItem"), "id", "item1");
        componentItem.add(element("button"));
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "keep", "text", "Keep"),
                componentItem);
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        assertEquals(1, userMenu.getItems().size());
        assertTrue(userMenu.findItem("item1").isEmpty());
    }

    @Test
    void testItemCountAndOrder() {
        Element items = itemsElement(
                withAttributes(element("textItem"), "id", "first", "text", "First"),
                element("separator"),
                withAttributes(element("textItem"), "id", "second", "text", "Second"));
        JmixUserMenu<String> userMenu = loadUserMenu(userMenuElement(items), element("view"), new FakeEnv());

        List<UserMenuItem> menuItems = userMenu.getItems();
        assertEquals(2, menuItems.size());
        assertEquals("first", menuItems.get(0).getId());
        assertEquals("second", menuItems.get(1).getId());
    }

    // ---- ownedAspects ----

    @Test
    void testOwnedAspectsWithItemsElement() {
        Element items = itemsElement(withAttributes(element("textItem"), "id", "item1", "text", "Text"));
        assertEquals(Set.of(ComponentCreationResult.ITEMS), loader.ownedAspects(userMenuElement(items)));
    }

    @Test
    void testOwnedAspectsWithoutItemsElement() {
        assertEquals(Set.of(), loader.ownedAspects(element("userMenu")));
    }

    @Test
    void testOwnedAspectsForFragmentIsEmpty() {
        assertEquals(Set.of(), loader.ownedAspects(element("fragment", FRAGMENT_NS)));
    }
}
