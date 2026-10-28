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
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.search.index.queue.IndexingQueueManager;
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
import test_support.entity.indexing.TestTenantEntity;

import java.util.Collection;
import java.util.List;

/**
 * Reindexing one tenant must record that tenant in the items it enqueues.
 * <p>
 * Both paths filter the records by tenant and then forget it: the item is stored with no tenant at all. Nothing
 * breaks right away - the tenant is worked out again when the item is processed, and the document lands in the
 * right index. It breaks later, in the queue: an item with no tenant cannot be told apart from an item addressed
 * to an index that is currently unavailable, so when any index of the entity is down, the batch leaves out every
 * tenantless item of that entity - and the reindexing of all the other tenants stops along with it.
 *
 * @see TenantOfIndexedRecordTest for the same question about an ordinary save
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TenantChangeTrackingTestConfiguration.class})
public class TenantOfReindexedRecordTest {

    private static final String ENTITY = "test_TenantEntity";

    @Autowired
    TestIndexingQueueItemsTracker queueItemsTracker;
    @Autowired
    IndexingQueueManager indexingQueueManager;
    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    SystemAuthenticator authenticator;

    @BeforeEach
    public void setUp() {
        queueItemsTracker.clear();
    }

    @Test
    @DisplayName("Enqueueing all instances of one tenant records that tenant in every item")
    public void synchronousReindexOfOneTenant() {
        TestTenantEntity ofAcme = save("Of acme", "acme");
        TestTenantEntity ofGlobex = save("Of globex", "globex");
        queueItemsTracker.clear();

        indexingQueueManager.enqueueIndexAll(ENTITY, "acme");

        assertTenantOfItems(ofAcme, "acme");
        Assertions.assertTrue(itemsOf(ofGlobex).isEmpty(),
                "the records of another tenant have nothing to do with this reindex");
    }

    @Test
    @DisplayName("An enqueueing session of one tenant records that tenant in every item")
    public void asyncSessionOfOneTenant() {
        TestTenantEntity ofAcme = save("Of acme", "acme");
        TestTenantEntity ofGlobex = save("Of globex", "globex");
        queueItemsTracker.clear();

        indexingQueueManager.initAsyncEnqueueIndexAll(ENTITY, "acme");
        indexingQueueManager.processEnqueueingSession(ENTITY, "acme", 100);

        assertTenantOfItems(ofAcme, "acme");
        Assertions.assertTrue(itemsOf(ofGlobex).isEmpty(),
                "the session belongs to one tenant, so it walks over the records of that tenant alone");
    }

    @Test
    @DisplayName("Enqueueing every tenant leaves each item with the tenant of its own record")
    public void reindexOfEveryTenant() {
        // Asked without a tenant, the reindex walks the tenants the application knows, not the values found in
        // the data - that is where the indexes to fill come from.
        tenant("acme");
        tenant("globex");
        TestTenantEntity ofAcme = save("Of acme", "acme");
        TestTenantEntity ofGlobex = save("Of globex", "globex");
        queueItemsTracker.clear();

        indexingQueueManager.enqueueIndexAll(ENTITY, null);

        assertTenantOfItems(ofAcme, "acme");
        assertTenantOfItems(ofGlobex, "globex");
    }

    protected void assertTenantOfItems(TestTenantEntity record, String expectedTenant) {
        Collection<IndexingQueueItem> items = itemsOf(record);
        Assertions.assertFalse(items.isEmpty(), "the record of tenant '" + expectedTenant + "' was not enqueued");

        List<String> tenants = items.stream().map(IndexingQueueItem::getTenantId).distinct().toList();
        Assertions.assertEquals(List.of(expectedTenant), tenants,
                "every item of this record must carry its tenant");
    }

    protected Collection<IndexingQueueItem> itemsOf(TestTenantEntity record) {
        return queueItemsTracker.getItemsForEntityAndOperation(record, IndexingOperation.INDEX);
    }

    protected void tenant(String tenantId) {
        authenticator.runWithSystem(() -> {
            Tenant tenant = metadata.create(Tenant.class);
            tenant.setTenantId(tenantId);
            tenant.setName(tenantId);
            dataManager.save(tenant);
        });
    }

    protected TestTenantEntity save(String name, String tenantId) {
        TestTenantEntity entity = metadata.create(TestTenantEntity.class);
        entity.setName(name);
        entity.setTenantId(tenantId);
        return dataManager.save(entity);
    }
}
