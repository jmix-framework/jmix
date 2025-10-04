package io.jmix.searchelasticsearch.searching.strategy.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.RequestContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents with at least one field
 * matches the entire phrase - all input words in provided order.
 */
@Component("search_PhraseElasticsearchSearchStrategy")
public class PhraseElasticsearchSearchStrategy extends AbstractElasticSearchStrategy implements ElasticsearchSearchStrategy {

    protected PhraseElasticsearchSearchStrategy(ElasticSearchQueryConfigurator queryConfigurator) {
        super(queryConfigurator);
    }

    @Override
    public String getName() {
        return "phrase";
    }

    @Override
    public void configureRequest(RequestContext<SearchRequest.Builder> requestContext) {
        queryConfigurator.configureRequest(
                requestContext,
                (queryBuilder, fields) -> queryBuilder
                        .multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(fields)
                                        .query(requestContext.getSearchContext().getEscapedSearchText())
                                        .type(TextQueryType.Phrase)));
    }
}
