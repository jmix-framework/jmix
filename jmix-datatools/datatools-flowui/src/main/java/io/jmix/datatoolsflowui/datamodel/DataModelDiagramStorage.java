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

package io.jmix.datatoolsflowui.datamodel;

import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * Interface for managing the storage of data model diagrams.
 * This provides methods to store, retrieve, remove, and clear diagram data,
 * which is identified by a unique {@link UUID}.
 */
public interface DataModelDiagramStorage {

    /**
     * Retrieves the diagram data associated with the specified unique identifier.
     *
     * @param id the unique {@link UUID} identifying the diagram data to retrieve
     * @return a byte array representing the diagram data if found, or null if no
     * data is associated with the given ID
     */
    @Nullable
    byte[] get(UUID id);

    /**
     * Stores the specified diagram data associated with the provided unique identifier.
     *
     * @param id          the unique {@link UUID} used to identify the diagram data
     * @param diagramData a byte array representing the diagram data to be stored
     */
    void put(UUID id, byte[] diagramData);

    /**
     * Removes the diagram data associated with the specified unique identifier.
     *
     * @param id the unique {@link UUID} identifying the diagram data to be removed
     * @return true if the diagram data was found and removed, false otherwise
     */
    boolean remove(UUID id);

    /**
     * Removes all stored diagram data from the storage.
     */
    void clear();
}
