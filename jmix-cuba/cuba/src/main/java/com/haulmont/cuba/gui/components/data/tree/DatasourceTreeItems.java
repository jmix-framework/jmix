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

package com.haulmont.cuba.gui.components.data.tree;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsHelper;
import io.jmix.core.Entity;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.meta.EntityTreeItems;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DatasourceTreeItems<E extends Entity, K> implements EntityTreeItems<E> {

    protected HierarchicalDatasource<E, K> datasource;
    protected EventHub events = new EventHub();

    protected BindingState state = BindingState.INACTIVE;

    public DatasourceTreeItems(HierarchicalDatasource<E, K> datasource) {
        if (!(datasource instanceof CollectionDatasource.Indexed)) {
            throw new IllegalArgumentException("Datasource must implement " +
                    "com.haulmont.cuba.gui.data.CollectionDatasource.Indexed");
        }

        this.datasource = datasource;

        this.datasource.addStateChangeListener(this::datasourceStateChanged);
        this.datasource.addItemPropertyChangeListener(this::datasourceItemPropertyChanged);
        this.datasource.addCollectionChangeListener(this::datasourceCollectionChanged);
        this.datasource.addItemChangeListener(this::datasourceItemChanged);

        CollectionDsHelper.autoRefreshInvalid(datasource, true);

        if (datasource.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        }
    }

    protected void datasourceItemChanged(Datasource.ItemChangeEvent<E> e) {
        events.publish(SelectedItemChangeEvent.class, new SelectedItemChangeEvent<>(this, e.getItem()));

    }

    protected void datasourceCollectionChanged(@SuppressWarnings("unused") CollectionDatasource.CollectionChangeEvent<E, K> e) {
        events.publish(ItemSetChangeEvent.class, new ItemSetChangeEvent<>(this));
    }

    @SuppressWarnings("unchecked")
    protected void datasourceItemPropertyChanged(Datasource.ItemPropertyChangeEvent<E> e) {
        events.publish(ValueChangeEvent.class, new ValueChangeEvent(this,
                e.getItem(), e.getProperty(), e.getPrevValue(), e.getValue()));
    }

    protected void datasourceStateChanged(Datasource.StateChangeEvent<E> e) {
        if (e.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }
    }

    public HierarchicalDatasource<E, K> getDatasource() {
        return datasource;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return datasource.getMetaClass();
    }

    @Override
    public BindingState getState() {
        return state;
    }

    public void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            events.publish(StateChangeEvent.class, new StateChangeEvent(this, state));
        }
    }

    @Override
    public Object getItemId(E item) {
        Preconditions.checkNotNullArgument(item);
        return EntityValues.getId(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getItem(@Nullable Object itemId) {
        return datasource.getItem((K) itemId);
    }

    @Override
    public Stream<E> getItems() {
        return datasource.getItems().stream();
    }

    @Override
    public boolean containsItem(E item) {
        return datasource.containsItem((K)EntityValues.getId(item));
    }

    @Override
    public int size() {
        return datasource.size();
    }

    @Override
    public E getSelectedItem() {
        return datasource.getItem();
    }

    @Override
    public void setSelectedItem(@Nullable E item) {
        datasource.setItem(item);
    }

    @Override
    public int getChildCount(E parent) {
        return Math.toIntExact(getChildren(parent).count());
    }

    @Override
    public Stream<E> getChildren(E item) {
        Collection<K> itemIds = item == null
                ? datasource.getRootItemIds()
                : datasource.getChildren((K) EntityValues.getId(item));

        return itemIds.stream()
                .map(id -> datasource.getItem(id));
    }

    @Override
    public boolean hasChildren(E item) {
        return datasource.hasChildren((K) EntityValues.getId(item));
    }

    @Nullable
    @Override
    public E getParent(E item) {
        Preconditions.checkNotNullArgument(item);
        K parentId = datasource.getParent((K) EntityValues.getId(item));
        return datasource.getItem(parentId);
    }

    @Override
    public String getHierarchyPropertyName() {
        return datasource.getHierarchyPropertyName();
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return events.subscribe(StateChangeEvent.class, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<E>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addItemSetChangeListener(Consumer<ItemSetChangeEvent<E>> listener) {
        return events.subscribe(ItemSetChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<E>> listener) {
        return events.subscribe(SelectedItemChangeEvent.class, (Consumer) listener);
    }
}
