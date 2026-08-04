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
import io.jmix.reports.yarg.formatters.factory.inline.DefaultInlinersProvider
import io.jmix.reports.yarg.formatters.impl.StreamingXlsxFormatter
import xlsx.StreamingBaseXlsxRenderTest

/**
 * Content inliners ({@code ${bitmap:WxH}}, {@code ${image:WxH}}) are not supported by the streaming
 * engine: the formatter must reject them with a clear error instead of silently writing the value's
 * {@code toString()} into the cell.
 */
class StreamingXlsxInlinerTest extends StreamingBaseXlsxRenderTest {

    @Override
    protected ReportFormatter createFormatter(FormatterFactoryInput input) {
        def formatter = new StreamingXlsxFormatter(input)
        formatter.setContentInliners(new DefaultInlinersProvider().getContentInliners().toList())
        return formatter
    }

    def "bitmap inliner format is rejected with a clear error instead of writing toString garbage"() {
        given:
        def template = buildTemplate { wb ->
            cell(sheet(wb), 0, 0, '${image}')
            defineBand(wb, "Data", 0, 0, 0, 0)
        }
        def root = rootBand("Data")
        withFieldFormats(root, fieldFormat("Data.image", '${bitmap:100x100}'))
        addBand(root, "Data", [image: new byte[16]])

        when:
        render(template, root)

        then:
        def e = thrown(ReportFormattingException)
        e.message.toLowerCase().contains("inliner")
    }
}
