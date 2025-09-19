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

package io.jmix.gridexportui.exporter.entitiesloader;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.gridexportui.GridExportProperties;
import io.jmix.gridexportui.exporter.EntityExportContext;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for the all entities loader which is used to export to other data formats such as excel or json
 */
public abstract class AbstractAllEntitiesLoader implements AllEntitiesLoader {

    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public AbstractAllEntitiesLoader(MetadataTools metadataTools,
                                     DataManager dataManager,
                                     PlatformTransactionManager platformTransactionManager,
                                     GridExportProperties gridExportProperties) {

        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
        this.platformTransactionManager = platformTransactionManager;
        this.gridExportProperties = gridExportProperties;
    }

    /**
     * @deprecated Use {@link #generateLoadContext(CollectionLoader)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.7")
    @SuppressWarnings("rawtypes")
    protected LoadContext generateLoadContext(DataUnit dataUnit, @Nullable Sort sort) {
        DataLoader dataLoader = getDataLoader(dataUnit);
        return generateLoadContext(((CollectionLoader) dataLoader));
    }

    @SuppressWarnings("rawtypes")
    protected abstract LoadContext generateLoadContext(CollectionLoader dataUnit);

    @Override
    public void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor, @Nullable Sort sort) {
        loadAll(dataUnit, exportedEntityVisitor);
    }

    /**
     * Loads all entity instances associated with the given {@code dataUnit} and calls the
     * {@code entityExporter} to export each loaded entity instance. Creation of the output file object is the
     * responsibility of {@code entityExporter}. Data is loaded in batches, the batch size is configured by the
     * {@link GridExportProperties#getExportAllBatchSize()}.
     *
     * @param dataUnit              data unit linked with the data
     * @param exportedEntityVisitor visitor which exports an entity to the appropriate format
     */
    public void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor) {
        Preconditions.checkNotNullArgument(exportedEntityVisitor,
                "Cannot export all rows. DataUnit can't be null");
        Preconditions.checkNotNullArgument(exportedEntityVisitor,
                "Cannot export all rows. Entity exporter can't be null");

        DataLoader dataLoader = getDataLoader(dataUnit);
        int loadBatchSize = gridExportProperties.getExportAllBatchSize();

        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            if (dataLoader instanceof CollectionLoader<?>) {
                loadEntities(((CollectionLoader<?>) dataLoader), exportedEntityVisitor, loadBatchSize);
            } else if (dataLoader instanceof KeyValueCollectionLoader) {
                loadKeyValueEntities(((KeyValueCollectionLoader) dataLoader), exportedEntityVisitor, loadBatchSize);
            } else {
                throw new IllegalArgumentException("Cannot export all rows. Loader type is not supported.");
            }
        });
    }

    protected abstract void loadEntities(CollectionLoader<?> collectionLoader,
                                         ExportedEntityVisitor exportedEntityVisitor,
                                         int loadBatchSize);

    protected DataLoader getDataLoader(DataUnit dataUnit) {
        if (!(dataUnit instanceof ContainerDataUnit<?>)) {
            throw new IllegalArgumentException(
                    String.format("Cannot get data loader. %s must be an instance of %s.",
                            DataUnit.class.getSimpleName(), dataUnit.getClass().getSimpleName())
            );
        }

        CollectionContainer<?> collectionContainer = ((ContainerDataUnit<?>) dataUnit).getContainer();
        if (!(collectionContainer instanceof HasLoader)) {
            throw new IllegalArgumentException(
                    String.format("Cannot data loader. %s must be an instance of %s.",
                            CollectionContainer.class.getSimpleName(), HasLoader.class.getSimpleName())
            );
        }

        return ((HasLoader) collectionContainer).getLoader();
    }

    protected void loadKeyValueEntities(KeyValueCollectionLoader loader,
                                        ExportedEntityVisitor exportedEntityVisitor,
                                        int loadBatchSize) {
        int rowNumber = 0;
        boolean proceedToExport = true;
        boolean lastBatchLoaded = false;

        for (int firstResult = 0; !lastBatchLoaded && proceedToExport; firstResult += loadBatchSize) {
            ValueLoadContext loadContext = loader.createLoadContext();
            ValueLoadContext.Query query = getValueLoadContextQuery(loadContext);

            query.setFirstResult(firstResult);
            query.setMaxResults(loadBatchSize);

            List<KeyValueEntity> keyValueEntities = loader.getDelegate() == null
                    ? dataManager.loadValues(loadContext)
                    : loader.getDelegate().apply(loadContext);
            for (KeyValueEntity keyValueEntity : keyValueEntities) {
                EntityExportContext entityExportContext = new EntityExportContext(keyValueEntity, ++rowNumber);
                proceedToExport = exportedEntityVisitor.visitEntity(entityExportContext);
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
