/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Interface of views that display an entity instance without editing it.
 *
 * @param <E> type of entity
 */
@NullMarked
public interface ReadView<E> {

    /**
     * Sets the entity instance to show. The view reloads the instance by its id, so what is displayed
     * always matches the view's fetch plan.
     *
     * @param entity entity instance to show
     */
    void setEntityToRead(E entity);

    /**
     * Returns the currently shown entity instance.
     *
     * @return currently shown entity instance
     * @throws IllegalStateException if the entity isn't loaded yet, for example in {@link View.InitEvent}
     */
    default E getEntity() {
        E entity = getEntityOrNull();
        if (entity == null) {
            throw new IllegalStateException("Entity isn't loaded yet");
        }

        return entity;
    }

    /**
     * Returns the currently shown entity instance or {@code null} if it isn't loaded yet.
     *
     * @return currently shown entity instance or {@code null} if it isn't loaded yet
     */
    @Nullable
    E getEntityOrNull();
}
