/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.app.events.AttributeChanges;
import com.haulmont.cuba.core.app.events.EntityChangedEvent;
import com.haulmont.cuba.core.entity.annotation.PublishEntityChangedEvents;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.Events;
import io.jmix.core.Entity;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("jmix_CubaEntityChangedEventManager")
public class CubaEntityChangedEventManager {

    @Autowired
    private Events eventPublisher;

    @Autowired
    private Metadata metadata;

    @Autowired
    private ExtendedEntities extendedEntities;

    private Map<Class, PublishingInfo> infoCache = new ConcurrentHashMap<>();

    private static class PublishingInfo {
        final boolean publish;
        final boolean onCreated;
        final boolean onUpdated;
        final boolean onDeleted;
        final MetaClass originalMetaClass;

        public PublishingInfo() {
            publish = false;
            onCreated = false;
            onUpdated = false;
            onDeleted = false;
            originalMetaClass = null;
        }

        public PublishingInfo(boolean onCreated, boolean onUpdated, boolean onDeleted, MetaClass originalMetaClass) {
            this.publish = true;
            this.onCreated = onCreated;
            this.onUpdated = onUpdated;
            this.onDeleted = onDeleted;
            this.originalMetaClass = originalMetaClass;
        }
    }

    @EventListener
    public void handleEvent(io.jmix.core.event.EntityChangedEvent<? extends Entity> event) {

        PublishingInfo info = infoCache.computeIfAbsent(event.getEntityId().getEntityClass(), aClass -> {
            MetaClass metaClass = metadata.getClass(event.getEntityId().getEntityClass());
            MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
            Map attrMap = (Map) metaClass.getAnnotations().get(PublishEntityChangedEvents.class.getName());
            if (attrMap != null) {
                return new PublishingInfo(
                        Boolean.TRUE.equals(attrMap.get("created")),
                        Boolean.TRUE.equals(attrMap.get("updated")),
                        Boolean.TRUE.equals(attrMap.get("deleted")),
                        originalMetaClass);
            }
            return new PublishingInfo();
        });

        EntityChangedEvent.Type type = resolveType(event.getType());

        if (!info.publish
                || (!info.onCreated && type == EntityChangedEvent.Type.CREATED)
                || (!info.onUpdated && type == EntityChangedEvent.Type.UPDATED)
                || (!info.onDeleted && type == EntityChangedEvent.Type.DELETED)) {
            return;
        }

        io.jmix.core.Id<? extends Entity> entityId = event.getEntityId();
        eventPublisher.publish(new EntityChangedEvent<>(event.getSource(),
                Id.of(entityId.getValue(), entityId.getEntityClass()),
                type,
                new AttributeChanges(event.getChanges())));
    }

    private EntityChangedEvent.Type resolveType(io.jmix.core.event.EntityChangedEvent.Type type) {
        if (type == io.jmix.core.event.EntityChangedEvent.Type.CREATED) {
            return EntityChangedEvent.Type.CREATED;
        } else if (type == io.jmix.core.event.EntityChangedEvent.Type.UPDATED) {
            return EntityChangedEvent.Type.UPDATED;
        } else if (type == io.jmix.core.event.EntityChangedEvent.Type.DELETED) {
            return EntityChangedEvent.Type.DELETED;
        }
        return null;
    }
}
