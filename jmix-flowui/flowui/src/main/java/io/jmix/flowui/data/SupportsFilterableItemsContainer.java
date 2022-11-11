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

import com.vaadin.flow.function.SerializableBiPredicate;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.model.CollectionContainer;

public interface SupportsFilterableItemsContainer<E> extends SupportsItemsContainer<E> {

    /**
     * Sets items from the passed {@link CollectionContainer}.
     *
     * @param container  a {@link CollectionContainer} to be used as items source
     * @param itemFilter a filter to check if an item is shown when user typed
     *                   some text into the field
     * @see ContainerDataProvider
     */
    void setItems(CollectionContainer<E> container, SerializableBiPredicate<E, String> itemFilter);
}
