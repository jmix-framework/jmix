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

import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitools.tool.AiToolStatusUpdateEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;

import java.util.function.Consumer;

/**
 * Generates assistant responses in an AI chat.
 * <p>
 * Whether a response can actually be produced depends on the application's AI
 * configuration; callers should check {@link #isAvailable()} before sending a message.
 */
public interface AiChatService {

    /**
     * Generates the assistant's reply to the given user message.
     *
     * @param message the user message to answer
     * @return the assistant's reply text, or an empty string if no reply was produced
     * @deprecated use {@link #generateResponse(AiChatResponseSpec)} and persist the response yourself
     */
    @Deprecated(since = "3.2", forRemoval = true)
    String processMessage(AiChatMessage message);

    /**
     * Generates the assistant's reply to the given user message, reporting progress
     * while the reply is being prepared.
     *
     * @param message        the user message to answer
     * @param statusCallback receives progress updates during processing
     * @return the assistant's reply text, or an empty string if no reply was produced
     * @deprecated use {@link #generateResponse(AiChatResponseSpec)} and persist the response yourself
     */
    @Deprecated(since = "3.2", forRemoval = true)
    String processMessage(AiChatMessage message, Consumer<AiToolStatusUpdate> statusCallback);

    /**
     * Generates the assistant's response to the given user message without persisting anything. Status updates
     * of the tools called while responding are published as {@link AiToolStatusUpdateEvent}s carrying the spec's
     * correlation id when it has one.
     *
     * @param spec what to respond to and how
     * @return the assistant's response text, or an empty string if the model produced none
     */
    String generateResponse(AiChatResponseSpec spec);

    /**
     * Returns whether AI chat is currently available, i.e. responses can be generated.
     *
     * @return {@code true} if chat is configured and ready to use
     */
    boolean isAvailable();
}
