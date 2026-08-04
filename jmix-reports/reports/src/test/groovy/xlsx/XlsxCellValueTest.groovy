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

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Verifies that a single {@code ${alias}} placeholder is rendered with the correct Excel cell type and value
 * for every supported Java type, and that value formats are applied.
 */
class XlsxCellValueTest extends BaseXlsxRenderTest {

    def "string value is written as a text cell"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${name}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [name: "Hello world"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == "Hello world"
    }

    def "integer and double values are written as numeric cells"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${count}')
                cell(sheet, 0, 1, '${price}')
                defineBand(wb, "Data", 0, 0, 0, 1)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [count: 42, price: 3.5d])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            requireCell(sheet, 0, 0).cellType == CellType.NUMERIC
            numericValue(sheet, 0, 0) == 42.0d
            numericValue(sheet, 0, 1) == 3.5d
    }

    def "boolean values are written as boolean cells"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${yes}')
                cell(sheet, 0, 1, '${no}')
                defineBand(wb, "Data", 0, 0, 0, 1)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [yes: true, no: false])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            requireCell(sheet, 0, 0).cellType == CellType.BOOLEAN
            booleanValue(sheet, 0, 0)
            !booleanValue(sheet, 0, 1)
    }

    def "LocalTime is written as an Excel time fraction"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${time}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [time: LocalTime.NOON])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            numericValue(sheet, 0, 0) == 0.5d
    }

    def "java.util.Date, LocalDate and LocalDateTime are written as Excel serial dates"() {
        given:
            def localDate = LocalDate.of(2025, 1, 15)
            def localDateTime = LocalDateTime.of(2025, 1, 15, 10, 30)
            def utilDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())

            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${utilDate}')
                cell(sheet, 0, 1, '${localDate}')
                cell(sheet, 0, 2, '${localDateTime}')
                defineBand(wb, "Data", 0, 0, 0, 2)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [utilDate: utilDate, localDate: localDate, localDateTime: localDateTime])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            numericValue(sheet, 0, 0) == DateUtil.getExcelDate(utilDate)
            numericValue(sheet, 0, 1) == DateUtil.getExcelDate(localDate)
            numericValue(sheet, 0, 2) == DateUtil.getExcelDate(localDateTime)
    }

    def "missing value replaces the placeholder with an empty cell"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${missing}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [present: "x"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "the literal placeholder is not left in the output"
            def c = cellOrNull(sheet, 0, 0)
            c == null || c.cellType == CellType.BLANK ||
                    (c.cellType == CellType.STRING && c.stringCellValue.isEmpty())
    }

    def "a cell with several placeholders is rendered as concatenated text"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${first} ${last}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [first: "John", last: "Doe"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == "John Doe"
    }

    def "static text without placeholders is preserved"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, "Total:")
                cell(sheet, 0, 1, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 1)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [value: "42"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == "Total:"
            stringValue(sheet, 0, 1) == "42"
    }

    def "a numeric value format is applied and produces a formatted text cell"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${amount}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            withFieldFormats(root, fieldFormat("Data.amount", "#,##0.00"))
            addBand(root, "Data", [amount: 1234.5d])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "compared against the same DecimalFormat the formatter uses, so the test is locale-independent"
            stringValue(sheet, 0, 0) == new DecimalFormat("#,##0.00").format(1234.5d)
    }

    def "a date value format is applied and produces a formatted text cell"() {
        given:
            def localDateTime = LocalDateTime.of(2025, 1, 15, 10, 30)
            def utilDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())

            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${date}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            withFieldFormats(root, fieldFormat("Data.date", "dd.MM.yyyy"))
            addBand(root, "Data", [date: utilDate])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == new SimpleDateFormat("dd.MM.yyyy").format(utilDate)
    }
}
