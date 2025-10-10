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
 * A functional interface for resolving subfields by the field information
 */
@FunctionalInterface
public interface VirtualSubfieldsProvider {

    Set<String> getSubfields(FieldInfo fieldInfo);

    /**
     * Describes search field information for subfields generating.
     *
     * @param indexConfiguration TODO Pavel Aleksandrov
     * @param fieldName
     */
    record FieldInfo(IndexConfiguration indexConfiguration, String fieldName) {
    }
}
