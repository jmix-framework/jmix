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

import org.jspecify.annotations.Nullable;

/**
 * Ephemeral status update emitted by an AI tool through {@link AiToolStatusPublisher}.
 * <p>
 * <b>Two-phase semantics.</b> A tool publishes the SAME {@code message}
 * twice during its lifetime:
 * <ol>
 *     <li>
 *         first with {@code resultSnippet == null} — meaning "I started this
 *         step" (the consumer renders it as an in-flight indicator);
 *     </li>
 *     <li>
 *         then with the same message and a non-blank {@code resultSnippet} —
 *         meaning "this step finished, here is the short result" (the consumer
 *         folds the second into the first, marks it as completed and shows
 *         the snippet next to the base text).
 *     </li>
 * </ol>
 * Use {@link #isCompleted()} to tell the two apart.
 */
public class AiToolStatusUpdate {

    protected final String message;

    @Nullable
    protected final String resultSnippet;

    public AiToolStatusUpdate(String message, @Nullable String resultSnippet) {
        this.message = message;
        this.resultSnippet = resultSnippet;
    }

    /**
     * Convenience constructor for an in-flight ("started, no result yet")
     * update. Equivalent to {@code new AiToolStatusUpdate(message, null)}.
     *
     * @param message status text describing the step that has started
     */
    public AiToolStatusUpdate(String message) {
        this(message, null);
    }

    /**
     * Returns the status message.
     *
     * @return status message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the short result snippet of a completed step.
     *
     * @return result snippet, or {@code null} for an in-flight update
     */
    @Nullable
    public String getResultSnippet() {
        return resultSnippet;
    }

    /**
     * {@code true} if the update carries a non-blank {@code resultSnippet}
     * (i.e. is the "completed" half of the two-phase publish).
     *
     * @return whether this update represents a completed step
     */
    public boolean isCompleted() {
        return resultSnippet != null && !resultSnippet.isBlank();
    }
}
