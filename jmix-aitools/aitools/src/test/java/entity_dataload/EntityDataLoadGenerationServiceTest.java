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

import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.tool.DomainModelDiscoveryTool;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import repair.test_support.StubChatModel;
import test_support.AiToolsTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class EntityDataLoadGenerationServiceTest {

    @Autowired
    EntityDataLoadGenerationService generationService;

    @Autowired
    StubChatModel stubChatModel;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    @DisplayName("Generates a query when the model calls a tool that takes a ToolContext")
    void testGeneratesQueryWhenModelCallsToolTakingToolContext() {
        stubChatModel.enqueueToolCall(DomainModelDiscoveryTool.AVAILABLE_ENTITIES_TOOL, "{}");
        stubChatModel.setContent("""
                {
                  "jpql": "select e.number as number from aitls_Order e",
                  "resultProperties": ["number"],
                  "parameters": [],
                  "explanation": "All orders"
                }
                """);

        systemAuthenticator.begin();
        try {
            EntityDataLoadQuery query = generationService.generate("list orders");

            assertEquals("select e.number as number from aitls_Order e", query.getJpql());
        } finally {
            systemAuthenticator.end();
        }

        assertTrue(stubChatModel.getLastPrompt().getInstructions().stream()
                        .map(Message::getMessageType)
                        .anyMatch(MessageType.TOOL::equals),
                "The tool result should have been sent back to the model");
    }
}
