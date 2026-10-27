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

package jasper

import io.jmix.reports.entity.ReportTemplate
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.JasperFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import spock.lang.Specification

import java.nio.charset.StandardCharsets

/**
 * {@code <a>} tags inside text fields with HTML markup of a JRXML template rendered to DOC (RTF): each becomes an RTF
 * {@code HYPERLINK} field around its text, unless the text field itself is a hyperlink.
 */
class JasperDocHyperlinkTest extends Specification {

    def "exports each <a> tag of HTML markup as a hyperlink field around its text"() {
        when:
        def rtf = renderDoc()

        then: "the text field that follows a hyperlinked one keeps both of its links"
        def fields = hyperlinkFields(rtf)
        fields['https://www.jmix.io']?.endsWith('Jmix\\plain')
        fields['https://docs.jmix.io']?.endsWith('Docs\\plain')
        rtf.count('HYPERLINK "https://www.jmix.io"') == 1
        rtf.count('HYPERLINK "https://docs.jmix.io"') == 1
    }

    def "does not export an <a> tag inside a text field that is a hyperlink itself"() {
        when:
        def rtf = renderDoc()

        then:
        hyperlinkFields(rtf)['https://www.jmix.io/element']?.endsWith('Run\\plain')
        !rtf.contains('https://www.jmix.io/run')
    }

    def "escapes the target of an <a> tag for the RTF field instruction"() {
        when:
        def rtf = renderDoc()

        then: "RTF control characters are escaped, while the quote and non-ASCII characters are percent-encoded"
        isWellFormed(rtf)
        def targets = hyperlinkFields(rtf).keySet()
        targets.contains($/https://example.com/?q=\{\}\}%22\\e/$)
        targets.contains('https://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0')
    }

    /**
     * Maps the target of every RTF {@code HYPERLINK} field that contains no nested group to the RTF of its result.
     */
    private static Map<String, String> hyperlinkFields(String rtf) {
        Map<String, String> fields = [:]
        def matcher = rtf =~ /\{\\field\{\\\*\\fldinst HYPERLINK "((?:\\.|[^"\\])*)"}\{\\fldrslt ([^{}]*)}}/
        matcher.each { String match, String target, String result -> fields[target] = result }
        return fields
    }

    /**
     * Whether the RTF groups are balanced, so that no content falls out of the document group.
     */
    private static boolean isWellFormed(String rtf) {
        int depth = 0
        for (int i = 0; i < rtf.length(); i++) {
            char ch = rtf.charAt(i)
            if (ch == '\\' as char) {
                // Skips an escaped character or the first letter of a control word.
                i++
            } else if (ch == '{' as char) {
                depth++
            } else if (ch == '}' as char && --depth < 0) {
                return false
            }
        }
        return depth == 0
    }

    private static String renderDoc() {
        def template = new ReportTemplate()
        template.setName("html-hyperlink.jrxml")
        template.setContent(JasperDocHyperlinkTest.getResourceAsStream("html-hyperlink.jrxml").bytes)

        def rootBand = new BandData(BandData.ROOT_BAND_NAME)
        def output = new ByteArrayOutputStream()
        new JasperFormatter(new FormatterFactoryInput("jrxml", rootBand, template, ReportOutputType.doc, output))
                .renderDocument()
        return output.toString(StandardCharsets.UTF_8)
    }
}
