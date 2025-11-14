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

package io.jmix.search.listener.dynattr;

import io.jmix.core.Id;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.listener.DependentEntitiesLoader;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Resolver for determining entities that are dependent on a dynamically updated reference entity.
 * This class is responsible for managing the resolution of dependent entity IDs based on updates
 * to a specified entity and its associated dynamic attributes.
 */
public class DynamicReferenceDependentEntitiesResolver {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final DependentEntitiesLoader dependentEntitiesLoader;

    public DynamicReferenceDependentEntitiesResolver(IndexConfigurationManager indexConfigurationManager,
                                                     DependentEntitiesLoader dependentEntitiesLoader) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.dependentEntitiesLoader = dependentEntitiesLoader;
    }

    /**
     * Retrieves a set of entity IDs that are dependent on the specified updated entity.
     *
     * @param updatedEntityId  the ID of the updated entity whose dependencies are being resolved
     * @param metaClass        the metadata class of the updated entity
     * @param dynamicAttributes dynamic attributes associated with the updated entity
     * @return a set of IDs representing entities that are dependent on the updated entity
     */
    public Set<Id<?>> getEntityIdsDependentOnUpdatedEntity(Id<Object> updatedEntityId,
                                                           MetaClass metaClass,
                                                           DynamicAttributes dynamicAttributes) {
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData =
                indexConfigurationManager.getDependenciesMetaDataForUpdate(
                        updatedEntityId.getEntityClass(),
                        dynamicAttributes.getKeys());
        return dependentEntitiesLoader.loadDependentEntityIds(updatedEntityId, metaClass, dependenciesMetaData);
    }
}
