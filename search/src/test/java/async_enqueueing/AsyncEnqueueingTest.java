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

package async_enqueueing;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.search.index.queue.entity.EnqueueingSession;
import io.jmix.search.index.queue.impl.EnqueueingSessionStatus;
import io.jmix.search.index.queue.impl.IndexingOperation;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AsyncEnqueueingTestConfiguration;
import test_support.TestCommonEntityWrapperManager;
import test_support.TestIndexingQueueItemsTracker;
import test_support.TestJpaIndexingQueueManager;
import test_support.entity.TestRootEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {AsyncEnqueueingTestConfiguration.class}
)
public class AsyncEnqueueingTest {

    @Autowired
    TestIndexingQueueItemsTracker indexingQueueItemsTracker;
    @Autowired
    TestJpaIndexingQueueManager indexingQueueManager;
    @Autowired
    TestCommonEntityWrapperManager ewm;

    @Autowired
    Metadata metadata;
    @Autowired
    DataManager dataManager;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @BeforeEach
    public void setUp() {
        indexingQueueItemsTracker.clear();
        indexingQueueManager.setIdsProcessingDelayMs(0);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        sessions.forEach(session -> dataManager.remove(session));

        List<TestRootEntity> entities = dataManager.load(TestRootEntity.class).all().list();
        entities.forEach(entity -> dataManager.remove(entity));
    }

    @Test
    @DisplayName("Initialize enqueueing session for entity")
    public void initSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);
        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();

        Assert.assertEquals(1, sessions.size());

        EnqueueingSession session = sessions.get(0);
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals("id", session.getOrderingProperty());
        Assert.assertEquals(EnqueueingSessionStatus.ACTIVE, session.getStatus());
        Assert.assertNull(session.getLastProcessedValue());
    }

    @Test
    @DisplayName("Terminate enqueueing session for entity")
    public void terminateSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.terminateAsyncEnqueueIndexAll(entityName);
        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }

    @Test
    @DisplayName("Suspend enqueueing session for entity")
    public void suspendSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);
        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionStatus.SUSPENDED, session.getStatus());
    }

    @Test
    @DisplayName("Resume suspending enqueueing session for entity")
    public void resumeSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);
        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionStatus.SUSPENDED, session.getStatus());

        indexingQueueManager.resumeAsyncEnqueueIndexAll(entityName);
        session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionStatus.ACTIVE, session.getStatus());
    }

    @Test
    @DisplayName("Process next enqueueing session")
    public void processNextSession() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();

        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        int processed = indexingQueueManager.processNextEnqueueingSession(2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);

        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionStatus.ACTIVE, session.getStatus());
        Assert.assertNotNull(session.getLastProcessedValue());

        processed = indexingQueueManager.processNextEnqueueingSession(2);
        itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(1, processed);
        Assert.assertEquals(3, itemsInQueue);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }

    @Test
    @DisplayName("Process enqueueing session for entity")
    public void processSessionForEntity() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        int processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);

        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionStatus.ACTIVE, session.getStatus());
        Assert.assertNotNull(session.getLastProcessedValue());

        processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(1, processed);
        Assert.assertEquals(3, itemsInQueue);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }

    @Test
    @DisplayName("Process suspended-resumed enqueueing session")
    public void processSuspendedAndResumedSession() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);

        int processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(0, processed);
        Assert.assertEquals(0, itemsInQueue);

        indexingQueueManager.resumeAsyncEnqueueIndexAll(entityName);

        processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);
    }

    @Test
    @DisplayName("Try to process absent enqueueing session")
    public void processAbsentSession() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();

        int processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(0, processed);
        Assert.assertEquals(0, itemsInQueue);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }

    @Test
    @DisplayName("Init enqueueing session during processing of session for the same entity (not last page)")
    public void initDuringProcessingNotLastPage() throws Exception {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        initDuringProcessingInternal();
    }

    @Test
    @DisplayName("Init enqueueing session during processing of session for the same entity (last page)")
    public void initDuringProcessingLastPage() throws Exception {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        initDuringProcessingInternal();
    }

    @Test
    @DisplayName("Terminate enqueueing session during processing of this session")
    public void terminateDuringProcessing() throws Exception {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.setIdsProcessingDelayMs(3000);

        Future<Integer> processing = executorService.submit(() -> indexingQueueManager.processEnqueueingSession(entityName, 2));
        Thread.sleep(1000);

        indexingQueueManager.terminateAsyncEnqueueIndexAll(entityName);

        int processed = processing.get(3000, TimeUnit.MILLISECONDS);

        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }

    @Test
    @DisplayName("Suspend enqueueing session during processing of this session")
    public void suspendDuringProcessing() throws Exception {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.setIdsProcessingDelayMs(3000);

        Future<Integer> processing = executorService.submit(() -> indexingQueueManager.processEnqueueingSession(entityName, 2));
        Thread.sleep(1000);

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);

        int processed = processing.get(3000, TimeUnit.MILLISECONDS);

        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);

        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionStatus.SUSPENDED, session.getStatus());
        Assert.assertNotNull(session.getLastProcessedValue());
    }

    protected void initDuringProcessingInternal() throws Exception {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.setIdsProcessingDelayMs(3000);

        Future<Integer> processing = executorService.submit(() -> indexingQueueManager.processEnqueueingSession(entityName, 2));
        Thread.sleep(1000);

        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        int processed = processing.get(3000, TimeUnit.MILLISECONDS);

        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);

        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionStatus.ACTIVE, session.getStatus());
        Assert.assertNull(session.getLastProcessedValue());
    }
}
