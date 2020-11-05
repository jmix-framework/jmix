/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.gui.components.data.datagrid;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import io.jmix.core.Entity;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.component.data.TreeDataGridItems;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

public class DatasourceTreeDataGridItems<E extends Entity, K>
        extends SortableDatasourceDataGridItems<E, K>
        implements TreeDataGridItems<E> {

    @SuppressWarnings("unchecked")
    public DatasourceTreeDataGridItems(HierarchicalDatasource<E, K> datasource) {
        super((CollectionDatasource.Sortable<E, K>) datasource);
    }

    @SuppressWarnings("unchecked")
    protected HierarchicalDatasource<E, K> getHierarchicalDatasource() {
        return (HierarchicalDatasource<E, K>) datasource;
    }

    @Override
    public int getChildCount(E parent) {
        return Math.toIntExact(getChildren(parent).count());
    }

    @Override
    public Stream<E> getChildren(E item) {
        Collection<K> itemIds = item == null
                ? getHierarchicalDatasource().getRootItemIds()
                : getHierarchicalDatasource().getChildren((K) EntityValues.getId(item));

        return itemIds.stream()
                .map(id -> datasource.getItem(id));
    }

    @Override
    public boolean hasChildren(E item) {
        return getHierarchicalDatasource().hasChildren((K) EntityValues.getId(item));
    }

    @Nullable
    @Override
    public E getParent(E item) {
        Preconditions.checkNotNullArgument(item);
        K parentId = getHierarchicalDatasource().getParent((K) EntityValues.getId(item));
        return getHierarchicalDatasource().getItem(parentId);
    }

    @Override
    public String getHierarchyPropertyName() {
        return getHierarchicalDatasource().getHierarchyPropertyName();
    }
}
