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

import io.jmix.search.index.queue.entity.IndexingQueueItem;
import io.jmix.search.index.queue.impl.JpaIndexingQueueManager;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * JpaIndexingQueueManager with additional test tracker of queue items
 */
public class TestJpaIndexingQueueManager extends JpaIndexingQueueManager {

    protected final TestIndexingQueueItemsTracker indexingQueueItemsTracker;

    public TestJpaIndexingQueueManager(TestIndexingQueueItemsTracker indexingQueueItemsTracker) {
        this.indexingQueueItemsTracker = indexingQueueItemsTracker;
    }

    @Override
    protected int enqueue(@NotNull Collection<IndexingQueueItem> queueItems) {
        indexingQueueItemsTracker.accept(queueItems);
        return super.enqueue(queueItems);
    }
}
