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

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.impl.keyvalue.KeyValueMetaClassFactory;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.model.CollectionChangeType;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.model.KeyValueContainer;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.lang.Nullable;
import java.util.Collection;
import java.util.List;

public class KeyValueCollectionContainerImpl
        extends CollectionContainerImpl<KeyValueEntity> implements KeyValueCollectionContainer {

    @Autowired
    private KeyValueMetaClassFactory keyValueMetaClassFactory;

    private String idName;

    public KeyValueCollectionContainerImpl() {
        super(new KeyValueMetaClass());
    }

    @Override
    public KeyValueMetaClass getEntityMetaClass() {
        return (KeyValueMetaClass) super.getEntityMetaClass();
    }

    @Override
    public KeyValueContainer setIdName(String name) {
        idName = name;
        return this;
    }

    @Override
    public String getIdName() {
        return idName;
    }

    @Override
    public KeyValueContainer addProperty(String name) {
        keyValueMetaClassFactory.configurer(getEntityMetaClass()).addProperty(name, String.class);
        return this;
    }

    @Override
    public KeyValueContainer addProperty(String name, Class<?> aClass) {
        keyValueMetaClassFactory.configurer(getEntityMetaClass()).addProperty(name, aClass);
        return this;
    }

    @Override
    public KeyValueContainer addProperty(String name, Datatype<?> datatype) {
        keyValueMetaClassFactory.configurer(getEntityMetaClass()).addProperty(name, datatype);
        return this;
    }

    @Override
    public KeyValueEntity createEntity() {
        KeyValueEntity entity = new KeyValueEntity();
        entity.setIdName(idName);
        entity.setInstanceMetaClass(entityMetaClass);
        return entity;
    }

    @Override
    public void setItems(@Nullable Collection<KeyValueEntity> entities) {
        if (entities != null) {
            for (KeyValueEntity entity : entities) {
                updateEntityMetadata(entity);
            }
        }
        super.setItems(entities);
    }

    @Override
    public List<KeyValueEntity> getMutableItems() {
        return new ObservableList<>(collection, idMap, (changeType, changes) -> {
            buildIdMap();
            clearItemIfNotExists();
            if (changeType == CollectionChangeType.ADD_ITEMS || changeType == CollectionChangeType.SET_ITEM) {
                for (KeyValueEntity entity : changes) {
                    updateEntityMetadata(entity);
                }
            } else if (changeType == CollectionChangeType.REFRESH) {
                for (KeyValueEntity entity : collection) {
                    updateEntityMetadata(entity);
                }
            }
            fireCollectionChanged(changeType, changes);
        });
    }

    protected void updateEntityMetadata(KeyValueEntity entity) {
        entity.setInstanceMetaClass(entityMetaClass);
        if (idName != null)
            entity.setIdName(idName);
    }

    @Override
    public String toString() {
        return "KeyValueCollectionContainerImpl{" +
                "size=" + collection.size() +
                '}';
    }
}
