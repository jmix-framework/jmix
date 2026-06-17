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

package io.jmix.aitoolsflowuidata.converter;

import io.jmix.aitoolsflowui.model.UserAiConversation;
import io.jmix.aitoolsflowuidata.entity.AiConversationEntity;
import io.jmix.core.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("aitls_ConversationConverter")
public class ConversationConverter {

    @Autowired
    protected Metadata metadata;

    public UserAiConversation convertToModel(AiConversationEntity conversation) {
        UserAiConversation userAiConversation = metadata.create(UserAiConversation.class);
        userAiConversation.setId(conversation.getId());
        userAiConversation.setTitle(conversation.getTitle());
        userAiConversation.setUsername(conversation.getUsername());
        userAiConversation.setCreatedDate(conversation.getCreatedDate());
        return userAiConversation;
    }

    public AiConversationEntity convertToEntity(UserAiConversation userAiConversation) {
        AiConversationEntity aiConversation = metadata.create(AiConversationEntity.class);
        aiConversation.setId(userAiConversation.getId());
        aiConversation.setTitle(userAiConversation.getTitle());
        aiConversation.setUsername(userAiConversation.getUsername());
        return aiConversation;
    }
}
