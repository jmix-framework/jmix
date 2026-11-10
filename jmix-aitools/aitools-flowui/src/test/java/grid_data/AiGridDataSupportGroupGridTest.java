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

package grid_data;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.aitools.AiExclusionSupport;
import io.jmix.aitoolsflowui.gridexport.AiGridDataContext;
import io.jmix.aitoolsflowui.gridexport.AiGridDataSupport;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.groupgrid.GroupListDataComponent;
import io.jmix.flowui.component.groupgrid.adapter.AbstractGroupDataGridAdapter;
import io.jmix.flowui.component.groupgrid.adapter.GroupDataGridAdapterFactory;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A group grid's columns are checked through its adapter, the way the exporters read its rows.
 */
class AiGridDataSupportGroupGridTest {

    AiGridDataSupport gridDataSupport;
    AiExclusionSupport exclusionSupport;
    AbstractGroupDataGridAdapter<Object> adapter;
    GroupListDataComponent<Object> groupGrid;
    Grid.Column<Object> groupColumn;
    Grid.Column<Object> column;
    MetaPropertyPath columnPath;
    Grid.Column<Object> groupingColumn;
    MetaPropertyPath groupingColumnPath;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        exclusionSupport = mock(AiExclusionSupport.class);
        groupGrid = mock(GroupListDataComponent.class);
        adapter = mock(AbstractGroupDataGridAdapter.class);
        groupColumn = mock(Grid.Column.class);
        column = mock(Grid.Column.class);
        columnPath = mock(MetaPropertyPath.class);
        groupingColumn = mock(Grid.Column.class);
        groupingColumnPath = mock(MetaPropertyPath.class);
        // the group column has no property of its own, so taken as it is it would always pass
        when(adapter.getColumns()).thenReturn(List.of(groupColumn, column, groupingColumn));
        when(adapter.isGroupColumn(groupColumn)).thenReturn(true);
        when(adapter.getColumnMetaPropertyPath(column)).thenReturn(columnPath);
        when(adapter.getColumnMetaPropertyPath(groupingColumn)).thenReturn(groupingColumnPath);
        when(exclusionSupport.isExcluded(groupingColumnPath)).thenReturn(true);

        GroupDataGridAdapterFactory adapterFactory = mock(GroupDataGridAdapterFactory.class);
        when(adapterFactory.getAdapter(groupGrid)).thenReturn(adapter);

        gridDataSupport = new AiGridDataSupport();
        ReflectionTestUtils.setField(gridDataSupport, "exclusionSupport", exclusionSupport);
        ReflectionTestUtils.setField(gridDataSupport, "groupDataGridAdapterFactory", adapterFactory);
    }

    @Test
    void hasAllowedColumns_groupGridColumnExcluded_false() {
        when(exclusionSupport.isExcluded(columnPath)).thenReturn(true);

        assertFalse(gridDataSupport.hasAllowedColumns(context(groupGrid)));
    }

    @Test
    void hasAllowedColumns_groupGridColumnAllowed_true() {
        when(exclusionSupport.isExcluded(columnPath)).thenReturn(false);

        assertTrue(gridDataSupport.hasAllowedColumns(context(groupGrid)));
    }

    @Test
    void hasAllowedColumns_groupingColumnAllowedButFilteredOut_true() {
        when(exclusionSupport.isExcluded(columnPath)).thenReturn(true);
        when(exclusionSupport.isExcluded(groupingColumnPath)).thenReturn(false);
        when(adapter.getGroupingColumns()).thenReturn(List.of(groupingColumn));

        // the exporter reads the grouping columns whatever the filter says
        assertTrue(gridDataSupport.hasAllowedColumns(context(groupGrid, c -> c == column)));
    }

    AiGridDataContext context(ListDataComponent<Object> target) {
        return context(target, c -> true);
    }

    AiGridDataContext context(ListDataComponent<Object> target, Predicate<Grid.Column<Object>> columnFilter) {
        return new AiGridDataContext(target, mock(AbstractDataGridExporter.class), columnFilter,
                List.of(ExportMode.CURRENT_PAGE));
    }
}
