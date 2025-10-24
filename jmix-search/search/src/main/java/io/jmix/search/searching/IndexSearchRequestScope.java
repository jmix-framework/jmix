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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents the scope of an index search request, encapsulating the index configuration and a set of fields
 * that is able for searching.
 * This record is primarily used to define the boundaries of a search request within a particular index.
 *
 * @param indexConfiguration the configuration of the index relevant to the search request
 * @param fields the set of fields that are included in the scope of the search request
 */
public record IndexSearchRequestScope(IndexConfiguration indexConfiguration, Set<String> fields) {

    /**
     * Returns the list of fields that are included in the scope of the search request.
     *
     * @return a list of field names available for searching within the current scope
     */
    public List<String> getFieldList(){
        return new ArrayList<>(fields);
    }
}
