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


import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.data.DataItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Chart data item, which contains an instance of an entity.
 */
public class EntityDataItem implements DataItem, DataItem.HasId {

    private static final long serialVersionUID = -2703129637028051748L;

    protected final Entity item;

    public EntityDataItem(Entity item) {
        this.item = item;
    }

    /**
     * Returns the value of entity property with the specified property name.
     *
     * @param property name of entity property
     * @return the value of entity property with the specified property name.
     * <ul>
     * <li>If property value is an instance of {@link Collection},
     * then method returns {@link List} of {@link EntityDataItem}.</li>
     * <li>Otherwise method returns value by {@link EntityValues#getValueEx(Object, String)}</li>
     * </ul>
     */
    @Override
    public Object getValue(String property) {
        Object value = EntityValues.getValueEx(item, property);
        if (value instanceof Collection) {
            List<DataItem> items = new ArrayList<>();

            for (Object item : (Collection) value) {
                items.add(new EntityDataItem((Entity) item));
            }
            return items;
        }
        return value;
    }

    /**
     * @return contained entity
     */
    public Entity getItem() {
        return item;
    }

    @Override
    public Object getId() {
        return EntityValues.getId(item);
    }
}