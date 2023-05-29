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

package io.jmix.gridexportflowui.exporter.json;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.data.DataUnit;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.AbstractAllRecordsExporter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

/**
 * Class is used by {@link io.jmix.gridexportflowui.action.ExportAction} for exporting all records from the database to JSON format.
 */
@Component("grdexp_JsonAllRecordsExporter")
public class JsonAllRecordsExporter extends AbstractAllRecordsExporter {

    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public JsonAllRecordsExporter(MetadataTools metadataTools,
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
     * {@code jsonObjectCreator} function to each loaded entity instance. Creation of the output file object is the
     * responsibility of the function. Data is loaded in batches, the batch size is configured by the
     * {@link GridExportProperties#getExportAllBatchSize()}.
     *
     * @param dataUnit          data unit linked with the data
     * @param jsonObjectCreator function that is being applied to each loaded instance
     */
    @SuppressWarnings("rawtypes")
    public void exportAll(DataUnit dataUnit, Consumer<Object> jsonObjectCreator) {
        Preconditions.checkNotNullArgument(jsonObjectCreator, "jsonObjectCreator can't be null");

        LoadContext loadContext = generateLoadContext(dataUnit);
        LoadContext.Query query = loadContext.getQuery();

        Preconditions.checkNotNullArgument(query, "Cannot export all rows. %s can't be null",
                LoadContext.Query.class.getSimpleName());

        int loadBatchSize = gridExportProperties.getExportAllBatchSize();
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            long count = dataManager.getCount(loadContext);
            boolean initialLoading = true;
            Object lastLoadedPkValue = null;

            for (int firstResult = 0; firstResult < count; firstResult += loadBatchSize) {
                if (initialLoading) {
                    initialLoading = false;
                } else {
                    query.setParameter(LAST_LOADED_PK_CONDITION_PARAMETER_NAME, lastLoadedPkValue);
                }
                query.setMaxResults(loadBatchSize);

                List entities = dataManager.loadList(loadContext);
                for (Object entity : entities) {
                    jsonObjectCreator.accept(entity);
                }

                Object lastEntity = entities.get(entities.size() - 1);
                lastLoadedPkValue = Id.of(lastEntity).getValue();
            }
        });
    }
}
