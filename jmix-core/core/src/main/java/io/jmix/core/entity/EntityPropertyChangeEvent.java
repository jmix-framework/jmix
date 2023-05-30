/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.entity;

import org.springframework.lang.Nullable;

/**
 * Event object for {@link EntityPropertyChangeListener}.
 */
public class EntityPropertyChangeEvent {
    private final Object item;
    private final String property;
    private final Object prevValue;
    private final Object value;

    public EntityPropertyChangeEvent(Object item, String property, Object prevValue, Object value) {
        this.item = item;
        this.property = property;
        this.prevValue = prevValue;
        this.value = value;
    }

    /**
     * @return property name
     */
    public String getProperty() {
        return property;
    }

    /**
     * @return data model object
     */
    public Object getItem() {
        return item;
    }

    /**
     * @return previous value of property
     */
    @Nullable
    public Object getPrevValue() {
        return prevValue;
    }

    /**
     * @return current value of property
     */
    @Nullable
    public Object getValue() {
        return value;
    }
}
