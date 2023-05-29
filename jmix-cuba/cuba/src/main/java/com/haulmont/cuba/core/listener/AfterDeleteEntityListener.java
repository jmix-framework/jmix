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

import java.sql.Connection;

/**
 * Defines the contract for handling of entities after they have been deleted or marked as deleted in DB.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.data.listener.AfterDeleteEntityListener}.
 */
@Deprecated
public interface AfterDeleteEntityListener<T extends Entity> extends io.jmix.data.listener.AfterDeleteEntityListener<T> {

    /**
     * Executes after the object has been deleted or marked as deleted in DB.
     * <p>
     * Modification of the entity state or using {@code EntityManager} is impossible here. Use {@code connection} if you
     * need to make changes in the database.
     *
     * @param entity        deleted entity
     * @param connection    JDBC connection to the database of the deleted entity
     */
    void onAfterDelete(T entity, Connection connection);

    @Override
    default void onAfterDelete(T entity) {
        onAfterDelete(entity, EntityListenerUtils.getCurrentConnection(entity));
    }
}
