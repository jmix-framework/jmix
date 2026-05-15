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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;
import texttodata.test_support.TextToDataServiceTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TextToDataTestConfiguration.class, TextToDataServiceTestConfiguration.class})
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

    @Test
    @DisplayName("Normalizes pagination into execution options")
    void testNormalizesPaginationIntoExecutionOptions() {
        TextToDataResult result = textToDataService.generateJpql("orders with limit");

        assertTrue(result.isValid());
        assertEquals("select e from textdt_Order e", result.getGeneratedJpqlResult().getJpql());
        assertEquals(10, result.getMaxResults());
        assertEquals(5, result.getFirstResult());
    }
}
