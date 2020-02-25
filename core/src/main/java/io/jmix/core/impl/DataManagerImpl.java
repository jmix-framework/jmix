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
import io.jmix.core.entity.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

@Component(DataManager.NAME)
public class DataManagerImpl implements DataManager {

    private static final Logger log = LoggerFactory.getLogger(DataManagerImpl.class);

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected EntityStates entityStates;

    @Inject
    protected Stores stores;

    @Inject
    protected DataStoreFactory dataStoreFactory;

    // todo entity log
//    @Inject
//    protected EntityLogAPI entityLog;

    @Nullable
    @Override
    public <E extends Entity> E load(LoadContext<E> context) {
        MetaClass metaClass = metadata.getClass(context.getMetaClass());
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));
        E entity = storage.load(context);
        if (entity != null)
            readCrossDataStoreReferences(Collections.singletonList(entity), context.getFetchPlan(), metaClass, context.isJoinTransaction());
        return entity;
    }

    @Override
    public <E extends Entity> List<E> loadList(LoadContext<E> context) {
        MetaClass metaClass = metadata.getClass(context.getMetaClass());
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));
        List<E> entities = storage.loadList(context);
        readCrossDataStoreReferences(entities, context.getFetchPlan(), metaClass, context.isJoinTransaction());
        return entities;
    }

    @Override
    public long getCount(LoadContext<? extends Entity> context) {
        MetaClass metaClass = metadata.getClass(context.getMetaClass());
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));
        return storage.getCount(context);
    }

    @Override
    public EntitySet save(Entity... entities) {
        return save(new SaveContext().saving(entities));
    }

    @Override
    public <E extends Entity> E save(E entity) {
        return save(new SaveContext().saving(entity))
                .optional(entity)
                .orElseThrow(() -> new IllegalStateException("Data store didn't return a saved entity"));
    }

    @Override
    public void remove(Entity... entities) {
        save(new SaveContext().removing(entities));
    }

    @Override
    public EntitySet save(SaveContext context) {
        Map<String, SaveContext> storeToContextMap = new TreeMap<>();
        Set<Entity> toRepeat = new HashSet<>();
        for (Entity entity : context.getEntitiesToSave()) {
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
        for (Entity entity : context.getEntitiesToRemove()) {
            MetaClass metaClass = metadata.getClass(entity.getClass());
            String storeName = getStoreName(metaClass);

            SaveContext sc = storeToContextMap.computeIfAbsent(storeName, key -> createSaveContext(context));
            sc.removing(entity);
            FetchPlan view = context.getFetchPlans().get(entity);
            if (view != null)
                sc.getFetchPlans().put(entity, view);
        }

        Set<Entity> result = new LinkedHashSet<>();
        for (Map.Entry<String, SaveContext> entry : storeToContextMap.entrySet()) {
            DataStore dataStore = dataStoreFactory.get(entry.getKey());
            Set<Entity> committed = dataStore.save(entry.getValue());
            result.addAll(committed);
        }

        if (!toRepeat.isEmpty()) {
            // todo entity log
//            boolean logging = entityLog.isLoggingForCurrentThread();
//            entityLog.processLoggingForCurrentThread(false);
            try {
                SaveContext sc = new SaveContext();
                sc.setJoinTransaction(context.isJoinTransaction());
                for (Entity entity : result) {
                    if (toRepeat.contains(entity)) {
                        sc.saving(entity, context.getFetchPlans().get(entity));
                    }
                }
                Set<Entity> committedEntities = save(sc);
                for (Entity committedEntity : committedEntities) {
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

    protected SaveContext createSaveContext(SaveContext context) {
        SaveContext newCtx = new SaveContext();
        newCtx.setSoftDeletion(context.isSoftDeletion());
        newCtx.setDiscardSaved(context.isDiscardSaved());
        newCtx.setAuthorizationRequired(context.isAuthorizationRequired());
        newCtx.setJoinTransaction(context.isJoinTransaction());
        return newCtx;
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        return metadata.create(entityClass);
    }

    @Override
    public <T extends BaseGenericIdEntity<K>, K> T getReference(Class<T> entityClass, K id) {
        T entity = metadata.create(entityClass);
        entity.setId(id);
        entityStates.makePatch(entity);
        return entity;
    }

    protected boolean writeCrossDataStoreReferences(Entity entity, Collection<Entity> allEntities) {
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
                        Entity refEntity = entity.getValue(property.getName());
                        if (refEntity == null) {
                            entity.setValue(relatedPropertyName, null);
                        } else {
                            Object refEntityId = refEntity.getId();
                            if (refEntityId instanceof IdProxy) {
                                Object realId = ((IdProxy) refEntityId).get();
                                if (realId == null) {
                                    if (allEntities.stream().anyMatch(e -> e.getId().equals(refEntityId))) {
                                        repeatRequired = true;
                                    } else {
                                        log.warn("No entity with ID={} in the context, skip handling different data store", refEntityId);
                                    }
                                } else {
                                    entity.setValue(relatedPropertyName, realId);
                                }
                            } else if (refEntityId instanceof EmbeddableEntity) {
                                MetaProperty relatedProperty = metaClass.getProperty(relatedPropertyName);
                                if (!relatedProperty.getRange().isClass()) {
                                    log.warn("PK of entity referenced by {} is a EmbeddableEntity, but related property {} is not", property, relatedProperty);
                                } else {
                                    entity.setValue(relatedPropertyName, metadataTools.copy((Entity) refEntityId));
                                }
                            } else {
                                entity.setValue(relatedPropertyName, refEntityId);
                            }
                        }
                    }
                }
            }
        }
        return repeatRequired;
    }

    protected void readCrossDataStoreReferences(Collection<? extends Entity> entities, FetchPlan view, MetaClass metaClass,
                                                boolean joinTransaction) {
        if (stores.getAdditional().isEmpty() || entities.isEmpty() || view == null)
            return;

        CrossDataStoreReferenceLoader crossDataStoreReferenceLoader = AppBeans.getPrototype(
                CrossDataStoreReferenceLoader.NAME, metaClass, view, joinTransaction);
        crossDataStoreReferenceLoader.processEntities(entities);
    }

    protected String getStoreName(MetaClass metaClass) {
        return metaClass.getStore().getName();
    }

    protected String getStoreName(@Nullable String storeName) {
        return storeName == null ? Stores.NOOP : storeName;
    }
}
