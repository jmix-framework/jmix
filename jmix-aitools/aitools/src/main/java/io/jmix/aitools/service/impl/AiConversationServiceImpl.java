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

package io.jmix.aitools.service.impl;

import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitools.service.AiConversationService;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Sort;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.PropertyCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("aitols_AiConversationServiceImpl")
public class AiConversationServiceImpl implements AiConversationService {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Messages messages;

    @Override
    public AiConversation createNewConversation() {
        AiConversation conversation = dataManager.create(AiConversation.class);
        conversation.setTitle(messages.getMessage("aiConversation.defaultTitle"));
        return dataManager.save(conversation);
    }

    @Override
    public ChatMessage createUserMessage(AiConversation conversation, String text) {
        Preconditions.checkNotNullArgument(conversation);
        Preconditions.checkNotEmptyString(text);

        ChatMessage message = dataManager.create(ChatMessage.class);
        message.setConversation(conversation);
        message.setType(ChatMessageType.USER);
        message.setContent(text);
        return dataManager.save(message);
    }

    @Override
    public List<ChatMessage> loadMessages(UUID conversationId, int limit) {
        Preconditions.checkNotNullArgument(conversationId);
        if (limit <= 0) {
            return List.of();
        }
        List<ChatMessage> all = dataManager.load(ChatMessage.class)
                .condition(PropertyCondition.equal("conversation.id", conversationId))
                .sort(Sort.by(Sort.Order.asc("createdDate"), Sort.Order.asc("id")))
                .list();
        if (all.size() <= limit) {
            return all;
        }
        return all.subList(all.size() - limit, all.size());
    }
}
