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

import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.tool.JpqlExecutionTool;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class JpqlExecutionToolTest {

    @Test
    @DisplayName("Delegates execution to JPQL execution service")
    void testDelegatesToExecutionService() {
        JpqlExecutionRequest request = new JpqlExecutionRequest();
        JpqlExecutionResult expectedResult = new JpqlExecutionResult(
                new GeneratedJpqlResult("select e from aitols_Customer e",
                        List.of(), "", List.of()),
                new JpqlValidationResult(true, List.of()),
                List.of(),
                10,
                0,
                false,
                true,
                null
        );

        JpqlExecutionTool tool = new JpqlExecutionTool();
        ReflectionTestUtils.setField(tool, "jpqlExecutionService", new JpqlExecutionService() {
            @Override
            public JpqlExecutionResult execute(JpqlExecutionRequest request) {
                return expectedResult;
            }
        });

        JpqlExecutionResult actualResult = tool.executeQuery(request);

        assertSame(expectedResult, actualResult);
    }
}
