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

import io.jmix.core.metamodel.model.MetaClass;

/**
 * A Spring application event which is sent when the entity is loaded from the data store.
 *
 * @param <E> entity type
 */
public class EntityLoadingEvent<E> extends AbstractEntityEvent {

    private final E entity;

    public EntityLoadingEvent(Object source, MetaClass metaClass, E entity) {
        super(source, metaClass);
        this.entity = entity;
    }

    /**
     * Returns the loaded entity.
     */
    public E getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "EntityLoadingEvent{" +
                "entity=" + entity +
                '}';
    }
}
