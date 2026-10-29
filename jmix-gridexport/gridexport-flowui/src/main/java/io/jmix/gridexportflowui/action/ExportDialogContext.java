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

package io.jmix.gridexportflowui.action;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.gridexportflowui.exporter.DataGridExporter;
import org.jspecify.annotations.NullMarked;

import java.util.function.Predicate;

/**
 * The export an {@link ExportDialogActionContributor} adds its actions for.
 */
@NullMarked
public class ExportDialogContext {

    protected final ListDataComponent<Object> target;
    protected final ExportAction exportAction;
    protected final DataGridExporter exporter;
    protected final Predicate<Grid.Column<Object>> columnFilter;

    /**
     * @param target       component being exported
     * @param exportAction action that opened the dialog
     * @param exporter     exporter of the action, with its column value providers
     * @param columnFilter columns the export would write, as configured on the action
     */
    public ExportDialogContext(ListDataComponent<Object> target,
                               ExportAction exportAction,
                               DataGridExporter exporter,
                               Predicate<Grid.Column<Object>> columnFilter) {
        this.target = target;
        this.exportAction = exportAction;
        this.exporter = exporter;
        this.columnFilter = columnFilter;
    }

    /**
     * Returns the component being exported.
     *
     * @return exported component
     */
    public ListDataComponent<Object> getTarget() {
        return target;
    }

    /**
     * Returns the action that opened the export dialog.
     *
     * @return export action
     */
    public ExportAction getExportAction() {
        return exportAction;
    }

    /**
     * Returns the exporter of the action, with its column value providers. It can be any {@link DataGridExporter};
     * rows can be read with {@code readRows} only when it is an {@code AbstractDataGridExporter}.
     *
     * @return exporter of the action
     */
    public DataGridExporter getExporter() {
        return exporter;
    }

    /**
     * Returns the filter of the columns the export would write, as configured on the action.
     *
     * @return column filter
     */
    public Predicate<Grid.Column<Object>> getColumnFilter() {
        return columnFilter;
    }
}
