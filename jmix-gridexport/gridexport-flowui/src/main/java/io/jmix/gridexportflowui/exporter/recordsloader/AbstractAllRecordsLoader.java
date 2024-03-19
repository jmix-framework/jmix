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

package io.jmix.gridexportflowui.exporter.recordsloader;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.model.*;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import io.jmix.gridexportflowui.exporter.EntityExporter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

public abstract class AbstractAllRecordsLoader {

    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public AbstractAllRecordsLoader(MetadataTools metadataTools,
                                    DataManager dataManager,
                                    PlatformTransactionManager platformTransactionManager,
                                    GridExportProperties gridExportProperties) {
        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
        this.platformTransactionManager = platformTransactionManager;
        this.gridExportProperties = gridExportProperties;
    }

    /**
     * Loads all entity instances associated with the given {@code dataUnit} and calls the
     * {@code entityExporter} to export each loaded entity instance. Creation of the output file object is the
     * responsibility of {@code entityExporter}. Data is loaded in batches, the batch size is configured by the
     * {@link GridExportProperties#getExportAllBatchSize()}.
     *
     * @param dataUnit       data unit linked with the data
     * @param entityExporter predicate that is applied to each loaded instance
     */
    public void exportAll(DataUnit dataUnit, EntityExporter entityExporter) {
        Preconditions.checkNotNullArgument(entityExporter,
                "Cannot export all rows. DataUnit can't be null");
        Preconditions.checkNotNullArgument(entityExporter,
                "Cannot export all rows. Entity exporter can't be null");

        DataLoader dataLoader = getDataLoader(dataUnit);
        int loadBatchSize = gridExportProperties.getExportAllBatchSize();

        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            if (dataLoader instanceof CollectionLoader<?> collectionLoader) {
                exportEntities(collectionLoader, entityExporter, loadBatchSize);
            } else if (dataLoader instanceof KeyValueCollectionLoader keyValueCollectionLoader) {
                exportKeyValueEntities(keyValueCollectionLoader, entityExporter, loadBatchSize);
            } else {
                throw new IllegalArgumentException("Cannot export all rows. Loader type is not supported.");
            }
        });
    }

    protected abstract LoadContext generateLoadContext(CollectionLoader loader);

    protected abstract void exportEntities(CollectionLoader<?> collectionLoader,
                                            EntityExporter entityExporter,
                                            int loadBatchSize);

    protected DataLoader getDataLoader(DataUnit dataUnit) {
        if (!(dataUnit instanceof ContainerDataUnit<?> containerDataUnit)) {
            throw new IllegalArgumentException("Cannot get data loader. DataUnit must be an instance of ContainerDataUnit.");
        }
        CollectionContainer<?> collectionContainer = containerDataUnit.getContainer();
        if (!(collectionContainer instanceof HasLoader hasLoader)) {
            throw new IllegalArgumentException("Cannot data loader. Collection container must be an instance of HasLoader.");
        }
        return hasLoader.getLoader();
    }


    protected void exportKeyValueEntities(KeyValueCollectionLoader loader,
                                          EntityExporter entityExporter,
                                          int loadBatchSize) {
        int rowNumber = 0;
        boolean proceedToExport = true;
        boolean lastBatchLoaded = false;

        for (int firstResult = 0; !lastBatchLoaded && proceedToExport; firstResult += loadBatchSize) {
            ValueLoadContext loadContext = loader.createLoadContext();
            ValueLoadContext.Query query = getValueLoadContextQuery(loadContext);

            query.setFirstResult(firstResult);
            query.setMaxResults(loadBatchSize);

            List<KeyValueEntity> keyValueEntities = dataManager.loadValues(loadContext);
            for (KeyValueEntity keyValueEntity : keyValueEntities) {
                EntityExportContext entityExportContext = new EntityExportContext(keyValueEntity, ++rowNumber);
                proceedToExport = entityExporter.createRecordFromEntity(entityExportContext);
                if (!proceedToExport) {
                    break;
                }
            }
            int loadedEntitiesAmount = keyValueEntities.size();
            lastBatchLoaded = loadedEntitiesAmount == 0 || loadedEntitiesAmount < loadBatchSize;
        }
    }

    protected ValueLoadContext.Query getValueLoadContextQuery(ValueLoadContext valueLoadContext) {
        ValueLoadContext.Query query = valueLoadContext.getQuery();
        //noinspection ConstantValue
        if (query == null) {
            throw new IllegalArgumentException("Query in ValueLoadContext must not be null.");
        }
        return query;
    }
}

