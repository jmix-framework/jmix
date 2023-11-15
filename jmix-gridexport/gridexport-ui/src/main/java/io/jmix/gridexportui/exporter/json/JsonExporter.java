/*
 * Copyright 2020 Haulmont.
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

package io.jmix.gridexportui.exporter.json;

import com.google.gson.*;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.gridexportui.action.ExportAction;
import io.jmix.gridexportui.exporter.AbstractTableExporter;
import io.jmix.gridexportui.exporter.ExportMode;
import io.jmix.gridexportui.exporter.ExporterSortHelper;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Table;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use this class to export {@link Table} into JSON format.
 * <br>Just create an instance of {@link ExportAction} with <code>withExporter</code> method.
 */
@Component("grdexp_JsonExporter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JsonExporter extends AbstractTableExporter<JsonExporter> {

    protected Metadata metadata;
    protected JsonAllRecordsExporter jsonAllRecordsExporter;

    protected Function<GsonBuilder, GsonBuilder> gsonConfigurer;

    public JsonExporter(Metadata metadata, JsonAllRecordsExporter jsonAllRecordsExporter) {
        this.metadata = metadata;
        this.jsonAllRecordsExporter = jsonAllRecordsExporter;
    }

    /**
     * Configure Gson builder for export
     *
     * @param gsonConfigurator Gson configurator function
     * @return exporter instance
     */
    public JsonExporter withGsonConfigurator(Function<GsonBuilder, GsonBuilder> gsonConfigurator) {
        this.gsonConfigurer = gsonConfigurator;
        return this;
    }

    @Override
    public void exportTable(Downloader downloader, Table<Object> table, ExportMode exportMode) {
        Gson gson = createGsonForSerialization();
        JsonArray jsonElements = new JsonArray();

        if (exportMode == ExportMode.ALL_ROWS) {
            jsonAllRecordsExporter.exportAll(table.getItems(),
                    entity -> {
                        JsonObject jsonObject = createJsonObjectFromEntity(table, entity);
                        jsonElements.add(jsonObject);
                    }, ExporterSortHelper.getSortOrder(table.getSortInfo()));
        } else {
            Collection<Object> items = getItems(table, exportMode);

            for (Object entity : items) {
                JsonObject jsonObject = createJsonObjectFromEntity(table, entity);
                jsonElements.add(jsonObject);
            }
        }

        downloader.download(new ByteArrayDataProvider(gson.toJson(jsonElements).getBytes(StandardCharsets.UTF_8),
                        uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()),
                getFileName(table) + ".json", DownloadFormat.JSON);
    }

    @Override
    public void exportDataGrid(Downloader downloader, DataGrid<Object> dataGrid, ExportMode exportMode) {
        Gson gson = createGsonForSerialization();
        JsonArray jsonElements = new JsonArray();

        if (exportMode == ExportMode.ALL_ROWS) {
            jsonAllRecordsExporter.exportAll(dataGrid.getItems(),
                    entity -> {
                        JsonObject jsonObject = createJsonObjectFromEntity(dataGrid, entity);
                        jsonElements.add(jsonObject);
                    }, ExporterSortHelper.getSortOrder(dataGrid.getSortOrder()));
        } else {
            Collection<Object> items = getItems(dataGrid, exportMode);

            for (Object entity : items) {
                JsonObject jsonObject = createJsonObjectFromEntity(dataGrid, entity);
                jsonElements.add(jsonObject);
            }
        }

        downloader.download(new ByteArrayDataProvider(gson.toJson(jsonElements).getBytes(StandardCharsets.UTF_8),
                        uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()),
                getFileName(dataGrid) + ".json", DownloadFormat.JSON);
    }

    protected JsonObject createJsonObjectFromEntity(DataGrid<Object> dataGrid, Object entity) {
        JsonObject jsonObject = new JsonObject();

        for (DataGrid.Column<Object> column : dataGrid.getColumns()) {
            Object columnValue = getColumnValue(dataGrid, column, entity);
            MetaPropertyPath metaPropertyPath = metadata.getClass(entity).getPropertyPath(column.getId());

            if (columnValue != null) {
                jsonObject.add(column.getId(),
                        new JsonPrimitive(formatValue(columnValue, metaPropertyPath)));
            } else {
                jsonObject.add(column.getId(),
                        JsonNull.INSTANCE);
            }
        }

        return jsonObject;
    }

    protected JsonObject createJsonObjectFromEntity(Table<Object> table, Object entity) {
        JsonObject jsonObject = new JsonObject();

        for (Table.Column<Object> column : table.getColumns()) {
            if (column.getId() instanceof MetaPropertyPath) {
                MetaPropertyPath propertyPath = (MetaPropertyPath) column.getId();
                Object columnValue = getColumnValue(table, column, entity);

                if (columnValue != null) {
                    jsonObject.add(propertyPath.getMetaProperty().getName(),
                            new JsonPrimitive(formatValue(columnValue, (MetaPropertyPath) column.getId())));
                } else {
                    jsonObject.add(propertyPath.getMetaProperty().getName(),
                            JsonNull.INSTANCE);
                }
            } else {
                Object columnValue = getColumnValue(table, column, entity);
                if (columnValue != null) {
                    jsonObject.add(column.getStringId(), new JsonPrimitive(formatValue(columnValue)));
                }
            }
        }

        return jsonObject;
    }

    protected Gson createGsonForSerialization() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (gsonConfigurer != null) {
            gsonConfigurer.apply(gsonBuilder);
        }
        return gsonBuilder.create();
    }

    protected Collection<Object> getItems(Table<Object> table, ExportMode exportMode) {
        return ExportMode.CURRENT_PAGE == exportMode
                ? table.getItems().getItems()
                : table.getSelected();
    }

    protected Collection<Object> getItems(DataGrid<Object> dataGrid, ExportMode exportMode) {
        return ExportMode.CURRENT_PAGE == exportMode
                ? dataGrid.getItems().getItems().collect(Collectors.toList())
                : dataGrid.getSelected();
    }

    @Override
    public String getCaption() {
        return messages.getMessage("jsonExporter.caption");
    }
}
