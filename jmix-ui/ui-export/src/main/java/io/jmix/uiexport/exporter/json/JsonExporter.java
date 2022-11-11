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

package io.jmix.uiexport.exporter.json;

import com.google.gson.*;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Table;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.uiexport.action.ExportAction;
import io.jmix.uiexport.exporter.AbstractTableExporter;
import io.jmix.uiexport.exporter.ExportMode;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component("ui_JsonExporter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JsonExporter extends AbstractTableExporter<JsonExporter> {

    @Autowired
    protected Metadata metadata;

    protected Function<GsonBuilder, GsonBuilder> gsonConfigurer;

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
        Collection<Object> items = getItems(table, exportMode);
        Gson gson = createGsonForSerialization();
        JsonArray jsonElements = new JsonArray();
        for (Object entity : items) {
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
            jsonElements.add(jsonObject);
        }
        downloader.download(new ByteArrayDataProvider(gson.toJson(jsonElements).getBytes(StandardCharsets.UTF_8),
                        uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()),
                getFileName(table) + ".json", DownloadFormat.JSON);
    }

    @Override
    public void exportDataGrid(Downloader downloader, DataGrid<Object> dataGrid, ExportMode exportMode) {
        Collection<Object> items = getItems(dataGrid, exportMode);
        Gson gson = createGsonForSerialization();
        JsonArray jsonElements = new JsonArray();
        for (Object entity : items) {
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
            jsonElements.add(jsonObject);
        }
        downloader.download(new ByteArrayDataProvider(gson.toJson(jsonElements).getBytes(StandardCharsets.UTF_8),
                        uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()),
                getFileName(dataGrid) + ".json", DownloadFormat.JSON);
    }

    protected Gson createGsonForSerialization() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (gsonConfigurer != null) {
            gsonConfigurer.apply(gsonBuilder);
        }
        return gsonBuilder.create();
    }

    protected Collection<Object> getItems(Table<Object> table, ExportMode exportMode) {
        return ExportMode.ALL == exportMode ? table.getItems().getItems() : table.getSelected();
    }

    protected Collection<Object> getItems(DataGrid<Object> dataGrid, ExportMode exportMode) {
        return ExportMode.ALL == exportMode ? dataGrid.getItems().getItems().collect(Collectors.toList()) : dataGrid.getSelected();
    }

    @Override
    public String getCaption() {
        return messages.getMessage("jsonExporter.caption");
    }
}
