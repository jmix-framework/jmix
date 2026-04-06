/*
 * Copyright 2025 Haulmont.
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

import org.jspecify.annotations.Nullable;

/**
 * The interface of views that display and may change the instance of an entity.
 *
 * @param <E> type of entity
 */
public interface HasEditedEntity<E> {

    /**
     * Returns the currently edited entity instance.
     *
     * @return currently edited entity instance
     * @throws IllegalStateException if the edited entity isn't initialized yet, for example in {@link View.InitEvent}
     */
    default E getEditedEntity() {
        E item = getEditedEntityOrNull();
        if (item == null) {
            throw new IllegalStateException("Edited entity isn't initialized yet");
        }

        return item;
    }

    /**
     * Returns the currently edited entity instance or {@code null} if not set.
     *
     * @return currently edited entity instance or {@code null} if not set
     */
    @Nullable
    E getEditedEntityOrNull();
}
