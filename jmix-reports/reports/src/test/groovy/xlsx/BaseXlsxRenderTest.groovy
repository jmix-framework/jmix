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

package xlsx

import io.jmix.reports.entity.ReportTemplate
import io.jmix.reports.yarg.formatters.ReportFormatter
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.XlsxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.CustomValueFormatter
import io.jmix.reports.yarg.structure.ReportFieldFormat
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Base class for XLSX rendering correctness tests.
 *
 * <p>Each test builds an XLSX template programmatically with Apache POI (cells with {@code ${alias}}
 * placeholders plus named ranges matching band names), constructs a {@link BandData} tree by hand, and runs
 * it through {@link XlsxFormatter} directly. The produced bytes are then read back with POI and asserted.
 *
 * <p>This keeps the tests deterministic and free of a database, a Spring context and binary template
 * resources — they verify the correctness of the generated workbook, not throughput.
 */
abstract class BaseXlsxRenderTest extends Specification {

    protected static final String SHEET = "Sheet1"

    // --- template building -------------------------------------------------------------------------------

    /**
     * Builds an XLSX template. The closure receives a fresh {@link XSSFWorkbook} to populate with sheets,
     * cells and named ranges (use {@link #cell}, {@link #formulaCell} and {@link #defineBand}). The required
     * {@code <calcPr/>} workbook element (absent in POI-generated workbooks) is injected afterwards.
     */
    protected byte[] buildTemplate(Closure configure) {
        def workbook = new XSSFWorkbook()
        try {
            configure.call(workbook)
            def bos = new ByteArrayOutputStream()
            workbook.write(bos)
            return injectCalcPr(bos.toByteArray())
        } finally {
            workbook.close()
        }
    }

    protected Sheet sheet(Workbook workbook, String name = SHEET) {
        return workbook.getSheet(name) ?: workbook.createSheet(name)
    }

    protected void cell(Sheet sheet, int row, int col, String value) {
        def r = sheet.getRow(row) ?: sheet.createRow(row)
        r.createCell(col).setCellValue(value)
    }

    protected void formulaCell(Sheet sheet, int row, int col, String formula) {
        def r = sheet.getRow(row) ?: sheet.createRow(row)
        r.createCell(col).setCellFormula(formula)
    }

    /**
     * Registers a named range covering the rectangle {@code (r1,c1)-(r2,c2)} (0-based, inclusive) on the
     * given sheet. The range name must match the {@link BandData} name the formatter renders into it.
     */
    protected void defineBand(Workbook workbook, String bandName, int r1, int c1, int r2, int c2, String sheetName = SHEET) {
        def first = new CellReference(sheetName, r1, c1, true, true)
        def last = new CellReference(r2, c2, true, true)
        def name = workbook.createName()
        name.setNameName(bandName)
        name.setRefersToFormula(first.formatAsString() + ":" + last.formatAsString())
    }

    // --- rendering ---------------------------------------------------------------------------------------

    protected byte[] render(byte[] template, BandData rootBand) {
        def output = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", rootBand, reportTemplate, ReportOutputType.xlsx, output)
        createFormatter(input).renderDocument()
        return output.toByteArray()
    }

    /** Override in streaming specs to exercise a different formatter implementation. */
    protected ReportFormatter createFormatter(FormatterFactoryInput input) {
        return new XlsxFormatter(input)
    }

    protected Workbook read(byte[] bytes) {
        return WorkbookFactory.create(new ByteArrayInputStream(bytes))
    }

    protected Sheet renderAndReadFirstSheet(byte[] template, BandData rootBand) {
        return read(render(template, rootBand)).getSheetAt(0)
    }

    // --- band tree ---------------------------------------------------------------------------------------

    protected BandData rootBand(String... firstLevelBandNames) {
        def root = new BandData(BandData.ROOT_BAND_NAME)
        root.setFirstLevelBandDefinitionNames(firstLevelBandNames.toList().toSet())
        return root
    }

    protected BandData addBand(BandData parent, String name, Map<String, Object> data) {
        def band = new BandData(name, parent)
        band.setData(data ?: [:])
        parent.addChild(band)
        return band
    }

    protected void withFieldFormats(BandData rootBand, ReportFieldFormat... formats) {
        rootBand.addReportFieldFormats(formats.toList())
    }

    protected ReportFieldFormat fieldFormat(String name, String format) {
        return new SimpleFieldFormat(name, format)
    }

    // --- cell reading helpers ----------------------------------------------------------------------------

    protected String stringValue(Sheet sheet, int row, int col) {
        return requireCell(sheet, row, col).getStringCellValue()
    }

    protected double numericValue(Sheet sheet, int row, int col) {
        return requireCell(sheet, row, col).getNumericCellValue()
    }

    protected boolean booleanValue(Sheet sheet, int row, int col) {
        return requireCell(sheet, row, col).getBooleanCellValue()
    }

    protected String formula(Sheet sheet, int row, int col) {
        return requireCell(sheet, row, col).getCellFormula()
    }

    protected Cell requireCell(Sheet sheet, int row, int col) {
        def r = sheet.getRow(row)
        assert r != null: "Row $row is missing"
        def c = r.getCell(col)
        assert c != null: "Cell ($row,$col) is missing"
        return c
    }

    protected Cell cellOrNull(Sheet sheet, int row, int col) {
        return sheet.getRow(row)?.getCell(col)
    }

    // --- calcPr injection --------------------------------------------------------------------------------

    /**
     * Adds an empty {@code <calcPr/>} element to {@code xl/workbook.xml}. POI-generated workbooks have no
     * {@code calcPr} element, but {@link XlsxFormatter#init()} calls {@code workbook.getCalcPr().setCalcMode(...)}.
     * Patching the raw bytes avoids POI's low-level {@code CTWorkbook}, whose schema classes are absent from
     * {@code poi-ooxml-lite}.
     */
    protected byte[] injectCalcPr(byte[] xlsx) {
        def zis = new ZipInputStream(new ByteArrayInputStream(xlsx))
        def bos = new ByteArrayOutputStream()
        def zos = new ZipOutputStream(bos)
        try {
            ZipEntry entry
            while ((entry = zis.getNextEntry()) != null) {
                byte[] content = zis.readAllBytes()
                if (entry.name == "xl/workbook.xml") {
                    content = new String(content, StandardCharsets.UTF_8)
                            .replace("</workbook>", "<calcPr/></workbook>")
                            .getBytes(StandardCharsets.UTF_8)
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

    static class SimpleFieldFormat implements ReportFieldFormat {
        private final String name
        private final String format

        SimpleFieldFormat(String name, String format) {
            this.name = name
            this.format = format
        }

        @Override
        String getName() {
            return name
        }

        @Override
        String getFormat() {
            return format
        }

        @Override
        CustomValueFormatter<?> getCustomFormatter() {
            return null
        }

        @Override
        Boolean isGroovyScript() {
            return false
        }
    }
}
