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

import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.BandOrientation

/**
 * Verifies how bands are laid out in the resulting sheet: horizontal bands grow down, vertical bands grow
 * right, nested bands are placed under their parent, multi-row templates are repeated as a block, and empty
 * data produces no rows.
 */
class XlsxBandLayoutTest extends BaseXlsxRenderTest {

    def "a horizontal band is repeated downwards for every data row"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${name}')
                cell(sheet, 0, 1, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 1)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [name: "a", value: 1])
            addBand(root, "Data", [name: "b", value: 2])
            addBand(root, "Data", [name: "c", value: 3])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == "a"
            numericValue(sheet, 0, 1) == 1.0d
            stringValue(sheet, 1, 0) == "b"
            numericValue(sheet, 1, 1) == 2.0d
            stringValue(sheet, 2, 0) == "c"
            numericValue(sheet, 2, 1) == 3.0d
    }

    def "a multi-row band template is repeated as a block"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${label}')
                cell(sheet, 1, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 1, 0)
            }
            def root = rootBand("Data")
            addBand(root, "Data", [label: "L1", value: "V1"])
            addBand(root, "Data", [label: "L2", value: "V2"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == "L1"
            stringValue(sheet, 1, 0) == "V1"
            stringValue(sheet, 2, 0) == "L2"
            stringValue(sheet, 3, 0) == "V2"
    }

    def "nested horizontal bands are placed under their parent in order"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${userName}')
                cell(sheet, 1, 0, '${gameName}')
                defineBand(wb, "Users", 0, 0, 0, 0)
                defineBand(wb, "Games", 1, 0, 1, 0)
            }
            def root = rootBand("Users")
            def alice = addBand(root, "Users", [userName: "Alice"])
            addBand(alice, "Games", [gameName: "Chess"])
            addBand(alice, "Games", [gameName: "Go"])
            def bob = addBand(root, "Users", [userName: "Bob"])
            addBand(bob, "Games", [gameName: "Tetris"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "each user is followed by its own games before the next user"
            stringValue(sheet, 0, 0) == "Alice"
            stringValue(sheet, 1, 0) == "Chess"
            stringValue(sheet, 2, 0) == "Go"
            stringValue(sheet, 3, 0) == "Bob"
            stringValue(sheet, 4, 0) == "Tetris"
    }

    def "sibling first-level bands are rendered in order"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${title}')
                cell(sheet, 1, 0, '${item}')
                defineBand(wb, "Header", 0, 0, 0, 0)
                defineBand(wb, "Data", 1, 0, 1, 0)
            }
            def root = rootBand("Header", "Data")
            addBand(root, "Header", [title: "My Report"])
            addBand(root, "Data", [item: "row1"])
            addBand(root, "Data", [item: "row2"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            stringValue(sheet, 0, 0) == "My Report"
            stringValue(sheet, 1, 0) == "row1"
            stringValue(sheet, 2, 0) == "row2"
    }

    def "a vertical band is repeated rightwards for every data row"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            verticalBand(root, "Data", [value: "x"])
            verticalBand(root, "Data", [value: "y"])
            verticalBand(root, "Data", [value: "z"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "values go into consecutive columns of the same row"
            stringValue(sheet, 0, 0) == "x"
            stringValue(sheet, 0, 1) == "y"
            stringValue(sheet, 0, 2) == "z"
    }

    def "a band with no data produces no output rows"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            // no data bands added

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then:
            cellOrNull(sheet, 0, 0) == null
    }

    protected BandData verticalBand(BandData parent, String name, Map<String, Object> data) {
        def band = new BandData(name, parent, BandOrientation.VERTICAL)
        band.setData(data)
        parent.addChild(band)
        return band
    }
}
