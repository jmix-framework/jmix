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

package io.jmix.data.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.*;
import io.jmix.data.DataProperties;
import io.jmix.data.EntityFetcher;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.accesscontext.LoadValuesAccessContext;
import io.jmix.data.accesscontext.ReadEntityQueryContext;
import io.jmix.data.persistence.DbmsSpecifics;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.internal.helper.CubaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.jmix.core.entity.EntityValues.getId;
import static io.jmix.core.entity.EntityValues.getValue;

/**
 * INTERNAL.
 * Implementation of the {@link DataStore} interface working with a relational database using JPA.
 */
@Component("data_JpaDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JpaDataStore extends AbstractDataStore implements DataSortingOptions {

    public static final String LOAD_TX_PREFIX = "JpaDataStore-load-";
    public static final String SAVE_TX_PREFIX = "JpaDataStore-save-";

    private static final Logger log = LoggerFactory.getLogger(JpaDataStore.class);

    @Autowired
    protected DataProperties properties;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected QueryResultsManager queryResultsManager;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected EntityFetcher entityFetcher;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected EntityChangedEventManager entityChangedEventManager;

    @Autowired
    protected EntityEventManager entityEventManager;

    @Autowired
    protected DbmsSpecifics dbmsSpecifics;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired(required = false)
    protected List<JpaDataStoreListener> dataStoreListeners;

    @Autowired
    protected EntityReferencesNormalizer entityReferencesNormalizer;

    @Autowired
    protected EntityAttributesEraser entityAttributesEraser;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected ObjectProvider<JpqlQueryBuilder> jpqlQueryBuilderProvider;

    @Autowired
    protected PersistenceSupport persistenceSupport;

    protected String storeName;

    protected static final AtomicLong txCount = new AtomicLong();

    @Override
    public String getName() {
        return storeName;
    }

    @Override
    public void setName(String name) {
        this.storeName = name;
    }

    @Nullable
    @Override
    protected Object loadOne(LoadContext<?> context) {
        if (log.isDebugEnabled()) {
            log.debug("load: store={}, metaClass={}, id={}, fetchPlan={}",
                    storeName, context.getEntityMetaClass(), context.getId(), context.getFetchPlan());
        }

        EntityManager em = storeAwareLocator.getEntityManager(storeName);

        em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

        Query query = createQuery(em, context, false);

        List<Object> resultList = executeQuery(query, isSingleResult(context));

        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Object> loadAll(LoadContext<?> context) {
        if (log.isDebugEnabled()) {
            log.debug("loadList: store={}, metaClass={}, fetchPlan={}, from selected={}, query={}",
                    storeName, context.getEntityMetaClass(), context.getFetchPlan(),
                    context.getPreviousQueries().isEmpty(), context.getQuery());
        }

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        queryResultsManager.savePreviousQueryResults(context);

        EntityManager em = storeAwareLocator.getEntityManager(storeName);
        em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

        if (!context.getIds().isEmpty()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass)) {
                return loadAllByIds(context, em);
            } else {
                return loadAllByIdBatches(context, em);
            }
        } else {
            Query query = createQuery(em, context, false);
            return executeQuery(query, false);
        }
    }

    protected List<Object> loadAllByIds(LoadContext<?> context, EntityManager em) {
        LoadContext<?> contextCopy = context.copy();
        contextCopy.setIds(Collections.emptyList());

        List<Object> entities = new ArrayList<>(context.getIds().size());

        for (Object id : context.getIds()) {
            contextCopy.setId(id);
            Query query = createQuery(em, contextCopy, false);
            List<Object> list = executeQuery(query, true);
            entities.addAll(list);
        }

        return entities;
    }

    @SuppressWarnings("unchecked")
    protected List<Object> loadAllByIdBatches(LoadContext<?> context, EntityManager em) {
        Integer batchSize = dbmsSpecifics.getDbmsFeatures(storeName).getMaxIdsBatchSize();

        List<Object> resultList = new ArrayList<>(context.getIds().size());

        List<List<Object>> partitions = Lists.partition((List<Object>) context.getIds(),
                batchSize == null ? Integer.MAX_VALUE : batchSize);
        for (List<Object> partition : partitions) {
            LoadContext<Object> contextCopy = (LoadContext<Object>) context.copy();
            contextCopy.setIds(partition);

            Query query = createQuery(em, contextCopy, false);
            List<Object> list = executeQuery(query, false);

            resultList.addAll(list);
        }

        return resultList;
    }

    @Override
    protected long countAll(LoadContext<?> context) {
        if (log.isDebugEnabled()) {
            log.debug("getCount: store={}, metaClass={}, from selected={}, query={}",
                    storeName, context.getEntityMetaClass(),
                    context.getPreviousQueries().isEmpty(), context.getQuery());
        }

        queryResultsManager.savePreviousQueryResults(context);

        EntityManager em = storeAwareLocator.getEntityManager(storeName);
        em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

        Query query = createQuery(em, context, true);
        Number result = (Number) query.getSingleResult();

        return result.longValue();
    }

    @Override
    public Set<?> save(SaveContext context) {
        log.debug("save: store={}, entitiesToSave={}, entitiesToRemove={}", storeName, context.getEntitiesToSave(), context.getEntitiesToRemove());

        Collection<AccessConstraint<?>> accessConstraints = context.getAccessConstraints();

        Set<Object> saved = new HashSet<>();
        List<Object> persisted = new ArrayList<>();

        boolean softDeletionBefore;
        EntityManager em;
        SavedEntitiesHolder savedEntitiesHolder = null;
        EntityAttributesEraser.ReferencesCollector referencesCollector = null;

        try {

            TransactionStatus txStatus = beginSaveTransaction(context.isJoinTransaction());
            try {
                checkCRUDConstraints(context);

                em = storeAwareLocator.getEntityManager(storeName);
                softDeletionBefore = PersistenceHints.isSoftDeletion(em);
                em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

                // persist new
                for (Object entity : context.getEntitiesToSave()) {
                    if (entityStates.isNew(entity)) {
                        MetaClass metaClass = metadata.getClass(entity.getClass());

                        entityEventManager.publishEntitySavingEvent(entity, true);

                        em.persist(entity);
                        saved.add(entity);
                        persisted.add(entity);

                        InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass);
                        accessManager.applyConstraints(crudContext, accessConstraints);

                        if (!crudContext.isCreatePermitted(entity)) {
                            throw new RowLevelSecurityException(String.format("Create is not permitted for entity %s", entity),
                                    metaClass.getName(), EntityOp.CREATE);
                        }

                        if (!context.isDiscardSaved()) {
                            FetchPlan fetchPlan = getFetchPlanFromContextOrNull(context, entity);
                            entityFetcher.fetch(entity, fetchPlan, true);
                        }
                    }
                }

                // merge the rest - instances can be detached or not
                for (Object entity : context.getEntitiesToSave()) {
                    if (!entityStates.isNew(entity)) {
                        entityEventManager.publishEntitySavingEvent(entity, false);

                        MetaClass metaClass = metadata.getClass(entity.getClass());

                        entityAttributesEraser.restoreAttributes(entity);

                        Object merged = em.merge(entity);
                        saved.add(merged);

                        entityFetcher.fetch(merged, getFetchPlanFromContext(context, entity));

                        InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass);
                        accessManager.applyConstraints(crudContext, accessConstraints);

                        if (!crudContext.isUpdatePermitted(entity)) {
                            throw new RowLevelSecurityException(String.format("Update is not permitted for entity %s", entity),
                                    metaClass.getName(), EntityOp.UPDATE);
                        }
                    }
                }

                fireSaveListeners(context.getEntitiesToSave(), context);

                // remove
                for (Object entity : context.getEntitiesToRemove()) {
                    MetaClass metaClass = metadata.getClass(entity.getClass());

                    entityAttributesEraser.restoreAttributes(entity);
                    Object e;
                    if (EntityValues.isSoftDeletionSupported(entity)) {
                        e = em.merge(entity);
                        entityFetcher.fetch((Entity) e, getFetchPlanFromContext(context, entity));
                    } else {
                        e = em.merge(entity);
                    }

                    InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass);
                    accessManager.applyConstraints(crudContext, accessConstraints);

                    if (!crudContext.isDeletePermitted(entity)) {
                        throw new RowLevelSecurityException(String.format("Delete is not permitted for entity %s", entity),
                                metaClass.getName(), EntityOp.DELETE);
                    }

                    em.remove(e);
                    saved.add(e);
                }

                if (!context.isDiscardSaved()) {
                    referencesCollector = entityAttributesEraser.collectErasingReferences(saved, e -> {
                        InMemoryCrudEntityContext childEntityContext = new InMemoryCrudEntityContext(metadata.getClass(e.getClass()));
                        accessManager.applyConstraints(childEntityContext, accessConstraints);
                        return childEntityContext.isReadPermitted(e);
                    });
                }

                savedEntitiesHolder = SavedEntitiesHolder.setEntities(saved);

                if (context.isJoinTransaction()) {
                    List<EntityChangedEventInfo> eventsInfo = entityChangedEventManager.collect(saved);

                    persistenceSupport.processFlush(em, false);
                    ((EntityManager) em.getDelegate()).flush();

                    List<EntityChangedEvent> events = new ArrayList<>(eventsInfo.size());
                    for (EntityChangedEventInfo info : eventsInfo) {
                        events.add(new EntityChangedEvent(info.getSource(),
                                Id.of(info.getEntity()), info.getType(), info.getChanges(), info.getOriginalMetaClass()));
                    }

                    for (Object entity : saved) {
                        detachEntity(em, entity, getFetchPlanFromContextOrNull(context, entity), true);
                        entityEventManager.publishEntityLoadingEvent(entity);
                    }

                    entityChangedEventManager.publish(events);
                }

            } catch (RuntimeException e) {
                rollbackTransaction(txStatus);
                throw e;
            }
            commitTransaction(txStatus);

            if (em.isOpen()) {
                em.setProperty(PersistenceHints.SOFT_DELETION, softDeletionBefore);
            } else {
                CubaUtil.setSoftDeletion(softDeletionBefore);
                CubaUtil.setOriginalSoftDeletion(softDeletionBefore);
            }
        } catch (IllegalStateException e) {
            handleCascadePersistException(e);
        }

        Set<Object> resultEntities = savedEntitiesHolder.getEntities(saved);

        reloadIfUnfetched(resultEntities, context);

        //todo: event
        if (!context.isDiscardSaved()) {
            entityAttributesEraser.eraseReferences(referencesCollector);
        }

        if (!context.isDiscardSaved()) {
            // Update references from newly persisted entities to merged detached entities. Otherwise a new entity can
            // contain a stale instance of a merged one.
            entityReferencesNormalizer.updateReferences(persisted, resultEntities);
        }

        return context.isDiscardSaved() ? Collections.emptySet() : resultEntities;
    }

    protected void checkCRUDConstraints(SaveContext context) {
        if (context.getAccessConstraints().isEmpty()) {
            return;
        }

        Map<MetaClass, CrudEntityContext> accessCache = new HashMap<>();

        for (Object entity : context.getEntitiesToSave()) {
            if (entity == null)
                continue;

            MetaClass metaClass = metadata.getClass(entity);

            CrudEntityContext entityContext = accessCache.computeIfAbsent(metaClass, key -> {
                CrudEntityContext newEntityContext = new CrudEntityContext(key);
                accessManager.applyConstraints(newEntityContext, context.getAccessConstraints());
                return newEntityContext;
            });

            if (entityStates.isNew(entity)) {
                if (!entityContext.isCreatePermitted()) {
                    throw new AccessDeniedException(PermissionType.ENTITY_OP, EntityOp.CREATE, metaClass.getName());
                }
            } else if (!entityContext.isUpdatePermitted()) {
                throw new AccessDeniedException(PermissionType.ENTITY_OP, EntityOp.UPDATE, metaClass.getName());
            }
        }

        for (Object entity : context.getEntitiesToRemove()) {
            if (entity == null)
                continue;

            MetaClass metaClass = metadata.getClass(entity);

            CrudEntityContext entityContext = accessCache.computeIfAbsent(metaClass, key -> {
                CrudEntityContext newEntityContext = new CrudEntityContext(key);
                accessManager.applyConstraints(newEntityContext, context.getAccessConstraints());
                return newEntityContext;
            });

            if (!entityContext.isDeletePermitted()) {
                throw new AccessDeniedException(PermissionType.ENTITY_OP, EntityOp.DELETE, metaClass.getName());
            }
        }
    }

    protected void reloadIfUnfetched(Set<Object> resultEntities, SaveContext context) {
        if (context.getFetchPlans().isEmpty())
            return;

        List<Object> entitiesToReload = resultEntities.stream()
                .filter(entity -> {
                    FetchPlan fetchPlan = context.getFetchPlans().get(entity);
                    return fetchPlan != null && !entityStates.isLoadedWithFetchPlan(entity, fetchPlan);
                })
                .collect(Collectors.toList());

        if (!entitiesToReload.isEmpty()) {
            TransactionStatus txStatus = beginSaveTransaction(context.isJoinTransaction());
            try {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);

                for (Object entity : entitiesToReload) {
                    FetchPlan fetchPlan = context.getFetchPlans().get(entity);
                    log.debug("Reloading {} according to the requested fetchPlan", entity);
                    Object reloadedEntity = em.find(entity.getClass(), getId(entity),
                            PersistenceHints.builder().withFetchPlan(fetchPlan).build());
                    resultEntities.remove(entity);
                    if (reloadedEntity != null) {
                        resultEntities.add(reloadedEntity);
                    }
                }
            } catch (RuntimeException e) {
                rollbackTransaction(txStatus);
                throw e;
            }
            commitTransaction(txStatus);
        }
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        Preconditions.checkNotNullArgument(context, "context is null");
        Preconditions.checkNotNullArgument(context.getQuery(), "query is null");

        ValueLoadContext.Query contextQuery = context.getQuery();
        Collection<AccessConstraint<?>> accessConstraints = context.getAccessConstraints();

        if (log.isDebugEnabled())
            log.debug("query: " + (JpqlQueryBuilder.printQuery(contextQuery.getQueryString()))
                    + (contextQuery.getFirstResult() == 0 ? "" : ", first=" + contextQuery.getFirstResult())
                    + (contextQuery.getMaxResults() == 0 ? "" : ", max=" + contextQuery.getMaxResults()));

        LoadValuesAccessContext queryContext =
                new LoadValuesAccessContext(contextQuery.getQueryString(), queryTransformerFactory, metadata);
        accessManager.applyConstraints(queryContext, accessConstraints);

        if (!queryContext.isPermitted()) {
            return Collections.emptyList();
        }

        List<KeyValueEntity> entities = new ArrayList<>();

        Object txStatus = beginLoadTransaction(context.isJoinTransaction());
        try {
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

            List<String> keys = context.getProperties();

            JpqlQueryBuilder queryBuilder = jpqlQueryBuilderProvider.getObject();

            queryBuilder.setValueProperties(context.getProperties())
                    .setQueryString(contextQuery.getQueryString())
                    .setCondition(contextQuery.getCondition())
                    .setSort(contextQuery.getSort())
                    .setQueryParameters(contextQuery.getParameters());

            Query query = queryBuilder.getQuery(em);

            if (contextQuery.getFirstResult() != 0)
                query.setFirstResult(contextQuery.getFirstResult());
            if (contextQuery.getMaxResults() != 0)
                query.setMaxResults(contextQuery.getMaxResults());

            List resultList = query.getResultList();
            List<Integer> deniedFieldIndexes = queryContext.getDeniedSelectedIndexes();
            for (Object item : resultList) {
                KeyValueEntity entity = new KeyValueEntity();
                entity.setIdName(context.getIdName());
                entities.add(entity);

                if (item instanceof Object[]) {
                    Object[] row = (Object[]) item;
                    for (int i = 0; i < keys.size(); i++) {
                        String key = keys.get(i);
                        if (row.length > i) {
                            if (deniedFieldIndexes.contains(i)) {
                                entity.setValue(key, null);
                            } else {
                                entity.setValue(key, row[i]);
                            }
                        }
                    }
                } else if (!keys.isEmpty()) {
                    if (!deniedFieldIndexes.isEmpty()) {
                        entity.setValue(keys.get(0), null);
                    } else {
                        entity.setValue(keys.get(0), item);
                    }
                }
            }

        } catch (RuntimeException e) {
            rollbackTransaction(txStatus);
            throw e;
        }
        commitTransaction(txStatus);

        return entities;
    }

    protected FetchPlan getFetchPlanFromContext(SaveContext context, Object entity) {
        FetchPlan fetchPlan = context.getFetchPlans().get(entity);
        if (fetchPlan == null) {
            fetchPlan = fetchPlanRepository.getFetchPlan(entity.getClass(), FetchPlan.LOCAL);
        }

        return fetchPlan;
    }

    @Nullable
    protected FetchPlan getFetchPlanFromContextOrNull(SaveContext context, Object entity) {
        return context.getFetchPlans().get(entity);
    }

    protected Query createQuery(EntityManager em, LoadContext<?> context, boolean countQuery) {
        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        LoadContext.Query contextQuery = context.getQuery();

        JpqlQueryBuilder queryBuilder = jpqlQueryBuilderProvider.getObject();

        queryBuilder.setId(context.getId())
                .setIds(context.getIds())
                .setEntityName(metaClass.getName());

        if (contextQuery != null) {
            queryBuilder.setQueryString(contextQuery.getQueryString())
                    .setCondition(contextQuery.getCondition())
                    .setQueryParameters(contextQuery.getParameters());
            if (!countQuery) {
                queryBuilder.setSort(contextQuery.getSort());
            }
        }

        if (countQuery) {
            queryBuilder.setCountQuery();
        }

        if (!context.getPreviousQueries().isEmpty()) {
            log.debug("Restrict query by previous results");
            //todo MG maybe use user key instead of session id
//            queryBuilder.setPreviousResults(userSessionSource.getUserSession().getId(), context.getQueryKey());
        }

        JmixQuery<?> query = queryBuilder.getQuery(em);

        if (contextQuery != null) {
            if (contextQuery.getFirstResult() != 0)
                query.setFirstResult(contextQuery.getFirstResult());
            if (contextQuery.getMaxResults() != 0)
                query.setMaxResults(contextQuery.getMaxResults());
            if (contextQuery.isCacheable()) {
                query.setHint(PersistenceHints.CACHEABLE, contextQuery.isCacheable());
            }
        }

        for (Map.Entry<String, Object> hint : context.getHints().entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }

        if (!countQuery) {
            query.setHint(PersistenceHints.FETCH_PLAN, createFetchPlan(context));
        }

        ReadEntityQueryContext queryContext = new ReadEntityQueryContext(query, metaClass, queryTransformerFactory);
        accessManager.applyConstraints(queryContext, context.getAccessConstraints());

        query = queryContext.getResultQuery();

        return query;
    }

    protected FetchPlan createFetchPlan(LoadContext<?> context) {
        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());
        FetchPlan fetchPlan = context.getFetchPlan() != null ? context.getFetchPlan() :
                fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.BASE);

        return fetchPlans.builder(fetchPlan)
                .partial(context.isLoadPartialEntities())
                .build();
    }

    protected List<Object> executeQuery(Query query, boolean singleResult) {
        List<Object> list;
        try {
            if (singleResult) {
                try {
                    Object result = query.getSingleResult();
                    list = new ArrayList<>(1);
                    list.add(result);
                } catch (NoResultException e) {
                    list = Collections.emptyList();
                }
            } else {
                //noinspection unchecked
                list = query.getResultList();
            }
        } catch (PersistenceException e) {
            if (e.getCause() instanceof QueryException
                    && e.getMessage() != null
                    && e.getMessage().contains("Fetch group cannot be set on report query")) {
                throw new DevelopmentException("DataManager cannot execute query for single attributes");
            } else {
                throw e;
            }
        }
        return list;
    }

    @Override
    protected Object beginLoadTransaction(boolean joinTransaction) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(LOAD_TX_PREFIX + txCount.incrementAndGet());

        if (properties.isUseReadOnlyTransactionForLoad()) {
            def.setReadOnly(true);
        }
        if (joinTransaction) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        } else {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }
        PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);

        return txManager.getTransaction(def);
    }

    @Override
    protected void commitLoadTransaction(Object transaction, LoadContext<?> context, List<Object> entities) {
        if (context.isJoinTransaction()) {
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            for (Object entity : entities) {
                detachEntity(em, entity, context.getFetchPlan(), false);
                entityEventManager.publishEntityLoadingEvent(entity);
            }
        }
        commitTransaction(transaction);
    }

    protected void rollbackTransaction(Object transaction) {
        TransactionStatus transactionStatus = (TransactionStatus) transaction;
        if (!transactionStatus.isCompleted()) {
            PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
            txManager.rollback(transactionStatus);
        }
    }

    protected void commitTransaction(Object transaction) {
        PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
        txManager.commit((TransactionStatus) transaction);
    }

    protected TransactionStatus beginSaveTransaction(boolean joinTransaction) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(SAVE_TX_PREFIX + txCount.incrementAndGet());

        if (joinTransaction) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        } else {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }
        PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
        return txManager.getTransaction(def);
    }

    protected <E> void detachEntity(EntityManager em, @Nullable E rootEntity,
                                    @Nullable FetchPlan fetchPlan, boolean loadedOnly) {
        if (rootEntity == null)
            return;

        em.detach(rootEntity);

        if (fetchPlan == null)
            return;

        metadataTools.traverseAttributesByFetchPlan(fetchPlan, rootEntity, loadedOnly, new EntityAttributeVisitor() {
            @Override
            public void visit(Object entity, MetaProperty property) {

                Object value = getValue(entity, property.getName());
                if (value != null) {
                    if (property.getRange().getCardinality().isMany()) {
                        @SuppressWarnings("unchecked")
                        Collection<Object> collection = (Collection<Object>) value;
                        for (Object element : collection) {
                            em.detach(element);
                        }
                    } else {
                        em.detach(value);
                    }
                }
            }

            @Override
            public boolean skip(MetaProperty property) {
                return !property.getRange().isClass()
                        || metadataTools.isEmbedded(property)
                        || !metadataTools.isPersistent(property);
            }
        });
    }

    protected void handleCascadePersistException(IllegalStateException e) throws IllegalStateException {
        IllegalStateException exception = e;
        if (!Strings.isNullOrEmpty(e.getMessage())
                && e.getMessage().contains("cascade PERSIST")) {
            exception = new IllegalStateException("An attempt to save an entity with reference to some not persisted entity. " +
                    "All newly created entities must be saved in the same transaction. " +
                    "Put all these objects to the CommitContext before commit.");
            exception.addSuppressed(e);
        }
        throw exception;
    }

    protected void fireSaveListeners(Collection<Object> entities, SaveContext saveContext) {
        if (dataStoreListeners != null) {
            for (JpaDataStoreListener dataStoreListener : dataStoreListeners) {
                dataStoreListener.onSave(entities, saveContext);
            }
        }
    }

    /**
     * @param context - loading context
     * @return false if maxResults=1 and the query is not by ID we should not use getSingleResult() for backward compatibility
     */
    protected boolean isSingleResult(LoadContext<?> context) {
        return !(context.getQuery() != null && context.getQuery().getMaxResults() == 1)
                && context.getId() != null;
    }

    @Override
    public boolean isNullsLastSorting() {
        return dbmsSpecifics.getDbmsFeatures(storeName).isNullsLastSorting();
    }

    @Override
    public boolean supportsLobSortingAndFiltering() {
        return dbmsSpecifics.getDbmsFeatures(storeName).supportsLobSortingAndFiltering();
    }
}
