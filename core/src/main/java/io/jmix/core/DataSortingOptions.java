/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core;

/**
 * Defines sorting options of a data store.
 */
public interface DataSortingOptions {

    /**
     * @return default sort order of null values
     */
    boolean isNullsLastSorting();

    /**
     * @return true if the data store supports equality check and sorting for LOB columns
     */
    boolean supportsLobSortingAndFiltering();
}
