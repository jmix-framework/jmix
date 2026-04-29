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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Creates sorters for collection containers.
 * <p>
 * The manager asks {@link CollectionContainerSortProvider} beans in Spring order and uses the first non-null sorter.
 * If no provider supports the context, it delegates to {@link SorterFactory}.
 */
@Component("flowui_CollectionContainerSortManager")
public class CollectionContainerSortManager {

    protected SorterFactory sorterFactory;
    protected ObjectProvider<CollectionContainerSortProvider> sortProviders;

    /**
     * Creates a collection container sort manager.
     *
     * @param sorterFactory fallback factory used if no provider returns a sorter
     * @param sortProviders provider beans that can supply sorters
     */
    public CollectionContainerSortManager(SorterFactory sorterFactory,
                                          ObjectProvider<CollectionContainerSortProvider> sortProviders) {
        this.sorterFactory = sorterFactory;
        this.sortProviders = sortProviders;
    }

    /**
     * Creates a sorter for the specified collection container.
     *
     * @param container collection container for which a sorter is created
     * @return sorter supplied by a provider, or a fallback sorter created by {@link SorterFactory}
     */
    public Sorter createCollectionContainerSorter(CollectionContainer<?> container) {
        return createCollectionContainerSorterInternal(container, null);
    }

    /**
     * Creates a sorter for the specified collection container and loader.
     *
     * @param container collection container for which a sorter is created
     * @param loader    collection loader associated with the container, or {@code null} if there is no loader
     * @return sorter supplied by a provider, or a fallback sorter created by {@link SorterFactory}
     */
    public Sorter createCollectionContainerSorter(CollectionContainer<?> container,
                                                  @Nullable BaseCollectionLoader loader) {
        return createCollectionContainerSorterInternal(container, loader);
    }

    /**
     * Creates a sorter using providers or a fallback factory.
     *
     * @param container collection container for which a sorter is created
     * @param loader    collection loader associated with the container, or {@code null} if there is no loader
     * @return sorter supplied by a provider, or a fallback sorter created by {@link SorterFactory}
     */
    protected Sorter createCollectionContainerSorterInternal(CollectionContainer<?> container,
                                                             @Nullable BaseCollectionLoader loader) {
        CollectionContainerSortContext context = new CollectionContainerSortContext(container, loader);
        return sortProviders.orderedStream()
                .map(provider -> provider.getSorter(context))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> createFallbackSorter(container, loader));
    }

    protected Sorter createFallbackSorter(CollectionContainer<?> container,
                                          @Nullable BaseCollectionLoader loader) {
        return container instanceof CollectionPropertyContainer<?> propertyContainer
                ? sorterFactory.createCollectionPropertyContainerSorter(propertyContainer)
                : sorterFactory.createCollectionContainerSorter(container, loader);
    }
}
