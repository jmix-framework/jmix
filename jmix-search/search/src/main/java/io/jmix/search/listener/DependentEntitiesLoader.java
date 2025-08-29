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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.PersistenceHints;
import io.jmix.search.listener.dynattr.DynamicAttributeReferenceFieldResolver;
import org.eclipse.persistence.exceptions.JPQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component("search_DependentEntitiesLoader")
public class DependentEntitiesLoader {

    protected static final Logger log = LoggerFactory.getLogger(DependentEntitiesLoader.class);

    protected final DataManager dataManager;
    protected final MetadataTools metadataTools;
    protected final DynamicAttributeReferenceFieldResolver dynamicAttributeReferenceFieldResolver;

    public DependentEntitiesLoader(DataManager dataManager,
                                   MetadataTools metadataTools,
                                   DynamicAttributeReferenceFieldResolver dynamicAttributeReferenceFieldResolver) {
        this.dataManager = dataManager;
        this.metadataTools = metadataTools;
        this.dynamicAttributeReferenceFieldResolver = dynamicAttributeReferenceFieldResolver;
    }

    public Set<Id<?>> loadDependentEntityIds(Id<?> targetEntityId,
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

                //TODO think about performance
                DependentEntitiesQuery dependentEntitiesQuery = new DependentEntitiesQueryBuilder(metadataTools, dynamicAttributeReferenceFieldResolver)
                        .loadEntity(metaClass)
                        .byProperty(propertyPath)
                        .dependedOn(targetMetaClass, targetEntityId)
                        .buildQuery();
                log.debug("{}", dependentEntitiesQuery);

                try {
                    List<Id<?>> refObjectIds = performLoadingDependentEntityIds(metaClass, dependentEntitiesQuery);
                    log.debug("Loaded primary keys of dependent references ({}): {}", refObjectIds.size(), refObjectIds);
                    result.addAll(refObjectIds);
                }
                catch (JPQLException e){
                    log.error("Can't execute query", e);
                    throw e;
                }
            }
        }

        return result;
    }

    protected List<Id<?>> performLoadingDependentEntityIds(MetaClass metaClass, DependentEntitiesQuery dependentEntitiesQuery) {
        return dataManager.load(metaClass.getJavaClass())
                .query(dependentEntitiesQuery.query())
                .parameters(dependentEntitiesQuery.parameters())
                .hint(PersistenceHints.SOFT_DELETION, false)
                .joinTransaction(true)
                .list()
                .stream()
                .map(Id::of)
                .collect(Collectors.toList());
    }
}
