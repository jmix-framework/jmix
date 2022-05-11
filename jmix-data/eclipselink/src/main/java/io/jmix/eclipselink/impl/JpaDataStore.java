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

import com.google.common.collect.Lists;
import io.jmix.core.Id;
import io.jmix.core.*;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.DataProperties;
import io.jmix.data.PersistenceHints;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.accesscontext.ReadEntityQueryContext;
import io.jmix.data.exception.UniqueConstraintViolationException;
import io.jmix.data.impl.EntityChangedEventInfo;
import io.jmix.data.impl.EntityEventManager;
import io.jmix.data.impl.JpqlQueryBuilder;
import io.jmix.data.impl.QueryResultsManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.impl.lazyloading.LazyLoadingContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.exceptions.QueryException;
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
import javax.persistence.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;
import static io.jmix.core.entity.EntityValues.getValue;
import static javax.persistence.CascadeType.*;

/**
 * INTERNAL.
 * Implementation of the {@link DataStore} interface working with a relational database using JPA.
 */
@Component("eclipselink_JpaDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JpaDataStore extends AbstractDataStore implements DataSortingOptions {

    public static final String LOAD_TX_PREFIX = "JpaDataStore-load-";
    public static final String SAVE_TX_PREFIX = "JpaDataStore-save-";

    private static final Logger log = LoggerFactory.getLogger(JpaDataStore.class);

    @Autowired
    protected DataProperties properties;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected QueryResultsManager queryResultsManager;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

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

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected ObjectProvider<JpqlQueryBuilder> jpqlQueryBuilderProvider;

    @Autowired
    protected EclipselinkPersistenceSupport persistenceSupport;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

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
        EntityManager em = storeAwareLocator.getEntityManager(storeName);

        em.setProperty(PersistenceHints.SOFT_DELETION, context.getHints().get(PersistenceHints.SOFT_DELETION));

        Query query = createQuery(em, context, false);

        List<Object> resultList = executeQuery(query, isSingleResult(context));

        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Object> loadAll(LoadContext<?> context) {
        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        queryResultsManager.savePreviousQueryResults(context);

        EntityManager em = storeAwareLocator.getEntityManager(storeName);
        em.setProperty(PersistenceHints.SOFT_DELETION, context.getHints().get(PersistenceHints.SOFT_DELETION));

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
        queryResultsManager.savePreviousQueryResults(context);

        EntityManager em = storeAwareLocator.getEntityManager(storeName);
        em.setProperty(PersistenceHints.SOFT_DELETION, context.getHints().get(PersistenceHints.SOFT_DELETION));

        Query query = createQuery(em, context, true);
        Number result = (Number) query.getSingleResult();

        return result.longValue();
    }


    @Override
    public Set<?> save(SaveContext context) {
        JpaSaveContext jpaContext = new JpaSaveContext(context);
        completeContextWithCascadeOperations(jpaContext);
        log.debug("save: cascaded: {}", jpaContext.getCascadeAffectedEntities());
        return super.save(jpaContext);
    }

    private void completeContextWithCascadeOperations(JpaSaveContext context) {
        Set<Object> cascadeSaved = new HashSet<>();
        for (Object entityToSave : context.getEntitiesToSave()) {
            processCascadeOperation(entityToSave, cascadeSaved, context.getEntitiesToSave(), entityStates.isNew(entityToSave) ? PERSIST : MERGE);
        }
        context.getEntitiesToSave().addAll(cascadeSaved);
        context.getCascadeAffectedEntities().addAll(cascadeSaved);

        Set<Object> cascadeRemoved = new HashSet<>();
        for (Object entityToRemove : context.getEntitiesToRemove()) {
            processCascadeOperation(entityToRemove, cascadeRemoved, context.getEntitiesToRemove(), REMOVE);
        }
        context.getEntitiesToRemove().addAll(cascadeRemoved);
        context.getCascadeAffectedEntities().addAll(cascadeRemoved);
    }


    private void processCascadeOperation(Object entity, Set<Object> result, Collection<Object> contextEntities, CascadeType type) {
        List<MetaProperty> properties = metadataTools.getCascadeProperties(metadata.getClass(entity), type);
        for (MetaProperty property : properties) {
            if (type == REMOVE || entityStates.isLoaded(entity, property.getName())) {
                Collection<?> referencedEntities;
                if (property.getRange().getCardinality().isMany()) {
                    referencedEntities = EntityValues.getValue(entity, property.getName());
                } else {
                    referencedEntities = Collections.singletonList(EntityValues.getValue(entity, property.getName()));
                }

                if (referencedEntities != null) {
                    for (Object referencedEntity : referencedEntities) {
                        if (referencedEntity != null
                                && !contextEntities.contains(referencedEntity)
                                && !result.contains(referencedEntity)) {
                            processCascadeOperation(referencedEntity, result, contextEntities, type);
                            result.add(referencedEntity);
                        }
                    }
                }
            }
        }

        for (String propertyName : metadataTools.getEmbeddedProperties(metadata.getClass(entity))) {
            if (entityStates.isLoaded(entity, propertyName)) {
                Object embeddedPropertyValue = EntityValues.getValue(entity, propertyName);
                if (embeddedPropertyValue != null) {
                    processCascadeOperation(embeddedPropertyValue, result, contextEntities, type);
                }
            }
        }
    }

    @Override
    protected Set<Object> saveAll(SaveContext context) {
        EntityManager em = storeAwareLocator.getEntityManager(storeName);

        Set<Object> result = new HashSet<>();
        for (Object entity : context.getEntitiesToSave()) {
            if (entityStates.isNew(entity)) {
                entityEventManager.publishEntitySavingEvent(entity, true);
                em.persist(entity);
                result.add(entity);
            }
        }

        for (Object entity : context.getEntitiesToSave()) {
            if (!entityStates.isNew(entity)) {
                entityEventManager.publishEntitySavingEvent(entity, false);
                Object merged = em.merge(entity);
                result.add(merged);
            }
        }

        return result;
    }

    @Override
    protected Set<Object> deleteAll(SaveContext context) {
        JpaSaveContext jpaContext = (JpaSaveContext) context;
        EntityManager em = storeAwareLocator.getEntityManager(storeName);
        Set<Object> result = new HashSet<>();
        boolean softDeletionBefore = PersistenceHints.isSoftDeletion(em);
        try {
            em.setProperty(PersistenceHints.SOFT_DELETION, jpaContext.getHints().get(PersistenceHints.SOFT_DELETION));
            for (Object entity : jpaContext.getEntitiesToRemove()) {
                if (!jpaContext.getCascadeAffectedEntities().contains(entity)) {
                    Object merged = em.merge(entity);
                    em.remove(merged);
                    result.add(merged);
                }
            }
            //for merged clones of removed entities that have been created by EntityManager cascade processing
            for (Object instance : persistenceSupport.getInstances(em)) {
                if (jpaContext.getEntitiesToRemove().contains(instance) && jpaContext.getCascadeAffectedEntities().contains(instance)) {
                    if (!EntityValues.isSoftDeletionSupported(instance) || !PersistenceHints.isSoftDeletion(em)) {
                        getEntityEntry(instance).setRemoved(true);//set entity entry removed state
                    }
                }
            }
        } finally {
            em.setProperty(PersistenceHints.SOFT_DELETION, softDeletionBefore);
        }
        return result;
    }

    @Override
    protected List<Object> loadAllValues(ValueLoadContext context) {
        EntityManager em = storeAwareLocator.getEntityManager(storeName);
        em.setProperty(PersistenceHints.SOFT_DELETION, context.getHints().get(PersistenceHints.SOFT_DELETION));

        Query query = createLoadQuery(em, context, false);
        return executeQuery(query, false);
    }

    @Override
    protected long countAllValues(ValueLoadContext context) {
        EntityManager em = storeAwareLocator.getEntityManager(storeName);
        em.setProperty(PersistenceHints.SOFT_DELETION, context.getHints().get(PersistenceHints.SOFT_DELETION));

        Query query = createLoadQuery(em, context, true);
        Number result = (Number) query.getSingleResult();

        return result.longValue();
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
    protected void beforeLoadTransactionCommit(LoadContext<?> context, Collection<Object> entities) {
        if (context.isJoinTransaction()) {
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            for (Object entity : entities) {
                detachEntity(em, entity, context.getFetchPlan(), false);
                entityEventManager.publishEntityLoadingEvent(entity);
            }
        }
    }

    @Override
    protected void rollbackTransaction(Object transaction) {
        TransactionStatus transactionStatus = (TransactionStatus) transaction;
        if (!transactionStatus.isCompleted()) {
            PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
            txManager.rollback(transactionStatus);
        }
    }

    @Override
    protected void commitTransaction(Object transaction) {
        PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
        txManager.commit((TransactionStatus) transaction);
    }

    protected Object beginSaveTransaction(boolean joinTransaction) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(SAVE_TX_PREFIX + txCount.incrementAndGet());

        if (joinTransaction) {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        } else {
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }
        PlatformTransactionManager txManager = storeAwareLocator.getTransactionManager(storeName);
        Object transaction = txManager.getTransaction(def);
        LazyLoadingContext.setDisabled();
        return transaction;
    }

    @Override
    protected void beforeSaveTransactionCommit(SaveContext context, Collection<Object> savedEntities, Collection<Object> removedEntities) {
        if (context.isJoinTransaction()) {
            List<Object> entities = new ArrayList<>(savedEntities);
            entities.addAll(removedEntities);

            List<EntityChangedEventInfo> eventsInfo;

            EntityManager em = storeAwareLocator.getEntityManager(storeName);

            boolean softDeletionBefore = PersistenceHints.isSoftDeletion(em);
            try {
                em.setProperty(PersistenceHints.SOFT_DELETION, context.getHints().get(PersistenceHints.SOFT_DELETION));
                persistenceSupport.processFlush(em, false);
                eventsInfo = entityChangedEventManager.collect(persistenceSupport.getInstances(em));
                ((EntityManager) em.getDelegate()).flush();
            } catch (PersistenceException e) {
                Pattern pattern = getUniqueConstraintViolationPattern();
                Matcher matcher = pattern.matcher(e.toString());
                if (matcher.find()) {
                    throw new UniqueConstraintViolationException(e.getMessage(), resolveConstraintName(matcher), e);
                }
                throw e;
            } finally {
                em.setProperty(PersistenceHints.SOFT_DELETION, softDeletionBefore);
            }

            //noinspection rawtypes
            List<EntityChangedEvent> events = new ArrayList<>(eventsInfo.size());
            for (EntityChangedEventInfo info : eventsInfo) {
                events.add(new EntityChangedEvent<>(info.getSource(),
                        Id.of(info.getEntity()), info.getType(), info.getChanges(), info.getOriginalMetaClass()));
            }

            for (Object entity : entities) {
                detachEntity(em, entity, context.getFetchPlans().get(entity), true);
            }

            entityChangedEventManager.publish(events);
        }
    }

    @Override
    protected void beforeSaveTransactionRollback(SaveContext context) {
        LazyLoadingContext.setEnabled();
        super.beforeSaveTransactionRollback(context);
    }

    protected Query createQuery(EntityManager em, LoadContext<?> context, boolean countQuery) {
        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        LoadContext.Query contextQuery = context.getQuery();

        JpqlQueryBuilder<JmixEclipseLinkQuery<?>> queryBuilder = jpqlQueryBuilderProvider.getObject();

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

        JmixEclipseLinkQuery<?> query = queryBuilder.getQuery(em);

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

        query = (JmixEclipseLinkQuery) queryContext.getResultQuery();

        return query;
    }

    protected Query createLoadQuery(EntityManager em, ValueLoadContext context, boolean count) {
        //noinspection unchecked
        JpqlQueryBuilder<JmixEclipseLinkQuery<?>> queryBuilder = jpqlQueryBuilderProvider.getObject();

        ValueLoadContext.Query contextQuery = context.getQuery();

        queryBuilder.setValueProperties(context.getProperties())
                .setQueryString(contextQuery.getQueryString())
                .setCondition(contextQuery.getCondition())
                .setQueryParameters(contextQuery.getParameters());

        if (!count) {
            queryBuilder.setSort(contextQuery.getSort());
        } else {
            queryBuilder.setCountQuery();
        }

        JmixEclipseLinkQuery<?> query = queryBuilder.getQuery(em);

        if (contextQuery.getFirstResult() != 0)
            query.setFirstResult(contextQuery.getFirstResult());
        if (contextQuery.getMaxResults() != 0)
            query.setMaxResults(contextQuery.getMaxResults());

        ReadEntityQueryContext queryContext = new ReadEntityQueryContext(query, queryTransformerFactory, metadata);
        accessManager.applyConstraints(queryContext, context.getAccessConstraints());

        query = (JmixEclipseLinkQuery<?>) queryContext.getResultQuery();

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


    protected <E> void detachEntity(EntityManager em, @Nullable E rootEntity, @Nullable FetchPlan fetchPlan, boolean loadedOnly) {
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
                        || !metadataTools.isJpa(property)
                        || !metadataTools.isJpaEntity(property.getRange().asClass());
            }
        });
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

    protected Pattern getUniqueConstraintViolationPattern() {
        String defaultPatternExpression = dbmsSpecifics.getDbmsFeatures().getUniqueConstraintViolationPattern();
        String patternExpression = properties.getUniqueConstraintViolationPattern();

        Pattern pattern;
        if (StringUtils.isBlank(patternExpression)) {
            pattern = Pattern.compile(defaultPatternExpression);
        } else {
            try {
                pattern = Pattern.compile(patternExpression);
            } catch (PatternSyntaxException e) {
                pattern = Pattern.compile(defaultPatternExpression);
                log.warn("Incorrect regexp property {}: {}",
                        "'jmix.data.uniqueConstraintViolationPattern'", patternExpression, e);
            }
        }
        return pattern;
    }

    protected String resolveConstraintName(Matcher matcher) {
        String constraintName = "";
        if (matcher.groupCount() == 1) {
            constraintName = matcher.group(1);
        } else {
            for (int i = 1; i < matcher.groupCount(); i++) {
                if (StringUtils.isNotBlank(matcher.group(i))) {
                    constraintName = matcher.group(i);
                    break;
                }
            }
        }
        return constraintName.toUpperCase();
    }
}
