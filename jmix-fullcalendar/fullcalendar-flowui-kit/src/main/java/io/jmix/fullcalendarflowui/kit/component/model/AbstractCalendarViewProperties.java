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

package io.jmix.fullcalendarflowui.kit.component.model;

import io.jmix.fullcalendarflowui.kit.component.model.option.CalendarOption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for configuring properties of calendar views ({@link CalendarViewType}).
 */
public abstract class AbstractCalendarViewProperties extends CalendarOption {

    protected Map<String, Object> properties;

    public AbstractCalendarViewProperties(String name) {
        super(name);
    }

    /**
     * @return unmodifiable map of properties
     */
    public Map<String, Object> getProperties() {
        return properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(properties);
    }

    /**
     * Adds property to calendar view
     *
     * @param name  property name
     * @param value property value
     */
    public void addProperty(String name, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);

        markAsDirty();
    }

    /**
     * Removes property from calendar view.
     *
     * @param name property name to remove
     */
    public void removeProperty(String name) {
        if (properties == null) {
            return;
        }

        if (properties.remove(name) != null) {
            markAsDirty();
        }
    }
}
