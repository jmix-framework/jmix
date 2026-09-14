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

import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory {@link AiConversationService} for UI tests.
 */
public class TestAiConversationService implements AiConversationService {

    @Autowired
    protected Metadata metadata;

    protected final Map<UUID, AiConversation> conversations = new LinkedHashMap<>();

    /**
     * Creates a conversation with the given title and registers it, so that the chat and the hub views can
     * load it afterwards.
     *
     * @param title title of the conversation
     * @return the registered conversation
     */
    public AiConversation addConversation(String title) {
        AiConversation conversation = metadata.create(AiConversation.class);
        conversation.setTitle(title);
        conversation.setCreatedDate(OffsetDateTime.now());
        conversations.put(conversation.getId(), conversation);
        return conversation;
    }

    public void clear() {
        conversations.clear();
    }

    @Nullable
    @Override
    public AiConversation loadConversation(UUID conversationId) {
        return conversations.get(conversationId);
    }

    @Override
    public List<AiConversation> loadConversations() {
        return new ArrayList<>(conversations.values());
    }

    @Override
    public AiConversation create() {
        return addConversation("New chat");
    }

    @Override
    public AiConversation save(AiConversation conversation) {
        conversations.put(conversation.getId(), conversation);
        return conversation;
    }

    @Override
    public void remove(AiConversation conversation) {
        conversations.remove(conversation.getId());
    }
}
