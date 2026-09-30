/*
 * Copyright 2024 Haulmont.
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


import io.jmix.reports.yarg.formatters.impl.xlsx.Range
import io.jmix.reports.yarg.formatters.impl.xlsx.RangeDependencies
import io.jmix.reports.yarg.formatters.impl.xlsx.XlsxUtils
import io.jmix.reports.yarg.structure.BandData
import org.apache.poi.ss.util.CellReference
import org.docx4j.dml.spreadsheetdrawing.CTAbsoluteAnchor
import org.docx4j.dml.spreadsheetdrawing.CTMarker
import org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor
import spock.lang.Specification

import static org.apache.poi.ss.SpreadsheetVersion.EXCEL2007
import static org.apache.poi.ss.SpreadsheetVersion.EXCEL97


class XlsxUtilsTest extends Specification {

    def "computeColumnIndex"() throws IOException, URISyntaxException {
        when: "Cell names is: A4"
            def cellName1 = "A4"
        then: "The column index must be eq to: 1"
            XlsxUtils.computeColumnIndex(cellName1) == 1

        when: "Cell name is: B3"
            def cellName2 = "B3"
        then: "The column index must be eq to: 2"
            XlsxUtils.computeColumnIndex(cellName2) == 2

        when: "Cell name is: F6"
            def cellName3 = "F6"
        then: "The column index must be eq to: 6"
            XlsxUtils.computeColumnIndex(cellName3) == 6

        when: "Cell name is: H8"
            def cellName4 = "H8"
        then: "The column index must be eq to: 8"
            XlsxUtils.computeColumnIndex(cellName4) == 8

        when: "Cell name is: Z6"
            def cellName5 = "Z6"
        then: "The column index must be eq to: 26"
            XlsxUtils.computeColumnIndex(cellName5) == 26

        when: "Cell name is: AB15"
            def cellName6 = "AB15"
        then: "The column index must be eq to: 28"
            XlsxUtils.computeColumnIndex(cellName6) == 28

        when: "Cell name is: CV122"
            def cellName7 = "CV122"
        then: "The column index must be eq to: 100"
            XlsxUtils.computeColumnIndex(cellName7) == 100

        when: "Cell name is: AAA50"
            def cellName8 = "AAA50"
        then: "The column index must be eq to: 703"
            XlsxUtils.computeColumnIndex(cellName8) == 703
    }

    def "parsePrintArea reads one area of cell references [#formula, #version]"() {
        expect:
            XlsxUtils.parsePrintArea(formula, version) == expected

        where: "the sheet name keeps doubled quotes like Range.fromFormula keeps them"
            formula                        | version   || expected
            'Sheet1!$A$1:$B$2'             | EXCEL2007 || new Range("Sheet1", 1, 1, 2, 2)
            "'My sheet'!\$B\$3:\$D\$20"    | EXCEL2007 || new Range("My sheet", 2, 3, 4, 20)
            "'O''Brien'!\$A\$1"            | EXCEL2007 || new Range("O''Brien", 1, 1, 1, 1)
            "'North, South'!\$A\$1:\$B\$2" | EXCEL2007 || new Range("North, South", 1, 1, 2, 2)
            "'Q1!\$'!\$A\$1"               | EXCEL2007 || new Range('Q1!$', 1, 1, 1, 1)
            '$A$1:$B$2'                    | EXCEL2007 || new Range(null, 1, 1, 2, 2)
            'Sheet1!A1:B2'                 | EXCEL2007 || new Range("Sheet1", 1, 1, 2, 2)
            'Sheet1!$a$1:$b$2'             | EXCEL2007 || new Range("Sheet1", 1, 1, 2, 2)
            'Sheet1!$B$2:$A$1'             | EXCEL2007 || new Range("Sheet1", 1, 1, 2, 2)
            'Sheet1!$XFD$1048576'          | EXCEL2007 || new Range("Sheet1", 16384, 1048576, 16384, 1048576)
            'Sheet1!$1:$20'                | EXCEL2007 || new Range("Sheet1", 1, 1, 16384, 20)
            'Sheet1!$20:$1'                | EXCEL2007 || new Range("Sheet1", 1, 1, 16384, 20)
            'Sheet1!$1:$20'                | EXCEL97   || new Range("Sheet1", 1, 1, 256, 20)
            'Sheet1!$A:$F'                 | EXCEL2007 || new Range("Sheet1", 1, 1, 6, 1048576)
            'Sheet1!F:A'                   | EXCEL97   || new Range("Sheet1", 1, 1, 6, 65536)
    }

    def "parsePrintArea reads lower-case column letters under any default locale"() {
        given: "the Turkish locale upper-cases i to a dotted capital I"
            def defaultLocales = [Locale.getDefault(), Locale.getDefault(Locale.Category.DISPLAY),
                                  Locale.getDefault(Locale.Category.FORMAT)]
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))

        expect:
            XlsxUtils.parsePrintArea('Sheet1!$i$1:$ai$2', EXCEL2007) == new Range("Sheet1", 9, 1, 35, 2)

        cleanup:
            Locale.setDefault(defaultLocales[0])
            Locale.setDefault(Locale.Category.DISPLAY, defaultLocales[1])
            Locale.setDefault(Locale.Category.FORMAT, defaultLocales[2])
    }

    def "parsePrintArea does not read other print areas [#formula]"() {
        expect:
            XlsxUtils.parsePrintArea(formula, EXCEL2007) == null

        where:
            formula << [null, '', 'Sheet1!$A$1:$B$2,Sheet1!$D$1:$E$2', 'OFFSET(Sheet1!$A$1,0,0,5,2)',
                        'Sheet1!$A$1:Sheet1!$B$2', 'Sheet1!#REF!', "'Sheet1!\$A\$1", 'Sheet1!$A$1:$B']
    }

    def "parsePrintAreas reads areas separated with commas [#formula]"() {
        expect:
            XlsxUtils.parsePrintAreas(formula, EXCEL2007) == expected

        where:
            formula                                        || expected
            'Sheet1!$A$1:$B$2'                             || [new Range("Sheet1", 1, 1, 2, 2)]
            'Sheet1!$A$1:$B$2,Sheet1!$D$1:$E$2'            || [new Range("Sheet1", 1, 1, 2, 2), new Range("Sheet1", 4, 1, 5, 2)]
            "'North, South'!\$A\$1,'North, South'!\$C\$1"  || [new Range("North, South", 1, 1, 1, 1),
                                                               new Range("North, South", 3, 1, 3, 1)]
            "'O''Brien'!\$A\$1,'O''Brien'!\$C\$1"          || [new Range("O''Brien", 1, 1, 1, 1), new Range("O''Brien", 3, 1, 3, 1)]
    }

    def "parsePrintAreas does not read a print area with a part it cannot read [#formula]"() {
        expect:
            XlsxUtils.parsePrintAreas(formula, EXCEL2007) == null

        where:
            formula << [null, 'Sheet1!$A$1,OFFSET(Sheet1!$A$1,0,0,5,2)', 'Sheet1!$A$1,', 'OFFSET(Sheet1!$A$1,0,0,5,2)']
    }

    def "formatPrintArea writes an area spanning all columns as whole rows, all rows as whole columns [#expected]"() {
        expect:
            XlsxUtils.formatPrintArea(range, version) == expected

        where:
            range                                     | version   || expected
            new Range("Sheet1", 1, 1, 2, 51)          | EXCEL2007 || "'Sheet1'!\$A\$1:\$B\$51"
            new Range("Sheet1", 3, 5, 3, 5)           | EXCEL2007 || "'Sheet1'!\$C\$5"
            new Range("Sheet1", 1, 1, 16384, 51)      | EXCEL2007 || "'Sheet1'!\$1:\$51"
            new Range("Sheet1", 1, 1, 256, 51)        | EXCEL97   || "'Sheet1'!\$1:\$51"
            new Range("Sheet1", 1, 1, 256, 51)        | EXCEL2007 || "'Sheet1'!\$A\$1:\$IV\$51"
            new Range("Sheet1", 1, 1, 2, 1048576)     | EXCEL2007 || "'Sheet1'!\$A:\$B"
            new Range("Sheet1", 1, 1, 16384, 1048576) | EXCEL2007 || "'Sheet1'!\$1:\$1048576"
            new Range("O''Brien", 1, 1, 1, 3)         | EXCEL2007 || "'O''Brien'!\$A\$1:\$A\$3"
    }

    def "getAnchorRange returns the cells a drawing object is anchored in"() {
        expect: "all the cells of a two-cell anchor, the anchor cell of an object sized in EMUs"
            XlsxUtils.getAnchorRange("Sheet1", twoCellAnchor(1, 2, 4, 6)) == new Range("Sheet1", 2, 3, 5, 7)
            XlsxUtils.getAnchorRange("Sheet1", oneCellAnchor(1, 2)) == new Range("Sheet1", 2, 3, 2, 3)
            XlsxUtils.getAnchorRange("Sheet1", new CTAbsoluteAnchor()) == null
    }

    def "getRenderedBandBlocks covers the rows of the nested bands rendered on the same sheet"() {
        given: "a group with items and a note on another sheet, a group right of it; a data band that is not rendered"
            def root = new BandData(BandData.ROOT_BAND_NAME)
            def group1 = band(root, "Group")
            def item1 = band(group1, "Item")
            def item2 = band(group1, "Item")
            def note = band(group1, "Note")
            def group2 = band(root, "Group")
            def data = band(root, "Data")
            def total = band(data, "Total")
            Map<BandData, Range> rendered = new IdentityHashMap<>([
                    (group1): range("A2:B2"), (item1): range("A3:B3"), (item2): range("A4:B4"),
                    (note)  : range("Notes!A10:A10"), (group2): range("C2:D2"), (total): range("A6:B6")])
            def templates = [Group: range("A2:B2"), Item: range("A3:B3"), Note: range("Notes!A1:A1"), Total: range("A4:B4")]

        when:
            def blocks = XlsxUtils.getRenderedBandBlocks(root, { rendered[it] }, { templates[it.name] })

        then: "the group block ends with the last item, the note on another sheet and the data band are left out"
            blocks.templates() == templates.values() as Set
            blocks.resultsForTemplate(templates.Group) == [range("A2:D4")]
            blocks.resultsForTemplate(templates.Item) == [range("A3:B4")]
            blocks.resultsForTemplate(templates.Note) == [range("Notes!A10:A10")]
            blocks.resultsForTemplate(templates.Total) == [range("A6:B6")]
    }

    def "rebasePrintArea covers what the bands and the objects of the print area are rendered to [#description]"() {
        expect:
            XlsxUtils.rebasePrintArea(range(printArea), dependencies(bands), dependencies(objects), version) ==
                    (expected != null ? range(expected) : null)

        where:
            description                                           | printArea     | bands                                                     | objects                  | version   || expected
            "bands grow down"                                     | "A1:B2"       | ["A1:B1": ["A1:B1"], "A2:B2": ["A2:B51"]]                 | [:]                      | EXCEL2007 || "A1:B51"
            "bands outside the area are left out"                 | "A1:B2"       | ["A1:B2": ["A1:B4"], "A3:B3": ["A5:B5"]]                  | [:]                      | EXCEL2007 || "A1:B4"
            "nothing intersecting the area is rendered"           | "A10:B10"     | ["A1:B1": ["A1:B1"]]                                      | [:]                      | EXCEL2007 || null
            "whole columns cover every rendered row"              | "A1:B1048576" | ["A1:B1": ["A1:B51"]]                                     | [:]                      | EXCEL2007 || null
            "whole rows keep all the columns"                     | "A1:XFD2"     | ["A2:B2": ["A2:B51"]]                                     | [:]                      | EXCEL2007 || "A2:XFD51"
            "whole rows keep all the columns, bands packed left"  | "A1:XFD1"     | ["A1:A1": ["A1:B1"], "D1:D1": ["C1:C1"]]                  | [:]                      | EXCEL2007 || "A1:XFD1"
            "a vertical band grows right"                         | "A1:A1"       | ["A1:A1": ["A1:C1"]]                                      | [:]                      | EXCEL2007 || "A1:C1"
            "a wider band keeps the columns of the area"          | "A1:B2"       | ["A1:C2": ["A1:C4"]]                                      | [:]                      | EXCEL2007 || "A1:B4"
            "the blank columns left of the bands are kept"        | "A1:C2"       | ["B1:C2": ["B1:C4"]]                                      | [:]                      | EXCEL2007 || "A1:C4"
            "the blank columns right of the bands are kept"       | "A1:D2"       | ["A1:B2": ["A1:B4"]]                                      | [:]                      | EXCEL2007 || "A1:D4"
            "the blank columns move with the bands"               | "D1:E1"       | ["D1:D1": ["B1:C1"]]                                      | [:]                      | EXCEL2007 || "B1:D1"
            "a band left of the area takes its blank columns"     | "C1:E1"       | ["A1:A1": ["A1:B1"], "D1:D1": ["C1:D1"]]                  | [:]                      | EXCEL2007 || "C1:E1"
            "a band right of the area takes its blank columns"    | "A1:C1"       | ["A1:A1": ["A1:B1"], "D1:D1": ["C1:D1"]]                  | [:]                      | EXCEL2007 || "A1:B1"
            "a band in other rows leaves the blank columns"       | "A1:C1"       | ["A1:A1": ["A1:B1"], "D2:D2": ["C2:D2"]]                  | [:]                      | EXCEL2007 || "A1:D1"
            "a band on another sheet leaves the blank columns"    | "A1:C1"       | ["A1:A1": ["A1:B1"], "Other!D1:D1": ["Other!C1:D1"]]      | [:]                      | EXCEL2007 || "A1:D1"
            "the blank columns end at the first column"           | "B1:E1"       | ["D1:D1": ["A1:A1"]]                                      | [:]                      | EXCEL2007 || "A1:B1"
            "the blank columns end at the last column"            | "B1:XFB1"     | ["B1:B1": ["B1:F1"]]                                      | [:]                      | EXCEL2007 || "B1:XFD1"
            "an object in place is covered inside the area"       | "A2:F4"       | ["A2:B2": ["A2:B2"]]                                      | ["D1:F5": ["D1:F5"]]     | EXCEL2007 || "A2:F4"
            "an object moves with its band"                       | "A1:C7"       | ["A1:B2": ["A1:B4"], "A3:B3": ["A5:B5"]]                  | ["A3:C7": ["A5:C9"]]     | EXCEL2007 || "A1:C9"
            "an object moved above the first row"                 | "A1:C6"       | [:]                                                       | ["A2:C6": ["A-1:C3"]]    | EXCEL2007 || "A1:C3"
            "an object moved entirely above the first row"        | "A1:C6"       | ["A4:C4": ["A3:C3"]]                                      | ["A2:C3": ["A-2:C-1"]]   | EXCEL2007 || "A3:C3"
            "an object moved below the last row"                  | "A1:C4"       | ["A1:C1": ["A1:C1"]]                                      | ["A2:C4": ["A65535:C65537"]] | EXCEL97 || "A1:C65536"
            "an object moved entirely below the last row"         | "A1:C3"       | ["A1:C1": ["A1:C1"]]                                      | ["A2:C3": ["A65537:C65538"]] | EXCEL97 || "A1:C1"
    }

    protected static BandData band(BandData parent, String name) {
        def band = new BandData(name, parent)
        parent.addChild(band)
        return band
    }

    /** A range like {@code A1:B2}, {@code A-1:C3} or {@code Other!A1:B2}, on Sheet1 if no sheet is given. */
    protected static Range range(String reference) {
        def matcher = reference =~ /^(?:(\w+)!)?([A-Z]+)(-?\d+)(?::([A-Z]+)(-?\d+))?$/
        assert matcher.matches(): "Bad range $reference"
        int firstColumn = CellReference.convertColStringToIndex(matcher.group(2)) + 1
        int firstRow = matcher.group(3) as int
        return new Range(matcher.group(1) ?: "Sheet1", firstColumn, firstRow,
                matcher.group(4) ? CellReference.convertColStringToIndex(matcher.group(4)) + 1 : firstColumn,
                matcher.group(5) ? matcher.group(5) as int : firstRow)
    }

    protected static RangeDependencies dependencies(Map<String, List<String>> templateToResults) {
        def dependencies = new RangeDependencies()
        templateToResults.each { template, results ->
            results.each { dependencies.addDependency(range(template), range(it)) }
        }
        return dependencies
    }

    protected static CTTwoCellAnchor twoCellAnchor(int col1, int row1, int col2, int row2) {
        def anchor = new CTTwoCellAnchor()
        anchor.from = marker(col1, row1)
        anchor.to = marker(col2, row2)
        return anchor
    }

    protected static CTOneCellAnchor oneCellAnchor(int col, int row) {
        def anchor = new CTOneCellAnchor()
        anchor.from = marker(col, row)
        return anchor
    }

    protected static CTMarker marker(int col, int row) {
        def marker = new CTMarker()
        marker.col = col
        marker.row = row
        return marker
    }
}
