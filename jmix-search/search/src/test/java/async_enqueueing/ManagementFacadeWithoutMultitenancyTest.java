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

package async_enqueueing;

import io.jmix.search.index.EntityIndexingManagementFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AsyncEnqueueingTestConfiguration;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The management operations of an application that has no multitenancy add-on.
 * <p>
 * The context of this test gets {@code NoopMultitenancyAdapter}, so {@code isMultitenancyActive()} is false and
 * the add-on guard throws exactly as it would in an application without the module. An empty tenant field means
 * "every tenant", which in such an application is the single index an entity has - so every operation must work,
 * and none of them may demand an add-on that is not there.
 * <p>
 * There was no integration test over this facade at all, which is how the opposite behaviour survived: an empty
 * field reached the addressed overload and the operation answered with {@code IllegalStateException}.
 * <p>
 * What this test does not cover: the index operations run against {@code TestNoopIndexManager}, which answers for
 * every configuration whatever tenant it is given. Scope resolution and the add-on guard are real here, the
 * engine side is not.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AsyncEnqueueingTestConfiguration.class})
public class ManagementFacadeWithoutMultitenancyTest {

    private static final String ENTITY = "test_RootEntity";
    private static final String MULTITENANCY_FAILURE = "Multitenancy is not available";

    @Autowired
    private EntityIndexingManagementFacade facade;

    private List<Operation> scopedOperations() {
        return List.of(
                new Operation("validateIndexes", facade::validateIndexes),
                new Operation("synchronizeIndexSchemas", facade::synchronizeIndexSchemas),
                new Operation("recreateIndexes", facade::recreateIndexes),
                new Operation("deleteIndexes", facade::deleteIndexes),
                new Operation("enqueueIndexAll", facade::enqueueIndexAll),
                new Operation("emptyIndexingQueue", facade::emptyIndexingQueue),
                new Operation("initAsyncEnqueueing", facade::initAsyncEnqueueing),
                new Operation("suspendAsyncEnqueueing", facade::suspendAsyncEnqueueing),
                new Operation("resumeAsyncEnqueueing", facade::resumeAsyncEnqueueing),
                new Operation("terminateAsyncEnqueueing", facade::terminateAsyncEnqueueing),
                new Operation("enqueueNextBatch", facade::enqueueNextBatch)
        );
    }

    @Test
    @DisplayName("Both fields empty: every operation covers the whole application and asks for no add-on")
    public void bothFieldsEmpty() {
        for (Operation operation : scopedOperations()) {
            assertWorks(operation, "", "");
        }
    }

    @Test
    @DisplayName("Blanks are the same as empty: a field of spaces is not a tenant id")
    public void fieldsOfSpaces() {
        for (Operation operation : scopedOperations()) {
            assertWorks(operation, "   ", "   ");
        }
    }

    @Test
    @DisplayName("An entity with an empty tenant: the single index of that entity, not an add-on error")
    public void entityWithoutTenant() {
        for (Operation operation : scopedOperations()) {
            assertWorks(operation, ENTITY, "");
        }
    }

    @Test
    @DisplayName("An entity name padded with spaces is accepted, because console values are typed by hand")
    public void paddedEntityName() {
        String padded = facade.emptyIndexingQueue("  " + ENTITY + "  ", "");
        String plain = facade.emptyIndexingQueue(ENTITY, "");

        Assertions.assertEquals(plain, padded,
                "a padded entity name must address the same entity as the plain one");
    }

    @Test
    @DisplayName("Listing the entities of sessions without a tenant lists them instead of failing")
    public void entityNamesOfSessions() {
        assertNotNull(facade.getEntityNamesOfAsyncEnqueueingSessions(""));
        assertNotNull(facade.getEntityNamesOfAsyncEnqueueingSessions("   "));
    }

    @Test
    @DisplayName("A named tenant is still refused, because there is no add-on that could have one")
    public void namedTenantIsRefused() {
        String result = facade.initAsyncEnqueueing("", "acme");

        assertTrue(result.contains("does not exist"),
                "an operation that creates something must refuse a tenant that cannot exist: " + result);
    }

    private void assertWorks(Operation operation, String entityName, String tenantId) {
        String result;
        try {
            result = operation.action().apply(entityName, tenantId);
        } catch (RuntimeException e) {
            throw new AssertionError(String.format(
                    "%s('%s', '%s') failed in an application without multitenancy", operation.name(),
                    entityName, tenantId), e);
        }
        assertNotNull(result, operation.name() + " answered nothing");
        assertFalse(result.contains(MULTITENANCY_FAILURE),
                String.format("%s('%s', '%s') demanded the add-on: %s", operation.name(), entityName, tenantId,
                        result));
    }

    private record Operation(String name, BiFunction<String, String, String> action) {
    }
}
