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

package io.jmix.aitoolsflowui.generation;

import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.util.UUID;

/**
 * Thrown when an assistant response is requested in a conversation that already has one running.
 */
@NullMarked
public class AiChatGenerationRejectedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected final UUID conversationId;

    /**
     * Creates the exception.
     *
     * @param conversationId the conversation that already has a response running
     */
    public AiChatGenerationRejectedException(UUID conversationId) {
        super("An assistant response is already being generated in conversation " + conversationId);

        this.conversationId = conversationId;
    }

    /**
     * Returns the conversation that already has a response running.
     *
     * @return the conversation id
     */
    public UUID getConversationId() {
        return conversationId;
    }
}
