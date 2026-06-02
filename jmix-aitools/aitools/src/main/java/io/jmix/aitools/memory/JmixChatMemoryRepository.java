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

package io.jmix.aitools.memory;

import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.PropertyCondition;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component("aitols_JmixChatMemoryRepository")
public class JmixChatMemoryRepository implements ChatMemoryRepository {

    private static final Logger log = LoggerFactory.getLogger(JmixChatMemoryRepository.class);

    public static final String NO_OP_CONVERSATION_ID = "{noop}";

    private static final String ENTITY_ID_METADATA_KEY = "jmixEntityId";

    @Autowired
    protected DataManager dataManager;

    @Override
    public List<String> findConversationIds() {
        return dataManager.loadValue("select c.id from aitols_AiConversation c", UUID.class)
                .list()
                .stream()
                .map(UUID::toString)
                .toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        if (NO_OP_CONVERSATION_ID.equals(conversationId)) {
            return Collections.emptyList();
        }

        UUID uuid = parseConversationId(conversationId);
        return dataManager.load(AiConversation.class)
                .id(uuid)
                .optional()
                .map(conversation -> loadChatMessages(uuid).stream()
                        .map(this::mapEntityToMessage)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        if (NO_OP_CONVERSATION_ID.equals(conversationId)) {
            return;
        }

        UUID uuid = parseConversationId(conversationId);
        AiConversation conversation = findConversation(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

        SaveContext saveContext = new SaveContext();
        List<ChatMessage> existingMessages = loadChatMessages(uuid);

        Set<UUID> existingEntityIds = existingMessages.stream()
                .map(ChatMessage::getId)
                .collect(Collectors.toSet());

        for (Message message : messages) {
            // The ToolCallAdvisor is configured to keep it in memory during the tool loop,
            // so it does not need to be persisted. Persisting it would store empty
            // assistant turns and large tool-result payloads.
            if (isToolMessage(message)) {
                continue;
            }
            UUID entityId = (UUID) message.getMetadata().get(ENTITY_ID_METADATA_KEY);
            if (entityId == null || !existingEntityIds.contains(entityId)) {
                saveContext.saving(mapMessageToEntity(message, conversation));
            }
        }

        saveContext.setDiscardSaved(true);
        dataManager.save(saveContext);
    }

    @Override
    @Transactional
    public void deleteByConversationId(String conversationId) {
        if (NO_OP_CONVERSATION_ID.equals(conversationId)) {
            return;
        }

        UUID uuid = parseConversationId(conversationId);
        dataManager.load(AiConversation.class)
                .id(uuid)
                .optional()
                .ifPresent(conversation -> {
                    SaveContext saveContext = new SaveContext();
                    loadChatMessages(uuid).forEach(saveContext::removing);
                    saveContext.removing(conversation);

                    saveContext.setDiscardSaved(true);
                    dataManager.save(saveContext);
                });
    }

    protected ChatMessage mapMessageToEntity(Message message, AiConversation conversation) {
        ChatMessage chatMessage = dataManager.create(ChatMessage.class);
        chatMessage.setConversation(conversation);
        chatMessage.setContent(message.getText());
        chatMessage.setType(mapMessageToType(message));
        return chatMessage;
    }

    protected boolean isToolMessage(Message message) {
        if (message instanceof ToolResponseMessage) {
            return true;
        }
        return message instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCalls();
    }

    protected Message mapEntityToMessage(ChatMessage chatMessage) {
        String content = chatMessage.getContent();
        ChatMessageType type = chatMessage.getType();
        return mapTypeToMessage(content, type, chatMessage.getId());
    }

    protected Message mapTypeToMessage(@Nullable String content, @Nullable ChatMessageType type, UUID entityId) {
        if (type == null) {
            return new SystemMessage(content != null ? content : "");
        }

        final Map<String, Object> metadata = Map.of(ENTITY_ID_METADATA_KEY, entityId);

        content = content == null ? "" : content;

        return switch (type) {
            case USER -> UserMessage.builder().text(content).metadata(metadata).build();
            case ASSISTANT, TOOL -> AssistantMessage.builder().content(content).properties(metadata).build();
            case SYSTEM -> SystemMessage.builder().text(content).metadata(metadata).build();
        };
    }

    protected ChatMessageType mapMessageToType(Message message) {
        if (message instanceof UserMessage) {
            return ChatMessageType.USER;
        } else if (message instanceof AssistantMessage) {
            return ChatMessageType.ASSISTANT;
        } else if (message instanceof SystemMessage) {
            return ChatMessageType.SYSTEM;
        }
        return ChatMessageType.TOOL;
    }

    protected Optional<AiConversation> findConversation(UUID conversationId) {
        return dataManager.load(AiConversation.class)
                .id(conversationId)
                .optional();
    }

    protected List<ChatMessage> loadChatMessages(UUID conversationId) {
        return dataManager.load(ChatMessage.class)
                .condition(PropertyCondition.equal("conversation.id", conversationId))
                .sort(Sort.by(Sort.Order.asc("createdDate"), Sort.Order.asc("id")))
                .list();
    }

    protected UUID parseConversationId(String conversationId) {
        try {
            return UUID.fromString(conversationId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for conversation ID: {}", conversationId, e);
            throw new IllegalArgumentException("Invalid conversation ID format: " + conversationId, e);
        }
    }
}
