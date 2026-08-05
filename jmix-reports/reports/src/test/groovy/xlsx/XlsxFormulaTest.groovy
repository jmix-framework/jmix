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
}
