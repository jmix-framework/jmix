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

import io.jmix.aitools.tool.AiToolStatusUpdate;
import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.util.UUID;

/**
 * Published when a tool reported a status while the response is being generated.
 */
@NullMarked
public class AiChatGenerationProgressEvent extends AiChatGenerationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    protected final AiToolStatusUpdate update;
    protected final long sequence;

    public AiChatGenerationProgressEvent(Object source, UUID conversationId, UUID generationId,
                                         AiToolStatusUpdate update, long sequence) {
        super(source, conversationId, generationId);
        this.update = update;
        this.sequence = sequence;
    }

    /**
     * Returns the status the tool reported. It is the raw update, not yet folded into the list of statuses.
     *
     * @return the status update
     */
    public AiToolStatusUpdate getUpdate() {
        return update;
    }

    /**
     * Returns the number of the update within its response, starting at 1 and growing in publication order. A chat
     * that took a snapshot compares it with {@code AiChatGeneration.getStatusSequence()} to skip an update the
     * snapshot already includes.
     *
     * @return the 1-based sequence number of the update
     */
    public long getSequence() {
        return sequence;
    }
}
