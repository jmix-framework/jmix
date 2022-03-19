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

import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;

import java.util.*;

public class HierarchicalDatasourceImpl<T extends Entity, K>
        extends CollectionDatasourceImpl<T, K>
        implements HierarchicalDatasource<T, K> {

    protected String hierarchyPropertyName;

    @Override
    public String getHierarchyPropertyName() {
        return hierarchyPropertyName;
    }

    @Override
    public void setHierarchyPropertyName(String hierarchyPropertyName) {
        this.hierarchyPropertyName = hierarchyPropertyName;
    }

    @Override
    public Collection<K> getChildren(K itemId) {
        if (hierarchyPropertyName != null) {
            final Entity currentItem = getItem(itemId);
            if (currentItem == null)
                return Collections.emptyList();

            List<K> res = new ArrayList<>();

            Collection<K> ids = getItemIds();
            for (K id : ids) {
                Entity item = getItemNN(id);
                Entity parentItem = EntityValues.getValue(item, hierarchyPropertyName);
                if (parentItem != null && EntityValues.getId(parentItem).equals(itemId))
                    res.add((K) EntityValues.getId(item));
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
                Entity parentItem = EntityValues.getValue(item, hierarchyPropertyName);
                return parentItem == null ? null : (K) EntityValues.getId(parentItem);
            }
        }
        return null;
    }

    @Override
    public Collection<K> getRootItemIds() {
        Collection<K> ids = getItemIds();

        if (hierarchyPropertyName != null) {
            Set<K> result = new LinkedHashSet<>();
            for (K id : ids) {
                Entity item = getItemNN(id);
                Entity parentItem = EntityValues.getValue(item, hierarchyPropertyName);
                if (parentItem == null || !containsItem((K) EntityValues.getId(parentItem)))
                    result.add((K) EntityValues.getId(item));
            }
            return result;
        } else {
            return new LinkedHashSet<>(ids);
        }
    }

    @Override
    public boolean isRoot(K itemId) {
        Entity item = getItem(itemId);
        if (item == null) return false;

        if (hierarchyPropertyName != null) {
            Entity parentItem = EntityValues.getValue(item, hierarchyPropertyName);
            return (parentItem == null || !containsItem((K) EntityValues.getId(parentItem)));
        } else {
            return true;
        }
    }

    @Override
    public boolean hasChildren(K itemId) {
        final Entity currentItem = getItem(itemId);
        if (currentItem == null)
            return false;

        if (hierarchyPropertyName != null) {
            Collection<K> ids = getItemIds();
            for (K id : ids) {
                Entity item = getItemNN(id);
                Entity parentItem = EntityValues.getValue(item, hierarchyPropertyName);
                if (parentItem != null && EntityValues.getId(parentItem).equals(itemId))
                    return true;
            }
        }

        return false;
    }
}
