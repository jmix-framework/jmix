/*
 * Copyright 2021 Haulmont.
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

package io.jmix.hibernate.impl.load;

import io.jmix.core.EntityEntry;
import io.jmix.core.EntityEntryExtraState;

import java.util.HashMap;
import java.util.Map;

public class InitialLoadedState implements EntityEntryExtraState {

    private static final long serialVersionUID = -4894693597814509836L;
    protected EntityEntry entityEntry;
    protected Map<String, Object> loadedMap = new HashMap<>();

    public InitialLoadedState(EntityEntry entityEntry) {
        this.entityEntry = entityEntry;
    }

    @Override
    public EntityEntry getEntityEntry() {
        return entityEntry;
    }

    public Map<String, Object> getLoadedMap() {
        return loadedMap;
    }

    public void setLoadedMap(Map<String, Object> loadedMap) {
        this.loadedMap = loadedMap;
    }

    public Object getLoadedValue(String key) {
        return loadedMap.get(key);
    }

    public static InitialLoadedState.Builder builder() {
        return new InitialLoadedState.Builder();
    }

    public static class Builder {
        protected Map<String, Object> loadedMap = new HashMap<>();

        protected Builder() {
        }

        public InitialLoadedState.Builder value(String key, Object value) {
            loadedMap.put(key, value);
            return this;
        }

        public InitialLoadedState build(EntityEntry entityEntry) {
            InitialLoadedState state = new InitialLoadedState(entityEntry);
            state.setLoadedMap(loadedMap);
            return state;
        }
    }
}
