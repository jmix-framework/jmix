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

package io.jmix.gridexportflowui.exporter.excel;

import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.data.DataUnit;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.AbstractAllRecordsExporter;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class is used by {@link io.jmix.gridexportflowui.action.ExportAction} for exporting all records from the database.
 */
@Component("grdexp_ExcelAllRecordsExporter")
public class ExcelAllRecordsExporter {

    public static final String KEYSET_EXPORT_STRATEGY = "keyset";
    public static final String LIMIT_OFFSET_EXPORT_STRATEGY = "limit-offset";

    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public ExcelAllRecordsExporter(MetadataTools metadataTools,
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
        Preconditions.checkNotNullArgument(excelRowCreator, "Cannot export all rows. ExcelRowCreator can't be null");
        Preconditions.checkNotNullArgument(excelRowChecker, "Cannot export all rows. ExcelRowChecker can't be null");

        Predicate<EntityExportContext> entityExporter = context -> {
            boolean exportNotAllowed = excelRowChecker.test(context.getEntityNumber());
            if (exportNotAllowed) {
                return false;
            } else {
                excelRowCreator.accept(new RowCreationContext(context.getEntity(), context.getEntityNumber()));
                return true;
            }
        };

        getExcelRecordsExporter().exportAll(dataUnit, entityExporter);
    }

    private AbstractAllRecordsExporter getExcelRecordsExporter() {
        AbstractAllRecordsExporter exporter;
        String exportStrategy = gridExportProperties.getExcel().getExportStrategy();
        if (KEYSET_EXPORT_STRATEGY.equals(exportStrategy)) {
            exporter = new ExcelKeysetExporter(
                    metadataTools, dataManager, platformTransactionManager, gridExportProperties);
        } else if (LIMIT_OFFSET_EXPORT_STRATEGY.equals(exportStrategy)){
            exporter = new ExcelLimitOffsetExporter(
                    metadataTools, dataManager, platformTransactionManager, gridExportProperties);
        } else {
            throw new IllegalStateException(String.format("Unknown excel export strategy: %s", exportStrategy));
        }
        return exporter;
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
