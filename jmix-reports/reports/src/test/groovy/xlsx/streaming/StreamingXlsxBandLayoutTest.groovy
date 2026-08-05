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
import io.jmix.reports.yarg.formatters.ReportFormatter
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.StreamingXlsxFormatter
import xlsx.BaseXlsxBandLayoutTest

/**
 * Band layout of the streaming XLSX formatter.
 *
 * <p>The layout rules themselves are the shared {@link BaseXlsxBandLayoutTest} contract, re-run here through
 * {@link StreamingXlsxFormatter}: the streaming engine must lay bands out exactly like the in-memory one.
 * What is specific to this engine are the layouts it must REJECT — forward-only writing emits each template
 * row once, so a layout that would need a row revisited is refused up front instead of silently dropping or
 * reordering data.
 */
class StreamingXlsxBandLayoutTest extends BaseXlsxBandLayoutTest {

    @Override
    protected ReportFormatter createFormatter(FormatterFactoryInput input) {
        return new StreamingXlsxFormatter(input)
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
}
