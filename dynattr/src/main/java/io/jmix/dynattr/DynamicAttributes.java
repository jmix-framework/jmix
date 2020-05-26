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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class DynamicAttributes implements Serializable {
    protected final Map<String, ValueHolder> values;

    public static class Changes implements Serializable {
        protected Set<String> created;
        protected Set<String> updated;
        protected Set<String> deleted;

        protected Changes(Set<String> created, Set<String> updated, Set<String> deleted) {
            this.created = created;
            this.updated = updated;
            this.deleted = deleted;
        }

        public boolean hasChanges() {
            return !updated.isEmpty() || !deleted.isEmpty() || !created.isEmpty();
        }

        public boolean isCreated(String attribute) {
            return created.contains(attribute);
        }

        public Set<String> getCreated() {
            return Collections.unmodifiableSet(created);
        }

        public boolean isUpdated(String attribute) {
            return updated.contains(attribute);
        }

        public Set<String> getUpdated() {
            return Collections.unmodifiableSet(updated);
        }

        public boolean isDeleted(String attribute) {
            return deleted.contains(attribute);
        }

        public Set<String> getDeleted() {
            return Collections.unmodifiableSet(deleted);
        }
    }

    protected static class ValueHolder implements Serializable {
        protected String code;
        protected Object value;
        protected State state;

        public ValueHolder(String code, Object value) {
            this.code = code;
            this.value = value;
            this.state = State.LOADED;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
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
                holder.setValue(null);
                holder.setState(State.DELETED);
            }
        } else {
            if (holder == null) {
                holder = new ValueHolder(code, value);
                holder.setState(State.CREATED);
                values.put(code, holder);
            } else {
                holder.setValue(value);
                holder.setState(State.UPDATED);
            }
        }
    }

    public Object getValue(String code) {
        ValueHolder holder = values.get(code);
        return holder == null ? null : holder.getValue();
    }

    public Changes getChanges() {
        Set<String> created = new HashSet<>();
        Set<String> updated = new HashSet<>();
        Set<String> deleted = new HashSet<>();

        for (ValueHolder holder : values.values()) {
            if (holder.getState() == State.CREATED) {
                created.add(holder.getCode());
            } else if (holder.getState() == State.UPDATED) {
                updated.add(holder.getCode());
            } else if (holder.getState() == State.DELETED) {
                deleted.add(holder.getCode());
            }
        }

        return new Changes(created, updated, deleted);
    }

    public void copy(DynamicAttributes dynamicAttributes) {
        for (Map.Entry<String, ValueHolder> entry : dynamicAttributes.values.entrySet()) {
            ValueHolder fromValueHolder = entry.getValue();
            ValueHolder valueHolder = new ValueHolder(fromValueHolder.getCode(), fromValueHolder.getValue());
            valueHolder.setState(fromValueHolder.getState());
            this.values.put(entry.getKey(), valueHolder);
        }
    }
}
