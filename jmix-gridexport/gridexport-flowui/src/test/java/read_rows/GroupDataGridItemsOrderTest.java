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

package read_rows;

import io.jmix.flowui.component.groupgrid.GroupInfo;
import io.jmix.flowui.component.groupgrid.adapter.AbstractGroupDataGridAdapter;
import io.jmix.flowui.component.groupgrid.data.GroupDataGridItems;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoaderFactory;
import io.jmix.gridexportflowui.exporter.json.JsonExporter;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A group data grid is read in the order of its group tree, which is not the order of the grid's item container.
 */
@SuppressWarnings("unchecked")
class GroupDataGridItemsOrderTest {

    JsonExporter exporter = new JsonExporter(mock(Metadata.class), mock(AllEntitiesLoaderFactory.class));
    AbstractGroupDataGridAdapter<Object> groupDataGrid = mock(AbstractGroupDataGridAdapter.class);
    GroupDataGridItems<Object> groupDataGridItems = mock(GroupDataGridItems.class);

    @BeforeEach
    void setUp() {
        when(groupDataGrid.getItems()).thenReturn(groupDataGridItems);
    }

    @Test
    void getGroupDataGridItems_walksGroupTreeDepthFirst_subGroupsBeforeOwnItems() {
        GroupInfo root1 = mock(GroupInfo.class);
        GroupInfo root1Child = mock(GroupInfo.class);
        GroupInfo root2 = mock(GroupInfo.class);
        // root1 has both a sub-group and own items.
        when(groupDataGrid.hasGroups()).thenReturn(true);
        when(groupDataGrid.getRootGroups()).thenReturn(List.of(root1, root2));
        when(groupDataGrid.getChildren(root1)).thenReturn(List.of(root1Child));
        when(groupDataGrid.getChildren(root1Child)).thenReturn(List.of());
        when(groupDataGrid.getChildren(root2)).thenReturn(List.of());
        when(groupDataGridItems.getOwnChildItems(root1)).thenReturn(List.of("root1-own"));
        when(groupDataGridItems.getOwnChildItems(root1Child)).thenReturn(List.of("child-own"));
        when(groupDataGridItems.getOwnChildItems(root2)).thenReturn(List.of("root2-own"));
        // The container order differs from the group order.
        when(groupDataGridItems.getItems()).thenReturn(List.of("root2-own", "root1-own", "child-own"));

        List<Object> items = ReflectionTestUtils.invokeMethod(exporter, "getGroupDataGridItems", groupDataGrid);

        assertEquals(List.of("child-own", "root1-own", "root2-own"), items);
    }

    @Test
    void getGroupDataGridItems_withoutGroups_keepsContainerOrder() {
        when(groupDataGrid.hasGroups()).thenReturn(false);
        when(groupDataGridItems.getItems()).thenReturn(List.of("b", "a", "c"));

        List<Object> items = ReflectionTestUtils.invokeMethod(exporter, "getGroupDataGridItems", groupDataGrid);

        assertEquals(List.of("b", "a", "c"), items);
    }
}
