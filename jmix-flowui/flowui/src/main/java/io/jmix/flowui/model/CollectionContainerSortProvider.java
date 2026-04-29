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

import org.jspecify.annotations.Nullable;

/**
 * Provides sorting functionality for {@link CollectionContainer}s based on a specific context.
 * This interface is used to obtain a {@link Sorter} instance that can be applied to
 * a {@link CollectionContainer} according to the information provided in the {@link CollectionContainerSortContext}.
 */
public interface CollectionContainerSortProvider {

    /**
     * Returns a sorter for the specified context.
     * <p>
     * Implementations should return {@code null} if the context is not supported.
     *
     * @param context context containing the target container and optional loader
     * @return sorter for the context, or {@code null} if this provider does not support it
     */
    @Nullable
    Sorter getSorter(CollectionContainerSortContext context);
}
