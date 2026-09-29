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

package execution;

import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.repair.JpqlRepairer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.SecuredDataLoadTestConfiguration;
import test_support.SecuredDataLoadTestSupport;
import test_support.role.SecCustomerReadRole;
import test_support.role.SecOrderReadRole;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService.RESULT_PROPERTIES_MISMATCH_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecuredDataLoadTestConfiguration.class,
        RepairedQueryColumnsTest.StubRepairerConfiguration.class})
@TestPropertySource(properties = {"eclipselink.ddl-generation=create-tables", "jmix.core.work-dir=build/test-home/work"})
class RepairedQueryColumnsTest {

    // The reserved alias "user" fails validation, so the query is repaired.
    static final String GENERATED_JPQL = "select o.number as orderNumber, c.name as user"
            + " from aitls_Order o join o.customer c where o.number = 'ORD-VisibleCo'";

    static final AtomicReference<String> REPAIRED_JPQL = new AtomicReference<>();

    @Autowired
    SecuredDataLoadTestSupport support;

    @BeforeEach
    void setUp() {
        support.createData();
        support.loginAs(SecCustomerReadRole.CODE, SecOrderReadRole.CODE);
    }

    @AfterEach
    void tearDown() {
        support.deleteData();
    }

    @Test
    void execute_repairRenamesAliasInPlace_keepsRequestedColumnNames() {
        REPAIRED_JPQL.set("select o.number as orderNumber, c.name as customerName"
                + " from aitls_Order o join o.customer c where o.number = 'ORD-VisibleCo'");

        JpqlExecutionResult result = support.execute(GENERATED_JPQL, "orderNumber", "user");

        assertTrue(result.isRepaired());
        assertTrue(result.isExecuted());
        assertEquals(List.of(Map.of("orderNumber", "ORD-VisibleCo", "user", "VisibleCo")), result.getRows());
    }

    @Test
    void execute_repairDropsSelectedValue_isRefused() {
        // A repair that dropped the first selected value, as an LLM repair may do: the remaining value would
        // otherwise come back under the first requested name.
        REPAIRED_JPQL.set("select c.name as customerName from aitls_Order o join o.customer c"
                + " where o.number = 'ORD-VisibleCo'");

        JpqlExecutionResult result = support.execute(GENERATED_JPQL, "orderNumber", "user");

        assertTrue(result.isRepaired());
        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(RESULT_PROPERTIES_MISMATCH_CODE)));
    }

    @Configuration
    static class StubRepairerConfiguration {

        @Bean
        JpqlRepairer stubRepairer() {
            return request -> new GeneratedJpqlResult(REPAIRED_JPQL.get(), List.of(), "", List.of());
        }
    }
}
