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
 * Band layout of the streaming XLSX formatter: horizontal bands repeat top-to-bottom,
 * nested bands render under their parent instance, empty bands produce no rows.
 */
class StreamingXlsxBandLayoutTest extends StreamingBaseXlsxRenderTest {

    def "horizontal band repeats one row per instance, top to bottom"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [n: "A"])
        addBand(root, "Data", [n: "B"])
        addBand(root, "Data", [n: "C"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "A"
        stringValue(sheet, 1, 0) == "B"
        stringValue(sheet, 2, 0) == "C"
    }

    def "multi-row band block repeats as a whole"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            cell(sheet, 1, 0, 'sub ${n}')
            defineBand(wb, "Data", 0, 0, 1, 0)
        }
        def root = rootBand("Data")
        addBand(root, "Data", [n: "A"])
        addBand(root, "Data", [n: "B"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "A"
        stringValue(sheet, 1, 0) == "sub A"
        stringValue(sheet, 2, 0) == "B"
        stringValue(sheet, 3, 0) == "sub B"
    }

    def "nested band renders under its parent instance in order"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${userName}')
            defineBand(wb, "Users", 0, 0, 0, 0)
            cell(sheet, 1, 0, '${gameName}')
            defineBand(wb, "Games", 1, 0, 1, 0)
        }
        def root = rootBand("Users")
        def alice = addBand(root, "Users", [userName: "Alice"])
        addBand(alice, "Games", [gameName: "Chess"])
        addBand(alice, "Games", [gameName: "Go"])
        def bob = addBand(root, "Users", [userName: "Bob"])
        addBand(bob, "Games", [gameName: "Poker"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "Alice"
        stringValue(sheet, 1, 0) == "Chess"
        stringValue(sheet, 2, 0) == "Go"
        stringValue(sheet, 3, 0) == "Bob"
        stringValue(sheet, 4, 0) == "Poker"
    }

    def "sibling first-level bands render in template order"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${a}')
            defineBand(wb, "First", 0, 0, 0, 0)
            cell(sheet, 1, 0, '${b}')
            defineBand(wb, "Second", 1, 0, 1, 0)
        }
        def root = rootBand("First", "Second")
        addBand(root, "First", [a: "one"])
        addBand(root, "Second", [b: "two"])

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then:
        stringValue(sheet, 0, 0) == "one"
        stringValue(sheet, 1, 0) == "two"
    }

    def "a sibling band between a parent and its child is rejected instead of silently reordering"() {
        given: "template order Parent (row0), Sibling (row1, first-level), Child (row2, child of Parent)"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${a}'); defineBand(wb, "Parent", 0, 0, 0, 0)
            cell(s, 1, 0, '${b}'); defineBand(wb, "Sibling", 1, 0, 1, 0)
            cell(s, 2, 0, '${c}'); defineBand(wb, "Child", 2, 0, 2, 0)
        }
        def root = rootBand("Parent", "Sibling")
        def parent = addBand(root, "Parent", [a: "a1"])
        addBand(parent, "Child", [c: "c1"])
        addBand(root, "Sibling", [b: "b1"])

        when: "Child belongs to Parent but is laid out below the Sibling band; rendering it under Parent would reorder Sibling"
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("between")
    }

    def "two bands sharing template rows (side by side) are rejected"() {
        given: "two first-level bands laid out on the same row in different columns"
        def template = buildTemplate { wb ->
            def s = sheet(wb)
            cell(s, 0, 0, '${l}'); defineBand(wb, "Left", 0, 0, 0, 0)
            cell(s, 0, 2, '${r}'); defineBand(wb, "Right", 0, 2, 0, 2)
        }
        def root = rootBand("Left", "Right")
        addBand(root, "Left", [l: "L"])
        addBand(root, "Right", [r: "R"])

        when: "forward-only writing emits each template row once, so a co-located band would be lost"
        render(template, root)

        then: "rejected up front instead of silently dropping one band's data"
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("overlap")
    }

    def "empty band yields no rows"() {
        given:
        def template = buildTemplate { wb ->
            def sheet = sheet(wb)
            cell(sheet, 0, 0, '${n}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")

        when:
        def sheet = renderAndReadFirstSheet(template, root)

        then: "no rows are created at all, not merely an empty cell at (0,0)"
        sheet.getPhysicalNumberOfRows() == 0
    }
}
