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

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.components.data.table.DatasourceTreeTableItems;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import io.jmix.core.Entity;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.data.TableItems;

import javax.annotation.Nullable;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.component.TreeTable} instead
 */
@Deprecated
@SuppressWarnings("rawtypes")
public interface TreeTable<E extends Entity> extends ListComponent<E>, io.jmix.ui.component.TreeTable<E>, Table<E>,
        HasSettings, HasDataLoadingSettings, HasPresentations, HasRowsCount, RowsCount.RowsCountTarget {

    static <T extends Entity> TypeToken<TreeTable<T>> of(@SuppressWarnings("unused") Class<T> itemClass) {
        return new TypeToken<TreeTable<T>>() {};
    }

    /**
     * @return hierarchical datasource
     */
    @Deprecated
    @Nullable
    default HierarchicalDatasource getDatasource() {
        TableItems<E> tableItems = getItems();
        if (tableItems == null) {
            return null;
        }

        if (tableItems instanceof DatasourceTreeTableItems) {
            DatasourceTreeTableItems adapter = (DatasourceTreeTableItems) tableItems;
            return (HierarchicalDatasource) adapter.getDatasource();
        }

        return null;
    }

    /**
     * Sets {@code CollectionDatasource} as TreeTable data source.
     *
     * @param datasource collection datasource
     * @deprecated Use {@link #setItems(TableItems)} instead
     */
    @Deprecated
    default void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setItems(null);
        } else {
            if (!(datasource instanceof HierarchicalDatasource)) {
                throw new IllegalArgumentException("TreeTable supports only HierarchicalDatasource");
            }

            setItems(new DatasourceTreeTableItems((HierarchicalDatasource) datasource));
        }
    }

    /**
     * Sets  {@code HierarchicalDatasource} as TreeTable data source.
     *
     * @param datasource hierarchical datasource
     */
    @Deprecated
    default void setDatasource(HierarchicalDatasource datasource) {
        setDatasource((CollectionDatasource) datasource);
    }
}
