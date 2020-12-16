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

import io.jmix.core.SaveContext;

public class DataStoreBeforeEntitySaveEvent extends BaseDataStoreEvent {
    private static final long serialVersionUID = -6243582872039288321L;

    protected boolean savePrevented;
    protected final EventSharedState eventState;

    public DataStoreBeforeEntitySaveEvent(SaveContext saveContext, EventSharedState eventState) {
        super(saveContext);
        this.eventState = eventState;
    }

    public SaveContext getSaveContext() {
        return (SaveContext) getSource();
    }

    public EventSharedState getEventState() {
        return eventState;
    }

    public void setSavePrevented() {
        this.savePrevented = true;
    }

    public boolean savePrevented() {
        return savePrevented;
    }

    @Override
    public void sendTo(DataStoreEventListener listener) {
        listener.beforeEntitySave(this);
    }
}
