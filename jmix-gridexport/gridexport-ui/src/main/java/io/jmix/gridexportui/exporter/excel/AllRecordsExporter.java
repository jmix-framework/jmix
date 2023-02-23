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

package io.jmix.gridexportui.exporter.excel;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.gridexportui.GridExportProperties;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class is used by {@link io.jmix.gridexportui.action.ExportAction} for exporting all records from the database.
 */
@Component("grdexp_AllRecordsExporter")
public class AllRecordsExporter {

    private MetadataTools metadataTools;

    private DataManager dataManager;

    private PlatformTransactionManager platformTransactionManager;

    private GridExportProperties gridExportProperties;

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
     * Method loads all entity instances associated with the given {@code dataUnit} and applies the
     * {@code excelRowCreator} function to each loaded entity instance. Creation of the output file row is the
     * responsibility of the function. Data is loaded in batches, the batch size is configured by the
     * {@link GridExportProperties#getExportAllBatchSize()}.
     *
     * @param dataUnit        data unit linked with the data
     * @param excelRowCreator function that is being applied to each loaded instance
     * @param excelRowChecker function that checks for exceeding the maximum number of rows in XLSX format
     */
    protected void exportAll(DataUnit dataUnit, Consumer<RowCreationContext> excelRowCreator,
                             Predicate<Integer> excelRowChecker) {
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

        MetaClass entityMetaClass = loadContext.getEntityMetaClass();
        if (metadataTools.hasCompositePrimaryKey(entityMetaClass)) {
            throw new RuntimeException("Cannot export all rows. Exporting of entities with composite key is not supported.");
        }

        //sort data by primary key. Next batch is loaded using the condition that compares the last primary key value
        //from the previous batch.
        String primaryKeyName = metadataTools.getPrimaryKeyName(entityMetaClass);
        if (primaryKeyName == null) {
            throw new RuntimeException("Cannot find a primary key for a meta class " + entityMetaClass.getName());
        }
        query.setSort(Sort.by(primaryKeyName));

        Condition condition = loadContext.getQuery().getCondition();

        LogicalCondition wrappingCondition = new LogicalCondition(LogicalCondition.Type.AND);
        if (condition != null) {
            //in case there is no filter on the screen a condition in the query may be null
            wrappingCondition.add(condition);
        }
        String lastLoadedPkConditionParameterName = "lastLoadedPkValue";
        PropertyCondition lastPkCondition = PropertyCondition.createWithParameterName(primaryKeyName,
                PropertyCondition.Operation.GREATER, lastLoadedPkConditionParameterName);
        wrappingCondition.add(lastPkCondition);
        loadContext.getQuery().setCondition(wrappingCondition);

        int loadBatchSize = gridExportProperties.getExportAllBatchSize();
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            long count = dataManager.getCount(loadContext);
            int rowNumber = 0;
            boolean initialLoading = true;
            Object lastLoadedPkValue = null;
            for (int firstResult = 0; firstResult < count && !excelRowChecker.test(rowNumber); firstResult += loadBatchSize) {
                if (initialLoading) {
                    initialLoading = false;
                } else {
                    query.setParameter(lastLoadedPkConditionParameterName, lastLoadedPkValue);
                }
                query.setMaxResults(loadBatchSize);

                List entities = dataManager.loadList(loadContext);
                for (Object entity : entities) {
                    if (excelRowChecker.test(++rowNumber)) {
                        break;
                    }

                    excelRowCreator.accept(new RowCreationContext(entity, rowNumber));
                }
                Object lastEntity = entities.get(entities.size() - 1);
                lastLoadedPkValue = Id.of(lastEntity).getValue();
            }
        });
    }

    public static class RowCreationContext {
        protected Object entity;
        protected int rowNumber;

        public RowCreationContext(Object entity, int rowNumber) {
            this.entity = entity;
            this.rowNumber = rowNumber;
        }

        public Object getEntity() {
            return entity;
        }

        public int getRowNumber() {
            return rowNumber;
        }
    }
}
