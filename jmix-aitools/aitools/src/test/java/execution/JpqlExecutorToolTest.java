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

import io.jmix.aitools.dataload.execution.EnumCaptionResultLocalizer;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.tool.JpqlExecutorTool;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.core.Messages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.entity.sales.Status;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpqlExecutorToolTest {

    @Test
    @DisplayName("Delegates execution to JPQL execution service")
    void testDelegatesToExecutionService() {
        JpqlExecutionRequest request = new JpqlExecutionRequest();
        JpqlExecutionResult expectedResult = new JpqlExecutionResult(
                new GeneratedJpqlResult("select e from aitls_Customer e",
                        List.of(), "", List.of()),
                new JpqlValidationResult(true, List.of()),
                List.of(Map.of("name", "Acme")),
                10,
                0,
                false,
                false,
                true,
                null
        );

        JpqlExecutorTool tool = createTool(expectedResult, mock(Messages.class));

        JpqlExecutionResult actualResult = tool.executeQuery(request, new ToolContext(Map.of()));

        assertEquals(List.of(Map.of("name", "Acme")), actualResult.getRows());
        assertEquals(expectedResult.isExecuted(), actualResult.isExecuted());
        assertEquals(expectedResult.getGeneratedJpqlResult(), actualResult.getGeneratedJpqlResult());
    }

    @Test
    @DisplayName("Localizes enum captions in the returned rows")
    void testLocalizesEnumCaptions() {
        JpqlExecutionRequest request = new JpqlExecutionRequest();
        JpqlExecutionResult executionResult = new JpqlExecutionResult(
                new GeneratedJpqlResult("select o.number as orderNumber, o.status as status from aitls_Order o",
                        List.of(), "", List.of()),
                new JpqlValidationResult(true, List.of()),
                List.of(Map.of("orderNumber", "A-1", "status", Status.OPEN)),
                10,
                0,
                false,
                false,
                true,
                null
        );

        Messages messages = mock(Messages.class);
        when(messages.getMessage(Status.OPEN)).thenReturn("Open");

        JpqlExecutorTool tool = createTool(executionResult, messages);

        JpqlExecutionResult actualResult = tool.executeQuery(request, new ToolContext(Map.of()));

        Map<String, Object> row = actualResult.getRows().get(0);
        assertEquals("A-1", row.get("orderNumber"));
        assertEquals("Open", row.get("status"));
    }

    protected JpqlExecutorTool createTool(JpqlExecutionResult executionResult, Messages messages) {
        JpqlExecutorTool tool = new JpqlExecutorTool();
        ReflectionTestUtils.setField(tool, "jpqlExecutionService", new JpqlExecutionService() {
            @Override
            public JpqlExecutionResult execute(JpqlExecutionRequest request) {
                return executionResult;
            }
        });

        EnumCaptionResultLocalizer localizer = new EnumCaptionResultLocalizer();
        ReflectionTestUtils.setField(localizer, "messages", messages);
        ReflectionTestUtils.setField(tool, "enumCaptionResultLocalizer", localizer);

        when(messages.getMessage(anyString())).thenReturn("status");
        ReflectionTestUtils.setField(tool, "messages", messages);
        ReflectionTestUtils.setField(tool, "toolStatusPublisher", new AiToolStatusPublisher());
        return tool;
    }
}
