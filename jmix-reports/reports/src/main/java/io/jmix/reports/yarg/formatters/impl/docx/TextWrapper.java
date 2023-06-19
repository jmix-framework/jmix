/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.impl.docx;

import io.jmix.reports.yarg.formatters.impl.AbstractFormatter;
import io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate;
import io.jmix.reports.yarg.structure.BandData;
import org.docx4j.wml.Text;

import java.util.regex.Matcher;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class TextWrapper {
    private DocxFormatterDelegate docxFormatter;
    protected Text text;

    protected TextWrapper(DocxFormatterDelegate docxFormatter, Text text) {
        this.docxFormatter = docxFormatter;
        this.text = text;
    }

    //todo eude - merge with io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate.handleStringWithAliases()
    public void fillTextWithBandData() {
        Matcher matcher = AbstractFormatter.ALIAS_WITH_BAND_NAME_PATTERN.matcher(text.getValue());
        while (matcher.find()) {
            String alias = matcher.group(1);
            String stringFunction = matcher.group(2);

            AbstractFormatter.BandPathAndParameterName bandAndParameter = docxFormatter.separateBandNameAndParameterName(alias);

            if (isBlank(bandAndParameter.getBandPath()) || isBlank(bandAndParameter.getParameterName())) {
                if (alias.matches("[A-z0-9_\\.]+?")) {//skip aliases in tables
                    continue;
                }

                throw docxFormatter.wrapWithReportingException("Bad alias : " + text.getValue());
            }

            BandData band = docxFormatter.findBandByPath(bandAndParameter.getBandPath());

            if (band == null) {
                throw docxFormatter.wrapWithReportingException(String.format("No band for alias [%s] found", alias));
            }

            String fullParameterName = band.getName() + "." + bandAndParameter.getParameterName();
            Object parameterValue = band.getParameterValue(bandAndParameter.getParameterName());

            if (docxFormatter.tryToApplyInliners(fullParameterName, parameterValue, text)) return;

            text.setValue(docxFormatter.inlineParameterValue(text.getValue(), alias,
                    docxFormatter.formatValue(parameterValue, bandAndParameter.getParameterName(), fullParameterName, stringFunction)));
            text.setSpace("preserve");
        }
    }
}
