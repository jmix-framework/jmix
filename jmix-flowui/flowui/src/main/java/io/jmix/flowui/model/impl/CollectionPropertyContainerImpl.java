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

package io.jmix.flowui.model.impl;

import io.jmix.core.entity.EntityPropertyChangeEvent;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * Implementation of the {@link CollectionPropertyContainer} interface that manages a collection of entities
 * as a property of a master entity.
 *
 * @param <E> the type of items in the collection
 */
public class CollectionPropertyContainerImpl<E>
        extends CollectionContainerImpl<E> implements CollectionPropertyContainer<E> {

    protected InstanceContainer<?> master;
    protected String property;

    public CollectionPropertyContainerImpl(MetaClass metaClass, InstanceContainer<?> master, String property) {
        super(metaClass);
        this.master = master;
        this.property = property;
    }

    @Override
    public InstanceContainer<?> getMaster() {
        return master;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public List<E> getDisconnectedItems() {
        return super.getMutableItems();
    }

    @Override
    public void setDisconnectedItems(@Nullable Collection<E> entities) {
        super.setItems(entities);
    }

    @Override
    public List<E> getMutableItems() {
        return new ObservableList<>(collection, idMap, (changeType, changes) -> {
            buildIdMap();
            clearItemIfNotExists();
            updateMaster();
            fireCollectionChanged(changeType, changes);
        });
    }

    @Override
    public void setItems(@Nullable Collection<E> entities) {
        super.setItems(entities);
        Object masterItem = master.getItemOrNull();
        if (masterItem != null) {
            MetaProperty masterProperty = getMasterProperty();
            Collection<?> masterCollection = EntityValues.getValue(masterItem, masterProperty.getName());
            if (masterCollection != entities) {
                updateMasterCollection(masterProperty, masterCollection, entities);
            }
        }
    }

    protected void updateMaster() {
        MetaProperty masterProperty = getMasterProperty();
        Collection<?> masterCollection = EntityValues.getValue(master.getItem(), masterProperty.getName());
        updateMasterCollection(masterProperty, masterCollection, this.collection);
    }

    protected MetaProperty getMasterProperty() {
        MetaClass masterMetaClass = master.getEntityMetaClass();
        MetaProperty masterProperty = masterMetaClass.getProperty(property);
        if (!masterProperty.getRange().getCardinality().isMany()) {
            throw new IllegalStateException(String.format("Property '%s' is not a collection", property));
        }
        return masterProperty;
    }

    @SuppressWarnings("unchecked")
    private void updateMasterCollection(MetaProperty metaProperty,
                                        @Nullable Collection masterCollection,
                                        @Nullable Collection<E> newCollection) {
        if (newCollection == null) {
            EntityValues.setValue(master.getItem(), metaProperty.getName(), null);
        } else {
            if (masterCollection == null) {
                initMasterCollection(metaProperty, newCollection);
            } else {
                masterCollection.clear();
                masterCollection.addAll(newCollection);
                if (master instanceof ItemPropertyChangeNotifier) {
                    EntityPropertyChangeEvent event = new EntityPropertyChangeEvent(
                            master.getItem(),
                            metaProperty.getName(),
                            masterCollection,
                            masterCollection
                    );
                    ((ItemPropertyChangeNotifier) master).itemPropertyChanged(event);
                }
            }
        }
    }

    protected Collection<E> initMasterCollection(MetaProperty metaProperty, Collection<E> newCollection) {
        Collection<E> masterCollection;
        if (Set.class.isAssignableFrom(metaProperty.getJavaType())) {
            masterCollection = new LinkedHashSet<>(newCollection);
        } else {
            masterCollection = new ArrayList<>(newCollection);
        }
        EntityValues.setValue(master.getItem(), metaProperty.getName(), masterCollection);
        return masterCollection;
    }

    @Override
    protected void replaceInCollection(int idx, E entity) {
        super.replaceInCollection(idx, entity);
        MetaProperty masterProperty = getMasterProperty();
        Collection<E> masterCollection = EntityValues.getValue(master.getItem(), masterProperty.getName());
        if (masterCollection == null) {
            masterCollection = initMasterCollection(masterProperty, Collections.emptyList());
            masterCollection.add(entity);
        } else {
            if (masterCollection instanceof List) {
                int masterCollectionIdx = ((List<E>) masterCollection).indexOf(entity);
                if (masterCollectionIdx >= 0) {
                    ((List<E>) masterCollection).set(masterCollectionIdx, entity);
                } else {
                    masterCollection.add(entity);
                }
            } else {
                masterCollection.remove(entity);
                masterCollection.add(entity);
            }
        }
    }

    @Override
    protected void addToCollection(E entity) {
        super.addToCollection(entity);
        MetaProperty masterProperty = getMasterProperty();
        Collection<E> masterCollection = EntityValues.getValue(master.getItem(), masterProperty.getName());
        if (masterCollection == null) {
            masterCollection = initMasterCollection(masterProperty, Collections.emptyList());
        }
        masterCollection.add(entity);
    }
}
