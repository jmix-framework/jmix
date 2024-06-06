package io.jmix.searchelasticsearch.searching.strategy;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;

public interface ElasticsearchSearchStrategy extends SearchStrategy {

    /**
     * Configures Elasticsearch {@link SearchRequest}.
     * <p>The main step - create appropriate query based on provided {@link SearchContext} and set it to request.
     * <p>Configuration of another request parameters is optional.
     * <p>Highlighting, size and offset shouldn't be configured here - these parameters will be overwritten.
     *
     * @param requestBuilder allows to configure search request
     * @param searchContext  contains details about search being performed
     */
    void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext);
}
