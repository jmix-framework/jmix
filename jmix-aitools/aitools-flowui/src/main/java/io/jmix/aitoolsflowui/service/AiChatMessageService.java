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

package io.jmix.aitoolsflowui.service;

import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Provides access to the messages of the current user's AI conversations.
 * <p>
 * Operates purely on the {@link AiChatMessage} model and is implicitly scoped to
 * the current user.
 */
public interface AiChatMessageService {

    /**
     * Creates a new message of the given type in the conversation.
     *
     * @param conversation conversation the message belongs to
     * @param type         message type
     * @param message      message text
     * @return the created message
     */
    AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message);

    /**
     * Loads the most recent message of the conversation, optionally restricted to a type.
     *
     * @param conversation conversation whose latest message is loaded
     * @param type         message type to match, or {@code null} for any type
     * @return the latest matching message, or {@code null} if there is none
     */
    @Nullable
    AiChatMessage loadLatestMessage(AiConversation conversation, @Nullable AiChatMessageType type);

    /**
     * Loads all messages of the conversation, oldest first.
     *
     * @param conversation conversation whose messages are loaded
     * @return the conversation's messages, oldest first; empty if there are none
     */
    Collection<AiChatMessage> loadMessages(AiConversation conversation);
}
