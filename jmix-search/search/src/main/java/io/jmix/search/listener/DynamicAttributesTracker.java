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
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
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

import java.util.Set;


@Component
public class DynamicAttributesTracker {

    private static final Logger log = LoggerFactory.getLogger(DynamicAttributesTracker.class);

    private final IndexConfigurationManager indexConfigurationManager;
    private final IndexingQueueManager indexingQueueManager;
    private final DependentEntitiesResolver dependentEntitiesResolver;

    public DynamicAttributesTracker(IndexConfigurationManager indexConfigurationManager,
                                    IndexingQueueManager indexingQueueManager,
                                    DependentEntitiesResolver dependentEntitiesResolver) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.indexingQueueManager = indexingQueueManager;
        this.dependentEntitiesResolver = dependentEntitiesResolver;
    }

    @EventListener
    public void onDynamicAttributesSavingNew(DynamicAttributeChangeEvent event) {
        DynamicAttributes dynamicAttributes = event.getDynamicAttributes();
        Object rawId = EntityValues.getId(event.getSource());

        MetaClass metaClass = event.getMetaClass();
        String entityName = metaClass.getName();
        Id<Object> objectId = Id.of(rawId, metaClass.getJavaClass());
        if (indexConfigurationManager.isDirectlyIndexed(entityName)) {
            log.debug("{} is directly indexed", rawId);
            indexingQueueManager.enqueueIndexByEntityId(objectId);
        }

        Set<Id<?>> dependentEntityIds = dependentEntitiesResolver.getEntityIdsDependentOnUpdatedEntity(objectId, metaClass, dynamicAttributes);

        if (!dependentEntityIds.isEmpty()) {
            indexingQueueManager.enqueueIndexCollectionByEntityIds(dependentEntityIds);
        }

    }


}
