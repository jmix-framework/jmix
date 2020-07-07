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

package io.jmix.ui.component.table;

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.widget.data.TableSortableContainer;

import javax.annotation.Nullable;
import java.util.Collection;

@SuppressWarnings("deprecation")
public class SortableDataContainer<I> extends TableDataContainer<I> implements Container.Sortable, TableSortableContainer {

    public SortableDataContainer(TableItems.Sortable<I> tableDataSource, TableItemsEventsDelegate<I> dataEventsDelegate) {
        super(tableDataSource, dataEventsDelegate);
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        getSortableTableSource().sort(propertyId, ascending);
    }

    protected TableItems.Sortable getSortableTableSource() {
        return (TableItems.Sortable) tableItems;
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        return properties;
    }

    @Nullable
    @Override
    public Object nextItemId(@Nullable Object itemId) {
        return getSortableTableSource().nextItemId(itemId);
    }

    @Nullable
    @Override
    public Object prevItemId(@Nullable Object itemId) {
        return getSortableTableSource().prevItemId(itemId);
    }

    @Nullable
    @Override
    public Object firstItemId() {
        return getSortableTableSource().firstItemId();
    }

    @Nullable
    @Override
    public Object lastItemId() {
        return getSortableTableSource().lastItemId();
    }

    @Override
    public boolean isFirstId(@Nullable Object itemId) {
        return getSortableTableSource().isFirstId(itemId);
    }

    @Override
    public boolean isLastId(@Nullable Object itemId) {
        return getSortableTableSource().isLastId(itemId);
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetSortOrder() {
        getSortableTableSource().resetSortOrder();
    }
}
