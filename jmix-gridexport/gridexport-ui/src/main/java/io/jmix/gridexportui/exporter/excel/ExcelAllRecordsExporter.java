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

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.common.util.Preconditions;
import io.jmix.gridexportui.GridExportProperties;
import io.jmix.gridexportui.exporter.AbstractAllRecordsExporter;
import io.jmix.ui.component.data.DataUnit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class is used by {@link io.jmix.gridexportui.action.ExportAction} for exporting all records from the database to XLSX format.
 */
@Component("grdexp_ExcelAllRecordsExporter")
public class ExcelAllRecordsExporter extends AbstractAllRecordsExporter {

    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public ExcelAllRecordsExporter(MetadataTools metadataTools,
                                   DataManager dataManager,
                                   PlatformTransactionManager platformTransactionManager,
                                   GridExportProperties gridExportProperties) {
        super(metadataTools);
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
     * @param sort An optional sorting specification for the data.
     *             If {@code null} sorting will be applied by the primary key.
     */
    @SuppressWarnings("rawtypes")
    protected void exportAll(DataUnit dataUnit, Consumer<RowCreationContext> excelRowCreator,
                             Predicate<Integer> excelRowChecker, Sort sort) {
        Preconditions.checkNotNullArgument(excelRowCreator, "Cannot export all rows. ExcelRowCreator can't be null");
        Preconditions.checkNotNullArgument(excelRowChecker, "Cannot export all rows. ExcelRowChecker can't be null");

        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            long count = dataManager.getCount(generateLoadContext(dataUnit, sort));
            int loadBatchSize = gridExportProperties.getExportAllBatchSize();

            int rowNumber = 0;
            boolean initialLoading = true;
            Object lastLoadedPkValue = null;

            for (int firstResult = 0; firstResult < count && !excelRowChecker.test(rowNumber); firstResult += loadBatchSize) {
                LoadContext loadContext = generateLoadContext(dataUnit, sort);
                LoadContext.Query query = loadContext.getQuery();

                if (initialLoading) {
                    initialLoading = false;
                } else {
                    query.setParameter(LAST_LOADED_PK_CONDITION_PARAMETER_NAME, lastLoadedPkValue);
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
