/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model;

import java.io.Serializable;
import java.util.EventObject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ChartObservableObject implements Serializable {

    protected Set<ChartObservableObject> children = new LinkedHashSet<>();

    protected Consumer<ObjectChangeEvent> listener;

    protected boolean dirty;

    public ChartObservableObject() {
        dirty = true;
    }

    protected void setChartObjectChangeListener(Consumer<ObjectChangeEvent> listener) {
        this.listener = listener;
    }

    protected void fireChangeEvent() {
        if (listener != null) {
            listener.accept(new ObjectChangeEvent(this));
        }
    }

    protected boolean isDirty() {
        return dirty;
    }

    protected void markAsDirty() {
        dirty = true;
        fireChangeEvent();
    }

    protected void unmarkDirtyInDepth() {
        dirty = false;
        children.forEach(ChartObservableObject::unmarkDirtyInDepth);
    }

    protected void markAsDirtyInDepth() {
        dirty = true;
        children.forEach(ChartObservableObject::markAsDirtyInDepth);
    }

    protected boolean isDirtyInDepth() {
        if (isDirty()) {
            return true;
        }

        for (ChartObservableObject child : children) {
            if (child.isDirtyInDepth()) {
                return true;
            }
        }

        return false;
    }

    protected void addChild(ChartObservableObject child) {
        markAsDirty();

        if (child != null) {
            child.setChartObjectChangeListener(__ -> fireChangeEvent());
            children.add(child);
            child.markAsDirtyInDepth();
        }
    }

    protected void removeChild(ChartObservableObject child) {
        children.remove(child);
        child.setChartObjectChangeListener(null);
        markAsDirty();
    }

    protected Set<ChartObservableObject> getChildren() {
        return children;
    }

    public static class ObjectChangeEvent extends EventObject {

        public ObjectChangeEvent(Object source) {
            super(source);
        }
    }
}
