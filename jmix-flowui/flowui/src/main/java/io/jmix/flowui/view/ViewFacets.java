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

package io.jmix.flowui.view;

import io.jmix.flowui.facet.Facet;
import org.springframework.lang.Nullable;

import java.util.stream.Stream;

/**
 * Interface for managing facets associated with a {@link View}.
 */
public interface ViewFacets {

    /**
     * Adds a facet to a view.
     *
     * @param facet the facet to be added
     */
    void addFacet(Facet facet);

    /**
     * Returns a facet by its ID.
     *
     * @param id the identifier of the facet to retrieve
     * @return the facet corresponding to the given identifier,
     * or {@code null} if no facet is associated with the identifier
     */
    @Nullable
    Facet getFacet(String id);

    /**
     * Removes the specified facet from the view.
     *
     * @param facet the facet to be removed
     */
    void removeFacet(Facet facet);

    /**
     * Returns a stream of all facets associated with the view.
     *
     * @return a stream of {@link Facet} instances associated with the view
     */
    Stream<Facet> getFacets();
}
