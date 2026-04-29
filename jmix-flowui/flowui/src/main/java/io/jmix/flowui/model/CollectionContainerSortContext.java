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

package io.jmix.flowui.model;

import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.Nullable;

/**
 * Contains information for creating a sorter for a {@link CollectionContainer}.
 * <p>
 * Providers that need to distinguish collection property containers can check whether {@link #container()} is an
 * instance of {@link CollectionPropertyContainer}.
 *
 * @param container collection container for which a sorter is requested
 * @param loader    collection loader associated with the container, or {@code null} if the container has no loader
 */
public record CollectionContainerSortContext(CollectionContainer<?> container,
                                             @Nullable BaseCollectionLoader loader) {

    /**
     * Creates a context for a collection container sorter request.
     *
     * @param container collection container for which a sorter is requested
     * @param loader    collection loader associated with the container, or {@code null} if the container has no loader
     */
    public CollectionContainerSortContext {
        Preconditions.checkNotNullArgument(container);
    }
}
