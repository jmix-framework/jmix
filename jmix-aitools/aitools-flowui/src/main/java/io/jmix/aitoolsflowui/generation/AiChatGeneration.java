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
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationProgressEvent;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.UUID;

/**
 * A running assistant response of a conversation as seen at the moment it was read: which conversation, which
 * response and the tool statuses folded so far.
 */
@NullMarked
public class AiChatGeneration {

    protected final UUID conversationId;

    protected final UUID generationId;

    protected final List<AiToolStatusUpdate> statuses;

    protected final long statusSequence;

    /**
     * Creates a snapshot.
     *
     * @param conversationId the conversation the response belongs to
     * @param generationId   the response, as identified in its {@code AiChatGenerationEvent}s
     * @param statuses       tool statuses folded so far, oldest first; copied
     * @param statusSequence sequence number of the last status update folded into {@code statuses}, {@code 0} if
     *                       none
     */
    public AiChatGeneration(UUID conversationId, UUID generationId, List<AiToolStatusUpdate> statuses,
                            long statusSequence) {
        this.conversationId = conversationId;
        this.generationId = generationId;
        this.statuses = List.copyOf(statuses);
        this.statusSequence = statusSequence;
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
     * Returns the response, matching the generation id of its events, so a reader can tell which response
     * the subsequent events belong to.
     *
     * @return the generation id
     */
    public UUID getGenerationId() {
        return generationId;
    }

    /**
     * Returns the tool statuses folded so far.
     *
     * @return unmodifiable list of statuses, oldest first
     */
    public List<AiToolStatusUpdate> getStatuses() {
        return statuses;
    }

    /**
     * Returns the sequence number of the last status update the statuses include, matching
     * {@link AiChatGenerationProgressEvent#getSequence()}: a progress event with a number not greater than this one
     * is already reflected in the snapshot.
     *
     * @return the sequence number of the last folded update, or {@code 0} if none was received
     */
    public long getStatusSequence() {
        return statusSequence;
    }
}
