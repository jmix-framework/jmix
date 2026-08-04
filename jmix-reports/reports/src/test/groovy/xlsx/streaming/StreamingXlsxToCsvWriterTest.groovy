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

import io.jmix.reports.yarg.exception.ReportingInterruptedException
import io.jmix.reports.yarg.formatters.impl.streaming.StreamingXlsxToCsvWriter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification

/**
 * The streamed XLSX -> CSV SAX conversion is a long phase for huge exports; it must react to thread
 * interruption (cancellation) like the non-streaming {@code saveXlsxAsCsv} did, instead of running to
 * completion.
 */
class StreamingXlsxToCsvWriterTest extends Specification {

    def "conversion reacts to thread interruption with ReportingInterruptedException"() {
        given: "a small xlsx file on disk"
        def xlsx = File.createTempFile("cancel-csv", ".xlsx")
        new XSSFWorkbook().withCloseable { wb ->
            def sheet = wb.createSheet("Sheet1")
            (0..5).each { r -> sheet.createRow(r).createCell(0).setCellValue("v$r") }
            xlsx.withOutputStream { wb.write(it) }
        }
        def out = new ByteArrayOutputStream()

        when: "the current thread is interrupted before converting"
        Thread.currentThread().interrupt()
        StreamingXlsxToCsvWriter.convert(xlsx, out)

        then: "the conversion aborts with the cancellation type, not a wrapped formatting error"
        thrown(ReportingInterruptedException)

        cleanup:
        Thread.interrupted() // clear the flag so it does not leak into other tests
        xlsx.delete()
    }
}
