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

package io.jmix.data.impl;

import io.jmix.core.JmixEntity;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.data.EntityChangeType;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Interface for persistence lifecycle listeners notified by {@link PersistenceSupport} before commit.
 */
public interface OrmLifecycleListener {

    /**
     * Invoked before entity commit.
     *
     * @param entity  entity
     * @param type    entity change type
     * @param changes object describing changes in the entity attributes - null for {@code CREATE} and {@code DELETE}
     *                change types
     */
    default void onEntityChange(JmixEntity entity, EntityChangeType type, @Nullable EntityAttributeChanges changes) {
    }

    /**
     * Invoked before committing to data store.
     *
     * @param storeName name of data store
     */
    default void onFlush(String storeName) {
    }

    /**
     * Invoked when entities are loaded from ORM store
     */
    default void onLoad(Collection<JmixEntity> entities, LoadContext loadContext) {
    }

    /**
     * Invoked when entities are saved in ORM store
     */
    default void onSave(Collection<JmixEntity> entities, SaveContext saveContext) {
    }
}
