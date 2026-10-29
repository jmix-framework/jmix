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

package test_support;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.KeepInContextResult;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link AiChatMessageService} for UI tests. Thread-safe, since generations save answers from worker
 * threads.
 */
public class TestAiChatMessageService implements AiChatMessageService {

    protected static final int DEFAULT_KEEP_IN_CONTEXT_LIMIT = 5;

    @Autowired
    protected Metadata metadata;

    protected final Map<UUID, List<AiChatMessage>> messagesByConversation = new ConcurrentHashMap<>();
    protected final Map<AiChatMessageType, RuntimeException> failures = new ConcurrentHashMap<>();
    protected final Map<AiChatMessageType, Runnable> beforeSaveActions = new ConcurrentHashMap<>();

    protected volatile int keepInContextLimit = DEFAULT_KEEP_IN_CONTEXT_LIMIT;

    public void clear() {
        keepInContextLimit = DEFAULT_KEEP_IN_CONTEXT_LIMIT;
        messagesByConversation.clear();
        failures.clear();
        beforeSaveActions.clear();
    }

    /**
     * Runs an action on the saving thread before a message of the given type is saved, e.g. to hold the save on a
     * latch, until {@link #clear()} or a call with {@code null}.
     *
     * @param type   type of messages the action precedes
     * @param action action to run, or {@code null} to save them without one
     */
    public void setBeforeSave(AiChatMessageType type, @Nullable Runnable action) {
        if (action == null) {
            beforeSaveActions.remove(type);
        } else {
            beforeSaveActions.put(type, action);
        }
    }

    /**
     * Makes saving messages of the given type throw, until {@link #clear()} or a call with {@code null}.
     *
     * @param type      type of messages that fail to save
     * @param exception exception to throw, or {@code null} to save them normally again
     */
    public void setFailure(AiChatMessageType type, @Nullable RuntimeException exception) {
        if (exception == null) {
            failures.remove(type);
        } else {
            failures.put(type, exception);
        }
    }

    @Override
    public AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message) {
        Runnable beforeSave = beforeSaveActions.get(type);
        if (beforeSave != null) {
            beforeSave.run();
        }
        RuntimeException failure = failures.get(type);
        if (failure != null) {
            throw failure;
        }
        AiChatMessage chatMessage = metadata.create(AiChatMessage.class);
        chatMessage.setConversation(conversation);
        chatMessage.setType(type);
        chatMessage.setContent(message);
        chatMessage.setCreatedDate(OffsetDateTime.now());

        messagesByConversation.computeIfAbsent(conversation.getId(),
                id -> Collections.synchronizedList(new ArrayList<>())).add(chatMessage);

        return chatMessage;
    }

    @Override
    public AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message,
                                       boolean keepInContext) {
        if (keepInContext) {
            checkKeepable(type);
        }
        AiChatMessage chatMessage = createMessage(conversation, type, message);
        if (keepInContext) {
            setKeepInContext(chatMessage, true);
        }
        return chatMessage;
    }

    @Override
    public KeepInContextResult setKeepInContext(AiChatMessage message, boolean keep) {
        checkKeepable(message.getType());
        if (keep && !Boolean.TRUE.equals(message.getKeepInContext())) {
            if (isKeepInContextLimitReached(message.getConversation())) {
                return KeepInContextResult.LIMIT_REACHED;
            }
        }
        message.setKeepInContext(keep);
        return KeepInContextResult.DONE;
    }

    @Override
    public Collection<AiChatMessage> loadKeptInContextMessages(AiConversation conversation) {
        return messagesOf(conversation).stream()
                .filter(message -> Boolean.TRUE.equals(message.getKeepInContext()))
                .toList();
    }

    @Override
    public boolean isKeepInContextLimitReached(AiConversation conversation) {
        return loadKeptInContextMessages(conversation).size() >= keepInContextLimit;
    }

    /**
     * Sets how many messages a conversation can keep in context, until {@link #clear()}.
     *
     * @param keepInContextLimit maximum number of kept messages per conversation
     */
    public void setKeepInContextLimit(int keepInContextLimit) {
        this.keepInContextLimit = keepInContextLimit;
    }

    @Nullable
    @Override
    public AiChatMessage loadLatestMessage(AiConversation conversation, @Nullable AiChatMessageType type) {
        return messagesOf(conversation).stream()
                .filter(message -> type == null || type.equals(message.getType()))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    @Override
    public Collection<AiChatMessage> loadMessages(AiConversation conversation) {
        return messagesOf(conversation);
    }

    @Override
    public Map<AiConversation, AiChatMessage> loadLatestMessages(Collection<AiConversation> conversations,
                                                                 @Nullable AiChatMessageType type) {
        Map<AiConversation, AiChatMessage> result = new LinkedHashMap<>();
        for (AiConversation conversation : conversations) {
            messagesOf(conversation).stream()
                    .filter(message -> type == null || type.equals(message.getType()))
                    .max(Comparator.comparing(AiChatMessage::getCreatedDate))
                    .ifPresent(message -> result.put(conversation, message));
        }
        return result;
    }

    /**
     * Registers a user message with an explicit creation date, so tests can control conversation activity.
     *
     * @param conversation conversation the message belongs to
     * @param createdDate  creation date of the message
     * @return the registered message
     */
    public AiChatMessage addMessage(AiConversation conversation, OffsetDateTime createdDate) {
        AiChatMessage chatMessage = createMessage(conversation, AiChatMessageType.USER, "message");
        chatMessage.setCreatedDate(createdDate);
        return chatMessage;
    }

    protected void checkKeepable(@Nullable AiChatMessageType type) {
        if (type != AiChatMessageType.USER && type != AiChatMessageType.ASSISTANT) {
            throw new IllegalArgumentException("Only user and assistant messages can be kept in context: " + type);
        }
    }

    protected List<AiChatMessage> messagesOf(AiConversation conversation) {
        List<AiChatMessage> messages = messagesByConversation.get(conversation.getId());
        if (messages == null) {
            return List.of();
        }
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }
}
