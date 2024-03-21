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

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

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

    @Override
    protected LoadContext generateLoadContext(CollectionLoader loader) {
        LoadContext loadContext = loader.createLoadContext();
        LoadContext.Query query = loadContext.getQuery();
        if (query == null) {
            throw new IllegalArgumentException("Cannot export all rows. Query in LoadContext is null.");
        }

        MetaClass entityMetaClass = loadContext.getEntityMetaClass();
        if (metadataTools.hasCompositePrimaryKey(entityMetaClass)) {
            throw new IllegalArgumentException(
                    "Cannot export all rows. Exporting of entities with composite key is not supported.");
        }

        return loadContext;
    }

    /**
     * Sequential data loading
     * @param exportedEntityVisitor {@link ExportedEntityVisitor#visitEntity(EntityExportContext)}
     * @param loadBatchSize {@link GridExportProperties#getExportAllBatchSize()} number of entities loaded in one query
     */
    @Override
    protected void loadEntities(CollectionLoader<?> collectionLoader, ExportedEntityVisitor exportedEntityVisitor,
                                int loadBatchSize) {
        int rowNumber = 0;
        int firstResultNumber = 0;
        boolean proceedToExport = true;
        boolean lastBatchLoaded = false;

        while (!lastBatchLoaded && proceedToExport) {
            LoadContext<?> loadContext = generateLoadContext(collectionLoader);
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
