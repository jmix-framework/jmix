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
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.openxml4j.util.ZipSecureFile
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
 * Baseline measurement of the in-memory XLSX engine ({@link XlsxFormatter}), and the contrast case for
 * {@link StreamingLargeXlsxReportGenerationTest}. It renders a 200-column report directly through the
 * formatter and logs what the render costs — rows written, elapsed time, peak heap, output size — so the
 * streaming engine's numbers can be read against a measured reference rather than a guess.
 *
 * <p>The engine (docx4j {@code SpreadsheetMLPackage}) builds the whole result document in memory as a JAXB
 * tree, with no streaming/SXSSF path: every cell is a live object, so heap grows with the cell count and the
 * render dies at a few million cells on an ordinary JVM. The default scale is therefore deliberately modest
 * (10 000 rows x 200 columns = 2 000 000 cells, about a third of the 2 GB test heap) so this test
 * <b>passes</b> and measures; it does not try to reach the breaking point. Driving a shared test JVM into
 * {@code OutOfMemoryError} on purpose is not a usable pipeline test: the error can surface on any thread,
 * and once the heap is exhausted every other test in the same worker is collateral damage.
 *
 * <p>The band tree intentionally shares a single data map across all rows, so heap pressure comes from the
 * formatter's output document rather than from constructing the input data.
 *
 * <p>The test is gated with {@code @IgnoreIf} and runs only with {@code -PincludeSlowTests=true} (the build
 * then sets the {@code slowTests} environment variable that the condition checks). To probe where the engine
 * actually breaks, dial the scale up. Gradle forks the test JVM and inherits environment variables but not
 * command-line {@code -D} properties, so via Gradle use
 * {@code REPORT_STRESS_ROWS=500000 ./gradlew :reports:test -PincludeSlowTests=true}; from an IDE run
 * configuration the system property {@code -Dreport.stress.rows=500000} works too. At such a scale the heap
 * watchdog in the sampler thread aborts the render (the engine checks {@code Thread.interrupted()} per cell)
 * and the test fails with the row count it reached — a clean, informative failure instead of a dead JVM.
 */
@IgnoreIf({ env['slowTests'] != 'true' })
class LargeXlsxReportGenerationTest extends Specification {

    private static final Logger log = LoggerFactory.getLogger(LargeXlsxReportGenerationTest)

    private static final String SHEET_NAME = "Sheet1"
    private static final String BAND_NAME = "Data"

    /**
     * Fits the 2 GB test heap with room to spare (measured at 200 columns: ~650-700 MB peak, ~10 s render,
     * 7 MB output). The in-memory engine cannot go much further — see the class javadoc.
     */
    protected static final int DEFAULT_ROWS = 10_000

    // Configurable so the breaking point can be probed without editing the test.
    // Read from a system property (IDE run) or an environment variable (Gradle forks the test JVM
    // and inherits environment variables, but does not forward command-line -D properties by default).
    private static final int COLUMNS = resolveInt("report.stress.columns", "REPORT_STRESS_COLUMNS", 200)
    private static final int ROWS = resolveInt("report.stress.rows", "REPORT_STRESS_ROWS", DEFAULT_ROWS)

    /** The watchdog aborts the render when used heap stays above this fraction of the max heap. */
    private static final double HEAP_ABORT_FRACTION = 0.9d
    /**
     * ...for this many consecutive samples (200 ms each, so ~5 s). Sustained near-max usage means the GC
     * death spiral; a single spike only means the collector has not run yet and must not trip the abort.
     */
    private static final int HEAP_ABORT_SAMPLES = 25

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
            def abortedAtRow = new AtomicLong(-1L)
            def renderThread = Thread.currentThread()
            long abortThreshold = (long) (runtime.maxMemory() * HEAP_ABORT_FRACTION)
            long startNanos = System.nanoTime()

            // Sample heap every 200 ms (to catch the peak) and log a progress line every second. The line
            // reports both rows-so-far and heap, so the trail shows how far the render got — during the
            // band-writing loop and the final serialization alike.
            def sampler = new Thread({
                long lastLogNanos = 0L
                int sustainedNearMax = 0
                while (sampling.get()) {
                    try {
                        long used = runtime.totalMemory() - runtime.freeMemory()
                        peakHeap.set(Math.max(peakHeap.get(), used))
                        long now = System.nanoTime()
                        if (now - lastLogNanos >= 1_000_000_000L) {
                            lastLogNanos = now
                            log.info("progress: {} / {} rows, usedHeap={} MB, peakHeap={} MB, elapsed={} s",
                                    rowsWritten.get(), ROWS, used >> 20, peakHeap.get() >> 20,
                                    (now - startNanos).intdiv(1_000_000_000L))
                        }

                        // Stop the render before the heap is actually exhausted. An OutOfMemoryError raised
                        // here (this thread allocates while logging) would escape uncaught and kill the whole
                        // Gradle test worker, failing every other test in the JVM instead of just this one.
                        // The engine calls checkThreadInterrupted() per template cell, so the interrupt turns
                        // into a ReportingInterruptedException almost immediately.
                        sustainedNearMax = used > abortThreshold ? sustainedNearMax + 1 : 0
                        if (sustainedNearMax >= HEAP_ABORT_SAMPLES && abortedAtRow.get() < 0) {
                            abortedAtRow.set(rowsWritten.get())
                            log.error("Used heap stayed above {} MB ({}% of {} MB) for {} s; aborting the render"
                                            + " at {} / {} rows to keep the test JVM alive",
                                    abortThreshold >> 20, (int) (HEAP_ABORT_FRACTION * 100),
                                    runtime.maxMemory() >> 20, (HEAP_ABORT_SAMPLES * 200).intdiv(1000),
                                    abortedAtRow.get(), ROWS)
                            renderThread.interrupt()
                        }

                        Thread.sleep(200L)
                    } catch (InterruptedException ignored) {
                        return
                    } catch (Throwable ignored) {
                        // Never let this thread's own failure escape: an uncaught error (typically
                        // OutOfMemoryError while logging) takes the test JVM down with it. Give up sampling;
                        // the render either finishes or fails on its own thread, where it is reported.
                        return
                    }
                }
            })
            sampler.daemon = true
            // Outer safety net for what the in-loop catch cannot cover: under memory pressure the error can
            // also be raised while Groovy links a call site, i.e. before the try block is entered. An
            // uncaught Throwable here reaches the default handler and takes the test worker down.
            sampler.setUncaughtExceptionHandler { Thread t, Throwable e -> sampling.set(false) }
            sampler.start()

            Throwable renderFailure = null
            try {
                formatter.renderDocument()
            } catch (Throwable t) {
                renderFailure = t
                try {
                    log.error("Render failed after {} / {} rows, usedHeap={} MB, peakHeap={} MB",
                            rowsWritten.get(), ROWS,
                            (runtime.totalMemory() - runtime.freeMemory()) >> 20, peakHeap.get() >> 20)
                } catch (Throwable ignored) {
                    // Logging under OutOfMemoryError may itself fail; the periodic progress log already captured the trail.
                }
            }
            long elapsedMs = (System.nanoTime() - startNanos).intdiv(1_000_000L)

            stopSampler(sampling, sampler)

            if (renderFailure != null) {
                if (abortedAtRow.get() >= 0) {
                    throw new AssertionError("The in-memory XLSX engine ran out of heap at "
                            + abortedAtRow.get() + " / " + ROWS + " rows x " + COLUMNS + " columns "
                            + "(max heap " + (runtime.maxMemory() >> 20) + " MB). This engine keeps the whole "
                            + "result document in memory, so it cannot render this scale — that is the very "
                            + "limitation the streaming engine addresses. Lower REPORT_STRESS_ROWS (default "
                            + DEFAULT_ROWS + ") or use StreamingXlsxFormatter.", renderFailure)
                }
                throw renderFailure
            }

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
     * Stops the sampler thread and clears a pending interrupt. The heap watchdog interrupts the render
     * thread, and the flag survives when the render finished before the engine consumed it — a stray
     * interrupt would then break the verification below, or leak into the next test in this JVM.
     */
    protected static void stopSampler(AtomicBoolean sampling, Thread sampler) {
        sampling.set(false)
        sampler.interrupt()
        sampler.join(1_000L)
        Thread.interrupted()
    }

    /**
     * Counts rows and the number of cells in the first row of the first sheet using streaming SAX parsing,
     * so verification does not load the whole (potentially huge) workbook into memory.
     *
     * <p>The bytes are spooled to a temp file and the package is opened read-only <b>by path</b> instead of
     * from a {@code ByteArrayInputStream}: the stream-based {@code OPCPackage.open} inflates every zip entry
     * into a byte array up front, which trips POI's 100 MB per-entry {@code IOUtils} cap on the uncompressed
     * sheet XML (reached already at a couple of million cells) and would defeat the streaming read anyway.
     */
    protected static Map<String, Object> countRowsAndFirstRowCells(byte[] xlsx) {
        def spool = File.createTempFile("large-report-verify", ".xlsx")
        long previousMaxEntrySize = ZipSecureFile.getMaxEntrySize()
        try {
            spool.bytes = xlsx
            // The uncompressed sheet XML legitimately exceeds POI's default zip-bomb threshold at scale.
            ZipSecureFile.setMaxEntrySize(16L * 1024 * 1024 * 1024)
            def counts = countRowsAndFirstRowCells(spool)
            // A zero row count means the verification itself went wrong (the parse saw no <row> at all),
            // not that the engine produced an empty sheet — the render logged the rows it wrote. Fail with
            // the state that distinguishes the two instead of an opaque "0 != 10000" further down.
            assert counts.rows > 0: "verification read no rows from the rendered workbook: " +
                    "spool=${spool.length()} bytes (source ${xlsx.length} bytes), counts=${counts}"
            return counts
        } finally {
            ZipSecureFile.setMaxEntrySize(previousMaxEntrySize)
            spool.delete()
        }
    }

    /**
     * The element's local name — the only prefix-independent identity available to a namespace-aware parser,
     * which is what {@code XMLHelper.newXMLReader()} gives us.
     *
     * <p>Matching {@code qName} instead is a trap: it carries whatever prefix the writer chose. docx4j maps
     * the spreadsheetml namespace to the default (empty) prefix only while it manages to install its own
     * {@code NamespacePrefixMapper} into the JAXB marshaller; that probe result is cached in static state, so
     * depending on what initialized JAXB earlier in the same JVM the very same render emits either
     * {@code <row>} or {@code <ns2:row>}. A qName comparison then counts zero rows — silently, and only in
     * some test orders. Falls back to qName for a parser configured without namespace awareness, where
     * localName is empty.
     */
    protected static String elementName(String localName, String qName) {
        return localName ? localName : qName
    }

    protected static Map<String, Object> countRowsAndFirstRowCells(File xlsx) {
        def pkg = OPCPackage.open(xlsx.path, PackageAccess.READ)
        try {
            def reader = new XSSFReader(pkg)
            def sheets = reader.getSheetsData()
            def sheetStream = sheets.next()
            try {
                long[] rowCount = [0L]
                int[] firstRowCells = [-1]
                int[] currentRowCells = [0]
                boolean[] inRow = [false]
                def seenElements = []

                def handler = new DefaultHandler() {
                    @Override
                    void startElement(String uri, String localName, String qName, Attributes attributes) {
                        if (seenElements.size() < 5) {
                            seenElements << "localName='$localName' qName='$qName'"
                        }
                        def name = elementName(localName, qName)
                        if (name == "row") {
                            inRow[0] = true
                            currentRowCells[0] = 0
                            rowCount[0]++
                        } else if (name == "c" && inRow[0]) {
                            currentRowCells[0]++
                        }
                    }

                    @Override
                    void endElement(String uri, String localName, String qName) {
                        if (elementName(localName, qName) == "row") {
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

                return [rows: rowCount[0], firstRowCells: firstRowCells[0], firstElements: seenElements]
            } finally {
                sheetStream.close()
            }
        } finally {
            pkg.close()
        }
    }
}
