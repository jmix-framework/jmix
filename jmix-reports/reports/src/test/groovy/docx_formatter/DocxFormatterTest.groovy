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

package docx_formatter

import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.factory.inline.DefaultInlinersProvider
import io.jmix.reports.yarg.formatters.impl.DocxFormatter
import io.jmix.reports.yarg.formatters.impl.docx.HtmlImportProcessorImpl
import io.jmix.reports.yarg.formatters.impl.inline.BitmapContentInliner
import io.jmix.reports.yarg.formatters.impl.inline.HtmlContentInliner
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import io.jmix.reports.yarg.structure.impl.ReportFieldFormatImpl
import io.jmix.reports.yarg.structure.impl.ReportTemplateImpl
import org.docx4j.jaxb.Context
import org.docx4j.model.table.TblFactory
import org.docx4j.TextUtils
import org.docx4j.openpackaging.io3.Save
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.ObjectFactory
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.Tbl
import org.docx4j.wml.Tc
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import spock.lang.Specification

import java.util.regex.Matcher

class DocxFormatterTest extends Specification {

    def "preserves text before html inline value"() {
        when:
        String text = renderText('Field header: ${Root.htmlValue}')

        then:
        appearsInOrder(text, "Field header:", "HTML value")
        !text.contains('${Root.htmlValue}')
    }

    def "preserves split run text before html inline value"() {
        when:
        String text = renderTextWithRuns('Field header: ', '${Root.htmlValue}')

        then:
        appearsInOrder(text, "Field header:", "HTML value")
        !text.contains('${Root.htmlValue}')
    }

    def "preserves text order around html inline value"() {
        when:
        String text = renderText('Before prefix ${Root.htmlValue} after suffix')

        then:
        appearsInOrder(text, "Before prefix", "HTML value", "after suffix")
        !text.contains('${Root.htmlValue}')
    }

    def "preserves split run text order around html inline value"() {
        when:
        String text = renderTextWithRuns('Before prefix ', '${Root.htmlValue}', ' after suffix')

        then:
        appearsInOrder(text, "Before prefix", "HTML value", "after suffix")
        !text.contains('${Root.htmlValue}')
    }

    def "formats regular aliases around html inline value"() {
        when:
        String text = renderText('${Root.name}: ${Root.htmlValue} ${Root.status}')

        then:
        appearsInOrder(text, "Report title:", "HTML value", "done")
        !text.contains('${Root.name}')
        !text.contains('${Root.status}')
        !text.contains('${Root.htmlValue}')
    }

    def "formats split run regular aliases around html inline value"() {
        when:
        String text = renderTextWithRuns('${Root.name}', ': ', '${Root.htmlValue}', ' ', '${Root.status}')

        then:
        appearsInOrder(text, "Report title:", "HTML value", "done")
        !text.contains('${Root.name}')
        !text.contains('${Root.status}')
        !text.contains('${Root.htmlValue}')
    }

    def "formats table aliases around html inline value"() {
        given:
        def rootBand = new BandData("Root")
        def tableBand = new BandData("items", rootBand)
        tableBand.addData("name", "Row title")
        tableBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        tableBand.addData("status", "ready")
        rootBand.addChild(tableBand)
        rootBand.addReportFieldFormats([new ReportFieldFormatImpl("items.htmlValue", '${html}')])

        def reportTemplate = createDocxTableTemplate("##band=items Header", '${name}: ${htmlValue} ${status}')
        def formatter = createFormatter(rootBand, reportTemplate)

        when:
        String text = extractText(formatter.createDocument())

        then:
        appearsInOrder(text, "Header", "Row title:", "HTML value", "ready")
        !text.contains('${name}')
        !text.contains('${status}')
        !text.contains('${htmlValue}')
    }

    def "formats split run table aliases around html inline value"() {
        given:
        def rootBand = new BandData("Root")
        def tableBand = new BandData("items", rootBand)
        tableBand.addData("name", "Row title")
        tableBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        tableBand.addData("status", "ready")
        rootBand.addChild(tableBand)
        rootBand.addReportFieldFormats([new ReportFieldFormatImpl("items.htmlValue", '${html}')])

        def reportTemplate = createDocxTableTemplateWithAliasRuns("##band=items Header",
                '${name}', ': ', '${htmlValue}', ' ', '${status}')
        def formatter = createFormatter(rootBand, reportTemplate)

        when:
        String text = extractText(formatter.createDocument())

        then:
        appearsInOrder(text, "Header", "Row title:", "HTML value", "ready")
        !text.contains('${name}')
        !text.contains('${status}')
        !text.contains('${htmlValue}')
    }

    def "keeps old html behavior when another inliner alias is in the same text"() {
        given:
        def rootBand = new BandData("Root")
        rootBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        rootBand.addData("bitmapValue", new byte[0])
        rootBand.addReportFieldFormats([
                new ReportFieldFormatImpl("Root.htmlValue", '${html}'),
                new ReportFieldFormatImpl("Root.bitmapValue", '${bitmap:10x10}')
        ])

        def htmlInliner = new RecordingHtmlContentInliner()
        def formatter = createFormatter(rootBand, createDocxTemplate('${Root.htmlValue} ${Root.bitmapValue}'))
        formatter.setContentInliners([new BitmapContentInliner(), htmlInliner])

        when:
        formatter.createDocument()

        then:
        htmlInliner.values == ["<p><strong>HTML value</strong></p>"]
    }

    def "keeps old html behavior when another inliner alias has short format name"() {
        given:
        def rootBand = new BandData("Root")
        rootBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        rootBand.addData("bitmapValue", new byte[0])
        rootBand.addReportFieldFormats([
                new ReportFieldFormatImpl("Root.htmlValue", '${html}'),
                new ReportFieldFormatImpl("bitmapValue", '${bitmap:10x10}')
        ])

        def htmlInliner = new RecordingHtmlContentInliner()
        def formatter = createFormatter(rootBand, createDocxTemplate('${Root.htmlValue} ${Root.bitmapValue}'))
        formatter.setContentInliners([new RecordingBitmapContentInliner(), htmlInliner])

        when:
        formatter.createDocument()

        then:
        htmlInliner.values == ["<p><strong>HTML value</strong></p>"]
    }

    def "keeps old html behavior when another inliner alias is in a sibling run"() {
        given:
        def rootBand = new BandData("Root")
        rootBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        rootBand.addData("bitmapValue", new byte[0])
        rootBand.addReportFieldFormats([
                new ReportFieldFormatImpl("Root.htmlValue", '${html}'),
                new ReportFieldFormatImpl("Root.bitmapValue", '${bitmap:10x10}')
        ])

        def htmlInliner = new RecordingHtmlContentInliner()
        def formatter = createFormatter(rootBand, createDocxTemplateWithRuns('${Root.htmlValue}', ' ', '${Root.bitmapValue}'))
        formatter.setContentInliners([new RecordingBitmapContentInliner(), htmlInliner])

        when:
        formatter.createDocument()

        then:
        htmlInliner.values == ["<p><strong>HTML value</strong></p>"]
    }

    def "keeps old html behavior when the same html alias is repeated in the same text"() {
        given:
        def rootBand = new BandData("Root")
        rootBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        rootBand.addReportFieldFormats([new ReportFieldFormatImpl("Root.htmlValue", '${html}')])

        def htmlInliner = new RecordingHtmlContentInliner()
        def formatter = createFormatter(rootBand, createDocxTemplate('${Root.htmlValue} ${Root.htmlValue}'))
        formatter.setContentInliners([htmlInliner])

        when:
        formatter.createDocument()

        then:
        htmlInliner.values == ["<p><strong>HTML value</strong></p>"]
    }

    def "keeps old html behavior when the same html alias is repeated in sibling runs"() {
        given:
        def rootBand = new BandData("Root")
        rootBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        rootBand.addReportFieldFormats([new ReportFieldFormatImpl("Root.htmlValue", '${html}')])

        def htmlInliner = new RecordingHtmlContentInliner()
        def formatter = createFormatter(rootBand, createDocxTemplateWithRuns('${Root.htmlValue}', ' ', '${Root.htmlValue}'))
        formatter.setContentInliners([htmlInliner])

        when:
        formatter.createDocument()

        then:
        htmlInliner.values == ["<p><strong>HTML value</strong></p>", "<p><strong>HTML value</strong></p>"]
    }

    protected String renderText(String paragraphText) {
        def rootBand = new BandData("Root")
        rootBand.addData("name", "Report title")
        rootBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        rootBand.addData("status", "done")
        rootBand.addReportFieldFormats([new ReportFieldFormatImpl("Root.htmlValue", '${html}')])

        def reportTemplate = createDocxTemplate(paragraphText)
        return extractText(createFormatter(rootBand, reportTemplate).createDocument())
    }

    protected String renderTextWithRuns(String... runTexts) {
        def rootBand = new BandData("Root")
        rootBand.addData("name", "Report title")
        rootBand.addData("htmlValue", "<p><strong>HTML value</strong></p>")
        rootBand.addData("status", "done")
        rootBand.addReportFieldFormats([new ReportFieldFormatImpl("Root.htmlValue", '${html}')])

        def reportTemplate = createDocxTemplateWithRuns(runTexts)
        return extractText(createFormatter(rootBand, reportTemplate).createDocument())
    }

    protected DocxFormatter createFormatter(BandData rootBand, ReportTemplateImpl reportTemplate) {
        def formatter = new DocxFormatter(new FormatterFactoryInput(
                "docx", rootBand, reportTemplate, ReportOutputType.docx, new ByteArrayOutputStream()))
        formatter.setContentInliners(new DefaultInlinersProvider().getContentInliners())
        formatter.setHtmlImportProcessor(new HtmlImportProcessorImpl())
        return formatter
    }

    protected ReportTemplateImpl createDocxTemplate(String paragraphText) {
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage()
        wordPackage.getMainDocumentPart().addParagraphOfText(paragraphText)

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        new Save(wordPackage).save(outputStream)

        return new ReportTemplateImpl(
                "default", "template.docx", "template.docx",
                new ByteArrayInputStream(outputStream.toByteArray()), ReportOutputType.docx)
    }

    protected ReportTemplateImpl createDocxTemplateWithRuns(String... runTexts) {
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage()
        ObjectFactory factory = Context.getWmlObjectFactory()
        P paragraph = factory.createP()
        addRuns(factory, paragraph, runTexts)
        wordPackage.getMainDocumentPart().addObject(paragraph)

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        new Save(wordPackage).save(outputStream)

        return new ReportTemplateImpl(
                "default", "template.docx", "template.docx",
                new ByteArrayInputStream(outputStream.toByteArray()), ReportOutputType.docx)
    }

    protected ReportTemplateImpl createDocxTableTemplate(String headerText, String aliasText) {
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage()
        ObjectFactory factory = Context.getWmlObjectFactory()
        Tbl table = TblFactory.createTable(2, 1, 9000)
        fillTableCell(factory, (Tr) table.getContent().get(0), headerText)
        fillTableCell(factory, (Tr) table.getContent().get(1), aliasText)
        wordPackage.getMainDocumentPart().addObject(table)

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        new Save(wordPackage).save(outputStream)

        return new ReportTemplateImpl(
                "default", "template.docx", "template.docx",
                new ByteArrayInputStream(outputStream.toByteArray()), ReportOutputType.docx)
    }

    protected ReportTemplateImpl createDocxTableTemplateWithAliasRuns(String headerText, String... aliasRuns) {
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage()
        ObjectFactory factory = Context.getWmlObjectFactory()
        Tbl table = TblFactory.createTable(2, 1, 9000)
        fillTableCell(factory, (Tr) table.getContent().get(0), headerText)
        fillTableCellWithRuns(factory, (Tr) table.getContent().get(1), aliasRuns)
        wordPackage.getMainDocumentPart().addObject(table)

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        new Save(wordPackage).save(outputStream)

        return new ReportTemplateImpl(
                "default", "template.docx", "template.docx",
                new ByteArrayInputStream(outputStream.toByteArray()), ReportOutputType.docx)
    }

    protected void fillTableCell(ObjectFactory factory, Tr row, String value) {
        fillTableCellWithRuns(factory, row, value)
    }

    protected void fillTableCellWithRuns(ObjectFactory factory, Tr row, String... values) {
        Tc cell = (Tc) row.getContent().get(0)
        P paragraph = (P) cell.getContent().get(0)
        paragraph.getContent().clear()
        addRuns(factory, paragraph, values)
    }

    protected void addRuns(ObjectFactory factory, P paragraph, String... values) {
        values.each { value ->
            Text text = factory.createText()
            text.setValue(value)
            text.setSpace("preserve")
            R run = factory.createR()
            run.getContent().add(text)
            paragraph.getContent().add(run)
        }
    }

    protected String extractText(byte[] documentContent) {
        WordprocessingMLPackage resultPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(documentContent))
        StringWriter writer = new StringWriter()
        TextUtils.extractText(resultPackage.getMainDocumentPart().getContents(), writer)
        return writer.toString()
    }

    protected boolean appearsInOrder(String text, String... values) {
        int fromIndex = 0
        for (String value : values) {
            int valueIndex = text.indexOf(value, fromIndex)
            if (valueIndex < 0) {
                return false
            }
            fromIndex = valueIndex + value.length()
        }
        return true
    }

    static class RecordingHtmlContentInliner extends HtmlContentInliner {
        List<Object> values = []

        @Override
        void inlineToDocx(WordprocessingMLPackage wordPackage, Text destination, Object paramValue, Matcher matcher) {
            values.add(paramValue)
            destination.setValue("")
        }
    }

    static class RecordingBitmapContentInliner extends BitmapContentInliner {
        @Override
        void inlineToDocx(WordprocessingMLPackage wordPackage, Text destination, Object paramValue, Matcher matcher) {
            destination.setValue("")
        }
    }
}
