/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.entity;

import io.jmix.core.entity.KeyValueEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JmixTableData implements Serializable {

    /**
     * Data represents band/group/table name and list of rows as key(column)-value maps.
     */
    protected Map<String, List<KeyValueEntity>> data;

    /**
     * Headers contain band/group/table name and set of pairs 'column name - column type as Class'.
     */
    protected Map<String, Set<ColumnInfo>> headers;

    public JmixTableData(Map<String, List<KeyValueEntity>> data, Map<String, Set<ColumnInfo>> headers) {
        this.data = data;
        this.headers = headers;
    }

    public Map<String, List<KeyValueEntity>> getData() {
        return data;
    }

    public void setData(Map<String, List<KeyValueEntity>> data) {
        this.data = data;
    }

    public Map<String, Set<ColumnInfo>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Set<ColumnInfo>> headers) {
        this.headers = headers;
    }

    public static class ColumnInfo implements Serializable {

        protected String key;

        protected Class columnClass;

        protected String caption;

        protected Integer position;

        public ColumnInfo(String key, Class columnClass, String caption) {
            this(key, columnClass, caption, null);
        }

        public ColumnInfo(String key, Class columnClass, String caption, Integer position) {
            this.key = key;
            this.columnClass = columnClass;
            this.caption = caption;
            this.position = position;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Class getColumnClass() {
            return columnClass;
        }

        public void setColumnClass(Class columnClass) {
            this.columnClass = columnClass;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ColumnInfo)) return false;
            ColumnInfo that = (ColumnInfo) o;
            return Objects.equals(getPosition(), that.getPosition()) &&
                    Objects.equals(getKey(), that.getKey()) &&
                    Objects.equals(getColumnClass(), that.getColumnClass()) &&
                    Objects.equals(getCaption(), that.getCaption());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getKey(), getColumnClass(), getCaption(), getPosition());
        }
    }
}
