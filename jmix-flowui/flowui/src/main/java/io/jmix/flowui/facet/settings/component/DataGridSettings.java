/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet.settings.component;

import io.jmix.flowui.facet.settings.Settings;
import org.springframework.lang.Nullable;

import java.util.List;

public class DataGridSettings implements Settings {

    protected String id;
    protected List<SortOrder> sortOrder;
    protected List<Column> columns;

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
    public List<SortOrder> getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(@Nullable List<SortOrder> sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Nullable
    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(@Nullable List<Column> columns) {
        this.columns = columns;
    }

    public static class SortOrder {
        protected String key;
        protected String sortDirection;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getSortDirection() {
            return sortDirection;
        }

        public void setSortDirection(String sortDirection) {
            this.sortDirection = sortDirection;
        }
    }

    public static class Column {
        protected String key;
        protected String width;
        protected Boolean visible;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Nullable
        public String getWidth() {
            return width;
        }

        public void setWidth(@Nullable String width) {
            this.width = width;
        }

        @Nullable
        public Boolean isVisible() {
            return visible;
        }

        public void setVisible(@Nullable Boolean visible) {
            this.visible = visible;
        }
    }
}
