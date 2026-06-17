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

package io.jmix.aitoolsflowui.service.impl;

import io.jmix.aitoolsflowui.model.UserAiConversation;
import io.jmix.aitoolsflowui.service.UserAiConversationService;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class UserAiConversationEmptyService implements UserAiConversationService {

    @Autowired
    protected Metadata metadata;

    @Nullable
    @Override
    public UserAiConversation loadConversation(UUID conversationId) {
        return null;
    }

    @Override
    public UserAiConversation create() {
        return metadata.create(UserAiConversation.class);
    }

    @Override
    public UserAiConversation save(UserAiConversation conversation) {
        return conversation;
    }

    @Override
    public void remove(UserAiConversation conversation) {
    }

    @Override
    public List<UserAiConversation> loadConversations() {
        return List.of();
    }
}
