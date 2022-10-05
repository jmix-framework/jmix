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
package io.jmix.audit.impl;

import com.google.common.base.Strings;
import io.jmix.audit.AuditProperties;
import io.jmix.audit.EntityLog;
import io.jmix.audit.entity.EntityLogAttr;
import io.jmix.audit.entity.EntityLogItem;
import io.jmix.audit.entity.LoggedAttribute;
import io.jmix.audit.entity.LoggedEntity;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.EntityOp;
import io.jmix.data.AttributeChangesProvider;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.impl.EntityEventManager;
import io.jmix.data.impl.JpaLifecycleListener;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("audit_EntityLog")
public class EntityLogImpl implements EntityLog, JpaLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(EntityLogImpl.class);

    public static final String RESOURCE_HOLDER_KEY = EntityLogResourceHolder.class.getName();

    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected AuditInfoProvider auditInfoProvider;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected Stores stores;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected AttributeChangesProvider attributeChangesProvider;
    @Autowired
    protected EntityEventManager entityEventManager;

    @PersistenceContext
    protected EntityManager entityManager;

    protected TransactionTemplate transaction;

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected AuditProperties properties;

    protected volatile boolean enabled;
    protected volatile boolean loaded;

    @GuardedBy("lock")
    protected Map<String, Set<String>> entitiesManual;
    @GuardedBy("lock")
    protected Map<String, Set<String>> entitiesAuto;

    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected ThreadLocal<Boolean> entityLogSwitchedOn = new ThreadLocal<>();

    @Autowired
    public void setProperties(AuditProperties properties) {
        this.properties = properties;
        this.enabled = properties.isEnabled();
    }

    @Override
    public void processLoggingForCurrentThread(boolean enabled) {
        entityLogSwitchedOn.set(enabled);
    }

    @Override
    public boolean isLoggingForCurrentThread() {
        return !Boolean.FALSE.equals(entityLogSwitchedOn.get());
    }

    @Override
    public void onEntityChange(Object entity, EntityOp entityOp, @Nullable AttributeChanges changes) {
        if (entity instanceof EntityLogItem) {
            return;
        }
        switch (entityOp) {
            case CREATE:
                registerCreate(entity, true);
                break;
            case UPDATE:
                registerModify(entity, true, changes);
                break;
            case DELETE:
                registerDelete(entity, true);
                break;
            default:
                //do nothing
        }
    }

    @Override
    public void onFlush(String storeName) {
        flush(storeName);
    }

    @Override
    public void flush(String storeName) {
        EntityLogResourceHolder holder = getEntityLogResourceHolder();
        List<EntityLogItem> items = holder.getItems(storeName);
        if (items == null || items.isEmpty())
            return;

        Set<EntityLogItem> saved = new LinkedHashSet<>();
        for (EntityLogItem item : items) {
            List<EntityLogItem> sameEntityList = items.stream()
                    .filter(entityLogItem -> entityLogItem.getDbGeneratedIdEntity() != null ?
                            entityLogItem.getDbGeneratedIdEntity().equals(item.getDbGeneratedIdEntity()) :
                            entityLogItem.getEntityRef().getObjectEntityId().equals(item.getEntityRef().getObjectEntityId()))
                    .collect(Collectors.toList());
            EntityLogItem itemToSave = sameEntityList.get(0);
            if (!saved.contains(itemToSave)) {
                computeChanges(itemToSave, sameEntityList);
                saved.add(itemToSave);
                saveItem(itemToSave);
            }
        }
    }

    protected void computeChanges(EntityLogItem itemToSave, List<EntityLogItem> sameEntityList) {
        Set<String> attributes = sameEntityList.stream()
                .flatMap(entityLogItem -> entityLogItem.getAttributes().stream().map(EntityLogAttr::getName))
                .collect(Collectors.toSet());

        processAttributes(itemToSave, sameEntityList, attributes);

        Properties properties = new Properties();

        for (EntityLogAttr attr : itemToSave.getAttributes()) {
            properties.setProperty(attr.getName(), Strings.nullToEmpty(attr.getValue()));
            if (attr.getValueId() != null) {
                properties.setProperty(attr.getName() + EntityLogAttr.VALUE_ID_SUFFIX, attr.getValueId());
            }
            if (attr.getOldValue() != null) {
                properties.setProperty(attr.getName() + EntityLogAttr.OLD_VALUE_SUFFIX, attr.getOldValue());
            }
            if (attr.getOldValueId() != null) {
                properties.setProperty(attr.getName() + EntityLogAttr.OLD_VALUE_ID_SUFFIX, attr.getOldValueId());
            }
            if (attr.getMessagesPack() != null) {
                properties.setProperty(attr.getName() + EntityLogAttr.MP_SUFFIX, attr.getMessagesPack());
            }
        }

        if (itemToSave.getType() == EntityLogItem.Type.MODIFY) {
            sameEntityList.stream()
                    .filter(entityLogItem -> entityLogItem.getType() == EntityLogItem.Type.CREATE)
                    .findFirst()
                    .ifPresent(entityLogItem -> itemToSave.setType(EntityLogItem.Type.CREATE));
        }
        itemToSave.setChanges(getChanges(properties));
    }

    protected void processAttributes(EntityLogItem itemToSave, List<EntityLogItem> sameEntityList, Set<String> attributes) {
        for (String attributeName : attributes) {
            // old value from the first item
            sameEntityList.get(0).getAttributes().stream()
                    .filter(entityLogAttr -> entityLogAttr.getName().equals(attributeName))
                    .findFirst()
                    .ifPresent(entityLogAttr -> setAttributeOldValue(entityLogAttr, itemToSave));
            // new value from the last item
            sameEntityList.get(sameEntityList.size() - 1).getAttributes().stream()
                    .filter(entityLogAttr -> entityLogAttr.getName().equals(attributeName))
                    .findFirst()
                    .ifPresent(entityLogAttr -> setAttributeNewValue(entityLogAttr, itemToSave));
        }
    }

    protected void setAttributeOldValue(EntityLogAttr entityLogAttr, EntityLogItem itemToSave) {
        EntityLogAttr attr = getAttrToSave(entityLogAttr, itemToSave);
        attr.setOldValue(entityLogAttr.getOldValue());
        attr.setOldValueId(entityLogAttr.getOldValueId());
    }

    protected void setAttributeNewValue(EntityLogAttr entityLogAttr, EntityLogItem itemToSave) {
        EntityLogAttr attr = getAttrToSave(entityLogAttr, itemToSave);
        attr.setValue(entityLogAttr.getValue());
        attr.setValueId(entityLogAttr.getValueId());
    }

    protected EntityLogAttr getAttrToSave(EntityLogAttr entityLogAttr, EntityLogItem itemToSave) {
        EntityLogAttr attr = itemToSave.getAttributes().stream()
                .filter(a -> a.getName().equals(entityLogAttr.getName()))
                .findFirst()
                .orElse(null);
        if (attr == null) {
            attr = metadata.create(EntityLogAttr.class);
            attr.setName(entityLogAttr.getName());
            itemToSave.getAttributes().add(attr);
        }
        return attr;
    }

    protected void saveItem(EntityLogItem item) {
        String storeName = metadata.getClass(item.getEntity()).getStore().getName();

        entityEventManager.publishEntitySavingEvent(item, true);//workaround for jmix-framework/jmix#1069
        if (item.getDbGeneratedIdEntity() == null) {
            if (Stores.isMain(storeName)) {
                entityManager.persist(item);
            } else {
                // Create a new transaction in main DB if we are saving an entity from additional data store
                transaction.executeWithoutResult(transactionStatus -> entityManager.persist(item));
            }
        } else {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    Object id = EntityValues.getId(item.getDbGeneratedIdEntity());
                    item.getEntityRef().setObjectEntityId(id);
                    transaction.executeWithoutResult(status -> {
                        entityManager.persist(item);
                    });
                }
            });
        }
    }

    @Override
    public synchronized boolean isEnabled() {
        return enabled && isLoggingForCurrentThread();
    }

    @Override
    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void invalidateCache() {
        lock.writeLock().lock();
        try {
            log.debug("Invalidating cache");
            entitiesManual = null;
            entitiesAuto = null;
            loaded = false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nullable
    protected Set<String> getLoggedAttributes(String entity, boolean auto) {
        lock.readLock().lock();
        try {
            if (!loaded) {
                // upgrade lock
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    if (!loaded) { // recheck because we unlocked for a while
                        loadEntities();
                        loaded = true;
                    }
                } finally {
                    // downgrade lock
                    lock.writeLock().unlock();
                    lock.readLock().lock();
                }
            }

            Set<String> attributes;
            if (auto)
                attributes = entitiesAuto.get(entity);
            else
                attributes = entitiesManual.get(entity);

            return attributes == null ? null : Collections.unmodifiableSet(attributes);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void loadEntities() {
        log.debug("Loading entities");
        entitiesManual = new HashMap<>();
        entitiesAuto = new HashMap<>();
        transaction.executeWithoutResult(status -> {
            TypedQuery<LoggedEntity> q = entityManager.createQuery(
                    "select e from audit_LoggedEntity e where e.auto = true or e.manual = true",
                    LoggedEntity.class);
            List<LoggedEntity> list = q.getResultList();
            for (LoggedEntity loggedEntity : list) {
                if (loggedEntity.getName() == null) {
                    throw new IllegalStateException("Unable to initialize EntityLog: empty LoggedEntity.name");
                }
                Set<String> attributes = new HashSet<>();
                for (LoggedAttribute loggedAttribute : loggedEntity.getAttributes()) {
                    if (loggedAttribute.getName() == null) {
                        throw new IllegalStateException("Unable to initialize EntityLog: empty LoggedAttribute.name");
                    }
                    attributes.add(loggedAttribute.getName());
                }
                if (BooleanUtils.isTrue(loggedEntity.getAuto()))
                    entitiesAuto.put(loggedEntity.getName(), attributes);
                if (BooleanUtils.isTrue(loggedEntity.getManual()))
                    entitiesManual.put(loggedEntity.getName(), attributes);
            }
        });
        log.debug("Loaded: entitiesAuto={}, entitiesManual={}", entitiesAuto.size(), entitiesManual.size());
    }

    protected String getEntityName(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        return extendedEntities.getOriginalOrThisMetaClass(metaClass).getName();
    }

    protected boolean doNotRegister(@Nullable Object entity) {
        if (entity == null) {
            return true;
        }
        if (entity instanceof EntityLogItem) {
            return true;
        }
        if (metadataTools.hasCompositePrimaryKey(metadata.getClass(entity))
                && !EntityValues.isUuidSupported(entity)) {
            return true;
        }
        return !isEnabled();
    }

    @Override
    public void registerCreate(Object entity) {
        registerCreate(entity, false);
    }

    @Override
    public void registerCreate(Object entity, boolean auto) {
        try {
            if (doNotRegister(entity))
                return;
            String entityName = getEntityName(entity);

            Set<String> attributes = getLoggedAttributes(entityName, auto);
            if (attributes != null && attributes.contains("*")) {
                attributes = getAllAttributes(entity);
            }
            if (attributes == null) {
                return;
            }

            MetaClass metaClass = metadata.getClass(entityName);
            attributes = filterRemovedAttributes(entity, attributes);
            String storeName = metaClass.getStore().getName();
            internalRegisterCreate(entity, entityName, storeName, attributes);
        } catch (Exception e) {
            logError(entity, e);
        }
    }

    protected Set<String> filterRemovedAttributes(Object entity, Set<String> attributes) {
        MetaClass metaClass = metadata.getClass(entity);
        // filter attributes that do not exists in entity anymore
        return attributes.stream()
                .filter(attributeName -> metadataTools.isAdditionalProperty(metaClass, attributeName)
                        || metaClass.getPropertyPath(attributeName) != null)
                .collect(Collectors.toSet());
    }

    protected void internalRegisterCreate(Object entity, String entityName, String storeName, Set<String> attributes) {
        EntityLogItem item;
        // Create a new transaction in main DB if we are saving an entity from additional data store
        if (!Stores.isMain(storeName)) {
            item = transaction.execute(status ->
                    generateEntityLogItem(entity, entityName, attributes, EntityLogItem.Type.CREATE));
        } else {
            item = generateEntityLogItem(entity, entityName, attributes, EntityLogItem.Type.CREATE);
        }
        assert item != null;
        enqueueItem(item, storeName);
    }

    protected String findUsername() {
        UserDetails currentUser = auditInfoProvider.getCurrentUser();
        if (currentUser != null)
            return currentUser.getUsername();
        else {
            String username = properties.getSystemUsername();
            if (username != null)
                return username;
            else
                throw new RuntimeException("The user '" + username + "' specified in jmix.audit.systemUsername does not exist");
        }
    }

    protected void enqueueItem(EntityLogItem item, String storeName) {
        if (item == null)
            return;

        EntityLogResourceHolder holder = getEntityLogResourceHolder();
        List<EntityLogItem> items = holder.getItems(storeName);
        if (items == null) {
            items = new ArrayList<>();
            holder.setItems(items, storeName);
        }
        items.add(item);
    }

    protected EntityLogResourceHolder getEntityLogResourceHolder() {
        EntityLogResourceHolder holder =
                (EntityLogResourceHolder) TransactionSynchronizationManager.getResource(RESOURCE_HOLDER_KEY);
        if (holder == null) {
            holder = new EntityLogResourceHolder();
            TransactionSynchronizationManager.bindResource(RESOURCE_HOLDER_KEY, holder);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive() && !holder.isSynchronizedWithTransaction()) {
            holder.setSynchronizedWithTransaction(true);
            TransactionSynchronizationManager.registerSynchronization(
                    new EntityLogResourceHolderSynchronization(holder, RESOURCE_HOLDER_KEY));
        }
        return holder;
    }

    @Override
    public void registerModify(Object entity) {
        registerModify(entity, false);
    }

    @Override
    public void registerModify(Object entity, boolean auto) {
        registerModify(entity, auto, null);
    }

    @Override
    public void registerModify(Object entity, boolean auto, @Nullable AttributeChanges changes) {
        try {
            if (doNotRegister(entity))
                return;

            String entityName = getEntityName(entity);
            Set<String> attributes = getLoggedAttributes(entityName, auto);
            if (attributes != null && attributes.contains("*")) {
                attributes = getAllAttributes(entity);
            }
            if (attributes == null) {
                return;
            }

            MetaClass metaClass = metadata.getClass(entityName);
            attributes = filterRemovedAttributes(entity, attributes);

            String storeName = metaClass.getStore().getName();
            EntityLogItem item;
            // Create a new transaction in main DB if we are saving an entity from additional data store
            if (!Stores.isMain(storeName)) {
                Set<String> finalAttributes = attributes;
                item = transaction.execute(status ->
                        internalRegisterModify(entity, changes, metaClass, storeName, finalAttributes));
            } else {
                item = internalRegisterModify(entity, changes, metaClass, storeName, attributes);
            }
            enqueueItem(item, storeName);
        } catch (Exception e) {
            logError(entity, e);
        }
    }

    protected EntityLogItem internalRegisterModify(Object entity, @Nullable AttributeChanges changes, MetaClass metaClass,
                                                   String storeName, Set<String> attributes) {
        EntityLogItem item = null;

        Date ts = timeSource.currentTimestamp();

        Set<String> dirty = calculateDirtyFields(entity, changes);
        Set<EntityLogAttr> entityLogAttrs;
        EntityLogItem.Type type;
        if (isSoftDeleteEntityRestored(entity, dirty)) {
            type = EntityLogItem.Type.RESTORE;
            entityLogAttrs = createLogAttributes(entity, attributes, type, changes);
        } else {
            type = EntityLogItem.Type.MODIFY;
            Set<String> dirtyAttributes = new HashSet<>();
            for (String attributePath : attributes) {
                if (metadataTools.isAdditionalProperty(metaClass, attributePath)) {
                    if (dirty.contains(attributePath)) {
                        dirtyAttributes.add(attributePath);
                    }
                    continue;
                }

                MetaPropertyPath propertyPath = metaClass.getPropertyPath(attributePath);
                Preconditions.checkNotNullArgument(propertyPath,
                        "Property path %s isn't exists for type %s", attributePath, metaClass.getName());
                if (dirty.contains(attributePath)) {
                    dirtyAttributes.add(attributePath);
                } else if (!stores.getAdditional().isEmpty()) {
                    String idAttributePath = getIdAttributePath(propertyPath, storeName);
                    if (idAttributePath != null && dirty.contains(idAttributePath)) {
                        dirtyAttributes.add(attributePath);
                    }
                }
            }
            entityLogAttrs = createLogAttributes(entity, dirtyAttributes, type, changes);
        }
        if (!entityLogAttrs.isEmpty() || type == EntityLogItem.Type.RESTORE) {
            item = metadata.create(EntityLogItem.class);
            item.setEventTs(ts);
            item.setUsername(findUsername());
            item.setType(type);
            item.setEntity(extendedEntities.getOriginalOrThisMetaClass(metaClass).getName());
            item.setEntityInstanceName(metadataTools.getInstanceName(entity));
            item.getEntityRef().setObjectEntityId(referenceToEntitySupport.getReferenceId(entity));
            item.setAttributes(entityLogAttrs);
        }

        return item;
    }

    protected Set<EntityLogAttr> createLogAttributes(Object entity, Set<String> attributes, EntityLogItem.Type type,
                                                     @Nullable AttributeChanges changes) {
        Set<EntityLogAttr> result = new HashSet<>();
        for (String name : attributes) {

            EntityLogAttr attr = metadata.create(EntityLogAttr.class);
            attr.setName(name);

            MetaPropertyPath propertyPath = metadata.getClass(entity).getPropertyPath(name);
            MetaProperty metaProperty = propertyPath == null ? null : propertyPath.getMetaProperty();

            String value = stringify(EntityValues.getValueEx(entity, name), metaProperty);
            Object valueId = getValueId(EntityValues.getValueEx(entity, name));
            if (EntityLogItem.Type.DELETE == type) {
                attr.setOldValue(value);
                if (valueId != null)
                    attr.setOldValueId(valueId.toString());
            } else {
                attr.setValue(value);
                if (valueId != null)
                    attr.setValueId(valueId.toString());

                if (changes != null) {
                    Object oldValue = changes.getOldValue(name);
                    attr.setOldValue(stringify(oldValue, metaProperty));
                    Object oldValueId = getValueId(oldValue);
                    if (oldValueId != null) {
                        attr.setOldValueId(oldValueId.toString());
                    }
                }
            }

//            TODO: enable if @LocalizedValue would be supported
//            if (metadata.getClass(entity).getProperty(name) != null) {
//                //skip embedded properties
//                MessageTools messageTools = AppBeans.get(MessageTools.NAME);
//                String mp = messageTools.inferMessagePack(name, entity);
//                if (mp != null)
//                    attr.setMessagesPack(mp);
//            }
            result.add(attr);
        }
        return result;
    }

    protected String getChanges(Properties properties) {
        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, null);
            String changes = writer.toString();
            if (changes.startsWith("#"))
                changes = changes.substring(changes.indexOf("\n") + 1); // cut off comments line
            return changes;
        } catch (IOException e) {
            throw new RuntimeException("Error writing entity log attributes", e);
        }
    }

    @Override
    public void registerDelete(Object entity) {
        registerDelete(entity, false);
    }

    @Override
    public void registerDelete(Object entity, boolean auto) {
        try {
            if (doNotRegister(entity))
                return;

            String entityName = getEntityName(entity);
            Set<String> attributes = getLoggedAttributes(entityName, auto);
            if (attributes != null && attributes.contains("*")) {
                attributes = getAllAttributes(entity);
            }
            if (attributes == null) {
                return;
            }

            MetaClass metaClass = metadata.getClass(entityName);
            attributes = filterRemovedAttributes(entity, attributes);
            String storeName = metaClass.getStore().getName();
            internalRegisterDelete(entity, entityName, storeName, attributes);
        } catch (Exception e) {
            logError(entity, e);
        }
    }

    protected void internalRegisterDelete(Object entity, String entityName, String storeName, Set<String> attributes) {
        EntityLogItem item;
        // Create a new transaction in main DB if we are saving an entity from additional data store
        if (!Stores.isMain(storeName)) {
            item = transaction.execute(status ->
                    generateEntityLogItem(entity, entityName, attributes, EntityLogItem.Type.DELETE));
        } else {
            item = generateEntityLogItem(entity, entityName, attributes, EntityLogItem.Type.DELETE);
        }
        assert item != null;
        enqueueItem(item, storeName);
    }

    protected EntityLogItem generateEntityLogItem(Object entity, String entityName, Set<String> attributes,
                                                  EntityLogItem.Type type) {
        EntityLogItem item;
        Date ts = timeSource.currentTimestamp();

        item = metadata.create(EntityLogItem.class);
        item.setEventTs(ts);
        item.setUsername(findUsername());
        item.setType(type);
        item.setEntity(entityName);
        item.setEntityInstanceName(metadataTools.getInstanceName(entity));
        if (metadataTools.hasDbGeneratedPrimaryKey(metadata.getClass(entity)) && EntityLogItem.Type.CREATE.equals(type)) {
            item.setDbGeneratedIdEntity(entity);
        } else {
            item.getEntityRef().setObjectEntityId(referenceToEntitySupport.getReferenceId(entity));
        }
        item.setAttributes(createLogAttributes(entity, attributes, type, null));
        return item;
    }

    protected Set<String> getAllAttributes(Object entity) {
        if (entity == null) {
            return null;
        }
        Set<String> attributes = new HashSet<>();
        MetaClass metaClass = metadata.getClass(entity);
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            Range range = metaProperty.getRange();
            if (range.isClass() && range.getCardinality().isMany()) {
                continue;
            }
            attributes.add(metaProperty.getName());
        }

        for (MetaProperty object : metadataTools.getAdditionalProperties(metaClass)) {
            attributes.add(object.getName());
        }
        return attributes;
    }

    @Nullable
    protected Object getValueId(@Nullable Object value) {
        if (value instanceof Entity) {
            if (EntitySystemAccess.isEmbeddable(value)) {
                return null;
            } else {
                return referenceToEntitySupport.getReferenceId(value);
            }
        } else {
            return null;
        }
    }

    protected String stringify(@Nullable Object value, @Nullable MetaProperty metaProperty) {
        if (value == null)
            return "";
        else if (value instanceof Entity) {
            return metadataTools.getInstanceName(value);
        } else if (value instanceof Date) {
            Datatype datatype;
            if (metaProperty != null) {
                datatype = metaProperty.getRange().asDatatype();
            } else {
                datatype = datatypeRegistry.get(value.getClass());
            }
            return datatype.format(value);
        } else if (value instanceof Iterable) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Object obj : (Iterable) value) {
                sb.append(stringify(obj, metaProperty)).append(",");
            }
            if (sb.length() > 1)
                sb.deleteCharAt(sb.length() - 1);
            sb.append("]");
            return sb.toString();
        } else if (metaProperty != null && metaProperty.getRange().isEnum() && EnumClass.class.isAssignableFrom(metaProperty.getJavaType())) {
            Class enumClass = metaProperty.getJavaType();
            try {
                Enum e = Enum.valueOf(enumClass, String.valueOf(value));
                return ((EnumClass) e).getId().toString();
            } catch (IllegalArgumentException e) {
                return String.valueOf(value);
            }
        } else {
            return String.valueOf(value);
        }
    }

    protected Set<String> calculateDirtyFields(Object entity, @Nullable AttributeChanges changes) {
        if (changes == null) {
            if (!entityStates.isManaged(entity)) {
                return Collections.emptySet();
            }
            AttributeChanges calculatedChanges = attributeChangesProvider.getAttributeChanges(entity);
            return calculatedChanges.getAttributes();
        } else {
            return changes.getAttributes();
        }
    }

    @Nullable
    protected String getIdAttributePath(MetaPropertyPath propertyPath, String storeName) {
        String idAttribute = metadataTools.getCrossDataStoreReferenceIdProperty(storeName, propertyPath.getMetaProperty());
        if (idAttribute != null) {
            List<String> parts = Stream.of(propertyPath.getMetaProperties())
                    .map(MetaProperty::getName)
                    .collect(Collectors.toList());
            parts.set(parts.size() - 1, idAttribute);
            return String.join(".", parts);
        }
        return null;
    }


    private boolean isSoftDeleteEntityRestored(Object entity, Set<String> dirty) {
        return EntityValues.isSoftDeletionSupported(entity)
                && dirty.contains(metadataTools.getDeletedDateProperty(entity))
                && !EntityValues.isSoftDeleted(entity);
    }

    protected void logError(Object entity, Exception e) {
        log.warn("Unable to log entity {}, id={}", entity, EntityValues.getId(entity), e);
    }

    public static class EntityLogResourceHolder extends ResourceHolderSupport {

        protected Map<String, List<EntityLogItem>> itemsMap = new HashMap<>();

        @Nullable
        protected List<EntityLogItem> getItems(String storeName) {
            return itemsMap.get(storeName);
        }

        protected void setItems(List<EntityLogItem> items, String storeName) {
            itemsMap.put(storeName, items);
        }

        protected void clearStoreItems(String storeName) {
            List<EntityLogItem> items = itemsMap.get(storeName);
            if (items != null) {
                items.clear();
            }
        }

        protected void clearItems() {
            itemsMap.clear();
        }
    }

    public static class EntityLogResourceHolderSynchronization
            extends ResourceHolderSynchronization<EntityLogResourceHolder, String> {

        public EntityLogResourceHolderSynchronization(EntityLogResourceHolder resourceHolder, String resourceKey) {
            super(resourceHolder, resourceKey);
        }

        @Override
        protected void cleanupResource(EntityLogResourceHolder resourceHolder, String resourceKey, boolean committed) {
            resourceHolder.clearItems();
        }
    }
}
