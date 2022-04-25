/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.event;

import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.EventObject;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EventBus implements Serializable {

    protected static final int EVENTS_MAP_EXPECTED_MAX_SIZE = 4;

    protected static final Consumer[] EMPTY_LISTENERS_ARRAY = new Consumer[0];

    protected Map<Class<?>, Consumer[]> events;

    public <E extends EventObject> Registration addListener(Class<E> eventType, Consumer<E> listener) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(listener, "Listener cannot be null");

        if (events == null) {
            events = new IdentityHashMap<>(EVENTS_MAP_EXPECTED_MAX_SIZE);
        }

        Consumer[] array = events.get(eventType);

        if (array == null || !ArrayUtils.contains(array, listener)) {
            int size = (array != null) ? array.length : 0;

            Consumer[] clone = newListenersArray(size + 1);
            clone[size] = listener;
            if (array != null) {
                System.arraycopy(array, 0, clone, 0, size);
            }
            events.put(eventType, clone);
        }

        return Registration.once(() -> removeListener(eventType, listener));
    }

    public <E extends EventObject> boolean hasListener(Class<E> eventType) {
        Objects.requireNonNull(eventType, "Event type cannot be null");

        if (events == null) {
            return false;
        }

        return events.get(eventType) != null;
    }

    public <E extends EventObject> void fireEvent(E event) {
        Objects.requireNonNull(event, "Event cannot be null");
        Class<E> eventType = (Class<E>) event.getClass();
        if (!hasListener(eventType)) {
            return;
        }

        Consumer[] eventListeners = events.get(eventType);
        if (eventListeners != null) {
            for (Consumer listener : eventListeners) {
                listener.accept(event);
            }
        }
    }

    public <E extends EventObject> void removeListener(Class<E> eventType, Consumer<E> listener) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(listener, "Listener cannot be null");

        if (events != null) {
            Consumer[] array = this.events.get(eventType);
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    if (listener.equals(array[i])) {
                        int size = array.length - 1;
                        if (size > 0) {
                            Consumer[] clone = newListenersArray(size);
                            System.arraycopy(array, 0, clone, 0, i);
                            System.arraycopy(array, i + 1, clone, i, size - i);
                            events.put(eventType, clone);
                        } else {
                            events.remove(eventType);
                            if (this.events.isEmpty()) {
                                this.events = null;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public <E extends EventObject> void removeListener(Class<E> eventType) {
        Objects.requireNonNull(eventType, "Event type cannot be null");

        if (events != null) {
            this.events.remove(eventType);
            if (this.events.isEmpty()) {
                this.events = null;
            }
        }
    }

    protected Consumer[] newListenersArray(int length) {
        return (0 < length)
                ? new Consumer[length]
                : EMPTY_LISTENERS_ARRAY;
    }
}
