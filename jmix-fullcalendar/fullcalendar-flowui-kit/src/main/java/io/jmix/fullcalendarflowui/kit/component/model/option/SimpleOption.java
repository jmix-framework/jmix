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

import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * INTERNAL.
 */
public class SimpleOption<V> extends CalendarOption {
    protected boolean isValueSet = false;

    protected V value;
    protected V defaultValue;

    public SimpleOption(String name) {
        this(name, null);
    }

    public SimpleOption(String name, @Nullable V defaultValue) {
        super(name);

        this.defaultValue = defaultValue;
        this.value = this.defaultValue;
    }

    @Nullable
    public V getValue() {
        return isValueSet ? value : null;
    }

    public V getNotNullValue() {
        return Objects.requireNonNull(value);
    }

    public void setValue(@Nullable V value) {
        this.value = value;

        if (value == null) {
            this.value = this.defaultValue;
            isValueSet = false;
        } else {
            isValueSet = true;
        }

        markAsDirty();
    }

    @Nullable
    public V getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    @Override
    protected V getValueToSerialize() {
        return value;
    }
}
