/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.datastore;

import io.jmix.core.LoadContext;
import org.jspecify.annotations.Nullable;

/**
 * Event fired before a datastore executes an entity count query.
 */
public class DataStoreBeforeEntityCountEvent extends BaseDataStoreEvent {
    private static final long serialVersionUID = -6243582872039288321L;

    protected boolean countPrevented;
    protected boolean countByItems;
    protected @Nullable Integer countByItemsBatchSize;
    protected final EventSharedState eventState;

    /**
     * Creates the event for the given count request.
     *
     * @param loadContext count load context
     * @param eventState shared state for communication between datastore event phases
     */
    public DataStoreBeforeEntityCountEvent(LoadContext<?> loadContext, EventSharedState eventState) {
        super(loadContext);
        this.eventState = eventState;
    }

    /**
     * Returns the count request context.
     */
    public LoadContext<?> getLoadContext() {
        return (LoadContext<?>) getSource();
    }

    /**
     * Returns state shared with the later datastore event phases for the same operation.
     */
    public EventSharedState getEventState() {
        return eventState;
    }

    /**
     * Prevents the datastore from executing the count query.
     */
    public void setCountPrevented() {
        this.countPrevented = true;
    }

    /**
     * Returns whether count execution has been prevented.
     */
    public boolean countPrevented() {
        return countPrevented;
    }

    /**
     * Requests exact counting by loading matching items and counting them after datastore listeners are applied.
     */
    public void setCountByItems() {
        this.countByItems = true;
        this.countByItemsBatchSize = null;
    }

    /**
     * Requests exact counting by loading matching items in batches and counting them after datastore listeners are applied.
     *
     * @param batchSize batch size for item loading
     */
    public void setCountByItems(int batchSize) {
        this.countByItems = true;
        this.countByItemsBatchSize = batchSize;
    }

    /**
     * Returns whether exact item-based counting was requested.
     */
    public boolean countByItems() {
        return countByItems;
    }

    /**
     * Returns the requested item-count batch size, or {@code null} when batching was not specified explicitly.
     */
    @Nullable
    public Integer getCountByItemsBatchSize() {
        return countByItemsBatchSize;
    }

    /**
     * Dispatches the event to the matching datastore listener callback.
     */
    @Override
    public void sendTo(DataStoreEventListener listener) {
        listener.beforeEntityCount(this);
    }
}
