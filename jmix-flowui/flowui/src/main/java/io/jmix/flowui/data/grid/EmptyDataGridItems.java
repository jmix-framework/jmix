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

package io.jmix.flowui.data.grid;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.EmptyDataUnit;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EmptyDataGridItems<T> extends AbstractDataProvider<T, Void>
        implements EntityDataGridItems<T>, DataGridItems.Sortable<T>, EmptyDataUnit {

    private MetaClass metaClass;

    public EmptyDataGridItems(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public int size(Query<T, Void> query) {
        return 0;
    }

    @Override
    public Stream<T> fetch(Query<T, Void> query) {
        return Stream.empty();
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return () -> {};
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return metaClass;
    }

    @Override
    public Class<T> getType() {
        return metaClass.getJavaClass();
    }

    @Nullable
    @Override
    public T getSelectedItem() {
        return null;
    }

    @Override
    public void setSelectedItem(@Nullable T item) {
        // do nothing
    }

    @Override
    public Registration addValueChangeListener(Consumer<ValueChangeEvent<T>> listener) {
        return () -> {};
    }

    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<T>> listener) {
        return () -> {};
    }

    @Override
    public Registration addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<T>> listener) {
        return () -> {};
    }

    @Override
    public boolean containsItem(T item) {
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
