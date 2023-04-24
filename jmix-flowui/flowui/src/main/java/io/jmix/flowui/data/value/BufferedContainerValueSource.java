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

package io.jmix.flowui.data.value;

import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.BufferedDataUnit;
import io.jmix.flowui.model.InstanceContainer;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.Objects;

public class BufferedContainerValueSource<E, V> extends ContainerValueSource<E, V> implements BufferedDataUnit {

    protected boolean buffered;
    protected V internalValue;

    public BufferedContainerValueSource(InstanceContainer<E> container, String property) {
        this(container, property, true);
    }

    public BufferedContainerValueSource(InstanceContainer<E> container, String property, boolean buffered) {
        super(container, property);
        this.buffered = buffered;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);

        // try to get value from container
        discard();
    }

    @Override
    protected void containerItemPropertyChanged(InstanceContainer.ItemPropertyChangeEvent<E> e) {
        if (!isBuffered()) {
            super.containerItemPropertyChanged(e);
        }
    }

    @Override
    protected void setState(BindingState state) {
        super.setState(state);

        if (state == BindingState.ACTIVE) {
            discard();
        }
    }

    @Override
    public boolean isBuffered() {
        return buffered;
    }

    @Nullable
    @Override
    public V getValue() {
        return isBuffered() ? internalValue : super.getValue();
    }

    @Override
    public void setValue(@Nullable V value) {
        if (isBuffered()) {
            setValueInternal(value);
        } else {
            super.setValue(value);
        }
    }

    protected void setValueInternal(@Nullable V value) {
        ValueChangeEvent<V> valueChangeEvent = new ValueChangeEvent<>(this, internalValue, value);

        internalValue = value;

        events.fireEvent(valueChangeEvent);
    }

    @Override
    public void write() {
        if (isBuffered()
                && isModified()) {
            super.setValue(internalValue);
        }
    }

    @Override
    public void discard() {
        if (isBuffered()
                && isModified()) {
            setValueInternal(super.getValue());
        }
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(internalValue, super.getValue());
    }
}
