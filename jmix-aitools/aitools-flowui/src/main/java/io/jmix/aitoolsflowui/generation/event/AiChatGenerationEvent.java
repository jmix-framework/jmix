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

package io.jmix.aitoolsflowui.generation.event;

import io.jmix.aitoolsflowui.model.AiConversation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Base of the UI events that report the lifecycle of an assistant response in a conversation. Delivered to all
 * open UIs of the conversation's owner (and of a user substituting them), so a listener filters by
 * {@link #getConversationId()} and tells successive responses of the conversation apart by
 * {@link #getGenerationId()}.
 */
@NullMarked
public abstract class AiChatGenerationEvent extends ApplicationEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected final UUID conversationId;
    protected final UUID generationId;

    protected AiChatGenerationEvent(Object source, UUID conversationId, UUID generationId) {
        super(source);
        this.conversationId = conversationId;
        this.generationId = generationId;
    }

    /**
     * Returns the conversation the response belongs to.
     *
     * @return the conversation id
     */
    public UUID getConversationId() {
        return conversationId;
    }

    /**
     * Returns the response the event belongs to. Every response gets a new id, so a late event of an earlier
     * response can be told from the events of the next one in the same conversation.
     *
     * @return the generation id
     */
    public UUID getGenerationId() {
        return generationId;
    }

    /**
     * Returns whether the event is about the given conversation. Events reach every open UI of the conversation's
     * owner, so a chat checks this before reacting.
     *
     * @param conversation conversation shown by the listener, or {@code null} if it shows none
     * @return {@code true} if the response belongs to the given conversation
     */
    public boolean belongsTo(@Nullable AiConversation conversation) {
        return conversation != null && conversationId.equals(conversation.getId());
    }
}
