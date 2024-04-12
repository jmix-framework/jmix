/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.loaders.impl.json;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class JsonMap implements Map<String, Object> {
    private Map<String, Object> instance;

    public JsonMap(Map<String, Object> entity) {
        instance = entity;
    }

    @Override
    public int size() {
        return instance.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return instance.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return instance.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return getValue(instance, key != null ? key.toString() : null);
    }

    @Override
    public Object put(String key, Object value) {
        return instance.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return instance.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        instance.putAll(m);
    }

    @Override
    public void clear() {
        instance.clear();
    }

    @Override
    public Set<String> keySet() {
        return instance.keySet();
    }

    @Override
    public Collection<Object> values() {
        return instance.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return instance.entrySet();
    }

    protected Object getValue(Map instance, String key) {
        if (key == null) return null;
        if (key.contains(".")) {
            String thisLevelProperty = StringUtils.substringBefore(key, ".");
            String remainingPath = StringUtils.substringAfter(key, ".");

            Object value = instance.get(thisLevelProperty);
            if (value instanceof Map) {
                return getValue((Map) value, remainingPath);
            } else {
                return null;
            }
        } else {
            return instance.get(key);
        }
    }
}