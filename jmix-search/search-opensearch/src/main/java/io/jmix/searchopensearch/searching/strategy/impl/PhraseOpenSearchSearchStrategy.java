package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchRequestContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchQueryConfigurer;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents with at least one field
 * matches the entire phrase - all input words in provided order.
 */
@Component("search_PhraseOpenSearchSearchStrategy")
public class PhraseOpenSearchSearchStrategy extends AbstractOpenSearchStrategy{

    public PhraseOpenSearchSearchStrategy(OpenSearchQueryConfigurer queryConfigurator) {
        super(queryConfigurator);
    }

    @Override
    public String getName() {
        return "phrase";
    }

    @Override
    public void configureRequest(SearchRequestContext<SearchRequest.Builder> requestContext) {
        queryConfigurer.configureRequest(
                requestContext,
                (queryBuilder, scope) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(scope.getFieldList())
                                        .query(requestContext.getSearchContext().getEscapedSearchText())
                                        .type(TextQueryType.Phrase)
                        )
        );
    }
}
