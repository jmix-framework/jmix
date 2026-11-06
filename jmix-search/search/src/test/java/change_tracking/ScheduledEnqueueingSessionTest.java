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
import io.jmix.search.index.queue.IndexingQueueManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TenantChangeTrackingTestConfiguration;
import test_support.entity.indexing.TestTenantEntity;

/**
 * Reindexing an entity does not put its records into the queue at once - it opens a session that a scheduled job
 * then advances batch by batch. The job takes whatever session is next, and a session belonging to a tenant is a
 * session like any other.
 * <p>
 * This matters beyond an administrator pressing a button: the startup synchronization opens exactly such a session
 * for every index it creates or recreates. A job that walks past them leaves a freshly created index empty, and
 * the search of that tenant silently returns nothing until its records happen to change one by one.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TenantChangeTrackingTestConfiguration.class})
public class ScheduledEnqueueingSessionTest {

    static final String ENTITY_NAME = "test_TenantEntity";
    static final String TENANT_ID = "tenanta";

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
        authenticator.runWithSystem(() -> {
            indexingQueueManager.emptyQueue(ENTITY_NAME);
            indexingQueueManager.terminateAsyncEnqueueIndexAll(ENTITY_NAME, TENANT_ID);

            TestTenantEntity entity = metadata.create(TestTenantEntity.class);
            entity.setName("To be reindexed");
            entity.setTenantId(TENANT_ID);
            dataManager.save(entity);
            indexingQueueManager.emptyQueue(ENTITY_NAME);
        });
    }

    @Test
    @DisplayName("The scheduled run takes a session of a tenant, not only the tenantless one")
    public void scheduledRunTakesTenantSession() {
        authenticator.runWithSystem(() ->
                indexingQueueManager.initAsyncEnqueueIndexAll(ENTITY_NAME, TENANT_ID));

        int enqueued = authenticator.withSystem(() -> indexingQueueManager.processNextEnqueueingSession());

        Assertions.assertTrue(enqueued > 0,
                "The scheduled run has to advance the session of a tenant: nothing else does, and the records of"
                        + " that tenant would never reach its index");
    }
}
