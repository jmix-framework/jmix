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

package chat_history;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.AiChatResponseSpec;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsFlowuiDataTestConfiguration;
import test_support.MessageTimeline;
import test_support.TestChatModel;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsFlowuiDataTestConfiguration.class)
@TestPropertySource(properties = {
        "eclipselink.ddl-generation=create-tables",
        "jmix.core.work-dir=build/test-home/work",
        "jmix.aitools.ui.data.chat-memory-max-messages=0"})
class KeptMessagesWithoutWindowTest {

    static final String HEADER = "Messages from earlier in this conversation the user asked you to keep in mind:";

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
    @Autowired
    UnconstrainedDataManager dataManager;

    MessageTimeline timeline;

    @BeforeEach
    void setUp() {
        timeline = new MessageTimeline(dataManager);
    }

    @AfterEach
    void tearDown() {
        chatModel.reset();
    }

    @Test
    void generateResponse_nonPositiveWindowNoKeptMessages_onlyCurrentMessage() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            add(conversation, AiChatMessageType.USER, "U1");
            add(conversation, AiChatMessageType.ASSISTANT, "A1");
            AiChatMessage current = add(conversation, AiChatMessageType.USER, "U2");

            generate(current);

            assertEquals(List.of("USER:U2"), history());
        });
    }

    @Test
    void generateResponse_nonPositiveWindow_blockMergedIntoCurrentMessage() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage kept = add(conversation, AiChatMessageType.USER, "U1");
            add(conversation, AiChatMessageType.ASSISTANT, "A1");
            AiChatMessage current = add(conversation, AiChatMessageType.USER, "U2");
            messageService.setKeepInContext(kept, true);

            generate(current);

            assertEquals(List.of("USER:" + HEADER + "\n\nUser: U1\n\nU2"), history());
        });
    }

    @Test
    void generateResponse_nonPositiveWindowCurrentMessageKept_currentMessageIsNotRepeated() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation conversation = conversationService.create();
            AiChatMessage current = messageService.createMessage(conversation, AiChatMessageType.USER, "U1", true);

            generate(current);

            assertEquals(List.of("USER:U1"), history());
        });
    }

    private AiChatMessage add(AiConversation conversation, AiChatMessageType type, String text) {
        return timeline.place(messageService.createMessage(conversation, type, text));
    }

    private void generate(AiChatMessage current) {
        chatModel.setContent("ok");
        chatService.generateResponse(new AiChatResponseSpec(current));
    }

    private List<String> history() {
        return Objects.requireNonNull(chatModel.getLastPrompt()).getInstructions().stream()
                .filter(message -> message.getMessageType() != MessageType.SYSTEM)
                .map(message -> message.getMessageType() + ":" + message.getText())
                .toList();
    }
}
