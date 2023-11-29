/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view.index;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data structure that stores a collection of {@link ViewIndexEntry}. This data structure may be used when you need to
 * grant access to multiple logically grouped views (e.g. all screens related to generic filter of BPMN modeler) in case
 * of using a simple-security add-on.
 */
public record ViewIndexEntryGroup(Collection<ViewIndexEntry> viewIndexEntries) {
    public Set<String> viewIds() {
        return viewIndexEntries.stream()
                .map(ViewIndexEntry::viewId)
                .collect(Collectors.toSet());
    }
}
