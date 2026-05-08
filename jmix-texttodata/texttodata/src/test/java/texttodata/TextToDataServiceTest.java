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

package texttodata;

import io.jmix.texttodata.TextToDataResult;
import io.jmix.texttodata.TextToDataService;
import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.generation.JpqlGenerationRequest;
import io.jmix.texttodata.generation.JpqlGenerator;
import io.jmix.texttodata.repair.JpqlRepairer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TextToDataTestConfiguration.class, TextToDataServiceTest.TestConfiguration.class})
class TextToDataServiceTest {

    @Autowired
    TextToDataService textToDataService;

    @Test
    @DisplayName("Builds valid end-to-end result for valid generator output")
    void testBuildsValidEndToEndResult() {
        TextToDataResult result = textToDataService.generateJpql("orders by customer name");

        assertNotNull(result.getGenerationRequest());
        assertFalse(result.getGenerationRequest().getCandidates().isEmpty());
        assertFalse(result.getGenerationRequest().getPromptContext().isBlank());

        assertEquals("select e from textdt_Order e where e.customer.name like :customerName",
                result.getGeneratedJpqlResult().getJpql());
        assertTrue(result.isValid());
        assertFalse(result.isRepaired());
        assertEquals(0, result.getRepairAttempts());
        assertTrue(result.getValidationResult().getIssues().isEmpty());
    }

    @Test
    @DisplayName("Repairs invalid generator output when repairer can fix it")
    void testRepairsInvalidGeneratorOutput() {
        TextToDataResult result = textToDataService.generateJpql("invalid request");

        assertTrue(result.isRepaired());
        assertEquals(1, result.getRepairAttempts());
        assertTrue(result.isValid());
        assertEquals("select e from textdt_Order e where e.customer.name like :customerName",
                result.getGeneratedJpqlResult().getJpql());
    }

    @Test
    @DisplayName("Stops after bounded repair attempts when output remains invalid")
    void testStopsAfterBoundedRepairAttempts() {
        TextToDataResult result = textToDataService.generateJpql("invalid forever");

        assertTrue(result.isRepaired());
        assertEquals(2, result.getRepairAttempts());
        assertFalse(result.isValid());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals("propertyPath.invalid")));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        JpqlGenerator testTextToJpqlGenerator() {
            return request -> isInvalidRequest(request)
                    ? invalidResult()
                    : validResult();
        }

        @Bean
        JpqlRepairer testTextToJpqlRepairer() {
            return request -> request.getGenerationRequest().getUserText().contains("forever")
                    ? invalidResult()
                    : validResult();
        }

        protected boolean isInvalidRequest(JpqlGenerationRequest request) {
            return request.getUserText().contains("invalid");
        }

        protected GeneratedJpqlResult validResult() {
            return new GeneratedJpqlResult(
                    "select e from textdt_Order e where e.customer.name like :customerName",
                    "textdt_Order",
                    List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                    List.of("textdt_Order", "textdt_Customer"),
                    List.of("customer.name"),
                    "Valid test result",
                    List.of()
            );
        }

        protected GeneratedJpqlResult invalidResult() {
            return new GeneratedJpqlResult(
                    "select e from textdt_Order e where e.customer.fullTitle like :customerName",
                    "textdt_Order",
                    List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                    List.of("textdt_Order", "textdt_Customer"),
                    List.of("customer.fullTitle"),
                    "Invalid test result",
                    List.of()
            );
        }
    }
}
