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

import io.jmix.core.ValueLoadContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataStoreBeforeValueLoadEvent extends BaseDataStoreEvent {
    private static final long serialVersionUID = -6243582872039288321L;

    protected boolean loadPrevented;
    protected List<Integer> deniedProperties;
    protected final EventSharedState eventState;

    public DataStoreBeforeValueLoadEvent(ValueLoadContext loadContext, EventSharedState eventState) {
        super(loadContext);
        this.eventState = eventState;
    }

    public ValueLoadContext getLoadContext() {
        return (ValueLoadContext) getSource();
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

    public void addDeniedProperty(Integer property) {
        if (deniedProperties == null) {
            deniedProperties = new ArrayList<>();
        }
        deniedProperties.add(property);
    }

    public List<Integer> deniedProperties() {
        return deniedProperties != null ? deniedProperties : Collections.emptyList();
    }

    @Override
    public void sendTo(DataStoreEventListener listener) {
        listener.beforeValueLoad(this);
    }
}
