package io.jmix.searchelasticsearch.searching.strategy.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import org.springframework.stereotype.Component;

/**
 * Describes {@link SearchStrategy} that searches documents with at least one field matches all input terms in any order.
 * Fields with partial match a not suitable.
 */
@Component("search_AllTermsSingleFieldElasticsearchSearchStrategy")
public class AllTermsSingleFieldElasticsearchSearchStrategy extends AbstractSearchStrategy
        implements ElasticsearchSearchStrategy {

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
