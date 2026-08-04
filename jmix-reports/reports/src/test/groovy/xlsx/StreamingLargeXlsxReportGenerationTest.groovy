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
import io.jmix.reports.yarg.formatters.impl.StreamingXlsxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.util.XMLHelper
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Contrast case for {@link LargeXlsxReportGenerationTest}: the same 200x500k single-sheet report that
 * exhausts the heap on the in-memory engine must complete on {@link StreamingXlsxFormatter} with the
 * retained heap staying bounded, because rendered rows are flushed to disk instead of being kept as a
 * document tree.
 *
 * <p>The band tree still shares one data map across all rows (input stays cheap); the streamed output
 * goes to a temp file so the measurement is not skewed by an in-memory output buffer. Verification uses
 * streaming SAX so it does not load the produced workbook either.
 *
 * <p>The assertion is on the heap retained after a forced GC once rendering completes, not on the raw
 * allocation peak: with a multi-GB test heap the JVM legitimately lets garbage accumulate, so a peak of
 * {@code totalMemory - freeMemory} measures collector laziness rather than what the engine holds. The
 * peak is still sampled and logged for the curious.
 *
 * <p>Runs only with {@code -PincludeSlowTests=true} (the build then sets the {@code slowTests}
 * environment variable). Scale and bound are overridable the same way as in the in-memory test:
 * {@code REPORT_STRESS_ROWS=50000 REPORT_STRESS_HEAP_BOUND_MB=256 ./gradlew :reports:test -PincludeSlowTests=true}.
 */
@IgnoreIf({ env['slowTests'] != 'true' })
class StreamingLargeXlsxReportGenerationTest extends Specification {

    private static final Logger log = LoggerFactory.getLogger(StreamingLargeXlsxReportGenerationTest)

    private static final String BAND_NAME = "Data"

    private static final int COLUMNS = resolveInt("report.stress.columns", "REPORT_STRESS_COLUMNS", 200)
    private static final int ROWS = resolveInt("report.stress.rows", "REPORT_STRESS_ROWS", 500_000)
    private static final int HEAP_BOUND_MB = resolveInt("report.stress.heap.bound.mb", "REPORT_STRESS_HEAP_BOUND_MB", 512)

    private static int resolveInt(String systemProperty, String environmentVariable, int defaultValue) {
        def value = System.getProperty(systemProperty) ?: System.getenv(environmentVariable)
        return value != null ? Integer.parseInt(value) : defaultValue
    }

    def "streams a large xlsx report within a bounded heap"() {
        given: "the same single-band template and shared-row band tree as the in-memory limit reproducer"
            log.info("Streaming XLSX: {} columns x {} rows = {} cells, heap bound {} MB",
                    COLUMNS, ROWS, ((long) COLUMNS) * ROWS, HEAP_BOUND_MB)

            def template = new ReportTemplate()
            template.setContent(LargeXlsxReportGenerationTest.buildTemplate(COLUMNS))

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

            File outputFile = File.createTempFile("streaming-large-report", ".xlsx")
            outputFile.deleteOnExit()

        when: "the document is streamed to a file while heap usage is sampled"
            def runtime = Runtime.runtime
            def peakHeap = new AtomicLong(0L)
            def sampling = new AtomicBoolean(true)
            long startNanos = System.nanoTime()

            def sampler = new Thread({
                long lastLogNanos = 0L
                while (sampling.get()) {
                    long used = runtime.totalMemory() - runtime.freeMemory()
                    peakHeap.set(Math.max(peakHeap.get(), used))
                    long now = System.nanoTime()
                    if (now - lastLogNanos >= 1_000_000_000L) {
                        lastLogNanos = now
                        log.info("progress: usedHeap={} MB, peakHeap={} MB, elapsed={} s",
                                used >> 20, peakHeap.get() >> 20, (now - startNanos).intdiv(1_000_000_000L))
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

            outputFile.withOutputStream { output ->
                def formatter = new StreamingXlsxFormatter(
                        new FormatterFactoryInput("xlsx", rootBand, template, ReportOutputType.xlsx, output))
                formatter.renderDocument()
            }
            sampling.set(false)
            long elapsedMs = (System.nanoTime() - startNanos).intdiv(1_000_000L)

            long retainedAfterGc = measureRetainedHeap(runtime)
            log.info("Rendered: {} rows, elapsed={} ms, peakHeap={} MB, retainedAfterGc={} MB, output={} MB",
                    ROWS, elapsedMs, peakHeap.get() >> 20, retainedAfterGc >> 20, outputFile.length() >> 20)

            def counts = countRowsAndFirstRowCells(outputFile)

        then: "the workbook is complete and the retained heap stays within the bound"
            outputFile.length() > 0
            counts.rows == ROWS
            counts.firstRowCells == COLUMNS
            retainedAfterGc < ((long) HEAP_BOUND_MB) << 20

        cleanup:
            outputFile?.delete()
    }

    /**
     * Used heap after strongly encouraging a full collection: what the render pipeline (input tree included,
     * since Phase 1 still materializes it) actually retains, as opposed to transient allocation garbage.
     */
    protected static long measureRetainedHeap(Runtime runtime) {
        for (int i = 0; i < 3; i++) {
            System.gc()
            Thread.sleep(100L)
        }
        return runtime.totalMemory() - runtime.freeMemory()
    }

    /**
     * Same streaming SAX verification as the in-memory test, reading from the file (read-only).
     * The uncompressed sheet XML legitimately exceeds POI's default 4 GB zip-bomb threshold at full scale,
     * so the limit is raised for the duration of the check.
     */
    protected static Map<String, Object> countRowsAndFirstRowCells(File xlsx) {
        long previousMaxEntrySize = ZipSecureFile.getMaxEntrySize()
        ZipSecureFile.setMaxEntrySize(16L * 1024 * 1024 * 1024)
        try {
            return doCountRowsAndFirstRowCells(xlsx)
        } finally {
            ZipSecureFile.setMaxEntrySize(previousMaxEntrySize)
        }
    }

    protected static Map<String, Object> doCountRowsAndFirstRowCells(File xlsx) {
        def pkg = OPCPackage.open(xlsx.path, PackageAccess.READ)
        try {
            def reader = new XSSFReader(pkg)
            def sheetStream = reader.getSheetsData().next()
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
