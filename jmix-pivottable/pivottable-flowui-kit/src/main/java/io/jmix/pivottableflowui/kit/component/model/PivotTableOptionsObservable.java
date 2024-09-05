/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.component.model;

import java.io.Serializable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Base class for all pivot table options objects that are to be serialized.
 * If an object changes, then {@link ObjectChangeEvent} is sent to the PivotTable component.
 * Then the component recreates data on the client.
 */
public abstract class PivotTableOptionsObservable implements Serializable {

    protected Consumer<ObjectChangeEvent> listener;
    protected boolean changedFromClient = false;

    public void setChangedFromClient(boolean changedFromClient) {
        this.changedFromClient = changedFromClient;
    }

    protected void setPivotTableObjectChangeListener(Consumer<ObjectChangeEvent> listener) {
        this.listener = listener;
    }

    protected void markAsChanged() {
        fireChangeEvent();
    }

    protected void fireChangeEvent() {
        if (listener != null && !changedFromClient) {
            listener.accept(new ObjectChangeEvent(this));
        }
    }

    public static class ObjectChangeEvent extends EventObject {

        public ObjectChangeEvent(Object source) {
            super(source);
        }
    }
}
