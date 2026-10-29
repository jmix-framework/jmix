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
import io.jmix.aitoolsflowui.service.KeepInContextResult;
import io.jmix.aitoolsflowui.service.impl.AiChatMessageEmptyService;
import io.jmix.core.Metadata;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.AiToolsFlowuiTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keep-in-context behavior of the no-op message service: nothing is stored, so nothing can be kept.
 */
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatMessageEmptyServiceTest {

    @Autowired
    Metadata metadata;

    AiChatMessageEmptyService service;
    AiConversation conversation;

    @BeforeEach
    void setUp() {
        service = new AiChatMessageEmptyService();
        ReflectionTestUtils.setField(service, "metadata", metadata);
        conversation = metadata.create(AiConversation.class);
    }

    @Test
    void createMessage_keepRequested_isNotKept() {
        AiChatMessage message = service.createMessage(conversation, AiChatMessageType.USER, "Hi", true);

        assertFalse(message.getKeepInContext());
    }

    @Test
    void createMessage_keepRequestedForToolMessage_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createMessage(conversation, AiChatMessageType.TOOL, "Result", true));
    }

    @Test
    void setKeepInContext_keep_limitReachedAndModelUnchanged() {
        AiChatMessage message = service.createMessage(conversation, AiChatMessageType.USER, "Hi");

        assertEquals(KeepInContextResult.LIMIT_REACHED, service.setKeepInContext(message, true));
        assertFalse(message.getKeepInContext());
    }

    @Test
    void setKeepInContext_release_done() {
        AiChatMessage message = service.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hi");
        message.setKeepInContext(true);

        assertEquals(KeepInContextResult.DONE, service.setKeepInContext(message, false));
        assertFalse(message.getKeepInContext());
    }

    @Test
    void setKeepInContext_toolMessage_throws() {
        AiChatMessage message = service.createMessage(conversation, AiChatMessageType.TOOL, "Result");

        assertThrows(IllegalArgumentException.class, () -> service.setKeepInContext(message, true));
        assertThrows(IllegalArgumentException.class, () -> service.setKeepInContext(message, false));
    }

    @Test
    void loadKeptInContextMessages_empty() {
        assertTrue(service.loadKeptInContextMessages(conversation).isEmpty());
    }

    @Test
    void isKeepInContextLimitReached_true() {
        assertTrue(service.isKeepInContextLimitReached(conversation));
    }
}
