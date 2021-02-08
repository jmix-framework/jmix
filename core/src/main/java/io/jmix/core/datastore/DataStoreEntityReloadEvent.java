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
import io.jmix.core.SaveContext;

public class DataStoreEntityReloadEvent extends BaseDataStoreEvent {
    private static final long serialVersionUID = -6243582872039288321L;

    protected final SaveContext saveContext;
    protected final EventSharedState eventState;

    public DataStoreEntityReloadEvent(LoadContext<?> loadContext,
                                      SaveContext saveContext,
                                      EventSharedState eventState) {
        super(loadContext);
        this.saveContext = saveContext;
        this.eventState = eventState;
    }

    public LoadContext<?> getLoadContext() {
        return (LoadContext<?>) getSource();
    }

    public SaveContext getSaveContext() {
        return saveContext;
    }

    public EventSharedState getEventState() {
        return eventState;
    }

    @Override
    public void sendTo(DataStoreEventListener listener) {
        listener.entityReload(this);
    }
}
