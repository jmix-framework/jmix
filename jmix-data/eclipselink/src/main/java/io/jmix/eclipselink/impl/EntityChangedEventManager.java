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

package io.jmix.eclipselink.impl;

import io.jmix.core.Entity;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.impl.EntityChangedEventInfo;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.descriptors.changetracking.AttributeChangeListener;
import org.eclipse.persistence.sessions.changesets.AggregateChangeRecord;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import java.util.*;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

@Component("eclipselink_EntityChangedEventManager")
public class EntityChangedEventManager {

    private static final Logger log = LoggerFactory.getLogger(EntityChangedEventManager.class);

    private static final String RESOURCE_KEY = AccumulatedInfoHolder.class.getName();

    @Autowired
    private Metadata metadata;
    @Autowired
    protected EclipselinkPersistenceSupport persistenceSupport;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ExtendedEntities extendedEntities;

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

    public void beforeFlush(Collection<Object> instances) {
        log.trace("beforeFlush {}", instances);
        List<EntityChangedEventInfo> infoList = internalCollect(instances);
        AccumulatedInfoHolder holder = getAccumulatedInfoHolder();
        holder.accumulatedList = merge(holder.accumulatedList, infoList);//store events because changes will be lost during implicit flush
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

    public List<EntityChangedEventInfo> collect(Collection<Object> entities) {
        log.trace("collect {}", entities);
        AccumulatedInfoHolder holder = getAccumulatedInfoHolder();
        List<EntityChangedEventInfo> infoList = internalCollect(entities);
        if (holder.accumulatedList != null) {
            infoList = merge(holder.accumulatedList, infoList);
            //clear stored events: it is time to publish them
            // otherwise stack overflow may occur if listener will commit something and the same events will be got and processed again
            holder.accumulatedList.clear();
        }
        return infoList;
    }

    public List<EntityChangedEventInfo> internalCollect(Collection<Object> entities) {
        List<EntityChangedEventInfo> list = new ArrayList<>();
        for (Object entity : entities) {

            EntityChangedEvent.Type type = null;
            AttributeChanges.Builder attributeChangesBuilder = null;
            if (getEntityEntry(entity).isNew()) {
                type = EntityChangedEvent.Type.CREATED;
                attributeChangesBuilder = getEntityAttributeChanges(entity, false);
            } else {
                AttributeChangeListener changeListener =
                        (AttributeChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener();
                if (changeListener == null) {
                    log.debug("Cannot publish EntityChangedEvent for {} because its AttributeChangeListener is null", entity);
                    continue;
                }
                if (persistenceSupport.isDeleted(entity, changeListener)) {
                    type = EntityChangedEvent.Type.DELETED;
                    attributeChangesBuilder = getEntityAttributeChanges(entity, true);
                } else if (changeListener.hasChanges()) {
                    type = EntityChangedEvent.Type.UPDATED;
                    attributeChangesBuilder = getEntityAttributeChanges(entity, changeListener.getObjectChangeSet());
                }
            }
            if (type != null && attributeChangesBuilder != null) {
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(entity));

                EntityChangedEventInfo eventData = new EntityChangedEventInfo(this, entity, type,
                        attributeChangesBuilder.build(), originalMetaClass);
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

    @SuppressWarnings("unchecked")
    @Nullable
    private AttributeChanges.Builder getEntityAttributeChanges(@Nullable Object entity, @Nullable ObjectChangeSet changeSet) {
        if (changeSet == null)
            return null;

        AttributeChanges.Builder builder = AttributeChanges.Builder.create();

        for (ChangeRecord changeRecord : changeSet.getChanges()) {
            if (changeRecord instanceof AggregateChangeRecord) {
                builder.withEmbedded(changeRecord.getAttribute(),
                        getEntityAttributeChanges(null, ((AggregateChangeRecord) changeRecord).getChangedObject()));
            } else {
                Object oldValue = changeRecord.getOldValue();
                if (oldValue instanceof Entity) {
                    builder.withChange(changeRecord.getAttribute(), Id.of(oldValue));
                } else if (oldValue instanceof Collection) {
                    Collection<Object> coll = (Collection<Object>) oldValue;
                    Collection<Object> resultColl = oldValue instanceof List ? new ArrayList<>() : new LinkedHashSet<>();
                    for (Object item : coll) {
                        if (item instanceof Entity) {
                            resultColl.add(Id.of(item));
                        } else {
                            resultColl.add(item);
                        }
                    }
                    builder.withChange(changeRecord.getAttribute(), resultColl);
                } else {
                    Object convertedValue;
                    if (entity != null) {
                        MetaClass metaClass = metadata.getClass(entity);
                        convertedValue = convertValueIfNeeded(metaClass.getProperty(changeRecord.getAttribute()), oldValue);
                    } else {
                        convertedValue = oldValue;
                    }
                    builder.withChange(changeRecord.getAttribute(), convertedValue);
                }
            }
        }

        // todo dynamic attributes
//        addDynamicAttributeChanges(entity, changes, false);

        return builder;
    }

    // todo dynamic attributes
//    @SuppressWarnings("unchecked")
//    private void addDynamicAttributeChanges(@Nullable Entity entity, Set<AttributeChanges.Change> changes, boolean deleted) {
//        if (entity instanceof BaseGenericIdEntity && ((BaseGenericIdEntity) entity).getDynamicAttributes() != null) {
//            Map<String, CategoryAttributeValue> dynamicAttributes = ((BaseGenericIdEntity) entity).getDynamicAttributes();
//            for (CategoryAttributeValue cav : dynamicAttributes.values()) {
//                if (BaseEntityInternalAccess.isNew(cav)) {
//                    changes.add(new AttributeChanges.Change(DynamicAttributesUtils.encodeAttributeCode(cav.getCode()), null));
//                } else {
//                    if (deleted) {
//                        Object oldValue;
//                        switch (cav.getCategoryAttribute().getDataType()) {
//                            case STRING:
//                            case ENUMERATION:
//                                oldValue = cav.getStringValue();
//                                break;
//                            case INTEGER:
//                                oldValue = cav.getIntValue();
//                                break;
//                            case DOUBLE:
//                                oldValue = cav.getDoubleValue();
//                                break;
//                            case BOOLEAN:
//                                oldValue = cav.getBooleanValue();
//                                break;
//                            case DATE:
//                                oldValue = cav.getDateValue();
//                                break;
//                            case ENTITY:
//                                Object entityId = cav.getEntityValue().getObjectEntityId();
//                                Class entityClass = cav.getCategoryAttribute().getJavaClassForEntity();
//                                oldValue = entityId != null ? Id.of(entityId, entityClass) : null;
//                                break;
//                            default:
//                                log.warn("Unsupported dynamic attribute type: " + cav.getCategoryAttribute().getDataType());
//                                oldValue = null;
//                        }
//                        changes.add(new AttributeChanges.Change(DynamicAttributesUtils.encodeAttributeCode(cav.getCode()), oldValue));
//                    } else {
//                        AttributeChangeListener changeListener =
//                                (AttributeChangeListener) ((ChangeTracker) cav)._persistence_getPropertyChangeListener();
//                        if (changeListener != null && changeListener.getObjectChangeSet() != null) {
//                            Object oldValue = null;
//                            boolean changed = false;
//                            for (ChangeRecord changeRecord : changeListener.getObjectChangeSet().getChanges()) {
//                                switch (changeRecord.getAttribute()) {
//                                    case "stringValue":
//                                    case "intValue":
//                                    case "doubleValue":
//                                    case "booleanValue":
//                                    case "dateValue":
//                                        oldValue = changeRecord.getOldValue();
//                                        changed = true;
//                                        break;
//                                    case "entityValue":
//                                        Object entityId = ((ReferenceToEntity) changeRecord.getOldValue()).getObjectEntityId();
//                                        Class entityClass = cav.getCategoryAttribute().getJavaClassForEntity();
//                                        oldValue = entityId != null ? Id.of(entityId, entityClass) : null;
//                                        changed = true;
//                                        break;
//                                }
//                                if (changed) {
//                                    changes.add(new AttributeChanges.Change(DynamicAttributesUtils.encodeAttributeCode(cav.getCode()), oldValue));
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    @SuppressWarnings("unchecked")
    private AttributeChanges.Builder getEntityAttributeChanges(Object entity, boolean deleted) {
        AttributeChanges.Builder builder = AttributeChanges.Builder.create();
        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (!property.isReadOnly()) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (deleted) {
                    if (value instanceof Entity) {
                        if (EntitySystemAccess.isEmbeddable(entity)) {
                            builder.withEmbedded(property.getName(), getEntityAttributeChanges(value, true));
                        } else {
                            builder.withChange(property.getName(), Id.of(value));
                        }
                    } else if (value instanceof Collection) {
                        Collection<Object> coll = (Collection<Object>) value;
                        Collection<Object> resultColl = value instanceof List ? new ArrayList<>() : new LinkedHashSet<>();
                        for (Object item : coll) {
                            if (item instanceof Entity) {
                                resultColl.add(Id.of(item));
                            } else {
                                resultColl.add(item);
                            }
                        }
                        builder.withChange(property.getName(), resultColl);
                    } else {
                        builder.withChange(property.getName(), convertValueIfNeeded(property, value));
                    }

                } else {
                    if (value != null) {
                        builder.withChange(property.getName(), null);
                    }
                }
            }
        }

        if (deleted) {
            // todo dynamic attributes
//            addDynamicAttributeChanges(entity, changes, true);
        }

        return builder;
    }

    private Object convertValueIfNeeded(MetaProperty property, Object value) {
        if (property.getRange().isEnum() && !(value instanceof EnumClass)) {
            for (Object enumValue : property.getRange().asEnumeration().getValues()) {
                if (enumValue instanceof EnumClass && ((EnumClass<?>) enumValue).getId().equals(value)) {
                    return enumValue;
                }
            }
        }
        return value;
    }
}

