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
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.data.QueryTransformerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.AiToolsTestConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the defense-in-depth guard that keeps a {@code @Secret} attribute out of the result rows
 * even when the query bypasses JPQL validation (here the validate/repair step is stubbed to success).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class JpqlExecutionServiceSecretColumnTest {

    private static final String SECRET_AND_PLAIN_JPQL =
            "select c.id as cid, c.secretToken as ctoken from aitls_Customer c";
    private static final String SECRET_ONLY_JPQL =
            "select c.secretToken as ctoken from aitls_Customer c";

    @Autowired
    QueryTransformerFactory queryTransformerFactory;
    @Autowired
    Metadata metadata;
    @Autowired
    MetadataTools metadataTools;

    @Test
    @DisplayName("Drops a @Secret column and keeps the readable ones")
    void testDropsSecretColumnAmongSeveral() {
        TestJpqlExecutionService service = createService(SECRET_AND_PLAIN_JPQL,
                List.of(Map.of("cid", 1L, "ctoken", "s3cret")));

        JpqlExecutionResult result = service.execute(new JpqlExecutionRequest(
                "Show customers", SECRET_AND_PLAIN_JPQL, List.of(), List.of("cid", "ctoken"), null, null));

        assertTrue(result.isExecuted());
        assertEquals(1, result.getRows().size());

        Map<String, Object> row = result.getRows().get(0);
        assertTrue(row.containsKey("cid"));
        assertFalse(row.containsKey("ctoken"));
        assertEquals(1L, row.get("cid"));
    }

    @Test
    @DisplayName("Returns an empty, non-executed result when the only column is @Secret")
    void testReturnsNonExecutedResultWhenOnlyColumnSecret() {
        TestJpqlExecutionService service = createService(SECRET_ONLY_JPQL,
                List.of(Map.of("ctoken", "s3cret")));

        JpqlExecutionResult result = service.execute(new JpqlExecutionRequest(
                "Show customers", SECRET_ONLY_JPQL, List.of(), List.of("ctoken"), null, null));

        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
    }

    TestJpqlExecutionService createService(String jpql, List<Map<String, Object>> stubbedRows) {
        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(jpql, List.of(), "", List.of());

        JpqlValidationAndRepairService validateAndRepair = mock(JpqlValidationAndRepairService.class);
        when(validateAndRepair.validateAndRepair(any()))
                .thenReturn(OperationResult.success(new JpqlExecutionRequest(), generatedResult,
                        new JpqlValidationResult(true, List.of()), null));

        JpqlParameterConversionService parameterConversionService = mock(JpqlParameterConversionService.class);
        when(parameterConversionService.convert(anyList())).thenReturn(Map.of());

        TestJpqlExecutionService service = new TestJpqlExecutionService(stubbedRows);
        ReflectionTestUtils.setField(service, "validateAndRepair", validateAndRepair);
        ReflectionTestUtils.setField(service, "jpqlParameterConversionService", parameterConversionService);
        // accessManager is left null: this isolates the @Secret guard from the security column filtering.
        ReflectionTestUtils.setField(service, "queryTransformerFactory", queryTransformerFactory);
        ReflectionTestUtils.setField(service, "metadata", metadata);
        ReflectionTestUtils.setField(service, "metadataTools", metadataTools);
        ReflectionTestUtils.setField(service, "dataLoadProperties",
                new AiToolsDataLoadProperties(true, true, true, 1, 20, 200, null, null, null, null));
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
