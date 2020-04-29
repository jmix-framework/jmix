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
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.entity.SoftDelete;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.*;
import io.jmix.data.*;
import io.jmix.data.event.EntityChangedEvent;
import io.jmix.data.persistence.DbmsSpecifics;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.jmix.core.entity.EntityValues.getId;
import static io.jmix.core.entity.EntityValues.getValue;

/**
 * INTERNAL.
 * Implementation of the {@link DataStore} interface working with a relational database through ORM.
 */
@Component(OrmDataStore.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrmDataStore implements DataStore {

    public static final String NAME = "jmix_OrmDataStore";

    public static final String LOAD_TX_PREFIX = "OrmDataStore-load-";
    public static final String SAVE_TX_PREFIX = "OrmDataStore-save-";

    private static final Logger log = LoggerFactory.getLogger(OrmDataStore.class);

//    @Value("jmix.data.inMemoryDistinct:false")
//    protected boolean inMemoryDistinct;
//
//    @Value("jmix.data.disableLoadValuesIfConstraints:false")
//    protected boolean disableLoadValuesIfConstraints;
//
//    @Value("jmix.data.dataManagerChecksSecurityOnMiddleware:false")
//    protected boolean dataManagerChecksSecurityOnMiddleware;
//
//    @Value("jmix.data.useReadOnlyTransactionForLoad:false")
//    protected boolean useReadOnlyTransactionForLoad;

    @Inject
    protected DataProperties properties;

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected FetchPlanRepository fetchPlanRepository;

    @Inject
    protected Security security;
    @Inject
    protected PersistenceSecurity persistenceSecurity;
    @Inject
    protected PersistenceAttributeSecurity attributeSecurity;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected QueryResultsManager queryResultsManager;

    @Inject
    protected QueryTransformerFactory queryTransformerFactory;

    @Inject
    protected EntityFetcher entityFetcher;

    @Inject
    protected EntityStates entityStates;

    @Inject
    protected EntityChangedEventManager entityChangedEventManager;

    @Inject
    protected DbmsSpecifics dbmsSpecifics;

    @Inject
    protected StoreAwareLocator storeAwareLocator;

    @Autowired(required = false)
    protected List<OrmLifecycleListener> ormLifecycleListeners;

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
    public <E extends Entity> E load(LoadContext<E> context) {
        if (log.isDebugEnabled()) {
            log.debug("load: store={}, metaClass={}, id={}, view={}", storeName, context.getMetaClass(), context.getId(), context.getFetchPlan());
        }

        final MetaClass metaClass = metadata.getSession().getClass(context.getMetaClass());
        if (isAuthorizationRequired(context) && !isEntityOpPermitted(metaClass, EntityOp.READ)) {
            log.debug("reading of {} not permitted, returning null", metaClass);
            return null;
        }

        E result = null;
        boolean needToApplyInMemoryReadConstraints = needToApplyInMemoryReadConstraints(context);

        TransactionStatus txStatus = beginLoadTransaction(context.isJoinTransaction());
        try {
            EntityManager em = storeAwareLocator.getEntityManager(storeName);

            if (!context.isSoftDeletion())
                em.setProperty(PersistenceHints.SOFT_DELETION, false);

            // If maxResults=1 and the query is not by ID we should not use getSingleResult() for backward compatibility
            boolean singleResult = !(context.getQuery() != null
                    && context.getQuery().getMaxResults() == 1
                    && context.getQuery().getQueryString() != null)
                    && context.getId() != null;

            FetchPlan fetchPlan = createRestrictedFetchPlan(context);
            Query query = createQuery(em, context, singleResult, false);
            query.setHint(PersistenceHints.FETCH_PLAN, fetchPlan);

            //noinspection unchecked
            List<E> resultList = executeQuery(query, singleResult);
            if (!resultList.isEmpty()) {
                result = resultList.get(0);
            }

            if (result != null && needToFilterByInMemoryReadConstraints(context) && persistenceSecurity.filterByConstraints(result)) {
                result = null;
            }
            if (result != null && needToApplyInMemoryReadConstraints) {
                persistenceSecurity.calculateFilteredData(result);
            }


            if (result != null) {
                fireLoadListeners(Collections.singletonList(result), context);
            }

            if (context.isJoinTransaction()) {
                em.flush();
                detachEntity(em, result, fetchPlan);
            }

        } catch (RuntimeException e) {
            rollbackTransaction(txStatus);
            throw e;
        }
        commitTransaction(txStatus);

        if (result != null) {
            if (needToApplyInMemoryReadConstraints) {
                persistenceSecurity.applyConstraints(result);
            }
            if (isAuthorizationRequired(context)) {
                attributeSecurity.afterLoad(result);
            }
        }

        return result;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <E extends Entity> List<E> loadList(LoadContext<E> context) {
        if (log.isDebugEnabled())
            log.debug("loadList: store=" + storeName + ", metaClass=" + context.getMetaClass() + ", view=" + context.getFetchPlan()
                    + (context.getPreviousQueries().isEmpty() ? "" : ", from selected")
                    + ", query=" + context.getQuery()
                    + (context.getQuery() == null || context.getQuery().getFirstResult() == 0 ? "" : ", first=" + context.getQuery().getFirstResult())
                    + (context.getQuery() == null || context.getQuery().getMaxResults() == 0 ? "" : ", max=" + context.getQuery().getMaxResults()));

        MetaClass metaClass = metadata.getClass(context.getMetaClass());

        if (isAuthorizationRequired(context) && !isEntityOpPermitted(metaClass, EntityOp.READ)) {
            log.debug("reading of {} not permitted, returning empty list", metaClass);
            return Collections.emptyList();
        }

        queryResultsManager.savePreviousQueryResults(context);

        List<E> resultList;
        boolean needToApplyInMemoryReadConstraints = needToApplyInMemoryReadConstraints(context);
        TransactionStatus txStatus = beginLoadTransaction(context.isJoinTransaction());
        try {
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

            boolean ensureDistinct = false;
            if (properties.isInMemoryDistinct() && context.getQuery() != null) {
                QueryTransformer transformer = queryTransformerFactory.transformer(
                        context.getQuery().getQueryString());
                ensureDistinct = transformer.removeDistinct();
                if (ensureDistinct) {
                    context.getQuery().setQueryString(transformer.getResult());
                }
            }
            FetchPlan fetchPlan = createRestrictedFetchPlan(context);
            List<E> entities;

            Integer maxIdsBatchSize = dbmsSpecifics.getDbmsFeatures(storeName).getMaxIdsBatchSize();
            if (!context.getIds().isEmpty() && entityHasEmbeddedId(metaClass)) {
                entities = loadListBySingleIds(context, em, fetchPlan);
            } else if (!context.getIds().isEmpty() && maxIdsBatchSize != null && context.getIds().size() > maxIdsBatchSize) {
                entities = loadListByBatchesOfIds(context, em, fetchPlan, maxIdsBatchSize);
            } else {
                Query query = createQuery(em, context, false, false);
                query.setHint(PersistenceHints.FETCH_PLAN, fetchPlan);
                entities = getResultList(context, query, ensureDistinct);
            }
            if (context.getIds().isEmpty()) {
                resultList = entities;
            } else {
                resultList = checkAndReorderLoadedEntities(context.getIds(), entities, metaClass);
            }

            if (!resultList.isEmpty()) {
                //noinspection rawtypes
                fireLoadListeners((List<Entity>) resultList, context);
            }

            if (needToApplyInMemoryReadConstraints) {
                persistenceSecurity.calculateFilteredData((Collection<Entity>) resultList);
            }

            if (context.isJoinTransaction()) {
                em.flush();
                for (E entity : resultList) {
                    detachEntity(em, entity, fetchPlan);
                }
            }

        } catch (RuntimeException e) {
            rollbackTransaction(txStatus);
            throw e;
        }
        commitTransaction(txStatus);

        if (needToApplyInMemoryReadConstraints) {
            persistenceSecurity.applyConstraints((Collection<Entity>) resultList);
        }
        if (isAuthorizationRequired(context)) {
            attributeSecurity.afterLoad(resultList);
        }

        return resultList;
    }

    protected boolean entityHasEmbeddedId(MetaClass metaClass) {
        MetaProperty pkProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        return pkProperty == null || pkProperty.getRange().isClass();
    }

    protected <E extends Entity> List<E> loadListBySingleIds(LoadContext<E> context, EntityManager em, FetchPlan fetchPlan) {
        LoadContext<?> contextCopy = context.copy();
        contextCopy.setIds(Collections.emptyList());

        List<E> entities = new ArrayList<>(context.getIds().size());
        for (Object id : context.getIds()) {
            contextCopy.setId(id);
            Query query = createQuery(em, contextCopy, true, false);
            query.setHint(PersistenceHints.FETCH_PLAN, fetchPlan);
            List<E> list = executeQuery(query, true);
            entities.addAll(list);
        }
        return entities;
    }

    @SuppressWarnings("unchecked")
    protected <E extends Entity> List<E> loadListByBatchesOfIds(LoadContext<E> context, EntityManager em, FetchPlan view, int batchSize) {
        List<List<Object>> partitions = Lists.partition((List<Object>) context.getIds(), batchSize);

        List<E> entities = new ArrayList<>(context.getIds().size());
        for (List partition : partitions) {
            LoadContext<E> contextCopy = (LoadContext<E>) context.copy();
            contextCopy.setIds(partition);

            Query query = createQuery(em, contextCopy, false, false);
            query.setHint(PersistenceHints.FETCH_PLAN, view);
            List<E> list = executeQuery(query, false);
            entities.addAll(list);
        }

        return entities;
    }

    protected <E extends Entity> List<E> checkAndReorderLoadedEntities(List<?> ids, List<E> entities, MetaClass metaClass) {
        List<E> result = new ArrayList<>(ids.size());
        Map<Object, E> idToEntityMap = entities.stream().collect(Collectors.toMap(e -> getId(e), Function.identity()));
        for (Object id : ids) {
            E entity = idToEntityMap.get(id);
            if (entity == null) {
                throw new EntityAccessException(metaClass, id);
            }
            result.add(entity);
        }
        return result;
    }

    @Override
    public long getCount(LoadContext<? extends Entity> context) {
        if (log.isDebugEnabled())
            log.debug("getCount: store=" + storeName + ", metaClass=" + context.getMetaClass()
                    + (context.getPreviousQueries().isEmpty() ? "" : ", from selected")
                    + ", query=" + context.getQuery());

        MetaClass metaClass = metadata.getClass(context.getMetaClass());

        if (isAuthorizationRequired(context) && !isEntityOpPermitted(metaClass, EntityOp.READ)) {
            log.debug("reading of {} not permitted, returning 0", metaClass);
            return 0;
        }

        queryResultsManager.savePreviousQueryResults(context);

        context = context.copy();
        if (context.getQuery() == null) {
            context.setQuery(new LoadContext.Query(null));
        }
        if (StringUtils.isBlank(context.getQuery().getQueryString())) {
            context.getQuery().setQueryString("select e from " + metaClass.getName() + " e");
        }

        if (security.hasInMemoryConstraints(metaClass, ConstraintOperationType.READ, ConstraintOperationType.ALL)) {
            List resultList;
            TransactionStatus txStatus = beginLoadTransaction(context.isJoinTransaction());
            try {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);
                em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

                boolean ensureDistinct = false;
                if (properties.isInMemoryDistinct() && context.getQuery() != null) {
                    QueryTransformer transformer = QueryTransformerFactory.createTransformer(
                            context.getQuery().getQueryString());
                    ensureDistinct = transformer.removeDistinct();
                    if (ensureDistinct) {
                        context.getQuery().setQueryString(transformer.getResult());
                    }
                }
                context.getQuery().setFirstResult(0);
                context.getQuery().setMaxResults(0);

                Query query = createQuery(em, context, false, false);
                query.setHint(PersistenceHints.FETCH_PLAN, createRestrictedFetchPlan(context));

                resultList = getResultList(context, query, ensureDistinct);

            } catch (RuntimeException e) {
                rollbackTransaction(txStatus);
                throw e;
            }
            commitTransaction(txStatus);
            return resultList.size();
        } else {
            QueryTransformer transformer = QueryTransformerFactory.createTransformer(context.getQuery().getQueryString());
            transformer.replaceWithCount();
            context.getQuery().setQueryString(transformer.getResult());

            Number result;
            TransactionStatus txStatus = beginLoadTransaction(context.isJoinTransaction());
            try {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);
                em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

                Query query = createQuery(em, context, false, true);
                result = (Number) query.getSingleResult();

            } catch (RuntimeException e) {
                rollbackTransaction(txStatus);
                throw e;
            }
            commitTransaction(txStatus);

            return result.longValue();
        }
    }

    @Override
    public Set<Entity> save(SaveContext context) {
        if (log.isDebugEnabled())
            log.debug("save: store=" + storeName + ", entitiesToSave=" + context.getEntitiesToSave()
                    + ", entitiesToRemove=" + context.getEntitiesToRemove());

        Set<Entity> saved = new HashSet<>();
        List<Entity> persisted = new ArrayList<>();

        // todo dynamic attributes
//        List<BaseGenericIdEntity> identityEntitiesToStoreDynamicAttributes = new ArrayList<>();
//        List<CategoryAttributeValue> attributeValuesToRemove = new ArrayList<>();

        SavedEntitiesHolder savedEntitiesHolder = null;

        try {

            TransactionStatus txStatus = beginSaveTransaction(context.isJoinTransaction());
            try {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);

                checkPermissions(context);

                if (!context.isSoftDeletion())
                    em.setProperty(PersistenceHints.SOFT_DELETION, false);

                // todo dynamic attributes
                //            List<BaseGenericIdEntity> entitiesToStoreDynamicAttributes = new ArrayList<>();

                // persist new
                for (Entity entity : context.getEntitiesToSave()) {
                    if (entityStates.isNew(entity)) {

                        if (isAuthorizationRequired(context)) {
                            attributeSecurity.beforePersist(entity);
                        }
                        em.persist(entity);
                        saved.add(entity);
                        persisted.add(entity);

                        if (isAuthorizationRequired(context))
                            checkOperationPermitted(entity, ConstraintOperationType.CREATE);

                        if (!context.isDiscardSaved()) {
                            FetchPlan view = getFetchPlanFromContextOrNull(context, entity);
                            entityFetcher.fetch(entity, view, true);
                        }

                    }
                }

                // merge the rest - instances can be detached or not
                for (Entity entity : context.getEntitiesToSave()) {
                    if (!entityStates.isNew(entity)) {

                        if (isAuthorizationRequired(context)) {
                            persistenceSecurity.assertToken(entity);
                        }
                        persistenceSecurity.restoreSecurityStateAndFilteredData(entity);
                        if (isAuthorizationRequired(context)) {
                            attributeSecurity.beforeMerge(entity);
                        }

                        Entity merged = em.merge(entity);
                        saved.add(merged);

                        entityFetcher.fetch(merged, getFetchPlanFromContext(context, entity));

                        if (isAuthorizationRequired(context))
                            checkOperationPermitted(merged, ConstraintOperationType.UPDATE);

                    }
                }


                // todo dynamic attributes
                //            for (BaseGenericIdEntity entity : entitiesToStoreDynamicAttributes) {
                //                dynamicAttributesManagerAPI.storeDynamicAttributes(entity);
                //            }

                fireSaveListeners(context.getEntitiesToSave());

                // remove
                for (Entity entity : context.getEntitiesToRemove()) {

                    if (isAuthorizationRequired(context)) {
                        persistenceSecurity.assertToken(entity);
                    }
                    persistenceSecurity.restoreSecurityStateAndFilteredData(entity);

                    Entity e;
                    if (entity instanceof SoftDelete) {
                        attributeSecurity.beforeMerge(entity);

                        e = em.merge(entity);
                        entityFetcher.fetch(e, getFetchPlanFromContext(context, entity));

                    } else {
                        e = em.merge(entity);
                    }

                    if (isAuthorizationRequired(context))
                        checkOperationPermitted(e, ConstraintOperationType.DELETE);

                    em.remove(e);
                    saved.add(e);

                    // todo dynamic attributes
                    //                if (entityHasDynamicAttributes(entity)) {
                    //                    Map<String, CategoryAttributeValue> dynamicAttributes = ((BaseGenericIdEntity) entity).getDynamicAttributes();
                    //
                    //                    // old values of dynamic attributes on deleted entity are used in EntityChangedEvent
                    //                    ((BaseGenericIdEntity) e).setDynamicAttributes(dynamicAttributes);
                    //
                    //                    //dynamicAttributes checked for null in entityHasDynamicAttributes()
                    //                    //noinspection ConstantConditions
                    //                    for (CategoryAttributeValue categoryAttributeValue : dynamicAttributes.values()) {
                    //                        if (!entityStates.isNew(categoryAttributeValue)) {
                    //                            if (Stores.isMain(storeName)) {
                    //                                em.remove(categoryAttributeValue);
                    //                            } else {
                    //                                attributeValuesToRemove.add(categoryAttributeValue);
                    //                            }
                    //                            saved.add(categoryAttributeValue);
                    //                        }
                    //                    }
                    //                }
                }

                if (!context.isDiscardSaved() && isAuthorizationRequired(context) && security.hasConstraints()) {
                    persistenceSecurity.calculateFilteredData(saved);
                }

                savedEntitiesHolder = SavedEntitiesHolder.setEntities(saved);

                if (context.isJoinTransaction()) {
                    List<EntityChangedEvent> events = entityChangedEventManager.collect(saved);
                    em.flush();
                    for (Entity entity : saved) {
                        em.detach(entity);
                    }
                    entityChangedEventManager.publish(events);
                }

            } catch (RuntimeException e) {
                rollbackTransaction(txStatus);
                throw e;
            }
            commitTransaction(txStatus);

        } catch (IllegalStateException e) {
            handleCascadePersistException(e);
        }

        // todo dynamic attributes
//        if (!attributeValuesToRemove.isEmpty()) {
//            try (Transaction tx = getSaveTransaction(Stores.MAIN, context.isJoinTransaction())) {
//                EntityManager em = getEntityManager();
//                for (CategoryAttributeValue entity : attributeValuesToRemove) {
//                    em.remove(entity);
//                }
//                tx.commit();
//            }
//        }

        // todo dynamic attributes
//        if (!identityEntitiesToStoreDynamicAttributes.isEmpty()) {
//            try (Transaction tx = getSaveTransaction(storeName, context.isJoinTransaction())) {
//                for (BaseGenericIdEntity entity : identityEntitiesToStoreDynamicAttributes) {
//                    dynamicAttributesManagerAPI.storeDynamicAttributes(entity);
//                }
//                tx.commit();
//            }
//        }

        Set<Entity> resultEntities = savedEntitiesHolder.getEntities(saved);

        reloadIfUnfetched(resultEntities, context);

        if (!context.isDiscardSaved() && isAuthorizationRequired(context) && security.hasConstraints()) {
            persistenceSecurity.applyConstraints(resultEntities);
        }

        if (!context.isDiscardSaved()) {

            if (isAuthorizationRequired(context)) {
                for (Entity entity : resultEntities) {
                    if (!persisted.contains(entity)) {
                        attributeSecurity.afterCommit(entity);
                    }
                }
            }
            updateReferences(persisted, resultEntities);
        }

        return context.isDiscardSaved() ? Collections.emptySet() : resultEntities;
    }

    protected void reloadIfUnfetched(Set<Entity> resultEntities, SaveContext context) {
        if (context.getFetchPlans().isEmpty())
            return;

        List<Entity> entitiesToReload = resultEntities.stream()
                .filter(entity -> {
                    FetchPlan fetchPlan = context.getFetchPlans().get(entity);
                    return fetchPlan != null && !entityStates.isLoadedWithFetchPlan(entity, fetchPlan);
                })
                .collect(Collectors.toList());

        if (!entitiesToReload.isEmpty()) {
            TransactionStatus txStatus = beginSaveTransaction(context.isJoinTransaction());
            try {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);

                for (Entity entity : entitiesToReload) {
                    FetchPlan fetchPlan = context.getFetchPlans().get(entity);
                    log.debug("Reloading {} according to the requested fetchPlan", entity);
                    Entity reloadedEntity = em.find(entity.getClass(), EntityValues.getId(entity),
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

        if (log.isDebugEnabled())
            log.debug("query: " + (JpqlQueryBuilder.printQuery(contextQuery.getQueryString()))
                    + (contextQuery.getFirstResult() == 0 ? "" : ", first=" + contextQuery.getFirstResult())
                    + (contextQuery.getMaxResults() == 0 ? "" : ", max=" + contextQuery.getMaxResults()));

        QueryParser queryParser = queryTransformerFactory.parser(contextQuery.getQueryString());
        if (isAuthorizationRequired(context) && !checkValueQueryPermissions(queryParser)) {
            return Collections.emptyList();
        }

        List<KeyValueEntity> entities = new ArrayList<>();

        TransactionStatus txStatus = beginLoadTransaction(context.isJoinTransaction());
        try {
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            em.setProperty(PersistenceHints.SOFT_DELETION, context.isSoftDeletion());

            List<String> keys = context.getProperties();

            JpqlQueryBuilder queryBuilder = AppBeans.get(JpqlQueryBuilder.NAME);

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
            List<Integer> notPermittedSelectIndexes = isAuthorizationRequired(context) ?
                    getNotPermittedSelectIndexes(queryParser) : Collections.emptyList();
            for (Object item : resultList) {
                KeyValueEntity entity = new KeyValueEntity();
                entity.setIdName(context.getIdName());
                entities.add(entity);

                if (item instanceof Object[]) {
                    Object[] row = (Object[]) item;
                    for (int i = 0; i < keys.size(); i++) {
                        String key = keys.get(i);
                        if (row.length > i) {
                            if (notPermittedSelectIndexes.contains(i)) {
                                entity.setValue(key, null);
                            } else {
                                entity.setValue(key, row[i]);
                            }
                        }
                    }
                } else if (!keys.isEmpty()) {
                    if (!notPermittedSelectIndexes.isEmpty()) {
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

    protected FetchPlan getFetchPlanFromContext(SaveContext context, Entity entity) {
        FetchPlan view = context.getFetchPlans().get(entity);
        if (view == null) {
            view = fetchPlanRepository.getFetchPlan(entity.getClass(), FetchPlan.LOCAL);
        }

        return isAuthorizationRequired(context) ? attributeSecurity.createRestrictedFetchPlan(view) : view;
    }

    @Nullable
    protected FetchPlan getFetchPlanFromContextOrNull(SaveContext context, Entity entity) {
        FetchPlan view = context.getFetchPlans().get(entity);
        if (view == null) {
            return null;
        }

        return isAuthorizationRequired(context) ? attributeSecurity.createRestrictedFetchPlan(view) : view;
    }

    protected void checkOperationPermitted(Entity entity, ConstraintOperationType operationType) {
        MetaClass metaClass = metadata.getClass(entity);
        if (security.hasConstraints()
                && security.hasConstraints(metaClass)
                && !security.isPermitted(entity, operationType)) {
            throw new RowLevelSecurityException(
                    operationType + " is not permitted for entity " + entity, metaClass.getName(), operationType);
        }
    }

    // todo dynamic attributes
//    protected boolean entityHasDynamicAttributes(Entity entity) {
//        return entity instanceof BaseGenericIdEntity
//                && ((BaseGenericIdEntity) entity).getDynamicAttributes() != null;
//    }

    protected Query createQuery(EntityManager em, LoadContext<?> context, boolean singleResult, boolean countQuery) {
        LoadContext.Query contextQuery = context.getQuery();

        JpqlQueryBuilder queryBuilder = AppBeans.get(JpqlQueryBuilder.NAME);

        queryBuilder.setId(context.getId())
                .setIds(context.getIds())
                .setEntityName(context.getMetaClass())
                .setSingleResult(singleResult);

        if (contextQuery != null) {
            queryBuilder.setQueryString(contextQuery.getQueryString())
                    .setCondition(contextQuery.getCondition())
                    .setQueryParameters(contextQuery.getParameters());
            if (!countQuery) {
                queryBuilder.setSort(contextQuery.getSort());
            }
        }

        if (!context.getPreviousQueries().isEmpty()) {
            log.debug("Restrict query by previous results");
            queryBuilder.setPreviousResults(userSessionSource.getUserSession().getId(), context.getQueryKey());
        }

        Query query = queryBuilder.getQuery(em);

        if (contextQuery != null) {
            if (contextQuery.getFirstResult() != 0)
                query.setFirstResult(contextQuery.getFirstResult());
            if (contextQuery.getMaxResults() != 0)
                query.setMaxResults(contextQuery.getMaxResults());
            if (contextQuery.isCacheable()) {
                query.setHint(PersistenceHints.CACHEABLE, contextQuery.isCacheable());
            }
        }

        if (context.getHints() != null) {
            for (Map.Entry<String, Object> hint : context.getHints().entrySet()) {
                query.setHint(hint.getKey(), hint.getValue());
            }
        }

        return query;
    }

    protected FetchPlan createRestrictedFetchPlan(LoadContext<?> context) {
        FetchPlan fetchPlan = context.getFetchPlan() != null ? context.getFetchPlan() :
                fetchPlanRepository.getFetchPlan(metadata.getClass(context.getMetaClass()), FetchPlan.BASE);

        FetchPlan copy = FetchPlan.copy(isAuthorizationRequired(context) ? attributeSecurity.createRestrictedFetchPlan(fetchPlan) : fetchPlan);
        if (context.isLoadPartialEntities()
                && !needToApplyInMemoryReadConstraints(context)
                && !needToFilterByInMemoryReadConstraints(context)) {
            copy.setLoadPartialEntities(true);
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    protected <E extends Entity> List<E> getResultList(LoadContext<E> context, Query query, boolean ensureDistinct) {
        List<E> list = executeQuery(query, false);
        int initialSize = list.size();
        if (initialSize == 0) {
            return list;
        }
        boolean needToFilterByInMemoryReadConstraints = needToFilterByInMemoryReadConstraints(context);
        boolean filteredByConstraints = false;

        if (needToFilterByInMemoryReadConstraints) {
            filteredByConstraints = persistenceSecurity.filterByConstraints((Collection<Entity>) list);
        }

        if (!ensureDistinct) {
            return filteredByConstraints ? getResultListIteratively(context, query, list, initialSize, true) : list;
        }

        int requestedFirst = context.getQuery().getFirstResult();
        LinkedHashSet<E> set = new LinkedHashSet<>(list);
        if (set.size() == list.size() && requestedFirst == 0 && !filteredByConstraints) {
            // If this is the first chunk and it has no duplicates and security constraints are not applied, just return it
            return list;
        }
        // In case of not first chunk, even if there where no duplicates, start filling the set from zero
        // to ensure correct paging
        return getResultListIteratively(context, query, set, initialSize, needToFilterByInMemoryReadConstraints);
    }

    @SuppressWarnings("unchecked")
    protected <E extends Entity> List<E> getResultListIteratively(LoadContext<E> context, Query query,
                                                                  Collection<E> filteredCollection,
                                                                  int initialSize, boolean needToFilterByInMemoryReadConstraints) {
        int requestedFirst = context.getQuery().getFirstResult();
        int requestedMax = context.getQuery().getMaxResults();

        if (requestedMax == 0) {
            // set contains all items if query without paging
            return new ArrayList<>(filteredCollection);
        }

        int setSize = initialSize + requestedFirst;
        int factor = filteredCollection.size() == 0 ? 2 : initialSize / filteredCollection.size() * 2;

        filteredCollection.clear();

        int firstResult = 0;
        int maxResults = (requestedFirst + requestedMax) * factor;
        int i = 0;
        while (filteredCollection.size() < setSize) {
            if (i++ > 10000) {
                log.warn("In-memory distinct: endless loop detected for " + context);
                break;
            }
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);
            //noinspection unchecked
            List<E> list = query.getResultList();
            if (list.size() == 0) {
                break;
            }

            if (needToFilterByInMemoryReadConstraints) {
                persistenceSecurity.filterByConstraints((Collection<Entity>) list);
            }

            filteredCollection.addAll(list);

            firstResult = firstResult + maxResults;
        }

        // Copy by iteration because subList() returns non-serializable class
        int max = Math.min(requestedFirst + requestedMax, filteredCollection.size());
        List<E> result = new ArrayList<>(max - requestedFirst);
        int j = 0;
        for (E item : filteredCollection) {
            if (j >= max)
                break;
            if (j >= requestedFirst)
                result.add(item);
            j++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected <E extends Entity> List<E> executeQuery(Query query, boolean singleResult) {
        List<E> list;
        try {
            if (singleResult) {
                try {
                    E result = (E) query.getSingleResult();
                    list = new ArrayList<>(1);
                    list.add(result);
                } catch (NoResultException e) {
                    list = Collections.emptyList();
                }
            } else {
                list = query.getResultList();
            }
        } catch (javax.persistence.PersistenceException e) {
            if (e.getCause() instanceof org.eclipse.persistence.exceptions.QueryException
                    && e.getMessage() != null
                    && e.getMessage().contains("Fetch group cannot be set on report query")) {
                throw new DevelopmentException("DataManager cannot execute query for single attributes");
            } else {
                throw e;
            }
        }
        return list;
    }

    protected void checkPermissions(SaveContext context) {
        if (!isAuthorizationRequired(context))
            return;

        Set<MetaClass> checkedCreateRights = new HashSet<>();
        Set<MetaClass> checkedUpdateRights = new HashSet<>();
        Set<MetaClass> checkedDeleteRights = new HashSet<>();

        for (Entity entity : context.getEntitiesToSave()) {
            if (entity == null)
                continue;

            MetaClass metaClass = metadata.getClass(entity);
            if (entityStates.isNew(entity)) {
                checkPermission(checkedCreateRights, metaClass, EntityOp.CREATE);
            } else {
                checkPermission(checkedUpdateRights, metaClass, EntityOp.UPDATE);
            }
        }

        for (Entity entity : context.getEntitiesToRemove()) {
            if (entity == null)
                continue;

            checkPermission(checkedDeleteRights, metadata.getClass(entity), EntityOp.DELETE);
        }
    }

    protected void checkPermission(Set<MetaClass> cache, MetaClass metaClass, EntityOp operation) {
        if (cache.contains(metaClass))
            return;
        checkPermission(metaClass, operation);
        cache.add(metaClass);
    }

    protected void checkPermission(MetaClass metaClass, EntityOp operation) {
        if (!isEntityOpPermitted(metaClass, operation))
            throw new AccessDeniedException(PermissionType.ENTITY_OP, operation, metaClass.getName());
    }

    protected boolean checkValueQueryPermissions(QueryParser queryParser) {
        queryParser.getQueryPaths().stream()
                .filter(path -> !path.isSelectedPath())
                .forEach(path -> {
                    MetaClass metaClass = metadata.getClass(path.getEntityName());
                    MetaPropertyPath propertyPath = metaClass.getPropertyPath(path.getPropertyPath());
                    if (propertyPath == null) {
                        throw new IllegalStateException(String.format("query path '%s' is unresolved", path.getFullPath()));
                    }

                    if (!isEntityAttrViewPermitted(propertyPath)) {
                        throw new AccessDeniedException(PermissionType.ENTITY_ATTR, metaClass + "." + path.getFullPath());
                    }
                });

        MetaClass metaClass = metadata.getClass(queryParser.getEntityName());
        if (!isEntityOpPermitted(metaClass, EntityOp.READ)) {
            log.debug("reading of {} not permitted, returning empty list", metaClass);
            return false;
        }
        if (security.hasInMemoryConstraints(metaClass, ConstraintOperationType.READ, ConstraintOperationType.ALL)) {
            String msg = String.format("%s is not permitted for %s", ConstraintOperationType.READ, metaClass.getName());
            if (properties.isDisableLoadValuesIfConstraints()) {
                throw new RowLevelSecurityException(msg, metaClass.getName(), ConstraintOperationType.READ);
            } else {
                log.debug(msg);
            }
        }
        Set<String> entityNames = queryParser.getAllEntityNames();
        entityNames.remove(metaClass.getName());
        for (String entityName : entityNames) {
            MetaClass entityMetaClass = metadata.getClass(entityName);
            if (!isEntityOpPermitted(entityMetaClass, EntityOp.READ)) {
                log.debug("reading of {} not permitted, returning empty list", entityMetaClass);
                return false;
            }
            if (security.hasConstraints(entityMetaClass)) {
                String msg = String.format("%s is not permitted for %s", ConstraintOperationType.READ, entityName);
                if (properties.isDisableLoadValuesIfConstraints()) {
                    throw new RowLevelSecurityException(msg, entityName, ConstraintOperationType.READ);
                } else {
                    log.debug(msg);
                }
            }
        }
        return true;
    }

    protected boolean isEntityOpPermitted(MetaClass metaClass, EntityOp operation) {
        return security.isEntityOpPermitted(metaClass, operation);
    }

    protected boolean isEntityAttrViewPermitted(MetaPropertyPath metaPropertyPath) {
        for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
            if (!security.isEntityAttrPermitted(metaProperty.getDomain(), metaProperty.getName(), EntityAttrAccess.VIEW)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isAuthorizationRequired(LoadContext context) {
        return context.isAuthorizationRequired() || properties.isDataManagerChecksSecurityOnMiddleware();
    }

    protected boolean isAuthorizationRequired(ValueLoadContext context) {
        return context.isAuthorizationRequired() || properties.isDataManagerChecksSecurityOnMiddleware();
    }

    protected boolean isAuthorizationRequired(SaveContext context) {
        return context.isAuthorizationRequired() || properties.isDataManagerChecksSecurityOnMiddleware();
    }

    protected List<Integer> getNotPermittedSelectIndexes(QueryParser queryParser) {
        List<Integer> indexes = new ArrayList<>();

        int index = 0;
        for (QueryParser.QueryPath path : queryParser.getQueryPaths()) {
            if (path.isSelectedPath()) {
                MetaClass metaClass = metadata.getClass(path.getEntityName());
                if (!Objects.equals(path.getPropertyPath(), path.getVariableName())
                        && !isEntityAttrViewPermitted(metaClass.getPropertyPath(path.getPropertyPath()))) {
                    indexes.add(index);
                }
                index++;
            }
        }
        return indexes;
    }

    /**
     * Update references from newly persisted entities to merged detached entities. Otherwise a new entity can
     * contain a stale instance of merged entity.
     *
     * @param persisted persisted entities
     * @param committed all committed entities
     */
    protected void updateReferences(Collection<Entity> persisted, Collection<Entity> committed) {
        for (Entity persistedEntity : persisted) {
            for (Entity entity : committed) {
                if (entity != persistedEntity) {
                    updateReferences(persistedEntity, entity, new HashSet<>());
                }
            }
        }
    }

    protected void updateReferences(Entity entity, Entity refEntity, Set<Entity> visited) {
        if (entity == null || refEntity == null || visited.contains(entity))
            return;
        visited.add(entity);

        MetaClass refEntityMetaClass = metadata.getClass(refEntity);
        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (!property.getRange().isClass() || !property.getRange().asClass().equals(refEntityMetaClass))
                continue;
            if (entityStates.isLoaded(entity, property.getName())) {
                if (property.getRange().getCardinality().isMany()) {
                    Collection collection = getValue(entity, property.getName());
                    if (collection != null) {
                        for (Object obj : collection) {
                            updateReferences((Entity) obj, refEntity, visited);
                        }
                    }
                } else {
                    Entity value = getValue(entity, property.getName());
                    if (value != null) {
                        if (Objects.equals(getId(value), getId(refEntity))) {
                            if (property.isReadOnly() && !metadataTools.isPersistent(property)) {
                                continue;
                            }
                            EntityValues.setValue(entity, property.getName(), refEntity, false);
                        } else {
                            updateReferences(value, refEntity, visited);
                        }
                    }
                }
            }
        }
    }

    protected boolean needToFilterByInMemoryReadConstraints(LoadContext context) {
        return security.hasConstraints() && security.hasInMemoryConstraints(metadata.getClass(context.getMetaClass()),
                ConstraintOperationType.READ, ConstraintOperationType.ALL);
    }

    protected boolean needToApplyInMemoryReadConstraints(LoadContext context) {
        return isAuthorizationRequired(context) && security.hasConstraints()
                && needToApplyByPredicate(context, metaClass ->
                security.hasInMemoryConstraints(metaClass, ConstraintOperationType.READ, ConstraintOperationType.ALL));
    }

    protected boolean needToApplyByPredicate(LoadContext context, Predicate<MetaClass> hasConstraints) {
        if (context.getFetchPlan() == null) {
            MetaClass metaClass = metadata.getSession().getClass(context.getMetaClass());
            return hasConstraints.test(metaClass);
        }

        for (Class aClass : collectEntityClasses(context.getFetchPlan(), new HashSet<>())) {
            if (hasConstraints.test(metadata.getClass(aClass))) {
                return true;
            }
        }
        return false;
    }

    // todo dynamic attributes
//    protected Set<Class> collectEntityClassesWithDynamicAttributes(@Nullable View view) {
//        if (view == null) {
//            return Collections.emptySet();
//        }
//        return collectEntityClasses(view, new HashSet<>()).stream()
//                .filter(BaseGenericIdEntity.class::isAssignableFrom)
//                .filter(aClass -> !dynamicAttributesManagerAPI.getAttributesForMetaClass(metadata.getClassNN(aClass)).isEmpty())
//                .collect(Collectors.toSet());
//    }

    protected Set<Class> collectEntityClasses(FetchPlan view, Set<FetchPlan> visited) {
        if (visited.contains(view)) {
            return Collections.emptySet();
        } else {
            visited.add(view);
        }

        HashSet<Class> classes = new HashSet<>();
        classes.add(view.getEntityClass());
        for (FetchPlanProperty viewProperty : view.getProperties()) {
            if (viewProperty.getFetchPlan() != null) {
                classes.addAll(collectEntityClasses(viewProperty.getFetchPlan(), visited));
            }
        }
        return classes;
    }

    protected TransactionStatus beginLoadTransaction(boolean joinTransaction) {
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

    protected void commitTransaction(TransactionStatus txStatus) {
        PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
        txManager.commit(txStatus);
    }

    protected void rollbackTransaction(TransactionStatus txStatus) {
        PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
        txManager.rollback(txStatus);
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

    protected <E extends Entity> void detachEntity(EntityManager em, @Nullable E rootEntity, FetchPlan view) {
        if (rootEntity == null)
            return;
        em.detach(rootEntity);
        metadataTools.traverseAttributesByView(view, rootEntity, (entity, property) -> {
            if (property.getRange().isClass() && !metadataTools.isEmbedded(property)) {
                Object value = getValue(entity, property.getName());
                if (value != null) {
                    if (property.getRange().getCardinality().isMany()) {
                        @SuppressWarnings("unchecked")
                        Collection<Entity> collection = (Collection<Entity>) value;
                        for (Entity element : collection) {
                            em.detach(element);
                        }
                    } else {
                        em.detach(value);
                    }
                }
            }
        });
    }

    protected void fireLoadListeners(Collection<Entity> entities, LoadContext<?> context) {
        if (ormLifecycleListeners != null) {
            for (OrmLifecycleListener lifecycleListener : ormLifecycleListeners) {
                //noinspection unchecked
                lifecycleListener.onLoad(entities, context);
            }
        }
    }

    protected void fireSaveListeners(Collection<Entity> entities) {
        if (ormLifecycleListeners != null) {
            for (OrmLifecycleListener lifecycleListener : ormLifecycleListeners) {
                lifecycleListener.onSave(entities);
            }
        }
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
}
