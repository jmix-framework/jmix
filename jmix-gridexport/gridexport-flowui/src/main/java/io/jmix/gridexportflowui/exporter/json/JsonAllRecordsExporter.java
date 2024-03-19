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

import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.data.DataUnit;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import io.jmix.gridexportflowui.exporter.DataExporterFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class is used by {@link io.jmix.gridexportflowui.action.ExportAction} for exporting all records from the database to JSON format.
 */
@Component("grdexp_JsonAllRecordsExporter")
public class JsonAllRecordsExporter {

    protected DataExporterFactory dataExporterFactory;

    public JsonAllRecordsExporter(DataExporterFactory dataExporterFactory) {
        this.dataExporterFactory = dataExporterFactory;
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
    public void exportAll(DataUnit dataUnit, Consumer<Object> jsonObjectCreator) {
        Preconditions.checkNotNullArgument(jsonObjectCreator, "jsonObjectCreator can't be null");

        Predicate<EntityExportContext> entityExporter = context -> {
            jsonObjectCreator.accept(context.getEntity());
            return true;
        };

        dataExporterFactory.getDataExporter().exportAll(dataUnit, entityExporter);
    }
}
