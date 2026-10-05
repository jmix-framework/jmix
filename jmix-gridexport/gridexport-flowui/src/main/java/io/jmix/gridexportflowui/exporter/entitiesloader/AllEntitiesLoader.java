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
import org.jspecify.annotations.NullMarked;

/**
 * This interface should be implemented by any bean which loads all entities for json or excel export.
 */
@NullMarked
public interface AllEntitiesLoader {

    /**
     * Visitor is passed to {@link AllEntitiesLoader} to export loaded entity
     */
    @NullMarked
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
     * Loads entities in batches of the size the loader chooses itself and exports each entity using
     * the {@link ExportedEntityVisitor}.
     *
     * @param dataUnit              data unit linked with the data
     * @param exportedEntityVisitor visitor which exports entity to appropriate format
     * @deprecated implement and use {@link #loadAll(DataUnit, ExportedEntityVisitor, int)} instead, which takes
     * the batch size configured for the exporter. This method will be removed and the one with the batch size
     * will become abstract.
     */
    @Deprecated(since = "3.1", forRemoval = true)
    void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor);

    /**
     * Loads entities in batches of the given size and exports each entity using the {@link ExportedEntityVisitor}.
     * A loader whose data source has no notion of batches may ignore the batch size.
     * <p>
     * The default implementation exists only for loaders written before the batch size was introduced: it ignores
     * {@code loadBatchSize} and delegates to {@link #loadAll(DataUnit, ExportedEntityVisitor)}. Override it.
     *
     * @param dataUnit              data unit linked with the data
     * @param exportedEntityVisitor visitor which exports entity to appropriate format
     * @param loadBatchSize         number of entities loaded in one query, positive
     */
    default void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor, int loadBatchSize) {
        loadAll(dataUnit, exportedEntityVisitor);
    }
}
