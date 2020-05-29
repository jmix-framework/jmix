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

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class TableSettings implements ComponentSettings, ComponentSettings.HasSettingsPresentation {

    protected String id;
    protected Boolean textSelection;

    protected String sortProperty;
    protected Boolean sortAscending;

    protected UUID presentationId;

    protected List<ColumnSettings> columns;

    public TableSettings() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getTextSelection() {
        return textSelection;
    }

    public void setTextSelection(Boolean textPresentation) {
        this.textSelection = textPresentation;
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(String sortProperty) {
        this.sortProperty = sortProperty;
    }

    public Boolean getSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(Boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    @Override
    public UUID getPresentationId() {
        return presentationId;
    }

    public void setPresentationId(UUID presentationId) {
        this.presentationId = presentationId;
    }

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

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }
    }
}
