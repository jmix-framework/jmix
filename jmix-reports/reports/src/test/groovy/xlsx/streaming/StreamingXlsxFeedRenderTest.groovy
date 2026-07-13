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
import io.jmix.reports.yarg.formatters.impl.streaming.StreamingBandFeed
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.ss.usermodel.ComparisonOperator
import org.apache.poi.ss.util.CellRangeAddress
import xlsx.StreamingBaseXlsxRenderTest

/**
 * The formatter consumes the hot band from a {@link StreamingBandFeed}: rows are pulled lazily one at
 * a time, never accumulated in the band tree, while small bands still render from the materialized tree.
 */
class StreamingXlsxFeedRenderTest extends StreamingBaseXlsxRenderTest {

    private byte[] renderWithFeed(byte[] template, BandData root, StreamingBandFeed feed) {
        def output = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, ReportOutputType.xlsx, output)
        def formatter = new StreamingXlsxFormatter(input)
        formatter.setStreamingBandFeed(feed)
        formatter.renderDocument()
        return output.toByteArray()
    }

    def "hot band rows come from the feed, small bands from the tree"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${title}')
            defineBand(wb, "Header", 0, 0, 0, 0)
            cell(sheet, 1, 0, '${n}')
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Header", "Data")
        addBand(root, "Header", [title: "T"])
        def feed = new StreamingBandFeed("Data", [[n: "A"], [n: "B"], [n: "C"]].iterator(), root)

        when:
        def sheet = read(renderWithFeed(template, root, feed)).getSheetAt(0)

        then:
        stringValue(sheet, 0, 0) == "T"
        stringValue(sheet, 1, 0) == "A"
        stringValue(sheet, 2, 0) == "B"
        stringValue(sheet, 3, 0) == "C"
        root.getChildrenByName("Data").isEmpty()
    }

    def "the fed band renders every source row in order, pulling each exactly once"() {
        given: "a finite source that counts pulls"
        int pulled = 0
        def data = [[n: "A"], [n: "B"], [n: "C"]]
        def rows = new Iterator<Map<String, Object>>() {
            int i = 0

            boolean hasNext() { i < data.size() }

            Map<String, Object> next() {
                pulled++
                return data[i++]
            }
        }
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        def feed = new StreamingBandFeed("Data", rows, root)

        when:
        def sheet = read(renderWithFeed(template, root, feed)).getSheetAt(0)

        then: "each source row is pulled exactly once (no over-read past the end) and rendered top-to-bottom in order"
        pulled == 3
        stringValue(sheet, 0, 0) == "A"
        stringValue(sheet, 1, 0) == "B"
        stringValue(sheet, 2, 0) == "C"
        cellOrNull(sheet, 3, 0) == null
    }

    def "a failing feed source surfaces the error at the failing pull"() {
        given: "a source that fails on the 3rd pull"
        int pulled = 0
        def rows = new Iterator<Map<String, Object>>() {
            boolean hasNext() { true }

            Map<String, Object> next() {
                pulled++
                if (pulled == 3) {
                    throw new IllegalStateException("stop")
                }
                return [n: "x" + pulled]
            }
        }
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        def feed = new StreamingBandFeed("Data", rows, root)

        when:
        renderWithFeed(template, root, feed)

        then: "the error propagates out of the render at the failing pull (true O(1) laziness is covered by the memory test)"
        thrown(IllegalStateException)
        pulled == 3
    }

    def "workbook is written to the output stream only in the completion phase"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        def feed = new StreamingBandFeed("Data", [[n: "A"]].iterator(), root)
        def output = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, ReportOutputType.xlsx, output)
        def formatter = new StreamingXlsxFormatter(input)
        formatter.setStreamingBandFeed(feed)

        when: "the data phase consumes the cursor"
        formatter.consumeData()

        then: "nothing reaches the output stream while the cursor is still open"
        output.size() == 0

        when: "the completion phase runs after the cursor is released"
        formatter.completeRendering()

        then:
        stringValue(read(output.toByteArray()).getSheetAt(0), 0, 0) == "A"
    }

    def "merged region inside a streaming (fed) band is rejected to keep memory bounded"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1))
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        def feed = new StreamingBandFeed("Data", [[n: "A"], [n: "B"]].iterator(), root)

        when:
        renderWithFeed(template, root, feed)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("merge")
    }

    def "trailing SUM grows over fed rows"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            formulaCell(sheet, 1, 0, 'SUM(A1:A1)')
        }
        def root = rootBand("Data")
        def feed = new StreamingBandFeed("Data", [[v: 1], [v: 2], [v: 3]].iterator(), root)

        when:
        def sheet = read(renderWithFeed(template, root, feed)).getSheetAt(0)

        then:
        formula(sheet, 3, 0) == "SUM(A1:A3)"
    }

    def "conditional formatting on a multi-row fed band covers the whole rendered span"() {
        given: "a 2-row streaming (fed) band with a CF rule on only its second template row"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${a}')
            cell(sheet, 1, 0, '${b}')
            def scf = sheet.getSheetConditionalFormatting()
            def rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "5")
            scf.addConditionalFormatting([new CellRangeAddress(1, 1, 0, 0)] as CellRangeAddress[], rule)
            defineBand(wb, "Data", 0, 0, 1, 0)
        }
        def root = rootBand("Data")
        def feed = new StreamingBandFeed("Data", [[a: 1, b: 2], [a: 3, b: 4]].iterator(), root)

        when: "2 instances render output rows 0..3"
        def sheet = read(renderWithFeed(template, root, feed)).getSheetAt(0)
        def scf = sheet.getSheetConditionalFormatting()
        def ranges = (0..<scf.numConditionalFormattings).collectMany {
            scf.getConditionalFormattingAt(it).formattingRanges as List
        }

        then: "fed rows cannot be replicated per instance (unbounded), so the rule covers the whole span A1:A4 " +
                "starting at the first rendered row, not from the rule's in-band row offset"
        ranges.contains(new CellRangeAddress(0, 3, 0, 0))
    }

    def "the fed band is consumed incrementally: a row is pulled only after the previous one is written"() {
        given: "a source that records how many output rows already exist in the result sheet at each pull"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")

        def output = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, ReportOutputType.xlsx, output)
        def formatter = new StreamingXlsxFormatter(input)

        def data = [[n: "A"], [n: "B"], [n: "C"], [n: "D"], [n: "E"]]
        def lastRowNumAtPull = []
        def rows = new Iterator<Map<String, Object>>() {
            int i = 0

            boolean hasNext() { i < data.size() }

            Map<String, Object> next() {
                // -1 means the result sheet is still empty (POI convention for no rows).
                def wb = formatter.@resultWorkbook
                lastRowNumAtPull << (wb == null ? -2 : wb.getSheetAt(0).getLastRowNum())
                return data[i++]
            }
        }
        def feed = new StreamingBandFeed("Data", rows, root)
        formatter.setStreamingBandFeed(feed)

        when:
        formatter.renderDocument()

        then: "each pull sees exactly the previously-written rows, proving the feed is not drained up front " +
                "(an eager materialization would pull all 5 rows while the sheet is still empty: [-1, -1, -1, -1, -1])"
        lastRowNumAtPull == [-1, 0, 1, 2, 3]
    }
}
