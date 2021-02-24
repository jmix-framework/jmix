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

package io.jmix.hibernate.impl.topology;

import java.util.*;

public class DirectedGraph<T> implements Iterable<T> {

    // key is a Node, value is a set of Nodes connected by outgoing edges from the key
    private final Map<T, Set<T>> graph = new HashMap<T, Set<T>>();

    public boolean addNode(T node) {
        if (graph.containsKey(node)) {
            return false;
        }

        graph.put(node, new HashSet<T>());
        return true;
    }

    public void addNodes(Collection<T> nodes) {
        for (T node : nodes) {
            addNode(node);
        }
    }

    public void addEdge(T src, T dest) {
        if (!graph.containsKey(dest)) {
            graph.put(dest, new HashSet<T>());
        }

        if (!graph.containsKey(src)) {
            graph.put(src, new HashSet<T>());
        }

        validateSourceAndDestinationNodes(src, dest);

        // Add the edge by adding the dest node into the outgoing edges
        graph.get(src).add(dest);
    }

    public void removeEdge(T src, T dest) {
        validateSourceAndDestinationNodes(src, dest);

        graph.get(src).remove(dest);
    }

    public boolean edgeExists(T src, T dest) {
        validateSourceAndDestinationNodes(src, dest);

        return graph.get(src).contains(dest);
    }

    public Set<T> edgeFrom(T node) {
        // Check that the node exists.
        Set<T> edges = graph.get(node);
        if (edges == null)
            throw new NoSuchElementException("Source node does not exist.");

        return Collections.unmodifiableSet(edges);
    }

    public Iterator<T> iterator() {
        return graph.keySet().iterator();
    }

    public int size() {
        return graph.size();
    }

    public boolean isEmpty() {
        return graph.isEmpty();
    }

    private void validateSourceAndDestinationNodes(T src, T dest) {
        // Confirm both endpoints exist
        if (!graph.containsKey(src) || !graph.containsKey(dest))
            throw new NoSuchElementException("Both nodes must be in the graph.");
    }

}
