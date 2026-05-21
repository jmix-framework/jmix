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

import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.execution.JpqlParameterConversionService;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static io.jmix.aitools.dataload.validation.validator.UsedPropertyPathsValidator.PROPERTY_PATH_INVALID_CODE;
import static org.junit.jupiter.api.Assertions.*;

class JpqlExecutionServiceTest {

    @Test
    @DisplayName("Repairs and executes JPQL request")
    void testRepairsAndExecutesRequest() {
        TestJpqlExecutionService executionService = new TestJpqlExecutionService(List.of(
                Map.of("id", 1001L, "name", "Acme")
        ));
        ReflectionTestUtils.setField(executionService, "validateAndRepair", new SuccessfulValidationAndRepairService(
                new GeneratedJpqlResult(
                        "select e from aitols_Customer e where e.id = :id",
                        List.of(new GeneratedJpqlParameter("id", "Long", "1001")),
                        "",
                        List.of()
                ),
                true
        ));
        ReflectionTestUtils.setField(executionService, "jpqlParameterConversionService", new JpqlParameterConversionService());

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer 1001",
                "select e from aitols_Customer e where e.badField = :id",
                List.of(new JpqlExecutionParameter("id", "Long", "1001")),
                List.of("id", "name"),
                null,
                null
        ));

        assertTrue(result.getValidationResult().isValid());
        assertTrue(result.isExecuted());
        assertTrue(result.isRepaired());
        assertEquals("select e from aitols_Customer e where e.id = :id", result.getGeneratedJpqlResult().getJpql());
        assertEquals(List.of(Map.of("id", 1001L, "name", "Acme")), result.getRows());
    }

    @Test
    @DisplayName("Skips execution when JPQL remains invalid after repair")
    void testSkipsExecutionWhenResultRemainsInvalid() {
        TestJpqlExecutionService executionService = new TestJpqlExecutionService(List.of(
                Map.of("id", 1001L, "name", "Acme")
        ));
        ReflectionTestUtils.setField(executionService, "validateAndRepair", new FailedValidationAndRepairService(
                new GeneratedJpqlResult(
                        "select e from aitols_Customer e where e.fullTitle = :name",
                        List.of(new GeneratedJpqlParameter("name", "String", "Acme")),
                        "",
                        List.of()
                ),
                new JpqlValidationResult(false, List.of(
                        new JpqlValidationIssue(PROPERTY_PATH_INVALID_CODE, "Invalid property path: fullTitle")
                )),
                false
        ));
        ReflectionTestUtils.setField(executionService, "jpqlParameterConversionService", new JpqlParameterConversionService());

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer by full title",
                "select e from aitols_Customer e where e.fullTitle = :name",
                List.of(new JpqlExecutionParameter("name", "String", "Acme")),
                List.of("id", "name"),
                null,
                null
        ));

        assertFalse(result.getValidationResult().isValid());
        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
        assertEquals(0, result.getRows().size());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(PROPERTY_PATH_INVALID_CODE)));
    }

    @Test
    @DisplayName("Fails loadValues execution without result properties")
    void testFailsValuesRequestWithoutResultProperties() {
        TestJpqlExecutionService executionService = new TestJpqlExecutionService(List.of());
        ReflectionTestUtils.setField(executionService, "validateAndRepair", new FailedValidationAndRepairService(
                new GeneratedJpqlResult(
                        "select c.name, count(o) from aitols_Customer c join c.orders o group by c.name",
                        List.of(),
                        "",
                        List.of()
                ),
                new JpqlValidationResult(false, List.of(
                        new JpqlValidationIssue("resultProperties.empty",
                                "resultProperties must be specified for loadValues execution")
                )),
                false
        ));
        ReflectionTestUtils.setField(executionService, "jpqlParameterConversionService", new JpqlParameterConversionService());

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer order counts",
                "select c.name, count(o) from aitols_Customer c join c.orders o group by c.name",
                List.of(),
                List.of(),
                null,
                null
        ));

        assertFalse(result.getValidationResult().isValid());
        assertFalse(result.isExecuted());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals("resultProperties.empty")));
    }

    protected static class TestJpqlExecutionService extends JpqlExecutionService {

        protected List<Map<String, Object>> rows;

        public TestJpqlExecutionService(List<Map<String, Object>> rows) {
            this.rows = rows;
        }

        @Override
        protected List<Map<String, Object>> executeQuery(JpqlExecutionRequest request,
                                                         GeneratedJpqlResult generatedJpqlResult,
                                                         Map<String, Object> executionParameters,
                                                         Integer maxResults,
                                                         Integer firstResult) {
            return rows;
        }
    }

    protected static class SuccessfulValidationAndRepairService extends JpqlValidationAndRepairService {

        protected GeneratedJpqlResult generatedResult;
        protected boolean repaired;

        public SuccessfulValidationAndRepairService(GeneratedJpqlResult generatedResult, boolean repaired) {
            this.generatedResult = generatedResult;
            this.repaired = repaired;
        }

        @Override
        protected OperationResult validateAndRepair(JpqlExecutionRequest request) {
            JpqlRepairResult repairResult = repaired
                    ? new JpqlRepairResult(generatedResult, new JpqlValidationResult(true, List.of()), 1, true)
                    : null;
            return OperationResult.success(request, generatedResult, new JpqlValidationResult(true, List.of()), repairResult);
        }
    }

    protected static class FailedValidationAndRepairService extends JpqlValidationAndRepairService {

        protected GeneratedJpqlResult generatedResult;
        protected JpqlValidationResult validationResult;
        protected boolean repaired;

        public FailedValidationAndRepairService(GeneratedJpqlResult generatedResult,
                                                JpqlValidationResult validationResult,
                                                boolean repaired) {
            this.generatedResult = generatedResult;
            this.validationResult = validationResult;
            this.repaired = repaired;
        }

        @Override
        protected OperationResult validateAndRepair(JpqlExecutionRequest request) {
            JpqlRepairResult repairResult = repaired
                    ? new JpqlRepairResult(generatedResult, validationResult, 1, true)
                    : null;
            return OperationResult.failed(request, generatedResult, validationResult, repairResult);
        }
    }
}
