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

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioColumnComponentProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioColumnComponentProcessorTest {

    final StudioColumnComponentProcessor processor = new StudioColumnComponentProcessor();

    @Test
    void testAddColumnCreatesAtKeyAndPlacesAtIndex() {
        JmixGrid<Object> grid = new JmixGrid<>();

        assertTrue(processor.addColumn(grid, "name", -1));
        assertTrue(processor.addColumn(grid, "age", 0));

        assertEquals(List.of("age", "name"), columnKeys(grid));
    }

    @Test
    void testAddColumnReusesColumnAlreadyMaterializedAtLoadTimeWithoutDuplicating() {
        JmixGrid<Object> grid = new JmixGrid<>();
        // simulates StudioGridPreviewLoader's load-time column building
        Grid.Column<Object> loadTimeColumn = grid.addColumn(item -> "").setKey("email");

        assertTrue(processor.addColumn(grid, "email", -1));

        assertEquals(1, grid.getColumns().size());
        assertEquals(loadTimeColumn, grid.getColumnByKey("email"));
    }

    @Test
    void testRemoveColumnRemovesByKeyAndReportsUnhandledWhenAbsent() {
        JmixGrid<Object> grid = new JmixGrid<>();
        grid.addColumn(item -> "").setKey("name");

        assertTrue(processor.removeColumn(grid, "name"));
        assertNull(grid.getColumnByKey("name"));

        // No column matches now: must not throw, and must report false so Studio's fallback runs.
        assertFalse(processor.removeColumn(grid, "name"));
    }

    @Test
    void testAddAndRemoveColumnReturnFalseForNonGridParent() {
        Component parent = new VerticalLayout();

        assertFalse(processor.addColumn(parent, "name", -1));
        assertFalse(processor.removeColumn(parent, "name"));
    }

    private List<String> columnKeys(Grid<Object> grid) {
        return grid.getColumns().stream().map(Grid.Column::getKey).toList();
    }
}
