package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

/**
 * Describes {@link SearchStrategy} that searches documents with at least one field matches all input terms in any order.
 * Fields with partial match a not suitable.
 */
@Component("search_AllTermsSingleFieldOpenSearchSearchStrategy")
public class AllTermsSingleFieldOpenSearchSearchStrategy extends AbstractSearchStrategy
        implements OpenSearchSearchStrategy {

    @Override
    public String getName() {
        return "allTermsSingleField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        requestBuilder.query(queryBuilder ->
                queryBuilder.multiMatch(multiMatchQueryBuilder ->
                        multiMatchQueryBuilder.fields("*")
                                .query(searchContext.getSearchText())
                                .operator(Operator.And)
                )
        );
    }
}
