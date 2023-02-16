/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowuiexport.exporter;

public enum ExportMode {

    /**
     * Export all records in the database
     */
    ALL_ROWS,

    /**
     * Export only records loaded to the current page of a table or a data grid
     */
    CURRENT_PAGE,

    /**
     * Export only records selected in a table or a data grid
     */
    SELECTED_ROWS
}
