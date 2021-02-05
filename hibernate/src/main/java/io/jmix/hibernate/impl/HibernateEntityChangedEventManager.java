/*
 * Copyright 2019 Haulmont.
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

package io.jmix.hibernate.impl;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.impl.EntityChangedEventInfo;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

@Component("hibernate_EntityChangedEventManager")
public class HibernateEntityChangedEventManager {

    private static final Logger log = LoggerFactory.getLogger(HibernateEntityChangedEventManager.class);

    private static final String RESOURCE_KEY = AccumulatedInfoHolder.class.getName();

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected HibernatePersistenceSupport persistenceSupport;

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected HibernateChangesProvider changesProvider;

    private static class AccumulatedInfoHolder extends ResourceHolderSupport {

        List<EntityChangedEventInfo> accumulatedList;
    }

    private static class AccumulatedInfoSynchronization extends ResourceHolderSynchronization<AccumulatedInfoHolder, String> {

        AccumulatedInfoSynchronization(AccumulatedInfoHolder resourceHolder) {
            super(resourceHolder, RESOURCE_KEY);
        }
    }

    private AccumulatedInfoHolder getAccumulatedInfoHolder() {
        AccumulatedInfoHolder holder = (AccumulatedInfoHolder) TransactionSynchronizationManager.getResource(RESOURCE_KEY);
        if (holder == null) {
            holder = new AccumulatedInfoHolder();
            TransactionSynchronizationManager.bindResource(RESOURCE_KEY, holder);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive() && !holder.isSynchronizedWithTransaction()) {
            holder.setSynchronizedWithTransaction(true);
            TransactionSynchronizationManager.registerSynchronization(new AccumulatedInfoSynchronization(holder));
        }
        return holder;
    }

    public void beforeFlush(SessionImplementor session, Collection<Object> instances) {
        log.trace("beforeFlush {}", instances);
        List<EntityChangedEventInfo> infoList = internalCollect(session, instances);
        AccumulatedInfoHolder holder = getAccumulatedInfoHolder();
        holder.accumulatedList = merge(holder.accumulatedList, infoList);
    }

    private List<EntityChangedEventInfo> merge(Collection<EntityChangedEventInfo> collection1, Collection<EntityChangedEventInfo> collection2) {
        List<EntityChangedEventInfo> list1 = collection1 != null ? new ArrayList<>(collection1) : new ArrayList<>();
        Collection<EntityChangedEventInfo> list2 = collection2 != null ? collection2 : Collections.emptyList();

        for (EntityChangedEventInfo info2 : list2) {
            Optional<EntityChangedEventInfo> opt = list1.stream()
                    .filter(info1 -> info1.getEntity() == info2.getEntity())
                    .findAny();
            if (opt.isPresent()) {
                opt.get().mergeWith(info2);
            } else {
                list1.add(info2);
            }
        }
        log.trace("merged {}", list1);
        return list1;
    }

    public List<EntityChangedEventInfo> collect(SessionImplementor session, Collection<Object> entities) {
        log.trace("collect {}", entities);
        AccumulatedInfoHolder holder = getAccumulatedInfoHolder();
        List<EntityChangedEventInfo> infoList = internalCollect(session, entities);
        return merge(holder.accumulatedList, infoList);
    }

    public List<EntityChangedEventInfo> internalCollect(SessionImplementor session, Collection<Object> entities) {
        List<EntityChangedEventInfo> list = new ArrayList<>();
        for (Object entity : entities) {
//            persistenceSupport.getInstances()
            EntityChangedEvent.Type type = null;
            AttributeChanges attributeChanges = null;
            if (getEntityEntry(entity).isNew()) {
                type = EntityChangedEvent.Type.CREATED;
                attributeChanges = changesProvider.getEntityAttributeChanges(entity, false);
            } else {
                if (session != null) {
                    EntityEntry entry = HibernateUtils.getEntityEntry(session, entity);

                    if (entry == null) {
                        log.debug("Cannot publish EntityChangedEvent for {} because its EntityEntry is null", entity);
                        continue;
                    }
                    if (persistenceSupport.isDeleted(entity, entry)) {
                        type = EntityChangedEvent.Type.DELETED;
                        attributeChanges = changesProvider.getEntityAttributeChanges(entity, true);
                    } else if (changesProvider.hasChanges(entity, entry)) {
                        type = EntityChangedEvent.Type.UPDATED;
                        attributeChanges = changesProvider.getEntityAttributeChanges(entity, entry);
                    }
                } else {
                    log.debug("Cannot publish EntityChangedEvent for {} because its Session is null", entity);
                }
            }
            if (type != null && attributeChanges != null) {
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(entity));

                EntityChangedEventInfo eventData = new EntityChangedEventInfo(this, entity, type,
                        attributeChanges, originalMetaClass);
                list.add(eventData);
            }
        }
        log.trace("collected {}", list);
        return list;
    }

    public void publish(Collection<EntityChangedEvent> events) {
        log.trace("publish {}", events);
        for (EntityChangedEvent event : events) {
            eventPublisher.publishEvent(event);
        }
    }


}

