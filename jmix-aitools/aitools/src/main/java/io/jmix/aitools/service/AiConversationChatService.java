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

import io.jmix.aitools.tool.AiUiStatusUpdate;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Conversation-aware chat orchestration. The caller must persist the user's
 * {@link io.jmix.aitools.entity.ChatMessage} (via {@link AiConversationService})
 * BEFORE invoking this service and pass the resulting id. This service:
 * <ol>
 *     <li>loads conversation history (windowed by
 *     {@code aitools.chatMemoryMaxMessages});</li>
 *     <li>creates an empty ASSISTANT placeholder for the upcoming turn;</li>
 *     <li>invokes the LLM with the history passed via
 *     {@link org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec#messages};</li>
 *     <li>writes the response into the placeholder (or removes it on error).</li>
 * </ol>
 * <p>
 * The optional {@code statusCallback} is delivered to tools via
 * {@link io.jmix.aitools.tool.AiToolStatusPublisher} for ephemeral progress messages.
 */
public interface AiConversationChatService {

    /**
     * Blocks until the LLM finishes; returns the full response text. Persists the
     * response into the assistant placeholder. Removes the placeholder if the LLM
     * call throws.
     *
     * @return the full LLM response text
     */
    String process(UUID userMessageId, @Nullable Consumer<AiUiStatusUpdate> statusCallback);

    /**
     * Streams the LLM response. Each emitted chunk is the next slice of text.
     * The accumulated content is persisted into the assistant placeholder on
     * {@code onComplete}. On {@code onError} or {@code onCancel} the placeholder
     * is removed.
     * <p>
     * <b>Single-subscriber contract.</b> The returned {@link Flux} is cold:
     * subscribing more than once triggers independent LLM invocations and
     * corrupts the shared accumulator and placeholder. Callers must subscribe
     * exactly once.
     *
     * @return a cold flux emitting LLM response chunks in order
     */
    Flux<String> processStream(UUID userMessageId, @Nullable Consumer<AiUiStatusUpdate> statusCallback);

    /**
     * Whether the underlying chat model is configured and ready to process messages. When
     * {@code false}, {@link #process} / {@link #processStream} would fail, so callers should
     * degrade the UI to read-only instead of invoking them.
     */
    boolean isAvailable();
}
