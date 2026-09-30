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

import io.jmix.reports.yarg.formatters.ReportFormatter
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.XLSFormatter
import io.jmix.reports.yarg.formatters.impl.XlsxFormatter
import io.jmix.reports.yarg.formatters.impl.xlsx.Range
import io.jmix.reports.yarg.formatters.impl.xlsx.RangeDependencies
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.hssf.model.HSSFFormulaParser
import org.apache.poi.hssf.record.NameRecord
import org.apache.poi.hssf.usermodel.HSSFPatriarch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.SpreadsheetVersion
import org.apache.poi.ss.formula.FormulaType
import org.apache.poi.ss.formula.ptg.Ptg
import org.apache.poi.ss.usermodel.ClientAnchor
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.AreaReference
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xddf.usermodel.chart.AxisPosition
import org.apache.poi.xddf.usermodel.chart.ChartTypes
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import xlsx.BaseXlsxRenderTest

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * The print area of a spreadsheet template must follow the rendered bands, otherwise printing the report
 * (or converting it to PDF) cuts the data off at the template rows. The contract is shared by the XLS and XLSX
 * engines; layouts that only one of them supports are checked for that engine only. The rules for the rows and the
 * columns of the print area are unit-tested in {@code XlsxUtilsTest}.
 */
class SpreadsheetPrintAreaTest extends BaseXlsxRenderTest {

    enum Engine {
        XLSX(ReportOutputType.xlsx, { new XSSFWorkbook() }, { new XlsxFormatter(it) }),
        XLS(ReportOutputType.xls, { new HSSFWorkbook() }, { new XLSFormatter(it) })

        final ReportOutputType outputType
        final Closure<Workbook> templateWorkbook
        final Closure<ReportFormatter> formatter

        Engine(ReportOutputType outputType, Closure<Workbook> templateWorkbook, Closure<ReportFormatter> formatter) {
            this.outputType = outputType
            this.templateWorkbook = templateWorkbook
            this.formatter = formatter
        }
    }

    def "print area grows to cover every row rendered by the bands inside it [#engine]"() {
        given: "a header and an item band, the print area is set to the template rows A1:B2"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 0, 1, "Amount")
                cell(s, 1, 0, '${name}')
                cell(s, 1, 1, '${amount}')
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Items", 1, 0, 1, 1)
                wb.setPrintArea(0, 0, 1, 0, 1)
            }
            def root = rootBand("Header", "Items")
            addBand(root, "Header", [:])
            (1..50).each { addBand(root, "Items", [name: "Item " + it, amount: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "the header row and all 50 item rows are inside the print area"
            printArea(result) == "A1:B51"

        and: "the print area is written with absolute references, as Excel writes it"
            result.getPrintArea(0).endsWith('!$A$1:$B$51')

        where:
            engine << Engine.values()
    }

    def "a band outside the template print area stays outside it [#engine]"() {
        given: "a footer band below the print area A1:B2"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${name}')
                cell(s, 2, 0, "Not printed")
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Items", 1, 0, 1, 1)
                defineBand(wb, "Footer", 2, 0, 2, 1)
                wb.setPrintArea(0, 0, 1, 0, 1)
            }
            def root = rootBand("Header", "Items", "Footer")
            addBand(root, "Header", [:])
            (1..3).each { addBand(root, "Items", [name: "Item " + it]) }
            addBand(root, "Footer", [:])

        when:
            def result = renderWith(engine, template, root)

        then: "the print area ends at the last item row, the footer row 5 is left out"
            printArea(result) == "A1:B4"

        where:
            engine << Engine.values()
    }

    def "a band in the rows of the print area but outside its columns stays outside it [#engine]"() {
        given: "the print area A1:B2 covers the header band, the item band in D2:E2 is right of it"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 3, '${name}')
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Items", 1, 3, 1, 4)
                wb.setPrintArea(0, 0, 1, 0, 1)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(5))

        then: "the item rows 2-6 are left out"
            printArea(result) == "A1:B1"

        where:
            engine << Engine.values()
    }

    def "print area keeps its template columns when a band is wider [#engine]"() {
        given: "bands span A:C, the print area excludes column C"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 0, 1, "Amount")
                cell(s, 0, 2, "Internal id")
                cell(s, 1, 0, '${name}')
                cell(s, 1, 1, '${amount}')
                cell(s, 1, 2, '${id}')
                defineBand(wb, "Header", 0, 0, 0, 2)
                defineBand(wb, "Items", 1, 0, 1, 2)
                wb.setPrintArea(0, 0, 1, 0, 1)
            }
            def root = rootBand("Header", "Items")
            addBand(root, "Header", [:])
            (1..3).each { addBand(root, "Items", [name: "Item " + it, amount: it, id: it]) }

        when:
            def result = renderWith(engine, template, root)

        then:
            printArea(result) == "A1:B4"

        where:
            engine << Engine.values()
    }

    def "print area keeps the columns of bands that start right of column A [#engine]"() {
        given: "the bands and the print area C1:D2 start in column C"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 2, "Name")
                cell(s, 0, 3, "Amount")
                cell(s, 1, 2, '${name}')
                cell(s, 1, 3, '${amount}')
                defineBand(wb, "Header", 0, 2, 0, 3)
                defineBand(wb, "Items", 1, 2, 1, 3)
                wb.setPrintArea(0, 2, 3, 0, 1)
            }
            def root = rootBand("Header", "Items")
            addBand(root, "Header", [:])
            (1..3).each { addBand(root, "Items", [name: "Item " + it, amount: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "columns A and B are not printed"
            printArea(result) == "C1:D4"

        where:
            engine << Engine.values()
    }

    def "print area keeps its blank columns left of the bands [#engine]"() {
        given: "the bands start in column B, the print area A1:C2 also covers the blank column A"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 1, "Name")
                cell(s, 1, 1, '${name}')
                defineBand(wb, "Header", 0, 1, 0, 2)
                defineBand(wb, "Items", 1, 1, 1, 2)
                wb.setPrintArea(0, 0, 2, 0, 1)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            printArea(result) == "A1:C4"

        where:
            engine << Engine.values()
    }

    def "print area reaching below the template ends at the last rendered row [#engine]"() {
        given: "the print area A1:B10 is larger than the two template rows"
            def template = headerAndItemsTemplate(engine, '$A$1:$B$10')

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            printArea(result) == "A1:B4"

        where:
            engine << Engine.values()
    }

    def "print area ending on an empty band still covers the rows above it [#engine]"() {
        given: "the print area A1:B3 ends on a summary band that has no data"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${name}')
                cell(s, 2, 0, '${total}')
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Items", 1, 0, 1, 1)
                defineBand(wb, "Summary", 2, 0, 2, 1)
                wb.setPrintArea(0, 0, 1, 0, 2)
            }
            def root = rootBand("Header", "Items", "Summary")
            addBand(root, "Header", [:])
            (1..3).each { addBand(root, "Items", [name: "Item " + it]) }

        when:
            def result = renderWith(engine, template, root)

        then:
            printArea(result) == "A1:B4"

        where:
            engine << Engine.values()
    }

    def "print area over bands without data is kept as authored [#engine]"() {
        given: "the print area A2:B2 covers only the item band, which has no data"
            def template = headerAndItemsTemplate(engine, '$A$2:$B$2')

        when:
            def result = renderWith(engine, template, headerAndItems(0))

        then:
            printArea(result) == "A2:B2"

        where:
            engine << Engine.values()
    }

    def "print area over a master-detail block covers the last group without details [#engine]"() {
        given: "a group band with a nested item band, the last group has no items"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${group}')
                cell(s, 2, 0, '${item}')
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Group", 1, 0, 1, 1)
                defineBand(wb, "Item", 2, 0, 2, 1)
                wb.setPrintArea(0, 0, 1, 0, 2)
            }
            def root = rootBand("Header", "Group")
            addBand(root, "Header", [:])
            def first = addBand(root, "Group", [group: "G1"])
            addBand(first, "Item", [item: "I1"])
            addBand(first, "Item", [item: "I2"])
            addBand(root, "Group", [group: "G2"])

        when:
            def result = renderWith(engine, template, root)

        then: "G2 is rendered to row 5 and printed"
            stringValue(result.getSheetAt(0), 4, 0) == "G2"
            printArea(result) == "A1:B5"

        where:
            engine << Engine.values()
    }

    def "print area starting on a nested band row covers all its rows [#engine]"() {
        given: "a group band with a nested item band and a static total; the print area A2:B3 starts on the item row"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${group}')
                cell(s, 1, 0, '${item}')
                cell(s, 2, 0, "Total")
                defineBand(wb, "Group", 0, 0, 0, 1)
                defineBand(wb, "Item", 1, 0, 1, 1)
                wb.setPrintArea(0, 0, 1, 1, 2)
            }
            def root = rootBand("Group")
            def first = addBand(root, "Group", [group: "G1"])
            addBand(first, "Item", [item: "I1"])
            addBand(first, "Item", [item: "I2"])
            def second = addBand(root, "Group", [group: "G2"])
            addBand(second, "Item", [item: "I3"])

        when:
            def result = renderWith(engine, template, root)

        then: "the area starts at I1 in row 2 and covers I3 in row 5"
            printArea(result) == expected

        where: "XLS and XLSX do not render the static total"
            engine                | expected
            Engine.XLSX           | "A2:B5"
            Engine.XLS            | "A2:B5"
    }

    def "print area over a parent band covers its nested band rows outside the area [#engine]"() {
        given: "the print area A1:B2 covers the header and group rows but not the nested item row"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${group}')
                cell(s, 2, 0, '${item}')
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Group", 1, 0, 1, 1)
                defineBand(wb, "Item", 2, 0, 2, 1)
                wb.setPrintArea(0, 0, 1, 0, 1)
            }
            def root = rootBand("Header", "Group")
            addBand(root, "Header", [:])
            def first = addBand(root, "Group", [group: "G1"])
            addBand(first, "Item", [item: "I1"])
            addBand(first, "Item", [item: "I2"])
            def second = addBand(root, "Group", [group: "G2"])
            addBand(second, "Item", [item: "I3"])
            addBand(second, "Item", [item: "I4"])

        when:
            def result = renderWith(engine, template, root)

        then: "the items of the last group, rendered to rows 6 and 7, are printed too"
            stringValue(result.getSheetAt(0), 6, 0) == "I4"
            printArea(result) == "A1:B7"

        where:
            engine << Engine.values()
    }

    def "print area ignores nested bands rendered on another sheet [#engine]"() {
        given: "a group band inside the print area of the first sheet, its nested item band on the second sheet"
            def template = template(engine) { Workbook wb ->
                cell(sheet(wb, "First"), 0, 0, '${group}')
                cell(sheet(wb, "Second"), 0, 0, '${item}')
                defineBand(wb, "Group", 0, 0, 0, 1, "First")
                defineBand(wb, "Item", 0, 0, 0, 1, "Second")
                wb.setPrintArea(0, 0, 1, 0, 0)
            }
            def root = rootBand("Group")
            def group = addBand(root, "Group", [group: "G1"])
            (1..30).each { addBand(group, "Item", [item: "I" + it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "the item rows of the second sheet do not stretch the print area of the first one"
            printArea(result, 0) == "A1:B1"

        where:
            engine << Engine.values()
    }

    def "print area covers a merged region that moves with its band [#engine]"() {
        given: "a one-cell notes band in A3 inside the merged region A3:C6, the print area A1:C6 covers the region"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${name}')
                cell(s, 2, 0, '${notes}')
                s.addMergedRegion(CellRangeAddress.valueOf("A3:C6"))
                defineBand(wb, "Header", 0, 0, 0, 2)
                defineBand(wb, "Items", 1, 0, 1, 2)
                defineBand(wb, "Notes", 2, 0, 2, 0)
                wb.setPrintArea(0, 0, 2, 0, 5)
            }
            def root = rootBand("Header", "Items", "Notes")
            addBand(root, "Header", [:])
            (1..3).each { addBand(root, "Items", [name: "Item " + it]) }
            addBand(root, "Notes", [notes: "Notes"])

        when:
            def result = renderWith(engine, template, root)

        then: "the notes and the region around them are rendered to rows 5-8, all of them are printed"
            stringValue(result.getSheetAt(0), 4, 0) == "Notes"
            result.getSheetAt(0).mergedRegions*.formatAsString() == ["A5:C8"]
            printArea(result) == "A1:C8"

        where:
            engine << Engine.values()
    }

    def "print area ignores the copies of a merged region that the engine skips [#engine]"() {
        given: "a one-cell notes band in A2 inside the merged region A2:C3, a footer band below the print area A1:C3"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${notes}')
                cell(s, 3, 0, "Footer")
                s.addMergedRegion(CellRangeAddress.valueOf("A2:C3"))
                defineBand(wb, "Header", 0, 0, 0, 2)
                defineBand(wb, "Notes", 1, 0, 1, 0)
                defineBand(wb, "Footer", 3, 0, 3, 2)
                wb.setPrintArea(0, 0, 2, 0, 2)
            }
            def root = rootBand("Header", "Notes", "Footer")
            addBand(root, "Header", [:])
            (1..4).each { addBand(root, "Notes", [notes: "Note " + it]) }
            addBand(root, "Footer", [:])

        when:
            def result = renderWith(engine, template, root)

        then: "the copies around the second and the fourth note overlap others and are skipped, the footer is left out"
            result.getSheetAt(0).mergedRegions*.formatAsString() == ["A2:C3", "A4:C5"]
            stringValue(result.getSheetAt(0), 5, 0) == "Footer"
            printArea(result) == "A1:C5"

        where: "the XLS engine skips a copy that overlaps another merged region"
            engine << [Engine.XLS]
    }

    def "print area of a merged region moved above the first row starts at the first row [#engine]"() {
        given: "a static title, a one-cell notes band in A4 inside the merged region A2:C6, the print area A1:C6"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Title")
                cell(s, 3, 0, '${notes}')
                s.addMergedRegion(CellRangeAddress.valueOf("A2:C6"))
                defineBand(wb, "Notes", 3, 0, 3, 0)
                wb.setPrintArea(0, 0, 2, 0, 5)
            }
            def root = rootBand("Notes")
            addBand(root, "Notes", [notes: "Notes"])

        when:
            def output = render(template, root, engine.outputType, engine.formatter)
            def result = read(output)

        then: "the notes are rendered to row 1; the engine moves the region with them to rows -1..3, a separate issue"
            stringValue(result.getSheetAt(0), 0, 0) == "Notes"
            part(output, "xl/worksheets/sheet1.xml").contains('ref="A-1:C3"')

        and: "the print area covers the region from row 1"
            printArea(result) == "A1:C3"

        where: "the XLS engine fails on such a template"
            engine << [Engine.XLSX]
    }

    def "print area covers a picture that stays in place [#engine]"() {
        given: "a picture anchored in D1:G10 next to the bands, the print area A1:F10 ends before its last anchor column"
            def template = headerAndItemsTemplate(engine, '$A$1:$F$10', SHEET) { Workbook wb ->
                picture(wb, wb.getSheet(SHEET), 3, 0, 6, 9)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then: "the rows of the picture are printed, although the bands end in row 4"
            printArea(result) == "A1:F10"

        where:
            engine << Engine.values()
    }

    def "print area covers a picture that moves with its band [#engine]"() {
        given: "a picture in A3:C7 anchored in the total band, the print area A1:C7 covers it"
            def template = headerItemsAndTotalTemplate(engine) { Workbook wb, Sheet s ->
                picture(wb, s, 0, 2, 2, 6)
            }

        when:
            def result = renderWith(engine, template, headerItemsAndTotal())

        then: "the total is rendered to row 5, the picture is printed where it is rendered"
            stringValue(result.getSheetAt(0), 4, 0) == "Total"
            printArea(result) == expected
            drawings(result).findAll { !isInside(expected, it) } == []

        where: "XLS moves the picture with the band to rows 5-9, XLSX copies it into the band cell of each instance"
            engine      | expected
            Engine.XLS  | "A1:C9"
            Engine.XLSX | "A1:C5"
    }

    def "a picture moved with its band keeps its size [#engine]"() {
        given: "a picture in A3:C7 anchored in the total band"
            def template = headerItemsAndTotalTemplate(engine) { Workbook wb, Sheet s ->
                picture(wb, s, 0, 2, 2, 6)
            }

        when:
            def result = renderWith(engine, template, headerItemsAndTotal())

        then: "the picture is moved with the total to row 5 and spans rows 5-9, columns A-C, as in the template"
            drawings(result).contains("A5:C9")

        where: "the XLS engine moves a picture anchored in a band with the band"
            engine << [Engine.XLS]
    }

    def "print area covers a chart that stays in place [#engine]"() {
        given: "a chart anchored in D1:J13 next to the bands, the print area A1:I12 ends before its last anchor row and column"
            def template = headerAndItemsTemplate(engine, '$A$1:$I$12', SHEET) { Workbook wb ->
                chart((XSSFSheet) wb.getSheet(SHEET), 3, 0, 9, 12)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then: "the rows of the chart inside the print area are printed, although the bands end in row 4"
            printArea(result) == "A1:I12"

        where: "only the XLSX engine renders charts"
            engine << [Engine.XLSX]
    }

    def "print area covers a chart that moves with its band [#engine]"() {
        given: "a chart in A3:F11 anchored in the chart band A3:F3, the print area A1:F11 covers it"
            def template = headerAndItemsTemplate(engine, '$A$1:$F$11', SHEET) { Workbook wb ->
                defineBand(wb, "Chart", 2, 0, 2, 5)
                chart((XSSFSheet) wb.getSheet(SHEET), 0, 2, 5, 10)
            }
            def root = rootBand("Header", "Items", "Chart")
            addBand(root, "Header", [:])
            (1..3).each { addBand(root, "Items", [name: "Item " + it]) }
            addBand(root, "Chart", [:])

        when:
            def result = renderWith(engine, template, root)

        then: "the chart band is rendered to row 5 and the chart with it to rows 5-13"
            drawings(result) == ["A5:F13"]
            printArea(result) == "A1:F13"

        where: "only the XLSX engine renders charts"
            engine << [Engine.XLSX]
    }

    def "print area of a chart moved above the first row starts at the first row [#engine]"() {
        given: "wide header and item bands with two blank rows between them, a chart in E1:J15 over both bands"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 3, 0, '${name}')
                defineBand(wb, "Header", 0, 0, 0, 9)
                defineBand(wb, "Items", 3, 0, 3, 9)
                chart((XSSFSheet) s, 4, 0, 9, 14)
                wb.setPrintArea(0, 0, 9, 0, 14)
            }
            def root = rootBand("Header", "Items")
            addBand(root, "Header", [:])
            (1..5).each { addBand(root, "Items", [name: "Item " + it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "the engine moves the chart once with each band, i.e. up with the item band to rows -1..13, a separate issue"
            drawings(result) == ["E-1:J13"]

        and: "the print area covers the chart from row 1"
            printArea(result) == "A1:J13"

        where: "only the XLSX engine renders charts"
            engine << [Engine.XLSX]
    }

    def "print area covers a shape that stays in place [#engine]"() {
        given: "a text box in A6:C9 below the bands, the print area A1:C10 covers it"
            def template = headerAndItemsTemplate(engine, '$A$1:$C$10', SHEET) { Workbook wb ->
                textBox(wb, wb.getSheet(SHEET), 0, 5, 2, 8)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then: "the rows of the text box are printed, although the bands end in row 4"
            printArea(result) == "A1:C9"

        where:
            engine << Engine.values()
    }

    def "print area ignores cell comments [#engine]"() {
        given: "a comment on A1 shown in C1:E8, the print area A1:E10"
            def template = headerAndItemsTemplate(engine, '$A$1:$E$10', SHEET) { Workbook wb ->
                comment(wb, wb.getSheet(SHEET), 0, 0, 2, 0, 4, 7)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then: "comments are not printed, the print area ends with the bands in row 4"
            printArea(result) == "A1:E4"

        where: "an XLS workbook keeps comments among the drawing objects, XLSX in a separate VML drawing"
            engine << Engine.values()
    }

    def "print area covers a picture that stays in the cell of a picture moved with a band [#engine]"() {
        given: "pictures P1 and P2 in A3:C7 anchored in the total band, a static cell D1 with a picture between them"
            def template = headerItemsAndTotalTemplate(engine) { Workbook wb, Sheet s ->
                cell(s, 0, 3, "Static")
                picture(wb, s, 0, 2, 2, 6)
                picture(wb, s, 3, 0, 5, 2)
                picture(wb, s, 0, 2, 2, 6)
            }

        when:
            def result = renderWith(engine, template, headerItemsAndTotal())

        then: "P1 is copied into the band cell A5; the engine stops copying pictures at the static one, a separate issue"
            drawings(result).sort() == ["A3:C7", "A5:A5", "D1:F3"]

        and: "P2, left in A3:C7, is printed"
            printArea(result) == "A1:C7"

        where: "only the XLSX engine copies pictures into band cells"
            engine << [Engine.XLSX]
    }

    def "print area covers a picture anchored in one cell [#engine]"() {
        given: "a picture anchored in A8 below the bands, sized to fit the cell"
            def template = anchorInOneCell(headerAndItemsTemplate(engine, '$A$1:$C$10', SHEET) { Workbook wb ->
                def s = wb.getSheet(SHEET)
                cell(s, 7, 0, "Static")
                picture(wb, s, 0, 7, 1, 8)
            })

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then: "the row of the picture is printed, although the bands end in row 4"
            printArea(result) == "A1:C8"

        where: "only the XLSX engine reads drawings anchored in one cell"
            engine << [Engine.XLSX]
    }

    def "print area follows a picture anchored in one cell of a band [#engine]"() {
        given: "a total band in A6 below blank rows, a picture anchored in its cell"
            def template = anchorInOneCell(template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${name}')
                cell(s, 5, 0, "Total")
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Items", 1, 0, 1, 1)
                defineBand(wb, "Total", 5, 0, 5, 1)
                picture(wb, s, 0, 5, 1, 6)
                wb.setPrintArea(0, 0, 2, 0, 9)
            })
            def root = rootBand("Header", "Items", "Total")
            addBand(root, "Header", [:])
            addBand(root, "Items", [name: "Item 1"])
            addBand(root, "Total", [:])

        when:
            def result = renderWith(engine, template, root)

        then: "the total and the picture copied into its cell are rendered to row 3, the template row 6 is not printed"
            stringValue(result.getSheetAt(0), 2, 0) == "Total"
            drawings(result) == ["A3:A3"]
            printArea(result) == "A1:C3"

        where: "only the XLSX engine copies pictures into band cells"
            engine << [Engine.XLSX]
    }

    def "print area follows a chart anchored in one cell of a band [#engine]"() {
        given: "a chart band in A6:F6 below blank rows, a chart anchored in its first cell"
            def template = anchorInOneCell(template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Name")
                cell(s, 1, 0, '${name}')
                defineBand(wb, "Header", 0, 0, 0, 1)
                defineBand(wb, "Items", 1, 0, 1, 1)
                defineBand(wb, "Chart", 5, 0, 5, 5)
                chart((XSSFSheet) s, 0, 5, 1, 6)
                wb.setPrintArea(0, 0, 5, 0, 9)
            })
            def root = rootBand("Header", "Items", "Chart")
            addBand(root, "Header", [:])
            addBand(root, "Items", [name: "Item 1"])
            addBand(root, "Chart", [:])

        when:
            def result = renderWith(engine, template, root)

        then: "the chart band and the chart are rendered to row 3, the template row 6 is not printed"
            printArea(result) == "A1:F3"

        where: "only the XLSX engine renders charts"
            engine << [Engine.XLSX]
    }

    def "print area that starts below the first band begins at the first rendered row of its bands [#engine]"() {
        given: "a static title, a header band and an item band; the print area A3:B3 covers only the items"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Title")
                cell(s, 1, 0, "Name")
                cell(s, 2, 0, '${name}')
                defineBand(wb, "Header", 1, 0, 1, 1)
                defineBand(wb, "Items", 2, 0, 2, 1)
                wb.setPrintArea(0, 0, 1, 2, 2)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            printArea(result) == expected

        where: "XLS and XLSX do not render static rows outside bands"
            engine                | expected
            Engine.XLSX           | "A2:B4"
            Engine.XLS            | "A2:B4"
    }

    def "single-cell print area grows with its band [#engine]"() {
        given:
            def template = template(engine) { Workbook wb ->
                cell(sheet(wb), 0, 0, '${name}')
                defineBand(wb, "Items", 0, 0, 0, 0)
                wb.setPrintArea(0, '$A$1')
            }
            def root = rootBand("Items")
            (1..3).each { addBand(root, "Items", [name: "Item " + it]) }

        when:
            def result = renderWith(engine, template, root)

        then:
            printArea(result) == "A1:A3"

        where:
            engine << Engine.values()
    }

    def "print area on a sheet with special characters in its name follows its bands [#engine, #sheetName]"() {
        given:
            def template = headerAndItemsTemplate(engine, '$A$1:$B$2', sheetName)

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            stringValue(result.getSheetAt(0), 3, 0) == "Item 3"
            printArea(result) == "A1:B4"

        where: "the XLSX engine does not resolve band ranges on such sheets at all, a separate issue"
            [engine, sheetName] << [[Engine.XLS], ["O'Brien", 'Q1!$']].combinations()
    }

    def "print area on a sheet with a comma in its name follows its bands [#engine]"() {
        given:
            def template = headerAndItemsTemplate(engine, '$A$1:$B$2', 'North, South')

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            printArea(result) == "A1:B4"

        where:
            engine << Engine.values()
    }

    def "whole-column print area stays whole columns [#engine]"() {
        given: "the print area is the whole columns A:B"
            def template = headerAndItemsTemplate(engine, '$A:$B')

        when:
            def result = renderWith(engine, template, headerAndItems(50))

        then: "whole columns cover every rendered row"
            printArea(result) == "A:B"

        where:
            engine << Engine.values()
    }

    def "whole-row print area grows with the bands inside it [#engine]"() {
        given: "the print area is the whole rows 1:2 of the template"
            def template = headerAndItemsTemplate(engine, '$1:$2')

        when:
            def result = renderWith(engine, template, headerAndItems(50))

        then: "the whole rows 1:51 are printed"
            printArea(result) == "1:51"

        where:
            engine << Engine.values()
    }

    def "print area is written with ASCII digits under any default locale [#engine, #authored]"() {
        given: "the default format locale formats numbers with Arabic-Indic digits"
            def defaultLocale = Locale.getDefault(Locale.Category.FORMAT)
            def template = headerAndItemsTemplate(engine, authored)
            Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag("ar-SA"))

        when:
            def result = renderWith(engine, template, headerAndItems(50))

        then:
            result.getPrintArea(0) ==~ /\p{ASCII}+/
            printArea(result) == expected

        cleanup:
            Locale.setDefault(Locale.Category.FORMAT, defaultLocale)

        where:
            engine                | authored    || expected
            Engine.XLSX           | '$A$1:$B$2' || "A1:B51"
            Engine.XLSX           | '$1:$2'     || "1:51"
            Engine.XLS            | '$A$1:$B$2' || "A1:B51"
    }

    def "print area refers to the sheet renamed from band data [#engine, #title, #authored, #items items]"() {
        given: "the sheet is named \${Root.title}, the root band has the title"
            def template = template(engine) { Workbook wb ->
                cell(sheet(wb, '${Root.title}'), 0, 0, '${name}')
                defineBand(wb, "Items", 0, 0, 0, 0, '${Root.title}')
                wb.setPrintArea(0, authored)
            }
            def root = rootBand("Items")
            root.setData([title: title])
            items.times { addBand(root, "Items", [name: "Item " + (it + 1)]) }

        when:
            def result = renderWith(engine, template, root)

        then: "the print area refers to the renamed sheet, re-based or not"
            result.getSheetName(0) == title
            printArea(result) == expected

        where: "the XLS engine does not rename sheets"
            engine                | title     | authored    | items || expected
            Engine.XLSX           | "Sales"   | '$A$1'      | 3     || "A1:A3"
            Engine.XLSX           | "Sales"   | '$A:$A'     | 3     || "A:A"
            Engine.XLSX           | "Sales"   | '$A$1'      | 0     || "A1"
            Engine.XLSX           | "Sales"   | '$A$1,$C$1' | 3     || "A1,C1"
            Engine.XLSX           | "O'Brien" | '$A$1'      | 3     || "A1:A3"
    }

    def "print area is kept as authored when re-basing it fails [#engine]"() {
        given: "a formatter that fails to compute the rendered bands"
            def template = headerAndItemsTemplate(engine, '$A$1:$B$2')

        when:
            def result = read(render(template, headerAndItems(3), engine.outputType, failingFormatter))

        then: "the report is rendered with the print area of the template"
            stringValue(result.getSheetAt(0), 3, 0) == "Item 3"
            printArea(result) == "A1:B2"

        where:
            engine                | failingFormatter
            Engine.XLSX           | { FormatterFactoryInput input ->
                new XlsxFormatter(input) {
                    @Override
                    protected RangeDependencies getRenderedBandBlocks() {
                        throw new IllegalStateException("Test failure")
                    }
                }
            }
            Engine.XLS            | { FormatterFactoryInput input ->
                new XLSFormatter(input) {
                    @Override
                    protected RangeDependencies getRenderedBandBlocks() {
                        throw new IllegalStateException("Test failure")
                    }
                }
            }
    }

    def "print area without a sheet name follows the bands [#engine]"() {
        given: "the print area is stored as \$A\$1:\$B\$2, without the sheet name"
            def template = headerAndItemsTemplate(engine, '$A$1:$B$2', SHEET) { Workbook wb ->
                setPrintAreaFormula(wb, 0, '$A$1:$B$2')
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            printArea(result) == "A1:B4"

        where: "the XLSX engine cannot render such a template at all, a separate issue"
            engine << [Engine.XLS]
    }

    def "print area without a definition does not break the report [#engine, sheet #broken]"() {
        given: "the print area of one of two sheets has lost its definition"
            def template = twoSheetsTemplate(engine) { Workbook wb ->
                setPrintAreaFormula(wb, broken, null)
            }

        when:
            def result = renderWith(engine, template, twoSheets())

        then: "the print area of the other sheet follows its band"
            printArea(result, 1 - broken) == expected

        where: "only an XLS workbook keeps such a print area"
            engine     | broken || expected
            Engine.XLS | 0      || "A1:A2"
            Engine.XLS | 1      || "A1:A3"
    }

    def "a workbook-level name called Print_Area is left alone [#engine]"() {
        given: "a workbook-level name Print_Area next to the print area of the sheet"
            def template = headerAndItemsTemplate(engine, '$A$1:$B$2', SHEET) { Workbook wb ->
                def name = wb.createName()
                name.nameName = "Print_Area"
                name.refersToFormula = 'Sheet1!$A$1:$B$2'
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            printArea(result) == "A1:B4"
            result.allNames.find { it.nameName == "Print_Area" && it.sheetIndex < 0 }.refersToFormula == 'Sheet1!$A$1:$B$2'

        where: "the XLS engine looks the print areas up among all the names of the workbook"
            engine << [Engine.XLS]
    }

    def "print area defined by a formula is not re-based [#engine, #formula]"() {
        given:
            def template = headerAndItemsTemplate(engine, '$A$1:$B$2', SHEET) { Workbook wb ->
                setPrintAreaFormula(wb, 0, formula)
            }

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then: "the report is rendered, the print area is not changed"
            stringValue(result.getSheetAt(0), 3, 0) == "Item 3"
            result.getPrintArea(0) == expected

        where: "XLS and XLSX keep the formula as authored; an XLS workbook writes whole columns without the dollar signs and cannot store the second formula"
            engine                | formula                                            || expected
            Engine.XLSX           | 'OFFSET(Sheet1!$A$1,0,0,COUNTA(Sheet1!$A:$A),2)'  || 'OFFSET(Sheet1!$A$1,0,0,COUNTA(Sheet1!$A:$A),2)'
            Engine.XLS            | 'OFFSET(Sheet1!$A$1,0,0,COUNTA(Sheet1!$A:$A),2)'  || 'OFFSET(Sheet1!$A$1,0,0,COUNTA(Sheet1!A:A),2)'
            Engine.XLSX           | 'Sheet1!$A$1:Sheet1!$B$2'                          || 'Sheet1!$A$1:Sheet1!$B$2'
    }

    def "print area of several areas is not re-based [#engine]"() {
        given:
            def template = headerAndItemsTemplate(engine, '$A$1:$B$2,$D$1:$E$2')

        when:
            def result = renderWith(engine, template, headerAndItems(3))

        then:
            printArea(result) == expected

        where: "XLS and XLSX keep such a print area as authored"
            engine                || expected
            Engine.XLSX           || "A1:B2,D1:E2"
            Engine.XLS            || "A1:B2,D1:E2"
    }

    def "no print area in the template means none in the result [#engine]"() {
        given:
            def template = template(engine) { Workbook wb ->
                cell(sheet(wb), 0, 0, '${name}')
                defineBand(wb, "Items", 0, 0, 0, 0)
            }
            def root = rootBand("Items")
            (1..3).each { addBand(root, "Items", [name: "Item " + it]) }

        when:
            def result = renderWith(engine, template, root)

        then:
            result.getPrintArea(0) == null

        where:
            engine << Engine.values()
    }

    def "print area grows to the right with a vertical band [#engine]"() {
        given: "a vertical band in A1 inside the print area A1:A1"
            def template = template(engine) { Workbook wb ->
                cell(sheet(wb), 0, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 0)
                wb.setPrintArea(0, 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            ["x", "y", "z"].each { verticalBand(root, "Data", [value: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "the three instances are rendered into A1:C1"
            printArea(result) == "A1:C1"

        where:
            engine << Engine.values()
    }

    def "print area follows a vertical band rendered left of its template column [#engine]"() {
        given: "vertical bands A in A1 and B in D1; the print area D1:E1 covers band B and the blank column right of it"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${a}')
                cell(s, 0, 3, '${b}')
                defineBand(wb, "A", 0, 0, 0, 0)
                defineBand(wb, "B", 0, 3, 0, 3)
                wb.setPrintArea(0, 3, 4, 0, 0)
            }
            def root = rootBand("A", "B")
            ["a1", "a2"].each { verticalBand(root, "A", [a: it]) }
            ["b1", "b2"].each { verticalBand(root, "B", [b: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "band B is rendered to C:D right of band A, the blank column E right of it is kept"
            stringValue(result.getSheetAt(0), bRow, 2) == "b1"
            printArea(result) == expected

        where: "XLSX lays band B out in the row of band A, XLS in the next row"
            engine      || bRow | expected
            Engine.XLSX || 0    | "C1:E1"
            Engine.XLS  || 1    | "C2:E2"
    }

    def "print area follows a vertical band pushed right by a band outside it [#engine]"() {
        given: "vertical bands V in A1 and W in B1; the print area B1:B1 covers band W"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${v}')
                cell(s, 0, 1, '${w}')
                defineBand(wb, "V", 0, 0, 0, 0)
                defineBand(wb, "W", 0, 1, 0, 1)
                wb.setPrintArea(0, 1, 1, 0, 0)
            }
            def root = rootBand("V", "W")
            ["v1", "v2", "v3"].each { verticalBand(root, "V", [v: it]) }
            ["w1", "w2"].each { verticalBand(root, "W", [w: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "band W is rendered to columns D:E right of band V, which is not printed"
            stringValue(result.getSheetAt(0), wRow, 3) == "w1"
            printArea(result) == expected

        where: "XLSX lays band W out in the row of band V, XLS in the next row"
            engine      || wRow | expected
            Engine.XLSX || 0    | "D1:E1"
            Engine.XLS  || 1    | "D2:E2"
    }

    def "print area does not spread over a band packed into its left margin [#engine]"() {
        given: "vertical bands A in A1 and B in D1; the print area C1:E1 covers band B and the blank columns around it"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${a}')
                cell(s, 0, 3, '${b}')
                defineBand(wb, "A", 0, 0, 0, 0)
                defineBand(wb, "B", 0, 3, 0, 3)
                wb.setPrintArea(0, 2, 4, 0, 0)
            }
            def root = rootBand("A", "B")
            ["a1", "a2"].each { verticalBand(root, "A", [a: it]) }
            ["b1", "b2"].each { verticalBand(root, "B", [b: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "band B is rendered to C:D, the blank column left of it would hold a2 in XLSX"
            stringValue(result.getSheetAt(0), bRow, 2) == "b1"
            printArea(result) == expected

        where: "XLSX lays band B out in the row of band A, XLS in the next row"
            engine      || bRow | expected
            Engine.XLSX || 0    | "C1:E1"
            Engine.XLS  || 1    | "B2:E2"
    }

    def "print area does not spread over a band packed into its right margin [#engine]"() {
        given: "vertical bands V in A1 and W in D1; the print area A1:C1 covers band V and the blank columns right of it"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${v}')
                cell(s, 0, 3, '${w}')
                defineBand(wb, "V", 0, 0, 0, 0)
                defineBand(wb, "W", 0, 3, 0, 3)
                wb.setPrintArea(0, 0, 2, 0, 0)
            }
            def root = rootBand("V", "W")
            ["v1", "v2"].each { verticalBand(root, "V", [v: it]) }
            ["w1", "w2"].each { verticalBand(root, "W", [w: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "band V is rendered to A:B, band W to C:D"
            stringValue(result.getSheetAt(0), wRow, 2) == "w1"
            printArea(result) == expected

        where: "XLSX lays band W out in the row of band V, so the blank columns give way to it; XLS in the next row"
            engine      || wRow | expected
            Engine.XLSX || 0    | "A1:B1"
            Engine.XLS  || 1    | "A1:D1"
    }

    def "print area does not grow left of column A with a band moved left [#engine]"() {
        given: "vertical bands A in A1 and B in D1; the print area B1:D1 covers band B and the blank columns left of it"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${a}')
                cell(s, 0, 3, '${b}')
                defineBand(wb, "A", 0, 0, 0, 0)
                defineBand(wb, "B", 0, 3, 0, 3)
                wb.setPrintArea(0, 1, 3, 0, 0)
            }
            def root = rootBand("A", "B")
            verticalBand(root, "A", [a: "a1"])
            ["b1", "b2"].each { verticalBand(root, "B", [b: it]) }

        when:
            def result = renderWith(engine, template, root)

        then: "band B is rendered to B2:C2, two columns left of its template column, so its blank columns end at column A"
            stringValue(result.getSheetAt(0), 1, 1) == "b1"
            printArea(result) == "A2:C2"

        where: "the XLS engine lays band B out in the next row, next to band A"
            engine << [Engine.XLS]
    }

    def "print area keeps a picture in its columns while its bands are packed left [#engine]"() {
        given: "vertical bands A in A1 and B in E1, a static picture in F1:H10; the print area A1:H10 covers them"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${a}')
                cell(s, 0, 4, '${b}')
                defineBand(wb, "A", 0, 0, 0, 0)
                defineBand(wb, "B", 0, 4, 0, 4)
                picture(wb, s, 5, 0, 7, 9)
                wb.setPrintArea(0, 0, 7, 0, 9)
            }
            def root = rootBand("A", "B")
            ["a1", "a2"].each { verticalBand(root, "A", [a: it]) }
            verticalBand(root, "B", [b: "b1"])

        when:
            def result = renderWith(engine, template, root)

        then: "band B is rendered to column C, the picture stays in F1:H10 and is printed"
            stringValue(result.getSheetAt(0), bRow, 2) == "b1"
            printArea(result) == "A1:H10"

        where: "XLSX lays band B out in the row of band A, XLS in the next row"
            engine      || bRow
            Engine.XLSX || 0
            Engine.XLS  || 1
    }

    def "print area keeps the part of a wider band in its columns while its bands are pushed right [#engine]"() {
        given: "vertical bands V in A1 and W in B1, horizontal band H in A3:C3; the area B1:C3 covers W and part of H"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${v}')
                cell(s, 0, 1, '${w}')
                cell(s, 2, 0, '${h1}')
                cell(s, 2, 1, '${h2}')
                cell(s, 2, 2, '${h3}')
                defineBand(wb, "V", 0, 0, 0, 0)
                defineBand(wb, "W", 0, 1, 0, 1)
                defineBand(wb, "H", 2, 0, 2, 2)
                wb.setPrintArea(0, 1, 2, 0, 2)
            }
            def root = rootBand("V", "W", "H")
            ["v1", "v2", "v3"].each { verticalBand(root, "V", [v: it]) }
            verticalBand(root, "W", [w: "w1"])
            addBand(root, "H", [h1: "h1", h2: "h2", h3: "h3"])

        when:
            def result = renderWith(engine, template, root)

        then: "W is pushed to column D, the cells h2 and h3 of H stay in B:C and are printed"
            stringValue(result.getSheetAt(0), hRow, 2) == "h3"
            printArea(result) == expected

        where: "XLSX lays band W out in the row of band V, XLS in the next row"
            engine      || hRow | expected
            Engine.XLSX || 1    | "B1:D2"
            Engine.XLS  || 2    | "B2:D3"
    }

    def "whole-row print area stays whole rows while its bands are packed left [#engine]"() {
        given: "vertical bands V in A1 and W in D1, the print area is the whole row 1"
            def template = template(engine) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, '${v}')
                cell(s, 0, 3, '${w}')
                defineBand(wb, "V", 0, 0, 0, 0)
                defineBand(wb, "W", 0, 3, 0, 3)
                wb.setPrintArea(0, '$1:$1')
            }
            def root = rootBand("V", "W")
            ["v1", "v2"].each { verticalBand(root, "V", [v: it]) }
            verticalBand(root, "W", [w: "w1"])

        when:
            def result = renderWith(engine, template, root)

        then: "band W is rendered to column C"
            printArea(result) == expected

        where: "XLSX lays band W out in the row of band V, XLS in the next row"
            engine      || expected
            Engine.XLSX || "1:1"
            Engine.XLS  || "1:2"
    }

    def "print area follows the bands of a formatter that resolves their ranges itself [#engine]"() {
        given: "the item band is defined by the name Items_range, which the formatter resolves"
            def template = template(engine) { Workbook wb ->
                cell(sheet(wb), 0, 0, '${name}')
                defineBand(wb, "Items_range", 0, 0, 0, 0)
                wb.setPrintArea(0, 0, 0, 0, 0)
            }
            def root = rootBand("Items")
            (1..3).each { addBand(root, "Items", [name: "Item " + it]) }

        when:
            def result = read(render(template, root, engine.outputType) { FormatterFactoryInput input ->
                new XlsxFormatter(input) {
                    @Override
                    protected Range getBandRange(BandData band) {
                        def name = this.template.getDefinedName(band.name + "_range")
                        return name != null ? Range.fromFormula(name.value) : null
                    }
                }
            })

        then:
            stringValue(result.getSheetAt(0), 2, 0) == "Item 3"
            printArea(result) == "A1:A3"

        where: "the XLSX engine resolves band ranges in a protected method"
            engine << [Engine.XLSX]
    }

    def "print area never grows beyond the last column of the sheet [#engine]"() {
        given: "a vertical band in B1 inside a print area that ends at or near the last column"
            def template = template(engine) { Workbook wb ->
                cell(sheet(wb), 0, 1, '${value}')
                defineBand(wb, "Data", 0, 1, 0, 1)
                wb.setPrintArea(0, authored)
            }
            def root = rootBand("Data")
            ["x", "y", "z", "u", "v"].each { verticalBand(root, "Data", [value: it]) }

        when:
            def result = renderWith(engine, template, root)

        then:
            printArea(result) == expected

        where: "the XLS print area is a whole row, the XLSX one ends two columns before the last one"
            engine      | authored      || expected
            Engine.XLS  | '$1:$1'       || "1:1"
            Engine.XLSX | '$B$1:$XFB$1' || "B1:XFD1"
    }

    def "each sheet's print area follows the bands of that sheet [#engine]"() {
        given: "one band and a print area on each of two sheets"
            def template = twoSheetsTemplate(engine)

        when:
            def result = renderWith(engine, template, twoSheets())

        then:
            printArea(result, 0) == "A1:A3"
            printArea(result, 1) == "A1:A2"

        where:
            engine << Engine.values()
    }

    protected byte[] template(Engine engine, Closure configure) {
        return buildTemplate(engine.templateWorkbook.call(), configure)
    }

    /**
     * A template with a header band in A1:B1 and an item band in A2:B2 on one sheet, and the given print area;
     * the closure may change the template further.
     */
    protected byte[] headerAndItemsTemplate(Engine engine, String printArea, String sheetName = SHEET,
                                            Closure customize = {}) {
        return template(engine) { Workbook wb ->
            def s = sheet(wb, sheetName)
            cell(s, 0, 0, "Name")
            cell(s, 1, 0, '${name}')
            defineBand(wb, "Header", 0, 0, 0, 1, sheetName)
            defineBand(wb, "Items", 1, 0, 1, 1, sheetName)
            wb.setPrintArea(0, printArea)
            customize.call(wb)
        }
    }

    /** Band data for {@link #headerAndItemsTemplate}: one header and the given number of items. */
    protected BandData headerAndItems(int items) {
        def root = rootBand("Header", "Items")
        addBand(root, "Header", [:])
        items.times { addBand(root, "Items", [name: "Item " + (it + 1)]) }
        return root
    }

    /**
     * A {@link #headerAndItemsTemplate} with a total band in A3:B3 below the items and the print area A1:C7; the
     * closure receives the workbook and the sheet to change the template further.
     */
    protected byte[] headerItemsAndTotalTemplate(Engine engine, Closure customize) {
        return headerAndItemsTemplate(engine, '$A$1:$C$7', SHEET) { Workbook wb ->
            def s = wb.getSheet(SHEET)
            cell(s, 2, 0, "Total")
            defineBand(wb, "Total", 2, 0, 2, 1)
            customize.call(wb, s)
        }
    }

    /** Band data for {@link #headerItemsAndTotalTemplate}: one header, three items and the total. */
    protected BandData headerItemsAndTotal() {
        def root = headerAndItems(3)
        root.setFirstLevelBandDefinitionNames(["Header", "Items", "Total"] as Set)
        addBand(root, "Total", [:])
        return root
    }

    /**
     * A template with the sheets First and Second, each with a band in A1 and the print area A1; the closure may
     * change the template further.
     */
    protected byte[] twoSheetsTemplate(Engine engine, Closure customize = {}) {
        return template(engine) { Workbook wb ->
            cell(sheet(wb, "First"), 0, 0, '${value}')
            cell(sheet(wb, "Second"), 0, 0, '${value}')
            defineBand(wb, "FirstItems", 0, 0, 0, 0, "First")
            defineBand(wb, "SecondItems", 0, 0, 0, 0, "Second")
            wb.setPrintArea(0, 0, 0, 0, 0)
            wb.setPrintArea(1, 0, 0, 0, 0)
            customize.call(wb)
        }
    }

    /** Band data for {@link #twoSheetsTemplate}: three instances of the first band and two of the second one. */
    protected BandData twoSheets() {
        def root = rootBand("FirstItems", "SecondItems")
        (1..3).each { addBand(root, "FirstItems", [value: "first " + it]) }
        (1..2).each { addBand(root, "SecondItems", [value: "second " + it]) }
        return root
    }

    /** Adds a picture anchored in the given cells (0-based, inclusive) to the sheet. */
    protected void picture(Workbook wb, Sheet sheet, int col1, int row1, int col2, int row2) {
        def image = new ByteArrayOutputStream()
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png", image)
        def pictureIndex = wb.addPicture(image.toByteArray(), Workbook.PICTURE_TYPE_PNG)
        sheet.createDrawingPatriarch().createPicture(anchor(wb, col1, row1, col2, row2), pictureIndex)
    }

    /** Adds a text box anchored in the given cells (0-based, inclusive) to the sheet. */
    protected void textBox(Workbook wb, Sheet sheet, int col1, int row1, int col2, int row2) {
        sheet.createDrawingPatriarch().createTextbox(anchor(wb, col1, row1, col2, row2))
    }

    /** Adds a comment to the cell (0-based), shown in the given cells (0-based, inclusive). */
    protected void comment(Workbook wb, Sheet sheet, int row, int col, int col1, int row1, int col2, int row2) {
        def comment = sheet.createDrawingPatriarch().createCellComment(anchor(wb, col1, row1, col2, row2))
        comment.string = wb.creationHelper.createRichTextString("Comment")
        sheet.getRow(row).getCell(col).cellComment = comment
    }

    protected ClientAnchor anchor(Workbook wb, int col1, int row1, int col2, int row2) {
        def anchor = wb.creationHelper.createClientAnchor()
        anchor.col1 = col1
        anchor.row1 = row1
        anchor.col2 = col2
        anchor.row2 = row2
        return anchor
    }

    /** Adds a bar chart of the item cells A2:B2 anchored in the given cells (0-based, inclusive) to the sheet. */
    protected void chart(XSSFSheet sheet, int col1, int row1, int col2, int row2) {
        def drawing = sheet.createDrawingPatriarch()
        def chart = drawing.createChart(drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2))
        def data = chart.createData(ChartTypes.BAR,
                chart.createCategoryAxis(AxisPosition.BOTTOM), chart.createValueAxis(AxisPosition.LEFT))
        data.addSeries(XDDFDataSourcesFactory.fromStringCellRange(sheet, CellRangeAddress.valueOf("A2:A2")),
                XDDFDataSourcesFactory.fromNumericCellRange(sheet, CellRangeAddress.valueOf("B2:B2")))
        chart.plot(data)
    }

    /**
     * Anchors the drawing objects of the first sheet of an XLSX template in their first cell and sizes them to fit
     * it, as some applications write them.
     */
    protected byte[] anchorInOneCell(byte[] template) {
        return rewritePart(template, "xl/drawings/drawing1.xml") { String xml ->
            xml.replaceAll(/<xdr:twoCellAnchor[^>]*>/, '<xdr:oneCellAnchor>')
                    .replaceAll(/(?s)<xdr:to>.*?<\/xdr:to>/, '<xdr:ext cx="95250" cy="95250"/>')
                    .replace('</xdr:twoCellAnchor>', '</xdr:oneCellAnchor>')
        }
    }

    /** The content of a part of an XLSX document, e.g. {@code xl/worksheets/sheet1.xml}. */
    protected String part(byte[] xlsx, String name) {
        def zip = new ZipInputStream(new ByteArrayInputStream(xlsx))
        try {
            ZipEntry entry
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.name == name) {
                    return new String(zip.readAllBytes(), StandardCharsets.UTF_8)
                }
            }
        } finally {
            zip.close()
        }
        throw new AssertionError("No part $name")
    }

    protected byte[] rewritePart(byte[] xlsx, String name, Closure<String> rewrite) {
        def input = new ZipInputStream(new ByteArrayInputStream(xlsx))
        def bytes = new ByteArrayOutputStream()
        def output = new ZipOutputStream(bytes)
        try {
            ZipEntry entry
            while ((entry = input.getNextEntry()) != null) {
                byte[] content = input.readAllBytes()
                if (entry.name == name) {
                    content = rewrite.call(new String(content, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8)
                }
                output.putNextEntry(new ZipEntry(entry.name))
                output.write(content)
                output.closeEntry()
            }
        } finally {
            input.close()
            output.close()
        }
        return bytes.toByteArray()
    }

    /**
     * Replaces the formula of the sheet's print area; {@code null} leaves an XLS print area without a definition.
     */
    protected void setPrintAreaFormula(Workbook wb, int sheetIndex, String formula) {
        if (wb instanceof HSSFWorkbook) {
            // HSSFWorkbook.getAllNames() lists a print area only once the workbook is read again
            def name = wb.internalWorkbook.getSpecificBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex + 1)
            assert name != null: "Sheet $sheetIndex has no print area"
            name.nameDefinition = formula != null
                    ? HSSFFormulaParser.parse(formula, wb, FormulaType.NAMEDRANGE, sheetIndex)
                    : new Ptg[0]
        } else {
            def name = wb.getAllNames().find { it.nameName == '_xlnm.Print_Area' && it.sheetIndex == sheetIndex }
            assert name != null: "Sheet $sheetIndex has no print area"
            name.setRefersToFormula(formula)
        }
    }

    protected Workbook renderWith(Engine engine, byte[] template, BandData root) {
        return read(render(template, root, engine.outputType, engine.formatter))
    }

    /**
     * The print area of the sheet as sheet-less references: {@code A1:B2}, {@code 1:20} for whole rows, {@code A:F}
     * for whole columns, several areas separated with commas; {@code null} if the sheet has none. The print area is
     * parsed strictly, so that a sheet name quoted or escaped wrongly fails the test, and must refer to that very
     * sheet.
     */
    protected String printArea(Workbook workbook, int sheetIndex = 0) {
        def formula = workbook.getPrintArea(sheetIndex)
        if (formula == null) {
            return null
        }
        def version = workbook.spreadsheetVersion
        return AreaReference.generateContiguous(version, formula).collect { AreaReference area ->
            assert area.firstCell.sheetName == workbook.getSheetName(sheetIndex)
            def address = new CellRangeAddress(area.firstCell.row, area.lastCell.row,
                    area.firstCell.col, area.lastCell.col)
            // an XLS workbook stores whole rows as a rectangle spanning all the columns
            if (address.firstColumn == 0 && address.lastColumn == version.lastColumnIndex) {
                address = new CellRangeAddress(address.firstRow, address.lastRow, -1, -1)
            }
            return address.formatAsString()
        }.join(",")
    }

    /** The cells the drawing objects of the sheet are anchored in, e.g. {@code A3:C7}; rows above row 1 as is. */
    protected List<String> drawings(Workbook workbook, int sheetIndex = 0) {
        def patriarch = workbook.getSheetAt(sheetIndex).drawingPatriarch
        def shapes = patriarch instanceof HSSFPatriarch ? patriarch.children : patriarch.shapes
        return shapes.collect { shape ->
            ClientAnchor anchor = shape.anchor
            "${cellName(anchor.col1, anchor.row1)}:${cellName(anchor.col2, anchor.row2)}".toString()
        }
    }

    protected String cellName(int col, int row) {
        return CellReference.convertNumToColString(col) + (row + 1)
    }

    /** Whether the range, e.g. {@code A3:C7}, lies inside the area, e.g. {@code A1:C9}. */
    protected boolean isInside(String area, String range) {
        def outer = CellRangeAddress.valueOf(area)
        def inner = CellRangeAddress.valueOf(range)
        return outer.isInRange(inner.firstRow, inner.firstColumn) && outer.isInRange(inner.lastRow, inner.lastColumn)
    }
}
