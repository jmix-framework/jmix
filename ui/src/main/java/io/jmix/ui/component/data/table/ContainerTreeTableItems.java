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

package io.jmix.ui.component.data.table;

import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.component.data.TreeTableItems;
import io.jmix.ui.model.CollectionContainer;

import java.util.*;

public class ContainerTreeTableItems<E extends Entity>
            extends ContainerTableItems<E>
            implements TreeTableItems<E> {

    private final String hierarchyProperty;

    public ContainerTreeTableItems(CollectionContainer<E> container, String hierarchyProperty) {
        super(container);
        this.hierarchyProperty = hierarchyProperty;
    }

    @Override
    public String getHierarchyPropertyName() {
        return hierarchyProperty;
    }

    @Override
    public Collection<?> getRootItemIds() {
        Collection<?> ids = getItemIds();

        if (hierarchyProperty != null) {
            Set<Object> result = new LinkedHashSet<>();
            for (Object id : ids) {
                Entity item = getItemNN(id);
                Entity parentItem = EntityValues.getValue(item, hierarchyProperty);
                if (parentItem == null || (container.getItemOrNull(EntityValues.getId(parentItem)) == null))
                    result.add(EntityValues.getId(item));
            }
            return result;
        } else {
            return new LinkedHashSet<>(ids);
        }
    }

    @Override
    public Object getParent(Object itemId) {
        if (hierarchyProperty != null) {
            Entity item = getItem(itemId);
            if (item == null)
                return null;
            else {
                Entity parentItem = EntityValues.getValue(item, hierarchyProperty);
                return parentItem == null ? null : EntityValues.getId(parentItem);
            }
        }
        return null;
    }

    @Override
    public Collection<?> getChildren(Object itemId) {
        if (hierarchyProperty != null) {
            Entity currentItem = getItem(itemId);
            if (currentItem == null)
                return Collections.emptyList();

            List<Object> res = new ArrayList<>();

            Collection ids = getItemIds();
            for (Object id : ids) {
                Entity item = getItemNN(id);
                Entity parentItem = EntityValues.getValue(item, hierarchyProperty);
                if (parentItem != null && EntityValues.getId(parentItem).equals(itemId))
                    res.add(EntityValues.getId(item));
            }

            return res;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isRoot(Object itemId) {
        Entity item = getItem(itemId);
        if (item == null) return false;

        if (hierarchyProperty != null) {
            Entity parentItem = EntityValues.getValue(item, hierarchyProperty);
            return (parentItem == null || (container.getItemOrNull(EntityValues.getId(parentItem)) == null));
        } else {
            return true;
        }
    }

    @Override
    public boolean hasChildren(Object itemId) {
        Entity currentItem = getItem(itemId);
        if (currentItem == null)
            return false;

        if (hierarchyProperty != null) {
            Collection ids = getItemIds();
            for (Object id : ids) {
                Entity item = getItemNN(id);
                Entity parentItem = EntityValues.getValue(item, hierarchyProperty);
                if (parentItem != null && EntityValues.getId(parentItem).equals(itemId))
                    return true;
            }
        }

        return false;
    }

    @Override
    public Object firstItemId() {
        Collection<?> rootItemIds = getRootItemIds();
        return rootItemIds.isEmpty() ? null : rootItemIds.iterator().next();
    }
}
