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

import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCancelledEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCompletedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.flowui.UiEventPublisher;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Captures the events {@link UiEventPublisher#publishEventForUsers} sends, together with their recipients, before
 * they are dispatched to Vaadin sessions. The local cluster channel delivers them synchronously on the publishing
 * thread, so a generation's events arrive from its worker thread.
 */
public class TestUiUserEventCollector {

    protected static final long TIMEOUT_MILLIS = 5000;

    protected final List<UiEventPublisher.UiUserEvent> events = new CopyOnWriteArrayList<>();

    protected volatile Consumer<UiEventPublisher.UiUserEvent> onEvent = event -> {
    };

    @EventListener
    public void onUiUserEvent(UiEventPublisher.UiUserEvent event) {
        onEvent.accept(event);
        events.add(event);
    }

    /**
     * Sets code that runs on the publishing thread when an event arrives, to observe the publisher's state at
     * that very moment.
     *
     * @param onEvent receives each event before it is recorded
     */
    public void setOnEvent(Consumer<UiEventPublisher.UiUserEvent> onEvent) {
        this.onEvent = onEvent;
    }

    public void clear() {
        onEvent = event -> {
        };
        events.clear();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * Waits until an event of the given type arrives.
     *
     * @param type type of the wrapped event
     * @return the first captured event of that type, with its recipients
     * @throws AssertionError if no such event arrives within the timeout
     */
    public UiEventPublisher.UiUserEvent await(Class<? extends ApplicationEvent> type) {
        awaitCondition(() -> find(type) != null, () -> "No " + type.getSimpleName() + "; got " + events);
        return Objects.requireNonNull(find(type));
    }

    /**
     * Waits until every generation that published a started event has also published its outcome, so that no
     * worker of a finished test writes into the next one.
     *
     * @throws AssertionError if some generation does not finish within the timeout
     */
    public void awaitAllFinished() {
        awaitCondition(() -> unfinishedConversations().isEmpty(),
                () -> "Generations still running in " + unfinishedConversations());
    }

    /**
     * Returns the generation events of a conversation in the order they were published.
     *
     * @param conversationId conversation to filter by
     * @return the unwrapped events
     */
    public List<ApplicationEvent> eventsOf(UUID conversationId) {
        return events.stream()
                .map(UiEventPublisher.UiUserEvent::getEvent)
                .filter(e -> e instanceof AiChatGenerationEvent g && g.getConversationId().equals(conversationId))
                .toList();
    }

    protected UiEventPublisher.@Nullable UiUserEvent find(Class<? extends ApplicationEvent> type) {
        for (UiEventPublisher.UiUserEvent event : events) {
            if (type.isInstance(event.getEvent())) {
                return event;
            }
        }
        return null;
    }

    protected Set<UUID> unfinishedConversations() {
        Set<UUID> started = conversationsWith(AiChatGenerationStartedEvent.class);
        started.removeAll(conversationsWith(AiChatGenerationCompletedEvent.class));
        started.removeAll(conversationsWith(AiChatGenerationFailedEvent.class));
        started.removeAll(conversationsWith(AiChatGenerationCancelledEvent.class));
        return started;
    }

    protected Set<UUID> conversationsWith(Class<? extends AiChatGenerationEvent> type) {
        return events.stream()
                .map(UiEventPublisher.UiUserEvent::getEvent)
                .filter(type::isInstance)
                .map(e -> ((AiChatGenerationEvent) e).getConversationId())
                .collect(Collectors.toSet());
    }

    protected void awaitCondition(BooleanSupplier condition, Supplier<String> failure) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError(failure.get());
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }
}
