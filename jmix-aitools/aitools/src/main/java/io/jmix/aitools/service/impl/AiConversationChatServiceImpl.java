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

import io.jmix.aitools.AiToolsProperties;
import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.ResponseLanguageProvider;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitools.service.AiConversationChatService;
import io.jmix.aitools.service.AiConversationService;
import io.jmix.aitools.tool.AiUiStatusUpdate;
import io.jmix.aitools.service.prompt.AiChatSystemPromptProvider;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Component("aitols_AiConversationChatServiceImpl")
public class AiConversationChatServiceImpl implements AiConversationChatService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AiConversationChatServiceImpl.class);

    protected static final String ASSISTANT_MESSAGE_ID_KEY = "assistantMessageId";

    @Autowired
    protected ChatClientFactory chatClientFactory;
    @Autowired
    protected AiConversationService aiConversationService;
    @Autowired
    protected AiToolRegistry aiToolRegistry;
    @Autowired
    protected AiChatSystemPromptProvider systemPromptProvider;
    @Autowired
    protected ResponseLanguageProvider responseLanguageProvider;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected AiToolsProperties toolsProperties;

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
    public String process(UUID userMessageId, @Nullable Consumer<AiUiStatusUpdate> statusCallback) {
        Preconditions.checkNotNullArgument(userMessageId);
        checkChatClient();

        ChatMessage userMessage = loadUserMessage(userMessageId);
        AiConversation conversation = userMessage.getConversation();
        List<Message> history = loadHistory(conversation.getId());

        ChatMessage placeholder = createAssistantPlaceholder(conversation);
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

    @Override
    public Flux<String> processStream(UUID userMessageId, @Nullable Consumer<AiUiStatusUpdate> statusCallback) {
        Preconditions.checkNotNullArgument(userMessageId);
        checkChatClient();

        ChatMessage userMessage = loadUserMessage(userMessageId);
        AiConversation conversation = userMessage.getConversation();
        List<Message> history = loadHistory(conversation.getId());

        ChatMessage placeholder = createAssistantPlaceholder(conversation);

        StringBuffer accumulator = new StringBuffer();
        return buildPromptSpec(history, placeholder.getId(), statusCallback)
                .stream()
                .content()
                .doOnNext(accumulator::append)
                .doOnComplete(() -> saveAssistantResponseSafely(placeholder, accumulator.toString()))
                .doOnError(e -> removeAssistantPlaceholderSafely(placeholder))
                .doOnCancel(() -> removeAssistantPlaceholderSafely(placeholder));
    }

    protected ChatClient.ChatClientRequestSpec buildPromptSpec(List<Message> history,
                                                               UUID assistantMessageId,
                                                               @Nullable Consumer<AiUiStatusUpdate> statusCallback) {
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put(ASSISTANT_MESSAGE_ID_KEY, assistantMessageId);
        if (statusCallback != null) {
            toolContext.put(AiToolStatusPublisher.STATUS_UPDATE_CALLBACK, statusCallback);
        }

        return chatClient.prompt()
                .system(s -> s
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage())
                        .param("additionalInstructions", ""))
                .tools(aiToolRegistry.getAllCallbacks())
                .toolContext(toolContext)
                .messages(history);
    }

    protected ChatMessage loadUserMessage(UUID userMessageId) {
        ChatMessage message = dataManager.load(ChatMessage.class)
                .id(userMessageId)
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE)
                        .add("conversation", FetchPlan.BASE))
                .optional()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ChatMessage not found: " + userMessageId));
        if (message.getType() != ChatMessageType.USER) {
            throw new IllegalArgumentException(
                    "Expected USER ChatMessage but got " + message.getType() + " for id " + userMessageId);
        }
        return message;
    }

    protected List<Message> loadHistory(UUID conversationId) {
        List<ChatMessage> entities = aiConversationService.loadMessages(
                conversationId, toolsProperties.getChatMemoryMaxMessages());
        return entities.stream()
                .map(this::mapEntityToMessage)
                .toList();
    }

    protected Message mapEntityToMessage(ChatMessage chatMessage) {
        String content = chatMessage.getContent() != null ? chatMessage.getContent() : "";
        ChatMessageType type = chatMessage.getType();
        if (type == null) {
            return new SystemMessage(content);
        }
        return switch (type) {
            case USER -> new UserMessage(content);
            case ASSISTANT, TOOL -> new AssistantMessage(content);
            case SYSTEM -> new SystemMessage(content);
        };
    }

    protected ChatMessage createAssistantPlaceholder(AiConversation conversation) {
        ChatMessage placeholder = dataManager.create(ChatMessage.class);
        placeholder.setConversation(conversation);
        placeholder.setType(ChatMessageType.ASSISTANT);
        placeholder.setContent("");
        return dataManager.save(placeholder);
    }

    protected void saveAssistantResponse(ChatMessage placeholder, String response) {
        placeholder.setContent(response);
        dataManager.save(placeholder);
    }

    protected void saveAssistantResponseSafely(ChatMessage placeholder, String response) {
        try {
            saveAssistantResponse(placeholder, response);
        } catch (Exception e) {
            log.warn("Failed to save streamed assistant content into placeholder {}", placeholder.getId(), e);
        }
    }

    protected void removeAssistantPlaceholderSafely(ChatMessage placeholder) {
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
