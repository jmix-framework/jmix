/*
 * Copyright 2024 Haulmont.
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

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.ExtendedSearchConstants;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.searching.impl.SearchFieldsAdapter;
import io.jmix.search.utils.Constants;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("search_SearchUtils")
public class SearchUtils {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;
    protected final Metadata metadata;
    protected final SearchFieldsAdapter searchFieldsAdapter;

    public SearchUtils(IndexConfigurationManager indexConfigurationManager,
                       SecureOperations secureOperations,
                       PolicyStore policyStore,
                       Metadata metadata, SearchFieldsAdapter searchFieldsAdapter) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
        this.metadata = metadata;
        this.searchFieldsAdapter = searchFieldsAdapter;
    }

    public List<String> resolveEntitiesAllowedToSearch(Collection<String> requestedEntities) {
        if (requestedEntities.isEmpty()) {
            requestedEntities = indexConfigurationManager.getAllIndexedEntities();
        }

        return requestedEntities.stream()
                .filter(entity -> {
                    MetaClass metaClass = metadata.getClass(entity);
                    return secureOperations.isEntityReadPermitted(metaClass, policyStore);
                })
                .collect(Collectors.toList());
    }

    public List<String> resolveEffectiveTargetIndexes(Collection<String> requestedEntities) {
        List<String> allowedEntities = resolveEntitiesAllowedToSearch(requestedEntities);

        return allowedEntities.stream()
                .map(metadata::getClass)
                .map(metaClass -> indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(IndexConfiguration::getIndexName)
                .collect(Collectors.toList());
    }

    /**
     * without security
     * @param requestedEntities
     * @return
     */
    @Deprecated
    public Set<String> resolveEffectiveSearchFields(Collection<String> requestedEntities) {
        List<String> allowedEntities = resolveEntitiesAllowedToSearch(requestedEntities);

        Set<String> effectiveFieldsToSearch = new HashSet<>();
        for (String targetEntity : allowedEntities) {
            IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(targetEntity);
            IndexMappingConfiguration mapping = indexConfiguration.getMapping();
            Map<String, MappingFieldDescriptor> fields = mapping.getFields();

            for (Map.Entry<String, MappingFieldDescriptor> entry : fields.entrySet()) {
                String fieldName = entry.getKey();
                MappingFieldDescriptor mappingFieldDescriptor = entry.getValue();
                MetaPropertyPath metaPropertyPath = mappingFieldDescriptor.getMetaPropertyPath();
                effectiveFieldsToSearch.addAll(searchFieldsAdapter.getFieldsForIndexByPath(metaPropertyPath, fieldName));
            }
        }

        addRootInstanceField(effectiveFieldsToSearch);

        return effectiveFieldsToSearch;
    }

    /**
     * TODO
     * @param requestedEntities
     * @return
     */
    public Map<String, Set<String>> resolveEffectiveSearchFieldsWithSecurity(Collection<String> requestedEntities) {
        return resolveEntitiesAllowedToSearch(requestedEntities)
                .stream()
                .collect(Collectors.toMap(Function.identity(), this::resolveEffectiveSearchFieldsForEntity));
    }

    /**
     *
     * @param targetEntity
     * @return
     */
    public Set<String> resolveEffectiveSearchFieldsForEntity(String targetEntity) {
        return resolveEffectiveSearchFieldsForIndex(indexConfigurationManager.getIndexConfigurationByEntityName(targetEntity));
    }

    /**
     *
     * @param indexConfiguration
     * @return
     */
    public Set<String> resolveEffectiveSearchFieldsForIndex(IndexConfiguration indexConfiguration) {
        Set<String> effectiveFieldsToSearch = new HashSet<>();
        Map<String, MappingFieldDescriptor> fields = indexConfiguration.getMapping().getFields();

        for (Map.Entry<String, MappingFieldDescriptor> entry : fields.entrySet()) {
            MetaPropertyPath metaPropertyPath = entry.getValue().getMetaPropertyPath();
            if(secureOperations.isEntityAttrReadPermitted(metaPropertyPath, policyStore)){
                effectiveFieldsToSearch.addAll(searchFieldsAdapter.getFieldsForIndexByPath(metaPropertyPath, entry.getKey()));
            }
        }
        addRootInstanceField(effectiveFieldsToSearch);
        return effectiveFieldsToSearch;
    }

    public Set<String> resolveEffectiveSearchFieldsForIndexWithPrefixes(IndexConfiguration indexConfiguration) {
        Set<String> result = new HashSet<>();
        Set<String> fields = resolveEffectiveSearchFieldsForIndex(indexConfiguration);
        for(String fieldName: fields){
            result.add(fieldName);
            result.add(fieldName+"."+ ExtendedSearchConstants.PREFIX_SUBFIELD_NAME);
        }
        return result;
    }

    protected static void addRootInstanceField(Set<String> effectiveFieldsToSearch) {
        effectiveFieldsToSearch.add(Constants.INSTANCE_NAME_FIELD);
    }
}
