/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.datastore;

import com.google.common.base.Preconditions;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDataStore implements DataStore {
    protected final List<DataStoreEventListener> listeners = new ArrayList<>();

    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected EntityStates entityStates;
    protected KeyValueMapper keyValueMapper;

    private static final Logger log = LoggerFactory.getLogger(AbstractDataStore.class);

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setEntityStates(EntityStates entityStates) {
        this.entityStates = entityStates;
    }

    @Autowired
    public void setKeyValueMapper(KeyValueMapper keyValueMapper) {
        this.keyValueMapper = keyValueMapper;
    }

    @Nullable
    @Override
    public Object load(LoadContext<?> context) {
        if (log.isDebugEnabled()) {
            log.debug("load: store={}, metaClass={}, id={}, fetchPlan={}",
                    getName(), context.getEntityMetaClass(), context.getId(), context.getFetchPlan());
        }

        EventSharedState loadState = new EventSharedState();

        DataStoreBeforeEntityLoadEvent beforeLoadEvent = new DataStoreBeforeEntityLoadEvent(context, loadState);

        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return null;
        }

        Object entity;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            entity = loadOne(context);

            DataStoreEntityLoadingEvent loadEvent = new DataStoreEntityLoadingEvent(context, entity, loadState);
            fireEvent(loadEvent);

            entity = loadEvent.getResultEntity();

            beforeLoadTransactionCommit(context,
                    entity == null ? Collections.emptyList() : Collections.singletonList(entity));
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }

        DataStoreAfterEntityLoadEvent afterLoadEvent = new DataStoreAfterEntityLoadEvent(context, entity, loadState);
        fireEvent(afterLoadEvent);

        return afterLoadEvent.getResultEntity();
    }

    @Override
    public List<Object> loadList(LoadContext<?> context) {
        if (log.isDebugEnabled()) {
            log.debug("loadList: store={}, metaClass={}, fetchPlan={}, from selected={}, query={}",
                    getName(), context.getEntityMetaClass(), context.getFetchPlan(),
                    context.getPreviousQueries().isEmpty(), context.getQuery());
        }

        EventSharedState loadState = new EventSharedState();

        DataStoreBeforeEntityLoadEvent beforeLoadEvent = new DataStoreBeforeEntityLoadEvent(context, loadState);
        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return Collections.emptyList();
        }

        List<Object> resultList;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            if (context.getIds().isEmpty()) {
                List<Object> entities = loadAll(context);

                DataStoreEntityLoadingEvent loadEvent = new DataStoreEntityLoadingEvent(context, entities, loadState);
                fireEvent(loadEvent);

                resultList = loadEvent.getResultEntities();

                if (entities.size() != resultList.size()) {
                    Preconditions.checkNotNull(context.getQuery());
                    if (context.getQuery().getMaxResults() != 0) {
                        resultList = loadListByBatches(context, resultList.size(), loadState);
                    }
                }
            } else {
                resultList = loadAll(context);

                DataStoreEntityLoadingEvent loadEvent = new DataStoreEntityLoadingEvent(context, resultList, loadState);
                fireEvent(loadEvent);

                resultList = checkAndReorderLoadedEntities(context, loadEvent.getResultEntities());
            }

            beforeLoadTransactionCommit(context, resultList);
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }

        DataStoreAfterEntityLoadEvent afterLoadEvent = new DataStoreAfterEntityLoadEvent(context, resultList, loadState);
        fireEvent(afterLoadEvent);

        return afterLoadEvent.getResultEntities();
    }

    @Override
    public long getCount(LoadContext<?> context) {
        if (log.isDebugEnabled()) {
            log.debug("getCount: store={}, metaClass={}, from selected={}, query={}",
                    getName(), context.getEntityMetaClass(),
                    context.getPreviousQueries().isEmpty(), context.getQuery());
        }

        EventSharedState eventState = new EventSharedState();

        DataStoreBeforeEntityCountEvent beforeCountEvent = new DataStoreBeforeEntityCountEvent(context, eventState);
        fireEvent(beforeCountEvent);

        if (beforeCountEvent.countPrevented()) {
            return 0;
        }

        long count = 0L;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            if (beforeCountEvent.countByItems()) {
                LoadContext<?> countContext = context.copy();
                if (countContext.getQuery() != null) {
                    countContext.getQuery().setFirstResult(0);
                    countContext.getQuery().setMaxResults(0);
                }

                List<?> entities = loadAll(countContext);

                DataStoreEntityLoadingEvent loadEvent = new DataStoreEntityLoadingEvent(context, entities, eventState);
                fireEvent(loadEvent);

                List<?> resultList = loadEvent.getResultEntities();
                count = resultList.size();
            } else {
                count = countAll(context);
            }

            beforeLoadTransactionCommit(context, Collections.emptyList());
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }
        return count;
    }

    @Override
    public Set<?> save(SaveContext context) {
        log.debug("save: store={}, entities to save: {}, entities to remove: {}",
                getName(), context.getEntitiesToSave(), context.getEntitiesToRemove());

        EventSharedState saveState = new EventSharedState();

        DataStoreBeforeEntitySaveEvent beforeSaveEvent = new DataStoreBeforeEntitySaveEvent(context, saveState);
        fireEvent(beforeSaveEvent);

        Set<Object> savedEntities;
        Set<Object> deletedEntities;
        Object transaction = beginSaveTransaction(context.isJoinTransaction());
        try {
            savedEntities = saveAll(context);
            DataStoreEntitySavingEvent savingEvent = new DataStoreEntitySavingEvent(context, savedEntities, saveState);
            fireEvent(savingEvent);

            deletedEntities = deleteAll(context);
            DataStoreEntityDeletingEvent deletingEvent = new DataStoreEntityDeletingEvent(context, deletedEntities, saveState);
            fireEvent(deletingEvent);

            beforeSaveTransactionCommit(context, savedEntities, deletedEntities);
            commitTransaction(transaction);
        } finally {
            beforeSaveTransactionRollback(context);
            rollbackTransaction(transaction);
        }

        return context.isDiscardSaved() ? Collections.emptySet() : loadAllAfterSave(context, savedEntities);
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        Preconditions.checkNotNull(context, "context is null");
        Preconditions.checkNotNull(context.getQuery(), "query is null");

        if (log.isDebugEnabled()) {
            log.debug("loadValues: store={}, query={}", getName(), context.getQuery());
        }

        EventSharedState eventState = new EventSharedState();

        DataStoreBeforeValueLoadEvent beforeLoadEvent = new DataStoreBeforeValueLoadEvent(context, eventState);
        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return Collections.emptyList();
        }

        List<KeyValueEntity> keyValueEntities;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            List<Object> values = loadAllValues(context);

            keyValueEntities = keyValueMapper.mapValues(values, context.getIdName(),
                    context.getProperties(), beforeLoadEvent.deniedProperties());

            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }

        return keyValueEntities;
    }

    @Override
    public long getCount(ValueLoadContext context) {
        Preconditions.checkNotNull(context, "context is null");
        Preconditions.checkNotNull(context.getQuery(), "query is null");

        if (log.isDebugEnabled()) {
            log.debug("getCountValues: store={}, query={}", getName(), context.getQuery());
        }

        EventSharedState eventState = new EventSharedState();

        DataStoreBeforeValueLoadEvent beforeLoadEvent = new DataStoreBeforeValueLoadEvent(context, eventState);
        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return 0;
        }

        long count = 0L;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            count = countAllValues(context);
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }

        return count;
    }

    @Nullable
    protected abstract Object loadOne(LoadContext<?> context);

    protected abstract List<Object> loadAll(LoadContext<?> context);

    protected abstract long countAll(LoadContext<?> context);

    protected abstract Set<Object> saveAll(SaveContext context);

    protected abstract Set<Object> deleteAll(SaveContext context);

    protected abstract List<Object> loadAllValues(ValueLoadContext context);

    protected abstract long countAllValues(ValueLoadContext context);

    protected abstract Object beginLoadTransaction(boolean joinTransaction);

    protected abstract Object beginSaveTransaction(boolean joinTransaction);

    protected abstract void commitTransaction(Object transaction);

    protected abstract void rollbackTransaction(Object transaction);

    protected void beforeLoadTransactionCommit(LoadContext<?> context, Collection<Object> entities) {
    }

    protected void beforeSaveTransactionCommit(SaveContext context, Collection<Object> savedEntities,
                                               Collection<Object> removedEntities) {
    }

    protected void beforeSaveTransactionRollback(SaveContext context) {
    }

    public void registerInterceptor(DataStoreEventListener listener) {
        listeners.add(listener);
        listeners.sort(Comparator.comparing(DataStoreEventListener::getOrder));
    }

    protected <T extends BaseDataStoreEvent> void fireEvent(T event) {
        for (DataStoreEventListener interceptor : listeners) {
            event.sendTo(interceptor);
        }
    }

    protected List<Object> loadListByBatches(LoadContext<?> context, int actualSize, EventSharedState eventState) {
        assert context.getQuery() != null;

        List<Object> entities = new ArrayList<>();

        int requestedFirst = context.getQuery().getFirstResult();
        int requestedMax = context.getQuery().getMaxResults();

        int expectedSize = requestedMax + requestedFirst;
        int factor = actualSize == 0 ? 2 : requestedMax / actualSize * 2;

        int firstResult = 0;
        int maxResults = (requestedFirst + requestedMax) * factor;
        int i = 0;
        while (entities.size() < expectedSize) {
            if (i++ > 100000) {
                log.warn("Loading by batches. Endless loop detected for {}", context);
                break;
            }

            LoadContext<?> batchContext = context.copy();

            assert batchContext.getQuery() != null;
            batchContext.getQuery().setFirstResult(firstResult);
            batchContext.getQuery().setMaxResults(maxResults);

            List<Object> list = loadAll(batchContext);
            if (list.size() == 0) {
                break;
            }

            DataStoreEntityLoadingEvent loadEvent = new DataStoreEntityLoadingEvent(context, list, eventState);
            fireEvent(loadEvent);

            entities.addAll(loadEvent.getResultEntities());
            firstResult = firstResult + maxResults;
        }

        // Copy by iteration because subList() returns non-serializable class
        int max = Math.min(requestedFirst + requestedMax, entities.size());
        List<Object> resultList = new ArrayList<>(max - requestedFirst);
        int j = 0;
        for (Object item : entities) {
            if (j >= max)
                break;
            if (j >= requestedFirst)
                resultList.add(item);
            j++;
        }

        return resultList;
    }

    protected List<Object> checkAndReorderLoadedEntities(LoadContext<?> context, List<Object> entities) {
        List<Object> result = new ArrayList<>(context.getIds().size());
        Map<Object, Object> idToEntityMap = entities.stream().collect(Collectors.toMap(EntityValues::getId, Function.identity()));
        for (Object id : context.getIds()) {
            Object entity = idToEntityMap.get(id);
            if (entity == null) {
                throw new EntityAccessException(context.getEntityMetaClass(), id);
            }
            result.add(entity);
        }
        return result;
    }

    protected Set<Object> loadAllAfterSave(SaveContext context, Set<Object> savedEntities) {
        Map<Object, EntityLoadInfo> loadInfoMap = new HashMap<>();

        Set<Object> loadedEntities = new HashSet<>();
        Object loadTransaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            for (Object entity : savedEntities) {
                EventSharedState loadState = new EventSharedState();
                LoadContext<?> loadContext = new LoadContext<>(metadata.getClass(entity))
                        .setId(Objects.requireNonNull(EntityValues.getId(entity)))
                        .setFetchPlan(getFetchPlanForSave(context.getFetchPlans(), entity));

                DataStoreEntityReloadEvent reloadEvent = new DataStoreEntityReloadEvent(loadContext, context, loadState);
                fireEvent(reloadEvent);

                DataStoreBeforeEntityLoadEvent beforeLoadEvent = new DataStoreBeforeEntityLoadEvent(loadContext, loadState);
                fireEvent(beforeLoadEvent);

                if (!beforeLoadEvent.loadPrevented()) {
                    Object fetchedEntity = loadOne(loadContext);

                    if (fetchedEntity != null) {
                        loadInfoMap.put(fetchedEntity, new EntityLoadInfo(loadContext, loadState));

                        copyNonPersistentAttributes(entity, fetchedEntity);

                        DataStoreEntityLoadingEvent loadEvent = new DataStoreEntityLoadingEvent(loadContext, fetchedEntity, loadState);
                        fireEvent(loadEvent);

                        loadedEntities.add(loadEvent.getResultEntity());
                    }
                }
            }

            for (Object entity : loadedEntities) {
                EntityLoadInfo loadInfo = loadInfoMap.get(entity);
                beforeLoadTransactionCommit(loadInfo.loadContext, Collections.singletonList(entity));
            }
            commitTransaction(loadTransaction);
        } finally {
            rollbackTransaction(loadTransaction);
        }

        Set<Object> resultEntities = new HashSet<>();
        for (Object entity : loadedEntities) {
            EntityLoadInfo loadInfo = loadInfoMap.get(entity);

            DataStoreAfterEntityLoadEvent afterLoadEvent = new DataStoreAfterEntityLoadEvent(loadInfo.loadContext, entity, loadInfo.eventState);
            fireEvent(afterLoadEvent);

            if (afterLoadEvent.getResultEntity() != null) {
                resultEntities.add(afterLoadEvent.getResultEntity());
            }
        }

        return resultEntities;
    }

    protected FetchPlan getFetchPlanForSave(Map<Object, FetchPlan> fetchPlans, Object entity) {
        FetchPlan fetchPlan = fetchPlans.get(entity);
        if (fetchPlan == null) {
            fetchPlan = entityStates.getCurrentFetchPlan(entity);
        }
        return fetchPlan;
    }

    protected void copyNonPersistentAttributes(Object source, Object destination) {
        // copy non-persistent attributes to the resulting merged instance
        for (MetaProperty property : metadata.getClass(source).getProperties()) {
            if (!metadataTools.isJpa(property) && !property.isReadOnly()) {
                // copy using reflection to avoid executing getter/setter code
                Field field = FieldUtils.getField(source.getClass(), property.getName(), true);
                if (field != null) {
                    try {
                        Object value = FieldUtils.readField(field, source);
                        if (value != null) {
                            FieldUtils.writeField(field, destination, value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error copying non-persistent attribute values", e);
                    }
                }
            }
        }
    }

    protected static class EntityLoadInfo {
        protected LoadContext<?> loadContext;
        protected EventSharedState eventState;

        public EntityLoadInfo(LoadContext<?> loadContext, EventSharedState eventState) {
            this.loadContext = loadContext;
            this.eventState = eventState;
        }
    }
}
