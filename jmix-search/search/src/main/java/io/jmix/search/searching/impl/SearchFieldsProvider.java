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
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.searching.SubfieldsProvider;
import io.jmix.search.utils.Constants;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jmix.search.searching.AbstractSearchQueryConfigurator.NO_SUBFIELDS;

/**
 * Contains logic for getting fields of the given index for the request query building.
 */
@Component("search_SearchFieldsResolver")
public class SearchFieldsProvider {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SearchFieldSubstitute searchFieldSubstitute;
    protected final SearchSecurityDecorator securityDecorator;

    public SearchFieldsProvider(IndexConfigurationManager indexConfigurationManager,
                                SearchFieldSubstitute searchFieldSubstitute,
                                SearchSecurityDecorator securityDecorator) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.searchFieldSubstitute = searchFieldSubstitute;
        this.securityDecorator = securityDecorator;
    }

    /**
     * Returns fields with subfields of the given index for the request query building.
     * The method taking into account if the current user has permission for the correspondent property reading or not.
     * The method adds subfields to the result if they are provided with the subfieldsProvider.
     *
     * @param indexConfiguration - index configuration for getting fields.
     *                           The field names are getting from the correspondent {@link IndexMappingConfiguration}
     * @param subfieldsProvider - a {@link SubfieldsProvider} for getting subfields.
     * @return set of the fields for searching
     */
    public Set<String> resolveFields(IndexConfiguration indexConfiguration, SubfieldsProvider subfieldsProvider) {
        Set<String> effectiveFieldsToSearch = new HashSet<>();
        Map<String, MappingFieldDescriptor> fields = indexConfiguration.getMapping().getFields();

        for (Map.Entry<String, MappingFieldDescriptor> entry : fields.entrySet()) {
            MetaPropertyPath metaPropertyPath = entry.getValue().getMetaPropertyPath();
            if (securityDecorator.canAttributeBeRead(metaPropertyPath)) {
                effectiveFieldsToSearch.addAll(searchFieldSubstitute.getFieldsForPath(metaPropertyPath, entry.getKey()));
            }
        }
        addRootInstanceField(effectiveFieldsToSearch);

        if (subfieldsProvider == NO_SUBFIELDS) {
            return effectiveFieldsToSearch;
        }

        return addSubfields(indexConfiguration, effectiveFieldsToSearch, subfieldsProvider);
    }

    protected Set<String> addSubfields(
            IndexConfiguration indexConfiguration,
            Set<String> fields,
            SubfieldsProvider subfieldsProvider) {
        return fields
                .stream()
                .map(fieldName -> {
                    HashSet<String> fieldNames = new HashSet<>();
                    fieldNames.add(fieldName);
                    fieldNames.addAll(subfieldsProvider.getSubfields(
                            new SubfieldsProvider.FieldInfo(indexConfiguration.getIndexName(), fieldName))
                    );
                    return fieldNames;
                })
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    protected static void addRootInstanceField(Set<String> effectiveFieldsToSearch) {
        effectiveFieldsToSearch.add(Constants.INSTANCE_NAME_FIELD);
    }

}
