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

import org.apache.poi.ss.util.CellRangeAddress
import xlsx.StreamingBaseXlsxRenderTest

/**
 * Defined names in the template that are NOT report bands (Excel service names such as Print Area /
 * AutoFilter / Print Titles, or user named constants) must not be treated as bands: they must neither
 * suppress rows of the template walk nor crash reference parsing.
 */
class StreamingXlsxDefinedNamesTest extends StreamingBaseXlsxRenderTest {

    def "print area over the band does not suppress rendering"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${value}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            wb.setPrintArea(0, 0, 1, 0, 5)
        }
        def root = rootBand("Data")
        (1..3).each { addBand(root, "Data", [value: "r" + it]) }

        when:
        def result = renderAndReadFirstSheet(template, root)

        then:
        stringValue(result, 0, 0) == "r1"
        stringValue(result, 2, 0) == "r3"
    }

    def "auto filter over the band does not suppress rendering"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, "Header")
            cell(s, 1, 0, '${value}')
            defineBand(wb, "Data", 1, 0, 1, 0)
            s.setAutoFilter(CellRangeAddress.valueOf("A1:A4"))
        }
        def root = rootBand("Data")
        (1..2).each { addBand(root, "Data", [value: "r" + it]) }

        when:
        def result = renderAndReadFirstSheet(template, root)

        then:
        stringValue(result, 0, 0) == "Header"
        stringValue(result, 1, 0) == "r1"
        stringValue(result, 2, 0) == "r2"
    }

    def "multi-area print titles do not crash the render"() {
        given:
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${value}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            s.setRepeatingRows(CellRangeAddress.valueOf("1:1"))
            s.setRepeatingColumns(CellRangeAddress.valueOf("A:A"))
        }
        def root = rootBand("Data")
        addBand(root, "Data", [value: "x"])

        when:
        def result = renderAndReadFirstSheet(template, root)

        then:
        stringValue(result, 0, 0) == "x"
    }

    def "user defined name that is not a band is ignored"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${value}')
            defineBand(wb, "Data", 0, 0, 0, 0)
            def constant = wb.createName()
            constant.setNameName("NotABand")
            constant.setRefersToFormula('Sheet1!$A$1:$B$3')
        }
        def root = rootBand("Data")
        (1..2).each { addBand(root, "Data", [value: "r" + it]) }

        when:
        def result = renderAndReadFirstSheet(template, root)

        then:
        stringValue(result, 0, 0) == "r1"
        stringValue(result, 1, 0) == "r2"
    }
}
