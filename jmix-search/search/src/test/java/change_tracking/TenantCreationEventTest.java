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
import io.jmix.multitenancy.event.TenantEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TenantChangeTrackingTestConfiguration;
import test_support.TestTenantEventTracker;

import java.util.List;

/**
 * A tenant that has just been created must announce itself: the search module listens for that announcement and
 * creates the tenant's indexes. Without it the tenant exists, its users can log in, and nothing they save is ever
 * indexed - with no error anywhere.
 * <p>
 * Reading the tenant back is the part that breaks: the listener runs after the transaction has completed, where
 * there is none to read in.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TenantChangeTrackingTestConfiguration.class})
public class TenantCreationEventTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    SystemAuthenticator authenticator;
    @Autowired
    TestTenantEventTracker eventTracker;

    @BeforeEach
    public void setUp() {
        eventTracker.clear();
    }

    @Test
    @DisplayName("Creating a tenant announces it, so that its indexes can be created")
    public void creatingTenantPublishesEvent() {
        authenticator.runWithSystem(() -> {
            Tenant tenant = metadata.create(Tenant.class);
            tenant.setTenantId("created-tenant");
            tenant.setName("created-tenant");
            dataManager.save(tenant);
        });

        Assertions.assertTrue(
                eventTracker.awaitAnnouncedTenants(TenantEvent.Type.CREATED, 5000).contains("created-tenant"),
                "A created tenant has to be announced, otherwise its indexes are never created");
    }

    @Test
    @DisplayName("Removing a tenant announces it as well")
    public void removingTenantPublishesEvent() {
        Tenant tenant = authenticator.withSystem(() -> {
            Tenant created = metadata.create(Tenant.class);
            created.setTenantId("removed-tenant");
            created.setName("removed-tenant");
            return dataManager.save(created);
        });
        eventTracker.clear();

        authenticator.runWithSystem(() -> dataManager.remove(tenant));

        List<String> announced = eventTracker.awaitAnnouncedTenants(TenantEvent.Type.DELETED, 5000);
        Assertions.assertTrue(announced.contains("removed-tenant"),
                "The removed tenant has to be announced. Announced: " + announced);
    }
}
