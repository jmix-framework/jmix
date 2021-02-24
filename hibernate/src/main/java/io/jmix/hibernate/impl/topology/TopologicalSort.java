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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopologicalSort {

    public static <T> List<T> sort(DirectedGraph<T> graph) {
        DirectedGraph<T> reversedGraph = reverseGraph(graph);

        List<T> result = new ArrayList<T>();
        Set<T> visited = new HashSet<T>();

        /* We'll also maintain a third set consisting of all nodes that have
         * been fully expanded.  If the graph contains a cycle, then we can
         * detect this by noting that a node has been explored but not fully
         * expanded.
         */
        Set<T> expanded = new HashSet<T>();

        // Fire off a Depth-First Search from each node in the graph
        for (T node : reversedGraph)
            explore(node, reversedGraph, result, visited, expanded);

        return result;
    }


    /**
     * Recursively performs a Depth-First Search from the specified node, marking all nodes
     * encountered by the search.
     *
     * @param node     The node to begin the search from.
     * @param graph    The graph in which to perform the search.
     * @param result   A list holding the topological sort of the graph.
     * @param visited  A set of nodes that have already been visited.
     * @param expanded A set of nodes that have been fully expanded.
     */
    private static <T> void explore(T node, DirectedGraph<T> graph, List<T> result, Set<T> visited, Set<T> expanded) {
        if (visited.contains(node)) {
            // if this node has already been expanded, then it's already been assigned a
            // position in the final topological sort and we don't need to explore it again.
            if (expanded.contains(node)) return;

            // if it hasn't been expanded, it means that we've just found a node that is currently being explored,
            // and therefore is part of a cycle.  In that case, we should report an error.
            throw new IllegalArgumentException("A cycle was detected within the Graph when exploring node " + node.toString());
        }

        visited.add(node);

        // recursively explore all predecessors of this node
        for (T predecessor : graph.edgeFrom(node))
            explore(predecessor, graph, result, visited, expanded);

        result.add(node);
        expanded.add(node);
    }

    private static <T> DirectedGraph<T> reverseGraph(DirectedGraph<T> graph) {
        DirectedGraph<T> result = new DirectedGraph<T>();

        // Add all the nodes from the original graph
        for (T node : graph) {
            result.addNode(node);
        }

        // Scan over all the edges in the graph, adding their reverse to the reverse graph.
        for (T node : graph) {
            for (T endpoint : graph.edgeFrom(node)) {
                result.addEdge(endpoint, node);
            }
        }

        return result;
    }

}
