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
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.utils.Constants;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.search.searching.AbstractSearchQueryConfigurator.NO_SUBFIELDS;

/**
 * TODO
 */
@Component("search_SearchFieldsResolver")
public class SearchFieldsResolver {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SearchFieldSubstitute searchFieldSubstitute;
    protected final SearchSecurityDecorator securityDecorator;

    public SearchFieldsResolver(IndexConfigurationManager indexConfigurationManager,
                                SearchFieldSubstitute searchFieldSubstitute,
                                SearchSecurityDecorator securityDecorator) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.searchFieldSubstitute = searchFieldSubstitute;
        this.securityDecorator = securityDecorator;
    }

    /**
     * TODO
     *
     * @param indexConfiguration
     * @return
     */
    public Set<String> resolveFields(IndexConfiguration indexConfiguration, Function<String, Set<String>> subfieldsGenerator) {
        Set<String> effectiveFieldsToSearch = new HashSet<>();
        Map<String, MappingFieldDescriptor> fields = indexConfiguration.getMapping().getFields();

        for (Map.Entry<String, MappingFieldDescriptor> entry : fields.entrySet()) {
            MetaPropertyPath metaPropertyPath = entry.getValue().getMetaPropertyPath();
            if (securityDecorator.canAttributeBeRead(metaPropertyPath)) {
                effectiveFieldsToSearch.addAll(searchFieldSubstitute.getFieldsForPath(metaPropertyPath, entry.getKey()));
            }
        }
        addRootInstanceField(effectiveFieldsToSearch);

        if (subfieldsGenerator == NO_SUBFIELDS) {
            return effectiveFieldsToSearch;
        }

        return addSubfields(effectiveFieldsToSearch, subfieldsGenerator);
    }

    protected Set<String> addSubfields(Set<String> fields, Function<String, Set<String>> subfieldsResolver) {
        return fields
                .stream()
                .map(fieldName -> {
                    HashSet<String> fieldNames = new HashSet<>();
                    fieldNames.add(fieldName);
                    fieldNames.addAll(subfieldsResolver.apply(fieldName));
                    return fieldNames;
                })
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    protected static void addRootInstanceField(Set<String> effectiveFieldsToSearch) {
        effectiveFieldsToSearch.add(Constants.INSTANCE_NAME_FIELD);
    }

}
