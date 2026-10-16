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

import io.jmix.aitools.tool.AiToolStatusUpdate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The rule that folds a stream of tool status updates into the short list a chat shows: a step that is still in
 * flight is replaced by its completion, a repeated in-flight update is ignored, and only the newest
 * {@link #MAX_STATUSES} entries are kept.
 */
@NullMarked
public final class AiToolStatusFolding {

    /**
     * Maximum number of statuses kept.
     */
    public static final int MAX_STATUSES = 6;

    private AiToolStatusFolding() {
    }

    /**
     * Folds an update into the list.
     *
     * @param statuses statuses folded so far, oldest first; modified in place
     * @param update   the next update; {@code null} or one with a blank message is ignored
     * @return {@code true} if the list changed
     */
    public static boolean fold(List<AiToolStatusUpdate> statuses, @Nullable AiToolStatusUpdate update) {
        if (update == null || update.getMessage().isBlank()) {
            return false;
        }
        if (!statuses.isEmpty()) {
            AiToolStatusUpdate last = statuses.get(statuses.size() - 1);
            // Fold only into a step that is still in flight: a completed entry with the same message belongs
            // to a previous tool call and must not swallow the start of the next one.
            if (last.getMessage().equals(update.getMessage()) && !last.isCompleted()) {
                if (!update.isCompleted()) {
                    return false;
                }
                statuses.set(statuses.size() - 1, update);
                return true;
            }
        }
        statuses.add(update);
        if (statuses.size() > MAX_STATUSES) {
            statuses.removeFirst();
        }
        return true;
    }
}
