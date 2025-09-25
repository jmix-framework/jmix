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

package io.jmix.search.searching.impl;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.ExtendedSearchConstants;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.utils.Constants;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class SearchFieldsResolver {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;
    protected final SearchFieldsAdapter searchFieldsAdapter;

    public SearchFieldsResolver(IndexConfigurationManager indexConfigurationManager, SecureOperations secureOperations, PolicyStore policyStore, SearchFieldsAdapter searchFieldsAdapter) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
        this.searchFieldsAdapter = searchFieldsAdapter;
    }

    /**
     *
     * @param targetEntity
     * @return
     */
    public Set<String> resolveFields(String targetEntity) {
        return resolveFields(indexConfigurationManager.getIndexConfigurationByEntityName(targetEntity));
    }

    /**
     *
     * @param targetEntity
     * @return
     */
    public Set<String> resolveFieldsWithPrefixes(String targetEntity) {
        return resolveFieldsWithPrefixes(indexConfigurationManager.getIndexConfigurationByEntityName(targetEntity));
    }


    /**
     *
     * @param indexConfiguration
     * @return
     */
    public Set<String> resolveFields(IndexConfiguration indexConfiguration) {
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

    public Set<String> resolveFieldsWithPrefixes(IndexConfiguration indexConfiguration) {
        Set<String> result = new HashSet<>();
        Set<String> fields = resolveFields(indexConfiguration);
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
