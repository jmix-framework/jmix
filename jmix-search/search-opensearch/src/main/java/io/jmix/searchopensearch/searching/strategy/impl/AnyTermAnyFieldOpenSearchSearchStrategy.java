package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

/**
 * Describes {@link SearchStrategy} that searches documents with at least one field matches at least one input term.
 */
@Component("search_AnyTermAnyFieldOpenSearchSearchStrategy")
public class AnyTermAnyFieldOpenSearchSearchStrategy extends AbstractSearchStrategy implements OpenSearchSearchStrategy {

    @Override
    public String getName() {
        return "anyTermAnyField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        requestBuilder.query(queryBuilder ->
                queryBuilder.multiMatch(multiMatchQueryBuilder ->
                        multiMatchQueryBuilder.fields("*")
                                .query(searchContext.getSearchText())
                )
        );
    }
}
