/*
 * Copyright 2022 Haulmont.
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

package io.jmix.gridexportui.exporter.entitiesloader;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.gridexportui.GridExportProperties;
import io.jmix.gridexportui.exporter.EntityExportContext;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * This loader implements limit-offset pagination strategy. Entities are fetched in the same order as in the data store.
 * The strategy uses two parameters. The first, offset, sets a number of the starting record of the page.
 * The second, limit, defines the number of records in the page.
 */
@Component
public class LimitOffsetAllEntitiesLoader extends AbstractAllEntitiesLoader {

    public static final String PAGINATION_STRATEGY = "limit-offset";

    public LimitOffsetAllEntitiesLoader(MetadataTools metadataTools, DataManager dataManager,
                                        PlatformTransactionManager platformTransactionManager,
                                        GridExportProperties gridExportProperties) {
        super(metadataTools, dataManager, platformTransactionManager, gridExportProperties);
    }

    @Override
    public String getPaginationStrategy() {
        return PAGINATION_STRATEGY;
    }

    /**
     * Generates the load context using the given {@code DataUnit}.
     *
     * @param dataUnit data unit linked with the data
     * @param sort An optional sorting specification for the data.
     *             If {@code null} sorting will be applied by the primary key.
     */
    @SuppressWarnings("rawtypes")
    public LoadContext generateLoadContext(DataUnit dataUnit, @Nullable Sort sort) {
        if (!(dataUnit instanceof ContainerDataUnit)) {
            throw new RuntimeException("Cannot export all rows. DataUnit must be an instance of ContainerDataUnit.");
        }
        CollectionContainer collectionContainer = ((ContainerDataUnit) dataUnit).getContainer();
        if (!(collectionContainer instanceof HasLoader)) {
            throw new RuntimeException("Cannot export all rows. Collection container must be an instance of HasLoader.");
        }

        DataLoader dataLoader = ((HasLoader) collectionContainer).getLoader();
        if (!(dataLoader instanceof CollectionLoader)) {
            throw new RuntimeException("Cannot export all rows. Data loader must be an instance of CollectionLoader.");
        }

        LoadContext loadContext = ((CollectionLoader) dataLoader).createLoadContext();
        LoadContext.Query query = loadContext.getQuery();
        if (query == null) {
            throw new RuntimeException("Cannot export all rows. Query in LoadContext is null.");
        }

        return loadContext;
    }

    /**
     * Sequential data loading
     * @param exportedEntityVisitor {@link ExportedEntityVisitor#visitEntity(EntityExportContext)}
     */
    @Override
    public void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor, @Nullable Sort sort) {
        int loadBatchSize = gridExportProperties.getExportAllBatchSize();
        int rowNumber = 0;
        int firstResultNumber = 0;
        boolean proceedToExport = true;
        boolean lastBatchLoaded = false;

        while (!lastBatchLoaded && proceedToExport) {
            LoadContext<?> loadContext = generateLoadContext(dataUnit, sort);
            //query is not null - checked when generated load context
            LoadContext.Query query = Objects.requireNonNull(loadContext.getQuery());
            query.setFirstResult(firstResultNumber);
            query.setMaxResults(loadBatchSize);

            List<?> entities = dataManager.loadList(loadContext);
            for (Object entity : entities) {
                EntityExportContext entityExportContext = new EntityExportContext(entity, ++rowNumber);
                proceedToExport = exportedEntityVisitor.visitEntity(entityExportContext);
                if (!proceedToExport) {
                    break;
                }
            }

            firstResultNumber += loadBatchSize;

            int loadedEntitiesAmount = entities.size();
            lastBatchLoaded = loadedEntitiesAmount == 0 || loadedEntitiesAmount < loadBatchSize;
        }
    }
}
