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

import io.jmix.reports.yarg.exception.ReportFormattingException
import xlsx.StreamingBaseXlsxRenderTest

/**
 * Formula handling of the streaming XLSX formatter, computed at row-write time: in-row ("inner")
 * formulas shift with each band instance; trailing aggregate ("outer") formulas below the data grow
 * to cover all rendered band rows.
 */
class StreamingXlsxFormulaTest extends StreamingBaseXlsxRenderTest {

    def "inner formula shifts per band instance"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${a}')
            cell(sheet, 0, 1, '${b}')
            formulaCell(sheet, 0, 2, 'A1*B1')
            defineBand(wb, "Data", 0, 0, 0, 2)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [a: 2, b: 3])
        addBand(root, "Data", [a: 4, b: 5])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        formula(sheet, 0, 2) == "A1*B1"
        formula(sheet, 1, 2) == "A2*B2"
    }

    def "outer SUM below the band grows to cover rendered band rows"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            formulaCell(sheet, 1, 0, 'SUM(A1:A1)')
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 10])
        addBand(root, "Data", [v: 20])
        addBand(root, "Data", [v: 30])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        formula(sheet, 3, 0) == "SUM(A1:A3)"
    }

    def "digit-suffixed function names are not treated as cell references"() {
        given:
        // The band sits on template row 10, so the "10" in LOG10 collides with an in-band row number.
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 9, 0, '${a}')
            formulaCell(sheet, 9, 1, 'LOG10(A10)')
            defineBand(wb, "Data", 9, 0, 9, 1)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [a: 100])
        addBand(root, "Data", [a: 1000])
        addBand(root, "Data", [a: 10000])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        formula(sheet, 9, 1) == "LOG10(A10)"
        formula(sheet, 10, 1) == "LOG10(A11)"
        formula(sheet, 11, 1) == "LOG10(A12)"
    }

    def "outer SUM with absolute references grows to cover rendered band rows"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            formulaCell(sheet, 1, 0, 'SUM($A$1:$A$1)')
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 10])
        addBand(root, "Data", [v: 20])
        addBand(root, "Data", [v: 30])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "rows are re-targeted; the author's absolute markers are preserved"
        formula(sheet, 3, 0) == 'SUM($A$1:$A$3)'
    }

    def "outer SUM over an empty band renders the empty-range error text"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            formulaCell(sheet, 1, 0, 'SUM(A1:A1)')
        }
        def root = rootBand("Data")

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "ERROR: Formula references to empty range"
    }

    def "string literal containing a cell-like token is not rewritten"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            formulaCell(sheet, 0, 1, 'IF(A1>0,"see A1","-")')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        (1..2).each { addBand(root, "Data", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        formula(sheet, 1, 1) == 'IF(A2>0,"see A1","-")'
    }

    def "sheet-qualified reference is left untouched while plain reference shifts"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            formulaCell(sheet, 0, 1, 'Sheet1!A1+A1')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        (1..2).each { addBand(root, "Data", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        formula(sheet, 1, 1) == 'Sheet1!A1+A2'
    }

    def "single reference into a band re-bases onto the last rendered instance"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            formulaCell(sheet, 1, 0, 'A1+100')
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "band rendered rows 0..2, the static row landed on row 3, A1 points at the last instance"
        formula(sheet, 3, 0) == 'A3+100'
    }

    def "grand total referencing two static subtotals is re-based"() {
        given: "band1 row0, subtotal row1, band2 row2, subtotal row3, grand total row4"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${v}')
            formulaCell(s, 1, 0, 'SUM(A1:A1)')
            cell(s, 2, 0, '${v}')
            formulaCell(s, 3, 0, 'SUM(A3:A3)')
            formulaCell(s, 4, 0, 'A2+A4')
            defineBand(wb, "Band1", 0, 0, 0, 0)
            defineBand(wb, "Band2", 2, 0, 2, 0)
        }
        def root = rootBand("Band1", "Band2")
        (1..3).each { addBand(root, "Band1", [v: it]) }
        (1..2).each { addBand(root, "Band2", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "B1 -> rows 0..2, subtotal1 -> row 3, B2 -> rows 4..5, subtotal2 -> row 6, grand -> row 7"
        formula(sheet, 3, 0) == 'SUM(A1:A3)'
        formula(sheet, 6, 0) == 'SUM(A5:A6)'
        formula(sheet, 7, 0) == 'A4+A7'
    }

    def "inner area formula extending outside the band is rejected instead of pinning the outer endpoint"() {
        given: "band on row 0; an in-band SUM whose range reaches below the band (A1:A5)"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            formulaCell(sheet, 0, 1, 'SUM(A1:A5)')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when:
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("extends below it")
    }

    def "in-band single reference to a row below the band is rejected"() {
        given: "band on row 0; an in-band formula referencing A3, a static total row below the band"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            formulaCell(sheet, 0, 1, 'A3')
            cell(sheet, 2, 0, 'total')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        (1..2).each { addBand(root, "Data", [v: it]) }

        when: "forward-only rendering writes the row below after the band, so its position is unknown"
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("below its band")
    }

    def "in-band absolute reference to a row below the band is rejected"() {
        given: "the percent-of-total idiom \$A\$3 referencing a fixed cell below the band"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            formulaCell(sheet, 0, 1, '$A$3')
            cell(sheet, 2, 0, 'total')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        (1..2).each { addBand(root, "Data", [v: it]) }

        when: "an absolute row below the band is equally unresolvable forward-only"
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("below its band")
    }

    def "an outer formula referencing a band laid out below it is rejected"() {
        given: "a total placed ABOVE the data — it references the Data band whose rows are written later"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            formulaCell(sheet, 0, 0, 'SUM(A2:A2)')
            cell(sheet, 1, 0, '${v}')
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "forward-only writing cannot resolve a reference to a band that renders after the formula"
        render(template, root)

        then: "rejected up front with a clear message, not the misleading empty-range error text"
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("below it")
    }

    def "an outer formula referencing a static row laid out below it is rejected"() {
        given: "a header formula =A3 references a static footer BELOW a band whose rows are written later"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            formulaCell(sheet, 0, 0, 'A3')
            cell(sheet, 1, 0, '${v}')
            cell(sheet, 2, 0, 'TOTAL')
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "forward-only rendering writes the footer after the band, so its shifted position is unknown"
        render(template, root)

        then: "rejected up front, not silently kept as the template row (which would point into band data)"
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("below it")
    }

    def "a whole-column outer formula above a band is not rejected"() {
        given: "a grand total ABOVE the data using a whole-column reference, which always covers the rendered rows"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            formulaCell(sheet, 0, 1, 'SUM(A:A)')
            cell(sheet, 1, 0, '${v}')
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "a whole-column reference needs no growth; Excel computes it on open, so it must render"
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the total is kept as authored, not wrongly rejected as a forward reference"
        formula(sheet, 0, 1) == "SUM(A:A)"
    }

    def "in-band formula referencing a static row above a shifted band remaps to its output row"() {
        given: "band Top expands above a static base row that the lower Data band's formula references"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${t}')
            defineBand(wb, "Top", 0, 0, 0, 0)
            cell(s, 1, 0, 'base')
            cell(s, 2, 0, '${d}')
            formulaCell(s, 2, 1, 'A2+A3')
            defineBand(wb, "Data", 2, 0, 2, 1)
        }
        def root = rootBand("Top", "Data")
        (1..3).each { addBand(root, "Top", [t: it]) }
        addBand(root, "Data", [d: 100])

        when: "Top renders rows 0..2, the static base shifts to row 3, Data to row 4"
        def sheet = renderAndReadFirstSheet(template, root)

        then: "A2 (the static base) remaps to A4, A3 (the in-band cell) shifts to A5"
        formula(sheet, 4, 1) == 'A4+A5'
    }

    def "inner area anchored above the band grows its end per instance (running total)"() {
        given: "static row 0; band on row 1; in-band running total SUM(A1:A2) anchored at the static A1"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'base')
            cell(sheet, 1, 0, '${v}')
            formulaCell(sheet, 1, 1, 'SUM(A1:A2)')
            defineBand(wb, "Data", 1, 0, 1, 1)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the anchor stays at A1 while the end tracks each rendered instance"
        formula(sheet, 1, 1) == 'SUM(A1:A2)'
        formula(sheet, 2, 1) == 'SUM(A1:A3)'
        formula(sheet, 3, 1) == 'SUM(A1:A4)'
    }

    def "inner absolute reference does not shift with the instance"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            formulaCell(sheet, 0, 1, '$A$1*2')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        (1..2).each { addBand(root, "Data", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "an absolute row reference is anchored, so it stays put across instances (Excel semantics, matches the non-streaming engine)"
        formula(sheet, 0, 1) == '$A$1*2'
        formula(sheet, 1, 1) == '$A$1*2'
    }

    def "in-band running total with an absolute anchor keeps the anchor and grows its relative end"() {
        given: 'single-row band on row 0; a running total SUM($A$1:A1) mixing an absolute start and a relative end'
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            formulaCell(sheet, 0, 1, 'SUM($A$1:A1)')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the absolute start stays at A1, the relative end tracks each rendered instance"
        formula(sheet, 0, 1) == 'SUM($A$1:A1)'
        formula(sheet, 1, 1) == 'SUM($A$1:A2)'
        formula(sheet, 2, 1) == 'SUM($A$1:A3)'
    }

    def "alias inside an in-band formula is substituted before parsing"() {
        given: 'in-band formula A1*${factor} whose alias resolves to a per-instance value (written raw, as POI cannot parse ${})'
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            def fCell = sheet.getRow(0).createCell(1)
            fCell.getCTCell().addNewF().setStringValue('A1*${factor}')
            defineBand(wb, "Data", 0, 0, 0, 1)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [v: 2, factor: 10])
        addBand(root, "Data", [v: 3, factor: 20])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the alias is substituted first, then the relative cell reference shifts per instance"
        formula(sheet, 0, 1) == 'A1*10'
        formula(sheet, 1, 1) == 'A2*20'
    }

    def "single reference to an empty band renders the empty-range error text"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            formulaCell(sheet, 1, 0, 'A1*2')
        }
        def root = rootBand("Data")

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "ERROR: Formula references to empty range"
    }

    def "outer area extending past a band keeps the static rows below in range"() {
        given: "band row0, two static rows below, total summing A1:A3 (band + two statics)"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${v}')
            cell(sheet, 1, 0, 'x')
            cell(sheet, 2, 0, 'y')
            formulaCell(sheet, 3, 0, 'SUM(A1:A3)')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "band renders rows 0..2, statics shift to rows 3..4, total to row 5"
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the band end grows to the last rendered band row and the static end shifts down"
        formula(sheet, 5, 0) == "SUM(A1:A5)"
    }

    def "outer area starting in a static row above a band grows over the rendered band rows"() {
        given: "static row0, band row1, total summing A1:A2 (static + first band row)"
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, 'base')
            cell(sheet, 1, 0, '${v}')
            formulaCell(sheet, 2, 0, 'SUM(A1:A2)')
            defineBand(wb, "Data", 1, 0, 1, 0)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "static stays on row0, band renders rows 1..3, total to row4"
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the static end keeps A1 while the band end grows to the last rendered band row A4"
        formula(sheet, 4, 0) == "SUM(A1:A4)"
    }

    def "outer SUM over part of a multi-row band honors the endpoint offset"() {
        given: "a 2-row band; a total summing only the SECOND row (A2) of the band block"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, 'hdr')
            cell(s, 1, 0, '${v}')
            defineBand(wb, "Data", 0, 0, 1, 0)
            formulaCell(s, 2, 0, 'SUM(A2:A2)')
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [v: it]) }

        when: "the band renders 3 instances x 2 rows (rows 0..5), the total lands on row 6"
        def sheet = renderAndReadFirstSheet(template, root)

        then: "the range starts at the 2nd row of the first instance (A2) and ends at the 2nd row of the last instance (A6)"
        formula(sheet, 6, 0) == 'SUM(A2:A6)'
    }
}
