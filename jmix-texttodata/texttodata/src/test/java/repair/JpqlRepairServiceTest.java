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

package repair;

import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.generation.JpqlGenerationRequest;
import io.jmix.texttodata.repair.JpqlRepairResult;
import io.jmix.texttodata.repair.JpqlRepairService;
import io.jmix.texttodata.repair.JpqlRepairer;
import io.jmix.texttodata.validation.JpqlValidationResult;
import io.jmix.texttodata.validation.JpqlValidationService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TextToDataTestConfiguration.class, JpqlRepairServiceTest.TestConfiguration.class})
class JpqlRepairServiceTest {

    @Autowired
    JpqlRepairService jpqlRepairService;

    @Autowired
    JpqlValidationService jpqlValidationService;

    @Test
    @DisplayName("Repairs invalid result on the first successful attempt")
    void testRepairsInvalidResult() {
        JpqlGenerationRequest generationRequest = generationRequest("repair once");
        GeneratedJpqlResult invalidResult = invalidResult();
        JpqlValidationResult validationResult = jpqlValidationService.validate(invalidResult);

        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(generationRequest, invalidResult, validationResult);

        assertTrue(repairResult.isRepaired());
        assertEquals(1, repairResult.getRepairAttempts());
        assertTrue(repairResult.getValidationResult().isValid());
        assertEquals("select e from textdt_Order e where e.customer.name like :customerName",
                repairResult.getGeneratedJpqlResult().getJpql());
    }

    @Test
    @DisplayName("Stops repairing when repairer returns null")
    void testStopsWhenRepairerReturnsNull() {
        JpqlGenerationRequest generationRequest = generationRequest("return null");
        GeneratedJpqlResult invalidResult = invalidResult();
        JpqlValidationResult validationResult = jpqlValidationService.validate(invalidResult);

        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(generationRequest, invalidResult, validationResult);

        assertTrue(repairResult.isRepaired());
        assertEquals(1, repairResult.getRepairAttempts());
        assertFalse(repairResult.getValidationResult().isValid());
        assertEquals(invalidResult.getJpql(), repairResult.getGeneratedJpqlResult().getJpql());
    }

    @Test
    @DisplayName("Respects max repair attempts when result stays invalid")
    void testRespectsMaxRepairAttempts() {
        JpqlGenerationRequest generationRequest = generationRequest("always invalid");
        GeneratedJpqlResult invalidResult = invalidResult();
        JpqlValidationResult validationResult = jpqlValidationService.validate(invalidResult);

        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(generationRequest, invalidResult, validationResult, 2);

        assertTrue(repairResult.isRepaired());
        assertEquals(2, repairResult.getRepairAttempts());
        assertFalse(repairResult.getValidationResult().isValid());
        assertEquals("select e from textdt_Order e where e.customer.fullTitle like :customerName",
                repairResult.getGeneratedJpqlResult().getJpql());
    }

    @Test
    @DisplayName("Does not call repairer for already valid result")
    void testSkipsRepairForValidResult() {
        JpqlGenerationRequest generationRequest = generationRequest("already valid");
        GeneratedJpqlResult validResult = validResult();
        JpqlValidationResult validationResult = jpqlValidationService.validate(validResult);

        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(generationRequest, validResult, validationResult);

        assertFalse(repairResult.isRepaired());
        assertEquals(0, repairResult.getRepairAttempts());
        assertTrue(repairResult.getValidationResult().isValid());
        assertEquals(validResult.getJpql(), repairResult.getGeneratedJpqlResult().getJpql());
    }

    protected JpqlGenerationRequest generationRequest(String userText) {
        return new JpqlGenerationRequest(userText, List.of(), "Entity textdt_Order");
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

    @Configuration
    static class TestConfiguration {

        @Bean
        JpqlRepairer testTextToJpqlRepairer() {
            return request -> {
                String userText = request.getGenerationRequest().getUserText();
                if (userText.contains("return null")) {
                    return null;
                }
                if (userText.contains("always invalid")) {
                    return invalidResultStatic();
                }
                return validResultStatic();
            };
        }

        protected static GeneratedJpqlResult validResultStatic() {
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

        protected static GeneratedJpqlResult invalidResultStatic() {
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
