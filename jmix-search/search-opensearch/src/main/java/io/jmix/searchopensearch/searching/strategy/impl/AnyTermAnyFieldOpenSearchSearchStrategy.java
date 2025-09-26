package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents
 * with at least one field matches at least one input word.
 */
@Component("search_AnyTermAnyFieldOpenSearchSearchStrategy")
public class AnyTermAnyFieldOpenSearchSearchStrategy extends AbstractOpenSearchStrategy implements OpenSearchSearchStrategy {

    protected AnyTermAnyFieldOpenSearchSearchStrategy(SearchFieldsResolver searchFieldsResolver, OpenSearchQueryConfigurator queryConfigurator) {
        super(searchFieldsResolver, queryConfigurator);
    }

    @Override
    public String getName() {
        return "anyTermAnyField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        queryConfigurator.configureRequest(
                requestBuilder,
                searchContext.getEntities(),
                searchFieldsResolver::resolveFields,
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(fields)
                                        .query(searchContext.getEscapedSearchText())
                                        .operator(Operator.Or)
                        )
        );
    }
}
