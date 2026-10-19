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

import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitools.tool.AiToolStatusUpdateEvent;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCancelledEvent;
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
import io.jmix.core.annotation.Internal;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Runs assistant responses of AI chat conversations on the server, independently of any view: a response keeps
 * going when the user refreshes the page, leaves the view or closes the tab, and its answer is persisted when it
 * is ready. At most one response runs per conversation. A response can be {@linkplain #cancel cancelled}, and one
 * that exceeds {@code jmix.aitools.ui.assistant-response-timeout} is stopped and reported as failed.
 * <p>
 * Progress and the outcome are delivered as {@link AiChatGenerationEvent}s to all open UIs of the user who started
 * the response and of the user substituting them; a chat opened while a response runs reads the current state with
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
    @Nullable
    protected ScheduledExecutorService timeoutScheduler;

    @PostConstruct
    protected void init() {
        executor = createExecutorService();
        timeoutScheduler = createTimeoutScheduler();
    }

    /**
     * Creates the pool that runs the responses. A response is submitted while its state is locked, so the pool must
     * neither block in {@code submit} nor run the task in the submitting thread (a bounded queue or
     * {@code CallerRunsPolicy} would deadlock or block the user's request).
     *
     * @return the pool for running responses
     */
    protected ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(properties.getGenerationPoolSize(),
                new CustomizableThreadFactory("aitls-generation-"));
    }

    protected ScheduledExecutorService createTimeoutScheduler() {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1,
                new CustomizableThreadFactory("aitls-generation-timeout-"));
        // A finished response drops its timeout at once instead of keeping it queued until it would expire.
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @PreDestroy
    protected void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
        if (timeoutScheduler != null) {
            timeoutScheduler.shutdownNow();
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
     * @param conversation conversation to answer in, accessible to the current user
     * @param text         text of the user's message
     * @return the saved user message
     * @throws IllegalStateException             if AI chat is not configured
     * @throws AccessDeniedException             if the conversation does not exist or is not accessible to the
     *                                           current user; nothing is saved
     * @throws AiChatGenerationRejectedException if a response is already running in the conversation
     * @throws RejectedExecutionException        if the manager is shutting down; the user message is already saved
     */
    public AiChatMessage start(AiConversation conversation, String text) {
        Preconditions.checkNotNullArgument(conversation);
        Preconditions.checkNotEmptyString(text);

        if (!chatService.isAvailable()) {
            throw new IllegalStateException("AI chat is not available");
        }

        checkAccess(conversation);

        Generation generation = reserve(conversation);
        AiChatMessage userMessage = saveUserMessage(generation, conversation, text);

        submit(generation, conversation, userMessage);

        return userMessage;
    }

    /**
     * Returns whether a response is running in the conversation. Answers only the user who started the response.
     *
     * @param conversation conversation to check
     * @return {@code true} if a response started by the current user runs
     */
    public boolean isGenerating(AiConversation conversation) {
        return findOwned(conversation).isPresent();
    }

    /**
     * Returns the current state of the response running in the conversation. Answers only the user who started the
     * response: tool statuses may contain that user's data.
     *
     * @param conversation conversation to check
     * @return the running response, or empty if none runs or the current user did not start it
     */
    public Optional<AiChatGeneration> getGeneration(AiConversation conversation) {
        return findOwned(conversation).map(Generation::snapshot);
    }

    /**
     * Cancels the response running in the conversation. Nothing is saved for it; a model call that does not react
     * to the interruption finishes in the background and its outcome is discarded.
     *
     * @param conversation conversation whose response to cancel
     * @return {@code true} if a response was cancelled; {@code false} if none runs or its answer is already being
     * saved
     * @throws AccessDeniedException if the conversation does not exist or is not accessible to the current user
     */
    public boolean cancel(AiConversation conversation) {
        Preconditions.checkNotNullArgument(conversation);

        checkAccess(conversation);

        // Not findOwned(): a response whose user message is still being saved must be cancellable too.
        Generation generation = generationsByConversation.get(Objects.requireNonNull(conversation.getId()));
        if (generation == null || !stop(generation)) {
            return false;
        }
        // Until the task is submitted, the starting thread reports the cancel, so it never precedes Started.
        if (generation.isTaskSubmitted()) {
            publish(generation, new AiChatGenerationCancelledEvent(this, generation.conversationId, generation.id));
        }
        return true;
    }

    /**
     * Relays a tool status of a running response to the owner's UIs. A status of a call this manager does not run
     * is ignored.
     *
     * @param event the tool status
     */
    @Internal
    @EventListener
    public void onToolStatus(AiToolStatusUpdateEvent event) {
        Generation generation = generationsByCorrelationId.get(event.getCorrelationId());
        if (generation == null) {
            return;
        }
        // Reporters publish one at a time, so numbers go out in order;
        // snapshot readers never wait for a publish.
        synchronized (generation.publishLock) {
            long sequence = generation.addStatus(event.getUpdate());
            if (sequence == 0) {
                return;
            }
            publish(generation, new AiChatGenerationProgressEvent(this, generation.conversationId, generation.id,
                    event.getUpdate(), sequence));
        }
    }

    /**
     * Checks the stored conversation rather than the passed instance, which the caller could have built. Whether the
     * current user may access it is decided by {@link AiConversationService#loadConversation(UUID)}.
     *
     * @param conversation conversation to check
     * @throws AccessDeniedException if the conversation does not exist or is not accessible to the current user
     */
    protected void checkAccess(AiConversation conversation) {
        UUID conversationId = conversation.getId();
        AiConversation stored = conversationId != null ? conversationService.loadConversation(conversationId) : null;
        if (stored == null) {
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
        // Any failure here must release the reservation,
        // or the conversation stays locked until a restart.
        try {
            GenerationExecutionContext context = GenerationExecutionContext.capture();
            // Keeps the metadata generation visible now for the whole response.
            Runnable task = metadataGenerationManager.wrap(
                    () -> context.run(() -> generate(generation, conversation, userMessage)));

            // Indexed before it turns visible: a racing stop()
            // either removes the entry itself or makes markVisible() fail,
            // and the entry is removed below. False if cancelled during the user message save.
            generationsByCorrelationId.put(generation.correlationId(), generation);
            boolean cancelled = !generation.markVisible();
            if (cancelled) {
                generationsByCorrelationId.remove(generation.correlationId());
            } else {
                // Started goes out before the worker runs,
                // so it can never arrive after the outcome.
                publish(generation, new AiChatGenerationStartedEvent(this, generation.conversationId, generation.id));
                cancelled = !execute(generation, task);
            }

            if (cancelled) {
                publishCancelledBeforeSubmit(generation);
            }
        } catch (RuntimeException e) {
            // The task may already run, so it is stopped rather than only released.
            if (stop(generation)) {
                publish(generation, new AiChatGenerationFailedEvent(this, generation.conversationId, generation.id));
            } else {
                publishCancelledBeforeSubmit(generation);
            }
            throw e;
        }
    }

    /**
     * Reports a cancel that came before the task was submitted to the pool. The cancelling thread leaves the report
     * to the starting thread, which publishes it after Started, or alone if Started never went out.
     *
     * @param generation response that may have been cancelled
     */
    protected void publishCancelledBeforeSubmit(Generation generation) {
        if (generation.isCancelledBeforeSubmit()) {
            publish(generation, new AiChatGenerationCancelledEvent(this, generation.conversationId, generation.id));
        }
    }

    /**
     * Hands the response's task to the pool and starts its timeout, unless the response is cancelled already.
     *
     * @param generation response to run
     * @param task       task that generates the answer
     * @return {@code false} if the response is cancelled and nothing was started
     */
    protected boolean execute(Generation generation, Runnable task) {
        return generation.submitTask(
                () -> Objects.requireNonNull(executor).submit(() -> {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        log.error("Unexpected failure of the assistant response in conversation {}",
                                generation.conversationId, e);
                        fail(generation);
                    }
                }),
                () -> Objects.requireNonNull(timeoutScheduler).schedule(() -> onTimeout(generation),
                        getResponseTimeout().toMillis(), TimeUnit.MILLISECONDS));
    }

    protected void generate(Generation generation, AiConversation conversation, AiChatMessage userMessage) {
        String answer;
        try {
            answer = chatService.generateResponse(
                    new AiChatResponseSpec(userMessage)
                            .setCorrelationId(generation.correlationId()));
        } catch (RuntimeException e) {
            if (!generation.isCancelled()) {
                log.error("Failed to generate an assistant response in conversation {}",
                        generation.conversationId, e);
                fail(generation);
            }
            return;
        }
        if (answer.isBlank()) {
            if (!generation.isCancelled()) {
                log.warn("The model returned an empty response in conversation {}", generation.conversationId);
                fail(generation);
            }
            return;
        }
        // From here on a cancel is refused: the answer is either saved and reported, or never written.
        if (!generation.markSaving()) {
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
        cancelTimeout(generation);
        publish(generation, new AiChatGenerationCompletedEvent(this, generation.conversationId, generation.id,
                Objects.requireNonNull(assistantMessage.getId())));
    }

    protected void fail(Generation generation) {
        if (generation.markFailed() && remove(generation)) {
            cancelTimeout(generation);
            publish(generation, new AiChatGenerationFailedEvent(this, generation.conversationId, generation.id));
        }
    }

    protected void onTimeout(Generation generation) {
        if (stop(generation)) {
            log.warn("The assistant response in conversation {} timed out after {}",
                    generation.conversationId, getResponseTimeout());
            publish(generation, new AiChatGenerationFailedEvent(this, generation.conversationId, generation.id));
        }
    }

    /**
     * Stops a running response: its task is interrupted, or never starts if still queued, and its registry entry
     * is released. Publishes nothing.
     *
     * @param generation response to stop
     * @return {@code true} if this call stopped it; {@code false} if it already ended, failed, is saving its answer
     * or was stopped before
     */
    protected boolean stop(Generation generation) {
        if (!generation.markCancelled()) {
            return false;
        }
        // The handles no longer change, because submitTask() refuses a cancelled response.
        Future<?> future = generation.getFuture();
        ScheduledFuture<?> timeout = generation.getTimeout();
        if (future != null) {
            future.cancel(true);
        }
        if (timeout != null) {
            timeout.cancel(false);
        }
        // False if the reservation was released meanwhile because its user message failed to save.
        return remove(generation);
    }

    protected void cancelTimeout(Generation generation) {
        ScheduledFuture<?> timeout = generation.getTimeout();
        if (timeout != null) {
            timeout.cancel(false);
        }
    }

    protected Duration getResponseTimeout() {
        return properties.getAssistantResponseTimeout();
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
     * Life cycle of a response: from {@code RUNNING} it either saves its answer, fails or is cancelled, and only
     * one of them; saving can still fail. Only a running response can be cancelled.
     */
    protected enum State {
        RUNNING, SAVING, FAILED, CANCELLED
    }

    /**
     * A running response: its registry keys, who may see it, the tool statuses folded so far and its life cycle.
     * The monitor of the instance guards the statuses, the state and the task handles; it is never held while
     * publishing.
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

        protected State state = State.RUNNING;
        @Nullable
        protected Future<?> future;
        @Nullable
        protected ScheduledFuture<?> timeout;

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

        /**
         * Folds a status update into the statuses and numbers it. Pairs with {@link #snapshot()}: a reader sees the
         * new status and its number together, or neither.
         *
         * @param update the status update
         * @return the number of the update, or {@code 0} if it changed nothing
         */
        protected synchronized long addStatus(AiToolStatusUpdate update) {
            statusSequence++;
            return AiToolStatusUtils.fold(statuses, update) ? statusSequence : 0;
        }

        /**
         * Makes the response visible to reads, unless it was cancelled meanwhile.
         *
         * @return {@code false} if the response is no longer running
         */
        protected synchronized boolean markVisible() {
            if (state != State.RUNNING) {
                return false;
            }
            visible = true;
            return true;
        }

        /**
         * Submits the task to the pool and schedules its timeout if the response is still running. A concurrent
         * {@link #markCancelled()} either comes first and prevents the submit, or comes after and finds both handles.
         *
         * @param startTask       submits the task to the pool
         * @param scheduleTimeout schedules the timeout
         * @return {@code false} if the response is no longer running and nothing was submitted
         */
        protected synchronized boolean submitTask(Supplier<Future<?>> startTask,
                                                Supplier<ScheduledFuture<?>> scheduleTimeout) {
            if (state != State.RUNNING) {
                return false;
            }
            future = startTask.get();
            timeout = scheduleTimeout.get();
            return true;
        }

        protected synchronized boolean markCancelled() {
            if (state != State.RUNNING) {
                return false;
            }
            state = State.CANCELLED;
            return true;
        }

        @Nullable
        protected synchronized Future<?> getFuture() {
            return future;
        }

        @Nullable
        protected synchronized ScheduledFuture<?> getTimeout() {
            return timeout;
        }

        protected synchronized AiChatGeneration snapshot() {
            return new AiChatGeneration(conversationId, id, statuses, statusSequence);
        }

        protected synchronized boolean markSaving() {
            if (state != State.RUNNING) {
                return false;
            }
            state = State.SAVING;
            return true;
        }

        protected synchronized boolean markFailed() {
            if (state != State.RUNNING && state != State.SAVING) {
                return false;
            }
            state = State.FAILED;
            return true;
        }

        protected synchronized boolean isCancelled() {
            return state == State.CANCELLED;
        }

        /**
         * Returns whether the task was submitted to the pool. From then on, whoever stops the response reports the
         * cancel.
         *
         * @return {@code true} once the task is submitted
         */
        protected synchronized boolean isTaskSubmitted() {
            return future != null;
        }

        protected synchronized boolean isCancelledBeforeSubmit() {
            return state == State.CANCELLED && future == null;
        }
    }
}
