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
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.execution.JpqlParameterConversionService;
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService;
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService.OperationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.data.QueryTransformerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.DataAccessTestConfiguration;
import test_support.DenyingLoadValuesConstraint;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DataAccessTestConfiguration.class})
class JpqlExecutionServiceColumnAccessTest {

    private static final String TWO_COLUMN_JPQL =
            "select c.id as cid, c.name as cname from aitls_Customer c";

    @Autowired
    AccessManager accessManager;
    @Autowired
    QueryTransformerFactory queryTransformerFactory;
    @Autowired
    Metadata metadata;
    @Autowired
    DenyingLoadValuesConstraint denyingConstraint;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeEach
    void authenticate() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
        denyingConstraint.reset();
    }

    @Test
    @DisplayName("Omits a denied column and keeps the readable ones")
    void testOmitsDeniedColumnAmongSeveral() {
        denyingConstraint.denySelectedPath("name");

        TestJpqlExecutionService service = createService(
                List.of(Map.of("cid", 1L, "cname", "Acme")));

        JpqlExecutionResult result = service.execute(new JpqlExecutionRequest(
                "Show customers", TWO_COLUMN_JPQL, List.of(), List.of("cid", "cname"), null, null));

        assertTrue(result.isExecuted());
        assertEquals(1, result.getRows().size());

        Map<String, Object> row = result.getRows().get(0);
        assertTrue(row.containsKey("cid"));
        assertFalse(row.containsKey("cname"));
        assertEquals(1L, row.get("cid"));
    }

    @Test
    @DisplayName("Returns an empty, non-executed result when all columns are denied")
    void testReturnsNonExecutedResultWhenAllColumnsDenied() {
        denyingConstraint.denySelectedPath("id");
        denyingConstraint.denySelectedPath("name");

        TestJpqlExecutionService service = createService(
                List.of(Map.of("cid", 1L, "cname", "Acme")));

        JpqlExecutionResult result = service.execute(new JpqlExecutionRequest(
                "Show customers", TWO_COLUMN_JPQL, List.of(), List.of("cid", "cname"), null, null));

        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    @DisplayName("Rejects the query when the entity itself is not readable")
    void testRejectsWhenEntityNotReadable() {
        denyingConstraint.denyEntity();

        TestJpqlExecutionService service = createService(List.of());

        assertThrows(AccessDeniedException.class, () -> service.execute(new JpqlExecutionRequest(
                "Show customers", TWO_COLUMN_JPQL, List.of(), List.of("cid", "cname"), null, null)));
    }

    TestJpqlExecutionService createService(List<Map<String, Object>> stubbedRows) {
        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(
                TWO_COLUMN_JPQL, List.of(), "", List.of());

        JpqlValidationAndRepairService validateAndRepair = mock(JpqlValidationAndRepairService.class);
        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(OperationResult.success(new JpqlExecutionRequest(), generatedResult,
                        new JpqlValidationResult(true, List.of()), null));

        JpqlParameterConversionService parameterConversionService = mock(JpqlParameterConversionService.class);
        when(parameterConversionService.convert(anyList())).thenReturn(Map.of());

        TestJpqlExecutionService service = new TestJpqlExecutionService(stubbedRows);
        ReflectionTestUtils.setField(service, "validateAndRepair", validateAndRepair);
        ReflectionTestUtils.setField(service, "jpqlParameterConversionService", parameterConversionService);
        ReflectionTestUtils.setField(service, "accessManager", accessManager);
        ReflectionTestUtils.setField(service, "queryTransformerFactory", queryTransformerFactory);
        ReflectionTestUtils.setField(service, "metadata", metadata);
        ReflectionTestUtils.setField(service, "dataLoadProperties",
                new AiToolsDataLoadProperties(true, true, 1, 20, 200, null, null, null, null));
        return service;
    }

    static class TestJpqlExecutionService extends JpqlExecutionService {

        private final List<Map<String, Object>> stubbedRows;

        TestJpqlExecutionService(List<Map<String, Object>> stubbedRows) {
            this.stubbedRows = stubbedRows;
        }

        @Override
        protected ExecutionRows executeQuery(JpqlExecutionRequest request,
                                             GeneratedJpqlResult generatedJpqlResult,
                                             Map<String, Object> executionParameters,
                                             Integer maxResults,
                                             Integer firstResult) {
            return createExecutionRows(stubbedRows, false);
        }
    }
}
