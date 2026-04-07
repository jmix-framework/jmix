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

package io.jmix.core.impl.metadata;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.impl.MetadataImpl;
import io.jmix.core.metamodel.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Coordinates publication, pinning, and retirement of runtime metadata generations.
 * <p>
 * Intended to provide snapshot-style metadata visibility: new work sees the latest published
 * generation, while in-flight work can stay pinned to the generation it started with until it
 * completes.
 */
@Component("core_MetadataGenerationManager")
public class MetadataGenerationManager {

    protected static final Logger log = LoggerFactory.getLogger(MetadataGenerationManager.class);

    protected final AtomicLong sequence = new AtomicLong();
    protected final AtomicReference<MetadataGeneration> currentGeneration = new AtomicReference<>();
    protected final ThreadLocal<Deque<MetadataGeneration>> pinnedGenerationHolder = new ThreadLocal<>();

    protected final MetadataImpl metadata;
    protected final ExtendedEntities extendedEntities;
    protected final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates the manager and initializes generation {@code 1} from bootstrap metadata.
     *
     * @param metadata metadata holder whose raw session is replaced on publication
     * @param extendedEntities extended-entities service providing bootstrap replacement state
     * @param applicationEventPublisher publisher used for generation lifecycle events
     */
    public MetadataGenerationManager(MetadataImpl metadata,
                                     ExtendedEntities extendedEntities,
                                     ApplicationEventPublisher applicationEventPublisher) {
        this.metadata = metadata;
        this.extendedEntities = extendedEntities;
        this.applicationEventPublisher = applicationEventPublisher;

        MetadataGeneration initialGeneration = new MetadataGeneration(
                sequence.incrementAndGet(),
                metadata.getRawSession(),
                extendedEntities.getBootstrapState(),
                List.of()
        );
        currentGeneration.set(initialGeneration);

        log.debug("Initialized metadata generation {} from bootstrap session", initialGeneration.getId());
    }

    /**
     * Returns the latest published metadata generation.
     */
    public MetadataGeneration getCurrentGeneration() {
        return currentGeneration.get();
    }

    /**
     * Returns the generation pinned to the current thread, or the latest published generation if none is pinned.
     */
    public MetadataGeneration getPinnedOrCurrentGeneration() {
        Deque<MetadataGeneration> generations = pinnedGenerationHolder.get();
        return generations != null && !generations.isEmpty() ? generations.peek() : getCurrentGeneration();
    }

    /**
     * Returns the identifier of the generation visible to the current thread.
     */
    public long getPinnedOrCurrentGenerationId() {
        return getPinnedOrCurrentGeneration().getId();
    }

    /**
     * Pins the generation currently visible to the thread and returns a scope that must be closed afterwards.
     */
    public MetadataGenerationScope enterCurrent() {
        return enter(getPinnedOrCurrentGeneration());
    }

    /**
     * Pins the specified generation to the current thread until the returned scope is closed.
     *
     * @param generation metadata generation to pin
     * @return closable scope that releases the generation pin
     */
    public MetadataGenerationScope enter(MetadataGeneration generation) {
        Deque<MetadataGeneration> generations = pinnedGenerationHolder.get();
        if (generations == null) {
            generations = new ArrayDeque<>();
            pinnedGenerationHolder.set(generations);
        }
        generations.push(generation);
        int activeScopes = generation.getActiveScopes().incrementAndGet();
        log.debug("Pinned metadata generation {} to thread '{}' (depth={}, activeScopes={})",
                generation.getId(), Thread.currentThread().getName(), generations.size(), activeScopes);
        return new MetadataGenerationScope(this, generation);
    }

    protected void leave(MetadataGeneration generation) {
        Deque<MetadataGeneration> generations = pinnedGenerationHolder.get();
        int depth = 0;
        if (generations != null) {
            MetadataGeneration current = generations.poll();
            if (current != generation) {
                throw new IllegalStateException("Metadata generation scope mismatch");
            }
            depth = generations.size();
            if (generations.isEmpty()) {
                pinnedGenerationHolder.remove();
            }
        }
        int activeScopes = generation.getActiveScopes().decrementAndGet();
        log.debug("Released metadata generation {} from thread '{}' (depth={}, activeScopes={}, retired={})",
                generation.getId(), Thread.currentThread().getName(), depth, activeScopes, generation.getRetired().get());
        tryRetireGeneration(generation);
    }

    /**
     * Wraps a runnable so it executes with the metadata generation visible at wrapping time.
     *
     * @param runnable task to wrap
     * @return generation-aware runnable
     */
    public Runnable wrap(Runnable runnable) {
        MetadataGeneration generation = getPinnedOrCurrentGeneration();
        return () -> {
            try (MetadataGenerationScope ignored = enter(generation)) {
                runnable.run();
            }
        };
    }

    /**
     * Wraps a callable so it executes with the metadata generation visible at wrapping time.
     *
     * @param callable task to wrap
     * @return generation-aware callable
     * @param <T> callable result type
     */
    public <T> Callable<T> wrap(Callable<T> callable) {
        MetadataGeneration generation = getPinnedOrCurrentGeneration();
        return () -> {
            try (MetadataGenerationScope ignored = enter(generation)) {
                return callable.call();
            }
        };
    }

    /**
     * Publishes a new metadata generation and schedules retirement actions for the generation being replaced.
     *
     * @param session metadata session snapshot to publish
     * @param extendedEntitiesState extended-entities state aligned with the session snapshot
     * @param retireActions actions to execute after the previous generation drains
     * @return newly published generation
     */
    public MetadataGeneration publish(Session session,
                                      ExtendedEntities.ExtendedEntitiesState extendedEntitiesState,
                                      List<Runnable> retireActions) {
        MetadataGeneration previousGeneration = currentGeneration.get();
        long newGenerationId = sequence.incrementAndGet();

        log.debug("Publishing metadata generation {} to replace generation {} (retireActions={})",
                newGenerationId, previousGeneration.getId(), retireActions.size());

        previousGeneration.getRetireActions().addAll(retireActions);
        MetadataGeneration newGeneration = new MetadataGeneration(
                newGenerationId,
                session,
                extendedEntitiesState,
                List.of()
        );

        metadata.setRawSession(session);
        currentGeneration.set(newGeneration);

        log.debug("Switched current metadata generation from {} to {}",
                previousGeneration.getId(), newGeneration.getId());


        try (MetadataGenerationScope ignored = enter(newGeneration)) {
            applicationEventPublisher.publishEvent(
                    new MetadataGenerationPublishedEvent(this, previousGeneration.getId(), newGeneration.getId()));
        }

        previousGeneration.getRetired().set(true);
        log.debug("Marked metadata generation {} as retired after publishing generation {}",
                previousGeneration.getId(), newGeneration.getId());
        tryRetireGeneration(previousGeneration);

        return newGeneration;
    }

    protected void tryRetireGeneration(MetadataGeneration generation) {
        if (!generation.getRetired().get()) {
            return;
        }

        int activeScopes = generation.getActiveScopes().get();
        if (activeScopes != 0) {
            log.debug("Metadata generation {} is retired but still pinned by {} active scope(s)",
                    generation.getId(), activeScopes);
            return;
        }

        if (!generation.getRetirementCompleted().compareAndSet(false, true)) {
            return;
        }

        log.debug("Retiring metadata generation {} (retireActions={})",
                generation.getId(), generation.getRetireActions().size());

        RuntimeException error = null;
        for (Runnable retireAction : generation.getRetireActions()) {
            try {
                retireAction.run();
            } catch (RuntimeException e) {
                if (error == null) {
                    error = e;
                } else {
                    error.addSuppressed(e);
                }
            }
        }

        applicationEventPublisher.publishEvent(new MetadataGenerationRetiredEvent(this, generation.getId()));

        if (error == null) {
            log.debug("Completed retirement of metadata generation {}", generation.getId());
        } else {
            log.error("Retirement of metadata generation {} failed", generation.getId(), error);
        }

        if (error != null) {
            throw error;
        }
    }
}
