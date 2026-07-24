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

package xlsx.streaming

import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellRangeAddressList
import xlsx.StreamingBaseXlsxRenderTest

/**
 * Sheet- and workbook-level template settings that the streaming engine must carry over to the result
 * document (the non-streaming engine renders on a template copy and keeps them for free): freeze panes,
 * page/print setup, user-defined names and the print area.
 */
class StreamingXlsxTemplateSettingsTest extends StreamingBaseXlsxRenderTest {

    def "freeze pane is copied from the template"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, 'header')
            s.createFreezePane(0, 1)
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [:])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        sheet.paneInformation != null
        sheet.paneInformation.horizontalSplitPosition == (1 as short)
    }

    def "landscape page setup is copied from the template"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            s.printSetup.landscape = true
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        sheet.printSetup.landscape
    }

    def "user-defined name is copied so formulas referencing it do not break"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            def name = wb.createName()
            name.setNameName("TaxRate")
            name.setRefersToFormula("0.2")
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def workbook = read(render(template, root))

        then:
        workbook.getName("TaxRate") != null
        workbook.getName("TaxRate").refersToFormula == "0.2"
    }

    def "data validation is copied from the template"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            def helper = s.dataValidationHelper
            def constraint = helper.createExplicitListConstraint(["A", "B", "C"] as String[])
            def validation = helper.createValidation(constraint, new CellRangeAddressList(0, 0, 0, 0))
            s.addValidationData(validation)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        sheet.dataValidations.size() >= 1
    }

    def "custom width of a column with no cell is copied"() {
        given: "column E has a custom width but no cell in any row"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            s.setColumnWidth(4, 5000)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        sheet.getColumnWidth(4) == 5000
    }

    def "hidden column is copied from the template"() {
        given: "column C is hidden in the template"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            s.setColumnHidden(2, true)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the hidden flag is carried over, not just the width"
        sheet.isColumnHidden(2)
    }

    def "column-level default style is copied from the template"() {
        given: "column C carries a column-level number format applied to no individual cell"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            def style = wb.createCellStyle()
            style.setDataFormat(wb.createDataFormat().getFormat("0.00%"))
            s.setDefaultColumnStyle(2, style)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the column default style survives with its number format"
        sheet.getColumnStyle(2).getDataFormatString() == "0.00%"
    }

    def "column-level styles on several columns are all copied"() {
        given: "two different columns each carrying its own column-level number format"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            def percent = wb.createCellStyle()
            percent.setDataFormat(wb.createDataFormat().getFormat("0.00%"))
            def money = wb.createCellStyle()
            money.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"))
            s.setDefaultColumnStyle(2, percent)
            s.setDefaultColumnStyle(4, money)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "every styled column keeps its own format — all <col> definitions are processed, not just the first"
        sheet.getColumnStyle(2).getDataFormatString() == "0.00%"
        sheet.getColumnStyle(4).getDataFormatString() == "#,##0.00"
    }

    def "print area is copied from the template"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            wb.setPrintArea(0, 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def workbook = read(render(template, root))

        then:
        workbook.getPrintArea(0) != null
    }

    def "band row height is carried to every rendered instance"() {
        given: "the band's template row has a custom height"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            def row = s.createRow(0)
            row.setHeightInPoints(42f)
            row.createCell(0).setCellValue('${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: "x"])
        addBand(root, "Data", [v: "y"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "each rendered band instance keeps the template row height, not the sheet default"
        sheet.getRow(0).getHeightInPoints() == 42f
        sheet.getRow(1).getHeightInPoints() == 42f
    }

    def "a hidden static template row stays hidden in the output"() {
        given: "a static footer row below the band is hidden in the template"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            s.createRow(0).createCell(0).setCellValue('${v}')
            def footer = s.createRow(1)
            footer.setZeroHeight(true)
            footer.createCell(0).setCellValue('footer')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: "x"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the static footer row keeps its hidden flag instead of becoming visible"
        sheet.getRow(1).getZeroHeight()
    }

    def "page header and footer placeholders are substituted from band data"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            s.header.center = 'Report ${Root.title}'
            s.footer.right = 'Page for ${Root.title}'
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        root.setData([title: "Q3"])
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the \${...} aliases in the page header/footer are resolved, not printed literally"
        sheet.header.center == "Report Q3"
        sheet.footer.right == "Page for Q3"
    }

    def "repeating rows (print titles) are carried to the output"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, 'Header')
            cell(s, 1, 0, '${v}')
            defineBand(wb, "Data", 1, 0, 1, 0)
            s.setRepeatingRows(CellRangeAddress.valueOf("1:1"))
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the print-titles repeating rows survive instead of being dropped"
        sheet.getRepeatingRows() != null
        sheet.getRepeatingRows().formatAsString() == "1:1"
    }

    def "print centering flags are carried to the output"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            s.setHorizontallyCenter(true)
            s.setVerticallyCenter(true)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        sheet.getHorizontallyCenter()
        sheet.getVerticallyCenter()
    }
}
