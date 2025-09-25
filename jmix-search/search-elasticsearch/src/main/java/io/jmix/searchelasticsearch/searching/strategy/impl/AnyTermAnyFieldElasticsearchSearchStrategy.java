package io.jmix.searchelasticsearch.searching.strategy.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import org.springframework.stereotype.Component;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents
 * with at least one field matches at least one input word.
 */
@Component("search_AnyTermAnyFieldElasticsearchSearchStrategy")
public class AnyTermAnyFieldElasticsearchSearchStrategy extends AbstractSearchStrategy
        implements ElasticsearchSearchStrategy {

    protected final ElasticSearchQueryConfigurator elasticSearchQueryConfigurator;
    protected final SearchFieldsResolver searchFieldsResolver;

    public AnyTermAnyFieldElasticsearchSearchStrategy(ElasticSearchQueryConfigurator elasticSearchQueryConfigurator, SearchFieldsResolver searchFieldsResolver) {
        this.elasticSearchQueryConfigurator = elasticSearchQueryConfigurator;
        this.searchFieldsResolver = searchFieldsResolver;
    }

    @Override
    public String getName() {
        return "anyTermAnyField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        elasticSearchQueryConfigurator.configureRequest(
                requestBuilder,
                searchContext.getEntities(),
                searchFieldsResolver::resolveFields,
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(fields)
                                        .query(searchContext.getEscapedSearchText())
                                        .operator(Operator.Or)
                        ));
    }
}
