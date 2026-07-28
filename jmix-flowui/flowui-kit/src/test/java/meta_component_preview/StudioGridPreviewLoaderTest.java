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
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioFlowuiComponentsPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.loader.StudioGridPreviewLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioGridPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioGridPreviewLoader loader = new StudioGridPreviewLoader();

    /** Fake env backed by two maps, per plan: messageKey -> resolved text, propertyPath -> caption. */
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

    BaseElement columnsElement(Element... children) {
        BaseElement columns = element("columns");
        for (Element child : children) {
            columns.add(child);
        }
        return columns;
    }

    BaseElement gridElement(String tag, Element columns) {
        BaseElement grid = element(tag);
        grid.add(columns);
        return grid;
    }

    @Test
    void testDataGridInstantiatesJmixGrid() {
        assertInstanceOf(JmixGrid.class, loader.load(element("dataGrid"), element("view")));
    }

    @Test
    void testTreeDataGridInstantiatesJmixTreeGrid() {
        assertInstanceOf(JmixTreeGrid.class, loader.load(element("treeDataGrid"), element("view")));
    }

    @Test
    void testColumnSkeletonKeysAndOrder() {
        Element columns = columnsElement(
                withAttributes(element("column"), "property", "name"),
                withAttributes(element("column"), "property", "email"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        List<? extends Grid.Column<?>> gridColumns = grid.getColumns();
        assertEquals(2, gridColumns.size());
        assertEquals("name", gridColumns.get(0).getKey());
        assertEquals("email", gridColumns.get(1).getKey());
        assertNotNull(grid.getColumnByKey("name"));
        assertNotNull(grid.getColumnByKey("email"));
    }

    @Test
    void testColumnKeyFallsBackToProperty() {
        Element columns = columnsElement(withAttributes(element("column"), "property", "name"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        assertNotNull(grid.getColumnByKey("name"));
    }

    @Test
    void testColumnWithoutKeyOrPropertyIsSkipped() {
        Element columns = columnsElement(
                element("column"),
                withAttributes(element("column"), "key", "onlyValid"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        assertEquals(1, grid.getColumns().size());
        assertNotNull(grid.getColumnByKey("onlyValid"));
    }

    @Test
    void testColumnHeaderRawText() {
        Element columns = columnsElement(
                withAttributes(element("column"), "key", "name", "header", "Name"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        assertEquals("Name", grid.getColumnByKey("name").getHeaderText());
    }

    @Test
    void testColumnHeaderResolvesMessageReference() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://some.key", "Resolved Name");
        Element columns = columnsElement(
                withAttributes(element("column"), "key", "name", "header", "msg://some.key"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), env);

        assertEquals("Resolved Name", grid.getColumnByKey("name").getHeaderText());
    }

    @Test
    void testColumnHeaderDerivedFromPropertyCaption() {
        FakeEnv env = new FakeEnv();
        env.captions.put("name", "Name Caption");
        Element columns = columnsElement(withAttributes(element("column"), "property", "name"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), env);

        assertEquals("Name Caption", grid.getColumnByKey("name").getHeaderText());
    }

    @Test
    void testColumnHeaderFallsBackToPropertyWhenCaptionIsNull() {
        FakeEnv env = new FakeEnv();
        Element columns = columnsElement(withAttributes(element("column"), "property", "email"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), env);

        assertEquals("email", grid.getColumnByKey("email").getHeaderText());
    }

    @Test
    void testColumnWidthAutoWidthFlexGrowFrozenTextAlign() {
        Element columns = columnsElement(withAttributes(element("column"), "key", "name",
                "width", "200px", "autoWidth", "true", "flexGrow", "2", "frozen", "true",
                "textAlign", "CENTER"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        Grid.Column<?> column = grid.getColumnByKey("name");
        assertEquals("200px", column.getWidth());
        assertTrue(column.isAutoWidth());
        assertEquals(2, column.getFlexGrow());
        assertTrue(column.isFrozen());
        assertEquals(ColumnTextAlign.CENTER, column.getTextAlign());
    }

    @Test
    void testColumnsLevelSortableResizableDefaults() {
        Element columns = columnsElement(withAttributes(element("column"), "key", "name"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        Grid.Column<?> column = grid.getColumnByKey("name");
        assertTrue(column.isSortable());
        assertFalse(column.isResizable());
    }

    @Test
    void testColumnsLevelSortableResizableExplicitDefaults() {
        Element columns = withAttributes(columnsElement(withAttributes(element("column"), "key", "name")),
                "sortable", "false", "resizable", "true");
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        Grid.Column<?> column = grid.getColumnByKey("name");
        assertFalse(column.isSortable());
        assertTrue(column.isResizable());
    }

    @Test
    void testColumnLevelSortableResizableOverridesColumnsLevelDefaults() {
        Element columns = columnsElement(
                withAttributes(element("column"), "key", "name", "sortable", "false", "resizable", "true"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        Grid.Column<?> column = grid.getColumnByKey("name");
        assertFalse(column.isSortable());
        assertTrue(column.isResizable());
    }

    @Test
    void testColumnVisibleFalse() {
        Element columns = columnsElement(
                withAttributes(element("column"), "key", "name", "visible", "false"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        assertFalse(grid.getColumnByKey("name").isVisible());
    }

    @Test
    void testEditorActionsColumnDefaultKey() {
        Element columns = columnsElement(element("editorActionsColumn"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        assertNotNull(grid.getColumnByKey("editorActionsColumn"));
    }

    @Test
    void testUnknownColumnsChildIsSkippedSilently() {
        Element columns = columnsElement(
                element("groupColumn"),
                withAttributes(element("column"), "key", "onlyValid"));
        Grid<?> grid = (Grid<?>) loader.load(gridElement("dataGrid", columns), element("view"), new FakeEnv());

        assertEquals(1, grid.getColumns().size());
    }

    /**
     * Compatibility test: released Studio (&ge; 2.3.0) calls the 2-arg {@code load} entry
     * point, which routes to {@link StudioPreviewEnvironment#NOOP}. Without a real environment
     * handshake there is no bind-by-key guard on the caller side, so the loader must not build
     * any columns — old Studio builds its own on top of whatever the loader returns, and any
     * loader-built column would be duplicated in the designer.
     */
    @Test
    void testTwoArgLoadReturnsZeroColumnsForOldStudioCompatibility() {
        Element columns = columnsElement(
                withAttributes(element("column"), "key", "name", "header", "msg://unresolved.key"));
        Component component = loader.load(gridElement("dataGrid", columns), element("view"));

        Grid<?> grid = (Grid<?>) component;
        assertEquals(0, grid.getColumns().size());
    }

    @Test
    void testDataGridWithRealEnvironmentGetsThreePlaceholderRows() {
        Grid<?> grid = (Grid<?>) loader.load(element("dataGrid"), element("view"), new FakeEnv());

        assertEquals(3, grid.getListDataView().getItemCount());
    }

    @Test
    void testDataGridWithNoopEnvironmentGetsNoPlaceholderRows() {
        Grid<?> grid = (Grid<?>) loader.load(element("dataGrid"), element("view"), StudioPreviewEnvironment.NOOP);

        assertEquals(0, grid.getListDataView().getItemCount());
    }

    @Test
    void testTreeDataGridWithRealEnvironmentDoesNotThrow() {
        assertNotNull(loader.load(element("treeDataGrid"), element("view"), new FakeEnv()));
    }

    @Test
    void testFlowuiComponentsLoaderNoLongerSupportsDataGridOrTreeDataGrid() {
        StudioFlowuiComponentsPreviewLoader flowuiLoader = new StudioFlowuiComponentsPreviewLoader();
        assertFalse(flowuiLoader.isSupported(element("dataGrid")));
        assertFalse(flowuiLoader.isSupported(element("treeDataGrid")));
    }
}
