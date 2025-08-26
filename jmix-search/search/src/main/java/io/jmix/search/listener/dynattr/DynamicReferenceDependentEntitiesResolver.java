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

@Component("search_DynamicReferenceDependentEntitiesResolver")
@Lazy
public class DynamicReferenceDependentEntitiesResolver {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final DependentEntitiesLoader dependentEntitiesLoader;

    public DynamicReferenceDependentEntitiesResolver(IndexConfigurationManager indexConfigurationManager,
                                                     DependentEntitiesLoader dependentEntitiesLoader) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.dependentEntitiesLoader = dependentEntitiesLoader;
    }


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
        return dependentEntitiesLoader.loadDependentEntityIds(updatedEntityId, metaClass, dependenciesMetaData);
    }
}
