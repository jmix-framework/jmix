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

package io.jmix.core.datastore;

import io.jmix.core.SaveContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataStoreBeforeSaveCommitEvent extends BaseDataStoreEvent {
    private static final long serialVersionUID = -6940314788251718595L;

    protected final EventSharedState eventState;
    protected final List<Object> savedEntities;
    protected final List<Object> removedEntities;

    public DataStoreBeforeSaveCommitEvent(SaveContext saveContext, Collection<Object> savedEntities,
                                          Collection<Object> removedEntities, EventSharedState eventState) {
        super(saveContext);
        this.eventState = eventState;
        this.savedEntities = new ArrayList<>(savedEntities);
        this.removedEntities = new ArrayList<>(removedEntities);
    }

    public SaveContext getSaveContext() {
        return (SaveContext) getSource();
    }

    public EventSharedState getEventState() {
        return eventState;
    }

    public List<Object> getSavedEntities() {
        return savedEntities;
    }

    public List<Object> getRemovedEntities() {
        return removedEntities;
    }

    @Override
    public void sendTo(DataStoreEventListener listener) {
        listener.beforeSaveCommit(this);
    }
}
