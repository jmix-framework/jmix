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

package io.jmix.search.listener;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.MetadataTools;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.PersistenceHints;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DependentEntitiesResolverImpl implements DependentEntitiesResolver {

    private static final Logger log = LoggerFactory.getLogger(DependentEntitiesResolverImpl.class);

    private final IndexConfigurationManager indexConfigurationManager;
    private final DataManager dataManager;
    private final MetadataTools metadataTools;

    public DependentEntitiesResolverImpl(IndexConfigurationManager indexConfigurationManager, DataManager dataManager, MetadataTools metadataTools) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.dataManager = dataManager;
        this.metadataTools = metadataTools;
    }

    @Override
    public Set<Id<?>> getEntityIdsDependentOnUpdatedEntity(Id<?> updatedEntityId, MetaClass metaClass, AttributeChanges changes) {
        return getEntityIdsDependentOnUpdatedEntityInternal(
                updatedEntityId,
                metaClass,
                updatedEntityId.getEntityClass(),
                changes.getAttributes());
    }

    @Override
    public Set<Id<?>> getEntityIdsDependentOnUpdatedEntity(Id<Object> updatedEntityId, MetaClass metaClass, DynamicAttributes dynamicAttributes) {
        return getEntityIdsDependentOnUpdatedEntityInternal(
                updatedEntityId,
                metaClass,
                updatedEntityId.getEntityClass(),
                dynamicAttributes.getKeys());
    }

    protected Set<Id<?>> getEntityIdsDependentOnUpdatedEntityInternal(Id<?> updatedEntityId, MetaClass metaClass, Class<?> entityClass, Set<String> attributes) {
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForUpdate(entityClass, attributes);
        return loadDependentEntityIds(updatedEntityId, metaClass, dependenciesMetaData);
    }

    @Override
    public Set<Id<?>> getEntityIdsDependentOnRemovedEntity(Id<?> removedEntityId, MetaClass metaClass) {
        Class<?> entityClass = removedEntityId.getEntityClass();
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForDelete(entityClass);
        return loadDependentEntityIds(removedEntityId, metaClass, dependenciesMetaData);
    }


    protected Set<Id<?>> loadDependentEntityIds(Id<?> targetEntityId,
                                                MetaClass targetMetaClass,
                                                Map<MetaClass, Set<MetaPropertyPath>> dependencyMetaData) {
        log.debug("Load dependent entity pks for entity {}: {}", targetEntityId, dependencyMetaData);

        Set<Id<?>> result = new HashSet<>();
        for (Map.Entry<MetaClass, Set<MetaPropertyPath>> entry : dependencyMetaData.entrySet()) {
            Set<MetaPropertyPath> properties = entry.getValue();
            if (properties.isEmpty()) {
                continue;
            }

            MetaClass metaClass = entry.getKey();
            String entityName = metaClass.getName();
            for (MetaPropertyPath propertyPath : properties) {
                log.debug("Load entities '{}' dependent via property '{}'", entityName, propertyPath);

                DependentEntitiesQuery dependentEntitiesQuery = new DependentEntitiesQueryBuilder(metadataTools)
                        .loadEntity(entityName)
                        .byProperty(propertyPath)
                        .dependedOn(targetMetaClass, targetEntityId)
                        .buildQuery();
                log.debug("{}", dependentEntitiesQuery);

                List<Id<?>> refObjectIds = performLoadingDependentEntityIds(metaClass, dependentEntitiesQuery);
                log.debug("Loaded primary keys of dependent references ({}): {}", refObjectIds.size(), refObjectIds);
                result.addAll(refObjectIds);
            }
        }

        return result;
    }

    protected List<Id<?>> performLoadingDependentEntityIds(MetaClass metaClass, DependentEntitiesQuery dependentEntitiesQuery) {
        return dataManager.load(metaClass.getJavaClass())
                .query(dependentEntitiesQuery.getQuery())
                .parameters(dependentEntitiesQuery.getParameters())
                .hint(PersistenceHints.SOFT_DELETION, false)
                .joinTransaction(true)
                .list()
                .stream()
                .map(Id::of)
                .collect(Collectors.toList());
    }
}