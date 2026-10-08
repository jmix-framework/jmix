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

import io.jmix.aitoolsflowuidata.AiToolsFlowuiDataProperties;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntityType;
import io.jmix.aitoolsflowuidata.service.impl.AiChatDataService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ChatMemoryWindowTest {

    /**
     * Exposes the protected history assembly and stubs the DB-backed windowed load so the
     * chat-memory windowing behavior can be exercised without a data store.
     */
    static class TestableChatDataService extends AiChatDataService {

        private final List<AiChatMessageEntity> windowed;

        TestableChatDataService(int window, List<AiChatMessageEntity> windowed) {
            this.dataProperties = new AiToolsFlowuiDataProperties(true, window);
            this.windowed = windowed;
        }

        @Override
        protected List<AiChatMessageEntity> loadRecentMessages(UUID conversationId, int limit) {
            return windowed;
        }

        List<Message> history(UUID conversationId, AiChatMessageEntity currentUserMessage) {
            return loadHistory(conversationId, currentUserMessage);
        }
    }

    private AiChatMessageEntity userMessage(String content) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setType(AiChatMessageEntityType.USER);
        message.setContent(content);
        return message;
    }

    @Test
    void nonPositiveWindow_stillSendsCurrentUserMessage() {
        // A non-positive window yields no windowed messages; the current user message must
        // still be sent so the assistant is never invoked with an empty request.
        AiChatMessageEntity current = userMessage("hello");
        TestableChatDataService service = new TestableChatDataService(0, List.of());

        List<Message> history = service.history(UUID.randomUUID(), current);

        assertEquals(1, history.size());
        assertInstanceOf(UserMessage.class, history.get(0));
    }

    @Test
    void positiveWindow_mapsWindowedMessagesInOrder() {
        AiChatMessageEntity current = userMessage("second");
        TestableChatDataService service = new TestableChatDataService(20,
                List.of(userMessage("first"), current));

        List<Message> history = service.history(UUID.randomUUID(), current);

        assertEquals(2, history.size());
        assertInstanceOf(UserMessage.class, history.get(0));
        assertInstanceOf(UserMessage.class, history.get(1));
    }

    @Test
    void currentUserMessageContentPreserved() {
        AiChatMessageEntity current = userMessage("only message");
        TestableChatDataService service = new TestableChatDataService(-5, List.of());

        List<Message> history = service.history(UUID.randomUUID(), current);

        UserMessage sent = assertInstanceOf(UserMessage.class, history.get(0));
        assertEquals("only message", sent.getText());
    }
}
