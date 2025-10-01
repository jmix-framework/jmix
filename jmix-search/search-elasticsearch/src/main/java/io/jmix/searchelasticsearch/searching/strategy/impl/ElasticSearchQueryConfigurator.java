/*
 * Copyright 2025 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.searchelasticsearch.searching.strategy.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.util.ObjectBuilder;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.AbstractSearchQueryConfigurator;
import io.jmix.search.searching.RequestContext;
import io.jmix.search.searching.SearchUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ElasticSearchQueryConfigurator extends AbstractSearchQueryConfigurator<SearchRequest.Builder, Query.Builder, ObjectBuilder<Query>> {

    public ElasticSearchQueryConfigurator(SearchUtils searchUtils, IndexConfigurationManager indexConfigurationManager) {
        super(searchUtils, indexConfigurationManager);
    }

    @Override
    protected void processEntitiesWithFields(RequestContext<SearchRequest.Builder> requestContext, TargetQueryBuilder<Query.Builder, ObjectBuilder<Query>> targetQueryBuilder, Map<String, Set<String>> indexNamesWithFields) {
        requestContext.getRequestBuilder().query(createQuery(targetQueryBuilder, indexNamesWithFields).build());
    }

    @Override
    protected ObjectBuilder<Query> createQueryForSingleIndex(TargetQueryBuilder<Query.Builder, ObjectBuilder<Query>> targetQueryBuilder, Map<String, Set<String>> indexesWithFields) {
        Query.Builder builder = new Query.Builder();
        return targetQueryBuilder
                .apply(
                        builder,
                        new ArrayList<>(indexesWithFields.entrySet().iterator().next().getValue()));
    }

    @Override
    protected ObjectBuilder<Query> createQueryForMultipleIndexes(TargetQueryBuilder<Query.Builder, ObjectBuilder<Query>> targetQueryBuilder, Map<String, Set<String>> indexesWithFields) {
        return new Query.Builder()
                .bool(boolBuilder ->
                        boolBuilder.should(createSubqueriesForIndexes(indexesWithFields, targetQueryBuilder)));
    }

    private List<Query> createSubqueriesForIndexes(Map<String, Set<String>> indexesWithFields, TargetQueryBuilder<Query.Builder, ObjectBuilder<Query>> targetQueryBuilder) {
        return indexesWithFields
                .entrySet()
                .stream()
                .map(entry -> createQueryForSingleIndex(entry.getKey(), entry.getValue(), targetQueryBuilder))
                .toList();
    }

    private Query createQueryForSingleIndex(String indexName, Set<String> fields, TargetQueryBuilder<Query.Builder, ObjectBuilder<Query>> targetQueryBuilder) {
        return Query.of(root ->
                root.bool(b -> b
                        .must(m -> m.term(t -> t.field("_index").value(indexName)))
                        .must(m2 -> targetQueryBuilder.apply(m2, new ArrayList<>(fields)))
                ));
    }
}
