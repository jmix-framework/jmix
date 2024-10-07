package io.jmix.searchelasticsearch.searching.strategy.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchUtils;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents
 * with at least one field matches at least one input word.
 */
@Component("search_AnyTermAnyFieldElasticsearchSearchStrategy")
public class AnyTermAnyFieldElasticsearchSearchStrategy extends AbstractSearchStrategy
        implements ElasticsearchSearchStrategy {

    protected final SearchUtils searchUtils;

    public AnyTermAnyFieldElasticsearchSearchStrategy(SearchUtils searchUtils) {
        this.searchUtils = searchUtils;
    }

    @Override
    public String getName() {
        return "anyTermAnyField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        Set<String> effectiveFieldsToSearch = searchUtils.resolveEffectiveSearchFields(searchContext.getEntities());
        requestBuilder.query(queryBuilder ->
                queryBuilder.multiMatch(multiMatchQueryBuilder ->
                        multiMatchQueryBuilder.fields(new ArrayList<>(effectiveFieldsToSearch))
                                .query(searchContext.getEscapedSearchText())
                                .operator(Operator.Or)
                )
        );
    }
}
