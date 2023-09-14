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

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.KeyValueCollectionLoader;
import io.jmix.gridexportflowui.GridExportProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Class is used by {@link io.jmix.gridexportflowui.action.ExportAction} for exporting all records from the database.
 */
@Component("grdexp_AllRecordsExporter")
public class AllRecordsExporter {

    protected static String LAST_LOADED_PK_CONDITION_PARAMETER_NAME = "lastLoadedPkValue";

    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public AllRecordsExporter(MetadataTools metadataTools,
                              DataManager dataManager,
                              PlatformTransactionManager platformTransactionManager,
                              GridExportProperties gridExportProperties) {
        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
        this.platformTransactionManager = platformTransactionManager;
        this.gridExportProperties = gridExportProperties;
    }

    /**
     * Method loads all entity instances associated with the given {@code dataUnit} and calls the
     * {@code entityExporter} to export each loaded entity instance. Creation of the output file object is the
     * responsibility of the function. Data is loaded in batches, the batch size is configured by the
     * {@link GridExportProperties#getExportAllBatchSize()}.
     *
     * @param dataUnit        data unit linked with the data
     * @param entityExporter function that is being applied to each loaded instance
     */
    public void exportAll(DataUnit dataUnit, EntityExporter entityExporter) {
        Preconditions.checkNotNullArgument(entityExporter, "Cannot export all rows. EntityExporter can't be null");

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

    protected void exportEntities(CollectionLoader<?> collectionLoader, EntityExporter entityExporter, int loadBatchSize) {
        int rowNumber = 0;
        boolean initialLoading = true;
        Object lastLoadedPkValue = null;
        boolean proceedToExport = true;
        boolean lastBatchLoaded = false;

        while (!lastBatchLoaded && proceedToExport) {
            LoadContext<?> loadContext = generateLoadContext(collectionLoader);
            //query is not null - checked when generated load context
            LoadContext.Query query = Objects.requireNonNull(loadContext.getQuery());

            if (initialLoading) {
                initialLoading = false;
            } else {
                query.setParameter(LAST_LOADED_PK_CONDITION_PARAMETER_NAME, lastLoadedPkValue);
            }
            query.setMaxResults(loadBatchSize);

            List<?> entities = dataManager.loadList(loadContext);
            for (Object entity : entities) {
                proceedToExport = entityExporter.export(entity, ++rowNumber);
                if (!proceedToExport) {
                    break;
                }
            }

            int loadedEntitiesAmount = entities.size();
            if (loadedEntitiesAmount > 0) {
                Object lastEntity = entities.get(loadedEntitiesAmount - 1);
                lastLoadedPkValue = Id.of(lastEntity).getValue();
            }
            lastBatchLoaded = loadedEntitiesAmount == 0 || loadedEntitiesAmount < loadBatchSize;
        }
    }

    @SuppressWarnings("rawtypes")
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

        //sort data by primary key. Next batch is loaded using the condition that compares the last primary key value
        //from the previous batch. In some databases (for example, PostgreSQL) it's faster than paging using firstResult
        String primaryKeyName = metadataTools.getPrimaryKeyName(entityMetaClass);
        if (primaryKeyName == null) {
            throw new IllegalStateException("Cannot find a primary key for a meta class " + entityMetaClass.getName());
        }
        query.setSort(Sort.by(primaryKeyName));

        Condition condition = loadContext.getQuery().getCondition();

        LogicalCondition wrappingCondition = new LogicalCondition(LogicalCondition.Type.AND);
        //noinspection ConstantValue
        if (condition != null) {
            //in case there is no filter on the screen a condition in the query may be null
            wrappingCondition.add(condition);
        }

        PropertyCondition lastPkCondition = PropertyCondition.createWithParameterName(primaryKeyName,
                PropertyCondition.Operation.GREATER, LAST_LOADED_PK_CONDITION_PARAMETER_NAME);
        wrappingCondition.add(lastPkCondition);
        query.setCondition(wrappingCondition);
        query.setFirstResult(0);

        return loadContext;
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
                proceedToExport = entityExporter.export(keyValueEntity, ++rowNumber);
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

