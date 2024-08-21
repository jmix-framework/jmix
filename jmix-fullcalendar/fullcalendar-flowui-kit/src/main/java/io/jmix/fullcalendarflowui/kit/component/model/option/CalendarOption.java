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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.event.EventBus;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.EventObject;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class CalendarOption implements Serializable {

    protected EventBus eventBus = new EventBus();

    protected String name;
    protected boolean dirty = false;

    public CalendarOption(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    @Nullable
    protected Object getValueToSerialize() {
        return this;
    }

    protected String getName() {
        return name;
    }

    protected boolean isDirty() {
        return dirty;
    }

    protected void unmarkAsDirty() {
        this.dirty = false;
    }

    protected void markAsDirty() {
        this.dirty = true;
        fireChangeEvent(this);
    }

    protected void fireChangeEvent(CalendarOption source) {
        eventBus.fireEvent(new OptionChangeEvent(source));
    }

    protected Registration addChangeListener(Consumer<OptionChangeEvent> listener) {
        return eventBus.addListener(OptionChangeEvent.class, listener);
    }

    public static class OptionChangeEvent extends EventObject {

        public OptionChangeEvent(CalendarOption source) {
            super(source);
        }

        @Override
        public CalendarOption getSource() {
            return (CalendarOption) super.getSource();
        }
    }
}
