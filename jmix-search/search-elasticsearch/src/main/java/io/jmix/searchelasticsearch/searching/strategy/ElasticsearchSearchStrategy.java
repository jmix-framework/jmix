package io.jmix.searchelasticsearch.searching.strategy;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchRequestContext;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;

/**
 * A Elasticsearch-specific extension of the common {link @SearchStrategy} interface
 */
public interface ElasticsearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {

    /**
     * Configures Elasticsearch {@link SearchRequest}.
     * <p>The main step - create appropriate query based on provided {@link SearchContext} and set it to request.
     * <p>Configuration of another request parameters is optional.
     * <p>Highlighting, size and offset shouldn't be configured here - these parameters will be overwritten.
     *
     * @param requestBuilder allows to configure search request
     * @param searchContext  contains details about search being performed
     * @deprecated TODO Pavel Aleksandrov
     * Use {@link ElasticsearchSearchStrategy#configureRequest(SearchRequestContext)}
     */
    @Deprecated(since = "2.7", forRemoval = true)
    void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext);

    @Override
    default void configureRequest(SearchRequestContext<SearchRequest.Builder> requestContext) {
        configureRequest(requestContext.getRequestBuilder(), requestContext.getSearchContext());
    }
}
