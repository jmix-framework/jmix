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

package chat_generation;

import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.AiChatResponseSpec;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsFlowuiDataTestConfiguration;
import test_support.TestChatModel;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsFlowuiDataTestConfiguration.class)
@TestPropertySource(properties = {
        "eclipselink.ddl-generation=create-tables",
        "jmix.core.work-dir=build/test-home/work"})
class AiChatDataServiceGenerationTest {

    @Autowired
    AiChatService chatService;
    @Autowired
    AiConversationService conversationService;
    @Autowired
    AiChatMessageService messageService;
    @Autowired
    TestChatModel chatModel;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @AfterEach
    void tearDown() {
        chatModel.reset();
    }

    @Test
    void generateResponse_returnsTextAndPersistsNothing() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
            chatModel.setContent("Hello!");

            String answer = chatService.generateResponse(new AiChatResponseSpec(question).setCorrelationId("gen-1"));

            assertEquals("Hello!", answer);
            assertEquals(List.of(question.getId()), ids(messageService.loadMessages(conversation)));
        });
    }

    @Test
    void generateResponse_putsCorrelationIdIntoToolContext() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
            chatModel.setContent("Hello!");

            chatService.generateResponse(new AiChatResponseSpec(question).setCorrelationId("gen-1"));

            ToolCallingChatOptions options =
                    (ToolCallingChatOptions) Objects.requireNonNull(chatModel.getLastPrompt()).getOptions();
            assertEquals("gen-1", options.getToolContext().get(AiToolStatusPublisher.CORRELATION_ID));
        });
    }

    @Test
    void generateResponse_withoutCorrelationId_noCorrelationIdInToolContext() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
            chatModel.setContent("Hello!");

            chatService.generateResponse(new AiChatResponseSpec(question));

            ToolCallingChatOptions options =
                    (ToolCallingChatOptions) Objects.requireNonNull(chatModel.getLastPrompt()).getOptions();
            assertFalse(options.getToolContext().containsKey(AiToolStatusPublisher.CORRELATION_ID));
        });
    }

    @Test
    void generateResponse_modelReturnsNull_returnsEmptyString() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");

            assertEquals("", chatService.generateResponse(new AiChatResponseSpec(question).setCorrelationId("gen-1")));
        });
    }

    @Test
    void processMessage_persistsOneAssistantMessageAfterTheCall() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
            chatModel.setContent("Hello!");

            chatService.processMessage(question);

            List<AiChatMessage> messages = List.copyOf(messageService.loadMessages(conversation));
            assertEquals(2, messages.size());
            assertEquals(AiChatMessageType.ASSISTANT, messages.get(1).getType());
            assertEquals("Hello!", messages.get(1).getContent());
        });
    }

    @Test
    void processMessage_blankAnswer_persistsNothing() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
            chatModel.setContent("  ");

            chatService.processMessage(question);

            assertEquals(1, messageService.loadMessages(conversation).size());
        });
    }

    @Test
    void processMessage_modelFails_persistsNothing() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
            chatModel.setFailure(new IllegalStateException("model down"));

            assertThrows(IllegalStateException.class, () -> chatService.processMessage(question));
            assertEquals(1, messageService.loadMessages(conversation).size());
        });
    }

    private List<UUID> ids(Collection<AiChatMessage> messages) {
        return messages.stream()
                .map(AiChatMessage::getId)
                .toList();
    }
}
