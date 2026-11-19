/*
 * Copyright 2026 Haulmont.
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

package async_enqueueing;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AsyncEnqueueingTestConfiguration;
import test_support.TestIndexingQueueItemsTracker;
import test_support.TestJpaIndexingQueueManager;
import test_support.entity.indexing.TestTextRootEntity;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AsyncEnqueueingTestConfiguration.class})
@TestPropertySource(properties = "jmix.search.process-queue-batch-time-budget=2ms")
class IndexingQueueBatchProcessingTest {

    @Autowired
    TestIndexingQueueItemsTracker indexingQueueItemsTracker;
    @Autowired
    TestJpaIndexingQueueManager indexingQueueManager;
    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    IndexStateRegistry indexStateRegistry;

    @BeforeEach
    void setUp() {
        indexingQueueItemsTracker.clear();
        indexingQueueManager.emptyQueue();
        indexingQueueManager.resetQueueProcessingTime();
        indexStateRegistry.markIndexAsAvailable(metadata.getClass(TestTextRootEntity.class).getName());

        dataManager.load(TestTextRootEntity.class).all().list().forEach(dataManager::remove);
    }

    @Test
    void processesFullBatchesUntilTimeBudgetIsReached() {
        List<Object> entities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TestTextRootEntity entity = new TestTextRootEntity();
            entity.setName("Entity " + i);
            entities.add(dataManager.save(entity));
        }
        indexingQueueManager.enqueueIndexCollection(entities);

        int processed = indexingQueueManager.processNextBatch(2);

        Assertions.assertEquals(4, processed);
        Assertions.assertEquals(1, dataManager.load(IndexingQueueItem.class).all().list().size());

        int processedInNextIteration = indexingQueueManager.processNextBatch(2);

        Assertions.assertEquals(1, processedInNextIteration);
        Assertions.assertTrue(dataManager.load(IndexingQueueItem.class).all().list().isEmpty());
    }
}
