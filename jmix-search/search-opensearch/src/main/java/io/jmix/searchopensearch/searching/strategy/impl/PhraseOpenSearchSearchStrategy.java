package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.NoAllowedEntitiesForSearching;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents with at least one field
 * matches the entire phrase - all input words in provided order.
 */
@Component("search_PhraseOpenSearchSearchStrategy")
public class PhraseOpenSearchSearchStrategy extends AbstractOpenSearchStrategy implements OpenSearchSearchStrategy {

    protected PhraseOpenSearchSearchStrategy(SearchFieldsResolver searchFieldsResolver, OpenSearchQueryConfigurator queryConfigurator) {
        super(searchFieldsResolver, queryConfigurator);
    }

    @Override
    public String getName() {
        return "phrase";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) throws NoAllowedEntitiesForSearching {
        queryConfigurator.configureRequest(
                requestBuilder,
                searchContext.getEntities(),
                searchFieldsResolver::resolveFields,
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(fields)
                                        .query(searchContext.getEscapedSearchText())
                                        .type(TextQueryType.Phrase)
                        )
        );
    }
}
