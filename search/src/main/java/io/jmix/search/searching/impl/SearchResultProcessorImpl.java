/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.searching.impl;

import com.google.common.collect.Iterables;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.SearchResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("search_SearchResultProcessor")
public class SearchResultProcessorImpl implements SearchResultProcessor {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected DataManager secureDataManager;

    @Override
    public Collection<Object> loadEntityInstances(SearchResult searchResult) {
        return loadEntityInstances(searchResult, Collections.emptyMap());
    }

    @Override
    public Collection<Object> loadEntityInstances(SearchResult searchResult, Map<String, FetchPlan> fetchPlans) {
        List<Object> result = new ArrayList<>(searchResult.getSize());
        searchResult.getEntityNames().forEach(entityName -> {
            MetaClass metaClass = metadata.getClass(entityName);
            Collection<SearchResultEntry> entries = searchResult.getEntriesByEntityName(entityName);

            for (Collection<SearchResultEntry> entriesPartition : Iterables.partition(entries, searchProperties.getSearchReloadEntitiesBatchSize())) {
                List<Object> partitionIds = entriesPartition.stream()
                        .map(entry -> idSerialization.stringToId(entry.getDocId()))
                        .map(Id::getValue)
                        .collect(Collectors.toList());

                FetchPlan fetchPlan = fetchPlans.get(entityName);
                FluentLoader.ByIds<Object> loader = secureDataManager.load(metaClass.getJavaClass()).ids(partitionIds);
                if (fetchPlan == null) {
                    loader.fetchPlan(FetchPlan.BASE);
                } else {
                    loader.fetchPlan(fetchPlan);
                }
                List<Object> partitionResult = loader.list();

                result.addAll(partitionResult);
            }
        });
        return result;
    }
}
