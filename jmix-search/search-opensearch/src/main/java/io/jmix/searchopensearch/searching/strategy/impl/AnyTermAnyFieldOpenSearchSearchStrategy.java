package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.RequestContext;
import io.jmix.search.searching.SearchStrategy;
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

    protected AnyTermAnyFieldOpenSearchSearchStrategy(OpenSearchQueryConfigurator queryConfigurator) {
        super(queryConfigurator);
    }

    @Override
    public String getName() {
        return "anyTermAnyField";
    }

    @Override
    public void configureRequest(RequestContext<SearchRequest.Builder> requestContext) {
        queryConfigurator.configureRequest(
                requestContext,
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(fields)
                                        .query(requestContext.getSearchContext().getEscapedSearchText())
                                        .operator(Operator.Or)
                        )
        );
    }
}
