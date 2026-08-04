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

import io.jmix.reports.entity.ReportTemplate
import io.jmix.reports.yarg.exception.ReportFormattingException
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.StreamingXlsxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.BandOrientation
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.ss.usermodel.ComparisonOperator
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress
import xlsx.StreamingBaseXlsxRenderTest

/**
 * Structural correctness of the streaming XLSX formatter: static content, sheet naming,
 * merged regions, workbook well-formedness.
 */
class StreamingXlsxStructureTest extends StreamingBaseXlsxRenderTest {

    def "static header text is preserved and workbook reopens"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'Report title')
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data")

        when:
        Sheet sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == 'Report title'
    }

    def "sheet-name placeholder is substituted from root band data"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = wb.createSheet('${Root.title}')
            cell(sheet, 0, 0, 'Header')
            defineBand(wb, "Data", 1, 0, 1, 0, '${Root.title}')
        }
        def root = rootBand("Data")
        root.setData([title: "Q3"])

        when:
        def bytes = render(template, root)
        def wb = read(bytes)

        then:
        wb.getSheetName(0) == "Q3"
        wb.getSheetAt(0).getRow(0).getCell(0).stringCellValue == "Header"
    }

    def "merged region inside a band is replicated and shifted per instance"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1))
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [n: "A"])
        addBand(root, "Data", [n: "B"])

        when:
        def workbook = read(render(template, root))
        def merges = workbook.getSheetAt(0).getMergedRegions() as Set

        then:
        merges.contains(new CellRangeAddress(0, 0, 0, 1))
        merges.contains(new CellRangeAddress(1, 1, 0, 1))
    }

    def "merged region on a static row above a band is preserved"() {
        given: "a merged title on a static header row above the band"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'Report')
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1))
            defineBand(wb, "Data", 1, 0, 1, 1)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [:])

        when:
        def workbook = read(render(template, root))
        def merges = workbook.getSheetAt(0).getMergedRegions() as Set

        then: "the static merge is carried into the streamed output, not dropped"
        merges.contains(new CellRangeAddress(0, 0, 0, 1))
    }

    def "merged region on a static row below a band shifts to its output row"() {
        given: "a merged footer on the static row below the band"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            cell(sheet, 1, 0, 'Footer')
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1))
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "the band renders rows 0..2, the footer shifts to output row 3"
        def workbook = read(render(template, root))
        def merges = workbook.getSheetAt(0).getMergedRegions() as Set

        then: "the merged footer moves to its actual output row 3, not the template row 1"
        merges.contains(new CellRangeAddress(3, 3, 0, 1))
        !merges.contains(new CellRangeAddress(1, 1, 0, 1))
    }

    def "a multi-row static merge whose lower rows are empty is shifted whole, not inverted"() {
        given: "a data band on row 0 and a 2-row merged footer (rows 1-2) whose lower row has no cell"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            cell(sheet, 1, 0, 'Footer')
            // the box spans rows 1-2 but only its top cell exists, so row 2 sits below getLastRowNum()
            sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 1))
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        (1..5).each { addBand(root, "Data", [v: it]) }

        when: "5 instances render rows 0..4, pushing the 2-row footer box to output rows 5..6"
        def workbook = read(render(template, root))
        def merges = workbook.getSheetAt(0).getMergedRegions() as Set

        then: "both endpoints shift together (no inverted range crash, no collapsed box)"
        merges.contains(new CellRangeAddress(5, 6, 0, 1))
    }

    def "row outline level is copied onto rendered band rows"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            sheet.groupRow(0, 0)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [n: "A"])
        addBand(root, "Data", [n: "B"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        sheet.getRow(0).getOutlineLevel() == 1
        sheet.getRow(1).getOutlineLevel() == 1
    }

    def "conditional formatting is applied over the rendered band rows"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            def scf = sheet.getSheetConditionalFormatting()
            def rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "5")
            scf.addConditionalFormatting([new CellRangeAddress(0, 0, 0, 0)] as CellRangeAddress[], rule)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])
        addBand(root, "Data", [v: 9])
        addBand(root, "Data", [v: 3])

        when:
        def sheet = renderAndReadFirstSheet(template, root)
        def scf = sheet.getSheetConditionalFormatting()
        def ranges = (0..<scf.numConditionalFormattings).collectMany {
            scf.getConditionalFormattingAt(it).formattingRanges as List
        }

        then: "exactly one single-row range per rendered instance — not collapsed to one span, no extras"
        ranges.contains(new CellRangeAddress(0, 0, 0, 0))
        ranges.contains(new CellRangeAddress(1, 1, 0, 0))
        ranges.contains(new CellRangeAddress(2, 2, 0, 0))
        ranges.size() == 3
    }

    def "conditional formatting wider than the band is replicated over the rendered rows"() {
        given: "a CF rule one column wider than the single-column band"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            def scf = sheet.getSheetConditionalFormatting()
            def rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "5")
            scf.addConditionalFormatting([new CellRangeAddress(0, 0, 0, 1)] as CellRangeAddress[], rule)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)
        def scf = sheet.getSheetConditionalFormatting()
        def ranges = (0..<scf.numConditionalFormattings).collectMany {
            scf.getConditionalFormattingAt(it).formattingRanges as List
        }

        then: "the rule is replicated for each rendered row, keeping its two-column width"
        ranges.contains(new CellRangeAddress(0, 0, 0, 1))
        ranges.contains(new CellRangeAddress(1, 1, 0, 1))
        ranges.contains(new CellRangeAddress(2, 2, 0, 1))
    }

    def "conditional formatting on a static row below a band follows it to the output row"() {
        given: "a CF rule on a footer static row placed below the band"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            cell(sheet, 1, 0, 'footer')
            def scf = sheet.getSheetConditionalFormatting()
            def rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "5")
            scf.addConditionalFormatting([new CellRangeAddress(1, 1, 0, 0)] as CellRangeAddress[], rule)
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "the band renders rows 0..2, the footer shifts to row 3"
        def sheet = renderAndReadFirstSheet(template, root)
        def scf = sheet.getSheetConditionalFormatting()
        def ranges = (0..<scf.numConditionalFormattings).collectMany {
            scf.getConditionalFormattingAt(it).formattingRanges as List
        }

        then: "the CF range moves to the footer's actual output row 3, not the template row 1"
        ranges.contains(new CellRangeAddress(3, 3, 0, 0))
        !ranges.contains(new CellRangeAddress(1, 1, 0, 0))
    }

    def "conditional formatting spanning a band into a static row re-bases the in-band endpoint after rows shift"() {
        given: "Above expands (2 rows) pushing Data down; a CF rule runs from Data's row into the footer below"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${a}')
            cell(sheet, 1, 0, '${v}')
            cell(sheet, 2, 0, 'footer')
            def scf = sheet.getSheetConditionalFormatting()
            def rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "5")
            // A2:A3 starts inside the Data band (template row 1) and ends on the footer static row
            // (template row 2), so it is a partial overlap — not fully contained in any band.
            scf.addConditionalFormatting([new CellRangeAddress(1, 2, 0, 0)] as CellRangeAddress[], rule)
            defineBand(wb, "Above", 0, 0, 0, 0)
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Above", "Data")
        addBand(root, "Above", [a: 1])
        addBand(root, "Above", [a: 2])
        addBand(root, "Data", [v: 9])

        when: "Above renders output rows 0..1, Data row 2, footer row 3"
        def sheet = renderAndReadFirstSheet(template, root)
        def scf = sheet.getSheetConditionalFormatting()
        def ranges = (0..<scf.numConditionalFormattings).collectMany {
            scf.getConditionalFormattingAt(it).formattingRanges as List
        }

        then: "the in-band start endpoint follows Data to output row 2, the footer endpoint to row 3 -> A3:A4"
        ranges.contains(new CellRangeAddress(2, 3, 0, 0))
        !ranges.contains(new CellRangeAddress(1, 3, 0, 0))
    }

    def "conditional formatting over a static row and an empty band keeps the rule on the static row"() {
        given: "a CF rule spanning a static header row and the Data band, which renders no rows"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'header')
            cell(sheet, 1, 0, '${v}')
            def scf = sheet.getSheetConditionalFormatting()
            def rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "5")
            scf.addConditionalFormatting([new CellRangeAddress(0, 1, 0, 0)] as CellRangeAddress[], rule)
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data") // no Data rows -> empty band

        when:
        def sheet = renderAndReadFirstSheet(template, root)
        def scf = sheet.getSheetConditionalFormatting()
        def ranges = (0..<scf.numConditionalFormattings).collectMany {
            scf.getConditionalFormattingAt(it).formattingRanges as List
        }

        then: "the rule is kept and re-based to A1:A2 (static header out-row 0 .. empty-band endpoint row 1)"
        ranges.contains(new CellRangeAddress(0, 1, 0, 0))
    }

    def "conditional formatting straddling a static row and an empty band below never produces an inverted range"() {
        given: "Top expands (pushing the footer down); a CF rule spans the footer and an EMPTY Data band below it"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${t}')
            cell(s, 1, 0, 'footer')
            cell(s, 2, 0, '${d}')
            def scf = s.getSheetConditionalFormatting()
            def rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "5")
            scf.addConditionalFormatting([new CellRangeAddress(1, 2, 0, 0)] as CellRangeAddress[], rule)
            defineBand(wb, "Top", 0, 0, 0, 0)
            defineBand(wb, "Data", 2, 0, 2, 0)
        }
        def root = rootBand("Top", "Data")
        addBand(root, "Top", [t: 1])
        addBand(root, "Top", [t: 2])
        addBand(root, "Top", [t: 3]) // Data has no rows -> empty band below the footer

        when: "the footer shifts to output row 3 while the empty Data endpoint falls back to its template row 2"
        def sheet = renderAndReadFirstSheet(template, root)
        def scf = sheet.getSheetConditionalFormatting()
        def ranges = (0..<scf.numConditionalFormattings).collectMany {
            scf.getConditionalFormattingAt(it).formattingRanges as List
        }

        then: "the rule re-bases to a valid, correctly ordered range (footer out-row 3, empty-band endpoint row 2 -> A3:A4)"
        ranges.contains(new CellRangeAddress(2, 3, 0, 0))
        ranges.every { it.firstRow <= it.lastRow }
    }

    def "the template workbook is closed after rendering so its OPCPackage is not leaked"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, 'title')
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [:])
        def output = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, ReportOutputType.xlsx, output)
        int closeCalls = 0
        def formatter = new StreamingXlsxFormatter(input) {
            @Override
            protected void closeTemplateWorkbook() {
                closeCalls++
                super.closeTemplateWorkbook()
            }
        }

        when:
        formatter.renderDocument()

        then: "the template workbook is released during the render rather than left to GC"
        closeCalls >= 1
    }

    def "calcPr with fullCalcOnLoad is emitted so Excel recalculates formulas on open"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when: "the workbook part is read as raw XML (poi-ooxml-lite has no CTCalcPr for a usermodel assertion)"
        def workbookXml = readWorkbookXml(render(template, root))

        then: "Excel is instructed to recalculate all formulas on open"
        workbookXml =~ /<calcPr[^>]*fullCalcOnLoad="(1|true)"/
    }

    protected static String readWorkbookXml(byte[] xlsxBytes) {
        def pkg = org.apache.poi.openxml4j.opc.OPCPackage.open(new ByteArrayInputStream(xlsxBytes))
        try {
            def partName = org.apache.poi.openxml4j.opc.PackagingURIHelper.createPartName("/xl/workbook.xml")
            return new String(pkg.getPart(partName).getInputStream().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8)
        } finally {
            pkg.close()
        }
    }

    def "vertical band is rejected with a clear error"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        def vertical = new BandData("Data", root, BandOrientation.VERTICAL)
        vertical.setData([v: 1])
        root.addChild(vertical)

        when:
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("vertical")
    }

    def "static content between a parent band and its child band is rejected"() {
        given: "parent Users row0, a static label row1, child Games row2 (Games is Users' child)"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${name}')
            cell(sheet, 1, 0, 'Games:')
            cell(sheet, 2, 0, '${title}')
            defineBand(wb, "Users", 0, 0, 0, 0)
            defineBand(wb, "Games", 2, 0, 2, 0)
        }
        def root = rootBand("Users")
        def user = addBand(root, "Users", [name: "Alice"])
        addBand(user, "Games", [title: "Chess"])

        when:
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("static content")
    }

    def "child band laid out above its parent is rejected"() {
        given: "parent Users on row 2, child Games (Users' child) laid out ABOVE on row 0"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${title}')
            cell(sheet, 2, 0, '${name}')
            defineBand(wb, "Games", 0, 0, 0, 0)
            defineBand(wb, "Users", 2, 0, 2, 0)
        }
        def root = rootBand("Users")
        def user = addBand(root, "Users", [name: "Alice"])
        addBand(user, "Games", [title: "Chess"])

        when: "the child renders attached to its parent instance, but its rows sit above the parent"
        render(template, root)

        then: "rejected up front instead of silently dropping the child band's data"
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("below its parent")
    }

    def "cross band is rejected with a clear error"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        def cross = new BandData("Data", root, BandOrientation.CROSS)
        cross.setData([v: 1])
        root.addChild(cross)

        when:
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("cross")
    }

    def "band defined by a named range renders its data"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${name}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [name: "Alice"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the named range is recognized as a band and its alias resolved, not left as literal \${name}"
        stringValue(sheet, 0, 0) == "Alice"
    }

    def "exceeding the xlsx row limit fails with a clear error"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        (1..5).each { addBand(root, "Data", [v: it]) }
        def reportTemplate = new io.jmix.reports.entity.ReportTemplate()
        reportTemplate.setContent(template)
        def formatter = new io.jmix.reports.yarg.formatters.impl.StreamingXlsxFormatter(
                new io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput(
                        "xlsx", root, reportTemplate,
                        io.jmix.reports.yarg.structure.ReportOutputType.xlsx, new ByteArrayOutputStream()))
        formatter.setMaxResultRows(3)

        when:
        formatter.renderDocument()

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("limit")
    }

    def "discard is idempotent and safe in any phase"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: "x"])
        def reportTemplate = new io.jmix.reports.entity.ReportTemplate()
        reportTemplate.setContent(template)
        def formatter = new io.jmix.reports.yarg.formatters.impl.StreamingXlsxFormatter(
                new io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput(
                        "xlsx", root, reportTemplate,
                        io.jmix.reports.yarg.structure.ReportOutputType.xlsx, new ByteArrayOutputStream()))

        expect: "before consumeData: no result workbook yet"
        formatter.discard()

        when:
        formatter.consumeData()
        formatter.discard()
        formatter.discard()

        then:
        noExceptionThrown()
    }

    def "a failed consumeData disposes the result workbook and leaves nothing for discard to re-dispose"() {
        given: "a template whose render fails after init (a vertical child band rejected in validateBandTree)"
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        def vertical = new BandData("Data", root, BandOrientation.VERTICAL)
        vertical.setData([v: 1])
        root.addChild(vertical)
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def formatter = new StreamingXlsxFormatter(new FormatterFactoryInput(
                "xlsx", root, reportTemplate, ReportOutputType.xlsx, new ByteArrayOutputStream()))

        when: "consumeData fails validation after the result workbook was already created in init"
        formatter.consumeData()

        then: "the result workbook is disposed and nulled, so a follow-up discard cannot dispose it twice"
        thrown(ReportFormattingException)
        formatter.@resultWorkbook == null

        when: "discard after the failure is a safe no-op"
        formatter.discard()

        then:
        noExceptionThrown()
    }

    def "template with more than one sheet is rejected with a clear error"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${Data.value}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            wb.createSheet("Legend")
        }
        def root = rootBand("Data")
        addBand(root, "Data", [value: "x"])

        when:
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.contains("single-sheet")
    }
}
