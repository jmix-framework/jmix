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

package change_tracking;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import io.jmix.search.index.queue.impl.IndexingOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TenantChangeTrackingTestConfiguration;
import test_support.TestIndexingQueueItemsTracker;
import test_support.entity.TestRootEntity;
import test_support.entity.indexing.TestTenantEntity;

import java.util.Collection;
import java.util.List;

/**
 * The tenant of a deleted record has to be taken while the record still exists: once the deletion reaches the queue
 * the record is gone from the database and nothing can tell which tenant's index holds its document.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TenantChangeTrackingTestConfiguration.class})
public class TenantOfDeletedRecordTest {

    @Autowired
    TestIndexingQueueItemsTracker queueItemsTracker;
    @Autowired
    Metadata metadata;
    @Autowired
    DataManager dataManager;

    @BeforeEach
    public void setUp() {
        queueItemsTracker.clear();
    }

    @Test
    @DisplayName("Deletion of a tenant-aware record carries the tenant into the queue")
    public void tenantIsCarriedIntoTheQueue() {
        TestTenantEntity entity = metadata.create(TestTenantEntity.class);
        entity.setName("Secret of A");
        entity.setTenantId("tenanta");
        TestTenantEntity saved = dataManager.save(entity);

        dataManager.remove(saved);

        Collection<IndexingQueueItem> items =
                queueItemsTracker.getItemsForEntityAndOperation(saved, IndexingOperation.DELETE);
        Assertions.assertEquals(1, items.size(), "The deletion is enqueued");
        Assertions.assertEquals(List.of("tenanta"), items.stream().map(IndexingQueueItem::getTenantId).toList(),
                "The queue item carries the tenant of the deleted record");
    }

    @Test
    @DisplayName("Deletion of a record without a tenant attribute carries no tenant")
    public void recordWithoutTenantAttributeCarriesNone() {
        TestRootEntity entity = metadata.create(TestRootEntity.class);
        entity.setName("Shared");
        TestRootEntity saved = dataManager.save(entity);

        dataManager.remove(saved);

        Collection<IndexingQueueItem> items =
                queueItemsTracker.getItemsForEntityAndOperation(saved, IndexingOperation.DELETE);
        Assertions.assertEquals(1, items.size(), "The deletion is enqueued");
        Assertions.assertNull(items.iterator().next().getTenantId(),
                "An entity without a tenant attribute leaves the tenant of the item unset");
    }
}
