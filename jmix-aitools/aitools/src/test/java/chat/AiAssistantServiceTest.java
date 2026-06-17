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

package chat;

import io.jmix.aitools.service.AiAssistantService;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import repair.test_support.StubChatModel;
import test_support.AiToolsTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class AiAssistantServiceTest {

    @Autowired
    AiAssistantService aiChatService;

    @Autowired
    StubChatModel stubChatModel;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    @DisplayName("send() attaches a system message rendered from the default template")
    void testAttachesSystemMessage() {
        stubChatModel.setContent("stub-response");

        systemAuthenticator.begin();

        String reply = aiChatService.send("hello");

        systemAuthenticator.end();

        assertEquals("stub-response", reply);

        Prompt lastPrompt = stubChatModel.getLastPrompt();
        assertNotNull(lastPrompt, "StubChatModel must have received a prompt");

        Message systemMessage = lastPrompt.getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Expected a SYSTEM message in the prompt, got: " + lastPrompt.getInstructions()));

        String systemText = systemMessage.getText();
        assertTrue(systemText.contains("Jmix application"),
                "System message must include the assistant identity from the default template; was: " + systemText);
        assertTrue(systemText.contains("Respond in this language:"),
                "System message must include the response-language directive; was: " + systemText);
        assertTrue(!systemText.contains("{additionalInstructions}"),
                "Reserved {additionalInstructions} placeholder must be bound (rendered, not literal); was: " + systemText);
        assertTrue(!systemText.contains("{responseLanguage}"),
                "{responseLanguage} placeholder must be bound (rendered, not literal); was: " + systemText);
    }
}
