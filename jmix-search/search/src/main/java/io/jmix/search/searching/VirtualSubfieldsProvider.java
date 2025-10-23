/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.searching;

import io.jmix.search.index.IndexConfiguration;

import java.util.Set;

/**
 * A functional interface for providing logic of virtual subfields resolving by the field information.
 */
@FunctionalInterface
public interface VirtualSubfieldsProvider {

    Set<String> getSubfields(FieldInfo fieldInfo);

    /**
     * Represents information about a specific index field.
     * This record holds details about a field that is part of a particular index configuration.
     * It is used in the context of index searching and virtual subfield resolution.
     *
     * @param indexConfiguration configuration of the index to which the field belongs
     * @param fieldName name of the field within the index
     */
    record FieldInfo(IndexConfiguration indexConfiguration, String fieldName) {
    }
}
