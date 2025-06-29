/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.data;

import io.jmix.flowui.model.CollectionContainer;

/**
 * Represents a data unit connected to a {@link CollectionContainer}.
 *
 * @param <E> the type of entity contained in the {@link CollectionContainer}
 */
public interface ContainerDataUnit<E> extends EntityDataUnit {

    /**
     * Returns the {@link CollectionContainer} associated with this data unit.
     *
     * @return the container that holds a collection of entity instances
     */
    CollectionContainer<E> getContainer();
}
