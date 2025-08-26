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
import org.eclipse.persistence.exceptions.JPQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component("search_DependentEntitiesResolver")
public class DependentEntitiesResolverImpl implements DependentEntitiesResolver {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final DependentEntitiesLoader dependentEntitiesLoader;

    public DependentEntitiesResolverImpl(IndexConfigurationManager indexConfigurationManager, DependentEntitiesLoader dependentEntitiesLoader) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.dependentEntitiesLoader = dependentEntitiesLoader;
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
    public Set<Id<?>> getEntityIdsDependentOnRemovedEntity(Id<?> removedEntityId, MetaClass metaClass) {
        Class<?> entityClass = removedEntityId.getEntityClass();
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForDelete(entityClass);
        return dependentEntitiesLoader.loadDependentEntityIds(removedEntityId, metaClass, dependenciesMetaData);
    }

    protected Set<Id<?>> getEntityIdsDependentOnUpdatedEntityInternal(Id<?> updatedEntityId, MetaClass metaClass, Class<?> entityClass, Set<String> attributes) {
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForUpdate(entityClass, attributes);
        return dependentEntitiesLoader.loadDependentEntityIds(updatedEntityId, metaClass, dependenciesMetaData);
    }


}