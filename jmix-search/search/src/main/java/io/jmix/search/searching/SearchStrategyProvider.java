package io.jmix.search.searching;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface SearchStrategyProvider<T extends SearchStrategy> {

    T getSearchStrategyByName(String strategyName);

    Collection<T> getAllSearchStrategies();

    @Nullable
    T findSearchStrategyByName(String strategyName);

    T getDefaultSearchStrategy();
}
