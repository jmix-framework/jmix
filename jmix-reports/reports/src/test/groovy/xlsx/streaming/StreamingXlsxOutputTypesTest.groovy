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
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import xlsx.StreamingBaseXlsxRenderTest

import java.nio.charset.StandardCharsets

/**
 * Non-xlsx output of the streaming formatter: the workbook is always produced as streamed XLSX first;
 * CSV is derived by a streaming SAX pass (raw cell values, semicolon-separated, empty rows skipped,
 * formulas unevaluated — parity with the non-streaming {@code saveXlsxAsCsv}).
 */
class StreamingXlsxOutputTypesTest extends StreamingBaseXlsxRenderTest {

    protected byte[] renderAs(byte[] template, BandData root, ReportOutputType outputType) {
        def output = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, outputType, output)
        new StreamingXlsxFormatter(input).renderDocument()
        return output.toByteArray()
    }

    def "csv output streams raw cell values with semicolons"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${name}')
            cell(sheet, 0, 1, '${qty}')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [name: "Alice", qty: 5])
        addBand(root, "Data", [name: "Bob", qty: 7])

        when:
        def csv = new String(renderAs(template, root, ReportOutputType.csv), StandardCharsets.UTF_8)
        def lines = csv.readLines()

        then:
        lines.size() == 2
        lines[0].split(";")*.replace('"', '') == ["Alice", "5"]
        lines[1].split(";")*.replace('"', '') == ["Bob", "7"]
    }

    def "csv renders whole numbers without a trailing .0 and avoids scientific notation"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${i}')
            cell(sheet, 0, 1, '${d}')
            cell(sheet, 0, 2, '${big}')
            defineBand(wb, "Data", 0, 0, 0, 2)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [i: 5, d: 3.5d, big: 1234567890123L])

        when:
        def csv = new String(renderAs(template, root, ReportOutputType.csv), StandardCharsets.UTF_8)
        def fields = csv.readLines()[0].split(";", -1)*.replace('"', '')

        then: "integers lose the .0, a large whole value stays plain, a real decimal is preserved"
        fields == ["5", "3.5", "1234567890123"]
    }

    def "csv keeps column alignment when a row omits a middle cell"() {
        given: "header and band both fill col0 and col2, leaving col1 empty (no cell emitted)"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'A')
            cell(sheet, 0, 2, 'C')
            cell(sheet, 1, 0, '${a}')
            cell(sheet, 1, 2, '${c}')
            defineBand(wb, "Data", 1, 0, 1, 2)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [a: "x", c: "z"])

        when:
        def csv = new String(renderAs(template, root, ReportOutputType.csv), StandardCharsets.UTF_8)
        def lines = csv.readLines()

        then: "the empty middle column stays an empty field, later columns are not shifted left"
        lines[0].split(";", -1)*.replace('"', '') == ["A", "", "C"]
        lines[1].split(";", -1)*.replace('"', '') == ["x", "", "z"]
    }

    def "csv output skips fully empty rows and leaves formula cells empty"() {
        given: "a template with a static gap row and a trailing SUM"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            formulaCell(sheet, 2, 0, 'SUM(A1:A1)')
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 3])

        when:
        def csv = new String(renderAs(template, root, ReportOutputType.csv), StandardCharsets.UTF_8)
        def lines = csv.readLines()

        then: "only the data row survives — the empty gap row and the value-less formula row are both skipped"
        lines.size() == 1
        lines[0].replace('"', '') == "3"
        lines.every { !it.contains("SUM") }
    }

    def "pdf output goes through the office document converter with valid xlsx bytes"() {
        given:
        def template = simpleTemplate()
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        and: "a stub converter capturing the input"
        DocumentConverter.FileType capturedType = null
        byte[] capturedBytes = null
        def converter = new DocumentConverter() {
            void convertToPdf(DocumentConverter.FileType fileType, byte[] documentBytes, OutputStream out) {
                capturedType = fileType
                capturedBytes = documentBytes
                out.write("PDF".bytes)
            }

            void convertToHtml(DocumentConverter.FileType fileType, byte[] documentBytes, OutputStream out) {
                throw new IllegalStateException("not expected")
            }
        }

        when:
        def output = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, ReportOutputType.pdf, output)
        def formatter = new StreamingXlsxFormatter(input)
        formatter.setDocumentConverter(converter)
        formatter.renderDocument()

        then:
        capturedType == DocumentConverter.FileType.SPREADSHEET
        new String(capturedBytes[0..1] as byte[]) == "PK"    // valid zip/xlsx magic
        output.toByteArray() == "PDF".bytes
    }

    def "pdf output without a converter fails with a clear message"() {
        given:
        def template = simpleTemplate()
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])

        when:
        renderAs(template, root, ReportOutputType.pdf)

        then: "a ReportingException so runReport routes it like every other formatter failure"
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("office")
    }

    def "xlsx output flushes a caller-owned buffered stream so the file is not truncated"() {
        given: "a caller-owned BufferedOutputStream that the caller does NOT flush/close itself"
        def template = simpleTemplate()
        def root = rootBand("Data")
        addBand(root, "Data", [v: 1])
        addBand(root, "Data", [v: 2])

        def bytes = new ByteArrayOutputStream()
        def buffered = new BufferedOutputStream(bytes)
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, ReportOutputType.xlsx, buffered)

        when: "the formatter renders; the caller relies on the legacy contract that the formatter flushes"
        new StreamingXlsxFormatter(input).renderDocument()

        then: "the emitted bytes are a complete, readable xlsx package (zip central directory present)"
        def produced = bytes.toByteArray()
        def pkg = org.apache.poi.openxml4j.opc.OPCPackage.open(new ByteArrayInputStream(produced))
        pkg.close()
    }

    private byte[] simpleTemplate() {
        return buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
    }
}
