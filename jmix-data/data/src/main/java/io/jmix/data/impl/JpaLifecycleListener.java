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

import io.jmix.core.event.AttributeChanges;
import io.jmix.core.security.EntityOp;

import org.springframework.lang.Nullable;

/**
 * Beans implementing this interface are notified by JPA implementation on their
 * specific events.
 */
public interface JpaLifecycleListener {

    /**
     * Invoked before entity commit.
     *
     * @param entity  entity
     * @param type    entity change type
     * @param changes object describing changes in the entity attributes - null for {@code CREATE} and {@code DELETE}
     *                change types
     */
    default void onEntityChange(Object entity, EntityOp type, @Nullable AttributeChanges changes) {
    }

    /**
     * Invoked before committing to data store.
     *
     * @param storeName name of data store
     */
    default void onFlush(String storeName) {
    }
}
