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
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.repair.JpqlRepairService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JpqlExecutionServiceTest {

    @Test
    @DisplayName("Repairs and executes JPQL request")
    void testRepairsAndExecutesRequest() {
        TestJpqlExecutionService executionService = new TestJpqlExecutionService(List.of(
                Map.of("id", 1001L, "name", "Acme")
        ));
        ReflectionTestUtils.setField(executionService, "jpqlValidationService", new SwitchingValidationService());
        ReflectionTestUtils.setField(executionService, "jpqlRepairService", new RepairingJpqlRepairService());
        ReflectionTestUtils.setField(executionService, "jpqlParameterConversionService", new JpqlParameterConversionService());

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer 1001",
                "select e from aitols_Customer e where e.badField = :id",
                "aitols_Customer",
                List.of(new JpqlExecutionParameter("id", "Long", "1001")),
                List.of("aitols_Customer"),
                List.of("badField"),
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
        ReflectionTestUtils.setField(executionService, "jpqlValidationService", new AlwaysInvalidValidationService());
        ReflectionTestUtils.setField(executionService, "jpqlRepairService", new NoOpJpqlRepairService());
        ReflectionTestUtils.setField(executionService, "jpqlParameterConversionService", new JpqlParameterConversionService());

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer by full title",
                "select e from aitols_Customer e where e.fullTitle = :name",
                "aitols_Customer",
                List.of(new JpqlExecutionParameter("name", "String", "Acme")),
                List.of("aitols_Customer"),
                List.of("fullTitle"),
                List.of("id", "name"),
                null,
                null
        ));

        assertFalse(result.getValidationResult().isValid());
        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
        assertEquals(0, result.getRows().size());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals("propertyPath.invalid")));
    }

    @Test
    @DisplayName("Fails loadValues execution without result properties")
    void testFailsValuesRequestWithoutResultProperties() {
        TestJpqlExecutionService executionService = new TestJpqlExecutionService(List.of());
        ReflectionTestUtils.setField(executionService, "jpqlValidationService", new SwitchingValidationService());
        ReflectionTestUtils.setField(executionService, "jpqlRepairService", new NoOpJpqlRepairService());
        ReflectionTestUtils.setField(executionService, "jpqlParameterConversionService", new JpqlParameterConversionService());

        JpqlExecutionResult result = executionService.execute(new JpqlExecutionRequest(
                "Show customer order counts",
                "select c.name, count(o) from aitols_Customer c join c.orders o group by c.name",
                "aitols_Customer",
                List.of(),
                List.of("aitols_Customer", "aitols_Order"),
                List.of("orders", "name"),
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

    protected static class SwitchingValidationService extends JpqlValidationService {

        @Override
        public JpqlValidationResult validate(GeneratedJpqlResult generatedJpqlResult) {
            if (generatedJpqlResult.getJpql().contains("badField")) {
                return new JpqlValidationResult(false, List.of(
                        new JpqlValidationIssue("propertyPath.invalid", "Invalid property path: badField")
                ));
            }
            return new JpqlValidationResult(true, List.of());
        }
    }

    protected static class AlwaysInvalidValidationService extends JpqlValidationService {

        @Override
        public JpqlValidationResult validate(GeneratedJpqlResult generatedJpqlResult) {
            return new JpqlValidationResult(false, List.of(
                    new JpqlValidationIssue("propertyPath.invalid", "Invalid property path: fullTitle")
            ));
        }
    }

    protected static class RepairingJpqlRepairService extends JpqlRepairService {

        @Override
        public JpqlRepairResult repairIfNeeded(JpqlExecutionRequest generationRequest,
                                               GeneratedJpqlResult generatedJpqlResult,
                                               JpqlValidationResult validationResult) {
            GeneratedJpqlResult repairedResult = new GeneratedJpqlResult(
                    "select e from aitols_Customer e where e.id = :id",
                    "aitols_Customer",
                    generatedJpqlResult.getParameters(),
                    List.of("aitols_Customer"),
                    List.of("id"),
                    "",
                    List.of()
            );
            return new JpqlRepairResult(repairedResult, new JpqlValidationResult(true, List.of()), 1, true);
        }
    }

    protected static class NoOpJpqlRepairService extends JpqlRepairService {

        @Override
        public JpqlRepairResult repairIfNeeded(JpqlExecutionRequest generationRequest,
                                               GeneratedJpqlResult generatedJpqlResult,
                                               JpqlValidationResult validationResult) {
            return new JpqlRepairResult(generatedJpqlResult, validationResult, 0, false);
        }
    }
}
