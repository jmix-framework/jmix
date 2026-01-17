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

import java.util.HashSet;
import java.util.Set;

/**
 * A listener that listens for changes in dynamic attributes of entities and processes
 * them for indexing into a search system.
 * <p>
 * This class is responsible for tracking updates to entities with dynamic attributes and
 * ensuring they are appropriately handled in terms of updating search indexes. It listens
 * for {@link DynamicAttributeChangeEvent} events and performs necessary actions such as
 * enqueuing the entity for indexing or resolving and processing dependent entities that
 * may be affected by the change.
 * <p>
 * It considers specific configurations, such as whether an entity is indexed directly
 * and checks if updates are required depending on the affected attributes. Additionally,
 * it resolves entities dependent on the updated entity and enqueues them for reindexing
 * if necessary.
 */
public class DynamicAttributesTrackingListener {

    private static final Logger log = LoggerFactory.getLogger(DynamicAttributesTrackingListener.class);

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final IndexingQueueManager indexingQueueManager;
    protected final EntityStates entityStates;
    protected final MetadataTools metadataTools;

    public DynamicAttributesTrackingListener(IndexConfigurationManager indexConfigurationManager,
                                             IndexingQueueManager indexingQueueManager,
                                             EntityStates entityStates,
                                             MetadataTools metadataTools) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.indexingQueueManager = indexingQueueManager;
        this.entityStates = entityStates;
        this.metadataTools = metadataTools;
    }

    @EventListener
    public void onDynamicAttributesChange(DynamicAttributeChangeEvent<?> event) {
        DynamicAttributes dynamicAttributes = event.getDynamicAttributes();
        Object rawObject = event.getSource();
        Object rawId = EntityValues.getId(rawObject);

        MetaClass metaClass = event.getMetaClass();
        String entityMetaName = metaClass.getName();
        boolean isNew = entityStates.isNew(rawObject);

        if (rawId == null) {
            throw new IllegalArgumentException(
                    String.format("Entity id is null. Entity type is: %s. Consider of using different entity id generating type.",
                            rawObject.getClass().getName()));
        }

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
    }

    protected boolean isUpdateRequired(Class<?> entityClass, Set<String> attributeList) {
        Set<String> affectedLocalPropertyNames = new HashSet<>(indexConfigurationManager.getLocalPropertyNamesAffectedByUpdate(entityClass));
        return attributeList
                .stream()
                .map(s -> "+" + s)
                .anyMatch(affectedLocalPropertyNames::contains);
    }
}
