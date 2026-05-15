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

import generation.test_support.SpringAiJpqlGeneratorTestConfiguration;
import generation.test_support.StubChatModel;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.generation.JpqlGenerationRequest;
import io.jmix.aitools.dataload.generation.impl.SpringAiJpqlGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TextToDataTestConfiguration.class, SpringAiJpqlGeneratorTestConfiguration.class})
class SpringAiJpqlGeneratorTest {

    @Autowired
    SpringAiJpqlGenerator generator;

    @Autowired
    StubChatModel stubChatModel;

    @Test
    @DisplayName("Parses JSON response from chat model into generated JPQL result")
    void testParsesJsonResponse() {
        String content = """
                {
                  "jpql": "select e from aitols_Order e where e.customer.name like :customerName",
                  "rootEntityName": "aitols_Order",
                  "parameters": [
                    {"name": "customerName", "type": "String", "value": "%Acme%"}
                  ],
                  "usedEntities": ["aitols_Order", "aitols_Customer"],
                  "usedPropertyPaths": ["customer.name"],
                  "explanation": "Find orders by customer name",
                  "warnings": ["Customer and counterparty may both match"],
                  "maxResults": 10,
                  "firstResult": 5
                }
                """;

        stubChatModel.setContent(content);

        GeneratedJpqlResult result = generator.generate(new JpqlGenerationRequest(
                "orders by customer name",
                List.of(),
                "Entity aitols_Order"
        ));

        assertEquals("select e from aitols_Order e where e.customer.name like :customerName", result.getJpql());
        assertEquals("aitols_Order", result.getRootEntityName());
        assertEquals(1, result.getParameters().size());
        assertEquals("customer.name", result.getUsedPropertyPaths().get(0));
        assertTrue(result.getWarnings().contains("Customer and counterparty may both match"));
        assertEquals(10, result.getMaxResults());
        assertEquals(5, result.getFirstResult());
    }

    @Test
    @DisplayName("Accepts JSON wrapped in markdown code fences")
    void testAcceptsMarkdownWrappedJson() {
        String content = """
                ```json
                {
                  "jpql": "select e from aitols_Order e",
                  "rootEntityName": "aitols_Order",
                  "parameters": [],
                  "usedEntities": ["aitols_Order"],
                  "usedPropertyPaths": [],
                  "explanation": "Return orders",
                  "warnings": []
                }
                ```
                """;

        stubChatModel.setContent(content);

        GeneratedJpqlResult result = generator.generate(new JpqlGenerationRequest(
                "orders",
                List.of(),
                "Entity aitols_Order"
        ));

        assertEquals("select e from aitols_Order e", result.getJpql());
    }
}
