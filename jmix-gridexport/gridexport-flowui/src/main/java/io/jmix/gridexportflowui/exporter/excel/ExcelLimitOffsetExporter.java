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

package io.jmix.gridexportflowui.exporter.excel;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.AbstractAllRecordsExporter;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ExcelLimitOffsetExporter extends AbstractAllRecordsExporter {
    public ExcelLimitOffsetExporter(MetadataTools metadataTools, DataManager dataManager,
                                    PlatformTransactionManager platformTransactionManager,
                                    GridExportProperties gridExportProperties) {
        super(metadataTools, dataManager, platformTransactionManager, gridExportProperties);
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

    @Override
    protected void exportEntities(CollectionLoader<?> collectionLoader, Predicate<EntityExportContext> entityExporter,
                                  int loadBatchSize) {
        int rowNumber = 0;
        boolean proceedToExport = true;
        boolean lastBatchLoaded = false;

        while (!lastBatchLoaded && proceedToExport) {
            LoadContext<?> loadContext = generateLoadContext(collectionLoader);
            //query is not null - checked when generated load context
            LoadContext.Query query = Objects.requireNonNull(loadContext.getQuery());
            query.setFirstResult(rowNumber);
            query.setMaxResults(loadBatchSize);

            List<?> entities = dataManager.loadList(loadContext);
            for (Object entity : entities) {
                EntityExportContext entityExportContext = new EntityExportContext(entity, ++rowNumber);
                proceedToExport = entityExporter.test(entityExportContext);
                if (!proceedToExport) {
                    break;
                }
            }

            int loadedEntitiesAmount = entities.size();
            lastBatchLoaded = loadedEntitiesAmount == 0 || loadedEntitiesAmount < loadBatchSize;
        }
    }
}
