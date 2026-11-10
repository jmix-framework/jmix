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
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
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
import test_support.entity.indexing.TestSoftDeletableTenantEntity;

import java.util.Collection;

/**
 * A soft-deleted record is not gone: it can come back, and the index has to follow it both ways.
 * <p>
 * Removal is a deletion for the index - the document has to leave it - while restoration is an update: the record
 * exists again, so its document has to be written again. Neither happens on its own: both rely on the change
 * listener recognizing what changed, and for a restoration what changed is only the deleted date.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TenantChangeTrackingTestConfiguration.class})
public class SoftDeleteRestoreTest {

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
    @DisplayName("Restoring a soft-deleted record enqueues it for indexing again")
    public void restoredRecordIsEnqueuedForIndexing() {
        TestSoftDeletableTenantEntity entity = metadata.create(TestSoftDeletableTenantEntity.class);
        entity.setName("Comes back");
        entity.setTenantId("tenanta");
        TestSoftDeletableTenantEntity saved = dataManager.save(entity);

        dataManager.remove(saved);
        Assertions.assertEquals(1, itemsFor(saved, IndexingOperation.DELETE).size(),
                "Removal takes the document out of the index");

        queueItemsTracker.clear();
        restore(saved);

        Collection<IndexingQueueItem> items = itemsFor(saved, IndexingOperation.INDEX);
        Assertions.assertEquals(1, items.size(),
                "Restoration puts the record back: the only attribute that changed is the deleted date, and that"
                        + " alone has to be enough to index it again");
    }

    @Test
    @DisplayName("A restored record carries its tenant into the queue")
    public void restoredRecordCarriesTheTenantInTheQueue() {
        TestSoftDeletableTenantEntity entity = metadata.create(TestSoftDeletableTenantEntity.class);
        entity.setName("Comes back too");
        entity.setTenantId("tenanta");
        TestSoftDeletableTenantEntity saved = dataManager.save(entity);
        dataManager.remove(saved);
        queueItemsTracker.clear();

        restore(saved);

        IndexingQueueItem item = itemsFor(saved, IndexingOperation.INDEX).iterator().next();
        Assertions.assertEquals("tenanta", item.getTenantId(),
                "An indexing item could work the tenant out later - the record exists - but recording it here lets"
                        + " the queue leave out only the items of a tenant whose index is unavailable, instead of"
                        + " holding back every item of the entity");
    }

    /**
     * Restores the record the way the Datatools add-on does it: reload it with soft deletion off and clear the
     * attributes that marked it deleted.
     */
    protected void restore(TestSoftDeletableTenantEntity entity) {
        Object reloaded = dataManager.load(Id.of(entity))
                .hint("jmix.softDeletion", false)
                .one();
        EntityValues.setDeletedDate(reloaded, null);
        EntityValues.setDeletedBy(reloaded, null);
        dataManager.save(reloaded);
    }

    protected Collection<IndexingQueueItem> itemsFor(Object entity, IndexingOperation operation) {
        return queueItemsTracker.getItemsForEntityAndOperation(entity, operation);
    }
}
