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

import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.tool.AiUiStatusUpdate;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Conversation-aware chat orchestration. The caller first persists the user's {@link ChatMessage}
 * (via {@link AiConversationService}) and passes its id; this service produces the assistant's reply
 * for that turn, taking the prior conversation history into account, and persists it back into the
 * conversation.
 * <p>
 * An optional status callback receives ephemeral progress updates while the reply is produced.
 */
public interface AiConversationChatService {

    /**
     * Produces and persists the assistant reply for the given user message, blocking until the reply
     * is complete.
     *
     * @param userMessageId  id of the previously persisted user message to reply to
     * @param statusCallback optional callback for ephemeral progress updates, or {@code null}
     * @return the full reply text
     */
    @Nullable
    String process(UUID userMessageId, @Nullable Consumer<AiUiStatusUpdate> statusCallback);

    /**
     * Produces the assistant reply as a stream of text chunks, persisting the full reply once the
     * stream completes.
     * <p>
     * <b>Single-subscriber contract.</b> The returned {@link Flux} is cold: subscribing more than
     * once triggers independent reply generations and corrupts the persisted result. Callers must
     * subscribe exactly once.
     *
     * @param userMessageId  id of the previously persisted user message to reply to
     * @param statusCallback optional callback for ephemeral progress updates, or {@code null}
     * @return a cold flux emitting reply chunks in order
     */
    Flux<String> processStream(UUID userMessageId, @Nullable Consumer<AiUiStatusUpdate> statusCallback);

    /**
     * Whether the service is configured and ready to process messages. When {@code false}, callers
     * should degrade the UI to read-only instead of calling {@link #process} / {@link #processStream}.
     */
    boolean isAvailable();
}
