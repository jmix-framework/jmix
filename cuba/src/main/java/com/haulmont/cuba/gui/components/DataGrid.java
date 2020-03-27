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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.components.data.datagrid.SortableDatasourceDataGridItems;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.ui.components.data.DataGridItems;
import com.haulmont.cuba.gui.components.data.datagrid.DatasourceDataGridItems;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.components.DataGrid} instead
 */
@SuppressWarnings("rawtypes, unchecked")
@Deprecated
public interface DataGrid<E extends Entity> extends ListComponent<E>, io.jmix.ui.components.DataGrid<E> {

    /**
     * @return the DataGrid data source
     * @deprecated use {@link #getItems()} instead
     */
    @Deprecated
    default CollectionDatasource getDatasource() {
        DataGridItems<E> dataGridItems = getItems();
        return dataGridItems instanceof DatasourceDataGridItems
                ? ((DatasourceDataGridItems) dataGridItems).getDatasource()
                : null;
    }

    /**
     * Sets an instance of {@code CollectionDatasource} as the DataGrid data source.
     *
     * @param datasource the DataGrid data source, not null
     * @deprecated use {@link #setItems(DataGridItems)} instead
     */

    @Deprecated
    default void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setItems(null);
        } else {
            DataGridItems<E> dataGridItems;
            if (datasource instanceof CollectionDatasource.Sortable) {
                dataGridItems = new SortableDatasourceDataGridItems<>((CollectionDatasource.Sortable) datasource);
            } else {
                dataGridItems = new DatasourceDataGridItems<>(datasource);
            }
            setItems(dataGridItems);
        }
    }
}
