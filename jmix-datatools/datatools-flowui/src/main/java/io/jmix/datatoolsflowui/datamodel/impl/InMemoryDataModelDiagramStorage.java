/*
 * Copyright 2026 Haulmont.
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

package io.jmix.datatoolsflowui.datamodel.impl;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import io.jmix.datatoolsflowui.datamodel.DataModelDiagramStorage;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDataModelDiagramStorage implements DataModelDiagramStorage {

    protected Map<UUID, byte[]> diagrams = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public byte[] get(UUID id) {
        return diagrams.get(id);
    }

    @Override
    public void put(UUID id, byte[] diagramData) {
        diagrams.put(id, diagramData);
    }

    @Override
    public boolean remove(UUID id) {
        return diagrams.remove(id) != null;
    }

    @Override
    public void clear() {
        diagrams.clear();
    }
}
