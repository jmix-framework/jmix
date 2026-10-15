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

package generation;

import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiConversation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatGenerationEventTest {

    final UUID conversationId = UUID.randomUUID();
    final AiChatGenerationStartedEvent event =
            new AiChatGenerationStartedEvent(this, conversationId, UUID.randomUUID());

    @Test
    void belongsTo_sameConversation_true() {
        assertTrue(event.belongsTo(conversation(conversationId)));
    }

    @Test
    void belongsTo_otherConversation_false() {
        assertFalse(event.belongsTo(conversation(UUID.randomUUID())));
    }

    @Test
    void belongsTo_noConversation_false() {
        assertFalse(event.belongsTo(null));
    }

    private AiConversation conversation(UUID id) {
        AiConversation conversation = new AiConversation();
        conversation.setId(id);
        return conversation;
    }
}
