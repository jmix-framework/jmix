package io.jmix.searchelasticsearch.searching.strategy;

import io.jmix.search.SearchProperties;
import io.jmix.search.searching.SearchStrategyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides Elasticsearch search strategies
 */
public class ElasticsearchSearchStrategyProvider implements SearchStrategyProvider<ElasticsearchSearchStrategy> {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSearchStrategyProvider.class);

    protected final Map<String, ElasticsearchSearchStrategy> registry;

    protected final String defaultStrategyName;

    public ElasticsearchSearchStrategyProvider(Collection<ElasticsearchSearchStrategy> searchStrategies,
                                               SearchProperties applicationProperties) {
        log.debug("Available search strategies: {}", searchStrategies);
        Map<String, ElasticsearchSearchStrategy> tmpRegistry = new HashMap<>();
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
     * Gets {@link ElasticsearchSearchStrategy} by provided name. Throws exception if there is no strategy with such name.
     *
     * @param strategyName strategy name
     * @return {@link ElasticsearchSearchStrategy}
     * @throws IllegalStateException if strategy with provided name not found
     */
    public ElasticsearchSearchStrategy getSearchStrategyByName(String strategyName) {
        ElasticsearchSearchStrategy searchStrategy = findSearchStrategyByName(strategyName);
        if (searchStrategy == null) {
            throw new IllegalArgumentException(String.format("Search strategy with the name '%s' not found", strategyName));
        }

        return searchStrategy;
    }

    /**
     * Gets default search strategy.
     *
     * @return {@link ElasticsearchSearchStrategy}
     */
    public ElasticsearchSearchStrategy getDefaultSearchStrategy() {
        return getSearchStrategyByName(defaultStrategyName);
    }

    /**
     * Returns all registered search strategies.
     *
     * @return all {@link ElasticsearchSearchStrategy}
     */
    public Collection<ElasticsearchSearchStrategy> getAllSearchStrategies() {
        return registry.values();
    }

    /**
     * Returns a {@link ElasticsearchSearchStrategy} by provided name. Returns null if there is no strategy with such name.
     *
     * @param strategyName strategy name
     * @return {@link ElasticsearchSearchStrategy} or null if no strategy was found
     */
    @Nullable
    public ElasticsearchSearchStrategy findSearchStrategyByName(String strategyName) {
        return registry.get(strategyName.toLowerCase());
    }
}
