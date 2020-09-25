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

package io.jmix.ui.component.data.table;

import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.event.sys.VoidSubscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.data.meta.EmptyDataUnit;
import io.jmix.ui.component.data.meta.EntityTableItems;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class EmptyTableItems<E> implements EntityTableItems<E>, TableItems.Sortable<E>, EmptyDataUnit {

    protected MetaClass metaClass;

    public EmptyTableItems(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Nullable
    @Override
    public E getSelectedItem() {
        return null;
    }

    @Override
    public void setSelectedItem(@Nullable E item) {
        // do nothing
    }

    @Override
    public Collection<?> getItemIds() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public E getItem(Object itemId) {
        return null;
    }

    @Nullable
    @Override
    public Object getItemValue(Object itemId, MetaPropertyPath propertyId) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean containsId(Object itemId) {
        return false;
    }

    @Override
    public Class<?> getType(Object propertyId) {
        MetaPropertyPath propertyPath = (MetaPropertyPath) propertyId;
        return propertyPath.getRangeJavaClass();
    }

    @Override
    public boolean supportsProperty(Object propertyId) {
        return false;
    }

    @Override
    public Collection<E> getItems() {
        return Collections.emptyList();
    }

    @Override
    public void updateItem(E item) {
        // do nothing
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<E>> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Subscription addItemSetChangeListener(Consumer<ItemSetChangeEvent<E>> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Subscription addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<E>> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Nullable
    @Override
    public MetaClass getEntityMetaClass() {
        return metaClass;
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Nullable
    @Override
    public Object nextItemId(@Nullable Object itemId) {
        return null;
    }

    @Nullable
    @Override
    public Object prevItemId(@Nullable Object itemId) {
        return null;
    }

    @Nullable
    @Override
    public Object firstItemId() {
        return null;
    }

    @Nullable
    @Override
    public Object lastItemId() {
        return null;
    }

    @Override
    public boolean isFirstId(@Nullable Object itemId) {
        return false;
    }

    @Override
    public boolean isLastId(@Nullable Object itemId) {
        return false;
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        // do nothing
    }

    @Override
    public void resetSortOrder() {
        // do nothing
    }
}
