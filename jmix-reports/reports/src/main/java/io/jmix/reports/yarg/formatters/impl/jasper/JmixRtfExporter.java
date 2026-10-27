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

package io.jmix.reports.yarg.formatters.impl.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.base.JRBasePrintHyperlink;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.util.JRTextAttribute;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.AttributedCharacterIterator;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

/**
 * RTF exporter that also exports hyperlinks of styled text runs, e.g. {@code <a>} tags of a text field with
 * HTML markup. {@link JRRtfExporter} exports only the hyperlink of the whole text element.
 * <p>
 * As in the DOCX exporter, a run hyperlink is not exported inside an exported element hyperlink, since a hyperlink
 * cannot contain another one. The target of a run hyperlink comes from the text and may hold report data, so it is
 * escaped before it is written into the RTF field instruction.
 */
@NullMarked
public class JmixRtfExporter extends JRRtfExporter {

    protected static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    protected boolean elementHyperlinkStarted;

    @Override
    protected boolean exportHyperlink(JRPrintHyperlink link) throws IOException {
        boolean started = super.exportHyperlink(link);
        // Called for a text element before its runs are exported, so the flag is reset for every element.
        if (link instanceof JRPrintText) {
            elementHyperlinkStarted = started;
        }

        return started;
    }

    @Override
    protected void exportStyledTextRun(Map<AttributedCharacterIterator.Attribute, Object> styledTextAttributes,
                                       String text, @Nullable Locale locale, Color backcolor)
            throws IOException, JRException {
        JRPrintHyperlink runHyperlink = (JRPrintHyperlink) styledTextAttributes.get(JRTextAttribute.HYPERLINK);
        boolean startedRunHyperlink = !elementHyperlinkStarted
                && runHyperlink != null
                && super.exportHyperlink(withEscapedReference(runHyperlink));

        super.exportStyledTextRun(styledTextAttributes, text, locale, backcolor);

        endHyperlink(startedRunHyperlink);
    }

    /**
     * Returns a copy of the hyperlink whose reference is escaped by {@link #escapeReference(String)}. Styled text
     * sets no anchor or page for a run hyperlink, so only the reference can hold text.
     */
    protected JRPrintHyperlink withEscapedReference(JRPrintHyperlink hyperlink) {
        JRBasePrintHyperlink escaped = new JRBasePrintHyperlink();
        escaped.setLinkType(hyperlink.getLinkType());
        escaped.setLinkTarget(hyperlink.getLinkTarget());
        escaped.setHyperlinkReference(escapeReference(hyperlink.getHyperlinkReference()));
        escaped.setHyperlinkAnchor(hyperlink.getHyperlinkAnchor());
        escaped.setHyperlinkPage(hyperlink.getHyperlinkPage());
        escaped.setHyperlinkTooltip(hyperlink.getHyperlinkTooltip());
        escaped.setHyperlinkParameters(hyperlink.getHyperlinkParameters());
        return escaped;
    }

    /**
     * Escapes a hyperlink reference for the quoted argument of an RTF field instruction. RTF control characters are
     * escaped as in text. The quote and non-ASCII characters are percent-encoded: the quote would end the argument,
     * and RTF readers decode raw non-ASCII bytes in an ANSI code page.
     */
    protected @Nullable String escapeReference(@Nullable String reference) {
        if (reference == null) {
            return null;
        }

        StringBuilder escaped = new StringBuilder(reference.length());
        for (int codePoint : reference.codePoints().toArray()) {
            if (codePoint == '\\' || codePoint == '{' || codePoint == '}') {
                escaped.append('\\').appendCodePoint(codePoint);
            } else if (codePoint == '"' || codePoint > '~') {
                for (byte b : Character.toString(codePoint).getBytes(StandardCharsets.UTF_8)) {
                    escaped.append('%').append(HEX_FORMAT.toHexDigits(b));
                }
            } else {
                escaped.appendCodePoint(codePoint);
            }
        }
        return escaped.toString();
    }
}
