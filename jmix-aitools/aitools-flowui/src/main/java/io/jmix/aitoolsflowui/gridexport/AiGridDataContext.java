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

package io.jmix.aitoolsflowui.gridexport;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Predicate;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * The data grid whose rows the user wants to ask the AI about, the way its rows are read and the rows the user may
 * choose from. The rows are read with the exporter and the column filter of the export the user started from, or of
 * the "Ask AI" action used by itself, so that the AI gets the values the export would write.
 */
@Experimental
@NullMarked
public class AiGridDataContext {

    protected final ListDataComponent<Object> target;
    protected final AbstractDataGridExporter<?> exporter;
    protected final Predicate<Grid.Column<Object>> columnFilter;
    protected final List<ExportMode> exportModes;

    /**
     * @param target       grid to read the rows from
     * @param exporter     exporter that reads the rows, with its column value providers
     * @param columnFilter columns to read
     * @param exportModes  rows the user may choose from: {@link ExportMode#SELECTED_ROWS} and/or
     *                     {@link ExportMode#CURRENT_PAGE}
     * @throws IllegalArgumentException if the modes contain {@link ExportMode#ALL_ROWS}, which is never sent
     */
    public AiGridDataContext(ListDataComponent<Object> target,
                             AbstractDataGridExporter<?> exporter,
                             Predicate<Grid.Column<Object>> columnFilter,
                             List<ExportMode> exportModes) {
        checkNotNullArgument(target);
        checkNotNullArgument(exporter);
        checkNotNullArgument(columnFilter);
        checkNotNullArgument(exportModes);
        if (exportModes.contains(ExportMode.ALL_ROWS)) {
            throw new IllegalArgumentException("Unsupported export mode: " + ExportMode.ALL_ROWS);
        }

        this.target = target;
        this.exporter = exporter;
        this.columnFilter = columnFilter;
        this.exportModes = List.copyOf(exportModes);
    }

    /**
     * Returns the grid to read the rows from.
     *
     * @return the grid
     */
    public ListDataComponent<Object> getTarget() {
        return target;
    }

    /**
     * Returns the exporter that reads the rows.
     *
     * @return the exporter
     */
    public AbstractDataGridExporter<?> getExporter() {
        return exporter;
    }

    /**
     * Returns the filter of the columns to read.
     *
     * @return the column filter
     */
    public Predicate<Grid.Column<Object>> getColumnFilter() {
        return columnFilter;
    }

    /**
     * Returns the rows the user may choose from.
     *
     * @return {@link ExportMode#SELECTED_ROWS} and/or {@link ExportMode#CURRENT_PAGE}, possibly none
     */
    public List<ExportMode> getExportModes() {
        return exportModes;
    }
}
