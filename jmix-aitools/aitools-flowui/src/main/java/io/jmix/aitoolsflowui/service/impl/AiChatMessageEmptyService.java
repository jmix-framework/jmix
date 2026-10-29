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

package io.jmix.aitoolsflowui.service.impl;

import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.KeepInContextResult;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * No-op implementation.
 */
public class AiChatMessageEmptyService implements AiChatMessageService {

    @Autowired
    protected Metadata metadata;

    @Override
    public AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message) {
        AiChatMessage aiMessage = metadata.create(AiChatMessage.class);
        aiMessage.setType(type);
        aiMessage.setConversation(conversation);
        aiMessage.setContent(message);
        aiMessage.setKeepInContext(false);
        aiMessage.setCreatedDate(OffsetDateTime.now());
        return aiMessage;
    }

    @Override
    public AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message,
                                       boolean keepInContext) {
        if (keepInContext) {
            checkKeepable(type);
        }
        // Nothing is stored, so there is no room for kept messages.
        return createMessage(conversation, type, message);
    }

    @Override
    public KeepInContextResult setKeepInContext(AiChatMessage message, boolean keep) {
        checkKeepable(message.getType());
        if (keep) {
            return KeepInContextResult.LIMIT_REACHED;
        }
        message.setKeepInContext(false);
        return KeepInContextResult.DONE;
    }

    @Override
    public Collection<AiChatMessage> loadKeptInContextMessages(AiConversation conversation) {
        return List.of();
    }

    @Override
    public boolean isKeepInContextLimitReached(AiConversation conversation) {
        return true;
    }

    @Nullable
    @Override
    public AiChatMessage loadLatestMessage(AiConversation conversation, @Nullable AiChatMessageType type) {
        return null;
    }

    @Override
    public Collection<AiChatMessage> loadMessages(AiConversation conversation) {
        return List.of();
    }

    @Override
    public Map<AiConversation, AiChatMessage> loadLatestMessages(Collection<AiConversation> conversations,
                                                                 @Nullable AiChatMessageType type) {
        return Map.of();
    }

    protected void checkKeepable(@Nullable AiChatMessageType type) {
        if (type != AiChatMessageType.USER
                && type != AiChatMessageType.ASSISTANT) {
            throw new IllegalArgumentException("Only user and assistant messages can be kept in context: " + type);
        }
    }
}
