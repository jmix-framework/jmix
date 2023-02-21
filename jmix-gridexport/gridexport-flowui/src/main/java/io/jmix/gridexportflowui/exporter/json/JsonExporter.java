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

import com.google.gson.*;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.data.grid.ContainerTreeDataGridItems;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use this class to export {@link DataGrid} into JSON format.
 * <br>Just create an instance of {@link ExportAction} with <code>withExporter</code> method.
 */
@Component("flowui_JsonExporter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JsonExporter extends AbstractDataGridExporter<JsonExporter> {

    @Autowired
    protected Metadata metadata;

    protected Function<GsonBuilder, GsonBuilder> gsonConfigurer;

    /**
     * Configure Gson builder for export
     *
     * @param gsonConfigurer Gson configurator function
     * @return exporter instance
     */
    protected JsonExporter withGsonConfigurator(Function<GsonBuilder, GsonBuilder> gsonConfigurer) {
        this.gsonConfigurer = gsonConfigurer;
        return this;
    }

    @Override
    public void exportDataGrid(Downloader downloader, Grid<Object> dataGrid, ExportMode exportMode) {
        Collection<Object> items = getItems(dataGrid, exportMode);
        Gson gson = createGsonForSerialization();
        JsonArray jsonElements = new JsonArray();

        for (Object entity : items) {
            JsonObject jsonObject = new JsonObject();

            for (Grid.Column<Object> column : dataGrid.getColumns()) {
                Object columnValue = getColumnValue(dataGrid, column, entity);
                MetaPropertyPath metaPropertyPath = metadata.getClass(entity).getPropertyPath(column.getKey());

                if (columnValue != null) {
                    jsonObject.add(column.getKey(),
                            new JsonPrimitive(formatValue(columnValue, metaPropertyPath)));
                } else {
                    jsonObject.add(column.getKey(), JsonNull.INSTANCE);
                }
            }

            jsonElements.add(jsonObject);
        }

        ByteArrayDownloadDataProvider downloadDataProvider = new ByteArrayDownloadDataProvider(
                gson.toJson(jsonElements).getBytes(StandardCharsets.UTF_8),
                flowuiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir()
        );

        downloader.download(downloadDataProvider, getFileName(dataGrid) + ".json", DownloadFormat.JSON);
    }

    protected Gson createGsonForSerialization() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (gsonConfigurer != null) {
            gsonConfigurer.apply(gsonBuilder);
        }

        return gsonBuilder.create();
    }

    protected Collection<Object> getItems(Grid<Object> dataGrid, ExportMode exportMode) {
        return ExportMode.CURRENT_PAGE == exportMode
                ? getDataGridItems(dataGrid)
                : dataGrid.getSelectedItems();
    }

    @SuppressWarnings("unchecked")
    protected Collection<Object> getDataGridItems(Grid<Object> dataGrid) {
        if (dataGrid instanceof TreeDataGrid) {
            TreeDataGrid<Object> treeDataGrid = (TreeDataGrid<Object>) dataGrid;

            return new ArrayList<>(
                    ((ContainerTreeDataGridItems<Object>) treeDataGrid.getItems()).getContainer().getItems());
        }

        return dataGrid.getGenericDataView().getItems().collect(Collectors.toList());
    }

    @Override
    public String getLabel() {
        return messages.getMessage("jsonExporter.label");
    }
}
