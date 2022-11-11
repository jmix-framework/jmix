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
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Primary
@Component("core_UnconstrainedDataManager")
public class UnconstrainedDataManagerImpl implements UnconstrainedDataManager {

    private static final Logger log = LoggerFactory.getLogger(UnconstrainedDataManagerImpl.class);

    public static final String SAVE_TX_PREFIX = "DataManager-save-";

    protected static final AtomicLong txCount = new AtomicLong();

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
    protected ObjectProvider<FluentValueLoader> fluentValueLoaderProvider;

    @Autowired
    protected ObjectProvider<FluentValuesLoader> fluentValuesLoaderProvider;

    @Autowired
    protected ObjectProvider<CrossDataStoreReferenceLoader> crossDataStoreReferenceLoaderProvider;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected CoreProperties properties;

    @Autowired
    protected TransactionManagerLocator transactionManagerLocator;

    @Nullable
    @Override
    public <E> E load(LoadContext<E> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));

        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));

        @SuppressWarnings("unchecked")
        E entity = (E) storage.load(context);

        if (entity != null)
            readCrossDataStoreReferences(Collections.singletonList(entity), context.getFetchPlan(), metaClass, context.isJoinTransaction());
        return entity;
    }

    @Override
    public <E> List<E> loadList(LoadContext<E> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));

        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));

        @SuppressWarnings("unchecked")
        List<E> entities = (List<E>) storage.loadList(context);

        readCrossDataStoreReferences(entities, context.getFetchPlan(), metaClass, context.isJoinTransaction());
        return entities;
    }

    @Override
    public long getCount(LoadContext<?> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        DataStore storage = dataStoreFactory.get(getStoreName(metaClass));

        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));

        return storage.getCount(context);
    }

    @Override
    public EntitySet save(Object... entities) {
        return save(new SaveContext().saving(entities));
    }

    @Override
    public <E> E save(E entity) {
        return save(new SaveContext().saving(entity))
                .optional(entity)
                .orElseThrow(() -> new IllegalStateException("Data store didn't return a saved entity"));
    }

    @Override
    public void remove(Object... entities) {
        save(new SaveContext().removing(entities));
    }

    @Override
    public <E> void remove(Id<E> entityId) {
        remove(getReference(entityId));
    }

    @Override
    public EntitySet save(SaveContext context) {
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        Map<String, SaveContext> storeToContextMap = new TreeMap<>();
        Set<Object> toRepeat = new HashSet<>();
        for (Object entity : context.getEntitiesToSave()) {
            MetaClass metaClass = metadata.getClass(entity);
            String storeName = getStoreName(metaClass);

            boolean repeatRequired = writeCrossDataStoreReferences(entity, context.getEntitiesToSave());
            if (repeatRequired) {
                toRepeat.add(entity);
            }

            SaveContext sc = storeToContextMap.computeIfAbsent(storeName, key -> createSaveContext(context));
            sc.saving(entity);
            FetchPlan fetchPlan = context.getFetchPlans().get(entity);
            if (fetchPlan != null)
                sc.getFetchPlans().put(entity, fetchPlan);
        }
        for (Object entity : context.getEntitiesToRemove()) {
            MetaClass metaClass = metadata.getClass(entity);
            String storeName = getStoreName(metaClass);

            SaveContext sc = storeToContextMap.computeIfAbsent(storeName, key -> createSaveContext(context));
            sc.removing(entity);
            FetchPlan fetchPlan = context.getFetchPlans().get(entity);
            if (fetchPlan != null)
                sc.getFetchPlans().put(entity, fetchPlan);
        }

        Map<PlatformTransactionManager, Set<String>> txManagerToStore = new HashMap<>();
        Set<String> storesWithoutTxManager = new TreeSet<>();
        for (String store : storeToContextMap.keySet()) {
            try {
                PlatformTransactionManager transactionManager = transactionManagerLocator.getTransactionManager(store);
                Set<String> stores = txManagerToStore.computeIfAbsent(transactionManager, key -> new TreeSet<>());
                stores.add(store);
            } catch (NoSuchBeanDefinitionException e) {
                storesWithoutTxManager.add(store);
            }
        }

        Set result = new LinkedHashSet<>();
        TransactionDefinition def = createTransactionDefinition(context.isJoinTransaction());
        for (Map.Entry<PlatformTransactionManager, Set<String>> txMapEntry : txManagerToStore.entrySet()) {
            Set<String> stores = txMapEntry.getValue();
            if (stores.size() > 1) {
                PlatformTransactionManager tm = txMapEntry.getKey();
                TransactionStatus transaction = tm.getTransaction(def);
                try {
                    for (String store : stores) {
                        SaveContext sc = storeToContextMap.get(store);
                        boolean joinTransaction = sc.isJoinTransaction();
                        sc.setJoinTransaction(true);
                        result.addAll(saveContextToStore(store, sc));
                        sc.setJoinTransaction(joinTransaction);
                    }
                    tm.commit(transaction);
                } finally {
                    if (!transaction.isCompleted()) {
                        tm.rollback(transaction);
                    }
                }
            } else {
                for (String store : stores) {
                    result.addAll(saveContextToStore(store, storeToContextMap.get(store)));
                }
            }
        }
        for (String store : storesWithoutTxManager) {
            result.addAll(saveContextToStore(store, storeToContextMap.get(store)));
        }

        if (!toRepeat.isEmpty()) {
            SaveContext sc = new SaveContext();
            sc.setJoinTransaction(context.isJoinTransaction());
            for (Object entity : result) {
                if (toRepeat.contains(entity)) {
                    sc.saving(entity, context.getFetchPlans().get(entity));
                }
            }
            Set committedEntities = save(sc);
            for (Object committedEntity : committedEntities) {
                if (result.contains(committedEntity)) {
                    result.remove(committedEntity);
                    result.add(committedEntity);
                }
            }
        }

        return EntitySet.of(result);
    }

    protected TransactionDefinition createTransactionDefinition(boolean isJoinTransaction) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(SAVE_TX_PREFIX + txCount.incrementAndGet());
        if (isJoinTransaction) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        } else {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }
        return def;
    }

    protected Set saveContextToStore(String store, SaveContext context) {
        DataStore dataStore = dataStoreFactory.get(store);
        return dataStore.save(context);
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        DataStore store = dataStoreFactory.get(getStoreName(context.getStoreName()));
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return store.loadValues(context);
    }

    @Override
    public long getCount(ValueLoadContext context) {
        DataStore store = dataStoreFactory.get(getStoreName(context.getStoreName()));
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return store.getCount(context);
    }

    @Override
    public <E> FluentLoader<E> load(Class<E> entityClass) {
        //noinspection unchecked
        FluentLoader<E> fluentLoader = fluentLoaderProvider.getObject(entityClass);
        fluentLoader.setDataManager(this);
        return fluentLoader;
    }

    @Override
    public <E> FluentLoader.ById<E> load(Id<E> entityId) {
        //noinspection unchecked
        FluentLoader<E> fluentLoader = fluentLoaderProvider.getObject(entityId.getEntityClass());
        fluentLoader.setDataManager(this);
        return fluentLoader.id(entityId.getValue());
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        FluentValuesLoader fluentValuesLoader = fluentValuesLoaderProvider.getObject(queryString);
        fluentValuesLoader.setDataManager(this);
        return fluentValuesLoader;
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        //noinspection unchecked
        FluentValueLoader<T> fluentValueLoader = fluentValueLoaderProvider.getObject(queryString, valueClass);
        fluentValueLoader.setDataManager(this);
        return fluentValueLoader;
    }

    protected SaveContext createSaveContext(SaveContext context) {
        SaveContext newCtx = new SaveContext();
        newCtx.setHints(context.getHints());
        newCtx.setDiscardSaved(context.isDiscardSaved());
        newCtx.setAccessConstraints(context.getAccessConstraints());
        newCtx.setJoinTransaction(context.isJoinTransaction());
        return newCtx;
    }

    @Override
    public <T> T create(Class<T> entityClass) {
        return metadata.create(entityClass);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object id) {
        T entity = metadata.create(entityClass);
        EntityValues.setId(entity, id);
        entityStates.makePatch(entity);
        return entity;
    }

    @Override
    public <T> T getReference(Id<T> entityId) {
        Preconditions.checkNotNullArgument(entityId, "entityId is null");
        return getReference(entityId.getEntityClass(), entityId.getValue());
    }

    protected boolean writeCrossDataStoreReferences(Object entity, Collection<Object> allEntities) {
        if (stores.getAdditional().isEmpty())
            return false;

        boolean repeatRequired = false;
        MetaClass metaClass = metadata.getClass(entity);
        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getRange().isClass() && !property.getRange().getCardinality().isMany()) {
                MetaClass propertyMetaClass = property.getRange().asClass();
                if (!Objects.equals(propertyMetaClass.getStore().getName(), metaClass.getStore().getName())) {
                    List<String> dependsOnProperties = metadataTools.getDependsOnProperties(property);
                    if (dependsOnProperties.size() == 0) {
                        continue;
                    }
                    if (dependsOnProperties.size() > 1) {
                        log.warn("More than 1 property is defined for attribute {} in DependsOnProperty annotation, skip handling different data store", property);
                        continue;
                    }
                    String relatedPropertyName = dependsOnProperties.get(0);
                    if (entityStates.isLoaded(entity, relatedPropertyName)) {
                        Object refEntity = EntityValues.getValue(entity, property.getName());
                        if (refEntity == null) {
                            EntityValues.setValue(entity, relatedPropertyName, null);
                        } else {
                            Object refEntityId = EntityValues.getId(refEntity);
                            MetaClass refEntityMetaClass = metadata.getClass(refEntity);
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
                                    EntityValues.setValue(entity, relatedPropertyName, metadataTools.copy(refEntityId));
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

    protected void readCrossDataStoreReferences(Collection<?> entities, FetchPlan fetchPlan, MetaClass metaClass,
                                                boolean joinTransaction) {
        if (stores.getAdditional().isEmpty() || entities.isEmpty() || fetchPlan == null)
            return;

        CrossDataStoreReferenceLoader crossDataStoreReferenceLoader = crossDataStoreReferenceLoaderProvider.getObject(
                metaClass, fetchPlan, joinTransaction);
        crossDataStoreReferenceLoader.processEntities(entities);
    }

    protected String getStoreName(MetaClass metaClass) {
        return metaClass.getStore().getName();
    }

    protected String getStoreName(@Nullable String storeName) {
        return storeName == null ? Stores.NOOP : storeName;
    }

    protected <E> MetaClass getEffectiveMetaClassFromContext(LoadContext<E> context) {
        return extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());
    }

    protected List<AccessConstraint<?>> mergeConstraints(List<AccessConstraint<?>> accessConstraints) {
        if (accessConstraints.isEmpty()) {
            return getAppliedConstraints();
        } else {
            Set<AccessConstraint<?>> newAccessConstraints = new LinkedHashSet<>(getAppliedConstraints());
            newAccessConstraints.addAll(accessConstraints);
            return new ArrayList<>(newAccessConstraints);
        }
    }

    protected List<AccessConstraint<?>> getAppliedConstraints() {
        return Collections.emptyList();
    }
}
