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

package entity_dataload;

import io.jmix.aitools.dataload.impl.AiDataLoadServiceImpl;
import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.EntityDataLoadResult;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiDataLoadServiceTest {

    @Test
    @DisplayName("Loads structured entity data without conversation id")
    void testLoadsStructuredEntityData() {
        EntityDataLoadGenerationService entityDataLoadGenerationService = mock(EntityDataLoadGenerationService.class);
        JpqlExecutionService jpqlExecutionService = mock(JpqlExecutionService.class);
        AiDataLoadServiceImpl service = createService(entityDataLoadGenerationService, jpqlExecutionService);

        EntityDataLoadQuery query = new EntityDataLoadQuery(
                "select c.name as clientName from aitls_Customer c where c.id = :id",
                List.of(new GeneratedJpqlParameter("id", "Long", "10")),
                List.of("clientName"),
                "Load one customer",
                List.of(),
                5,
                0
        );
        JpqlExecutionResult executionResult = new JpqlExecutionResult(
                new GeneratedJpqlResult(
                        query.getJpql(),
                        query.getParameters(),
                        query.getExplanation(),
                        query.getWarnings(),
                        query.getMaxResults(),
                        query.getFirstResult()
                ),
                new JpqlValidationResult(true, List.of()),
                List.of(Map.of("clientName", "Acme")),
                5,
                0,
                true,
                false,
                true,
                null
        );

        when(entityDataLoadGenerationService.generate("show customer")).thenReturn(query);
        when(jpqlExecutionService.execute(any())).thenReturn(executionResult);

        EntityDataLoadResult result = service.loadData("show customer");

        ArgumentCaptor<JpqlExecutionRequest> requestCaptor = ArgumentCaptor.forClass(JpqlExecutionRequest.class);
        verify(jpqlExecutionService).execute(requestCaptor.capture());
        JpqlExecutionRequest request = requestCaptor.getValue();

        assertEquals("show customer", request.getUserText());
        assertEquals(query.getJpql(), request.getJpql());
        assertEquals(query.getResultProperties(), request.getResultProperties());
        assertEquals(query.getMaxResults(), request.getMaxResults());
        assertEquals(query.getFirstResult(), request.getFirstResult());
        assertEquals(1, request.getParameters().size());

        JpqlExecutionParameter parameter = request.getParameters().get(0);
        assertEquals("id", parameter.getName());
        assertEquals("Long", parameter.getType());
        assertEquals("10", parameter.getValue());

        assertSame(query, result.getQuery());
        assertEquals(List.of(Map.of("clientName", "Acme")), result.getRows());
        assertTrue(result.isHasMore());
        assertTrue(result.isExecuted());
    }

    @Test
    @DisplayName("Loads structured entity data with conversation id")
    void testLoadsStructuredEntityDataWithConversationId() {
        EntityDataLoadGenerationService entityDataLoadGenerationService = mock(EntityDataLoadGenerationService.class);
        JpqlExecutionService jpqlExecutionService = mock(JpqlExecutionService.class);
        AiDataLoadServiceImpl service = createService(entityDataLoadGenerationService, jpqlExecutionService);

        EntityDataLoadQuery query = new EntityDataLoadQuery(
                "select c.name as clientName from aitls_Customer c",
                List.of(),
                List.of("clientName"),
                "",
                List.of(),
                null,
                null
        );
        JpqlExecutionResult executionResult = new JpqlExecutionResult(
                new GeneratedJpqlResult(query.getJpql(), query.getParameters(), query.getExplanation(), query.getWarnings()),
                new JpqlValidationResult(true, List.of()),
                List.of(Map.of("clientName", "Acme")),
                20,
                null,
                false,
                false,
                true,
                null
        );

        when(entityDataLoadGenerationService.generate("show customers")).thenReturn(query);
        when(jpqlExecutionService.execute(any())).thenReturn(executionResult);

        EntityDataLoadResult result = service.loadData("show customers");

        ArgumentCaptor<JpqlExecutionRequest> requestCaptor = ArgumentCaptor.forClass(JpqlExecutionRequest.class);
        verify(jpqlExecutionService).execute(requestCaptor.capture());
        JpqlExecutionRequest request = requestCaptor.getValue();

        assertEquals("show customers", request.getUserText());
        assertEquals(query.getJpql(), request.getJpql());
        assertEquals(query.getResultProperties(), request.getResultProperties());
        assertSame(query, result.getQuery());
        assertEquals(List.of(Map.of("clientName", "Acme")), result.getRows());
    }

    protected AiDataLoadServiceImpl createService(EntityDataLoadGenerationService entityDataLoadGenerationService,
                                                  JpqlExecutionService jpqlExecutionService) {
        AiDataLoadServiceImpl service = new AiDataLoadServiceImpl();
        ReflectionTestUtils.setField(service, "entityDataLoadGenerationService", entityDataLoadGenerationService);
        ReflectionTestUtils.setField(service, "jpqlExecutionService", jpqlExecutionService);
        return service;
    }
}
