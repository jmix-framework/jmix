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
import io.jmix.aitoolsflowui.service.UserAiConversationService;
import io.jmix.aitoolsflowuidata.converter.ConversationConverter;
import io.jmix.aitoolsflowuidata.entity.AiConversationEntity;
import io.jmix.core.FetchPlan;
import io.jmix.core.Messages;
import io.jmix.core.Sort;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Default {@link UserAiConversationService} backed by persisted {@link AiConversationEntity} entities,
 * mapped to and from the persistence-agnostic {@link UserAiConversation} model.
 * <p>
 * The underlying entities are an internal detail, accessed through {@link UnconstrainedDataManager}
 * so the chat user needs no direct permissions on them. Access control is enforced here: every
 * operation is scoped to the current (effective) user, and modifying another user's conversation
 * is denied.
 */
public class UserAiConversationDataService implements UserAiConversationService {

    @Autowired
    protected ConversationConverter conversationConverter;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Nullable
    @Override
    public UserAiConversation loadConversation(UUID conversationId) {
        AiConversationEntity aiConversation = loadAiConversation(conversationId);
        if (aiConversation == null || !isOwner(aiConversation)) {
            return null;
        }
        return conversationConverter.convertToModel(aiConversation);
    }

    @Override
    public UserAiConversation create() {
        AiConversationEntity conversation = dataManager.create(AiConversationEntity.class);
        conversation.setTitle(messages.getMessage("aiConversation.defaultTitle"));
        conversation.setUsername(currentUsername());
        return conversationConverter.convertToModel(dataManager.save(conversation));
    }

    @Override
    public UserAiConversation save(UserAiConversation conversation) {
        AiConversationEntity aiConversation = loadAiConversation(conversation.getId());
        if (aiConversation == null) {
            aiConversation = conversationConverter.convertToEntity(conversation);
            aiConversation.setUsername(currentUsername());
        } else {
            checkOwner(aiConversation);
            aiConversation.setTitle(conversation.getTitle());
        }
        return conversationConverter.convertToModel(dataManager.save(aiConversation));
    }

    @Override
    public void remove(UserAiConversation conversation) {
        AiConversationEntity aiConversation = loadAiConversation(conversation.getId());
        if (aiConversation == null) {
            return;
        }
        checkOwner(aiConversation);
        dataManager.remove(aiConversation);
    }

    @Override
    public List<UserAiConversation> loadConversations() {
        List<AiConversationEntity> conversations = dataManager.load(AiConversationEntity.class)
                .condition(PropertyCondition.equal("username", currentUsername()))
                .sort(Sort.by(Sort.Order.desc("createdDate")))
                .fetchPlan(FetchPlan.BASE)
                .list();
        return convert(conversations);
    }

    protected List<UserAiConversation> convert(List<AiConversationEntity> conversations) {
        List<UserAiConversation> result = new ArrayList<>(conversations.size());
        for (AiConversationEntity conversation : conversations) {
            result.add(conversationConverter.convertToModel(conversation));
        }
        return result;
    }

    protected boolean isOwner(AiConversationEntity conversation) {
        return Objects.equals(conversation.getUsername(), currentUsername());
    }

    protected void checkOwner(AiConversationEntity conversation) {
        if (!isOwner(conversation)) {
            throw new AccessDeniedException("entity", "aitls_AiConversationEntity");
        }
    }

    protected String currentUsername() {
        return currentUserSubstitution.getEffectiveUser().getUsername();
    }

    @Nullable
    protected AiConversationEntity loadAiConversation(@Nullable UUID conversationId) {
        if (conversationId == null) {
            return null;
        }
        return dataManager.load(AiConversationEntity.class)
                .id(conversationId)
                .fetchPlan(FetchPlan.BASE)
                .optional()
                .orElse(null);
    }
}
