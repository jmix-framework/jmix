/*
 * Copyright 2024 Haulmont.
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

package excel_exporter

import com.fasterxml.jackson.databind.ObjectMapper
import io.jmix.core.Messages
import io.jmix.flowui.download.Downloader
import io.jmix.pivottableflowui.component.PivotTable
import io.jmix.pivottableflowui.export.PivotTableExcelExporter
import io.jmix.pivottableflowui.export.model.PivotData
import io.jmix.pivottableflowui.export.model.PivotDataCell
import io.jmix.pivottableflowui.export.model.PivotDataSeparatedCell
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer
import jakarta.validation.constraints.NotNull
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class ExcelExporterTest extends Specification {

    def pivotData
    def pivotTable = new PivotTable()
    def exporter

    def CUSTOM_FILENAME = "customFileName"
    def rowsCount = 8
    def cellsCount = 10
    def mergedRegionsCount = 17

    void setup() {
        URL resource = this.getClass().getResource("/excel_exporter/pivotData.json")
        byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()))
        def pivotDataJson = new String(encoded, StandardCharsets.UTF_8)

        ObjectMapper objectMapper = new ObjectMapper()
        pivotData = objectMapper.readValue(pivotDataJson, PivotData.class)

        exporter = new TestPivotExcelExporter(pivotTable)

        Messages messages = Mock()
        messages.getMessage("pivotExcelExporter.doubleFormat") >> { String key -> "#,##0.00##############" }
        exporter.setMessages(messages)
    }

    def "test file name"() {
        when: "set custom file name"
        exporter.exportPivotTable(pivotData, CUSTOM_FILENAME)
        then:
        exporter.fileName == CUSTOM_FILENAME

        when: "test default file name"
        exporter.fileName = null
        exporter.metaClass = null
        exporter.exportPivotTable(pivotData, null)
        then:
        exporter.fileName == exporter.DEFAULT_FILE_NAME
    }

    def "test generated sheet count values"() {
        when: "generate sheet"
        exporter.exportPivotTable(pivotData, null)
        then:
        exporter.sheet.getLastRowNum() == rowsCount - 1
        exporter.sheet.getNumMergedRegions() == mergedRegionsCount
    }

    def "test header cells type"() {
        when: "generate sheet"
        exporter.exportPivotTable(pivotData, null)
        then:
        for (int i = 0; i < 3; i++) {
            Row row = exporter.sheet.getRow(i)
            for (int j = 0; j < cellsCount; j++) {
                Cell cell = row.getCell(j)
                if ((i == 0 || i == 1) && (j == 0 || j == 1)
                        || (i == 2 && j == 2)) {
                    assert cell.getCellType() == CellType.BLANK
                    continue
                }

                assert cell.getCellType() == CellType.STRING

                def cellStyleIndex = cell.cellStyle.fontIndexAsInt
                assert exporter.wb.getFontAt(cellStyleIndex).bold
            }
        }
    }

    def "check body cells type"() {
        when: "generate sheet"
        exporter.exportPivotTable(pivotData, null)
        then:
        for (int i = 3; i < rowsCount; i++) {
            Row row = exporter.sheet.getRow(i)
            for (int j = 0; j < cellsCount; j++) {
                Cell cell = row.getCell(j)
                if (0 <= j && j < 3) {
                    assert cell.getCellType() == CellType.STRING

                    def cellStyleIndex = cell.cellStyle.fontIndexAsInt
                    assert exporter.wb.getFontAt(cellStyleIndex).bold
                    continue
                }

                if (((3 < i && i < 7) && j == 3) || (i == 5 && j == 6)) {
                    assert cell.getCellType() == CellType.BLANK
                    continue
                }

                assert cell.getCellType() == CellType.NUMERIC
                if (j == 9 || i == 7) {
                    def cellStyleIndex = cell.cellStyle.fontIndexAsInt
                    assert exporter.wb.getFontAt(cellStyleIndex).bold
                }
            }
        }
    }

    class TestPivotExcelExporter extends PivotTableExcelExporter {

        TestPivotExcelExporter(PivotTable pivotTable) {
            super(pivotTable)
        }

        @Override
        protected void export(Downloader downloader) {
        }

        @Override
        protected void initCell(Cell excelCell, PivotDataSeparatedCell cell) {
            Object cellValue = cell.getType() == PivotDataCell.Type.DECIMAL
                    ? Double.parseDouble(cell.getValue())
                    : cell.getValue()
            excelCell.setCellValue(cellValue)

            if (cell.isBold()) {
                excelCell.setCellStyle(cellLabelBoldStyle)
            }
        }
    }
}

