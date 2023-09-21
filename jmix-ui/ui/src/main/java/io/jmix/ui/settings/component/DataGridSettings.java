/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.settings.component;

import io.jmix.ui.component.DataGrid;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataGridSettings implements ComponentSettings {

    protected String id;

    protected Map<String, DataGrid.SortDirection> sortedColumnMap = new LinkedHashMap<>();

    protected String sortColumnId;
    protected DataGrid.SortDirection sortDirection;

    protected List<ColumnSettings> columns;

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getSortColumnId() {
        return sortColumnId;
    }

    public void setSortColumnId(@Nullable String sortColumnId) {
        this.sortColumnId = sortColumnId;
    }

    @Nullable
    public DataGrid.SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(@Nullable DataGrid.SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public void clearSortedColumnMap() {
        sortedColumnMap.clear();
    }

    public Map<String, DataGrid.SortDirection> getSortedColumnMap() {
        return Collections.unmodifiableMap(sortedColumnMap);
    }

    public void addSortedColumn(String sortedColumnId, DataGrid.SortDirection sortDirection) {
        sortedColumnMap.put(sortedColumnId, sortDirection);
    }

    @Nullable
    public List<ColumnSettings> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnSettings> columns) {
        this.columns = columns;
    }

    public static class ColumnSettings implements Serializable {

        protected String id;
        protected Double width;
        protected Boolean collapsed;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Nullable
        public Double getWidth() {
            return width;
        }

        public void setWidth(Double width) {
            this.width = width;
        }

        @Nullable
        public Boolean getCollapsed() {
            return collapsed;
        }

        public void setCollapsed(Boolean collapsed) {
            this.collapsed = collapsed;
        }
    }
}
