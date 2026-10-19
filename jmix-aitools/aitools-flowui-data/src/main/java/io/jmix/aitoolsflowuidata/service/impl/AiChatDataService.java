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

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.ResponseLanguageProvider;
import io.jmix.aitoolsflowuidata.service.prompt.AiChatSystemPromptProvider;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.service.AiChatResponseSpec;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowuidata.AiToolsFlowuiDataProperties;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.aitoolsflowuidata.entity.AiConversationEntity;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntityType;
import io.jmix.core.FetchPlan;
import io.jmix.core.Sort;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Default {@link AiChatService}. Generates the reply to an already-persisted user message from the conversation
 * history, windowed by {@code jmix.aitools.ui.data.chat-memory-max-messages}, and persists nothing. Only the
 * deprecated {@code processMessage} path saves the reply, as a single assistant message, when it is not blank.
 */
public class AiChatDataService implements AiChatService, InitializingBean {

    @Autowired
    protected ChatClientFactory chatClientFactory;
    @Autowired
    protected AiToolRegistry aiToolRegistry;
    @Autowired
    protected AiChatSystemPromptProvider systemPromptProvider;
    @Autowired
    protected ResponseLanguageProvider responseLanguageProvider;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected AiToolsFlowuiDataProperties dataProperties;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Nullable
    protected ChatClient chatClient;

    @Override
    public void afterPropertiesSet() {
        chatClient = chatClientFactory.createChatClientWithDefaultAdvisors().orElse(null);
    }

    @Override
    public boolean isAvailable() {
        return chatClient != null;
    }

    @Deprecated(since = "3.2", forRemoval = true)
    @Override
    public String processMessage(AiChatMessage message) {
        return processMessageInternal(message, null);
    }

    @Deprecated(since = "3.2", forRemoval = true)
    @Override
    public String processMessage(AiChatMessage message, @Nullable Consumer<AiToolStatusUpdate> statusCallback) {
        return processMessageInternal(message, statusCallback);
    }

    @Override
    public String generateResponse(AiChatResponseSpec spec) {
        Preconditions.checkNotNullArgument(spec);
        checkChatClient();

        AiChatMessageEntity userMessageEntity = loadUserMessage(spec.getUserMessage().getId());
        List<Message> history = loadHistory(userMessageEntity.getConversation().getId(), userMessageEntity);

        Map<String, Object> toolContext = new HashMap<>();
        String correlationId = spec.getCorrelationId();
        if (correlationId != null) {
            toolContext.put(AiToolStatusPublisher.CORRELATION_ID, correlationId);
        }

        String response = buildPromptSpec(history, toolContext)
                .call()
                .content();

        return response != null ? response : "";
    }

    protected String processMessageInternal(AiChatMessage message,
                                            @Nullable Consumer<AiToolStatusUpdate> statusCallback) {
        Preconditions.checkNotNullArgument(message);
        checkChatClient();

        AiChatMessageEntity userMessage = loadUserMessage(message.getId());
        List<Message> history = loadHistory(userMessage.getConversation().getId(), userMessage);

        Map<String, Object> toolContext = new HashMap<>();
        if (statusCallback != null) {
            toolContext.put(AiToolStatusPublisher.STATUS_UPDATE_CALLBACK, statusCallback);
        }

        String response = buildPromptSpec(history, toolContext).call().content();
        if (response == null || response.isBlank()) {
            return "";
        }

        saveAssistantResponse(userMessage.getConversation(), response);

        return response;
    }

    protected ChatClient.ChatClientRequestSpec buildPromptSpec(List<Message> history,
                                                               Map<String, Object> toolContext) {
        checkChatClient();

        ChatClient.ChatClientRequestSpec spec = Objects.requireNonNull(chatClient).prompt()
                .system(s -> s
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage())
                        .param("additionalInstructions", ""))
                .tools(aiToolRegistry.getAllCallbacks())
                .messages(history);

        if (!toolContext.isEmpty()) {
            spec = spec.toolContext(toolContext);
        }

        return spec;
    }

    protected AiChatMessageEntity loadUserMessage(UUID messageId) {
        AiChatMessageEntity message = dataManager.load(AiChatMessageEntity.class)
                .id(messageId)
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("conversation", FetchPlan.BASE))
                .one();

        if (message.getType() != AiChatMessageEntityType.USER) {
            throw new IllegalArgumentException("Expected %s %s but got %s for ID %s"
                    .formatted(AiChatMessageEntityType.USER, AiChatMessageEntity.class.getSimpleName(),
                            message.getType(), messageId));
        }

        AiConversationEntity conversation = message.getConversation();
        if (!Objects.equals(conversation.getUsername(), currentUsername())) {
            throw new AccessDeniedException("entity", "aitls_AiConversationEntity");
        }

        return message;
    }

    protected String currentUsername() {
        return currentUserSubstitution.getEffectiveUser().getUsername();
    }

    protected List<Message> loadHistory(UUID conversationId, AiChatMessageEntity currentUserMessage) {
        List<AiChatMessageEntity> recent = loadRecentMessages(conversationId, dataProperties.getChatMemoryMaxMessages());
        if (recent.isEmpty()) {
            // Never send an empty history: the request must always include the current user message.
            recent = List.of(currentUserMessage);
        }
        return recent.stream()
                .map(this::mapEntityToMessage)
                .toList();
    }

    protected List<AiChatMessageEntity> loadRecentMessages(UUID conversationId, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        List<AiChatMessageEntity> recent = new ArrayList<>(dataManager.load(AiChatMessageEntity.class)
                .condition(PropertyCondition.equal("conversation.id", conversationId))
                .sort(Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id")))
                .maxResults(limit)
                .list());
        Collections.reverse(recent);

        return recent;
    }

    protected Message mapEntityToMessage(AiChatMessageEntity chatMessage) {
        String content = chatMessage.getContent() != null ? chatMessage.getContent() : "";
        AiChatMessageEntityType type = chatMessage.getType();
        if (type == null) {
            return new SystemMessage(content);
        }
        return switch (type) {
            case USER -> new UserMessage(content);
            case ASSISTANT, TOOL -> new AssistantMessage(content);
            case SYSTEM -> new SystemMessage(content);
        };
    }

    protected AiChatMessageEntity saveAssistantResponse(AiConversationEntity conversation, String response) {
        AiChatMessageEntity message = dataManager.create(AiChatMessageEntity.class);
        message.setConversation(conversation);
        message.setType(AiChatMessageEntityType.ASSISTANT);
        message.setContent(response);
        return dataManager.save(message);
    }

    protected String resolveResponseLanguage() {
        return responseLanguageProvider.getResponseLanguage();
    }

    protected void checkChatClient() {
        if (chatClient == null) {
            throw new IllegalStateException(ChatClient.class.getSimpleName() + " is not configured in application");
        }
    }
}
