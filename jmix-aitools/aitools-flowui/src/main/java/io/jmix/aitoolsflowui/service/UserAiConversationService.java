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

import io.jmix.aitoolsflowui.model.UserAiConversation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Provides access to the current user's AI conversations for the chat UI.
 * <p>
 * Operates purely on the {@link UserAiConversation} model: the chat fragment, hub
 * and conversation view depend on this contract and stay independent of how (or
 * whether) conversations are persisted. List operations are implicitly scoped to
 * the current user.
 */
public interface UserAiConversationService {

    /**
     * Loads a conversation by id.
     *
     * @param conversationId id of the conversation to load
     * @return the conversation, or {@code null} if it does not exist or is not
     * accessible to the current user
     */
    @Nullable
    UserAiConversation loadConversation(UUID conversationId);

    /**
     * Loads the current user's conversations, newest first. Messages are not loaded;
     * use {@link UserAiMessageService} to fetch a conversation's messages on demand.
     *
     * @return the conversations, newest first; empty if there are none
     */
    List<UserAiConversation> loadConversations();

    /**
     * Creates a new conversation owned by the current user.
     *
     * @return the created conversation
     */
    UserAiConversation create();

    /**
     * Persists the given conversation (insert or update).
     *
     * @param conversation conversation to save
     * @return the saved conversation
     */
    UserAiConversation save(UserAiConversation conversation);

    /**
     * Removes the given conversation together with its messages.
     *
     * @param conversation conversation to remove
     */
    void remove(UserAiConversation conversation);
}
