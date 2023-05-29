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
package com.haulmont.cuba.gui.data.impl;

import io.jmix.core.Entity;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import io.jmix.core.entity.EntityValues;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class HierarchicalPropertyDatasourceImpl<T extends Entity, K>
        extends CollectionPropertyDatasourceImpl<T, K>
        implements HierarchicalDatasource<T, K> {

    protected String hierarchyPropertyName;

    protected String sortPropertyName;

    @Override
    public String getHierarchyPropertyName() {
        return hierarchyPropertyName;
    }

    @Override
    public void setHierarchyPropertyName(String hierarchyPropertyName) {
        this.hierarchyPropertyName = hierarchyPropertyName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<K> getChildren(K itemId) {
        if (hierarchyPropertyName != null) {
            Entity item = getItem(itemId);
            if (item == null) {
                return Collections.emptyList();
            }

            List<K> res = new ArrayList<>();

            Collection<K> ids = getItemIds();
            for (K id : ids) {
                Entity currentItem = getItemNN(id);
                Object parentItem = EntityValues.getValue(currentItem, hierarchyPropertyName);
                if (parentItem != null && parentItem.equals(item))
                    res.add((K) EntityValues.getId(currentItem));
            }

            if (StringUtils.isNotBlank(sortPropertyName)) {
                res.sort((o1, o2) -> {
                    Entity item1 = getItemNN(o1);
                    Entity item2 = getItemNN(o2);

                    Object value1 = EntityValues.getValue(item1, sortPropertyName);
                    Object value2 = EntityValues.getValue(item2, sortPropertyName);

                    if ((value1 instanceof Comparable)
                            && (value2 instanceof Comparable)) {
                        return ((Comparable) value1).compareTo(value2);
                    }

                    return 0;
                });
            }

            return res;
        }
        return Collections.emptyList();
    }

    @Override
    public K getParent(K itemId) {
        if (hierarchyPropertyName != null) {
            Entity item = getItem(itemId);
            if (item == null)
                return null;
            else {
                Entity value = EntityValues.getValue(item, hierarchyPropertyName);
                return value == null ? null : (K) EntityValues.getId(value);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<K> getRootItemIds() {
        Collection<K> ids = getItemIds();

        if (hierarchyPropertyName != null) {
            Set<K> result = new LinkedHashSet<>();
            for (K id : ids) {
                Entity item = getItemNN(id);
                Object value = EntityValues.getValue(item, hierarchyPropertyName);
                if (value == null || !containsItem((K) EntityValues.getId(((T) value))))
                    result.add((K) EntityValues.getId(item));
            }
            return result;
        } else {
            return new LinkedHashSet<>(ids);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isRoot(K itemId) {
        Entity item = getItem(itemId);
        if (item == null) return false;

        if (hierarchyPropertyName != null) {
            Object value = EntityValues.getValue(item, hierarchyPropertyName);
            return (value == null || !containsItem((K) EntityValues.getId(((T) value))));
        } else {
            return true;
        }
    }

    @Override
    public boolean hasChildren(K itemId) {
        final Entity item = getItem(itemId);
        if (item == null) return false;

        if (hierarchyPropertyName != null) {
            for (T currentItem : getItems()) {
                Object parentItem = EntityValues.getValue(currentItem, hierarchyPropertyName);
                if (parentItem != null && parentItem.equals(item))
                    return true;
            }
        }

        return false;
    }

    /**
     * @return Property of entity which sort the nodes
     */
    public String getSortPropertyName() {
        return sortPropertyName;
    }

    /**
     * Set property of entity which sort the nodes
     *
     * @param sortPropertyName Sort property
     */
    public void setSortPropertyName(String sortPropertyName) {
        this.sortPropertyName = sortPropertyName;
    }
}
