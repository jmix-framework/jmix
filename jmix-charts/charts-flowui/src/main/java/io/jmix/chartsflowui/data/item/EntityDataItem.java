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
import io.jmix.core.entity.EntityValues;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chart data item, which contains an instance of an entity.
 */
public class EntityDataItem implements DataItem {

    protected final Object item;

    public EntityDataItem(Object item) {
        this.item = item;
    }

    /**
     * @param property name of entity property
     * @return the value of entity property with the specified property name
     * <ul>
     * <li>If property value is an instance of {@link Collection},
     * then method returns {@link List} of {@link EntityDataItem}.</li>
     * <li>Otherwise method returns value by {@link EntityValues#getValueEx(Object, String)}</li>
     * </ul>
     */
    @Override
    public Object getValue(String property) {
        Object value = EntityValues.getValueEx(item, property);

        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(EntityDataItem::new)
                    .collect(Collectors.toList());
        }

        return value;
    }

    /**
     * @return contained entity
     */
    public Object getItem() {
        return item;
    }

    @Override
    public Object getId() {
        return EntityValues.getId(item);
    }
}
