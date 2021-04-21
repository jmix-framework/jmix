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

package io.jmix.core.impl;

import io.jmix.core.JmixModuleDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sorts {@link JmixModuleDescriptor}s using the topological sort algorithm.
 */
public class JmixModulesSorter {

    /**
     * Returns a list of {@code JmixModuleDescriptor}s sorted with the topological sort algorithm.
     */
    public static List<JmixModuleDescriptor> sort(List<JmixModuleDescriptor> sourceList) {
        List<JmixModuleDescriptor> resultList = new ArrayList<>();
        Set<JmixModuleDescriptor> visited = new HashSet<>();

        for (JmixModuleDescriptor descriptor : sourceList) {
            recursiveSort(descriptor, visited, resultList);
        }

        return resultList;
    }

    private static void recursiveSort(JmixModuleDescriptor descriptor,
                                      Set<JmixModuleDescriptor> visited,
                                      List<JmixModuleDescriptor> resultList) {
        if (visited.contains(descriptor)) {
            return;
        }
        visited.add(descriptor);

        for (JmixModuleDescriptor dependency : descriptor.getDependencies()) {
            recursiveSort(dependency, visited, resultList);
        }
        resultList.add(descriptor);
    }
}
