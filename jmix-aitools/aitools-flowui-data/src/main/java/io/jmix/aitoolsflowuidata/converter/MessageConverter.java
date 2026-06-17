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
import io.jmix.aitoolsflowuidata.entity.ChatMessage;
import io.jmix.aitoolsflowuidata.entity.ChatMessageType;
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

    public UserAiMessage convertToUserAiMessage(ChatMessage chatMessage) {
        UserAiMessage userAiMessage = metadata.create(UserAiMessage.class);
        userAiMessage.setId(chatMessage.getId());
        userAiMessage.setContent(chatMessage.getContent());
        userAiMessage.setType(convertUserAiMessageType(chatMessage.getType()));
        userAiMessage.setCreatedBy(chatMessage.getCreatedBy());
        userAiMessage.setCreatedDate(chatMessage.getCreatedDate());
        return userAiMessage;
    }

    public Collection<UserAiMessage> convertToUserAiMessages(Collection<ChatMessage> chatMessages) {
        List<UserAiMessage> userAiMessages = new ArrayList<>(chatMessages.size());
        for (ChatMessage chatMessage : chatMessages) {
            userAiMessages.add(convertToUserAiMessage(chatMessage));
        }
        return userAiMessages;
    }

    public UserAiMessageType convertUserAiMessageType(ChatMessageType chatMessageType) {
        return switch (chatMessageType) {
            case USER -> UserAiMessageType.USER;
            case ASSISTANT -> UserAiMessageType.ASSISTANT;
            case TOOL -> UserAiMessageType.TOOL;
            default -> UserAiMessageType.SYSTEM;
        };
    }

    public ChatMessageType convertChatMessageType(UserAiMessageType userAiMessageType) {
        return switch (userAiMessageType) {
            case USER -> ChatMessageType.USER;
            case ASSISTANT -> ChatMessageType.ASSISTANT;
            case TOOL -> ChatMessageType.TOOL;
            default -> ChatMessageType.SYSTEM;
        };
    }
}
