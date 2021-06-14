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

import io.jmix.search.SearchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("search_SearchStrategyManager")
public class SearchStrategyManager {

    private static final Logger log = LoggerFactory.getLogger(SearchStrategyManager.class);

    protected final Map<String, SearchStrategy> registry;

    protected final String defaultStrategyName;

    @Autowired
    public SearchStrategyManager(Collection<SearchStrategy> searchStrategies,
                                 SearchProperties applicationProperties) {
        log.debug("Available search strategies: {}", searchStrategies);
        Map<String, SearchStrategy> tmpRegistry = new HashMap<>();
        searchStrategies.forEach(searchStrategy -> {
            String strategyName = searchStrategy.getName().toLowerCase();
            if (tmpRegistry.containsKey(strategyName)) {
                throw new IllegalStateException(
                        String.format("Detected several search strategies with the same name '%s'", searchStrategy.getName())
                );
            }
            tmpRegistry.put(strategyName, searchStrategy);
        });

        String defaultSearchStrategy = applicationProperties.getDefaultSearchStrategy();
        if (!tmpRegistry.containsKey(defaultSearchStrategy.toLowerCase())) {
            throw new IllegalStateException(
                    String.format("Search strategy with the name '%s' defined as default not found", defaultSearchStrategy)
            );
        }

        registry = tmpRegistry;
        defaultStrategyName = defaultSearchStrategy;
    }

    /**
     * Gets {@link SearchStrategy} by provided name. Throws exception if there is no strategy with such name.
     *
     * @param strategyName strategy name
     * @return {@link SearchStrategy}
     * @throws IllegalStateException if strategy with provided name not found
     */
    public SearchStrategy getSearchStrategyByName(String strategyName) {
        SearchStrategy searchStrategy = findSearchStrategyByName(strategyName);
        if (searchStrategy == null) {
            throw new IllegalArgumentException(String.format("Search strategy with the name '%s' not found", strategyName));
        }

        return searchStrategy;
    }

    /**
     * Gets default search strategy.
     *
     * @return {@link SearchStrategy}
     */
    public SearchStrategy getDefaultSearchStrategy() {
        return getSearchStrategyByName(defaultStrategyName);
    }

    /**
     * Returns all registered search strategies.
     */
    public Collection<SearchStrategy> getAllSearchStrategies() {
        return registry.values();
    }

    /**
     * Returns a {@link SearchStrategy} by provided name. Returns null if there is no strategy with such name.
     *
     * @param strategyName strategy name
     * @return {@link SearchStrategy} or null if no strategy was found
     */
    @Nullable
    public SearchStrategy findSearchStrategyByName(String strategyName) {
        return registry.get(strategyName.toLowerCase());
    }
}
