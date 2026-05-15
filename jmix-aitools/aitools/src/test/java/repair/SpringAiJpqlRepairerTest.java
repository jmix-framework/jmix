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

import generation.test_support.StubChatModel;
import io.jmix.aitools.dataload.repair.impl.SpringAiJpqlRepairer;
import io.jmix.aitools.dataload.generation.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.generation.JpqlGenerationRequest;
import io.jmix.aitools.dataload.repair.JpqlRepairRequest;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import repair.test_support.SpringAiJpqlRepairerTestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TextToDataTestConfiguration.class, SpringAiJpqlRepairerTestConfiguration.class})
class SpringAiJpqlRepairerTest {

    @Autowired
    SpringAiJpqlRepairer repairer;

    @Autowired
    StubChatModel stubChatModel;

    @Test
    @DisplayName("Repairs invalid JPQL result from validation feedback")
    void testRepairsFromValidationFeedback() {
        String content = """
                {
                  "jpql": "select e from textdt_Order e where e.customer.name like :customerName",
                  "rootEntityName": "textdt_Order",
                  "parameters": [
                    {"name": "customerName", "type": "String", "value": "%Acme%"}
                  ],
                  "usedEntities": ["textdt_Order", "textdt_Customer"],
                  "usedPropertyPaths": ["customer.name"],
                  "explanation": "Fixed property path",
                  "warnings": []
                }
                """;

        stubChatModel.setContent(content);

        GeneratedJpqlResult repaired = repairer.repair(new JpqlRepairRequest(
                new JpqlGenerationRequest("orders by customer name", List.of(), "Entity textdt_Order"),
                new GeneratedJpqlResult(
                        "select e from textdt_Order e where e.customer.fullTitle like :customerName",
                        "textdt_Order",
                        List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                        List.of("textdt_Order", "textdt_Customer"),
                        List.of("customer.fullTitle"),
                        "Broken query",
                        List.of()
                ),
                new JpqlValidationResult(false, List.of(
                        new JpqlValidationIssue("propertyPath.invalid", "Unknown property path: customer.fullTitle")
                )),
                1
        ));

        assertEquals("select e from textdt_Order e where e.customer.name like :customerName", repaired.getJpql());
        assertEquals("customer.name", repaired.getUsedPropertyPaths().get(0));
    }
}
