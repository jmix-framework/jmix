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
import org.apache.poi.ss.util.CellRangeAddress

/**
 * Verifies structural aspects of the produced workbook: XML-sensitive characters are escaped, merged regions
 * inside a band are replicated and shifted, sheet names are substituted, and gaps between filled cells are
 * preserved.
 */
class XlsxStructureTest extends BaseXlsxRenderTest {

    def "special XML characters in values are preserved"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [value: 'a & b < c > d "e"'])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == 'a & b < c > d "e"'
    }

    def "a merged region inside a band is replicated for every row"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${a}')
                cell(sheet, 0, 1, '${b}')
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1))
                defineBand(wb, "Data", 0, 0, 0, 1)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [a: "r1a", b: "r1b"])
            addBand(root, "Data", [a: "r2a", b: "r2b"])

        when:
            def result = render(template, root)
            def sheet = read(result).getSheetAt(0)

        then: "two merged regions, one per rendered row, each spanning columns A:B"
            // CellRangeAddress(firstRow, lastRow, firstCol, lastCol)
            def regions = sheet.getMergedRegions()
            regions.size() == 2
            regions.contains(new CellRangeAddress(0, 0, 0, 1))
            regions.contains(new CellRangeAddress(1, 1, 0, 1))
    }

    def "a placeholder in the sheet name is substituted from the root band"() {
        given:
            def sheetName = '${Root.title}'
            def template = buildTemplate { wb ->
                def sheet = sheet(wb, sheetName)
                cell(sheet, 0, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 0, sheetName)
            }
            def root = rootBand("Data")
            root.setData([title: "Sales"])
            addBand(root, "Data", [value: "x"])

        when:
            def workbook = read(render(template, root))

        then:
            workbook.getSheetName(0) == "Sales"
    }

    def "gaps between filled cells are preserved as empty cells"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${left}')
                // column B (index 1) is intentionally left without a cell
                cell(sheet, 0, 2, '${right}')
                defineBand(wb, "Data", 0, 0, 0, 2)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [left: "L", right: "R"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == "L"
            stringValue(sheet, 0, 2) == "R"
        and: "the gap cell is empty, not a leftover placeholder"
            def middle = cellOrNull(sheet, 0, 1)
            middle == null || middle.cellType == CellType.BLANK ||
                    (middle.cellType == CellType.STRING && middle.stringCellValue.isEmpty())
    }

    def "the workbook is well-formed and reopenable after rendering"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [value: "ok"])

        when:
            def bytes = render(template, root)

        then: "no exception when reopening and the single data row is present"
            def workbook = read(bytes)
            workbook.getNumberOfSheets() == 1
            workbook.getSheetAt(0).getRow(0).getCell(0).stringCellValue == "ok"
    }
}
