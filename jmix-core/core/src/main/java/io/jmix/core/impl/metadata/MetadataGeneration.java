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
import io.jmix.core.metamodel.model.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable descriptor of a published metadata snapshot.
 * <p>
 * Instances of this class represent one metadata generation visible to application code,
 * including the session snapshot, aligned extended-entities state, active-scope counter,
 * and deferred cleanup actions that must run only after the generation is no longer pinned.
 */
public class MetadataGeneration {

    protected final long id;
    protected final Session session;
    protected final ExtendedEntities.ExtendedEntitiesState extendedEntitiesState;
    protected final AtomicInteger activeScopes = new AtomicInteger();
    protected final List<Runnable> retireActions;
    protected final AtomicBoolean retired = new AtomicBoolean();
    protected final AtomicBoolean retirementCompleted = new AtomicBoolean();

    /**
     * Creates an immutable metadata snapshot descriptor that can be pinned to running work.
     *
     * @param id metadata generation identifier
     * @param session metadata session snapshot published for the generation
     * @param extendedEntitiesState extended-entities state aligned with the session snapshot
     * @param retireActions actions to run after the generation is retired and no longer in use
     */
    public MetadataGeneration(long id,
                              Session session,
                              ExtendedEntities.ExtendedEntitiesState extendedEntitiesState,
                              List<Runnable> retireActions) {
        this.id = id;
        this.session = session;
        this.extendedEntitiesState = extendedEntitiesState;
        this.retireActions = new ArrayList<>(retireActions);
    }

    /**
     * Returns the identifier of this metadata generation.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the metadata session snapshot published for this generation.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns extended-entities state aligned with this generation.
     */
    public ExtendedEntities.ExtendedEntitiesState getExtendedEntitiesState() {
        return extendedEntitiesState;
    }

    /**
     * Returns the number of active scopes currently pinned to this generation.
     */
    public AtomicInteger getActiveScopes() {
        return activeScopes;
    }

    /**
     * Returns actions that must be executed when the generation is retired.
     */
    public List<Runnable> getRetireActions() {
        return retireActions;
    }

    /**
     * Returns the retirement marker of this generation.
     */
    public AtomicBoolean getRetired() {
        return retired;
    }

    /**
     * Returns the flag guarding one-time retirement completion.
     */
    public AtomicBoolean getRetirementCompleted() {
        return retirementCompleted;
    }
}
