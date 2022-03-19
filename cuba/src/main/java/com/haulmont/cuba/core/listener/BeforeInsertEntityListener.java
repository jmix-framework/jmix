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
package com.haulmont.cuba.core.listener;

import com.haulmont.cuba.core.global.impl.EntityListenerUtils;
import io.jmix.core.Entity;
import com.haulmont.cuba.core.EntityManager;

/**
 * Defines the contract for handling entities before they have been inserted into DB.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.data.listener.BeforeInsertEntityListener}.
 */
@Deprecated
public interface BeforeInsertEntityListener<T extends Entity> extends io.jmix.data.listener.BeforeInsertEntityListener<T> {

    /**
     * Executes before the object has been inserted into DB.
     *
     * @param entity        inserted entity instance
     * @param entityManager EntityManager that owns the entity instance
     */
    void onBeforeInsert(T entity, EntityManager entityManager);

    @Override
    default void onBeforeInsert(T entity) {
        onBeforeInsert(entity, EntityListenerUtils.getCurrentEntityManager(entity));
    }
}
