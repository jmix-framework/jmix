package io.jmix.searchopensearch.searching.strategy;

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import org.opensearch.client.opensearch.core.SearchRequest;

public interface OpenSearchSearchStrategy extends SearchStrategy {

    /**
     * Configures OpenSearch {@link SearchRequest}.
     * <p>The main step - create appropriate query based on provided {@link SearchContext} and set it to request.
     * <p>Configuration of another request parameters is optional.
     * Custom highlighting can be configured here. If it wasn't - the default one will be used.
     * <p>Size and offset shouldn't be configured here - these parameters will be overwritten.
     *
     * @param requestBuilder allows to configure search request
     * @param searchContext  contains details about search being performed
     */
    void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext);
}
