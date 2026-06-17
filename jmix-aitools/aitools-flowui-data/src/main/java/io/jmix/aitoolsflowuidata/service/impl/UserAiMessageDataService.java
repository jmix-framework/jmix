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

package io.jmix.aitoolsflowuidata.service.impl;

import io.jmix.aitoolsflowui.model.UserAiConversation;
import io.jmix.aitoolsflowui.model.UserAiMessage;
import io.jmix.aitoolsflowui.model.UserAiMessageType;
import io.jmix.aitoolsflowui.service.UserAiMessageService;
import io.jmix.aitoolsflowuidata.converter.MessageConverter;
import io.jmix.aitoolsflowuidata.entity.AiConversation;
import io.jmix.aitoolsflowuidata.entity.ChatMessage;
import io.jmix.core.FetchPlan;
import io.jmix.core.Sort;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Default {@link UserAiMessageService} backed by persisted {@link ChatMessage} entities,
 * mapped to and from the {@link UserAiMessage} model.
 */
public class UserAiMessageDataService implements UserAiMessageService {

    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected MessageConverter messageConverter;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Override
    public UserAiMessage createMessage(UserAiConversation conversation, UserAiMessageType type, String message) {
        Preconditions.checkNotNullArgument(conversation);
        Preconditions.checkNotNullArgument(type);
        Preconditions.checkNotEmptyString(message);

        AiConversation aiConversation = loadAiConversation(conversation.getId());
        if (aiConversation == null) {
            throw new IllegalArgumentException("Conversation not found: " + conversation.getId());
        }
        checkOwner(aiConversation);

        ChatMessage chatMessage = dataManager.create(ChatMessage.class);
        chatMessage.setConversation(aiConversation);
        chatMessage.setType(messageConverter.convertChatMessageType(type));
        chatMessage.setContent(message);

        return messageConverter.convertToUserAiMessage(dataManager.save(chatMessage));
    }

    @Nullable
    @Override
    public UserAiMessage loadLatestMessage(UserAiConversation conversation, @Nullable UserAiMessageType type) {
        Preconditions.checkNotNullArgument(conversation);

        PropertyCondition conversationCondition = PropertyCondition.equal("conversation.id", conversation.getId());
        Condition condition = type == null
                ? conversationCondition
                : LogicalCondition.and(conversationCondition,
                        PropertyCondition.equal("type", messageConverter.convertChatMessageType(type)));

        ChatMessage chatMessage = dataManager.load(ChatMessage.class)
                .condition(condition)
                .sort(Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id")))
                .fetchPlan(FetchPlan.BASE)
                .maxResults(1)
                .optional()
                .orElse(null);

        return chatMessage != null ? messageConverter.convertToUserAiMessage(chatMessage) : null;
    }

    @Override
    public Collection<UserAiMessage> loadMessages(UserAiConversation conversation) {
        Preconditions.checkNotNullArgument(conversation);
        List<ChatMessage> messages = dataManager.load(ChatMessage.class)
                .condition(PropertyCondition.equal("conversation.id", conversation.getId()))
                .sort(Sort.by(Sort.Order.asc("createdDate"), Sort.Order.asc("id")))
                .fetchPlan(FetchPlan.BASE)
                .list();
        return messageConverter.convertToUserAiMessages(messages);
    }

    protected void checkOwner(AiConversation conversation) {
        if (!Objects.equals(conversation.getUsername(), currentUsername())) {
            throw new AccessDeniedException("entity", "aitls_AiConversation");
        }
    }

    protected String currentUsername() {
        return currentUserSubstitution.getEffectiveUser().getUsername();
    }

    @Nullable
    protected AiConversation loadAiConversation(@Nullable UUID conversationId) {
        if (conversationId == null) {
            return null;
        }
        return dataManager.load(AiConversation.class)
                .id(conversationId)
                .fetchPlan(FetchPlan.BASE)
                .optional()
                .orElse(null);
    }
}
