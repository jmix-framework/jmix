/*
 * Copyright 2026 Haulmont.
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

package read_rows;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.DownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.DataGridRows;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import read_rows.view.ReadRowsTestView;
import test_support.GridExportFlowuiTestConfiguration;
import test_support.entity.Product;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Rows read from a data grid must be exactly what the export would write, so that the data shown to another
 * consumer (e.g. an AI chat) matches a file the user could download.
 */
@UiTest(viewBasePackages = "read_rows.view")
@SpringBootTest(classes = {GridExportFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class ReadRowsTest {

    static final Predicate<Grid.Column<Object>> ALL_COLUMNS = column -> true;

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    DataManager dataManager;

    DataGrid<Product> dataGrid;

    @BeforeEach
    void setUp() {
        for (int i = 1; i <= 3; i++) {
            Product product = dataManager.create(Product.class);
            product.setName("Product " + i);
            product.setPosition(i * 1000);
            // The second product has no description, to check how a missing value is read.
            product.setDescription(i == 2 ? null : "Description " + i);
            dataManager.saveWithoutReload(product);
        }

        navigationSupport.navigate(ReadRowsTestView.class);
        ReadRowsTestView view = UiTestUtils.getCurrentView();
        dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
    }

    @AfterEach
    void tearDown() {
        dataManager.remove(dataManager.load(Product.class).all().list());
    }

    @Test
    void readRows_currentPage_matchesJsonExport() throws IOException {
        AbstractDataGridExporter<?> exporter = exporter("jsonExportAction");

        DataGridRows rows = exporter.readRows(dataGrid, ExportMode.CURRENT_PAGE, ALL_COLUMNS);

        assertEquals(exportToJson(exporter, ExportMode.CURRENT_PAGE), toKeyedRows(rows));
    }

    @Test
    void readRows_selectedRows_readsOnlySelectedAndMatchesJsonExport() throws IOException {
        List<Product> items = dataGrid.getGenericDataView().getItems().toList();
        dataGrid.select(items.get(0));
        dataGrid.select(items.get(2));
        AbstractDataGridExporter<?> exporter = exporter("jsonExportAction");

        DataGridRows rows = exporter.readRows(dataGrid, ExportMode.SELECTED_ROWS, ALL_COLUMNS);

        assertEquals(2, rows.getRows().size());
        assertEquals(exportToJson(exporter, ExportMode.SELECTED_ROWS), toKeyedRows(rows));
    }

    @Test
    void readRows_selectedRows_comeInDisplayOrderNotSelectionOrder() {
        List<Product> items = dataGrid.getGenericDataView().getItems().toList();
        dataGrid.select(items.get(2));
        dataGrid.select(items.get(0));

        DataGridRows rows = exporter("jsonExportAction").readRows(dataGrid, ExportMode.SELECTED_ROWS, ALL_COLUMNS);

        assertEquals(List.of("Product 1", "Product 3"), rows.getRows().stream().map(row -> row.get(0)).toList());
    }

    @Test
    void readRows_columnValueProvider_isApplied() throws IOException {
        AbstractDataGridExporter<?> exporter = exporter("jsonExportAction");
        exporter.addColumnValueProvider("name", context -> "#" + context.<Product>getEntity().getName());

        DataGridRows rows = exporter.readRows(dataGrid, ExportMode.CURRENT_PAGE, ALL_COLUMNS);

        assertEquals("#Product 1", rows.getRows().get(0).get(0));
        assertEquals(exportToJson(exporter, ExportMode.CURRENT_PAGE), toKeyedRows(rows));
    }

    @Test
    void readRows_missingValue_isEmptyString() {
        DataGridRows rows = exporter("jsonExportAction").readRows(dataGrid, ExportMode.CURRENT_PAGE, ALL_COLUMNS);

        assertEquals("", rows.getRows().get(1).get(2));
    }

    @Test
    void readRows_columns_carryKeyHeaderAndPropertyPath() {
        DataGridRows rows = exporter("excelExportAction").readRows(dataGrid, ExportMode.CURRENT_PAGE, ALL_COLUMNS);

        DataGridRows.Column name = rows.getColumns().get(0);
        assertEquals("name", name.getKey());
        assertEquals(dataGrid.getColumnByKey("name").getHeaderText(), name.getHeaderText());
        assertNotNull(name.getMetaPropertyPath());
        assertEquals("name", name.getMetaPropertyPath().toPathString());
    }

    @Test
    void readRows_columnFilter_isApplied() {
        DataGridRows rows = exporter("jsonExportAction")
                .readRows(dataGrid, ExportMode.CURRENT_PAGE, column -> "position".equals(column.getKey()));

        assertEquals(List.of("position"), rows.getColumns().stream().map(DataGridRows.Column::getKey).toList());
        assertEquals(1, rows.getRows().get(0).size());
    }

    @Test
    void readRows_columnWithoutProperty_readsRenderedComponentText() throws IOException {
        AbstractDataGridExporter<?> exporter = exporter("jsonExportAction");

        DataGridRows rows = exporter.readRows(dataGrid, ExportMode.CURRENT_PAGE, ALL_COLUMNS);

        int labelIndex = rows.getColumns().stream().map(DataGridRows.Column::getKey).toList().indexOf("label");
        assertEquals(3, labelIndex);
        assertNull(rows.getColumns().get(labelIndex).getMetaPropertyPath());
        assertEquals(List.of("Item 1000", "Item 2000", "Item 3000"),
                rows.getRows().stream().map(row -> row.get(labelIndex)).toList());
        assertEquals(exportToJson(exporter, ExportMode.CURRENT_PAGE), toKeyedRows(rows));
    }

    @Test
    void readRows_allRows_throws() {
        AbstractDataGridExporter<?> exporter = exporter("jsonExportAction");

        assertThrows(IllegalArgumentException.class,
                () -> exporter.readRows(dataGrid, ExportMode.ALL_ROWS, ALL_COLUMNS));
    }

    AbstractDataGridExporter<?> exporter(String actionId) {
        ExportAction action = (ExportAction) dataGrid.getAction(actionId);
        assertNotNull(action);
        AbstractDataGridExporter<?> exporter = action.getDataGridExporter();
        assertNotNull(exporter);
        return exporter;
    }

    /**
     * Converts read rows to {@code key=value} entries, one per column.
     */
    List<List<String>> toKeyedRows(DataGridRows rows) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> row : rows.getRows()) {
            List<String> keyed = new ArrayList<>();
            for (int i = 0; i < rows.getColumns().size(); i++) {
                keyed.add(rows.getColumns().get(i).getKey() + "=" + row.get(i));
            }
            result.add(keyed);
        }
        return result;
    }

    /**
     * Exports the grid to JSON and reads the exported rows as {@code key=value} entries, one per column. The entries
     * are taken from the JSON objects themselves, so an extra or reordered key fails the test. Gson omits null
     * members, so a column with a null value is absent from the object; it is read as an empty value at its column
     * position, which is how the rows hold a missing value.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<List<String>> exportToJson(AbstractDataGridExporter<?> exporter, ExportMode exportMode) throws IOException {
        Downloader downloader = mock(Downloader.class);
        exporter.exportDataGrid(downloader, (Grid) dataGrid, exportMode, ALL_COLUMNS);

        ArgumentCaptor<DownloadDataProvider> dataProvider = ArgumentCaptor.forClass(DownloadDataProvider.class);
        verify(downloader).download(dataProvider.capture(), anyString(), any(DownloadFormat.class));

        List<String> columnKeys = dataGrid.getColumns().stream().map(Grid.Column::getKey).toList();
        List<List<String>> result = new ArrayList<>();
        try (Reader reader = new InputStreamReader(dataProvider.getValue().getStream(), StandardCharsets.UTF_8)) {
            JsonArray jsonRows = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement jsonRow : jsonRows) {
                Map<String, String> exported = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> entry : jsonRow.getAsJsonObject().entrySet()) {
                    JsonElement value = entry.getValue();
                    exported.put(entry.getKey(), value.isJsonNull() ? "" : value.getAsString());
                }

                List<String> expectedKeys = columnKeys.stream().filter(exported::containsKey).toList();
                assertEquals(expectedKeys, new ArrayList<>(exported.keySet()),
                        "Exported keys are the grid column keys, in column order, without the null ones");

                List<String> keyed = new ArrayList<>();
                for (String key : columnKeys) {
                    keyed.add(key + "=" + exported.getOrDefault(key, ""));
                }
                result.add(keyed);
            }
        }
        return result;
    }
}
