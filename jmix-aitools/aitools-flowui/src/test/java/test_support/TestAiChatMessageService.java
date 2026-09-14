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

package test_support;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory {@link AiChatMessageService} for UI tests.
 */
public class TestAiChatMessageService implements AiChatMessageService {

    @Autowired
    protected Metadata metadata;

    protected final Map<UUID, List<AiChatMessage>> messagesByConversation = new LinkedHashMap<>();

    public void clear() {
        messagesByConversation.clear();
    }

    @Override
    public AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message) {
        AiChatMessage chatMessage = metadata.create(AiChatMessage.class);
        chatMessage.setConversation(conversation);
        chatMessage.setType(type);
        chatMessage.setContent(message);
        chatMessage.setCreatedDate(OffsetDateTime.now());

        messagesByConversation.computeIfAbsent(conversation.getId(), id -> new ArrayList<>()).add(chatMessage);

        return chatMessage;
    }

    @Nullable
    @Override
    public AiChatMessage loadLatestMessage(AiConversation conversation, @Nullable AiChatMessageType type) {
        List<AiChatMessage> messages = messagesByConversation.getOrDefault(conversation.getId(), List.of());
        return messages.stream()
                .filter(message -> type == null || type.equals(message.getType()))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    @Override
    public Collection<AiChatMessage> loadMessages(AiConversation conversation) {
        return List.copyOf(messagesByConversation.getOrDefault(conversation.getId(), List.of()));
    }
}
