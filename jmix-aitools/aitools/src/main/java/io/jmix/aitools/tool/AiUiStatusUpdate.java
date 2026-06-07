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

package io.jmix.aitools.tool;

import io.jmix.aitools.service.AiConversationChatService;
import org.jspecify.annotations.Nullable;

/**
 * Ephemeral status update emitted by an AI tool through
 * {@link io.jmix.aitools.tool.AiToolStatusPublisher} and delivered to the UI
 * through the callback put into the tool context by
 * {@link AiConversationChatService}. Not persisted.
 * <p>
 * <b>Two-phase semantics.</b> A tool publishes the SAME {@code message}
 * twice during its lifetime:
 * <ol>
 *     <li>first with {@code resultSnippet == null} — meaning "I started this
 *         step" (the UI renders it as an in-flight indicator);</li>
 *     <li>then with the same message and a non-blank {@code resultSnippet} —
 *         meaning "this step finished, here is the short result" (the UI
 *         folds the second into the first, marks it as completed and shows
 *         the snippet next to the base text).</li>
 * </ol>
 * Use {@link #isCompleted()} on the UI side to tell the two apart.
 */
public record AiUiStatusUpdate(String message, @Nullable String resultSnippet) {

    /**
     * Convenience constructor for an in-flight ("started, no result yet")
     * update. Equivalent to {@code new AiUiStatusUpdate(message, null)}.
     */
    public AiUiStatusUpdate(String message) {
        this(message, null);
    }

    /**
     * {@code true} if the update carries a non-blank {@code resultSnippet}
     * (i.e. is the "completed" half of the two-phase publish).
     */
    public boolean isCompleted() {
        return resultSnippet != null && !resultSnippet.isBlank();
    }
}
