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

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataStoreAfterEntityLoadEvent extends BaseDataStoreEvent {
    private static final long serialVersionUID = -6243582872039288321L;

    protected final List<Object> entities;
    protected List<Object> excludedEntities;
    protected final EventSharedState eventState;

    public DataStoreAfterEntityLoadEvent(LoadContext<?> loadContext, List<Object> entities, EventSharedState eventState) {
        super(loadContext);
        this.entities = entities;
        this.eventState = eventState;
    }

    public DataStoreAfterEntityLoadEvent(LoadContext<?> loadContext, @Nullable Object entity, EventSharedState eventState) {
        super(loadContext);
        this.entities = entity == null ? Collections.emptyList() : Collections.singletonList(entity);
        this.eventState = eventState;
    }

    public LoadContext<?> getLoadContext() {
        return (LoadContext<?>) getSource();
    }

    public EventSharedState getEventState() {
        return eventState;
    }

    public void excludeEntity(Object entity) {
        if (excludedEntities == null) {
            excludedEntities = new ArrayList<>();
        }
        excludedEntities.add(entity);
    }

    @Nullable
    public Object getResultEntity() {
        List<Object> resultEntities = getResultEntities();
        return resultEntities.isEmpty() ? null : resultEntities.get(0);
    }

    public List<Object> getResultEntities() {
        if (excludedEntities == null) {
            return entities;
        } else {
            List<Object> resultEntities = new ArrayList<>(entities);
            resultEntities.removeAll(excludedEntities);
            return resultEntities;
        }
    }

    @Override
    public void sendTo(DataStoreEventListener listener) {
        listener.afterEntityLoad(this);
    }
}
