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

package io.jmix.gridexportflowui.exporter;

/**
 * Entity exporter which is used by {@link AllRecordsExporter} to export entities one by one
 */
public interface EntityExporter {

    /**
     * Processes export operation on given entity and entity number
     * @param entity entity to export
     * @param entityNumber sequential number of entity being processed
     * @return true if export operation has been successfully completed
     */
    boolean export(Object entity, int entityNumber);
}
