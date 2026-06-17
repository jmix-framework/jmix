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
import io.jmix.aitools.service.prompt.AiChatSystemPromptProvider;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.aitools.tool.AiUiStatusUpdate;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.service.UserAiChatService;
import io.jmix.aitoolsflowuidata.AiToolsFlowuiDataProperties;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.aitoolsflowuidata.entity.AiConversationEntity;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntityType;
import io.jmix.core.FetchPlan;
import io.jmix.core.Sort;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.PropertyCondition;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Default {@link UserAiChatService}. For an already-persisted user message it:
 * <ol>
 *     <li>loads the conversation history, windowed by {@code jmix.aitools.ui.data.chat-memory-max-messages};</li>
 *     <li>creates an empty {@code ASSISTANT} placeholder message;</li>
 *     <li>invokes the chat client with that history;</li>
 *     <li>writes the reply into the placeholder, or removes it if the call fails.</li>
 * </ol>
 * The optional status callback is delivered to tools via {@link AiToolStatusPublisher} for
 * ephemeral progress messages.
 */
public class UserAiChatDataService implements UserAiChatService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(UserAiChatDataService.class);

    protected static final String ASSISTANT_MESSAGE_ID_KEY = "assistantMessageId";

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

    @Override
    public String processMessage(AiChatMessage message) {
        return processMessageInternal(message, null);
    }

    @Override
    public String processMessage(AiChatMessage message, @Nullable Consumer<AiUiStatusUpdate> statusCallback) {
        return processMessageInternal(message, statusCallback);
    }

    protected String processMessageInternal(AiChatMessage message,
                                            @Nullable Consumer<AiUiStatusUpdate> statusCallback) {
        Preconditions.checkNotNullArgument(message);
        String response = process(message.getId(), statusCallback);
        return response != null ? response : "";
    }

    @Nullable
    protected String process(UUID userMessageId, @Nullable Consumer<AiUiStatusUpdate> statusCallback) {
        Preconditions.checkNotNullArgument(userMessageId);
        checkChatClient();

        AiChatMessageEntity userMessage = loadUserMessage(userMessageId);
        AiConversationEntity conversation = userMessage.getConversation();
        List<Message> history = loadHistory(conversation.getId());

        AiChatMessageEntity placeholder = createAssistantPlaceholder(conversation);
        try {
            String response = buildPromptSpec(history, placeholder.getId(), statusCallback)
                    .call()
                    .content();
            saveAssistantResponse(placeholder, response);
            return response;
        } catch (RuntimeException e) {
            removeAssistantPlaceholderSafely(placeholder);
            throw e;
        }
    }

    protected ChatClient.ChatClientRequestSpec buildPromptSpec(List<Message> history,
                                                               UUID assistantMessageId,
                                                               @Nullable Consumer<AiUiStatusUpdate> statusCallback) {
        checkChatClient();

        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put(ASSISTANT_MESSAGE_ID_KEY, assistantMessageId);
        if (statusCallback != null) {
            toolContext.put(AiToolStatusPublisher.STATUS_UPDATE_CALLBACK, statusCallback);
        }

        return Objects.requireNonNull(chatClient).prompt()
                .system(s -> s
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage())
                        .param("additionalInstructions", ""))
                .tools(aiToolRegistry.getAllCallbacks())
                .toolContext(toolContext)
                .messages(history);
    }

    protected AiChatMessageEntity loadUserMessage(UUID userMessageId) {
        AiChatMessageEntity message = dataManager.load(AiChatMessageEntity.class)
                .id(userMessageId)
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE)
                        .add("conversation", FetchPlan.BASE))
                .optional()
                .orElseThrow(() -> new IllegalArgumentException(
                        "AiChatMessageEntity not found: " + userMessageId));
        if (message.getType() != AiChatMessageEntityType.USER) {
            throw new IllegalArgumentException(
                    "Expected USER AiChatMessageEntity but got " + message.getType() + " for id " + userMessageId);
        }
        return message;
    }

    protected List<Message> loadHistory(UUID conversationId) {
        return loadRecentMessages(conversationId, dataProperties.getChatMemoryMaxMessages()).stream()
                .map(this::mapEntityToMessage)
                .toList();
    }

    protected List<AiChatMessageEntity> loadRecentMessages(UUID conversationId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<AiChatMessageEntity> all = dataManager.load(AiChatMessageEntity.class)
                .condition(PropertyCondition.equal("conversation.id", conversationId))
                .sort(Sort.by(Sort.Order.asc("createdDate"), Sort.Order.asc("id")))
                .list();
        if (all.size() <= limit) {
            return all;
        }
        return all.subList(all.size() - limit, all.size());
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

    protected AiChatMessageEntity createAssistantPlaceholder(AiConversationEntity conversation) {
        AiChatMessageEntity placeholder = dataManager.create(AiChatMessageEntity.class);
        placeholder.setConversation(conversation);
        placeholder.setType(AiChatMessageEntityType.ASSISTANT);
        placeholder.setContent("");
        return dataManager.save(placeholder);
    }

    protected void saveAssistantResponse(AiChatMessageEntity placeholder, @Nullable String response) {
        placeholder.setContent(response);
        dataManager.saveWithoutReload(placeholder);
    }

    protected void removeAssistantPlaceholderSafely(AiChatMessageEntity placeholder) {
        try {
            dataManager.remove(placeholder);
        } catch (Exception cleanupError) {
            log.warn("Failed to remove assistant placeholder {}", placeholder.getId(), cleanupError);
        }
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
