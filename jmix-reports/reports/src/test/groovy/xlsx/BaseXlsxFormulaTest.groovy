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
 * Engine-independent contract for formula post-processing. Run by both engines —
 * {@link XlsxFormulaTest} and {@code StreamingXlsxFormulaTest} — so the shared rules are compared rather than
 * described. The streaming spec adds the many cases only it can express (forward references rejected,
 * absolute anchors, sheet-qualified references left alone).
 *
 * <p>Only the in-row shift is shared, because <b>aggregate formulas have no placement that works on both
 * engines</b>: the in-memory engine grows an aggregate that sits inside a totals band and emits nothing for a
 * row outside every band, whereas the streaming engine grows an aggregate on a static row and leaves one
 * inside a band pointing at its template range. Each placement is therefore asserted in the spec of the
 * engine that supports it, and the streaming spec pins the silent-wrong-total case.
 */
abstract class BaseXlsxFormulaTest extends BaseXlsxRenderTest {

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
