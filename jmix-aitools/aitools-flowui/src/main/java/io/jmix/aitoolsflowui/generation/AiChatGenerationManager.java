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

package io.jmix.aitoolsflowui.generation;

import io.jmix.aitools.tool.AiToolStatusUpdateEvent;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCompletedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationProgressEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.AiChatResponseSpec;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.impl.metadata.MetadataGenerationManager;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.UiEventPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Runs assistant responses of AI chat conversations on the server, independently of any view: a response keeps
 * going when the user refreshes the page, leaves the view or closes the tab, and its answer is persisted when it
 * is ready. At most one response runs per conversation.
 * <p>
 * Progress and the outcome are delivered as {@link AiChatGenerationEvent}s to all open UIs of the conversation's
 * owner and of the user substituting them; a chat opened while a response runs reads the current state with
 * {@link #getGeneration(AiConversation)}. The state lives in memory of the node that runs the response.
 */
@NullMarked
@Component("aitls_AiChatGenerationManager")
public class AiChatGenerationManager {

    private static final Logger log = LoggerFactory.getLogger(AiChatGenerationManager.class);

    @Autowired
    protected AiChatService chatService;
    @Autowired
    protected AiChatMessageService messageService;
    @Autowired
    protected AiConversationService conversationService;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    protected MetadataGenerationManager metadataGenerationManager;
    @Autowired
    protected AiToolsFlowuiProperties properties;

    protected final ConcurrentMap<UUID, Generation> generationsByConversation = new ConcurrentHashMap<>();
    protected final ConcurrentMap<String, Generation> generationsByCorrelationId = new ConcurrentHashMap<>();

    @Nullable
    protected ExecutorService executor;

    @PostConstruct
    protected void init() {
        executor = createExecutorService();
    }

    protected ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(properties.getGenerationPoolSize(),
                new CustomizableThreadFactory("aitls-generation-"));
    }

    @PreDestroy
    protected void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * Saves the user's message and starts generating the assistant's answer to it in the background.
     * <p>
     * Must be called on the requesting user's UI or request thread: the generation runs with the authentication
     * and HTTP session attributes captured here, which {@code session_*} parameters of row-level policies read.
     * <p>
     * If the call fails, the conversation is not left locked: a failure to save the message leaves nothing behind,
     * and a failure after the message is saved publishes {@link AiChatGenerationFailedEvent} before rethrowing.
     *
     * @param conversation conversation to answer in, owned by the current user
     * @param text         text of the user's message
     * @return the saved user message
     * @throws IllegalStateException             if AI chat is not configured
     * @throws AccessDeniedException             if the conversation does not exist or the current user does not own
     *                                           it; nothing is saved
     * @throws AiChatGenerationRejectedException if a response is already running in the conversation
     * @throws RejectedExecutionException        if the manager is shutting down; the user message is already saved
     */
    public AiChatMessage start(AiConversation conversation, String text) {
        Preconditions.checkNotNullArgument(conversation);
        Preconditions.checkNotEmptyString(text);

        if (!chatService.isAvailable()) {
            throw new IllegalStateException("AI chat is not available");
        }

        checkOwner(conversation);

        Generation generation = reserve(conversation);
        AiChatMessage userMessage = saveUserMessage(generation, conversation, text);

        submit(generation, conversation, userMessage);

        return userMessage;
    }

    /**
     * Returns whether a response is running in the conversation. Answers only the conversation's owner.
     *
     * @param conversation conversation to check
     * @return {@code true} if a response runs and the current user owns the conversation
     */
    public boolean isGenerating(AiConversation conversation) {
        return findOwned(conversation).isPresent();
    }

    /**
     * Returns the current state of the response running in the conversation. Answers only the conversation's
     * owner: tool statuses may contain the user's data.
     *
     * @param conversation conversation to check
     * @return the running response, or empty if none runs or the current user does not own the conversation
     */
    public Optional<AiChatGeneration> getGeneration(AiConversation conversation) {
        return findOwned(conversation).map(Generation::snapshot);
    }

    /**
     * Relays a tool status of a running response to the owner's UIs. A status of a call this manager does not run
     * is ignored.
     *
     * @param event the tool status
     */
    @EventListener
    public void onToolStatus(AiToolStatusUpdateEvent event) {
        Generation generation = generationsByCorrelationId.get(event.getCorrelationId());
        if (generation == null) {
            return;
        }
        // Reporters publish one at a time, so numbers go out in order;
        // snapshot readers never wait for a publish.
        synchronized (generation.publishLock) {
            long sequence;
            // Pairs with snapshot(): a reader sees the new status and its number together, or neither.
            synchronized (generation) {
                if (!generation.addStatus(event.getUpdate())) {
                    return;
                }
                sequence = generation.statusSequence;
            }
            publish(generation, new AiChatGenerationProgressEvent(this, generation.conversationId, generation.id,
                    event.getUpdate(), sequence));
        }
    }

    /**
     * Checks the stored conversation rather than the passed instance, whose owner the caller could have set.
     *
     * @param conversation conversation to check
     * @throws AccessDeniedException if the conversation does not exist or the current user does not own it
     */
    protected void checkOwner(AiConversation conversation) {
        UUID conversationId = conversation.getId();
        AiConversation stored = conversationId != null ? conversationService.loadConversation(conversationId) : null;
        if (stored == null || !currentUsername().equals(stored.getUsername())) {
            throw new AccessDeniedException("entity", "aitls_AiConversation");
        }
    }

    protected Generation reserve(AiConversation conversation) {
        UUID conversationId = Objects.requireNonNull(conversation.getId());
        Generation generation = new Generation(UUID.randomUUID(), conversationId, currentUsername(), recipients());

        if (generationsByConversation.putIfAbsent(conversationId, generation) != null) {
            throw new AiChatGenerationRejectedException(conversationId);
        }

        return generation;
    }

    protected AiChatMessage saveUserMessage(Generation generation, AiConversation conversation, String text) {
        try {
            return messageService.createMessage(conversation, AiChatMessageType.USER, text);
        } catch (RuntimeException e) {
            generationsByConversation.remove(generation.conversationId, generation);
            throw e;
        }
    }

    protected void submit(Generation generation, AiConversation conversation, AiChatMessage userMessage) {
        // Any failure here must release the reservation, or the conversation stays locked until a restart.
        try {
            GenerationExecutionContext context = GenerationExecutionContext.capture();
            // Keeps the metadata generation visible now for the whole response.
            Runnable task = metadataGenerationManager.wrap(
                    () -> context.run(() -> generate(generation, conversation, userMessage)));
            generationsByCorrelationId.put(generation.correlationId(), generation);
            generation.visible = true;

            // Started goes out before the worker runs, so it can never arrive after the outcome.
            publish(generation, new AiChatGenerationStartedEvent(this, generation.conversationId, generation.id));

            Objects.requireNonNull(executor).execute(() -> {
                try {
                    task.run();
                } catch (Throwable e) {
                    log.error("Unexpected failure of the assistant response in conversation {}",
                            generation.conversationId, e);
                    fail(generation);
                }
            });
        } catch (RuntimeException e) {
            fail(generation);
            throw e;
        }
    }

    protected void generate(Generation generation, AiConversation conversation, AiChatMessage userMessage) {
        String answer;
        try {
            answer = chatService.generateResponse(
                    new AiChatResponseSpec(userMessage)
                            .setCorrelationId(generation.correlationId()));
        } catch (RuntimeException e) {
            log.error("Failed to generate an assistant response in conversation {}", generation.conversationId, e);
            fail(generation);
            return;
        }
        if (answer.isBlank()) {
            log.warn("The model returned an empty response in conversation {}", generation.conversationId);
            fail(generation);
            return;
        }

        AiChatMessage assistantMessage;
        try {
            assistantMessage = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, answer);
        } catch (RuntimeException e) {
            log.error("Failed to save the assistant response in conversation {}", generation.conversationId, e);
            fail(generation);
            return;
        }
        // Persist, then remove, then publish: a chat that reads "no generation" must find the answer stored.
        remove(generation);
        publish(generation, new AiChatGenerationCompletedEvent(this, generation.conversationId, generation.id,
                Objects.requireNonNull(assistantMessage.getId())));
    }

    protected void fail(Generation generation) {
        if (remove(generation)) {
            publish(generation, new AiChatGenerationFailedEvent(this, generation.conversationId, generation.id));
        }
    }

    protected boolean remove(Generation generation) {
        generationsByCorrelationId.remove(generation.correlationId());
        return generationsByConversation.remove(generation.conversationId, generation);
    }

    protected void publish(Generation generation, AiChatGenerationEvent event) {
        try {
            uiEventPublisher.publishEventForUsers(event, generation.recipients);
        } catch (RuntimeException e) {
            log.warn("Failed to publish {}", event.getClass().getSimpleName(), e);
        }
    }

    protected Optional<Generation> findOwned(AiConversation conversation) {
        Generation generation = conversation.getId() != null
                ? generationsByConversation.get(conversation.getId())
                : null;
        return generation != null && generation.visible && generation.owner.equals(currentUsername())
                ? Optional.of(generation)
                : Optional.empty();
    }

    protected String currentUsername() {
        return currentUserSubstitution.getEffectiveUser().getUsername();
    }

    /**
     * The owner plus, under substitution, the user acting for them — passed explicitly because
     * {@code UiEventPublisher} resolves the substituting user on the receiving side, where another cluster node has
     * no authentication.
     *
     * @return usernames whose UIs receive the response's events
     */
    protected List<String> recipients() {
        String effective = currentUsername();
        String authenticated = currentUserSubstitution.getAuthenticatedUser().getUsername();
        return effective.equals(authenticated) ? List.of(effective) : List.of(effective, authenticated);
    }

    /**
     * A running response: its registry keys, who may see it, and the tool statuses folded so far.
     */
    protected static class Generation {

        protected final UUID id;
        protected final UUID conversationId;
        protected final String owner;
        protected final List<String> recipients;
        protected final List<AiToolStatusUpdate> statuses = new ArrayList<>();

        /**
         * Orders the publication of status updates; taken only by reporting threads, never by readers.
         */
        protected final Object publishLock = new Object();

        /**
         * Number of status updates received so far, including those that changed nothing.
         */
        protected long statusSequence;

        /**
         * Set once the user message is saved: until then the reservation only rejects a concurrent start, and reads
         * do not report a response whose question a chat could not load yet.
         */
        protected volatile boolean visible;

        protected Generation(UUID id, UUID conversationId, String owner, List<String> recipients) {
            this.id = id;
            this.conversationId = conversationId;
            this.owner = owner;
            this.recipients = recipients;
        }

        /**
         * The id under which tools of this response report their statuses.
         *
         * @return the correlation id of the model call
         */
        protected String correlationId() {
            return id.toString();
        }

        protected synchronized boolean addStatus(AiToolStatusUpdate update) {
            statusSequence++;
            return AiToolStatusFolding.fold(statuses, update);
        }

        protected synchronized AiChatGeneration snapshot() {
            return new AiChatGeneration(conversationId, id, statuses, statusSequence);
        }
    }
}
