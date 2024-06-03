package io.jmix.searchelasticsearch.searching.strategy;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;

public interface ElasticsearchSearchStrategy extends SearchStrategy {

    /**
     * Provides the name of this search strategy.
     * Name should be unique among all search strategies in application.
     *
     * @return name
     */
    String getName();

    /**
     * Configures Elasticsearch {@link SearchRequest}.
     * <p>The main step - create appropriate query based on provided {@link SearchContext} and set it to request.
     * <p>Configuration of another request parameters is optional.
     * Custom highlighting can be configured here. If it wasn't - the default one will be used.
     * <p>Size and offset shouldn't be configured here - these parameters will be overwritten.
     *
     * @param searchRequest {@link SearchRequest}
     * @param searchContext {@link SearchContext}
     */
    void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext);
}
