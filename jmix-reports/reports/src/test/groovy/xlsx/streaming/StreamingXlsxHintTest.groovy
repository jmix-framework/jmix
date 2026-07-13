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

import org.apache.poi.ss.usermodel.BorderStyle
import xlsx.StreamingBaseXlsxRenderTest

import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Processing hints of the streaming XLSX formatter, applied at row-write time: {@code hint_style_<param>}
 * (dynamic named cell style resolved from band data) and {@code hint_rowAutoHeight}.
 */
class StreamingXlsxHintTest extends StreamingBaseXlsxRenderTest {

    def "hint_style applies a named cell style resolved from band data"() {
        given:
        int boldFontId = 0
        def template = buildTemplate { wb ->
            def font = wb.createFont()
            font.bold = true
            boldFontId = font.index
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            defineBand(wb, "hint_style_rowStyle", 0, 0, 0, 0)
        }
        template = injectNamedStyle(template, "highlight", boldFontId)
        def root = rootBand("Data")
        addBand(root, "Data", [v: "x", rowStyle: "highlight"])

        when:
        def workbook = read(render(template, root))
        def cell = workbook.getSheetAt(0).getRow(0).getCell(0)

        then:
        workbook.getFontAt(cell.cellStyle.fontIndex).bold
    }

    def "hint_style preserves each cell's own border, not the first rendered cell's"() {
        given: "two cells share one hint_style range but carry different top borders"
        int boldFontId = 0
        def template = buildTemplate { wb ->
            def font = wb.createFont()
            font.bold = true
            boldFontId = font.index
            def sheet = sheet(wb)
            def thin = wb.createCellStyle()
            thin.setBorderTop(BorderStyle.THIN)
            def thick = wb.createCellStyle()
            thick.setBorderTop(BorderStyle.THICK)
            def r = sheet.createRow(0)
            def c0 = r.createCell(0); c0.setCellValue('${v}'); c0.setCellStyle(thin)
            def c1 = r.createCell(1); c1.setCellValue('${w}'); c1.setCellStyle(thick)
            defineBand(wb, "Data", 0, 0, 0, 1)
            defineBand(wb, "hint_style_s", 0, 0, 0, 1)
        }
        template = injectNamedStyle(template, "highlight", boldFontId)
        def root = rootBand("Data")
        addBand(root, "Data", [v: "x", w: "y", s: "highlight"])

        when:
        def workbook = read(render(template, root))
        def row = workbook.getSheetAt(0).getRow(0)

        then: "both cells get the named style (bold), each keeps its own border"
        workbook.getFontAt(row.getCell(0).cellStyle.fontIndex).bold
        workbook.getFontAt(row.getCell(1).cellStyle.fontIndex).bold
        row.getCell(0).cellStyle.borderTop == BorderStyle.THIN
        row.getCell(1).cellStyle.borderTop == BorderStyle.THICK
    }

    def "hint_style with unknown style name keeps the template cell style"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            defineBand(wb, "hint_style_rowStyle", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: "x", rowStyle: "no-such-style"])

        when:
        def workbook = read(render(template, root))
        def cell = workbook.getSheetAt(0).getRow(0).getCell(0)

        then:
        !workbook.getFontAt(cell.cellStyle.fontIndex).bold
        cell.stringCellValue == "x"
    }

    def "hint_rowAutoHeight raises the row height for long wrapped text"() {
        given:
        def longText = "word " * 60
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            sheet.setColumnWidth(0, 2000)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            defineBand(wb, "hint_rowAutoHeight", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: longText])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "long wrapped text spans several lines, raising the row well above default and capping at the Excel max"
        sheet.getRow(0).getHeightInPoints() > 2 * sheet.getDefaultRowHeightInPoints()
        sheet.getRow(0).getHeightInPoints() <= 410
    }

    def "short text in a hint_rowAutoHeight range keeps a modest height"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            sheet.setColumnWidth(0, 8000)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            defineBand(wb, "hint_rowAutoHeight", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: "short"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "a single short line does not inflate the row — it stays close to the default height"
        sheet.getRow(0).getHeightInPoints() <= 1.5 * sheet.getDefaultRowHeightInPoints()
    }

    /**
     * Registers a named cell style in the template's {@code xl/styles.xml} by patching the raw bytes:
     * adds a second entry to {@code cellStyleXfs} referencing the given font and a {@code cellStyle}
     * element pointing at it. POI's public API cannot create named cell styles, and the low-level
     * {@code CTCellStyle} schema classes are absent from {@code poi-ooxml-lite} (same approach as
     * {@code BaseXlsxRenderTest#injectCalcPr}).
     */
    protected static byte[] injectNamedStyle(byte[] xlsx, String styleName, int fontId) {
        def zis = new ZipInputStream(new ByteArrayInputStream(xlsx))
        def bos = new ByteArrayOutputStream()
        def zos = new ZipOutputStream(bos)
        try {
            ZipEntry entry
            while ((entry = zis.getNextEntry()) != null) {
                byte[] content = zis.readAllBytes()
                if (entry.name == "xl/styles.xml") {
                    def xml = new String(content, StandardCharsets.UTF_8)
                    def styleXf = "<xf numFmtId=\"0\" fontId=\"$fontId\" fillId=\"0\" borderId=\"0\" applyFont=\"1\"/>"
                    def cellStyle = "<cellStyle name=\"$styleName\" xfId=\"1\"/>"
                    if (xml.contains("</cellStyleXfs>")) {
                        xml = xml.replace("</cellStyleXfs>", "$styleXf</cellStyleXfs>")
                    } else {
                        xml = xml.replace("<cellXfs",
                                "<cellStyleXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/>$styleXf</cellStyleXfs><cellXfs")
                    }
                    if (xml.contains("</cellStyles>")) {
                        xml = xml.replace("</cellStyles>", "$cellStyle</cellStyles>")
                    } else {
                        xml = xml.replace("</styleSheet>",
                                "<cellStyles count=\"2\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/>$cellStyle</cellStyles></styleSheet>")
                    }
                    content = xml.getBytes(StandardCharsets.UTF_8)
                }
                zos.putNextEntry(new ZipEntry(entry.name))
                zos.write(content)
                zos.closeEntry()
            }
        } finally {
            zis.close()
            zos.close()
        }
        return bos.toByteArray()
    }
}
