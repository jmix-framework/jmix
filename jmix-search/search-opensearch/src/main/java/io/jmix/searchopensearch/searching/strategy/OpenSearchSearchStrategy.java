package io.jmix.searchopensearch.searching.strategy;

import io.jmix.search.searching.SearchRequestContext;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import org.opensearch.client.opensearch.core.SearchRequest;

/**
 * A OpenSearch-specific extension of the common {link @SearchStrategy} interface
 */
public interface OpenSearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {

    /**
     * Configures OpenSearch {@link SearchRequest}.
     * <p>The main step - create appropriate query based on provided {@link SearchContext} and set it to request.
     * <p>Configuration of another request parameters is optional.
     * <p>Highlighting, size and offset shouldn't be configured here - these parameters will be overwritten.
     *
     * @param requestBuilder allows to configure search request
     * @param searchContext  contains details about search being performed
     * @deprecated Because the new method with a more flexible signature is created.
     * Use {@link OpenSearchSearchStrategy#configureRequest(SearchRequestContext)}
     */
    @Deprecated(since = "2.7", forRemoval = true)
    void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext);

    @Override
    default void configureRequest(SearchRequestContext<SearchRequest.Builder> requestContext) {
        configureRequest(requestContext.getRequestBuilder(), requestContext.getSearchContext());
    }
}
