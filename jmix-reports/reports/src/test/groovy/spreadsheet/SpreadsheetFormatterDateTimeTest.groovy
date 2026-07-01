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

package spreadsheet

import io.jmix.reports.entity.ReportTemplate
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.XLSFormatter
import io.jmix.reports.yarg.formatters.impl.XlsxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification

import java.time.LocalTime

class SpreadsheetFormatterDateTimeTest extends Specification {

    def "XLSX formatter writes LocalTime as Excel time fraction"() {
        when:
        def output = renderXlsx()
        def workbook = new XSSFWorkbook(new ByteArrayInputStream(output))

        then:
        findNumericCellValue(workbook) == 0.5d
    }

    def "XLS formatter writes LocalTime as Excel time fraction"() {
        when:
        def output = render(new HSSFWorkbook(), "xls", ReportOutputType.xls)
        def workbook = new HSSFWorkbook(new ByteArrayInputStream(output))
        def cell = workbook.getSheetAt(0).getRow(0).getCell(0)

        then:
        cell.cellType == CellType.NUMERIC
        cell.numericCellValue == 0.5d
    }

    protected byte[] renderXlsx() {
        def output = new ByteArrayOutputStream()
        def rootBand = new BandData(BandData.ROOT_BAND_NAME)
        rootBand.setFirstLevelBandDefinitionNames(["Users"].toSet())
        def band = new BandData("Users", rootBand)
        band.setData([id: LocalTime.NOON, email: "mail@example.com"])
        rootBand.addChild(band)

        def template = new ReportTemplate()
        template.setContent(readFile("/xlsx/template.xlsx"))

        def formatterInput = new FormatterFactoryInput("xlsx", rootBand, template, ReportOutputType.xlsx, output)
        def formatter = new XlsxFormatter(formatterInput)
        formatter.renderDocument()

        return output.toByteArray()
    }

    protected byte[] render(Workbook workbook, String extension, ReportOutputType outputType) {
        def output = new ByteArrayOutputStream()
        def rootBand = new BandData(BandData.ROOT_BAND_NAME)
        rootBand.setFirstLevelBandDefinitionNames(["Band"].toSet())
        def band = new BandData("Band", rootBand)
        band.setData([time: LocalTime.NOON])
        rootBand.addChild(band)

        def template = new ReportTemplate()
        template.setContent(createTemplate(workbook))

        def formatterInput = new FormatterFactoryInput(extension, rootBand, template, outputType, output)
        def formatter = new XLSFormatter(formatterInput)
        formatter.renderDocument()

        return output.toByteArray()
    }

    protected byte[] createTemplate(Workbook workbook) {
        def sheet = workbook.createSheet("Sheet1")
        sheet.createRow(0).createCell(0).setCellValue('${time}')

        def name = workbook.createName()
        name.setNameName("Band")
        name.setRefersToFormula("'" + sheet.sheetName + "'!" + '$A$1')

        def output = new ByteArrayOutputStream()
        workbook.write(output)
        workbook.close()

        return output.toByteArray()
    }

    protected byte[] readFile(String resourceName) {
        return SpreadsheetFormatterDateTimeTest.class.getResource(resourceName).bytes
    }

    protected double findNumericCellValue(Workbook workbook) {
        for (def sheet : workbook) {
            for (def row : sheet) {
                for (def cell : row) {
                    if (cell.cellType == CellType.NUMERIC) {
                        return cell.numericCellValue
                    }
                }
            }
        }

        throw new AssertionError()
    }
}
