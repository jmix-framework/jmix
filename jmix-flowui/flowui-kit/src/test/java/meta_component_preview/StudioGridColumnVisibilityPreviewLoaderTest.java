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
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioGridColumnVisibilityPreviewLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioGridColumnVisibilityPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioGridColumnVisibilityPreviewLoader loader = new StudioGridColumnVisibilityPreviewLoader();

    /** Fake env backed by two maps, per the grid loader test's FakeEnv pattern. */
    static class FakeEnv implements StudioPreviewEnvironment {
        final Map<String, String> messages = new HashMap<>();
        final Map<String, String> captions = new HashMap<>();

        @Override
        public String resolveMessage(String messageKey) {
            return messages.get(messageKey);
        }

        @Override
        public String propertyCaption(String dataContainerId, String metaClass, String propertyPath) {
            return captions.get(propertyPath);
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

    BaseElement columnElement(String... nameValuePairs) {
        return withAttributes(element("column"), nameValuePairs);
    }

    BaseElement columnsElement(Element... children) {
        BaseElement columns = element("columns");
        for (Element child : children) {
            columns.add(child);
        }
        return columns;
    }

    /** {@code nameValuePairs} must include an {@code id} pair; grid tag is separate. */
    BaseElement gridElement(String tag, Element columns, String... nameValuePairs) {
        BaseElement grid = withAttributes(element(tag), nameValuePairs);
        grid.add(columns);
        return grid;
    }

    /** Wraps {@code grid} in a {@code <view><layout>...} tree, mirroring real view structure. */
    BaseElement viewWithGrid(Element grid) {
        BaseElement view = element("view");
        BaseElement layout = element("layout");
        layout.add(grid);
        view.add(layout);
        return view;
    }

    BaseElement visibilityElement(String dataGridId, String... nameValuePairs) {
        BaseElement visibility = withAttributes(element("gridColumnVisibility"), nameValuePairs);
        visibility.addAttribute("dataGrid", dataGridId);
        return visibility;
    }

    JmixMenuItem rootItem(Component menuBar) {
        return (JmixMenuItem) ((JmixMenuBar) menuBar).getItems().get(0);
    }

    @Test
    void testIsSupportedForGridColumnVisibilityOnly() {
        assertTrue(loader.isSupported(element("gridColumnVisibility")));
        assertFalse(loader.isSupported(element("horizontalMenu")));
        assertFalse(loader.isSupported(element("dataGrid")));
    }

    @Test
    void testInstantiatesJmixMenuBarWithJmixRoleAttribute() {
        Component component = loader.load(visibilityElement("grid1"), element("view"));

        assertInstanceOf(JmixMenuBar.class, component);
        assertEquals("jmix-grid-column-visibility", component.getElement().getAttribute("jmix-role"));
    }

    /**
     * Old-Studio compatibility: without an environment handshake (2-arg {@code load}, routed to
     * {@link StudioPreviewEnvironment#NOOP}) the loader must build neither a root item nor submenu
     * entries — Studio's {@code postInitHasMenuItems} adds its own fake root + 5 entries on top of
     * whatever the loader returns, so a loader-built root here would give old Studio two roots.
     */
    @Test
    void testNoopEnvironmentBuildsBareMenuBarWithNoItems() {
        Element columns = columnsElement(columnElement("property", "name"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("grid1"), view);

        assertEquals(0, ((JmixMenuBar) component).getItems().size());
    }

    @Test
    void testFakeEnvBuildsRootAndSubmenuEntriesFromReferencedGrid() {
        Element columns = columnsElement(
                columnElement("property", "name"),
                columnElement("property", "email"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("grid1"), view, new FakeEnv());

        JmixMenuBar menuBar = (JmixMenuBar) component;
        assertEquals(1, menuBar.getItems().size());
        List<MenuItem> subItems = rootItem(menuBar).getSubMenu().getItems();
        assertEquals(2, subItems.size());
        assertEquals("name", subItems.get(0).getText());
        assertEquals("email", subItems.get(1).getText());
    }

    @Test
    void testResolvesTreeDataGridElementByIdToo() {
        Element columns = columnsElement(columnElement("property", "name"));
        Element grid = gridElement("treeDataGrid", columns, "id", "tree1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("tree1"), view, new FakeEnv());

        assertEquals(1, rootItem(component).getSubMenu().getItems().size());
    }

    @Test
    void testRootItemIconAttributeAddsIconIgnoringText() {
        Element visibility = withAttributes(visibilityElement("missingGrid"), "icon", "CHECK", "text", "Ignored");

        Component component = loader.load(visibility, element("view"), new FakeEnv());

        JmixMenuBar menuBar = (JmixMenuBar) component;
        assertEquals(1, menuBar.getItems().size());
        String text = rootItem(menuBar).getText();
        assertTrue(text == null || text.isEmpty());
    }

    @Test
    void testRootItemTextAttributeResolvesMessageReferenceWhenNoIcon() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://root.text", "Resolved Root");
        Element visibility = withAttributes(visibilityElement("missingGrid"), "text", "msg://root.text");

        Component component = loader.load(visibility, element("view"), env);

        assertEquals("Resolved Root", rootItem(component).getText());
    }

    @Test
    void testRootItemEmptyWhenNeitherIconNorText() {
        Component component = loader.load(visibilityElement("missingGrid"), element("view"), new FakeEnv());

        String text = rootItem(component).getText();
        assertTrue(text == null || text.isEmpty());
    }

    @Test
    void testLabelMenuItemOverrideTakesPrecedenceOverHeaderAndCaption() {
        Element columns = columnsElement(columnElement("property", "name", "header", "Header Text"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1", "dataContainer", "dc", "metaClass", "test_Entity");
        Element view = viewWithGrid(grid);
        Element visibility = visibilityElement("grid1");
        visibility.add(withAttributes(element("menuItem"), "refColumn", "name", "text", "Overridden"));

        FakeEnv env = new FakeEnv();
        env.captions.put("name", "Caption Text");

        Component component = loader.load(visibility, view, env);

        assertEquals("Overridden", rootItem(component).getSubMenu().getItems().get(0).getText());
    }

    @Test
    void testLabelMenuItemOverrideResolvesMessageReference() {
        Element columns = columnsElement(columnElement("property", "name"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);
        Element visibility = visibilityElement("grid1");
        visibility.add(withAttributes(element("menuItem"), "refColumn", "name", "text", "msg://col.name"));

        FakeEnv env = new FakeEnv();
        env.messages.put("msg://col.name", "Resolved Column Name");

        Component component = loader.load(visibility, view, env);

        assertEquals("Resolved Column Name", rootItem(component).getSubMenu().getItems().get(0).getText());
    }

    @Test
    void testLabelHeaderAttributeUsedWhenNoMenuItemOverride() {
        Element columns = columnsElement(columnElement("property", "name", "header", "Header Text"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        FakeEnv env = new FakeEnv();
        env.captions.put("name", "Caption Text"); // must be ignored: header wins

        Component component = loader.load(visibilityElement("grid1"), view, env);

        assertEquals("Header Text", rootItem(component).getSubMenu().getItems().get(0).getText());
    }

    @Test
    void testLabelHeaderResolvesMessageReference() {
        Element columns = columnsElement(columnElement("property", "name", "header", "msg://header.key"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        FakeEnv env = new FakeEnv();
        env.messages.put("msg://header.key", "Resolved Header");

        Component component = loader.load(visibilityElement("grid1"), view, env);

        assertEquals("Resolved Header", rootItem(component).getSubMenu().getItems().get(0).getText());
    }

    @Test
    void testLabelPropertyCaptionUsedWhenNoHeaderOrOverride() {
        Element columns = columnsElement(columnElement("property", "name"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1", "dataContainer", "dc", "metaClass", "test_Entity");
        Element view = viewWithGrid(grid);

        FakeEnv env = new FakeEnv();
        env.captions.put("name", "Caption Text");

        Component component = loader.load(visibilityElement("grid1"), view, env);

        assertEquals("Caption Text", rootItem(component).getSubMenu().getItems().get(0).getText());
    }

    @Test
    void testLabelFallsBackToRawPropertyWhenCaptionIsNull() {
        Element columns = columnsElement(columnElement("property", "email"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("grid1"), view, new FakeEnv());

        assertEquals("email", rootItem(component).getSubMenu().getItems().get(0).getText());
    }

    @Test
    void testLabelFallsBackToKeyWhenOnlyKeyDeclared() {
        Element columns = columnsElement(columnElement("key", "customKey"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("grid1"), view, new FakeEnv());

        assertEquals("customKey", rootItem(component).getSubMenu().getItems().get(0).getText());
    }

    @Test
    void testColumnWithoutKeyOrPropertyIsSkipped() {
        Element columns = columnsElement(
                element("column"),
                columnElement("key", "onlyValid"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("grid1"), view, new FakeEnv());

        assertEquals(1, rootItem(component).getSubMenu().getItems().size());
    }

    @Test
    void testNonColumnChildrenOfColumnsAreIgnored() {
        Element columns = columnsElement(
                element("editorActionsColumn"),
                columnElement("property", "name"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("grid1"), view, new FakeEnv());

        assertEquals(1, rootItem(component).getSubMenu().getItems().size());
    }

    @Test
    void testIncludeFiltersToListedKeysPreservingGridDocumentOrder() {
        Element columns = columnsElement(
                columnElement("property", "name"),
                columnElement("property", "email"),
                columnElement("property", "age"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);
        // include lists "age" before "name", but grid document order must still win.
        Element visibility = withAttributes(visibilityElement("grid1"), "include", "age, name");

        Component component = loader.load(visibility, view, new FakeEnv());

        List<MenuItem> items = rootItem(component).getSubMenu().getItems();
        assertEquals(2, items.size());
        assertEquals("name", items.get(0).getText());
        assertEquals("age", items.get(1).getText());
    }

    @Test
    void testExcludeDropsListedKeys() {
        Element columns = columnsElement(
                columnElement("property", "name"),
                columnElement("property", "email"));
        Element grid = gridElement("dataGrid", columns, "id", "grid1");
        Element view = viewWithGrid(grid);
        Element visibility = withAttributes(visibilityElement("grid1"), "exclude", "email");

        Component component = loader.load(visibility, view, new FakeEnv());

        List<MenuItem> items = rootItem(component).getSubMenu().getItems();
        assertEquals(1, items.size());
        assertEquals("name", items.get(0).getText());
    }

    /**
     * Pins the build gate to a missing/empty {@code dataGrid} attribute (normal transient state
     * while typing the XML): Studio's postInitHasMenuItems is NOT skipped and adds its own root —
     * so the loader must build nothing even with a real environment, or the preview would show
     * two dropdown roots.
     */
    @Test
    void testFakeEnvWithoutDataGridAttributeBuildsNoItems() {
        Component component = loader.load(element("gridColumnVisibility"), element("view"), new FakeEnv());

        assertEquals(0, ((JmixMenuBar) component).getItems().size());
        assertEquals("jmix-grid-column-visibility", component.getElement().getAttribute("jmix-role"));
    }

    @Test
    void testMissingGridReferenceBuildsRootOnly() {
        Component component = loader.load(visibilityElement("nonexistentGrid"), element("view"), new FakeEnv());

        assertEquals(1, ((JmixMenuBar) component).getItems().size());
        assertEquals(0, rootItem(component).getSubMenu().getItems().size());
    }

    @Test
    void testGridWithoutColumnsElementBuildsRootOnly() {
        BaseElement grid = element("dataGrid");
        grid.addAttribute("id", "grid1");
        Element view = viewWithGrid(grid);

        Component component = loader.load(visibilityElement("grid1"), view, new FakeEnv());

        assertEquals(0, rootItem(component).getSubMenu().getItems().size());
    }

}
