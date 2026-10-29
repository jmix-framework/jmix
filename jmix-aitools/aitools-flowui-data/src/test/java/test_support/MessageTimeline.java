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

package test_support;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.core.UnconstrainedDataManager;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Gives test messages distinct, increasing creation times, so that their order in a conversation does not depend
 * on how fast the test creates them.
 */
public class MessageTimeline {

    protected final UnconstrainedDataManager dataManager;
    protected OffsetDateTime next = OffsetDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS);

    public MessageTimeline(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Moves the stored creation time of the message to the next point of the timeline, one second after the
     * previous one.
     *
     * @param message stored message
     * @return the same message, with the new creation time
     * @throws IllegalStateException if the stored creation time did not change
     */
    public AiChatMessage place(AiChatMessage message) {
        AiChatMessageEntity entity = dataManager.load(AiChatMessageEntity.class).id(message.getId()).one();
        entity.setCreatedDate(next);
        AiChatMessageEntity saved = dataManager.save(entity);
        if (saved.getCreatedDate() == null || !saved.getCreatedDate().isEqual(next)) {
            throw new IllegalStateException("Creation time was not stored: " + saved.getCreatedDate());
        }

        message.setCreatedDate(next);
        next = next.plusSeconds(1);
        return message;
    }
}
