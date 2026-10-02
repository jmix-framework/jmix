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

package test_support;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.search.index.EntityIndexer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.indexing.TestTenantEntity;
import test_support.entity.indexing.TestUuidPkEntity;

import java.util.List;

/**
 * The data of a tenant-aware entity is stored in a separate index per tenant, so an instance must reach the index
 * of its own tenant and no other.
 * <p>
 * This is the layer that runs on every build: it stops at the engine boundary and asserts the requests the indexer
 * produces. {@link AbstractTenantIsolationEngineTest} proves the same behaviour against a real engine, but it is
 * disabled unless {@code -PrunSearchEngineTests=true} is passed — removing the cases here as duplicates would
 * leave an ordinary build with nothing guarding them.
 * <p>
 * The context is booted with the Multitenancy add-on active, so this also covers the wiring the starter performs
 * when the add-on is on the classpath.
 */
public abstract class AbstractTenantIndexingTest {

    @Autowired
    protected EntityIndexer entityIndexer;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SystemAuthenticator authenticator;

    @BeforeEach
    public void setUp() {
        clearRequests();
        authenticator.begin();
    }

    @AfterEach
    public void tearDown() {
        authenticator.end();
    }

    @Test
    @DisplayName("Instances of different tenants are indexed into different indexes")
    public void instancesOfDifferentTenantsGoToOwnIndexes() {
        TestTenantEntity first = tenantEntity("First", "tenant-a");
        TestTenantEntity second = tenantEntity("Second", "tenant-b");

        entityIndexer.indexCollection(List.of(first, second));

        Assertions.assertEquals(
                List.of("search_index_test_tenantentity_tenant-a", "search_index_test_tenantentity_tenant-b"),
                targetIndexes().stream().sorted().toList());
    }

    @Test
    @DisplayName("An entity without a tenant attribute stays in the index shared by all tenants")
    public void entityWithoutTenantAttributeStaysShared() {
        TestUuidPkEntity shared = metadata.create(TestUuidPkEntity.class);
        shared.setName("Shared");
        dataManager.save(shared);

        entityIndexer.index(shared);

        Assertions.assertEquals(List.of("search_index_test_uuidpkentity"), targetIndexes());
    }

    @Test
    @DisplayName("An instance that belongs to no tenant is not indexed")
    public void instanceWithoutTenantIsNotIndexed() {
        TestTenantEntity withoutTenant = tenantEntity("Nobody", null);

        entityIndexer.index(withoutTenant);

        Assertions.assertTrue(targetIndexes().isEmpty(),
                "The data of the entity is stored per tenant, so there is no index to write the instance to");
    }

    /**
     * @return index names the indexer has addressed, in the order the actions were put into the bulk requests
     */
    protected abstract List<String> targetIndexes();

    /**
     * Forgets the requests captured so far.
     */
    protected abstract void clearRequests();

    protected TestTenantEntity tenantEntity(String name, String tenantId) {
        TestTenantEntity entity = metadata.create(TestTenantEntity.class);
        entity.setName(name);
        entity.setTenantId(tenantId);
        return dataManager.save(entity);
    }
}
