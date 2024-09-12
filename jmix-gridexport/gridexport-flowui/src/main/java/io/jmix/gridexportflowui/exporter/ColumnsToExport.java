/*
 * Copyright 2024 Haulmont.
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

package io.jmix.gridexportflowui.exporter;

import com.vaadin.flow.component.grid.Grid;

import java.util.function.Predicate;

/**
 * Columns that will be used for export.
 */
public enum ColumnsToExport {

    /**
     * Export all columns.
     */
    ALL_COLUMNS(column -> true),

    /**
     * Export only visible columns.
     */
    VISIBLE_COLUMNS(Grid.Column::isVisible);

    private final Predicate<Grid.Column<Object>> filterPredicate;

    ColumnsToExport(Predicate<Grid.Column<Object>> filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    public Predicate<Grid.Column<Object>> getFilterPredicate() {
        return filterPredicate;
    }
}
