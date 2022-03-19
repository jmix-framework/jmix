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

public class DataStoreBeforeEntityLoadEvent extends BaseDataStoreEvent {
    private static final long serialVersionUID = -6243582872039288321L;

    protected boolean loadPrevented;
    protected final EventSharedState eventState;

    public DataStoreBeforeEntityLoadEvent(LoadContext<?> loadContext, EventSharedState eventState) {
        super(loadContext);
        this.eventState = eventState;
    }

    public LoadContext<?> getLoadContext() {
        return (LoadContext<?>) getSource();
    }

    public EventSharedState getEventState() {
        return eventState;
    }

    public void setLoadPrevented() {
        this.loadPrevented = true;
    }

    public boolean loadPrevented() {
        return loadPrevented;
    }

    @Override
    public void sendTo(DataStoreEventListener listener) {
        listener.beforeEntityLoad(this);
    }
}
