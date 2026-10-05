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

package export_all;

import com.google.common.base.Throwables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.grid.Grid;
import export_all.view.ExportAllTestView;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.DownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import io.jmix.gridexportflowui.exporter.entitiesloader.LimitOffsetAllEntitiesLoader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.GridExportFlowuiTestConfiguration;
import test_support.TestAllEntitiesLoader;
import test_support.entity.Product;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Export of all rows can be tuned per exporter, e.g. to export a grid whose data source supports only
 * a query with pagination while the application-wide keyset pagination stays in place for other grids.
 */
@UiTest(viewBasePackages = "export_all.view")
@SpringBootTest(classes = {GridExportFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class ExportAllTest {

    static final List<String> PRODUCT_NAMES = List.of("Product 1", "Product 2", "Product 3", "Product 4", "Product 5");

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    DataManager dataManager;
    @Autowired
    GridExportProperties gridExportProperties;
    @Autowired
    TestAllEntitiesLoader testAllEntitiesLoader;

    ExportAllTestView view;

    @BeforeEach
    void setUp() {
        for (int i = 0; i < PRODUCT_NAMES.size(); i++) {
            Product product = dataManager.create(Product.class);
            product.setName(PRODUCT_NAMES.get(i));
            product.setPosition(i + 1);
            dataManager.saveWithoutReload(product);
        }

        navigationSupport.navigate(ExportAllTestView.class);
        view = UiTestUtils.getCurrentView();
        // Only the pages requested by the export itself are of interest, not the ones of the initial view load.
        view.clearPageRequests();
    }

    @AfterEach
    void tearDown() {
        dataManager.remove(dataManager.load(Product.class).all().list());
    }

    @Test
    void excelExportAll_withoutExporterSettings_exportsAllRows() throws IOException {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");

        List<String> exportedNames = exportAllToExcel(dataGrid, exporter(dataGrid, "excelExportAction"));

        // Keyset pagination loads the whole data in one application-wide sized batch, in the order of primary keys.
        assertEquals(List.of("0/" + gridExportProperties.getExportAllBatchSize()), view.getDatabasePageRequests());
        assertEquals(PRODUCT_NAMES, sorted(exportedNames));
    }

    @Test
    void excelExportAll_withBatchSize_exportsAllRowsInBatchesOfThatSize() throws IOException {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
        AbstractDataGridExporter<?> exporter = exporter(dataGrid, "excelExportAction")
                .withExportAllBatchSize(2);

        List<String> exportedNames = exportAllToExcel(dataGrid, exporter);

        // Keyset pagination always starts from the first result and continues after the last loaded primary key.
        assertEquals(List.of("0/2", "0/2", "0/2"), view.getDatabasePageRequests());
        assertEquals(PRODUCT_NAMES, sorted(exportedNames));
    }

    @Test
    void excelExportAll_keysetOnExternalGrid_fails() {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "externalProductsDataGrid");
        AbstractDataGridExporter<?> exporter = exporter(dataGrid, "excelExportAction");

        // The application-wide keyset pagination filters by the last loaded primary key, which the external API
        // cannot do. This is why the pagination strategy has to be tuned for this grid only.
        RuntimeException exception = assertThrows(RuntimeException.class, () -> exportAll(dataGrid, exporter));
        assertInstanceOf(UnsupportedOperationException.class, Throwables.getRootCause(exception));
    }

    @Test
    void excelExportAll_limitOffsetWithBatchSize_requestsDelegatePagesOfThatSize() throws IOException {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "externalProductsDataGrid");
        AbstractDataGridExporter<?> exporter = exporter(dataGrid, "excelExportAction")
                .withExportAllPaginationStrategy(LimitOffsetAllEntitiesLoader.PAGINATION_STRATEGY)
                .withExportAllBatchSize(2);

        List<String> exportedNames = exportAllToExcel(dataGrid, exporter);

        assertEquals(List.of("0/2", "2/2", "4/2"), view.getExternalPageRequests());
        assertEquals(PRODUCT_NAMES, exportedNames);
    }

    @Test
    void jsonExportAll_limitOffsetWithBatchSize_requestsDelegatePagesOfThatSize() throws IOException {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "externalProductsDataGrid");
        AbstractDataGridExporter<?> exporter = exporter(dataGrid, "jsonExportAction")
                .withExportAllPaginationStrategy(LimitOffsetAllEntitiesLoader.PAGINATION_STRATEGY)
                .withExportAllBatchSize(2);

        List<String> exportedNames = exportAllToJson(dataGrid, exporter);

        assertEquals(List.of("0/2", "2/2", "4/2"), view.getExternalPageRequests());
        assertEquals(PRODUCT_NAMES, exportedNames);
    }

    @Test
    void excelExportAll_withCustomPaginationStrategy_usesLoaderRegisteredByApplication() throws IOException {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
        AbstractDataGridExporter<?> exporter = exporter(dataGrid, "excelExportAction")
                .withExportAllPaginationStrategy(TestAllEntitiesLoader.PAGINATION_STRATEGY)
                .withExportAllBatchSize(2);

        List<String> exportedNames = exportAllToExcel(dataGrid, exporter);

        assertEquals(List.of("Product 5", "Product 4", "Product 3", "Product 2", "Product 1"), exportedNames);
        assertEquals(2, testAllEntitiesLoader.getLastLoadBatchSize());
    }

    @Test
    void excelExportAll_unknownPaginationStrategy_throws() {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
        AbstractDataGridExporter<?> exporter = exporter(dataGrid, "excelExportAction")
                .withExportAllPaginationStrategy("unknown");

        assertThrows(IllegalStateException.class, () -> exportAll(dataGrid, exporter));
    }

    @Test
    void exportersOfDifferentActions_settingsOfOneDoNotAffectAnother() {
        DataGrid<Product> productsDataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
        DataGrid<Product> externalProductsDataGrid = UiTestUtils.getComponent(view, "externalProductsDataGrid");
        AbstractDataGridExporter<?> productsExporter = exporter(productsDataGrid, "excelExportAction");
        AbstractDataGridExporter<?> externalProductsExporter = exporter(externalProductsDataGrid, "excelExportAction");

        externalProductsExporter
                .withExportAllPaginationStrategy(LimitOffsetAllEntitiesLoader.PAGINATION_STRATEGY)
                .withExportAllBatchSize(2);

        assertNotSame(productsExporter, externalProductsExporter);
        assertNull(productsExporter.getExportAllPaginationStrategy());
        assertNull(productsExporter.getExportAllBatchSize());
    }

    @Test
    void setExportAllBatchSize_nonPositive_throws() {
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
        AbstractDataGridExporter<?> exporter = exporter(dataGrid, "excelExportAction");

        assertThrows(IllegalArgumentException.class, () -> exporter.setExportAllBatchSize(0));
    }

    List<String> sorted(List<String> names) {
        return names.stream()
                .sorted()
                .toList();
    }

    AbstractDataGridExporter<?> exporter(DataGrid<Product> dataGrid, String actionId) {
        ExportAction action = (ExportAction) dataGrid.getAction(actionId);
        return action.getDataGridExporter();
    }

    List<String> exportAllToExcel(DataGrid<Product> dataGrid, AbstractDataGridExporter<?> exporter)
            throws IOException {
        List<String> names = new ArrayList<>();
        try (InputStream stream = exportAll(dataGrid, exporter).getStream();
             XSSFWorkbook workbook = new XSSFWorkbook(stream)) {
            Sheet sheet = workbook.getSheetAt(0);
            // The first row is the header.
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                names.add(row.getCell(0).getStringCellValue());
            }
        }
        return names;
    }

    List<String> exportAllToJson(DataGrid<Product> dataGrid, AbstractDataGridExporter<?> exporter)
            throws IOException {
        List<String> names = new ArrayList<>();
        try (Reader reader = new InputStreamReader(exportAll(dataGrid, exporter).getStream(), StandardCharsets.UTF_8)) {
            JsonArray rows = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement row : rows) {
                names.add(row.getAsJsonObject().get("name").getAsString());
            }
        }
        return names;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    DownloadDataProvider exportAll(DataGrid<Product> dataGrid, AbstractDataGridExporter<?> exporter) {
        Downloader downloader = mock(Downloader.class);

        exporter.exportDataGrid(downloader, (Grid) dataGrid, ExportMode.ALL_ROWS);

        ArgumentCaptor<DownloadDataProvider> dataProvider = ArgumentCaptor.forClass(DownloadDataProvider.class);
        verify(downloader).download(dataProvider.capture(), anyString(), any(DownloadFormat.class));
        return dataProvider.getValue();
    }
}
