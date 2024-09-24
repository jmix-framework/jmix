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

/**
 * A Spring application event which is sent after the entity is saved to the REST data store.
 *
 * @param <E> entity type
 */
public class RestEntitySavedEvent<E> extends AbstractEntityEvent {

    private final E savedEntity;
    private final E returnedEntity;
    private final boolean isNewEntity;

    public RestEntitySavedEvent(Object source, MetaClass metaClass, E savedEntity, E returnedEntity, boolean isNewEntity) {
        super(source, metaClass);
        this.savedEntity = savedEntity;
        this.returnedEntity = returnedEntity;
        this.isNewEntity = isNewEntity;
    }

    /**
     * The saved entity instance with the state right before sending to the data store.
     */
    public E getSavedEntity() {
        return savedEntity;
    }

    /**
     * The entity instance returned from the data store after saving.
     */
    public E getReturnedEntity() {
        return returnedEntity;
    }

    public boolean isNewEntity() {
        return isNewEntity;
    }
}
