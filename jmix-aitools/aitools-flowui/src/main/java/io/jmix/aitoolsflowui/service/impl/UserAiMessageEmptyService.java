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
import io.jmix.aitoolsflowui.model.UserAiMessage;
import io.jmix.aitoolsflowui.model.UserAiMessageType;
import io.jmix.aitoolsflowui.service.UserAiMessageService;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public class UserAiMessageEmptyService implements UserAiMessageService {

    @Autowired
    protected Metadata metadata;

    @Override
    public UserAiMessage createMessage(UserAiConversation conversation, UserAiMessageType type, String message) {
        UserAiMessage aiMessage = metadata.create(UserAiMessage.class);
        aiMessage.setType(type);
        aiMessage.setConversation(conversation);
        aiMessage.setContent(message);
        aiMessage.setCreatedDate(OffsetDateTime.now());
        return aiMessage;
    }

    @Nullable
    @Override
    public UserAiMessage loadLatestMessage(UserAiConversation conversation, @Nullable UserAiMessageType type) {
        return null;
    }

    @Override
    public Collection<UserAiMessage> loadMessages(UserAiConversation conversation) {
        return List.of();
    }
}
