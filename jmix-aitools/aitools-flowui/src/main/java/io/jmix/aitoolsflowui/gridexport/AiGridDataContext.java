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
import org.jspecify.annotations.NullMarked;

import java.util.function.Predicate;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * The data grid whose rows the user wants to ask the AI about, together with the way its rows are read: the exporter
 * and the column filter of the export the user started from, so that the AI gets the values the export would write.
 */
@Experimental
@NullMarked
public class AiGridDataContext {

    protected final ListDataComponent<Object> target;
    protected final AbstractDataGridExporter<?> exporter;
    protected final Predicate<Grid.Column<Object>> columnFilter;

    /**
     * @param target       grid to read the rows from
     * @param exporter     exporter that reads the rows, with its column value providers
     * @param columnFilter columns to read
     */
    public AiGridDataContext(ListDataComponent<Object> target,
                             AbstractDataGridExporter<?> exporter,
                             Predicate<Grid.Column<Object>> columnFilter) {
        checkNotNullArgument(target);
        checkNotNullArgument(exporter);
        checkNotNullArgument(columnFilter);

        this.target = target;
        this.exporter = exporter;
        this.columnFilter = columnFilter;
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
}
