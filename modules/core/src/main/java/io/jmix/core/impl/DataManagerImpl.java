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

package io.jmix.core.impl;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

@Component(DataManager.NAME)
public class DataManagerImpl implements DataManager {

    private static final Logger log = LoggerFactory.getLogger(DataManagerImpl.class);

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected Stores stores;

    @Autowired
    protected DataStoreFactory dataStoreFactory;

    @Autowired
    protected ObjectProvider<FluentLoader> fluentLoaderProvider;

    @Autowired
    protected ObjectProvider<CrossDataStoreReferenceLoader> crossDataStoreReferenceLoaderProvider;

    @Autowired
    protected ExtendedEntities extendedEntities;

    // todo entity log
//    @Autowired
//    protected EntityLogAPI entityLog;

    @Nullable
    @Override
    public <E extends JmixEntity> E load(LoadContext<E> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));
        E entity = storage.load(context);
        if (entity != null)
            readCrossDataStoreReferences(Collections.singletonList(entity), context.getFetchPlan(), metaClass, context.isJoinTransaction());
        return entity;
    }

    @Override
    public <E extends JmixEntity> List<E> loadList(LoadContext<E> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));
        List<E> entities = storage.loadList(context);
        readCrossDataStoreReferences(entities, context.getFetchPlan(), metaClass, context.isJoinTransaction());
        return entities;
    }

    @Override
    public long getCount(LoadContext<? extends JmixEntity> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));
        return storage.getCount(context);
    }

    @Override
    public EntitySet save(JmixEntity... entities) {
        return save(new SaveContext().saving(entities));
    }

    @Override
    public <E extends JmixEntity> E save(E entity) {
        return save(new SaveContext().saving(entity))
                .optional(entity)
                .orElseThrow(() -> new IllegalStateException("Data store didn't return a saved entity"));
    }

    @Override
    public void remove(JmixEntity... entities) {
        save(new SaveContext().removing(entities));
    }

    @Override
    public <E extends JmixEntity> void remove(Id<E> entityId) {
        remove(getReference(entityId));
    }

    @Override
    public EntitySet save(SaveContext context) {
        Map<String, SaveContext> storeToContextMap = new TreeMap<>();
        Set<JmixEntity> toRepeat = new HashSet<>();
        for (JmixEntity entity : context.getEntitiesToSave()) {
            MetaClass metaClass = metadata.getClass(entity.getClass());
            String storeName = getStoreName(metaClass);

            boolean repeatRequired = writeCrossDataStoreReferences(entity, context.getEntitiesToSave());
            if (repeatRequired) {
                toRepeat.add(entity);
            }

            SaveContext sc = storeToContextMap.computeIfAbsent(storeName, key -> createSaveContext(context));
            sc.saving(entity);
            FetchPlan view = context.getFetchPlans().get(entity);
            if (view != null)
                sc.getFetchPlans().put(entity, view);
        }
        for (JmixEntity entity : context.getEntitiesToRemove()) {
            MetaClass metaClass = metadata.getClass(entity.getClass());
            String storeName = getStoreName(metaClass);

            SaveContext sc = storeToContextMap.computeIfAbsent(storeName, key -> createSaveContext(context));
            sc.removing(entity);
            FetchPlan view = context.getFetchPlans().get(entity);
            if (view != null)
                sc.getFetchPlans().put(entity, view);
        }

        Set<JmixEntity> result = new LinkedHashSet<>();
        for (Map.Entry<String, SaveContext> entry : storeToContextMap.entrySet()) {
            DataStore dataStore = dataStoreFactory.get(entry.getKey());
            Set<JmixEntity> committed = dataStore.save(entry.getValue());
            result.addAll(committed);
        }

        if (!toRepeat.isEmpty()) {
            // todo entity log
//            boolean logging = entityLog.isLoggingForCurrentThread();
//            entityLog.processLoggingForCurrentThread(false);
            try {
                SaveContext sc = new SaveContext();
                sc.setJoinTransaction(context.isJoinTransaction());
                for (JmixEntity entity : result) {
                    if (toRepeat.contains(entity)) {
                        sc.saving(entity, context.getFetchPlans().get(entity));
                    }
                }
                Set<JmixEntity> committedEntities = save(sc);
                for (JmixEntity committedEntity : committedEntities) {
                    if (result.contains(committedEntity)) {
                        result.remove(committedEntity);
                        result.add(committedEntity);
                    }
                }
            } finally {
                // todo entity log
//                entityLog.processLoggingForCurrentThread(logging);
            }
        }

        return EntitySet.of(result);
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        DataStore store = dataStoreFactory.get(getStoreName(context.getStoreName()));
        return store.loadValues(context);
    }

    @Override
    public <E extends JmixEntity> FluentLoader<E> load(Class<E> entityClass) {
        return fluentLoaderProvider.getObject(entityClass);
    }

    @Override
    public <E extends JmixEntity> FluentLoader.ById<E> load(Id<E> entityId) {
        return fluentLoaderProvider.getObject(entityId.getEntityClass())
                .id(entityId.getValue());
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        return new FluentValuesLoader(queryString, this);
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        return new FluentValueLoader<>(queryString, valueClass, this);
    }

    protected SaveContext createSaveContext(SaveContext context) {
        SaveContext newCtx = new SaveContext();
        newCtx.setSoftDeletion(context.isSoftDeletion());
        newCtx.setDiscardSaved(context.isDiscardSaved());
        newCtx.setAccessConstraints(context.getAccessConstraints());
        newCtx.setJoinTransaction(context.isJoinTransaction());
        return newCtx;
    }

    @Override
    public <T extends JmixEntity> T create(Class<T> entityClass) {
        return metadata.create(entityClass);
    }

    @Override
    public <T extends JmixEntity> T getReference(Class<T> entityClass, Object id) {
        T entity = metadata.create(entityClass);
        EntityValues.setId(entity, id);
        entityStates.makePatch(entity);
        return entity;
    }

    @Override
    public <T extends JmixEntity> T getReference(Id<T> entityId) {
        Preconditions.checkNotNullArgument(entityId, "entityId is null");
        return getReference(entityId.getEntityClass(), entityId.getValue());
    }

    protected boolean writeCrossDataStoreReferences(JmixEntity entity, Collection<JmixEntity> allEntities) {
        if (stores.getAdditional().isEmpty())
            return false;

        boolean repeatRequired = false;
        MetaClass metaClass = metadata.getClass(entity.getClass());
        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getRange().isClass() && !property.getRange().getCardinality().isMany()) {
                MetaClass propertyMetaClass = property.getRange().asClass();
                if (!Objects.equals(metadataTools.getStoreName(propertyMetaClass), metadataTools.getStoreName(metaClass))) {
                    List<String> relatedProperties = metadataTools.getRelatedProperties(property);
                    if (relatedProperties.size() == 0) {
                        continue;
                    }
                    if (relatedProperties.size() > 1) {
                        log.warn("More than 1 related property is defined for attribute {}, skip handling different data store", property);
                        continue;
                    }
                    String relatedPropertyName = relatedProperties.get(0);
                    if (entityStates.isLoaded(entity, relatedPropertyName)) {
                        JmixEntity refEntity = EntityValues.getValue(entity, property.getName());
                        if (refEntity == null) {
                            EntityValues.setValue(entity, relatedPropertyName, null);
                        } else {
                            Object refEntityId = EntityValues.getId(refEntity);
                            MetaClass refEntityMetaClass = metadata.getClass(refEntity.getClass());
                            if (refEntityId == null) {
                                Object refEntityGeneratedId = EntityValues.getGeneratedId(refEntity);
                                if (allEntities.stream().anyMatch(e -> EntityValues.getGeneratedId(e).equals(refEntityGeneratedId))) {
                                    repeatRequired = true;
                                } else {
                                    log.warn("No entity with generated ID={} in the context, skip handling different data store", refEntityGeneratedId);
                                }
                            } else if (metadataTools.hasCompositePrimaryKey(refEntityMetaClass)) {
                                MetaProperty relatedProperty = metaClass.getProperty(relatedPropertyName);
                                if (!relatedProperty.getRange().isClass()) {
                                    log.warn("PK of entity referenced by {} is a EmbeddableEntity, but related property {} is not", property, relatedProperty);
                                } else {
                                    EntityValues.setValue(entity, relatedPropertyName, metadataTools.copy((JmixEntity) refEntityId));
                                }
                            } else {
                                EntityValues.setValue(entity, relatedPropertyName, refEntityId);
                            }
                        }
                    }
                }
            }
        }
        return repeatRequired;
    }

    protected void readCrossDataStoreReferences(Collection<? extends JmixEntity> entities, FetchPlan view, MetaClass metaClass,
                                                boolean joinTransaction) {
        if (stores.getAdditional().isEmpty() || entities.isEmpty() || view == null)
            return;

        CrossDataStoreReferenceLoader crossDataStoreReferenceLoader = crossDataStoreReferenceLoaderProvider.getObject(
                metaClass, view, joinTransaction);
        crossDataStoreReferenceLoader.processEntities(entities);
    }

    protected String getStoreName(MetaClass metaClass) {
        return metaClass.getStore().getName();
    }

    protected String getStoreName(@Nullable String storeName) {
        return storeName == null ? Stores.NOOP : storeName;
    }

    protected <E extends JmixEntity> MetaClass getEffectiveMetaClassFromContext(LoadContext<E> context) {
        return extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());
    }
}
