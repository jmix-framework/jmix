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

import io.jmix.aitools.dataload.execution.JpqlAccessSupport;
import io.jmix.aitools.dataload.execution.JpqlAccessConstraintException;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
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
import test_support.role.SecOrderReadRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecuredDataLoadTestConfiguration.class)
@TestPropertySource(properties = {"eclipselink.ddl-generation=create-tables", "jmix.core.work-dir=build/test-home/work"})
class JpqlAccessWholeEntityTest {

    @Autowired
    SecuredDataLoadTestSupport support;
    @Autowired
    JpqlAccessSupport accessSupport;

    @BeforeEach
    void setUp() {
        support.createData();
    }

    @AfterEach
    void tearDown() {
        support.deleteData();
    }

    @Test
    void execute_rootAliasSelected_failsWithoutRows() {
        support.loginAs(SecOrderReadRole.CODE);

        JpqlExecutionResult result = support.execute("select a as approval from aitls_OrderApproval a", "approval");

        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    void execute_referenceSelected_failsWithoutRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE);

        JpqlExecutionResult result = support.execute("select o.customer as customer from aitls_Order o", "customer");

        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    void execute_aggregateOverAlias_executes() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select count(distinct c) as cnt from aitls_Order o join o.customer c", "cnt");

        assertTrue(result.isExecuted(), () -> "not executed: " + result.getExecutionError());
        assertEquals(3L, ((Number) result.getRows().get(0).get("cnt")).longValue());
    }

    @Test
    void applyAccessConstraints_wholeEntitySelected_refuses() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE);

        assertThrows(JpqlAccessConstraintException.class,
                () -> accessSupport.applyAccessConstraints("select o.customer as customer from aitls_Order o", List.of()));
        assertThrows(JpqlAccessConstraintException.class,
                () -> accessSupport.applyAccessConstraints("select o.address as address from aitls_Order o", List.of()));
        assertThrows(JpqlAccessConstraintException.class,
                () -> accessSupport.applyAccessConstraints("select c from aitls_Order o join o.customer c", List.of()));
    }

    @Test
    void applyAccessConstraints_embeddedAttributeSelected_returnsTextUnchanged() {
        support.loginAs(SecOrderReadRole.CODE);

        String jpql = "select o.address.city as city from aitls_Order o";
        assertEquals(jpql, accessSupport.applyAccessConstraints(jpql, List.of()));
    }
}
