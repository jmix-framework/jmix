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
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.entity.EntityEntrySoftDelete;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.EntityOp;
import io.jmix.core.security.PermissionType;
import io.jmix.data.*;
import io.jmix.data.event.EntityChangedEvent;
import io.jmix.data.impl.context.CrudEntityContext;
import io.jmix.data.impl.context.InMemoryCrudEntityContext;
import io.jmix.data.impl.context.LoadValuesAccessContext;
import io.jmix.data.impl.context.ReadEntityQueryContext;
import io.jmix.data.persistence.DbmsSpecifics;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.exceptions.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
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

    public static final String NAME = "data_OrmDataStore";

    public static final String LOAD_TX_PREFIX = "OrmDataStore-load-";
    public static final String SAVE_TX_PREFIX = "OrmDataStore-save-";

    private static final Logger log = LoggerFactory.getLogger(OrmDataStore.class);

    @Autowired
    protected DataProperties properties;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

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
    protected DbmsSpecifics dbmsSpecifics;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected BeanLocator beanLocator;

    @Autowired(required = false)
    protected List<OrmLifecycleListener> ormLifecycleListeners;

    @Autowired
    protected EntityReferencesNormalizer entityReferencesNormalizer;

    @Autowired
    protected EntityAttributesEraser entityAttributesEraser;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected ObjectProvider<JpqlQueryBuilder> jpqlQueryBuilderProvider;

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
    public <E extends JmixEntity> E load(LoadContext<E> context) {
        if (log.isDebugEnabled()) {
            log.debug("load: store={}, metaClass={}, id={}, view={}", storeName, context.getEntityMetaClass(), context.getId(), context.getFetchPlan());
        }

        final MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        final Collection<AccessConstraint<?>> accessConstraints = context.getAccessConstraints();

        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, accessConstraints);

        if (!entityContext.isReadPermitted()) {
            log.debug("reading of {} not permitted, returning null", metaClass);
            return null;
        }

        E result = null;
        EntityAttributesEraser.ReferencesCollector referencesCollector = null;

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

            FetchPlan fetchPlan = createFetchPlan(context);
            Query query = createQuery(em, context, singleResult, false);

            query.setHint(PersistenceHints.FETCH_PLAN, fetchPlan);

            //noinspection unchecked
            List<E> resultList = executeQuery(query, singleResult);
            if (!resultList.isEmpty()) {
                result = resultList.get(0);
            }

            InMemoryCrudEntityContext inMemoryEntityContext = new InMemoryCrudEntityContext(metaClass);
            accessManager.applyConstraints(inMemoryEntityContext, accessConstraints);

            if (result != null && !inMemoryEntityContext.isReadPermitted(result)) {
                //noinspection unchecked
                result = null;
            }

            if (result != null) {
                referencesCollector = entityAttributesEraser.collectErasingReferences(result, entity -> {
                    InMemoryCrudEntityContext childEntityContext =
                            new InMemoryCrudEntityContext(metadata.getClass(entity.getClass()));
                    accessManager.applyConstraints(childEntityContext, accessConstraints);
                    return childEntityContext.isReadPermitted(entity);
                });
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
            entityAttributesEraser.eraseReferences(referencesCollector);
        }

        return result;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <E extends JmixEntity> List<E> loadList(LoadContext<E> context) {
        if (log.isDebugEnabled())
            log.debug("loadList: store=" + storeName + ", metaClass=" + context.getEntityMetaClass() + ", view=" + context.getFetchPlan()
                    + (context.getPreviousQueries().isEmpty() ? "" : ", from selected")
                    + ", query=" + context.getQuery()
                    + (context.getQuery() == null || context.getQuery().getFirstResult() == 0 ? "" : ", first=" + context.getQuery().getFirstResult())
                    + (context.getQuery() == null || context.getQuery().getMaxResults() == 0 ? "" : ", max=" + context.getQuery().getMaxResults()));

        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        final Collection<AccessConstraint<?>> accessConstraints = context.getAccessConstraints();


        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, accessConstraints);

        if (!entityContext.isReadPermitted()) {
            log.debug("reading of {} not permitted, returning empty list", metaClass);
            return Collections.emptyList();
        }

        queryResultsManager.savePreviousQueryResults(context);

        List<E> resultList;
        EntityAttributesEraser.ReferencesCollector referencesCollector = null;

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
            FetchPlan fetchPlan = createFetchPlan(context);

            InMemoryCrudEntityContext inMemoryEntityContext = new InMemoryCrudEntityContext(metaClass);
            accessManager.applyConstraints(inMemoryEntityContext, accessConstraints);

            List<E> entities;

            Integer maxIdsBatchSize = dbmsSpecifics.getDbmsFeatures(storeName).getMaxIdsBatchSize();
            if (!context.getIds().isEmpty() && entityHasEmbeddedId(metaClass)) {
                entities = loadListBySingleIds(context, inMemoryEntityContext.readPredicate(), em, fetchPlan);
            } else if (!context.getIds().isEmpty() && maxIdsBatchSize != null && context.getIds().size() > maxIdsBatchSize) {
                entities = loadListByBatchesOfIds(context, inMemoryEntityContext.readPredicate(), em, fetchPlan, maxIdsBatchSize);
            } else {
                Query query = createQuery(em, context, false, false);
                query.setHint(PersistenceHints.FETCH_PLAN, fetchPlan);
                entities = getResultList(context, query, inMemoryEntityContext.readPredicate(), ensureDistinct);
            }
            if (context.getIds().isEmpty()) {
                resultList = entities;
            } else {
                resultList = checkAndReorderLoadedEntities(context.getIds(), entities, metaClass);
            }

            if (!resultList.isEmpty()) {
                referencesCollector = entityAttributesEraser.collectErasingReferences(resultList, entity -> {
                    InMemoryCrudEntityContext childEntityContext =
                            new InMemoryCrudEntityContext(metadata.getClass(entity.getClass()));
                    accessManager.applyConstraints(childEntityContext, accessConstraints);
                    return childEntityContext.isReadPermitted(entity);
                });
                fireLoadListeners((List<JmixEntity>) resultList, context);
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


        if (referencesCollector != null) {
            entityAttributesEraser.eraseReferences(referencesCollector);
        }

        return resultList;
    }

    protected <E extends JmixEntity> MetaClass getEffectiveMetaClassFromContext(LoadContext<E> context) {
        return extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());
    }

    protected boolean entityHasEmbeddedId(MetaClass metaClass) {
        MetaProperty pkProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        return pkProperty == null || pkProperty.getRange().isClass();
    }

    protected <E extends
            JmixEntity> List<E> loadListBySingleIds(LoadContext<E> context, @Nullable Predicate<JmixEntity> filteringPredicate, EntityManager em, FetchPlan fetchPlan) {
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

        if (filteringPredicate != null) {
            return entities.stream()
                    .filter(filteringPredicate)
                    .collect(Collectors.toList());
        } else {
            return entities;
        }
    }

    @SuppressWarnings("unchecked")
    protected <E extends JmixEntity> List<E> loadListByBatchesOfIds(LoadContext<E> context, @Nullable Predicate<JmixEntity> filteringPredicate, EntityManager em, FetchPlan view, int batchSize) {
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

        if (filteringPredicate != null) {
            return entities.stream()
                    .filter(filteringPredicate)
                    .collect(Collectors.toList());
        } else {
            return entities;
        }
    }

    protected <E extends JmixEntity> List<E> checkAndReorderLoadedEntities(List<?> ids, List<E> entities, MetaClass metaClass) {
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
    public long getCount(LoadContext<? extends JmixEntity> context) {
        if (log.isDebugEnabled())
            log.debug("getCount: store=" + storeName + ", metaClass=" + context.getEntityMetaClass()
                    + (context.getPreviousQueries().isEmpty() ? "" : ", from selected")
                    + ", query=" + context.getQuery());

        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        Collection<AccessConstraint<?>> accessConstraints = context.getAccessConstraints();

        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, accessConstraints);

        if (!entityContext.isReadPermitted()) {
            log.debug("reading of {} not permitted, returning 0", metaClass);
            return 0;
        }

        queryResultsManager.savePreviousQueryResults(context);

        context = context.copy();
        if (context.getQuery() == null) {
            context.setQuery(new LoadContext.Query(""));
        }
        if (StringUtils.isBlank(context.getQuery().getQueryString())) {
            context.getQuery().setQueryString("select e from " + metaClass.getName() + " e");
        }

        InMemoryCrudEntityContext inMemoryEntityContext = new InMemoryCrudEntityContext(metaClass);
        accessManager.applyConstraints(inMemoryEntityContext, accessConstraints);

        Predicate<JmixEntity> filteringPredicate = inMemoryEntityContext.readPredicate();

        if (filteringPredicate != null) {
            List resultList;
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
                context.getQuery().setFirstResult(0);
                context.getQuery().setMaxResults(0);

                Query query = createQuery(em, context, false, false);
                query.setHint(PersistenceHints.FETCH_PLAN, createFetchPlan(context));

                resultList = getResultList(context, query, filteringPredicate, ensureDistinct);

            } catch (RuntimeException e) {
                rollbackTransaction(txStatus);
                throw e;
            }
            commitTransaction(txStatus);
            return resultList.size();
        } else {
            QueryTransformer transformer = queryTransformerFactory.transformer(context.getQuery().getQueryString());
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
    public Set<JmixEntity> save(SaveContext context) {
        log.debug("save: store={}, entitiesToSave={}, entitiesToRemove={}", storeName, context.getEntitiesToSave(), context.getEntitiesToRemove());

        Collection<AccessConstraint<?>> accessConstraints = context.getAccessConstraints();

        Set<JmixEntity> saved = new HashSet<>();
        List<JmixEntity> persisted = new ArrayList<>();

        SavedEntitiesHolder savedEntitiesHolder = null;
        EntityAttributesEraser.ReferencesCollector referencesCollector = null;

        try {

            TransactionStatus txStatus = beginSaveTransaction(context.isJoinTransaction());
            try {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);

                checkCRUDConstraints(context);

                if (!context.isSoftDeletion())
                    em.setProperty(PersistenceHints.SOFT_DELETION, false);

                // persist new
                for (JmixEntity entity : context.getEntitiesToSave()) {
                    if (entityStates.isNew(entity)) {
                        MetaClass metaClass = metadata.getClass(entity.getClass());

                        em.persist(entity);
                        saved.add(entity);
                        persisted.add(entity);

                        InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass);
                        accessManager.applyConstraints(new InMemoryCrudEntityContext(metaClass), accessConstraints);

                        if (!crudContext.isCreatePermitted(entity)) {
                            throw new RowLevelSecurityException(String.format("Create is not permitted for entity %s", entity),
                                    metaClass.getName(), EntityOp.CREATE);
                        }

                        if (!context.isDiscardSaved()) {
                            FetchPlan view = getFetchPlanFromContextOrNull(context, entity);
                            entityFetcher.fetch(entity, view, true);
                        }
                    }
                }

                // merge the rest - instances can be detached or not
                for (JmixEntity entity : context.getEntitiesToSave()) {
                    if (!entityStates.isNew(entity)) {
                        MetaClass metaClass = metadata.getClass(entity.getClass());

                        assertToken(accessConstraints, entity);
                        entityAttributesEraser.restoreAttributes(entity);

                        JmixEntity merged = em.merge(entity);
                        saved.add(merged);

                        entityFetcher.fetch(merged, getFetchPlanFromContext(context, entity));

                        InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass);
                        accessManager.applyConstraints(new InMemoryCrudEntityContext(metaClass), accessConstraints);

                        if (!crudContext.isUpdatePermitted(entity)) {
                            throw new RowLevelSecurityException(String.format("Update is not permitted for entity %s", entity),
                                    metaClass.getName(), EntityOp.UPDATE);
                        }
                    }
                }

                fireSaveListeners(context.getEntitiesToSave(), context);

                // remove
                for (JmixEntity entity : context.getEntitiesToRemove()) {
                    MetaClass metaClass = metadata.getClass(entity.getClass());

                    assertToken(accessConstraints, entity);
                    entityAttributesEraser.restoreAttributes(entity);
                    JmixEntity e;
                    if (entity.__getEntityEntry() instanceof EntityEntrySoftDelete) {
                        e = em.merge(entity);
                        entityFetcher.fetch(e, getFetchPlanFromContext(context, entity));
                    } else {
                        e = em.merge(entity);
                    }

                    InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass);
                    accessManager.applyConstraints(new InMemoryCrudEntityContext(metaClass), accessConstraints);

                    if (!crudContext.isDeletePermitted(entity)) {
                        throw new RowLevelSecurityException(String.format("Delete is not permitted for entity %s", entity),
                                metaClass.getName(), EntityOp.DELETE);
                    }

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

                if (!context.isDiscardSaved()) {
                    referencesCollector = entityAttributesEraser.collectErasingReferences(saved, e -> {
                        InMemoryCrudEntityContext childEntityContext = new InMemoryCrudEntityContext(metadata.getClass(e.getClass()));
                        accessManager.applyConstraints(childEntityContext, accessConstraints);
                        return childEntityContext.isReadPermitted(e);
                    });
                }

                savedEntitiesHolder = SavedEntitiesHolder.setEntities(saved);

                if (context.isJoinTransaction()) {
                    List<EntityChangedEvent> events = entityChangedEventManager.collect(saved);
                    em.flush();
                    for (JmixEntity entity : saved) {
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

        Set<JmixEntity> resultEntities = savedEntitiesHolder.getEntities(saved);

        reloadIfUnfetched(resultEntities, context);

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

    protected void reloadIfUnfetched(Set<JmixEntity> resultEntities, SaveContext context) {
        if (context.getFetchPlans().isEmpty())
            return;

        List<JmixEntity> entitiesToReload = resultEntities.stream()
                .filter(entity -> {
                    FetchPlan fetchPlan = context.getFetchPlans().get(entity);
                    return fetchPlan != null && !entityStates.isLoadedWithFetchPlan(entity, fetchPlan);
                })
                .collect(Collectors.toList());

        if (!entitiesToReload.isEmpty()) {
            TransactionStatus txStatus = beginSaveTransaction(context.isJoinTransaction());
            try {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);

                for (JmixEntity entity : entitiesToReload) {
                    FetchPlan fetchPlan = context.getFetchPlans().get(entity);
                    log.debug("Reloading {} according to the requested fetchPlan", entity);
                    JmixEntity reloadedEntity = em.find(entity.getClass(), getId(entity),
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

        TransactionStatus txStatus = beginLoadTransaction(context.isJoinTransaction());
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

    protected FetchPlan getFetchPlanFromContext(SaveContext context, JmixEntity entity) {
        FetchPlan view = context.getFetchPlans().get(entity);
        if (view == null) {
            view = fetchPlanRepository.getFetchPlan(entity.getClass(), FetchPlan.LOCAL);
        }

        return view;
    }

    @Nullable
    protected FetchPlan getFetchPlanFromContextOrNull(SaveContext context, JmixEntity entity) {
        FetchPlan view = context.getFetchPlans().get(entity);
        if (view == null) {
            return null;
        }

        return view;
    }

    protected Query createQuery(EntityManager em, LoadContext<?> context, boolean singleResult, boolean countQuery) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);

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

        if (!context.getPreviousQueries().isEmpty()) {
            log.debug("Restrict query by previous results");
            //todo MG maybe use user key instead of session id
//            queryBuilder.setPreviousResults(userSessionSource.getUserSession().getId(), context.getQueryKey());
        }

        JmixQuery<?> query = queryBuilder.getQuery(em);

        ReadEntityQueryContext queryContext = new ReadEntityQueryContext(query, metaClass, queryTransformerFactory);
        accessManager.applyConstraints(queryContext, context.getAccessConstraints());

        query = queryContext.getResultQuery();

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

        return query;
    }

    protected FetchPlan createFetchPlan(LoadContext<?> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        FetchPlan fetchPlan = context.getFetchPlan() != null ? context.getFetchPlan() :
                fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.BASE);

        return FetchPlan.copy(fetchPlan)
                .setLoadPartialEntities(context.isLoadPartialEntities());
    }

    @SuppressWarnings("unchecked")
    protected <E extends
            JmixEntity> List<E> getResultList(LoadContext<E> context, Query query, @Nullable Predicate<JmixEntity> filteringPredicate,
                                              boolean ensureDistinct) {
        List<E> list = executeQuery(query, false);
        int initialSize = list.size();
        if (initialSize == 0) {
            return list;
        }

        List<E> filteredList = list;
        if (filteringPredicate != null) {
            filteredList = list.stream()
                    .filter(filteringPredicate)
                    .collect(Collectors.toList());
        }

        if (!ensureDistinct) {
            return list.size() != filteredList.size() ?
                    getResultListIteratively(context, query, filteringPredicate, filteredList, initialSize) : filteredList;
        }

        int requestedFirst = context.getQuery().getFirstResult();
        LinkedHashSet<E> set = new LinkedHashSet<>(filteredList);
        if (set.size() == filteredList.size() && requestedFirst == 0 && list.size() == filteredList.size()) {
            // If this is the first chunk and it has no duplicates and security constraints are not applied, just return it
            return filteredList;
        }
        // In case of not first chunk, even if there where no duplicates, start filling the set from zero
        // to ensure correct paging
        return getResultListIteratively(context, query, filteringPredicate, set, initialSize);
    }

    @SuppressWarnings("unchecked")
    protected <E extends JmixEntity> List<E> getResultListIteratively(LoadContext<E> context, Query query,
                                                                      @Nullable Predicate<JmixEntity> filteredPredicate,
                                                                      Collection<E> filteredList,
                                                                      int initialSize) {
        int requestedFirst = context.getQuery().getFirstResult();
        int requestedMax = context.getQuery().getMaxResults();

        if (requestedMax == 0) {
            // set contains all items if query without paging
            return new ArrayList<>(filteredList);
        }

        int setSize = initialSize + requestedFirst;
        int factor = filteredList.size() == 0 ? 2 : initialSize / filteredList.size() * 2;

        filteredList.clear();

        int firstResult = 0;
        int maxResults = (requestedFirst + requestedMax) * factor;
        int i = 0;
        while (filteredList.size() < setSize) {
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

            if (filteredPredicate != null) {
                filteredList.addAll(list.stream()
                        .filter(filteredPredicate)
                        .collect(Collectors.toList()));
            } else {
                filteredList.addAll(list);
            }

            firstResult = firstResult + maxResults;
        }

        // Copy by iteration because subList() returns non-serializable class
        int max = Math.min(requestedFirst + requestedMax, filteredList.size());
        List<E> result = new ArrayList<>(max - requestedFirst);
        int j = 0;
        for (E item : filteredList) {
            if (j >= max)
                break;
            if (j >= requestedFirst)
                result.add(item);
            j++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected <E extends JmixEntity> List<E> executeQuery(Query query, boolean singleResult) {
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

    protected void checkCRUDConstraints(SaveContext context) {
        if (context.getAccessConstraints().isEmpty()) {
            return;
        }

        Map<MetaClass, CrudEntityContext> accessCache = new HashMap<>();

        for (JmixEntity entity : context.getEntitiesToSave()) {
            if (entity == null)
                continue;

            MetaClass metaClass = metadata.getClass(entity);

            CrudEntityContext entityContext = accessCache.computeIfAbsent(metaClass, key -> {
                CrudEntityContext newEntityContext = new CrudEntityContext(key);
                accessManager.applyConstraints(newEntityContext, context.getAccessConstraints());
                return newEntityContext;
            });

            if (entityStates.isNew(entity) && !entityContext.isCreatePermitted()) {
                throw new AccessDeniedException(PermissionType.ENTITY_OP, EntityOp.CREATE, metaClass.getName());
            } else if (!entityContext.isUpdatePermitted()) {
                throw new AccessDeniedException(PermissionType.ENTITY_OP, EntityOp.UPDATE, metaClass.getName());
            }
        }

        for (JmixEntity entity : context.getEntitiesToRemove()) {
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


//    protected boolean needToApplyInMemoryReadConstraints(LoadContext context) {
//        return isAuthorizationRequired(context) && security.hasConstraints()
//                && needToApplyByPredicate(context, metaClass ->
//                security.hasInMemoryConstraints(metaClass, ConstraintOperationType.READ, ConstraintOperationType.ALL));
//    }

    protected boolean needToApplyByPredicate(LoadContext context, Predicate<MetaClass> hasConstraints) {
        if (context.getFetchPlan() == null) {
            MetaClass metaClass = getEffectiveMetaClassFromContext(context);
            return hasConstraints.test(metaClass);
        }

        for (Class aClass : collectEntityClasses(context.getFetchPlan(), new HashSet<>())) {
            if (hasConstraints.test(metadata.getClass(aClass))) {
                return true;
            }
        }
        return false;
    }

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

    protected <E extends JmixEntity> void detachEntity(EntityManager em, @Nullable E rootEntity, FetchPlan view) {
        if (rootEntity == null)
            return;
        em.detach(rootEntity);
        metadataTools.traverseAttributesByView(view, rootEntity, (entity, property) -> {
            if (property.getRange().isClass() && !metadataTools.isEmbedded(property)) {
                Object value = getValue(entity, property.getName());
                if (value != null) {
                    if (property.getRange().getCardinality().isMany()) {
                        @SuppressWarnings("unchecked")
                        Collection<JmixEntity> collection = (Collection<JmixEntity>) value;
                        for (JmixEntity element : collection) {
                            em.detach(element);
                        }
                    } else {
                        em.detach(value);
                    }
                }
            }
        });
    }

    protected void fireLoadListeners(Collection<JmixEntity> entities, LoadContext<?> context) {
        if (ormLifecycleListeners != null) {
            for (OrmLifecycleListener lifecycleListener : ormLifecycleListeners) {
                //noinspection unchecked
                lifecycleListener.onLoad(entities, context);
            }
        }
    }

    protected void fireSaveListeners(Collection<JmixEntity> entities, SaveContext saveContext) {
        if (ormLifecycleListeners != null) {
            for (OrmLifecycleListener lifecycleListener : ormLifecycleListeners) {
                lifecycleListener.onSave(entities, saveContext);
            }
        }
    }

    protected void assertToken(Collection<AccessConstraint<?>> accessConstraints, JmixEntity entity) {
        //TODO: use InMemoryCRUD entity context

//        @Override
//        public void assertToken(Entity entity) {
//            EntityEntry entityEntry = entity.__getEntityEntry();
//            if (entityEntry.getSecurityState().getSecurityToken() == null) {
//                assertSecurityConstraints(entity, (e, metaProperty) -> entityStates.isDetached(entity)
//                        && !entityStates.isLoaded(entity, metaProperty.getName()));
//            }
//        }
//
//        protected void assertSecurityConstraints(Entity entity, BiPredicate<Entity, MetaProperty> predicate) {
//            MetaClass metaClass = metadata.getClass(entity.getClass());
//            for (MetaProperty metaProperty : metaClass.getProperties()) {
//                if (metaProperty.getRange().isClass() && metadataTools.isPersistent(metaProperty)) {
//                    if (predicate.test(entity, metaProperty)) {
//                        continue;
//                    }
//                    if (security.hasInMemoryConstraints(metaProperty.getRange().asClass(), ConstraintOperationType.READ,
//                            ConstraintOperationType.ALL)) {
//                        throw new RowLevelSecurityException(format("Could not read security token from entity %s, " +
//                                        "even though there are active READ/ALL constraints for the property: %s", entity,
//                                metaProperty.getName()),
//                                metaClass.getName());
//                    }
//                }
//            }
//        }
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
