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
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.XlsxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.util.CellReference
import org.apache.poi.util.XMLHelper
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Stress test that generates an XLSX report with 200 columns and 500 000 rows (100 000 000 cells)
 * directly through {@link XlsxFormatter}.
 *
 * <p>It is a deliberate <b>limit reproducer</b>: the XLSX engine (docx4j {@code SpreadsheetMLPackage})
 * builds the whole result document in memory as a JAXB tree, with no streaming/SXSSF path. At the default
 * scale every cell is a live object, so the render is expected to exhaust the heap ({@code OutOfMemoryError})
 * or degrade badly on a regular JVM. It logs a progress line (rows written so far and heap usage) every
 * second, so even when the render runs out of memory the last logged line shows how many rows were written
 * and how much heap it took — both while rows are being written and during the final serialization.
 *
 * <p>The band tree intentionally shares a single data map across all rows, so heap pressure comes from the
 * formatter's output document rather than from constructing the input data.
 *
 * <p>The test is gated with {@code @IgnoreIf} and runs only with {@code -PincludeSlowTests=true} (the build
 * then sets the {@code slowTests} environment variable that the condition checks). To probe where the engine
 * actually breaks, dial the scale down. Gradle forks the test JVM and inherits environment variables but not
 * command-line {@code -D} properties, so via Gradle use
 * {@code REPORT_STRESS_ROWS=50000 ./gradlew :reports:test -PincludeSlowTests=true}; from an IDE run
 * configuration the system property {@code -Dreport.stress.rows=50000} works too.
 */
@IgnoreIf({ env['slowTests'] != 'true' })
class LargeXlsxReportGenerationTest extends Specification {

    private static final Logger log = LoggerFactory.getLogger(LargeXlsxReportGenerationTest)

    private static final String SHEET_NAME = "Sheet1"
    private static final String BAND_NAME = "Data"

    // Configurable so the breaking point can be probed without editing the test.
    // Read from a system property (IDE run) or an environment variable (Gradle forks the test JVM
    // and inherits environment variables, but does not forward command-line -D properties by default).
    private static final int COLUMNS = resolveInt("report.stress.columns", "REPORT_STRESS_COLUMNS", 200)
    private static final int ROWS = resolveInt("report.stress.rows", "REPORT_STRESS_ROWS", 500_000)

    private static int resolveInt(String systemProperty, String environmentVariable, int defaultValue) {
        def value = System.getProperty(systemProperty) ?: System.getenv(environmentVariable)
        return value != null ? Integer.parseInt(value) : defaultValue
    }

    def "generates a large xlsx report and measures the in-memory engine limit"() {
        given: "a single-band template and a band tree sized by COLUMNS and ROWS"
            log.info("Generating XLSX: {} columns x {} rows = {} cells",
                    COLUMNS, ROWS, ((long) COLUMNS) * ROWS)

            def template = new ReportTemplate()
            template.setContent(buildTemplate(COLUMNS))

            // One shared map for every row keeps the input cheap; the heap cost is the formatter's output.
            Map<String, Object> rowData = [:]
            for (int c = 0; c < COLUMNS; c++) {
                rowData.put("col" + c, "v" + c)
            }

            def rootBand = new BandData(BandData.ROOT_BAND_NAME)
            rootBand.setFirstLevelBandDefinitionNames([BAND_NAME].toSet())
            for (int r = 0; r < ROWS; r++) {
                def band = new BandData(BAND_NAME, rootBand)
                band.setData(rowData)
                rootBand.addChild(band)
            }

            def output = new ByteArrayOutputStream()
            // Count rows as the formatter writes them (renderDocument() calls writeBand once per top-level
            // band), so progress is observable even if the render later runs out of memory.
            def rowsWritten = new AtomicLong(0L)
            def formatter = new XlsxFormatter(
                    new FormatterFactoryInput("xlsx", rootBand, template, ReportOutputType.xlsx, output)) {
                @Override
                protected void writeBand(BandData band) {
                    if (band.name == BAND_NAME) {
                        rowsWritten.incrementAndGet()
                    }
                    super.writeBand(band)
                }
            }

        when: "the document is rendered while periodically logging rows written and heap usage"
            def runtime = Runtime.runtime
            def peakHeap = new AtomicLong(0L)
            def sampling = new AtomicBoolean(true)
            long startNanos = System.nanoTime()

            // Sample heap every 200 ms (to catch the peak) and log a progress line every second. The line
            // reports both rows-so-far and heap, so if renderDocument() runs out of memory the last logged
            // line shows how far it got — during the band-writing loop and the final serialization alike.
            def sampler = new Thread({
                long lastLogNanos = 0L
                while (sampling.get()) {
                    long used = runtime.totalMemory() - runtime.freeMemory()
                    peakHeap.set(Math.max(peakHeap.get(), used))
                    long now = System.nanoTime()
                    if (now - lastLogNanos >= 1_000_000_000L) {
                        lastLogNanos = now
                        log.info("progress: {} / {} rows, usedHeap={} MB, peakHeap={} MB, elapsed={} s",
                                rowsWritten.get(), ROWS, used >> 20, peakHeap.get() >> 20,
                                (now - startNanos).intdiv(1_000_000_000L))
                    }
                    try {
                        Thread.sleep(200L)
                    } catch (InterruptedException ignored) {
                        return
                    }
                }
            })
            sampler.daemon = true
            sampler.start()

            try {
                formatter.renderDocument()
            } catch (Throwable t) {
                try {
                    log.error("Render failed after {} / {} rows, usedHeap={} MB, peakHeap={} MB",
                            rowsWritten.get(), ROWS,
                            (runtime.totalMemory() - runtime.freeMemory()) >> 20, peakHeap.get() >> 20)
                } catch (Throwable ignored) {
                    // Logging under OutOfMemoryError may itself fail; the periodic progress log already captured the trail.
                }
                throw t
            }
            long elapsedMs = (System.nanoTime() - startNanos).intdiv(1_000_000L)

            sampling.set(false)
            byte[] bytes = output.toByteArray()

            log.info("Rendered: {} rows, elapsed={} ms, peakHeap={} MB, maxHeap={} MB, output={} MB",
                    rowsWritten.get(), elapsedMs, peakHeap.get() >> 20, runtime.maxMemory() >> 20,
                    ((long) bytes.length) >> 20)

            def counts = countRowsAndFirstRowCells(bytes)

        then: "the produced workbook has exactly the requested dimensions"
            bytes.length > 0
            counts.rows == ROWS
            counts.firstRowCells == COLUMNS
    }

    /**
     * Builds an XLSX template with a single horizontal band {@code Data}: one row of {@code columns} cells
     * holding {@code ${colN}} placeholders, covered by a named range matching the band name.
     */
    protected static byte[] buildTemplate(int columns) {
        def workbook = new XSSFWorkbook()
        try {
            def sheet = workbook.createSheet(SHEET_NAME)
            def row = sheet.createRow(0)
            for (int c = 0; c < columns; c++) {
                row.createCell(c).setCellValue('${col' + c + '}')
            }

            def lastColumn = CellReference.convertNumToColString(columns - 1)
            def name = workbook.createName()
            name.setNameName(BAND_NAME)
            name.setRefersToFormula("'" + SHEET_NAME + "'!\$A\$1:\$" + lastColumn + "\$1")

            def bos = new ByteArrayOutputStream()
            workbook.write(bos)
            return injectCalcPr(bos.toByteArray())
        } finally {
            workbook.close()
        }
    }

    /**
     * Adds an empty {@code <calcPr/>} element to the workbook part. {@link XlsxFormatter#init()} calls
     * {@code workbook.getCalcPr().setCalcMode(...)}, but a POI-generated workbook has no {@code calcPr}
     * element (a real Excel file does). It is injected by post-processing the bytes rather than via POI's
     * low-level {@code CTWorkbook}, which pulls in schema classes absent from poi-ooxml-lite on the classpath.
     */
    protected static byte[] injectCalcPr(byte[] xlsx) {
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

    /**
     * Counts rows and the number of cells in the first row of the first sheet using streaming SAX parsing,
     * so verification does not load the whole (potentially huge) workbook into memory.
     */
    protected static Map<String, Object> countRowsAndFirstRowCells(byte[] xlsx) {
        def pkg = OPCPackage.open(new ByteArrayInputStream(xlsx))
        try {
            def reader = new XSSFReader(pkg)
            def sheets = reader.getSheetsData()
            def sheetStream = sheets.next()
            try {
                long[] rowCount = [0L]
                int[] firstRowCells = [-1]
                int[] currentRowCells = [0]
                boolean[] inRow = [false]

                def handler = new DefaultHandler() {
                    @Override
                    void startElement(String uri, String localName, String qName, Attributes attributes) {
                        if (qName == "row") {
                            inRow[0] = true
                            currentRowCells[0] = 0
                            rowCount[0]++
                        } else if (qName == "c" && inRow[0]) {
                            currentRowCells[0]++
                        }
                    }

                    @Override
                    void endElement(String uri, String localName, String qName) {
                        if (qName == "row") {
                            if (firstRowCells[0] < 0) {
                                firstRowCells[0] = currentRowCells[0]
                            }
                            inRow[0] = false
                        }
                    }
                }

                def parser = XMLHelper.newXMLReader()
                parser.setContentHandler(handler)
                parser.parse(new InputSource(sheetStream))

                return [rows: rowCount[0], firstRowCells: firstRowCells[0]]
            } finally {
                sheetStream.close()
            }
        } finally {
            pkg.close()
        }
    }
}
