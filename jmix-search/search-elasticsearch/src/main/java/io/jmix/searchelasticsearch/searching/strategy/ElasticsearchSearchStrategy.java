package io.jmix.searchelasticsearch.searching.strategy;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.RequestContext;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;

public interface ElasticsearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {

    @Deprecated(since = "2.7", forRemoval = true)
    void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext);

    @Override
    default void configureRequest(RequestContext<SearchRequest.Builder> requestContext) {
        configureRequest(requestContext.getRequestBuilder(), requestContext.getSearchContext());
    }
}
