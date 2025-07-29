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

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.dynattr.impl.DynamicAttributeChangeEvent;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
public class DynamicAttributesTrackingListener {

    private static final Logger log = LoggerFactory.getLogger(DynamicAttributesTrackingListener.class);

    private final IndexConfigurationManager indexConfigurationManager;
    private final IndexingQueueManager indexingQueueManager;
    private final DependentEntitiesResolver dependentEntitiesResolver;
    private final EntityStates entityStates;
    private final MetadataTools metadataTools;

    public DynamicAttributesTrackingListener(IndexConfigurationManager indexConfigurationManager,
                                             IndexingQueueManager indexingQueueManager,
                                             DependentEntitiesResolver dependentEntitiesResolver, EntityStates entityStates, MetadataTools metadataTools) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.indexingQueueManager = indexingQueueManager;
        this.dependentEntitiesResolver = dependentEntitiesResolver;
        this.entityStates = entityStates;
        this.metadataTools = metadataTools;
    }

    @EventListener
    public void onDynamicAttributesChanging(DynamicAttributeChangeEvent<?> event) {
        DynamicAttributes dynamicAttributes = event.getDynamicAttributes();
        Object rawObject = event.getSource();
        Object rawId = EntityValues.getId(rawObject);

        MetaClass metaClass = event.getMetaClass();
        String entityMetaName = metaClass.getName();
        boolean isNew = entityStates.isNew(rawObject);
        //TODO null check
        Id<Object> entityId = Id.of(rawId, metaClass.getJavaClass());
        if (indexConfigurationManager.isDirectlyIndexed(entityMetaName)) {
            log.debug("{} is directly indexed", rawId);
            if (isNew) {

                indexingQueueManager.enqueueIndexByEntityId(entityId);
            } else {
                if (isUpdateRequired(metaClass.getJavaClass(), dynamicAttributes.getKeys())) {
                    indexingQueueManager.enqueueIndexByEntityId(entityId);
                }
            }
        }

        if (!isNew) {
            Set<Id<?>> dependentEntityIds = dependentEntitiesResolver.getEntityIdsDependentOnUpdatedEntity(entityId, metaClass, dynamicAttributes);
            if (!dependentEntityIds.isEmpty()) {
                indexingQueueManager.enqueueIndexCollectionByEntityIds(dependentEntityIds);
            }
        }
    }

    //TODO copy past
    protected boolean isUpdateRequired(Class<?> entityClass, Set<String> attributeList) {
        Set<String> affectedLocalPropertyNames = new HashSet<>(indexConfigurationManager.getLocalPropertyNamesAffectedByUpdate(entityClass));
        if (metadataTools.isSoftDeletable(entityClass)) {
            affectedLocalPropertyNames.add(metadataTools.findDeletedDateProperty(entityClass));
        }
        return attributeList
                .stream()
                .map(s -> "+" + s)
                .anyMatch(affectedLocalPropertyNames::contains);
    }
}
