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
import java.util.Map;

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
     * Creates a new message of the given type in the conversation, keeping it in context if asked and the
     * conversation's limit of kept messages allows. Otherwise the message is created without being kept, which
     * the returned message reports through {@link AiChatMessage#getKeepInContext()}.
     *
     * @param conversation  conversation the message belongs to
     * @param type          message type
     * @param message       message text
     * @param keepInContext whether to keep the message in context
     * @return the created message
     * @throws IllegalArgumentException if {@code keepInContext} is requested for a message that is neither a
     *                                  user nor an assistant message
     */
    AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message,
                                boolean keepInContext);

    /**
     * Keeps the message in context, or releases it. A kept message stays part of what the assistant sees with
     * every request, regardless of the conversation history window. Only user and assistant messages can be kept.
     * <p>
     * Keeping an already kept message and releasing a message always succeed. When the result is
     * {@link KeepInContextResult#DONE}, the passed message reflects the new state; on
     * {@link KeepInContextResult#LIMIT_REACHED} and on an exception it is left unchanged.
     *
     * @param message message to change
     * @param keep    {@code true} to keep the message in context, {@code false} to release it
     * @return {@link KeepInContextResult#LIMIT_REACHED} if the message could not be kept because the
     * conversation already holds the maximum number of kept messages, {@link KeepInContextResult#DONE} otherwise
     * @throws IllegalArgumentException if the message is neither a user nor an assistant message
     */
    KeepInContextResult setKeepInContext(AiChatMessage message, boolean keep);

    /**
     * Loads the messages of the conversation that are kept in context, oldest first.
     *
     * @param conversation conversation whose kept messages are loaded
     * @return the kept messages, oldest first; empty if there are none or the conversation is not accessible to
     * the current user
     */
    Collection<AiChatMessage> loadKeptInContextMessages(AiConversation conversation);

    /**
     * Tells whether the conversation already holds the maximum number of messages kept in context, so no further
     * message can be kept until one is released.
     *
     * @param conversation conversation to check
     * @return {@code true} if keeping one more message would fail, which is also the case for a conversation not
     * accessible to the current user
     */
    boolean isKeepInContextLimitReached(AiConversation conversation);

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
     * Loads the latest message of each of the given conversations, keyed by the conversation
     * instances passed in. A conversation with no matching message, or one not accessible to
     * the current user, is absent from the result.
     *
     * @param conversations conversations whose latest messages are loaded
     * @param type          message type to match, or {@code null} for any type
     * @return the latest matching message by conversation; empty if none of them has a matching message
     */
    Map<AiConversation, AiChatMessage> loadLatestMessages(Collection<AiConversation> conversations,
                                                          @Nullable AiChatMessageType type);

    /**
     * Loads all messages of the conversation, oldest first.
     *
     * @param conversation conversation whose messages are loaded
     * @return the conversation's messages, oldest first; empty if there are none
     */
    Collection<AiChatMessage> loadMessages(AiConversation conversation);
}
