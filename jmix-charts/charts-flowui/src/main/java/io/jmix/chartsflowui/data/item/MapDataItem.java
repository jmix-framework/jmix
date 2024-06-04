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

package io.jmix.chartsflowui.data.item;

import io.jmix.chartsflowui.kit.data.chart.DataItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data item, which is a set of key-value pairs.
 */
public class MapDataItem implements DataItem {

    protected Map<String, Object> properties = new HashMap<>();

    protected final UUID id = UUID.randomUUID();

    public MapDataItem() {
    }

    public MapDataItem(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    /**
     * @param property name of property
     * @return the value of a property with the specified property name
     */
    @Override
    public Object getValue(String property) {
        return properties.get(property);
    }

    /**
     * Adds value with given key.
     *
     * @param key   name of property
     * @param value property value
     * @return this {@link MapDataItem}
     */
    public MapDataItem add(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Removes property from this {@link MapDataItem}
     *
     * @param key name of property
     */
    public void remove(String key) {
        properties.remove(key);
    }

    @Override
    public Object getId() {
        return id;
    }
}
