/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.xml.layout.inittask;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class AssignGridColumnVisibilityPropertiesInitTask implements ComponentLoader.InitTask {

    protected View<?> view;
    protected DeferredLoadContext loadContext;

    public AssignGridColumnVisibilityPropertiesInitTask(DeferredLoadContext loadContext) {
        this.loadContext = loadContext;
    }

    @Override
    public void execute(ComponentLoader.ComponentContext context, View<?> view) {
        Preconditions.checkNotNullArgument(context);
        Preconditions.checkNotNullArgument(view);

        String dataGridId = loadContext.getDataGridId();
        Component gridComponent = UiComponentUtils.findComponent(view, dataGridId).orElse(null);

        if (!(gridComponent instanceof DataGrid<?> grid)) {
            throw new GuiDevelopmentException("Failed to find a data grid", context, "Data Grid", dataGridId);
        }

        JmixGridColumnVisibility columnVisibilityComponent = loadContext.getComponent();
        columnVisibilityComponent.setGrid(grid);

        loadColumnItems(grid);
    }

    protected void loadColumnItems(DataGrid<?> grid) {
        List<? extends DataGridColumn<?>> includeColumns = getColumnsToInclude(grid);

        JmixGridColumnVisibility columnVisibility = loadContext.getComponent();
        Map<String, ColumnItemParam> columnItemParams = loadContext.getColumnItemParams();
        for (DataGridColumn<?> column : includeColumns) {
            ColumnItemParam columnItemParam = columnItemParams.get(column.getKey());
            if (columnItemParam != null && !Strings.isNullOrEmpty(columnItemParam.getText())) {
                columnVisibility.addColumnItem(column, columnItemParam.getText());
            } else {
                columnVisibility.addColumnItem(column);
            }
        }
    }

    protected List<? extends DataGridColumn<?>> getColumnsToInclude(DataGrid<?> grid) {
        List<String> includeColumnKeys = splitByComma(loadContext.getIncludeColumns());
        List<String> excludeColumnKeys = splitByComma(loadContext.getExcludeColumns());

        if (includeColumnKeys.isEmpty()) {
            return grid.getColumns().stream()
                    .filter(c -> !excludeColumnKeys.contains(c.getKey()))
                    .map(c -> (DataGridColumn<?>) c)
                    .toList();
        } else {
            return grid.getColumns().stream()
                    .filter(c -> includeColumnKeys.contains(c.getKey()) && !excludeColumnKeys.contains(c.getKey()))
                    .map(c -> (DataGridColumn<?>) c)
                    .toList();
        }
    }

    protected List<String> splitByComma(@Nullable String str) {
        return Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(Strings.nullToEmpty(str));
    }

    public static class DeferredLoadContext {

        protected String dataGridId;
        protected JmixGridColumnVisibility component;
        protected String includeColumns;
        protected String excludeColumns;
        protected Map<String, ColumnItemParam> columnItemParams;

        public DeferredLoadContext(JmixGridColumnVisibility component, String dataGridId) {
            this.component = component;
            this.dataGridId = dataGridId;
        }

        public String getDataGridId() {
            return dataGridId;
        }

        public JmixGridColumnVisibility getComponent() {
            return component;
        }

        public String getIncludeColumns() {
            return includeColumns;
        }

        public void setIncludeColumns(String includeColumns) {
            this.includeColumns = includeColumns;
        }

        public String getExcludeColumns() {
            return excludeColumns;
        }

        public void setExcludeColumns(String excludeColumns) {
            this.excludeColumns = excludeColumns;
        }

        public Map<String, ColumnItemParam> getColumnItemParams() {
            return columnItemParams;
        }

        public void setColumnItemParams(Map<String, ColumnItemParam> columnItemParams) {
            this.columnItemParams = columnItemParams;
        }
    }

    public static class ColumnItemParam {

        protected String ref;
        protected String text;

        public ColumnItemParam(String ref) {
            this.ref = ref;
        }

        public String getRef() {
            return ref;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
