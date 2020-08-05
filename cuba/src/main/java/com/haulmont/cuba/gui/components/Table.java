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

import com.haulmont.cuba.gui.components.data.table.DatasourceTableItems;
import com.haulmont.cuba.gui.components.data.table.SortableDatasourceTableItems;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.JmixEntity;
import io.jmix.core.AppBeans;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.TableItems;

import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.component.Table} instead
 */
@Deprecated
@SuppressWarnings("rawtypes")
public interface Table<E extends JmixEntity> extends ListComponent<E>, io.jmix.ui.component.Table<E>, HasSettings,
        HasDataLoadingSettings, HasRowsCount, RowsCount.RowsCountTarget {

    /**
     * Sets {@code CollectionDatasource} as Table data source.
     *
     * @param datasource collection datasource
     * @deprecated Use {@link #setItems(TableItems)} instead
     */
    @Deprecated
    default void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setItems(null);
        } else {
            TableItems<E> tableItems;
            if (datasource instanceof CollectionDatasource.Sortable) {
                tableItems = new SortableDatasourceTableItems((CollectionDatasource.Sortable) datasource);
            } else {
                tableItems = new DatasourceTableItems(datasource);
            }
            setItems(tableItems);
        }
    }

    /**
     * @return collection datasource
     * @deprecated Use {@link #getItems()} instead
     */
    @Deprecated
    default CollectionDatasource getDatasource() {
        TableItems<E> tableItems = getItems();
        return tableItems instanceof DatasourceTableItems
                ? ((DatasourceTableItems) tableItems).getDatasource()
                : null;
    }

    class Column<T extends JmixEntity> extends io.jmix.ui.component.Table.Column {

        public Column(Object id) {
            super(id);
        }

        public Column(String id) {
            super(id);
        }

        public Column(MetaPropertyPath propertyPath, String caption) {
            super(propertyPath, caption);
        }

        public Column(String id, String caption) {
            super(id, caption);
        }

        @Deprecated
        public Column(Object id, String caption) {
            super((String) id, caption);
        }

        // todo provide Table instance method as replacement
        @Deprecated
        public Column(Class entityClass, String propertyPath) {
            super(
                    AppBeans.get(Metadata.class).getClass(entityClass).getPropertyPath(propertyPath),
                    AppBeans.get(MessageTools.class).getPropertyCaption(AppBeans.get(Metadata.class).getClass(entityClass), propertyPath)
            );
        }

        @Deprecated
        @SuppressWarnings("unchecked")
        public void setFormatter(Function formatter) {
            super.setFormatter(value -> (String) formatter.apply(value));
        }
    }
}
