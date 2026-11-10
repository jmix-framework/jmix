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

package excel_header;

import com.vaadin.flow.component.grid.Grid;
import excel_header.view.ExcelHeaderTestView;
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
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.GridExportFlowuiTestConfiguration;
import test_support.entity.Product;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UiTest(viewBasePackages = {"excel_header.view", "io.jmix.flowui.app"})
@SpringBootTest(classes = {GridExportFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class ExcelHeaderTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    DataManager dataManager;

    @BeforeEach
    void setUp() {
        Product product = dataManager.create(Product.class);
        product.setName("Product 1");
        product.setPosition(1);
        dataManager.saveWithoutReload(product);
    }

    @AfterEach
    void tearDown() {
        dataManager.remove(dataManager.load(Product.class).all().list());
    }

    @Test
    void excelExport_aggregationRowOnTop_headerRowHasColumnHeaders() throws IOException {
        navigationSupport.navigate(ExcelHeaderTestView.class);
        DataGrid<Product> dataGrid = UiTestUtils.getComponent(UiTestUtils.getCurrentView(), "productsDataGrid");

        assertEquals(List.of("Name header", "Position header"), exportHeaderRow(dataGrid));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<String> exportHeaderRow(DataGrid<Product> dataGrid) throws IOException {
        ExportAction action = (ExportAction) dataGrid.getAction("excelExportAction");
        Downloader downloader = mock(Downloader.class);
        action.getDataGridExporter().exportDataGrid(downloader, (Grid) dataGrid, ExportMode.CURRENT_PAGE);

        ArgumentCaptor<DownloadDataProvider> dataProvider = ArgumentCaptor.forClass(DownloadDataProvider.class);
        verify(downloader).download(dataProvider.capture(), anyString(), any(DownloadFormat.class));

        List<String> headers = new ArrayList<>();
        try (InputStream stream = dataProvider.getValue().getStream();
             XSSFWorkbook workbook = new XSSFWorkbook(stream)) {
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }
        }
        return headers;
    }
}
