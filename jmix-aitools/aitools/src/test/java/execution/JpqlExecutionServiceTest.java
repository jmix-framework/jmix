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

import io.jmix.aitools.AiToolsDataLoadProperties;
import io.jmix.aitools.dataload.execution.*;
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService.OperationResult;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static io.jmix.aitools.dataload.validation.validator.UsedPropertyPathsValidator.PROPERTY_PATH_INVALID_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpqlExecutionServiceTest {

    @Mock
    JpqlValidationAndRepairService validateAndRepair;

    @Mock
    JpqlParameterConversionService jpqlParameterConversionService;

    @Test
    @DisplayName("Repairs and executes JPQL request")
    void testRepairsAndExecutesRequest() {
        TestJpqlExecutionService executionService = createService();
        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(
                "select e from aitls_Customer e where e.id = :id",
                List.of(new GeneratedJpqlParameter("id", "Long", "1001")),
                "",
                List.of());

        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(
                        OperationResult.success(new JpqlExecutionRequest(),
                                generatedResult,
                                new JpqlValidationResult(true, List.of()),
                                new JpqlRepairResult(generatedResult, new JpqlValidationResult(true, List.of()), 1, true)));

        when(jpqlParameterConversionService.convert(anyList()))
                .thenReturn(Map.of("id", 1001L));

        executionService.stubRows(List.of(Map.of("id", 1001L, "name", "Acme")), false);

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer 1001",
                "select e from aitls_Customer e where e.badField = :id",
                List.of(new JpqlExecutionParameter("id", "Long", "1001")),
                List.of("id", "name"),
                null,
                null));

        assertTrue(result.getValidationResult().isValid());
        assertTrue(result.isExecuted());
        assertTrue(result.isRepaired());
        assertFalse(result.isHasMore());
        assertEquals("select e from aitls_Customer e where e.id = :id", result.getGeneratedJpqlResult().getJpql());
        assertEquals(List.of(Map.of("id", 1001L, "name", "Acme")), result.getRows());
    }

    @Test
    @DisplayName("Skips execution when JPQL remains invalid after repair")
    void testSkipsExecutionWhenResultRemainsInvalid() {
        TestJpqlExecutionService executionService = createService();

        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(
                "select e from aitls_Customer e where e.fullTitle = :name",
                List.of(new GeneratedJpqlParameter("name", "String", "Acme")),
                "",
                List.of());

        JpqlValidationResult validationResult = new JpqlValidationResult(false, List.of(
                new JpqlValidationIssue(PROPERTY_PATH_INVALID_CODE, "Invalid property path: fullTitle")));

        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(
                        OperationResult.failed(
                                new JpqlExecutionRequest(), generatedResult, validationResult, null));

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer by full title",
                "select e from aitls_Customer e where e.fullTitle = :name",
                List.of(new JpqlExecutionParameter("name", "String", "Acme")),
                List.of("id", "name"),
                null,
                null));

        assertFalse(result.getValidationResult().isValid());
        assertFalse(result.isExecuted());
        assertFalse(result.isHasMore());
        assertTrue(result.getRows().isEmpty());
        assertEquals(0, result.getRows().size());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(PROPERTY_PATH_INVALID_CODE)));
    }

    @Test
    @DisplayName("Fails loadValues execution without result properties")
    void testFailsValuesRequestWithoutResultProperties() {
        TestJpqlExecutionService executionService = createService();
        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(
                "select c.name, count(o) from aitls_Customer c join c.orders o group by c.name",
                List.of(),
                "",
                List.of());

        JpqlValidationResult validationResult = new JpqlValidationResult(false, List.of(
                new JpqlValidationIssue("resultProperties.empty",
                        "resultProperties must be specified for loadValues execution")));

        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(
                        OperationResult.failed(new JpqlExecutionRequest(), generatedResult, validationResult, null));

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer order counts",
                "select c.name, count(o) from aitls_Customer c join c.orders o group by c.name",
                List.of(),
                List.of(),
                null,
                null));

        assertFalse(result.getValidationResult().isValid());
        assertFalse(result.isExecuted());
        assertFalse(result.isHasMore());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals("resultProperties.empty")));
    }

    @Test
    @DisplayName("Sets hasMore when one extra row is available")
    void testSetsHasMoreWhenExtraRowExists() {
        TestJpqlExecutionService executionService = createService();
        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(
                "select e.id as id from aitls_Customer e",
                List.of(),
                "",
                List.of(),
                2,
                0);

        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(
                        OperationResult.success(
                                new JpqlExecutionRequest(), generatedResult,
                                new JpqlValidationResult(true, List.of()), null));

        when(jpqlParameterConversionService.convert(anyList())).thenReturn(Map.of());

        executionService.stubRows(List.of(Map.of("id", 1L), Map.of("id", 2L)), true);

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customers",
                "select e.id as id from aitls_Customer e",
                List.of(),
                List.of("id"),
                2,
                0));

        assertTrue(result.isExecuted());
        assertTrue(result.isHasMore());
        assertEquals(2, result.getRows().size());
    }

    @Test
    @DisplayName("Does not set hasMore when all rows fit into maxResults")
    void testDoesNotSetHasMoreWhenAllRowsFit() {
        TestJpqlExecutionService executionService = createService();
        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(
                "select e.id as id from aitls_Customer e",
                List.of(), "", List.of(), 2, 0);

        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(
                        OperationResult.success(
                                new JpqlExecutionRequest(), generatedResult,
                                new JpqlValidationResult(true, List.of()), null));

        when(jpqlParameterConversionService.convert(anyList())).thenReturn(Map.of());

        executionService.stubRows(List.of(Map.of("id", 1L), Map.of("id", 2L)), false);

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customers",
                "select e.id as id from aitls_Customer e",
                List.of(),
                List.of("id"),
                2,
                0));

        assertTrue(result.isExecuted());
        assertFalse(result.isHasMore());
        assertEquals(2, result.getRows().size());
    }

    @Test
    @DisplayName("Caps maxResults to the configured limit")
    void testCapsMaxResultsToConfiguredLimit() {
        TestJpqlExecutionService executionService = createService();
        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(
                "select e.id as id from aitls_Customer e",
                List.of(), "", List.of(), 1000, 0);

        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(
                        OperationResult.success(
                                new JpqlExecutionRequest(), generatedResult,
                                new JpqlValidationResult(true, List.of()), null));

        when(jpqlParameterConversionService.convert(anyList())).thenReturn(Map.of());

        executionService.stubRows(List.of(Map.of("id", 1L)), false);

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customers",
                "select e.id as id from aitls_Customer e",
                List.of(),
                List.of("id"),
                1000,
                0));

        assertTrue(result.isExecuted());
        assertEquals(200, result.getMaxResults().intValue());
    }

    TestJpqlExecutionService createService() {
        TestJpqlExecutionService executionService = new TestJpqlExecutionService();
        ReflectionTestUtils.setField(executionService, "validateAndRepair", validateAndRepair);
        ReflectionTestUtils.setField(executionService, "jpqlParameterConversionService", jpqlParameterConversionService);
        ReflectionTestUtils.setField(executionService, "dataLoadProperties",
                new AiToolsDataLoadProperties(true, true, 1, 20, 200, null, null, null, null));
        return executionService;
    }

    static class TestJpqlExecutionService extends JpqlExecutionService {

        private List<Map<String, Object>> stubbedRows = List.of();
        private boolean stubbedHasMore;

        void stubRows(List<Map<String, Object>> rows, boolean hasMore) {
            this.stubbedRows = rows;
            this.stubbedHasMore = hasMore;
        }

        @Override
        protected ExecutionRows executeQuery(JpqlExecutionRequest request,
                                             GeneratedJpqlResult generatedJpqlResult,
                                             Map<String, Object> executionParameters,
                                             Integer maxResults,
                                             Integer firstResult) {
            return createExecutionRows(stubbedRows, stubbedHasMore);
        }
    }
}
