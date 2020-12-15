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
    protected final List<DataStoreInterceptor> interceptors = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(AbstractDataStore.class);

    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected EntityStates entityStates;

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

    @Nullable
    @Override
    public Object load(LoadContext<?> context) {
        EventSharedState loadState = new EventSharedState();

        BeforeEntityLoadEvent beforeLoadEvent = new BeforeEntityLoadEvent(context, loadState);

        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return null;
        }

        Object entity;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            entity = loadOne(context);

            EntityLoadingEvent loadEvent = new EntityLoadingEvent(context, entity, loadState);
            fireEvent(loadEvent);

            entity = loadEvent.getResultEntity();

            beforeCommitLoadTransaction(context,
                    entity == null ? Collections.emptyList() : Collections.singletonList(entity));
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }

        AfterEntityLoadEvent afterLoadEvent = new AfterEntityLoadEvent(context, entity, loadState);
        fireEvent(afterLoadEvent);

        return afterLoadEvent.getResultEntity();
    }

    @Override
    public List<Object> loadList(LoadContext<?> context) {
        EventSharedState loadState = new EventSharedState();

        BeforeEntityLoadEvent beforeLoadEvent = new BeforeEntityLoadEvent(context, loadState);
        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return Collections.emptyList();
        }

        List<Object> resultList;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            if (context.getIds().isEmpty()) {
                List<Object> entities = loadAll(context);

                EntityLoadingEvent loadEvent = new EntityLoadingEvent(context, entities, loadState);
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

                EntityLoadingEvent loadEvent = new EntityLoadingEvent(context, resultList, loadState);
                fireEvent(loadEvent);

                resultList = checkAndReorderLoadedEntities(context, loadEvent.getResultEntities());
            }

            beforeCommitLoadTransaction(context, resultList);
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }

        AfterEntityLoadEvent afterLoadEvent = new AfterEntityLoadEvent(context, resultList, loadState);
        fireEvent(afterLoadEvent);

        return afterLoadEvent.getResultEntities();
    }

    @Override
    public long getCount(LoadContext<?> context) {
        EventSharedState eventState = new EventSharedState();

        BeforeEntityCountEvent beforeCountEvent = new BeforeEntityCountEvent(context, eventState);
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

                EntityLoadingEvent loadEvent = new EntityLoadingEvent(context, entities, eventState);
                fireEvent(loadEvent);

                List<?> resultList = loadEvent.getResultEntities();
                count = resultList.size();
            } else {
                count = countAll(context);
            }

            beforeCommitLoadTransaction(context, Collections.emptyList());
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }
        return count;
    }

    @Override
    public Set<?> save(SaveContext context) {
        EventSharedState saveState = new EventSharedState();

        BeforeEntitySaveEvent beforeSaveEvent = new BeforeEntitySaveEvent(context, saveState);
        fireEvent(beforeSaveEvent);

        Set<Object> savedEntities;
        Set<Object> deletedEntities;
        Object transaction = beginSaveTransaction(context.isJoinTransaction());
        try {
            savedEntities = saveAll(context);
            EntitySavingEvent savingEvent = new EntitySavingEvent(context, savedEntities, saveState);
            fireEvent(savingEvent);

            deletedEntities = deleteAll(context);
            EntityDeletingEvent deletingEvent = new EntityDeletingEvent(context, savedEntities, saveState);
            fireEvent(deletingEvent);

            beforeCommitSaveTransaction(context, savedEntities, deletedEntities);
            commitTransaction(transaction);
        } finally {
            rollbackTransaction(transaction);
        }

        return context.isDiscardSaved() ? Collections.emptySet() : loadAllAfterSave(context, savedEntities);
    }

    @Nullable
    protected abstract Object loadOne(LoadContext<?> context);

    protected abstract List<Object> loadAll(LoadContext<?> context);

    protected abstract long countAll(LoadContext<?> context);

    protected abstract Set<Object> saveAll(SaveContext saveContext);

    protected abstract Set<Object> deleteAll(SaveContext saveContext);

    protected abstract Object beginLoadTransaction(boolean joinTransaction);

    protected abstract Object beginSaveTransaction(boolean joinTransaction);

    protected abstract void commitTransaction(Object transaction);

    protected abstract void rollbackTransaction(Object transaction);

    protected void beforeCommitLoadTransaction(LoadContext<?> context, Collection<Object> entities) {
    }

    protected void beforeCommitSaveTransaction(SaveContext context, Collection<Object> savedEntities,
                                               Collection<Object> removedEntities) {
    }

    public void registerInterceptor(DataStoreInterceptor interceptor) {
        interceptors.add(interceptor);
        interceptors.sort(Comparator.comparing(DataStoreInterceptor::getOrder));
    }

    protected <T extends BaseDataStoreEvent> void fireEvent(T event) {
        for (DataStoreInterceptor interceptor : interceptors) {
            event.applyBy(interceptor);
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

            EntityLoadingEvent loadEvent = new EntityLoadingEvent(context, list, eventState);
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

                BeforeEntityLoadEvent beforeLoadEvent = new BeforeEntityLoadEvent(loadContext, loadState);
                fireEvent(beforeLoadEvent);

                if (!beforeLoadEvent.loadPrevented()) {
                    Object fetchedEntity = loadOne(loadContext);

                    if (fetchedEntity != null) {
                        loadInfoMap.put(fetchedEntity, new EntityLoadInfo(loadContext, loadState));

                        copyNonPersistentAttributes(entity, fetchedEntity);

                        EntityLoadingEvent loadEvent = new EntityLoadingEvent(loadContext, fetchedEntity, loadState);
                        fireEvent(loadEvent);

                        loadedEntities.add(loadEvent.getResultEntity());
                    }
                }
            }

            for (Object entity : loadedEntities) {
                EntityLoadInfo loadInfo = loadInfoMap.get(entity);
                beforeCommitLoadTransaction(loadInfo.loadContext, Collections.singletonList(entity));
            }
            commitTransaction(loadTransaction);
        } finally {
            rollbackTransaction(loadTransaction);
        }

        Set<Object> resultEntities = new HashSet<>();
        for (Object entity : loadedEntities) {
            EntityLoadInfo loadInfo = loadInfoMap.get(entity);

            AfterEntityLoadEvent afterLoadEvent = new AfterEntityLoadEvent(loadInfo.loadContext, entity, loadInfo.eventState);
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
        for (MetaProperty property : metadata.getClass(source.getClass()).getProperties()) {
            if (!metadataTools.isPersistent(property) && !property.isReadOnly()) {
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
