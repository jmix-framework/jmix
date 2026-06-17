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

import io.jmix.aitoolsflowui.model.UserAiMessage;
import io.jmix.aitoolsflowui.model.UserAiMessageType;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageType;
import io.jmix.core.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("aitls_UserAiMessageConverter")
public class MessageConverter {

    @Autowired
    protected Metadata metadata;

    public UserAiMessage convertToModel(AiChatMessageEntity entity) {
        UserAiMessage model = metadata.create(UserAiMessage.class);
        model.setId(entity.getId());
        model.setContent(entity.getContent());
        model.setType(convertToModelType(entity.getType()));
        model.setCreatedBy(entity.getCreatedBy());
        model.setCreatedDate(entity.getCreatedDate());
        return model;
    }

    public Collection<UserAiMessage> convertToModel(Collection<AiChatMessageEntity> entities) {
        List<UserAiMessage> models = new ArrayList<>(entities.size());
        for (AiChatMessageEntity chatMessage : entities) {
            models.add(convertToModel(chatMessage));
        }
        return models;
    }

    public UserAiMessageType convertToModelType(AiChatMessageType entityType) {
        return switch (entityType) {
            case USER -> UserAiMessageType.USER;
            case ASSISTANT -> UserAiMessageType.ASSISTANT;
            case TOOL -> UserAiMessageType.TOOL;
            default -> UserAiMessageType.SYSTEM;
        };
    }

    public AiChatMessageType convertToEntityType(UserAiMessageType modelType) {
        return switch (modelType) {
            case USER -> AiChatMessageType.USER;
            case ASSISTANT -> AiChatMessageType.ASSISTANT;
            case TOOL -> AiChatMessageType.TOOL;
            default -> AiChatMessageType.SYSTEM;
        };
    }
}
