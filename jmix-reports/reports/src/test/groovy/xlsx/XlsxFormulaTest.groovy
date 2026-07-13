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

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet

/**
 * Verifies the formula post-processing pass: in-row ("inner") formulas are shifted for every repetition of
 * their band, and aggregate ("outer") formulas referencing another band grow to cover all of its rows.
 */
class XlsxFormulaTest extends BaseXlsxRenderTest {

    def "an in-row formula is shifted down for every band row"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${qty}')
                cell(sheet, 0, 1, '${price}')
                formulaCell(sheet, 0, 2, "A1*B1")
                defineBand(wb, "Data", 0, 0, 0, 2)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [qty: 2, price: 10])
            addBand(root, "Data", [qty: 3, price: 20])
            addBand(root, "Data", [qty: 4, price: 30])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "each rendered row keeps its own references"
            formula(sheet, 0, 2) == "A1*B1"
            formula(sheet, 1, 2) == "A2*B2"
            formula(sheet, 2, 2) == "A3*B3"
        and: "input columns hold the data"
            numericValue(sheet, 0, 0) == 2.0d
            numericValue(sheet, 2, 1) == 30.0d
    }

    def "an aggregate formula grows to cover all rows of the referenced band"() {
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

    protected Cell findFormulaCell(Sheet sheet) {
        for (def row : sheet) {
            for (def c : row) {
                if (c.cellType == CellType.FORMULA) {
                    return c
                }
            }
        }
        return null
    }
}
