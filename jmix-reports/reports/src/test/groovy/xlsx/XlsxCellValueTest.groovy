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

/**
 * Runs the {@link BaseXlsxCellValueTest} cell-value contract against the in-memory {@code XlsxFormatter}
 * (the default {@link BaseXlsxRenderTest#createFormatter}). The scenarios live in the base spec because the
 * streaming engine must satisfy the same ones — see {@code StreamingXlsxCellValueTest}.
 *
 * <p>Add a scenario here only when it is specific to the in-memory engine; anything both engines must do
 * belongs in the base spec.
 */
class XlsxCellValueTest extends BaseXlsxCellValueTest {

    /**
     * Pins a structural difference between the engines, so it cannot silently change: this engine builds the
     * result from band ranges only, so a template row that no named range covers is not emitted at all and
     * the band moves up to take its place. The streaming engine copies such rows through (see
     * {@code StreamingXlsxCellValueTest} and the streaming structure spec), which means the same template can
     * render differently on the two engines — worth knowing when migrating a report to streaming.
     */
    def "a template row outside every band range is not emitted by this engine"() {
        given: "row 0 is covered by no named range and holds a numeric and a boolean literal"
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                numericCell(sheet, 0, 0, 2026d)
                booleanCell(sheet, 0, 1, false)
                cell(sheet, 1, 0, '${name}')
                defineBand(wb, "Data", 1, 0, 1, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [name: "a"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "the band renders at the very top — the out-of-band row above it is gone, literals and all"
            stringValue(sheet, 0, 0) == "a"
            cellOrNull(sheet, 0, 1) == null

        and: "and nothing else is emitted"
            sheet.getPhysicalNumberOfRows() == 1
    }
}
