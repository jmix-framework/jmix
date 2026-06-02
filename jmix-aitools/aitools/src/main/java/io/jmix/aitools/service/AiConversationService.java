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

package io.jmix.aitools.service;

import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;

import java.util.List;
import java.util.UUID;

/**
 * CRUD over {@link AiConversation} and {@link ChatMessage}. Owns persistence of
 * user messages and reads of conversation history as domain entities. The chat
 * orchestration ({@link AiConversationChatService}) is the consumer of the
 * history loader.
 */
public interface AiConversationService {

    /**
     * Creates and persists an empty {@link AiConversation}.
     */
    AiConversation createNewConversation();

    /**
     * Creates and persists a USER {@link ChatMessage} attached to the given conversation.
     *
     * @param conversation the (already persisted) target conversation
     * @param text         non-empty message text
     * @return the persisted message entity (with id populated)
     */
    ChatMessage createUserMessage(AiConversation conversation, String text);

    /**
     * Loads the most recent {@code limit} chat messages for the conversation,
     * ordered chronologically (oldest first). Returns an empty list if the
     * conversation has no messages or {@code limit} is non-positive.
     */
    List<ChatMessage> loadMessages(UUID conversationId, int limit);
}
