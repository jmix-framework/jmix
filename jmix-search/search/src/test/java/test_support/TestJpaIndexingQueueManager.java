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

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.impl.IndexingLocker;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import io.jmix.search.index.queue.impl.EnqueueingSessionManager;
import io.jmix.search.index.queue.impl.EntityIdsLoaderProvider;
import io.jmix.search.index.queue.impl.JpaIndexingQueueManager;
import org.jspecify.annotations.NonNull;
import java.util.Collection;
import java.util.List;

/**
 * JpaIndexingQueueManager with additional test tracker of queue items
 */
public class TestJpaIndexingQueueManager extends JpaIndexingQueueManager {

    protected final TestIndexingQueueItemsTracker indexingQueueItemsTracker;

    protected long idsProcessingDelay = 0;

    public TestJpaIndexingQueueManager(TestIndexingQueueItemsTracker indexingQueueItemsTracker,
                                       SearchProperties searchProperties,
                                       UnconstrainedDataManager dataManager,
                                       Metadata metadata,
                                       MetadataTools metadataTools,
                                       EntityIndexer entityIndexer,
                                       StoreAwareLocator storeAwareLocator,
                                       IndexConfigurationManager indexConfigurationManager,
                                       IdSerialization idSerialization,
                                       SystemAuthenticator authenticator,
                                       IndexingLocker locker,
                                       IndexStateRegistry indexStateRegistry,
                                       EnqueueingSessionManager enqueueingSessionManager,
                                       EntityIdsLoaderProvider entityIdsLoaderProvider) {
        super(searchProperties, dataManager, metadata, metadataTools, entityIndexer, storeAwareLocator,
                indexConfigurationManager, idSerialization, authenticator, locker, indexStateRegistry,
                enqueueingSessionManager, entityIdsLoaderProvider);
        this.indexingQueueItemsTracker = indexingQueueItemsTracker;
    }

    public long getIdsProcessingDelayMs() {
        return idsProcessingDelay;
    }

    public void setIdsProcessingDelayMs(long idsProcessingDelay) {
        this.idsProcessingDelay = idsProcessingDelay;
    }

    @Override
    protected int enqueue(@NonNull Collection<IndexingQueueItem> queueItems) {
        indexingQueueItemsTracker.accept(queueItems);
        return super.enqueue(queueItems);
    }

    @Override
    protected int processRawIds(@NonNull List<?> rawIds, @NonNull MetaClass metaClass, int batchSize) {
        try {
            Thread.sleep(idsProcessingDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted during processing delay", e);
        }
        return super.processRawIds(rawIds, metaClass, batchSize);
    }
}
