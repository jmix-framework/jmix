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

import com.haulmont.cuba.gui.components.data.datagrid.DatasourceDataGridItems;
import com.haulmont.cuba.gui.components.data.datagrid.SortableDatasourceDataGridItems;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.ui.component.data.DataGridItems;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.component.DataGrid} instead
 */
@SuppressWarnings("rawtypes, unchecked")
@Deprecated
public interface DataGrid<E extends Entity> extends ListComponent<E>, io.jmix.ui.component.DataGrid<E> {

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

    /**
     * @param <E> DataGrid data type
     * @param <T> column data type
     */
    interface ColumnGenerator<E extends Entity, T> {
        /**
         * Returns value for given event.
         *
         * @param event an event providing more information
         * @return generated value
         */
        T getValue(ColumnGeneratorEvent<E> event);

        /**
         * Return column type for this generator.
         *
         * @return type of generated property
         */
        Class<T> getType();
    }

    /**
     * INTERNAL
     * <p>
     * ColumnGenerator that is used for declaratively installed generators.
     *
     * @param <E> DataGrid data type
     * @param <T> Column data type
     */
    interface GenericColumnGenerator<E extends Entity, T> {

        /**
         * Returns value for given event.
         *
         * @param event an event providing more information
         * @return generated value
         */
        T getValue(ColumnGeneratorEvent<E> event);
    }

    /**
     * Add a generated column to the DataGrid.
     *
     * @param columnId  column identifier as defined in XML descriptor
     * @param generator column generator instance
     * @see #addGeneratedColumn(String, ColumnGenerator, int)
     */
    io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, ColumnGenerator<E, ?> generator);

    /**
     * Add a generated column to the DataGrid.
     *
     * @param columnId  column identifier as defined in XML descriptor
     * @param generator column generator instance
     * @param index     index of a new generated column
     * @see #addGeneratedColumn(String, ColumnGenerator)
     */
    io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, ColumnGenerator<E, ?> generator, int index);

    /**
     * INTERNAL
     * <p>
     * Adds a generated column to the DataGrid.
     *
     * @param columnId  column identifier as defined in XML descriptor
     * @param generator column generator instance
     */
    io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, GenericColumnGenerator<E, ?> generator);

    /**
     * A column in the DataGrid.
     */
    interface Column<E extends Entity> extends io.jmix.ui.component.DataGrid.Column<E> {

        /**
         * @return the type of value represented by this column
         */
        Class getType();

        /**
         * INTERNAL
         * <p>
         * Sets a type of generated column.
         *
         * @param generatedType generated column type
         */
        void setGeneratedType(Class generatedType);

        /**
         * INTERNAL.
         *
         * @return generated column type
         */
        Class getGeneratedType();
    }
}
