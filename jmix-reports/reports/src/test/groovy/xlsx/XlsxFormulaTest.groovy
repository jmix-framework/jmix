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

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCell
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType

/**
 * Runs the {@link BaseXlsxFormulaTest} formula contract against the in-memory {@code XlsxFormatter}, plus the
 * empty-band case, where the two engines deliberately differ: this one leaves the formula pointing at the
 * template range, while the streaming engine writes the {@code "ERROR: Formula references to empty range"}
 * text (asserted in {@code StreamingXlsxFormulaTest}).
 */
class XlsxFormulaTest extends BaseXlsxFormulaTest {

    /**
     * The idiomatic placement for this engine: the aggregate lives in a totals band. It cannot go on a row
     * outside every band, because this engine emits band ranges only. The streaming engine is the exact
     * opposite — see {@code StreamingXlsxFormulaTest}.
     */
    def "an aggregate formula in a totals band grows to cover all rows of the referenced band"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 1, '${price}')
                formulaCell(sheet, 1, 1, "SUM(B1:B1)")
                defineBand(wb, "Data", 0, 1, 0, 1)
                defineBand(wb, "Total", 1, 1, 1, 1)
            }
            def root = rootBand("Data", "Total")
            addBand(root, "Data", [price: 10])
            addBand(root, "Data", [price: 20])
            addBand(root, "Data", [price: 30])
            addBand(root, "Total", [:])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "the SUM range is expanded from the 3 rendered data rows"
            def total = findFormulaCell(sheet)
            total != null
            total.cellFormula == "SUM(B1:B3)"
    }

    def "an aggregate formula referencing a band with no data is left unchanged"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 1, '${price}')
                formulaCell(sheet, 1, 1, "SUM(B1:B1)")
                defineBand(wb, "Data", 0, 1, 0, 1)
                defineBand(wb, "Total", 1, 1, 1, 1)
            }
            def root = rootBand("Data", "Total")
            // no Data rows are produced, so the Data range never makes it into the rendered ranges
            addBand(root, "Total", [:])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "the formula is not expanded — it still refers to the original template range"
            def total = findFormulaCell(sheet)
            total != null
            total.cellFormula == "SUM(B1:B1)"
    }

    def "a formula filled right in Excel gets its own text in every cell of every band row"() {
        given: "Excel stores the formula filled over D1:F1 as a shared one, only D1 keeps the text"
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${a}')
                cell(sheet, 0, 1, '${b}')
                cell(sheet, 0, 2, '${c}')
                sharedFormula(sheet, "D1:F1", "A1*2")
                defineBand(wb, "Data", 0, 0, 0, 5)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [a: 1, b: 2, c: 3])
            addBand(root, "Data", [a: 10, b: 20, c: 30])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            ownFormula(sheet, 0, 3) == "A1*2"
            ownFormula(sheet, 0, 5) == "C1*2"
            ownFormula(sheet, 1, 3) == "A2*2"
            ownFormula(sheet, 1, 5) == "C2*2"
    }

    def "a formula filled right in a totals band grows over the rendered rows in every column"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${a}')
                cell(sheet, 0, 1, '${b}')
                sharedFormula(sheet, "A2:B2", "SUM(A1:A1)")
                defineBand(wb, "Data", 0, 0, 0, 1)
                defineBand(wb, "Total", 1, 0, 1, 1)
            }
            def root = rootBand("Data", "Total")
            addBand(root, "Data", [a: 1, b: 2])
            addBand(root, "Data", [a: 10, b: 20])
            addBand(root, "Data", [a: 100, b: 200])
            addBand(root, "Total", [:])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            ownFormula(sheet, 3, 0) == "SUM(A1:A3)"
            ownFormula(sheet, 3, 1) == "SUM(B1:B3)"
    }

    def "a shared formula that cannot be parsed is kept as authored"() {
        given: "a hand-made template with an alias in a shared formula"
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${a}')
                sharedFormula(sheet, "B1:C1", '${k}*A1')
                defineBand(wb, "Data", 0, 0, 0, 2)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [a: 1, k: 2])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "the second cell still takes its formula from the first one"
            formula(sheet, 0, 2) == "2*B1"
    }

    /**
     * Writes a formula filled over {@code ref} the way Excel stores it: the first cell holds the text and the
     * group's range, the other cells only the group index.
     */
    private static void sharedFormula(Sheet sheet, String ref, String formula) {
        def range = CellRangeAddress.valueOf(ref)
        for (int row = range.firstRow; row <= range.lastRow; row++) {
            for (int col = range.firstColumn; col <= range.lastColumn; col++) {
                def r = sheet.getRow(row) ?: sheet.createRow(row)
                def f = ((XSSFCell) r.createCell(col)).getCTCell().addNewF()
                f.setT(STCellFormulaType.SHARED)
                f.setSi(0)
                if (row == range.firstRow && col == range.firstColumn) {
                    f.setRef(ref)
                    f.setStringValue(formula)
                }
            }
        }
    }

    /** The formula text written in the cell itself, failing if the cell takes it from a shared group. */
    private String ownFormula(Sheet sheet, int row, int col) {
        def f = ((XSSFCell) requireCell(sheet, row, col)).getCTCell().getF()
        assert f != null && f.getT() != STCellFormulaType.SHARED
        return f.getStringValue()
    }
}
