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

package io.jmix.dynattr;

import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicAttributes implements Serializable {
    protected final Map<String, ValueHolder> values;

    public static class Changes implements Serializable {
        protected Map<String, Object> created;
        protected Map<String, Object> updated;
        protected Map<String, Object> deleted;

        protected Changes(Map<String, Object> created, Map<String, Object> updated, Map<String, Object> deleted) {
            this.created = created;
            this.updated = updated;
            this.deleted = deleted;
        }

        public boolean hasChanges() {
            return !updated.isEmpty() || !deleted.isEmpty() || !created.isEmpty();
        }

        public boolean isCreated(String attribute) {
            return created.containsKey(attribute);
        }

        public Map<String, Object> getCreated() {
            return Collections.unmodifiableMap(created);
        }

        public boolean isUpdated(String attribute) {
            return updated.containsKey(attribute);
        }

        public Map<String, Object> getUpdated() {
            return Collections.unmodifiableMap(updated);
        }

        public boolean isDeleted(String attribute) {
            return deleted.containsKey(attribute);
        }

        public Map<String, Object> getDeleted() {
            return Collections.unmodifiableMap(deleted);
        }
    }

    protected static class ValueHolder implements Serializable {
        protected String code;
        protected Object value;
        protected State state;
        protected Object oldValue;

        public ValueHolder(String code, Object value) {
            this.code = code;
            this.value = value;
            this.state = State.LOADED;
            this.oldValue = null;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        public void setValue(@Nullable Object value) {
            this.value = value;
        }

        @Nullable
        public Object getOldValue() {
            return oldValue;
        }

        public void setOldValue(@Nullable Object oldValue) {
            this.oldValue = oldValue;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public String getCode() {
            return code;
        }
    }

    protected enum State {
        CREATED,
        LOADED,
        UPDATED,
        DELETED
    }

    public DynamicAttributes() {
        this.values = new HashMap<>();
    }

    public DynamicAttributes(Map<String, Object> values) {
        this.values = values.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, e -> new ValueHolder(e.getKey(), e.getValue())));
    }

    public void setValue(String code, @Nullable Object value) {
        ValueHolder holder = values.get(code);
        if (value == null) {
            if (holder != null) {
                holder.setOldValue(holder.getValue());
                holder.setValue(null);
                holder.setState(State.DELETED);
            }
        } else {
            if (holder == null) {
                holder = new ValueHolder(code, value);
                holder.setState(State.CREATED);
                values.put(code, holder);
            } else {
                if (holder.getOldValue() == null) {
                    holder.setOldValue(holder.getValue());
                }
                holder.setValue(value);
                holder.setState(State.UPDATED);
            }
        }
    }

    @Nullable
    public Object getValue(String code) {
        ValueHolder holder = values.get(code);
        return holder == null ? null : holder.getValue();
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Changes getChanges() {
        Map<String, Object> created = new HashMap<>();
        Map<String, Object> updated = new HashMap<>();
        Map<String, Object> deleted = new HashMap<>();

        for (ValueHolder holder : values.values()) {
            if (holder.getState() == State.CREATED) {
                created.put(holder.getCode(), holder.getOldValue());
            } else if (holder.getState() == State.UPDATED) {
                updated.put(holder.getCode(), holder.getOldValue());
            } else if (holder.getState() == State.DELETED) {
                deleted.put(holder.getCode(), holder.getOldValue());
            }
        }

        return new Changes(created, updated, deleted);
    }

    public void copy(DynamicAttributes dynamicAttributes) {
        if (dynamicAttributes == null) {
            return;
        }
        for (Map.Entry<String, ValueHolder> entry : dynamicAttributes.values.entrySet()) {
            ValueHolder fromValueHolder = entry.getValue();
            ValueHolder valueHolder = new ValueHolder(fromValueHolder.getCode(), fromValueHolder.getValue());
            valueHolder.setState(fromValueHolder.getState());
            valueHolder.setOldValue(fromValueHolder.getOldValue());
            this.values.put(entry.getKey(), valueHolder);
        }
    }
}
