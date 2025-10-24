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
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.searching.impl.FullFieldNamesProvider;
import io.jmix.search.utils.Constants;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @deprecated Use {@link SearchRequestScopeProvider}
 */
@Component("search_SearchUtils")
@Deprecated(since = "2.7", forRemoval = true)
public class SearchUtils {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final Metadata metadata;
    protected final FullFieldNamesProvider fullFieldNamesProvider;
    protected final SearchSecurityDecorator securityDecorator;

    public SearchUtils(IndexConfigurationManager indexConfigurationManager,
                       Metadata metadata,
                       FullFieldNamesProvider fullFieldNamesProvider,
                       SearchSecurityDecorator securityDecorator) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.metadata = metadata;
        this.fullFieldNamesProvider = fullFieldNamesProvider;
        this.securityDecorator = securityDecorator;
    }

    /**
     * @deprecated The method doesn't take into account security rights of the user
     * Use {@link SearchRequestScopeProvider#getSearchRequestScope(List, VirtualSubfieldsProvider)}
     */
    @Deprecated(since = "2.7", forRemoval = true)
    public List<String> resolveEntitiesAllowedToSearch(Collection<String> entityNames) {
        Collection<String> allIndexedEntities = indexConfigurationManager.getAllIndexedEntities();
        Collection<String> entityNamesWithConfigurations;
        if (entityNames.isEmpty()) {
            entityNamesWithConfigurations = allIndexedEntities;
        } else {
            entityNamesWithConfigurations = entityNames
                    .stream()
                    .filter(allIndexedEntities::contains)
                    .toList();
        }

        return securityDecorator.resolveEntitiesAllowedToSearch(entityNamesWithConfigurations);
    }

    /**
     * @deprecated The method doesn't take into account that some entity can be accessible by a user but the fields of the entity are not.
     * Use {@link SearchRequestScopeProvider#getSearchRequestScope(List, VirtualSubfieldsProvider)}.
     */
    @Deprecated(since = "2.7", forRemoval = true)
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
     * @deprecated The method doesn't take into account security constraints of entity fields.
     * The method doesn't separate result fields by the entities.
     * Use {@link SearchRequestScopeProvider#getSearchRequestScope(List, VirtualSubfieldsProvider)}
     */
    @Deprecated(since = "2.7", forRemoval = true)
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
                effectiveFieldsToSearch.addAll(fullFieldNamesProvider.getFieldNamesForBaseField(metaPropertyPath, fieldName));
            }
        }
        effectiveFieldsToSearch.add(Constants.INSTANCE_NAME_FIELD);
        return effectiveFieldsToSearch;
    }
}
