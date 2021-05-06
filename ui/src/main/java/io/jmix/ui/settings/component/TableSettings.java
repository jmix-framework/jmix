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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public class TableSettings implements ComponentSettings {

    protected String id;
    protected Boolean textSelection;

    protected String sortProperty;
    protected Boolean sortAscending;

    protected List<ColumnSettings> columns;

    public TableSettings() {
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public Boolean getTextSelection() {
        return textSelection;
    }

    public void setTextSelection(Boolean textPresentation) {
        this.textSelection = textPresentation;
    }

    @Nullable
    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(@Nullable String sortProperty) {
        this.sortProperty = sortProperty;
    }

    @Nullable
    public Boolean getSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(@Nullable Boolean sortAscending) {
        this.sortAscending = sortAscending;
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
        protected Integer width;
        protected Boolean visible;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Nullable
        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        @Nullable
        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }
    }
}
