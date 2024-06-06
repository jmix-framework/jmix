package io.jmix.searchopensearch.searching.strategy;

import io.jmix.search.SearchProperties;
import io.jmix.search.searching.SearchStrategyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides OpenSearch search strategies
 */
public class OpenSearchSearchStrategyProvider implements SearchStrategyProvider<OpenSearchSearchStrategy> {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchSearchStrategyProvider.class);

    protected final Map<String, OpenSearchSearchStrategy> registry;

    protected final String defaultStrategyName;

    public OpenSearchSearchStrategyProvider(Collection<OpenSearchSearchStrategy> searchStrategies,
                                            SearchProperties applicationProperties) {
        log.debug("Available search strategies: {}", searchStrategies);
        Map<String, OpenSearchSearchStrategy> tmpRegistry = new HashMap<>();
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

        this.registry = tmpRegistry;
        this.defaultStrategyName = defaultSearchStrategy;
    }

    /**
     * Gets {@link OpenSearchSearchStrategy} by provided name. Throws exception if there is no strategy with such name.
     *
     * @param strategyName strategy name
     * @return {@link OpenSearchSearchStrategy}
     * @throws IllegalStateException if strategy with provided name not found
     */
    public OpenSearchSearchStrategy getSearchStrategyByName(String strategyName) {
        OpenSearchSearchStrategy searchStrategy = findSearchStrategyByName(strategyName);
        if (searchStrategy == null) {
            throw new IllegalArgumentException(String.format("Search strategy with the name '%s' not found", strategyName));
        }

        return searchStrategy;
    }

    /**
     * Gets default search strategy.
     *
     * @return {@link OpenSearchSearchStrategy}
     */
    public OpenSearchSearchStrategy getDefaultSearchStrategy() {
        return getSearchStrategyByName(defaultStrategyName);
    }

    /**
     * Returns all registered search strategies.
     *
     * @return all {@link OpenSearchSearchStrategy}
     */
    public Collection<OpenSearchSearchStrategy> getAllSearchStrategies() {
        return registry.values();
    }

    /**
     * Returns a {@link OpenSearchSearchStrategy} by provided name. Returns null if there is no strategy with such name.
     *
     * @param strategyName strategy name
     * @return {@link OpenSearchSearchStrategy} or null if no strategy was found
     */
    @Nullable
    public OpenSearchSearchStrategy findSearchStrategyByName(String strategyName) {
        return registry.get(strategyName.toLowerCase());
    }
}
