package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchRequestContext;
import io.jmix.search.searching.SearchStrategy;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents
 * with at least one field matches at least one input word.
 */
@Component("search_AnyTermAnyFieldOpenSearchSearchStrategy")
public class AnyTermAnyFieldOpenSearchSearchStrategy extends AbstractOpenSearchStrategy{

    public AnyTermAnyFieldOpenSearchSearchStrategy(OpenSearchQueryConfigurer queryConfigurator) {
        super(queryConfigurator);
    }

    @Override
    public String getName() {
        return "anyTermAnyField";
    }

    @Override
    public void configureRequest(SearchRequestContext<SearchRequest.Builder> requestContext) {
        queryConfigurator.configureRequest(
                requestContext,
                (queryBuilder, scope) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(scope.getFieldList())
                                        .query(requestContext.getSearchContext().getEscapedSearchText())
                                        .operator(Operator.Or)
                        )
        );
    }
}
