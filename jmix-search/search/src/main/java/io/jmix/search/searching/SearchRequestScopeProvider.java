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

package io.jmix.search.searching;

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.IndexLayout;
import io.jmix.search.index.impl.MultitenancyAdapter;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * The class is responsible for calculating scopes used for building
 * search requests in the search engine. It considers permissions for the current user and attaches
 * additional subfields to indexed fields as needed.
 * This component utilizes {@link SearchSecurityDecorator} to enforce security constraints,
 * {@link IndexConfigurationManager} for managing index configurations, and
 * {@link SearchFieldsProvider} to resolve fields of the index.
 */
@Component("search_SearchRequestScopeProvider")
public class SearchRequestScopeProvider {

    private static final Logger log = LoggerFactory.getLogger(SearchRequestScopeProvider.class);

    protected final SearchSecurityDecorator securityDecorator;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SearchFieldsProvider searchFieldsProvider;

    @Autowired
    protected IndexLayout indexLayout;

    @Autowired
    protected MultitenancyAdapter multitenancyAdapter;

    public SearchRequestScopeProvider(SearchSecurityDecorator securityDecorator,
                                      IndexConfigurationManager indexConfigurationManager,
                                      SearchFieldsProvider searchFieldsProvider) {
        this.securityDecorator = securityDecorator;
        this.indexConfigurationManager = indexConfigurationManager;
        this.searchFieldsProvider = searchFieldsProvider;
    }

    /**
     * Generates a list of {@link IndexSearchRequestScope} objects that define the scope of
     * search requests for the specified entities. The method evaluates the entities allowed
     * for the current user based on security constraints and resolves the fields required
     * for search using the provided {@link VirtualSubfieldsProvider}.
     *
     * @param entities                 list of entity names to evaluate for search scope. If empty, all indexed entities are considered.
     * @param virtualSubfieldsProvider provider used to resolve additional subfields for indexed fields.
     * @return list of {@link IndexSearchRequestScope} objects representing the search scope for the specified entities,
     * or an empty list if no valid scopes are resolved.
     */
    public List<IndexSearchRequestScope> getSearchRequestScope(List<String> entities, VirtualSubfieldsProvider virtualSubfieldsProvider) {

        Collection<String> entitiesWithSearchConfiguration = getEntitiesWithConfiguration(entities);

        List<String> allowedEntityNames = securityDecorator.resolveEntitiesAllowedToSearch(entitiesWithSearchConfiguration);

        if (allowedEntityNames.isEmpty()) {
            return emptyList();
        }

        String currentTenantId = multitenancyAdapter.getCurrentUserTenantId();

        List<IndexSearchRequestScope> result = new ArrayList<>(allowedEntityNames.size());
        for (String entityName : allowedEntityNames) {
            IndexConfiguration configuration = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);

            String indexName = indexLayout.indexName(configuration, currentTenantId);
            if (indexName == null) {
                log.debug("Entity '{}' is excluded from the search scope: there is no index for tenant '{}'",
                        entityName, currentTenantId);
                continue;
            }

            Set<String> fields = searchFieldsProvider.resolveFields(configuration, virtualSubfieldsProvider);
            if (fields.isEmpty()) {
                continue;
            }

            result.add(new IndexSearchRequestScope(configuration, fields, indexName));
        }
        return result;
    }

    protected Collection<String> getEntitiesWithConfiguration(List<String> entities) {
        Collection<String> allIndexedEntities = indexConfigurationManager.getAllIndexedEntities();
        if (entities.isEmpty()) {
            return allIndexedEntities;
        }
        return entities
                .stream()
                .filter(allIndexedEntities::contains)
                .toList();
    }
}
