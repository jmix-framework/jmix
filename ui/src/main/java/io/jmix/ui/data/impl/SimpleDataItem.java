/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.data.impl;

import io.jmix.core.metamodel.model.utils.MethodsCache;
import io.jmix.core.metamodel.model.utils.ObjectPathUtils;
import io.jmix.ui.data.DataItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data item, which contains an instance of any class.
 */
public class SimpleDataItem implements DataItem, DataItem.HasId {

    private static final long serialVersionUID = -283289357315588981L;

    private static transient Map<Class, MethodsCache> methodCacheMap = new ConcurrentHashMap<>();

    protected Object item;

    public SimpleDataItem(Object item) {
        this.item = item;
    }

    /**
     * @return item
     */
    public Object getItem() {
        return item;
    }

    /**
     * Sets item.
     *
     * @param item item to be set
     */
    public void setItem(Object item) {
        this.item = item;
    }

    /**
     * Get an property value. Locates the property by the given path in object graph starting from this object.
     * <p>
     * Each property in a class which will be used by {@code SimpleDataItem} must have a {@code public} getter method.
     * Reflection is used to get property values.
     *
     * @param path path to the attribute
     * @return the value of a property with the specified property path.
     * If property value is an instance of {@link Collection},
     * then method returns {@link List} of {@link SimpleDataItem}.
     * If any traversing property value is null, this method stops here and returns current value.
     * Otherwise method returns getter value
     */
    @Override
    public Object getValue(String path) {
        String[] properties = ObjectPathUtils.parseValuePath(path);
        Object currentValue = null;
        Object currentObject = item;

        for (String property : properties) {
            if (currentObject == null) {
                break;
            }

            currentValue = getMethodsCache(currentObject).getGetter(property).apply(currentObject);

            if (currentValue == null) {
                break;
            }

            if (currentValue instanceof Collection) {
                List<DataItem> items = new ArrayList<>();

                for (Object item : (Collection) currentValue) {
                    items.add(new SimpleDataItem(item));
                }
                return items;
            }

            currentObject = currentValue;
        }

        return currentValue;
    }

   protected MethodsCache getMethodsCache(Object object) {
        Class cls = object.getClass();
        return methodCacheMap.computeIfAbsent(cls, k -> MethodsCache.getOrCreate(cls));
    }

    @Override
    public Object getId() {
        return item;
    }
}