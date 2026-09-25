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

package execution_access;

import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.core.security.AccessDeniedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.SecuredDataLoadTestConfiguration;
import test_support.SecuredDataLoadTestSupport;
import test_support.role.SecCustomerReadRole;
import test_support.role.SecOrderNoReadRole;
import test_support.role.SecOrderReadRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecuredDataLoadTestConfiguration.class)
@TestPropertySource(properties = {"eclipselink.ddl-generation=create-tables", "jmix.core.work-dir=build/test-home/work"})
class JpqlAccessEntityReadTest {

    @Autowired
    SecuredDataLoadTestSupport support;

    @BeforeEach
    void setUp() {
        support.createData();
    }

    @AfterEach
    void tearDown() {
        support.deleteData();
    }

    @Test
    void execute_rootNotReadable_throwsAccessDenied() {
        support.loginAs(SecCustomerReadRole.CODE, SecOrderNoReadRole.CODE);

        assertThrows(AccessDeniedException.class,
                () -> support.execute("select o.number as number from aitls_Order o", "number"));
    }

    @Test
    void execute_joinedRootNotReadable_throwsAccessDenied() {
        support.loginAs(SecCustomerReadRole.CODE, SecOrderNoReadRole.CODE);

        assertThrows(AccessDeniedException.class,
                () -> support.execute("select c.name as name from aitls_Order o join o.customer c", "name"));
    }

    @Test
    void execute_joinedEntityNotReadableAndNotSelected_throwsAccessDenied() {
        support.loginAs(SecOrderReadRole.CODE);

        assertThrows(AccessDeniedException.class,
                () -> support.execute("select o.number as number from aitls_Order o join o.customer c", "number"));
    }

    @Test
    void execute_entityOnlyInExists_throwsAccessDenied() {
        support.loginAs(SecCustomerReadRole.CODE, SecOrderNoReadRole.CODE);

        // Nothing of the order is selected, not even inside the subquery, so the platform's own check sees no order.
        assertThrows(AccessDeniedException.class, () -> support.execute(
                "select c.name as name from aitls_Customer c "
                        + "where exists (select 1 from aitls_Order o where o.customer = c)", "name"));
    }

    @Test
    void execute_entityOnlyInSubqueryBesideReferencePath_throwsAccessDenied() {
        support.loginAs(SecOrderReadRole.CODE);

        // The path ending in the customer reference does not stand for the subquery's customers.
        assertThrows(AccessDeniedException.class, () -> support.execute(
                "select o.number as number from aitls_Order o "
                        + "where o.customer is null or (select count(c) from aitls_Customer c) > 10", "number"));
    }

    @Test
    void execute_referencePathOnly_executesWithoutReadOnReferencedEntity() {
        support.loginAs(SecOrderReadRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.number as number from aitls_Order o where o.customer is not null", "number");

        assertTrue(result.isExecuted(), () -> "not executed: " + result.getExecutionError());
        assertEquals(3, result.getRows().size());
    }

    @Test
    void execute_pathReachedEntityNotReadable_throwsAccessDenied() {
        support.loginAs(SecOrderReadRole.CODE);

        assertThrows(AccessDeniedException.class,
                () -> support.execute("select o.customer.name as name from aitls_Order o", "name"));
        assertThrows(AccessDeniedException.class, () -> support.execute(
                "select o.number as number from aitls_Order o where o.customer.name = 'VisibleCo'", "number"));
        assertThrows(AccessDeniedException.class, () -> support.execute(
                "select o.number as number from aitls_Order o where o.customer.id = 1", "number"));
    }

    @Test
    void execute_everyEntityReadable_executes() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.number as number, c.name as name from aitls_Order o join o.customer c "
                        + "where exists (select l.id from aitls_OrderLine l where l.order = o) order by o.number",
                "number", "name");

        assertTrue(result.isExecuted());
        assertEquals(3, result.getRows().size());
    }
}
