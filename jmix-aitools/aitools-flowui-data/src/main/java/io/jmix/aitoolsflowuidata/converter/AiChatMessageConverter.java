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

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntityType;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("aitls_AiChatMessageConverter")
public class AiChatMessageConverter {

    @Autowired
    protected Metadata metadata;

    public AiChatMessage convertToModel(AiChatMessageEntity entity, AiConversation conversation) {
        AiChatMessage model = metadata.create(AiChatMessage.class);
        model.setId(entity.getId());
        model.setContent(entity.getContent());
        model.setType(convertToModelType(entity.getType()));
        model.setCreatedBy(entity.getCreatedBy());
        model.setCreatedDate(entity.getCreatedDate());
        model.setConversation(conversation);
        return model;
    }

    public Collection<AiChatMessage> convertToModel(Collection<AiChatMessageEntity> entities,
                                                    AiConversation conversation) {
        List<AiChatMessage> models = new ArrayList<>(entities.size());
        for (AiChatMessageEntity chatMessage : entities) {
            models.add(convertToModel(chatMessage, conversation));
        }
        return models;
    }

    public AiChatMessageType convertToModelType(@Nullable AiChatMessageEntityType entityType) {
        if (entityType == null) {
            // The stored type value is not one of the known types (e.g. data written by a
            // newer version or corrupted). Fall back to SYSTEM so the conversation still
            // loads instead of failing with an NPE.
            return AiChatMessageType.SYSTEM;
        }
        return switch (entityType) {
            case USER -> AiChatMessageType.USER;
            case ASSISTANT -> AiChatMessageType.ASSISTANT;
            case TOOL -> AiChatMessageType.TOOL;
            case SYSTEM -> AiChatMessageType.SYSTEM;
        };
    }

    public AiChatMessageEntityType convertToEntityType(AiChatMessageType modelType) {
        return switch (modelType) {
            case USER -> AiChatMessageEntityType.USER;
            case ASSISTANT -> AiChatMessageEntityType.ASSISTANT;
            case TOOL -> AiChatMessageEntityType.TOOL;
            case SYSTEM -> AiChatMessageEntityType.SYSTEM;
        };
    }
}
