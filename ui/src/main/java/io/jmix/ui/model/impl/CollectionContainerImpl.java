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

package io.jmix.ui.model.impl;

import io.jmix.core.MetadataTools;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.entity.EmbeddableEntity;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.Instance;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.Sorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;

/**
 *
 */
public class CollectionContainerImpl<E extends Entity>
        extends InstanceContainerImpl<E> implements CollectionContainer<E> {

    private static final Logger log = LoggerFactory.getLogger(CollectionContainerImpl.class);

    protected List<E> collection = new ArrayList<>();

    protected Map<IndexKey, Integer> idMap = new HashMap<>();

    protected Sorter sorter;

    public CollectionContainerImpl(ApplicationContext applicationContext, MetaClass metaClass) {
        super(applicationContext, metaClass);
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.NAME, MetadataTools.class);
    }

    @Override
    public void setItem(@Nullable E item) {
        if (item != null) {
            int idx = getItemIndex(item.getId());
            if (idx == -1) {
                throw new IllegalArgumentException("CollectionContainer does not contain " + item);
            }
            E existingItem = collection.get(idx);
            super.setItem(existingItem);
        } else {
            super.setItem(null);
        }
    }

    @Override
    public List<E> getItems() {
        return Collections.unmodifiableList(collection);
    }

    @Override
    public List<E> getMutableItems() {
        return new ObservableList<>(collection, idMap,
                (changeType, changes) -> {
                    buildIdMap();
                    clearItemIfNotExists();
                    fireCollectionChanged(changeType, changes);
                },
                this::detachListener,
                this::attachListener
        );
    }

    @Override
    public void setItems(@Nullable Collection<E> entities) {
        detachListener(collection);
        collection.clear();
        if (entities != null) {
            collection.addAll(entities);
            attachListener(collection);
        }
        buildIdMap();
        clearItemIfNotExists();
        fireCollectionChanged(CollectionChangeType.REFRESH, Collections.emptyList());
    }

    @Nonnull
    @Override
    public E getItem(Object entityId) {
        E item = getItemOrNull(entityId);
        if (item == null)
            throw new IllegalArgumentException("Item with id='" + entityId + "' not found");
        return item;
    }

    @Nullable
    @Override
    public E getItemOrNull(Object entityId) {
        int idx = getItemIndex(entityId);
        return idx != -1 ? collection.get(idx) : null;
    }

    @Override
    public int getItemIndex(Object entityOrId) {
        checkNotNullArgument(entityOrId, "entity or id is null");
        IndexKey indexKey;
        if (entityOrId instanceof Entity && !(entityOrId instanceof EmbeddableEntity)) {
            // if an entity instance is passed instead of id, check if the entity is of valid class and extract id
            Entity entity = (Entity) entityOrId;
            if (!entityMetaClass.getJavaClass().isAssignableFrom(entity.getClass())) {
                throw new IllegalArgumentException("Invalid entity class: " + entity.getClass());
            }
            indexKey = IndexKey.ofEntity(entity);
        } else {
            indexKey = IndexKey.of(entityOrId);
        }
        Integer idx = idMap.get(indexKey);
        return idx != null ? idx : -1;
    }

    @Override
    public boolean containsItem(Object entityOrId) {
        return getItemIndex(entityOrId) > -1;
    }

    @Override
    public void replaceItem(E entity) {
        checkNotNullArgument(entity, "entity is null");

        Object id = entity.getId();
        int idx = getItemIndex(id);
        CollectionChangeType changeType;
        if (idx > -1) {
            E prev = collection.get(idx);
            detachListener(prev);
            if (prev == getItemOrNull()) {
                this.item = entity;
                fireItemChanged(prev);
            }
            collection.set(idx, entity);
            changeType = CollectionChangeType.SET_ITEM;
        } else {
            collection.add(entity);
            changeType = CollectionChangeType.ADD_ITEMS;
        }
        attachListener(entity);
        buildIdMap();
        fireCollectionChanged(changeType, Collections.singletonList(entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addCollectionChangeListener(Consumer<CollectionChangeEvent<E>> listener) {
        return events.subscribe(CollectionChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Sorter getSorter() {
        return sorter;
    }

    @Override
    public void setSorter(Sorter sorter) {
        this.sorter = sorter;
    }

    @Override
    public void unmute(UnmuteEventsMode mode) {
        this.listenersEnabled = true;

        if (mode ==  UnmuteEventsMode.FIRE_REFRESH_EVENT) {
            fireCollectionChanged(CollectionChangeType.REFRESH, Collections.emptyList());
        }
    }

    @Override
    public void itemPropertyChanged(Instance.PropertyChangeEvent e) {
        if (!listenersEnabled) {
            return;
        }
        // if id has been changed, put the entity to the content with the new id
        MetaProperty primaryKeyProperty = getMetadataTools().getPrimaryKeyProperty(e.getItem().getClass());
        if (primaryKeyProperty != null && e.getProperty().equals(primaryKeyProperty.getName())) {
            // we cannot remove the old entry because its hashCode is based on the entity instance but now the same
            // instance has different hashCode based on id
            @SuppressWarnings("unchecked")
            E entity = (E) e.getItem();
            idMap.put(IndexKey.ofEntity(entity), collection.indexOf(entity));
        }

        super.itemPropertyChanged(e);
    }

    protected void fireCollectionChanged(CollectionChangeType type, Collection<? extends E> changes) {
        if (!listenersEnabled) {
            return;
        }

        CollectionChangeEvent<E> collectionChangeEvent = new CollectionChangeEvent<>(this, type, changes);
        log.trace("collectionChanged: {}", collectionChangeEvent);
        events.publish(CollectionChangeEvent.class, collectionChangeEvent);
    }

    protected void attachListener(Collection<E> entities) {
        for (E entity : entities) {
            attachListener(entity);
        }
    }

    protected void detachListener(Collection<E> entities) {
        for (E entity : entities) {
            detachListener(entity);
        }
    }

    protected void buildIdMap() {
        idMap.clear();
        for (int i = 0; i < collection.size(); i++) {
            idMap.put(IndexKey.ofEntity(collection.get(i)), i);
        }
    }

    protected void clearItemIfNotExists() {
        if (item != null) {
            int idx = getItemIndex(item.getId());
            if (idx == -1) {
                // item doesn't exist in the collection
                E prevItem = item;
                detachListener(prevItem);
                item = null;
                fireItemChanged(prevItem);
            } else {
                E newItem = collection.get(idx);
                if (newItem != item) {
                    E prevItem = item;
                    detachListener(prevItem);
                    item = newItem;
                    fireItemChanged(prevItem);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "CollectionContainerImpl{" +
                "entity=" + entityMetaClass +
                ", view=" + view +
                ", size=" + collection.size() +
                '}';
    }
}