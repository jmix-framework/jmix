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

package generation;

import generation.test_support.JpqlGenerationServiceTestConfiguration;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.generation.JpqlGenerationRequest;
import io.jmix.aitools.dataload.generation.JpqlGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AiToolsTestConfiguration.class, JpqlGenerationServiceTestConfiguration.class})
class JpqlGenerationServiceTest {

    @Autowired
    JpqlGenerationService jpqlGenerationService;

    @Test
    @DisplayName("Prepares generation request from user text")
    void testPreparesGenerationRequest() {
        JpqlGenerationRequest request = jpqlGenerationService.prepareRequest("orders by customer name");

        assertEquals("orders by customer name", request.getUserText());
        assertFalse(request.getCandidates().isEmpty());
        assertFalse(request.getPromptContext().isBlank());
        assertTrue(request.getPromptContext().contains("Entity aitols_Order"));
    }

    @Test
    @DisplayName("Delegates generation to configured generator")
    void testDelegatesGenerationToConfiguredGenerator() {
        GeneratedJpqlResult result = jpqlGenerationService.generate("orders by customer name");

        assertEquals("select e from aitols_Order e", result.getJpql());
        assertEquals("Fake generator result", result.getExplanation());
        assertEquals("customerName", result.getParameters().get(0).getName());
    }
}
