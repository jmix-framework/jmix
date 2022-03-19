/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.searching;

import io.jmix.core.FetchPlan;

import java.util.Collection;
import java.util.Map;

/**
 * Provides additional functionality to work with {@link SearchResult}
 */
public interface SearchResultProcessor {

    /**
     * Loads all entity instances based on provided {@link SearchResult}.
     * Instances will be loaded using LOCAL fetch plan.
     *
     * @param searchResult {@link SearchResult}
     * @return collection of entity instances
     */
    Collection<Object> loadEntityInstances(SearchResult searchResult);

    /**
     * Loads all entity instances based on provided {@link SearchResult} and fetch plans.
     * Fetch plans can be provided for any entity.
     * Instances of entities that don't have provided fetch plans will be loaded using BASE fetch plan
     *
     * @param searchResult {@link SearchResult}
     * @param fetchPlans   Map: Entity name - {@link FetchPlan}
     * @return collection of entity instances
     */
    Collection<Object> loadEntityInstances(SearchResult searchResult, Map<String, FetchPlan> fetchPlans);
}
