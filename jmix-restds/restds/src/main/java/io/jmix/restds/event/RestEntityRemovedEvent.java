/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.event;

import io.jmix.core.event.AbstractEntityEvent;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.lang.Nullable;

/**
 * A Spring application event which is sent after the entity is removed from the REST data store.
 *
 * @param <E> entity type
 */
public class RestEntityRemovedEvent<E> extends AbstractEntityEvent {

    private final E entity;

    public RestEntityRemovedEvent(Object source, MetaClass metaClass, E entity) {
        super(source, metaClass);
        this.entity = entity;
    }

    /**
     * Returns the removed entity with the state right before sending to the data store.
     */
    public E getEntity() {
        return entity;
    }
}
