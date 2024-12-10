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
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("search_SearchUtils")
public class SearchUtils {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;
    protected final Metadata metadata;

    public SearchUtils(IndexConfigurationManager indexConfigurationManager,
                       SecureOperations secureOperations,
                       PolicyStore policyStore,
                       Metadata metadata) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
        this.metadata = metadata;
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

    public Set<String> resolveEffectiveSearchFields(Collection<String> requestedEntities) {
        List<String> allowedEntities = resolveEntitiesAllowedToSearch(requestedEntities);

        Set<String> effectiveFieldsToSearch = new HashSet<>();
        for (String targetEntity : allowedEntities) {
            IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(targetEntity);
            IndexMappingConfiguration mapping = indexConfiguration.getMapping();
            Map<String, MappingFieldDescriptor> fields = mapping.getFields();
            Set<String> fieldNames = fields.keySet();

            for (String fieldName : fieldNames) {
                MappingFieldDescriptor mappingFieldDescriptor = fields.get(fieldName);
                MetaPropertyPath metaPropertyPath = mappingFieldDescriptor.getMetaPropertyPath();
                if (isFileRefProperty(metaPropertyPath)) {
                    // Add nested fields created by FileFieldMapper
                    effectiveFieldsToSearch.add(fieldName + "._file_name");
                    effectiveFieldsToSearch.add(fieldName + "._content");
                } else {
                    effectiveFieldsToSearch.add(fieldName);
                }
            }
        }

        return effectiveFieldsToSearch;
    }

    protected boolean isFileRefProperty(MetaPropertyPath propertyPath) {
        if (propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            return datatype instanceof FileRefDatatype;
        } else {
            return false;
        }
    }
}
