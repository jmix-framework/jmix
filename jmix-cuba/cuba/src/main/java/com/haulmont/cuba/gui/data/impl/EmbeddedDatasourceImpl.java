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
package com.haulmont.cuba.gui.data.impl;

import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.EmbeddedDatasource;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanProperty;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.annotation.Nullable;
import java.util.*;

public class EmbeddedDatasourceImpl<T extends Entity>
        extends AbstractDatasource<T>
        implements Datasource<T>, DatasourceImplementation<T>, EmbeddedDatasource<T> {

    protected Datasource masterDs;
    protected MetaProperty metaProperty;

    @Override
    public void setup(String id, Datasource masterDs, String property) {
        this.id = id;
        this.masterDs = masterDs;
        metaProperty = masterDs.getMetaClass().findProperty(property);
        initParentDsListeners();
    }

    protected void initParentDsListeners() {
        //noinspection unchecked
        masterDs.addItemChangeListener(e -> {
            Entity prevValue = getItem(e.getPrevItem());
            Entity newValue = getItem(e.getItem());
            reattachListeners(prevValue, newValue);

            fireItemChanged((T) prevValue);
        });

        //noinspection unchecked
        masterDs.addStateChangeListener(e -> fireStateChanged(e.getPrevState()));

        //noinspection unchecked
        masterDs.addItemPropertyChangeListener(e -> {
            if (e.getProperty().equals(metaProperty.getName()) && !Objects.equals(e.getPrevValue(), e.getValue())) {
                reattachListeners((Entity) e.getPrevValue(), (Entity) e.getValue());
                fireItemChanged((T) e.getPrevValue());
            }
        });
    }

    protected void reattachListeners(Entity prevItem, Entity item) {
        if (prevItem != item) {
            detachListener(prevItem);
            attachListener(item);
        }
    }

    @Override
    public DsContext getDsContext() {
        return masterDs.getDsContext();
    }

    @Override
    public DataSupplier getDataSupplier() {
        return masterDs.getDataSupplier();
    }

    @Override
    public void commit() {
        if (!allowCommit) {
            return;
        }

        clearCommitLists();
        modified = false;
    }

    @Override
    public State getState() {
        return masterDs.getState();
    }

    @Override
    public T getItem() {
        backgroundWorker.checkUIAccess();

        final Entity item = masterDs.getItem();
        return getItem(item);
    }

    @Nullable
    @Override
    public T getItemIfValid() {
        backgroundWorker.checkUIAccess();

        return getState() == State.VALID ? getItem() : null;
    }

    protected T getItem(Entity item) {
        return item == null ? null : (T) EntityValues.getValue(item, metaProperty.getName());
    }

    @Override
    public void setItem(T item) {
        backgroundWorker.checkUIAccess();

        if (getItem() != null) {
            metadata.getTools().copy(item, getItem());
            itemsToUpdate.add(item);
        } else {
            final Entity parentItem = masterDs.getItem();
            EntityValues.setValue(parentItem, metaProperty.getName(), item);
        }
        setModified(true);
        ((DatasourceImplementation) masterDs).modified(masterDs.getItem());
    }

    @Override
    public MetaClass getMetaClass() {
        MetaClass metaClass = metaProperty.getRange().asClass();
        return metadata.getExtendedEntities().getEffectiveMetaClass(metaClass);
    }

    @Override
    public FetchPlan getView() {
        final FetchPlanProperty property = masterDs.getView().getProperty(metaProperty.getName());
        return property == null ? null : metadata.getViewRepository().getView(getMetaClass(), property.getFetchPlan().getName());
    }

    @Override
    public void committed(Set<Entity> entities) {
        Entity item = masterDs.getItem();

        Entity newItem = null;
        Entity previousItem = null;

        if (item != null) {
            Iterator<Entity> commitIter = entities.iterator();
            while (commitIter.hasNext() && (previousItem == null) && (newItem == null)) {
                Entity commitItem = commitIter.next();
                if (commitItem.equals(item)) {
                    previousItem = item;
                    newItem = commitItem;
                }
            }
            if (previousItem != null) {
                detachListener(getItem(previousItem));
            }
            if (newItem != null) {
                attachListener(getItem(newItem));
            }
        }

        modified = false;
        clearCommitLists();
    }

    @Override
    public Datasource getMaster() {
        return masterDs;
    }

    @Override
    public MetaProperty getProperty() {
        return metaProperty;
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void initialized() {
    }

    @Override
    public void valid() {
    }

    @Override
    public void modified(T item) {
        super.modified(item);

        if (masterDs != null) {
            ((AbstractDatasource) masterDs).modified(masterDs.getItem());
        }
    }

    @Override
    public Collection<T> getItemsToCreate() {
        return Collections.emptyList();
    }

    @Override
    public Collection<T> getItemsToUpdate() {
        return Collections.emptyList();
    }

    @Override
    public Collection<T> getItemsToDelete() {
        return Collections.emptyList();
    }
}
