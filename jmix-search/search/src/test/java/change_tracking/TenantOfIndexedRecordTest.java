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

/**
 * An indexing item records the tenant of its record when the tenant is known while the record is being saved.
 * <p>
 * It could be worked out later - the record is loaded anyway to build the document - but recorded here it lets
 * the queue leave out the items of a tenant whose index is unavailable and keep processing everything else.
 * Where it cannot be learned cheaply, the item carries nothing and the tenant is worked out at processing time,
 * which is what the module did for every indexing item before.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TenantChangeTrackingTestConfiguration.class})
public class TenantOfIndexedRecordTest {

    @Autowired
    TestIndexingQueueItemsTracker queueItemsTracker;
    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;

    @BeforeEach
    public void setUp() {
        queueItemsTracker.clear();
    }

    @Test
    @DisplayName("A created record carries its tenant into the queue")
    public void createdRecordCarriesTheTenant() {
        TestTenantEntity saved = save("Created", "tenanta");

        Assertions.assertEquals("tenanta", onlyItem(saved).getTenantId());
    }

    @Test
    @DisplayName("An updated record carries its tenant into the queue")
    public void updatedRecordCarriesTheTenant() {
        TestTenantEntity saved = save("Before", "tenantb");
        queueItemsTracker.clear();

        saved.setName("After");
        dataManager.save(saved);

        Assertions.assertEquals("tenantb", onlyItem(saved).getTenantId());
    }

    @Test
    @DisplayName("A record of an entity without a tenant attribute carries none")
    public void recordWithoutTenantAttributeCarriesNothing() {
        TestRootEntity entity = metadata.create(TestRootEntity.class);
        entity.setName("No tenants here");
        TestRootEntity saved = dataManager.save(entity);

        Assertions.assertNull(onlyItem(saved).getTenantId());
    }

    @Test
    @DisplayName("A record loaded by a narrow fetch plan still carries its tenant")
    public void narrowlyLoadedRecordStillCarriesTheTenant() {
        TestTenantEntity saved = save("Loaded narrowly", "tenanta");
        queueItemsTracker.clear();

        TestTenantEntity narrow = dataManager.load(TestTenantEntity.class)
                .id(saved.getId())
                .fetchPlan(fetchPlanBuilder -> fetchPlanBuilder.add("name"))
                .one();
        narrow.setName("Changed through a narrow fetch plan");
        dataManager.save(narrow);

        // A fetch plan that lists attributes does not include system properties, and the tenant attribute is one
        // of them - yet the value is there. The attribute arrives with the row, so the case the guard in the
        // listener protects against does not arise here. The guard stays: it costs a question, and an attribute
        // that is genuinely absent would otherwise throw on the save path of the application.
        Assertions.assertEquals("tenanta", onlyItem(saved).getTenantId());
    }

    protected TestTenantEntity save(String name, String tenantId) {
        TestTenantEntity entity = metadata.create(TestTenantEntity.class);
        entity.setName(name);
        entity.setTenantId(tenantId);
        return dataManager.save(entity);
    }

    protected IndexingQueueItem onlyItem(Object entity) {
        Collection<IndexingQueueItem> items =
                queueItemsTracker.getItemsForEntityAndOperation(entity, IndexingOperation.INDEX);
        Assertions.assertEquals(1, items.size(), "Expected exactly one indexing item, got: " + items);
        return items.iterator().next();
    }
}
