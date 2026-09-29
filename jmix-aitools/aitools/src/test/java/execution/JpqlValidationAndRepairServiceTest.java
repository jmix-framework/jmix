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
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService;
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService.OperationResult;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.repair.JpqlRepairService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService.RESULT_PROPERTIES_MISMATCH_CODE;
import static io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService.RESULT_PROPERTIES_UNREADABLE_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpqlValidationAndRepairServiceTest {

    static final JpqlValidationResult VALID = new JpqlValidationResult(true, List.of());
    static final JpqlValidationResult INVALID = new JpqlValidationResult(false,
            List.of(new JpqlValidationIssue("jpql.reservedAlias", "Reserved word used as alias: user")));

    JpqlValidationService validationService = mock(JpqlValidationService.class);
    JpqlRepairService repairService = mock(JpqlRepairService.class);
    JpqlValidationAndRepairService service = new JpqlValidationAndRepairService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "jpqlValidationService", validationService);
        ReflectionTestUtils.setField(service, "jpqlRepairService", repairService);
    }

    @Test
    void validateAndRepair_repairRenamedAliasInPlace_succeeds() {
        repairInto("select o.number as orderNumber, c.name as customerName, o.total as orderTotal"
                + " from aitls_Order o join o.customer c");

        OperationResult result = service.validateAndRepair(request(
                "select o.number as orderNumber, c.name as user, o.total as orderTotal"
                        + " from aitls_Order o join o.customer c",
                "orderNumber", "user", "orderTotal"));

        assertFalse(result.isFailed());
        assertTrue(result.isRepaired());
    }

    @Test
    void validateAndRepair_repairDroppedSelectedValue_fails() {
        repairInto("select c.name as customerName, o.total as orderTotal from aitls_Order o join o.customer c");

        OperationResult result = service.validateAndRepair(request(
                "select o.number as orderNumber, c.name as user, o.total as orderTotal"
                        + " from aitls_Order o join o.customer c",
                "orderNumber", "user", "orderTotal"));

        assertTrue(result.isFailed());
        assertTrue(result.isRepaired());
        assertHasIssue(result, RESULT_PROPERTIES_MISMATCH_CODE);
        // The guidance is a ready next step for the model: the repaired query's own aliases, ready to paste.
        assertTrue(issueGuidance(result, RESULT_PROPERTIES_MISMATCH_CODE)
                .contains("resultProperties set to [customerName, orderTotal]"));
    }

    @Test
    void validateAndRepair_repairMovedSelectedValue_fails() {
        repairInto("select o.total as orderTotal, o.number as orderNumber from aitls_Order o");

        OperationResult result = service.validateAndRepair(request(
                "select o.number as orderNumber, o.total as user from aitls_Order o", "orderNumber", "user"));

        assertTrue(result.isFailed());
        assertHasIssue(result, RESULT_PROPERTIES_MISMATCH_CODE);
    }

    @Test
    void validateAndRepair_repairFixedExpressionKeepingAlias_succeeds() {
        repairInto("select o.number as orderNumber, c.name as customerName from aitls_Order o join o.customer c");

        OperationResult result = service.validateAndRepair(request(
                "select o.numbr as orderNumber, c.name as customerName from aitls_Order o join o.customer c",
                "orderNumber", "customerName"));

        assertFalse(result.isFailed());
    }

    @Test
    void validateAndRepair_repairChangedExpressionAndAliasAtSamePosition_fails() {
        repairInto("select c.firstName as login, o.total as orderTotal from aitls_Order o join o.customer c");

        OperationResult result = service.validateAndRepair(request(
                "select c.name as user, o.total as orderTotal from aitls_Order o join o.customer c",
                "user", "orderTotal"));

        assertTrue(result.isFailed());
        assertHasIssue(result, RESULT_PROPERTIES_MISMATCH_CODE);
    }

    @Test
    void validateAndRepair_repairedQueryWithUnaliasedValue_fails() {
        repairInto("select c.name, o.total as orderTotal from aitls_Order o join o.customer c");

        OperationResult result = service.validateAndRepair(request(
                "select c.name as user, o.total as orderTotal from aitls_Order o join o.customer c",
                "user", "orderTotal"));

        assertTrue(result.isFailed());
        assertHasIssue(result, RESULT_PROPERTIES_UNREADABLE_CODE);
    }

    @Test
    void validateAndRepair_fewerNamesThanSelectedValues_fails() {
        noRepairNeeded();

        OperationResult result = service.validateAndRepair(request(
                "select o.number as orderNumber, o.total as orderTotal from aitls_Order o", "orderNumber"));

        assertTrue(result.isFailed());
        assertHasIssue(result, RESULT_PROPERTIES_MISMATCH_CODE);
    }

    @Test
    void validateAndRepair_namesInOtherOrderThanAliases_fails() {
        noRepairNeeded();

        OperationResult result = service.validateAndRepair(request(
                "select o.number as orderNumber, o.total as orderTotal from aitls_Order o",
                "orderTotal", "orderNumber"));

        assertTrue(result.isFailed());
        assertHasIssue(result, RESULT_PROPERTIES_MISMATCH_CODE);
    }

    @Test
    void validateAndRepair_namesMatchAliasesIgnoringCase_succeeds() {
        noRepairNeeded();

        OperationResult result = service.validateAndRepair(request(
                "select o.number as OrderNumber, o.total as orderTotal from aitls_Order o",
                "orderNumber", "orderTotal"));

        assertFalse(result.isFailed());
    }

    @Test
    void validateAndRepair_unaliasedValueBesideMatchingAlias_succeeds() {
        noRepairNeeded();

        OperationResult result = service.validateAndRepair(request(
                "select o.number, o.total as orderTotal from aitls_Order o", "orderNumber", "orderTotal"));

        assertFalse(result.isFailed());
    }

    @Test
    void validateAndRepair_partiallyAliasedNamesInOtherOrder_fails() {
        noRepairNeeded();

        OperationResult result = service.validateAndRepair(request(
                "select o.total as orderTotal, o.number from aitls_Order o", "orderNumber", "orderTotal"));

        assertTrue(result.isFailed());
        assertHasIssue(result, RESULT_PROPERTIES_MISMATCH_CODE);
    }

    void repairInto(String repairedJpql) {
        GeneratedJpqlResult repaired = new GeneratedJpqlResult(repairedJpql, List.of(), "", List.of());
        when(validationService.validate(any())).thenReturn(INVALID, VALID);
        when(repairService.repairIfNeeded(any(), any(), any()))
                .thenReturn(new JpqlRepairResult(repaired, VALID, 1, true));
    }

    void noRepairNeeded() {
        when(validationService.validate(any())).thenReturn(VALID);
        when(repairService.repairIfNeeded(any(), any(), any()))
                .thenAnswer(invocation -> new JpqlRepairResult(invocation.getArgument(1), VALID, 0, false));
    }

    JpqlExecutionRequest request(String jpql, String... resultProperties) {
        return new JpqlExecutionRequest("test", jpql, List.of(), List.of(resultProperties), null, null);
    }

    String issueGuidance(OperationResult result, String code) {
        return result.getValidationResult().getIssues().stream()
                .filter(issue -> issue.getCode().equals(code))
                .map(JpqlValidationIssue::getGuidance)
                .findFirst()
                .orElseThrow();
    }

    void assertHasIssue(OperationResult result, String code) {
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(code)), "no issue " + code);
        assertEquals(false, result.getValidationResult().isValid());
    }
}
