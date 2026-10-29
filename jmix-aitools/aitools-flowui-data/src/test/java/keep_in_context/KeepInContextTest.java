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
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsFlowuiDataTestConfiguration;
import test_support.MessageTimeline;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsFlowuiDataTestConfiguration.class)
@TestPropertySource(properties = {
        "eclipselink.ddl-generation=create-tables",
        "jmix.core.work-dir=build/test-home/work",
        "jmix.aitools.ui.data.keep-in-context-max-messages=2"})
class KeepInContextTest {

    @Autowired
    AiConversationService conversationService;
    @Autowired
    AiChatMessageService messageService;
    @Autowired
    SystemAuthenticator systemAuthenticator;
    @Autowired
    UnconstrainedDataManager dataManager;

    @Test
    void createMessage_withoutKeepRequest_isNotKept() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();

            AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");

            assertFalse(message.getKeepInContext());
            assertTrue(messageService.loadKeptInContextMessages(conversation).isEmpty());
        });
    }

    @Test
    void createMessage_keepUnderLimit_isKept() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();

            AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi", true);

            assertTrue(message.getKeepInContext());
            assertEquals(List.of(message.getId()), ids(messageService.loadKeptInContextMessages(conversation)));
        });
    }

    @Test
    void createMessage_keepOverLimit_savedWithoutFlag() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            messageService.createMessage(conversation, AiChatMessageType.USER, "One", true);
            messageService.createMessage(conversation, AiChatMessageType.USER, "Two", true);

            AiChatMessage third = messageService.createMessage(conversation, AiChatMessageType.USER, "Three", true);

            assertFalse(third.getKeepInContext());
            assertEquals(3, messageService.loadMessages(conversation).size());
            assertEquals(2, messageService.loadKeptInContextMessages(conversation).size());
        });
    }

    @Test
    void createMessage_keepToolMessage_throws() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();

            assertThrows(IllegalArgumentException.class,
                    () -> messageService.createMessage(conversation, AiChatMessageType.TOOL, "Result", true));
            assertTrue(messageService.loadMessages(conversation).isEmpty());
        });
    }

    @Test
    void setKeepInContext_keepAndRelease_updatesKeptMessages() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hi");

            assertEquals(KeepInContextResult.DONE, messageService.setKeepInContext(message, true));
            assertTrue(message.getKeepInContext());
            assertEquals(List.of(message.getId()), ids(messageService.loadKeptInContextMessages(conversation)));

            assertEquals(KeepInContextResult.DONE, messageService.setKeepInContext(message, false));
            assertFalse(message.getKeepInContext());
            assertTrue(messageService.loadKeptInContextMessages(conversation).isEmpty());
        });
    }

    @Test
    void setKeepInContext_atLimit_limitReachedAndNotKept() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            messageService.createMessage(conversation, AiChatMessageType.USER, "One", true);
            messageService.createMessage(conversation, AiChatMessageType.USER, "Two", true);
            AiChatMessage third = messageService.createMessage(conversation, AiChatMessageType.USER, "Three");

            assertEquals(KeepInContextResult.LIMIT_REACHED, messageService.setKeepInContext(third, true));
            assertFalse(third.getKeepInContext());
            assertEquals(2, messageService.loadKeptInContextMessages(conversation).size());
            assertFalse(ids(messageService.loadKeptInContextMessages(conversation)).contains(third.getId()));
        });
    }

    @Test
    void setKeepInContext_alreadyKeptAtLimit_done() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage first = messageService.createMessage(conversation, AiChatMessageType.USER, "One", true);
            messageService.createMessage(conversation, AiChatMessageType.USER, "Two", true);

            assertEquals(KeepInContextResult.DONE, messageService.setKeepInContext(first, true));
            assertTrue(first.getKeepInContext());
            assertEquals(2, messageService.loadKeptInContextMessages(conversation).size());
        });
    }

    @Test
    void setKeepInContext_releaseFreesSlot() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage first = messageService.createMessage(conversation, AiChatMessageType.USER, "One", true);
            messageService.createMessage(conversation, AiChatMessageType.USER, "Two", true);
            AiChatMessage third = messageService.createMessage(conversation, AiChatMessageType.USER, "Three");

            messageService.setKeepInContext(first, false);

            assertEquals(KeepInContextResult.DONE, messageService.setKeepInContext(third, true));
        });
    }

    @Test
    void setKeepInContext_releaseOfNotKeptMessage_done() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");

            assertEquals(KeepInContextResult.DONE, messageService.setKeepInContext(message, false));
        });
    }

    @Test
    void setKeepInContext_toolOrSystemMessage_throws() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage tool = messageService.createMessage(conversation, AiChatMessageType.TOOL, "Result");
            AiChatMessage system = messageService.createMessage(conversation, AiChatMessageType.SYSTEM, "Rules");

            assertThrows(IllegalArgumentException.class, () -> messageService.setKeepInContext(tool, true));
            assertThrows(IllegalArgumentException.class, () -> messageService.setKeepInContext(system, true));
            assertThrows(IllegalArgumentException.class, () -> messageService.setKeepInContext(tool, false));
            assertFalse(tool.getKeepInContext());
        });
    }

    @Test
    void setKeepInContext_modelClaimsKeepableTypeForToolMessage_throws() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage tool = messageService.createMessage(conversation, AiChatMessageType.TOOL, "Result");
            // The caller's model claims a keepable type, but the stored message is a tool message.
            tool.setType(AiChatMessageType.USER);

            assertThrows(IllegalArgumentException.class, () -> messageService.setKeepInContext(tool, true));
            assertFalse(tool.getKeepInContext());
            assertTrue(messageService.loadKeptInContextMessages(conversation).isEmpty());
        });
    }

    @Test
    void loadKeptInContextMessages_returnsOldestFirstAndOnlyKept() {
        systemAuthenticator.runWithSystem(() -> {
            MessageTimeline timeline = new MessageTimeline(dataManager);
            AiConversation conversation = conversationService.create();
            AiChatMessage first = timeline.place(
                    messageService.createMessage(conversation, AiChatMessageType.USER, "Message 0"));
            timeline.place(messageService.createMessage(conversation, AiChatMessageType.USER, "Message 1"));
            AiChatMessage third = timeline.place(
                    messageService.createMessage(conversation, AiChatMessageType.USER, "Message 2"));

            messageService.setKeepInContext(third, true);
            messageService.setKeepInContext(first, true);

            assertEquals(List.of(first.getId(), third.getId()),
                    ids(messageService.loadKeptInContextMessages(conversation)));
        });
    }

    @Test
    void isKeepInContextLimitReached_reflectsKeptCount() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            assertFalse(messageService.isKeepInContextLimitReached(conversation));

            AiChatMessage first = messageService.createMessage(conversation, AiChatMessageType.USER, "One", true);
            assertFalse(messageService.isKeepInContextLimitReached(conversation));

            messageService.createMessage(conversation, AiChatMessageType.USER, "Two", true);
            assertTrue(messageService.isKeepInContextLimitReached(conversation));

            messageService.setKeepInContext(first, false);
            assertFalse(messageService.isKeepInContextLimitReached(conversation));
        });
    }

    @Test
    void isKeepInContextLimitReached_countsPerConversation() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation busy = conversationService.create();
            AiConversation other = conversationService.create();
            messageService.createMessage(busy, AiChatMessageType.USER, "One", true);
            messageService.createMessage(busy, AiChatMessageType.USER, "Two", true);

            assertTrue(messageService.isKeepInContextLimitReached(busy));
            assertFalse(messageService.isKeepInContextLimitReached(other));
        });
    }

    private List<UUID> ids(Collection<AiChatMessage> messages) {
        return messages.stream()
                .map(AiChatMessage::getId)
                .toList();
    }
}
