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
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.core.Id;
import io.jmix.search.index.EntityDeletionTarget;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.IndexOperationResult;
import io.jmix.search.index.IndexSynchronizationStatus;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import io.jmix.core.entity.EntityValues;
import test_support.entity.indexing.TestSoftDeletableTenantEntity;
import test_support.entity.indexing.TestTenantEntity;

import java.io.IOException;
import java.util.List;

/**
 * The layer of the tenant tests that talks to a real engine: what a captured request cannot tell us — that the
 * names we generate are accepted, that the per-tenant indexes are actually created, and that a search in the index
 * of one tenant returns that tenant's documents only.
 * <p>
 * The scenarios go through {@link IndexManager} and {@link EntityIndexer}, which know nothing about the engine, so
 * they are written once here. A subclass supplies the two things that are engine-specific: how to read the
 * documents of an index back and how to make the engine visible to a search.
 */
public abstract class AbstractTenantIsolationEngineTest {

    protected static final String TENANT_A = "tenanta";
    protected static final String TENANT_B = "tenantb";

    protected static final String INDEX_A = "search_index_test_tenantentity_" + TENANT_A;
    protected static final String INDEX_B = "search_index_test_tenantentity_" + TENANT_B;
    protected static final String SOFT_DELETABLE_INDEX_A =
            "search_index_test_softdeletabletenantentity_" + TENANT_A;

    @Autowired
    protected IndexManager indexManager;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected EntityIndexer entityIndexer;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    protected IndexingQueueManager indexingQueueManager;

    @BeforeEach
    public void setUp() {
        authenticator.begin();
        registerTenants();
        dropIndexes();
    }

    @AfterEach
    public void tearDown() {
        dropIndexes();
        removeTenants();
        authenticator.end();
    }

    /**
     * Puts the tenants into the database rather than passing their ids around: everything that works out which
     * indexes an entity has - index creation, validation, deletion without a known tenant - reads the tenants from
     * there through {@code MultitenancyService}, and a test that only passes ids never exercises that.
     */
    protected void registerTenants() {
        List.of(TENANT_A, TENANT_B).forEach(tenantId -> {
            Tenant tenant = metadata.create(Tenant.class);
            tenant.setTenantId(tenantId);
            tenant.setName(tenantId);
            dataManager.save(tenant);
        });
    }

    protected void removeTenants() {
        dataManager.load(Tenant.class).all().list().forEach(dataManager::remove);
    }

    @Test
    @DisplayName("An index is created for every tenant, and the engine accepts the generated names")
    public void indexIsCreatedPerTenant() {
        List<IndexOperationResult<IndexSynchronizationStatus>> results =
                indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_A);

        Assertions.assertTrue(results.stream().anyMatch(result -> INDEX_A.equals(result.indexName())),
                "The index of the tenant is among the synchronized ones: " + results);
        Assertions.assertTrue(indexManager.isIndexExist(INDEX_A));
        Assertions.assertFalse(indexManager.isIndexExist(INDEX_B),
                "Synchronizing one tenant must not create an index of another one");
    }

    @Test
    @DisplayName("A search in the index of a tenant returns the documents of that tenant only")
    public void searchReturnsDocumentsOfOwnTenantOnly() throws IOException {
        indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_A);
        indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_B);

        entityIndexer.index(tenantEntity("Secret of A", TENANT_A));
        entityIndexer.index(tenantEntity("Secret of B", TENANT_B));
        refresh();

        Assertions.assertEquals(List.of("Secret of A"), instanceNamesIn(INDEX_A));
        Assertions.assertEquals(List.of("Secret of B"), instanceNamesIn(INDEX_B));
    }

    @Test
    @DisplayName("A record deleted with its tenant known disappears from that tenant's index only")
    public void recordDeletedWithKnownTenantDisappears() throws IOException {
        indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_A);
        indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_B);

        TestTenantEntity ofA = tenantEntity("Secret of A", TENANT_A);
        entityIndexer.index(ofA);
        entityIndexer.index(tenantEntity("Secret of B", TENANT_B));
        refresh();

        entityIndexer.deleteCollectionByTargets(List.of(new EntityDeletionTarget(Id.of(ofA), TENANT_A)));
        refresh();

        Assertions.assertEquals(List.of(), instanceNamesIn(INDEX_A));
        Assertions.assertEquals(List.of("Secret of B"), instanceNamesIn(INDEX_B),
                "Deleting a record of one tenant must not touch the documents of another");
    }

    @Test
    @DisplayName("A record deleted without a known tenant disappears anyway: it is deleted from every index")
    public void recordDeletedWithUnknownTenantDisappears() throws IOException {
        indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_A);
        indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_B);

        TestTenantEntity ofA = tenantEntity("Secret of A", TENANT_A);
        entityIndexer.index(ofA);
        entityIndexer.index(tenantEntity("Secret of B", TENANT_B));
        refresh();

        entityIndexer.deleteCollectionByTargets(List.of(EntityDeletionTarget.tenantUnknown(Id.of(ofA))));
        refresh();

        Assertions.assertEquals(List.of(), instanceNamesIn(INDEX_A),
                "The document is found and deleted even though the tenant was not known");
        Assertions.assertEquals(List.of("Secret of B"), instanceNamesIn(INDEX_B),
                "The indexes that do not hold the document report it as missing and stay as they were");
    }

    @Test
    @DisplayName("A restored record comes back to the index of its own tenant")
    public void restoredRecordComesBackToItsTenantIndex() throws IOException {
        indexManager.synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), TENANT_A);
        indexingQueueManager.emptyQueue();

        TestSoftDeletableTenantEntity record = softDeletableEntity("Comes back", TENANT_A);
        processQueue();
        Assertions.assertEquals(List.of("Comes back"), instanceNamesIn(SOFT_DELETABLE_INDEX_A),
                "A saved record is indexed");

        dataManager.remove(record);
        processQueue();
        Assertions.assertEquals(List.of(), instanceNamesIn(SOFT_DELETABLE_INDEX_A),
                "Removal takes the document out, even though the row stays in the database");

        restore(record);
        processQueue();
        Assertions.assertEquals(List.of("Comes back"), instanceNamesIn(SOFT_DELETABLE_INDEX_A),
                "Restoration puts the document back, into the index of the tenant the record belongs to");
    }

    /**
     * Drives the whole chain the way the scheduled job does: the change listener has put items into the queue, and
     * processing them is what reaches the engine. Unlike the other scenarios here, this one does not call the
     * indexer directly - the point is that a restoration travels all the way on its own.
     */
    protected void processQueue() throws IOException {
        indexingQueueManager.processEntireQueue();
        refresh();
    }

    /**
     * Restores the record the way the Datatools add-on does: reload it with soft deletion off and clear what marked
     * it deleted.
     */
    protected void restore(TestSoftDeletableTenantEntity record) {
        Object reloaded = dataManager.load(Id.of(record))
                .hint("jmix.softDeletion", false)
                .one();
        EntityValues.setDeletedDate(reloaded, null);
        EntityValues.setDeletedBy(reloaded, null);
        dataManager.save(reloaded);
    }

    protected TestSoftDeletableTenantEntity softDeletableEntity(String name, String tenantId) {
        TestSoftDeletableTenantEntity entity = metadata.create(TestSoftDeletableTenantEntity.class);
        entity.setName(name);
        entity.setTenantId(tenantId);
        return dataManager.save(entity);
    }

    /**
     * @return instance names of the documents the index holds, sorted
     */
    protected abstract List<String> instanceNamesIn(String indexName) throws IOException;

    /**
     * Makes the documents indexed so far visible to a search.
     */
    protected abstract void refresh() throws IOException;

    protected TestTenantEntity tenantEntity(String name, String tenantId) {
        TestTenantEntity entity = metadata.create(TestTenantEntity.class);
        entity.setName(name);
        entity.setTenantId(tenantId);
        return dataManager.save(entity);
    }

    protected void dropIndexes() {
        List.of(INDEX_A, INDEX_B, SOFT_DELETABLE_INDEX_A).forEach(indexName -> {
            if (indexManager.isIndexExist(indexName)) {
                indexManager.dropIndex(indexName);
            }
        });
    }
}
