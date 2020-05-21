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
import io.jmix.core.common.datastruct.Node;
import io.jmix.core.common.datastruct.Tree;
import io.jmix.core.entity.EntityValues;

import java.util.*;

/**
 * @param <T> Entity
 * @param <K> Key
 */
@Deprecated
public abstract class AbstractTreeDatasource<T extends Entity, K>
        extends CollectionDatasourceImpl<T, K>
        implements HierarchicalDatasource<T, K> {

    protected Tree<T> tree;
    protected Map<K, Node<T>> nodes;

    @Override
    protected void loadData(Map<String, Object> params) {
        clear();

        this.tree = loadTree(params);

        Map<K, Node<T>> targetNodes = new HashMap<>();
        if (tree != null) {
            for (Node<T> node : tree.toList()) {
                final T entity = node.getData();
                final K id = (K) EntityValues.getId(entity);

                data.put(id, entity);
                attachListener(entity);

                targetNodes.put(id, node);
            }
        }

        this.nodes = targetNodes;
    }

    protected abstract Tree<T> loadTree(Map<String, Object> params);

    @Override
    public String getHierarchyPropertyName() {
        return null;
    }

    @Override
    public void setHierarchyPropertyName(String parentPropertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<K> getRootItemIds() {
        if (state == State.NOT_INITIALIZED) {
            return Collections.emptyList();
        } else {
            if (tree == null) {
                return Collections.emptyList();
            }

            List ids = new ArrayList();
            for (Node<T> rootNode : tree.getRootNodes()) {
                ids.add(EntityValues.<K>getId(rootNode.getData()));
            }
            return (Collection<K>) Collections.unmodifiableCollection(ids);
        }
    }

    @Override
    public K getParent(K itemId) {
        if (nodes == null || tree == null) {
            return null;
        }

        final Node<T> node = nodes.get(itemId);
        return node == null ? null : node.getParent() == null ? null : (K) EntityValues.getId(node.getParent().getData());
    }

    @Override
    public Collection<K> getChildren(K itemId) {
        if (nodes == null || tree == null) {
            return Collections.emptyList();
        }

        final Node<T> node = nodes.get(itemId);
        if (node == null) {
            return Collections.emptyList();
        } else {
            final List<Node<T>> children = node.getChildren();

            final List<K> ids = new ArrayList<>();
            for (Node<T> targetNode : children) {
                ids.add((K) EntityValues.getId(targetNode.getData()));
            }

            return ids;
        }
    }

    @Override
    public boolean isRoot(K itemId) {
        if (nodes == null || tree == null) {
            return false;
        }

        final Node<T> node = nodes.get(itemId);

        for (Node<T> tNode : tree.getRootNodes()) {
            if (Objects.equals(tNode, node)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void clear() {
        backgroundWorker.checkUIAccess();

        // replaced refresh call with state initialization
        if (state != State.VALID) {
            invalidate();

            State prevState = state;
            if (!prevState.equals(State.VALID)) {
                valid();
                fireStateChanged(prevState);
            }
        }

        // Get items
        List<Object> collectionItems = new ArrayList<>(data.values());
        // Clear container
        data.clear();

        tree = null;
        nodes = null;

        // Notify listeners
        for (Object obj : collectionItems) {
            T item = (T) obj;
            detachListener(item);
        }

        setItem(null);

        fireCollectionChanged(Operation.CLEAR, Collections.emptyList());
    }

    @Override
    public boolean hasChildren(K itemId) {
        if (nodes == null || tree == null) {
            return false;
        }

        final Node<T> node = nodes.get(itemId);
        return node != null && !node.getChildren().isEmpty();
    }
}
