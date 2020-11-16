/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.event;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;

/**
 * A Spring application event which is sent when the entity is saved to the data store.
 *
 * @param <E> entity type
 */
public class EntitySavingEvent<E> extends AbstractEntityEvent {

    private final E entity;
    private final boolean isNewEntity;

    @Internal
    public EntitySavingEvent(Object source, MetaClass metaClass, E entity, boolean isNewEntity) {
        super(source, metaClass);
        Preconditions.checkNotNullArgument(entity, "entity is null");
        this.entity = entity;
        this.isNewEntity = isNewEntity;
    }

    /**
     * Returns the entity being saved.
     */
    public E getEntity() {
        return entity;
    }

    /**
     * True if the entity is new, i.e. it is being created in the data store.
     */
    public boolean isNewEntity() {
        return isNewEntity;
    }

    @Override
    public String toString() {
        return "EntitySavingEvent{" +
                "entity=" + entity +
                ", new=" + isNewEntity +
                '}';
    }
}
