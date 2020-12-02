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
import io.jmix.core.DataStore;
import io.jmix.core.EntityAccessException;
import io.jmix.core.LoadContext;
import io.jmix.core.entity.EntityValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDataStore implements DataStore {
    protected final List<DataStoreInterceptor> interceptors = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(AbstractDataStore.class);

    @Nullable
    @Override
    public Object load(LoadContext<?> context) {
        EventSharedState eventState = new EventSharedState();

        BeforeEntityLoadEvent beforeLoadEvent = new BeforeEntityLoadEvent(context, eventState);

        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return null;
        }

        Object entity = null;
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            entity = loadOne(context);

            EntityLoadedEvent loadEvent = new EntityLoadedEvent(context, entity, eventState);
            fireEvent(loadEvent);

            entity = loadEvent.getResultEntity();

            commitLoadTransaction(transaction, context,
                    entity == null ? Collections.emptyList() : Collections.singletonList(entity));
        } finally {
            rollbackTransaction(transaction);
        }

        AfterEntityLoadEvent afterLoadEvent = new AfterEntityLoadEvent(context, entity, eventState);
        fireEvent(afterLoadEvent);

        return afterLoadEvent.getResultEntity();
    }

    @Override
    public List<Object> loadList(LoadContext<?> context) {
        EventSharedState eventState = new EventSharedState();

        BeforeEntityLoadEvent beforeLoadEvent = new BeforeEntityLoadEvent(context, eventState);
        fireEvent(beforeLoadEvent);

        if (beforeLoadEvent.loadPrevented()) {
            return Collections.emptyList();
        }

        List<Object> resultList = Collections.emptyList();
        Object transaction = beginLoadTransaction(context.isJoinTransaction());
        try {
            if (context.getIds().isEmpty()) {
                List<Object> entities = loadAll(context);

                EntityLoadedEvent loadEvent = new EntityLoadedEvent(context, entities, eventState);
                fireEvent(loadEvent);

                resultList = loadEvent.getResultEntities();

                if (entities.size() != resultList.size()) {
                    Preconditions.checkNotNull(context.getQuery());
                    if (context.getQuery().getMaxResults() != 0) {
                        resultList = loadListByBatches(context, resultList.size(), eventState);
                    }
                }
            } else {
                resultList = loadAll(context);

                EntityLoadedEvent loadEvent = new EntityLoadedEvent(context, resultList, eventState);
                fireEvent(loadEvent);

                resultList = checkAndReorderLoadedEntities(context, loadEvent.getResultEntities());
            }

            commitLoadTransaction(transaction, context, resultList);
        } finally {
            rollbackTransaction(transaction);
        }

        AfterEntityLoadEvent afterLoadEvent = new AfterEntityLoadEvent(context, resultList, eventState);
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

                //noinspection unchecked,rawtypes,rawtypes
                EntityLoadedEvent loadEvent = new EntityLoadedEvent(context, entities, eventState);
                fireEvent(loadEvent);

                List<?> resultList = loadEvent.getResultEntities();
                count = resultList.size();
            } else {
                count = countAll(context);
            }

            commitLoadTransaction(transaction, context, Collections.emptyList());
        } finally {
            rollbackTransaction(transaction);
        }
        return count;
    }

    @Nullable
    protected abstract Object loadOne(LoadContext<?> context);

    protected abstract List<Object> loadAll(LoadContext<?> context);

    protected abstract long countAll(LoadContext<?> context);

    protected abstract Object beginLoadTransaction(boolean joinTransaction);

    protected abstract void commitLoadTransaction(Object transaction, LoadContext<?> context, List<Object> entities);

    protected abstract void rollbackTransaction(Object transaction);

    public void registerInterceptor(DataStoreInterceptor interceptor) {
        interceptors.add(interceptor);
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

            //noinspection unchecked
            LoadContext<?> batchContext = context.copy();

            assert batchContext.getQuery() != null;
            batchContext.getQuery().setFirstResult(firstResult);
            batchContext.getQuery().setMaxResults(maxResults);

            List<Object> list = loadAll(batchContext);
            if (list.size() == 0) {
                break;
            }

            EntityLoadedEvent loadEvent = new EntityLoadedEvent(context, list, eventState);
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
}
