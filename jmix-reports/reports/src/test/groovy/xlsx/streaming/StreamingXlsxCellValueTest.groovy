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

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Sheet
import xlsx.StreamingBaseXlsxRenderTest

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Value typing of the streaming XLSX formatter — must be byte-compatible with the non-streaming
 * engine (see {@code XlsxCellValueTest}): text stays text, numbers/dates become numeric cells,
 * booleans become boolean cells, formatted values become formatted text.
 */
class StreamingXlsxCellValueTest extends StreamingBaseXlsxRenderTest {

    private Sheet renderSingle(Object value, String format = null) {
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${value}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        if (format != null) {
            withFieldFormats(root, fieldFormat("Data.value", format))
        }
        addBand(root, "Data", [value: value])
        return renderAndReadFirstSheet(template, root)
    }

    def "string value -> text cell"() {
        given:
        def sheet = renderSingle("Alice")

        expect:
        requireCell(sheet, 0, 0).cellType == CellType.STRING
        stringValue(sheet, 0, 0) == "Alice"
    }

    def "integer -> numeric cell"() {
        given:
        def sheet = renderSingle(42)

        expect:
        requireCell(sheet, 0, 0).cellType == CellType.NUMERIC
        numericValue(sheet, 0, 0) == 42.0d
    }

    def "double -> numeric cell"() {
        given:
        def sheet = renderSingle(3.5d)

        expect:
        numericValue(sheet, 0, 0) == 3.5d
    }

    def "integer beyond double precision is written as a rounded numeric cell"() {
        given: "a Long larger than 2^53 that a double cannot represent exactly"
        def sheet = renderSingle(9007199254740993L)

        expect: "POI sets a numeric cell only through a double, so the value is rounded; unlike the " +
                "non-streaming engine it is not kept exact in the raw file (a field format or a string select is needed)"
        requireCell(sheet, 0, 0).cellType == CellType.NUMERIC
        numericValue(sheet, 0, 0) == 9007199254740993L.doubleValue()
    }

    def "Float keeps its exact decimal instead of float-to-double widening artifacts"() {
        given: "0.1f widens to 0.10000000149011612 when written via Float.doubleValue()"
        def sheet = renderSingle(0.1f)

        expect: "the numeric cell reads back as 0.1, matching the non-streaming engine (String.valueOf)"
        requireCell(sheet, 0, 0).cellType == CellType.NUMERIC
        numericValue(sheet, 0, 0) == 0.1d
    }

    def "boolean -> boolean cell"() {
        given:
        def sheet = renderSingle(true)

        expect:
        requireCell(sheet, 0, 0).cellType == CellType.BOOLEAN
        booleanValue(sheet, 0, 0)
    }

    def "Date -> Excel serial numeric"() {
        given:
        def date = new Date(1700000000000L)
        def sheet = renderSingle(date)

        expect:
        numericValue(sheet, 0, 0) == DateUtil.getExcelDate(date)
    }

    def "LocalDate -> Excel serial numeric"() {
        given:
        def date = LocalDate.of(2024, 3, 15)
        def sheet = renderSingle(date)

        expect:
        numericValue(sheet, 0, 0) == DateUtil.getExcelDate(date)
    }

    def "LocalDateTime -> Excel serial numeric"() {
        given:
        def dateTime = LocalDateTime.of(2024, 3, 15, 13, 30)
        def sheet = renderSingle(dateTime)

        expect:
        numericValue(sheet, 0, 0) == DateUtil.getExcelDate(dateTime)
    }

    def "LocalTime NOON -> half-day fraction"() {
        given:
        def sheet = renderSingle(LocalTime.NOON)

        expect:
        numericValue(sheet, 0, 0) == 0.5d
    }

    def "missing value -> blank cell"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${absent}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [:])

        when:
        def sheet = renderAndReadFirstSheet(template, root)
        def cell = cellOrNull(sheet, 0, 0)

        then: "the cell is emitted and explicitly blank (matches the non-streaming engine's setBlank)"
        cell != null
        cell.cellType == CellType.BLANK
    }

    def "format string -> formatted text cell"() {
        given:
        def sheet = renderSingle(1234.5d, "#,##0.00")

        expect:
        requireCell(sheet, 0, 0).cellType == CellType.STRING
        stringValue(sheet, 0, 0) == new DecimalFormat("#,##0.00").format(1234.5d)
    }

    def "multiple aliases and static text concatenate into text"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'Total: ${a} of ${b}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [a: 5, b: 10])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "Total: 5 of 10"
    }

    def "text with a repeated alias resolves every occurrence"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'x=${v}, again ${v} and ${w}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 7, w: "z"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "x=7, again 7 and z"
    }

    def "lone alias with characters outside the alias grammar is kept as literal text"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${foo-bar}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [x: 1])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the hyphen is rejected by the alias grammar, so the text stays literal (as the non-streaming engine)"
        stringValue(sheet, 0, 0) == '${foo-bar}'
    }

    def "lone alias with surrounding whitespace stays text (byte-compatible with non-streaming)"() {
        given: "a numeric alias with trailing spaces — not a whole-cell single alias"
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${value}  ')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [value: 42])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the untrimmed value fails the full single-alias match, so XlsxFormatter writes text with the spaces kept"
        requireCell(sheet, 0, 0).cellType == CellType.STRING
        stringValue(sheet, 0, 0) == "42  "
    }
}
