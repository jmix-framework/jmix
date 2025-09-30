package io.jmix.searchopensearch.searching.strategy;

import io.jmix.search.searching.RequestContext;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import org.opensearch.client.opensearch.core.SearchRequest;

public interface OpenSearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {

    @Deprecated(since = "2.7", forRemoval = true)
    void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext);

    @Override
    default void configureRequest(RequestContext<SearchRequest.Builder> requestContext) {
        configureRequest(requestContext.getRequestBuilder(), requestContext.getSearchContext());
    }
}
