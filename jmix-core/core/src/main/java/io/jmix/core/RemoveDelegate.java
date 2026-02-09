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

package io.jmix.core;

import io.jmix.core.annotation.Experimental;

/**
 * Interface to be implemented by custom update services.
 * The {@link RemoveDelegate#remove} method is called by generic framework mechanisms instead of {@code DataManager}
 * when removing entities of type {@code E}.
 *
 * @param <E> entity type
 */
@Experimental
public interface RemoveDelegate<E> {

    /**
     * Called by generic framework mechanisms instead of {@code DataManager} when removing entities of type {@code E}.
     *
     * @param entity entity to remove
     */
    void remove(E entity);
}
