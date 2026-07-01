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

package excel_exporter;

import io.jmix.core.Messages;
import io.jmix.flowui.download.Downloader;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.export.PivotTableExcelExporter;
import io.jmix.pivottableflowui.export.model.PivotData;
import io.jmix.pivottableflowui.export.model.PivotDataCell;
import io.jmix.pivottableflowui.export.model.PivotDataSeparatedCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExcelExporterTest {

    private static final String CUSTOM_FILENAME = "customFileName";
    private static final int ROWS_COUNT = 8;
    private static final int CELLS_COUNT = 10;
    private static final int MERGED_REGIONS_COUNT = 17;

    private PivotData pivotData;
    private final PivotTable<?> pivotTable = new PivotTable<>();
    private TestPivotExcelExporter exporter;

    @BeforeEach
    void setUp() throws Exception {
        URL resource = getClass().getResource("/excel_exporter/pivotData.json");
        if (resource == null) {
            throw new IllegalStateException("Cannot find /excel_exporter/pivotData.json");
        }

        byte[] encoded = Files.readAllBytes(toPath(resource));
        String pivotDataJson = new String(encoded, StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        pivotData = objectMapper.readValue(pivotDataJson, PivotData.class);

        exporter = new TestPivotExcelExporter(pivotTable);

        Messages messages = mock(Messages.class);
        when(messages.getMessage(eq("pivottable.excelExporter.doubleFormat")))
                .thenReturn("#,##0.00##############");
        exporter.setMessages(messages);
    }

    @Test
    @DisplayName("Test file name")
    void testFileName() {
        /*
         * Set a custom file name
         */
        exporter.exportPivotTable(pivotData, CUSTOM_FILENAME);
        assertEquals(CUSTOM_FILENAME, exporter.getFileName());

        /*
         * Test default file name
         */
        exporter.resetFileName();
        exporter.exportPivotTable(pivotData, null);
        assertEquals(PivotTableExcelExporter.DEFAULT_FILE_NAME, exporter.getFileName());
    }

    @Test
    @DisplayName("Test generated sheet count values")
    void testGeneratedSheetCountValues() {
        exporter.exportPivotTable(pivotData, null);

        assertEquals(ROWS_COUNT - 1, exporter.getSheet().getLastRowNum());
        assertEquals(MERGED_REGIONS_COUNT, exporter.getSheet().getNumMergedRegions());
    }

    @Test
    @DisplayName("Test header cells type")
    void testHeaderCellsType() {
        exporter.exportPivotTable(pivotData, null);

        for (int i = 0; i < 3; i++) {
            Row row = exporter.getSheet().getRow(i);
            for (int j = 0; j < CELLS_COUNT; j++) {
                Cell cell = row.getCell(j);
                if (((i == 0 || i == 1) && (j == 0 || j == 1)) || (i == 2 && j == 2)) {
                    assertEquals(CellType.BLANK, cell.getCellType());
                    continue;
                }

                assertEquals(CellType.STRING, cell.getCellType());

                int cellStyleIndex = cell.getCellStyle().getFontIndexAsInt();
                assertTrue(exporter.getWorkbook().getFontAt(cellStyleIndex).getBold());
            }
        }
    }

    @Test
    @DisplayName("Check body cells type")
    void checkBodyCellsType() {
        exporter.exportPivotTable(pivotData, null);

        for (int i = 3; i < ROWS_COUNT; i++) {
            Row row = exporter.getSheet().getRow(i);
            for (int j = 0; j < CELLS_COUNT; j++) {
                Cell cell = row.getCell(j);
                if (0 <= j && j < 3) {
                    assertEquals(CellType.STRING, cell.getCellType());

                    int cellStyleIndex = cell.getCellStyle().getFontIndexAsInt();
                    assertTrue(exporter.getWorkbook().getFontAt(cellStyleIndex).getBold());
                    continue;
                }

                if (((3 < i && i < 7) && j == 3) || (i == 5 && j == 6)) {
                    assertEquals(CellType.BLANK, cell.getCellType());
                    continue;
                }

                assertEquals(CellType.NUMERIC, cell.getCellType());
                if (j == 9 || i == 7) {
                    int cellStyleIndex = cell.getCellStyle().getFontIndexAsInt();
                    assertTrue(exporter.getWorkbook().getFontAt(cellStyleIndex).getBold());
                }
            }
        }
    }

    private static Path toPath(URL resource) throws URISyntaxException {
        return Path.of(resource.toURI());
    }

    static class TestPivotExcelExporter extends PivotTableExcelExporter {

        TestPivotExcelExporter(PivotTable<?> pivotTable) {
            super(pivotTable);
        }

        @Override
        protected void export(Downloader downloader) {
        }

        @Override
        protected void initCell(Cell excelCell, PivotDataSeparatedCell cell) {
            Object cellValue = cell.getType() == PivotDataCell.Type.DECIMAL
                    ? Double.parseDouble(cell.getValue())
                    : cell.getValue();
            if (cellValue == null) {
                excelCell.setBlank();
            } else if (cellValue instanceof String value) {
                excelCell.setCellValue(value);
            } else {
                Double value = (Double) cellValue;
                excelCell.setCellValue(value);
            }

            if (cell.getType() == PivotDataCell.Type.DECIMAL) {
                excelCell.setCellValue(Double.parseDouble(cell.getValue()));
            }

            if (cell.isBold()) {
                excelCell.setCellStyle(cellLabelBoldStyle);
            }
        }

        String getFileName() {
            return fileName;
        }

        void resetFileName() {
            fileName = null;
        }

        org.apache.poi.ss.usermodel.Sheet getSheet() {
            return sheet;
        }

        org.apache.poi.ss.usermodel.Workbook getWorkbook() {
            return wb;
        }
    }
}
