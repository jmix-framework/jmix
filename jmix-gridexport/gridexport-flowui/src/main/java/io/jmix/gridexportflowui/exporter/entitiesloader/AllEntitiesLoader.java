/*
 * Copyright 2024 Haulmont.
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

package io.jmix.gridexportflowui.exporter.entitiesloader;

import io.jmix.flowui.data.DataUnit;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.EntityExportContext;

/**
 * This interface should be implemented by any bean which loads entities for json or excel export.
 */
public interface AllEntitiesLoader {

    /**
     * Visitor is passed to {@link AllEntitiesLoader} to export loaded entity
     */
    interface ExportedEntityVisitor {

        /**
         * Export entity to an appropriate format (json, excel)
         * @param entityExportContext loaded entity
         * @return false if entity cannot be exported
         */
        boolean visitEntity(EntityExportContext entityExportContext);
    }

    /**
     * Type of data loading strategy defined as string constant.
     * {@link AllEntitiesLoaderFactory#getEntitiesLoader()} returns loader which pagination strategy equals to
     * {@link GridExportProperties#getExportAllPaginationStrategy()}
     */
    String getPaginationStrategy();

    /**
     * Load entities and export each entity using the {@link ExportedEntityVisitor}
     */
    void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor);
}
