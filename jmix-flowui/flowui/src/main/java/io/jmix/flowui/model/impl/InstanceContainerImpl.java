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

package io.jmix.flowui.model.impl;

import io.jmix.core.DevelopmentException;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.EntityPropertyChangeEvent;
import io.jmix.core.entity.EntityPropertyChangeListener;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.HasInstanceMetaClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.RequiresChanges;
import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 *
 */
@SameAsUi
@RequiresChanges
public class InstanceContainerImpl<E> implements InstanceContainer<E>, HasLoader, ItemPropertyChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(InstanceContainerImpl.class);

    @Autowired
    protected Metadata metadata;

    protected E item;
    protected MetaClass entityMetaClass;
    protected FetchPlan fetchPlan;

    protected EventHub events = new EventHub();
    protected EntityPropertyChangeListener listener = this::itemPropertyChanged;
    protected DataLoader loader;

    protected boolean listenersEnabled = true;

    public InstanceContainerImpl(MetaClass entityMetaClass) {
        this.entityMetaClass = entityMetaClass;
    }

    @Nullable
    @Override
    public E getItemOrNull() {
        return item;
    }

    @Override
    public E getItem() {
        E item = getItemOrNull();
        if (item == null) {
            throw new IllegalStateException("Current item is null");
        }
        return item;
    }

    @Override
    public void setItem(@Nullable E item) {
        E prevItem = this.item;

        if (this.item != null) {
            detachListener(this.item);
        }

        if (item != null) {
            MetaClass aClass = item instanceof HasInstanceMetaClass ?
                    ((HasInstanceMetaClass) item).getInstanceMetaClass() : metadata.getClass(item);
            if (!aClass.equals(entityMetaClass) && !entityMetaClass.getDescendants().contains(aClass)) {
                throw new DevelopmentException(String.format("Invalid item's metaClass '%s'", aClass),
                        ParamsMap.of("container", toString(), "metaClass", aClass));
            }
            detachListener(item);
            attachListener(item);
        }

        this.item = item;

        fireItemChanged(prevItem);
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return entityMetaClass;
    }

    @Nullable
    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addItemPropertyChangeListener(Consumer<ItemPropertyChangeEvent<E>> listener) {
        return events.subscribe(ItemPropertyChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addItemChangeListener(Consumer<ItemChangeEvent<E>> listener) {
        return events.subscribe(ItemChangeEvent.class, (Consumer) listener);
    }

    protected void attachListener(Object entity) {
        if (entity != null) {
            EntitySystemAccess.addPropertyChangeListener(entity, listener);
        }
    }

    protected void detachListener(Object entity) {
        if (entity != null) {
            EntitySystemAccess.removePropertyChangeListener(entity, listener);
        }
    }

    @Override
    public String toString() {
        return "InstanceContainerImpl{" +
                "entity=" + entityMetaClass +
                ", view=" + fetchPlan +
                '}';
    }

    @Nullable
    @Override
    public DataLoader getLoader() {
        return loader;
    }

    @Override
    public void setLoader(DataLoader loader) {
        this.loader = loader;
    }

    protected void fireItemChanged(@Nullable E prevItem) {
        if (!listenersEnabled) {
            return;
        }

        ItemChangeEvent<E> itemChangeEvent = new ItemChangeEvent<>(this, prevItem, getItemOrNull());
        log.trace("itemChanged: {}", itemChangeEvent);
        events.publish(ItemChangeEvent.class, itemChangeEvent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void itemPropertyChanged(EntityPropertyChangeEvent e) {
        if (!listenersEnabled) {
            return;
        }

        ItemPropertyChangeEvent<E> itemPropertyChangeEvent = new ItemPropertyChangeEvent<>(InstanceContainerImpl.this,
                (E) e.getItem(), e.getProperty(), e.getPrevValue(), e.getValue());

        log.trace("propertyChanged: {}", itemPropertyChangeEvent);

        events.publish(ItemPropertyChangeEvent.class, itemPropertyChangeEvent);
    }

    @Override
    public void mute() {
        this.listenersEnabled = false;
    }

    @Override
    public void unmute() {
        this.listenersEnabled = true;
    }
}
