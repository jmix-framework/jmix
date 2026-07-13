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

package io.jmix.flowui.kit.meta.component.preview.processor;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewColumnProcessor;

/**
 * Studio preview processor for {@link Grid} columns.
 * <p>
 * Columns declared in the initial view XML are already materialized at load time by
 * {@code StudioGridPreviewLoader}; {@link #addColumn} reuses that column (looked up by key) instead
 * of creating a duplicate.
 */
public class StudioColumnComponentProcessor implements StudioPreviewColumnProcessor {

    @Override
    public boolean addColumn(Component parent, String key, int index) {
        if (parent instanceof Grid<?> grid) {
            return addColumn(grid, key, index);
        }
        return false;
    }

    private <T> boolean addColumn(Grid<T> grid, String key, int index) {
        Grid.Column<T> column = grid.getColumnByKey(key);
        if (column == null) {
            column = grid.addColumn(item -> "").setKey(key);
        }
        if (index >= 0) {
            reorderColumn(grid, column, index);
        }
        return true;
    }

    private <T> void reorderColumn(Grid<T> grid, Grid.Column<T> column, int index) {
        List<Grid.Column<T>> columns = new ArrayList<>(grid.getColumns());
        columns.remove(column);
        int clampedIndex = Math.min(Math.max(index, 0), columns.size());
        columns.add(clampedIndex, column);
        grid.setColumnOrder(columns);
    }

    @Override
    public boolean removeColumn(Component parent, String key) {
        if (!(parent instanceof Grid<?> grid)) {
            return false;
        }
        // removeColumnByKey throws for an unknown key: guard so this stays idempotent.
        if (grid.getColumnByKey(key) != null) {
            grid.removeColumnByKey(key);
        }
        return true;
    }
}
