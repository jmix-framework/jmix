package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

/**
 * Describes {@link SearchStrategy} that searches documents with fields match all input terms in any order.
 * Different terms can be present in different fields.
 */
@Component("search_AllTermsAnyFieldOpenSearchSearchStrategy")
public class AllTermsAnyFieldOpenSearchSearchStrategy extends AbstractSearchStrategy
        implements OpenSearchSearchStrategy {

    @Override
    public String getName() {
        return "allTermsAnyField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        requestBuilder.query(queryBuilder ->
                queryBuilder.simpleQueryString(simpleQueryStringQueryBuilder ->
                        simpleQueryStringQueryBuilder.fields("*")
                                .query(searchContext.getSearchText())
                                .defaultOperator(Operator.And)
                )
        );
    }
}
