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

package keep_in_context;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.aitoolsflowui.service.KeepInContextResult;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsFlowuiDataTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsFlowuiDataTestConfiguration.class)
@TestPropertySource(properties = {
        "eclipselink.ddl-generation=create-tables",
        "jmix.core.work-dir=build/test-home/work",
        "jmix.aitools.ui.data.keep-in-context-max-messages=0"})
class KeepInContextDisabledTest {

    @Autowired
    AiConversationService conversationService;
    @Autowired
    AiChatMessageService messageService;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    void setKeepInContext_nonPositiveLimit_limitReached() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");

            assertEquals(KeepInContextResult.LIMIT_REACHED, messageService.setKeepInContext(message, true));
            assertTrue(messageService.loadKeptInContextMessages(conversation).isEmpty());
        });
    }

    @Test
    void createMessage_nonPositiveLimit_savedWithoutFlag() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();

            AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi", true);

            assertFalse(message.getKeepInContext());
            assertEquals(1, messageService.loadMessages(conversation).size());
        });
    }

    @Test
    void isKeepInContextLimitReached_nonPositiveLimit_true() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();

            assertTrue(messageService.isKeepInContextLimitReached(conversation));
        });
    }

    @Test
    void setKeepInContext_nonPositiveLimit_releaseIsDone() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");

            assertEquals(KeepInContextResult.DONE, messageService.setKeepInContext(message, false));
        });
    }
}
