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

import io.jmix.core.Id;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * The {@code DependentEntitiesResolver} is responsible for determining the set of entities
 * that are dependent on a specific entity when that entity is updated or removed.
 * It uses configurations and dependency metadata to resolve the dependent entity IDs.
 */
@Component("search_DependentEntitiesResolver")
public class DependentEntitiesResolver {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final DependentEntitiesLoader dependentEntitiesLoader;

    public DependentEntitiesResolver(IndexConfigurationManager indexConfigurationManager,
                                     DependentEntitiesLoader dependentEntitiesLoader) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.dependentEntitiesLoader = dependentEntitiesLoader;
    }

    public Set<Id<?>> getEntityIdsDependentOnUpdatedEntity(Id<?> updatedEntityId,
                                                           MetaClass metaClass,
                                                           AttributeChanges changes) {
        return getEntityIdsDependentOnUpdatedEntityInternal(
                updatedEntityId,
                metaClass,
                updatedEntityId.getEntityClass(),
                changes.getAttributes());
    }

    public Set<Id<?>> getEntityIdsDependentOnRemovedEntity(Id<?> removedEntityId, MetaClass metaClass) {
        Class<?> entityClass = removedEntityId.getEntityClass();
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForDelete(entityClass);
        return dependentEntitiesLoader.loadDependentEntityIds(removedEntityId, metaClass, dependenciesMetaData);
    }

    protected Set<Id<?>> getEntityIdsDependentOnUpdatedEntityInternal(Id<?> updatedEntityId,
                                                                      MetaClass metaClass,
                                                                      Class<?> entityClass,
                                                                      Set<String> attributes) {
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForUpdate(entityClass, attributes);
        return dependentEntitiesLoader.loadDependentEntityIds(updatedEntityId, metaClass, dependenciesMetaData);
    }
}