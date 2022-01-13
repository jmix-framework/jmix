/*
 * Copyright 2021 Haulmont.
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

package test_support;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.jmix.core.Id;
import io.jmix.core.IdSerialization;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import io.jmix.search.index.queue.impl.IndexingOperation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Keep all Indexing Queue Items enqueued by IndexingQueueManager
 */
public class TestIndexingQueueItemsTracker implements Consumer<Collection<IndexingQueueItem>> {

    protected Map<IndexingOperation, Multimap<String, IndexingQueueItem>> registry = new ConcurrentHashMap<>();

    protected final IdSerialization idSerialization;

    public TestIndexingQueueItemsTracker(IdSerialization idSerialization) {
        this.idSerialization = idSerialization;
    }

    @Override
    public void accept(Collection<IndexingQueueItem> indexingQueueItems) {
        indexingQueueItems.forEach(item -> {
            IndexingOperation operation = item.getOperation();
            Multimap<String, IndexingQueueItem> itemsForOperation = registry.computeIfAbsent(
                    operation,
                    key -> Multimaps.synchronizedMultimap(ArrayListMultimap.create())
            );
            itemsForOperation.put(item.getEntityId(), item);
        });
    }

    public boolean containsAnyQueueItemsForEntityAndOperation(Object entity, IndexingOperation operation) {
        return !getItemsForEntityAndOperation(entity, operation).isEmpty();
    }

    public boolean containsQueueItemsForEntityAndOperation(Object entity, IndexingOperation operation, int strictAmount) {
        return getItemsForEntityAndOperation(entity, operation).size() == strictAmount;
    }

    public int getAmountOfItems(Object entity, IndexingOperation operation) {
        return getItemsForEntityAndOperation(entity, operation).size();
    }

    public int getAmountOfItemsForEntity(String entityName, IndexingOperation operation) {
        Multimap<String, IndexingQueueItem> itemsForOperation = registry.get(operation);
        if(itemsForOperation == null) {
            return 0;
        }
        return (int)itemsForOperation.values().stream()
                .filter(item -> entityName.equalsIgnoreCase(item.getEntityName()))
                .count();
    }

    public void clear() {
        registry = new ConcurrentHashMap<>();
    }

    protected Collection<IndexingQueueItem> getItemsForEntityAndOperation(Object entity, IndexingOperation operation) {
        String entityId = idSerialization.idToString(Id.of(entity));
        Multimap<String, IndexingQueueItem> itemsForOperation = registry.get(operation);
        Collection<IndexingQueueItem> result;
        if (itemsForOperation == null) {
            result = Collections.emptyList();
        } else {
            result = itemsForOperation.get(entityId);
        }
        return result;
    }
}
