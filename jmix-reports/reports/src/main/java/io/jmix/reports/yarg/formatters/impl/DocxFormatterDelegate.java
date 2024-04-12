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

package io.jmix.reports.yarg.formatters.impl;

import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.structure.BandData;
import org.docx4j.TextUtils;
import org.docx4j.wml.Text;

import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;

import static org.apache.commons.lang3.StringUtils.isBlank;

//**********************************************************************************************************************
//**********************************************************************************************************************
//                                                        Attention
//        Please do not use the class, as it is part of private API and might be removed in later releases
//**********************************************************************************************************************
//**********************************************************************************************************************
public class DocxFormatterDelegate {
    protected DocxFormatter docxFormatter;

    public DocxFormatterDelegate(DocxFormatter docxFormatter) {
        this.docxFormatter = docxFormatter;
    }

    public String getElementText(Object element) {
        StringWriter w = new StringWriter();
        try {
            TextUtils.extractText(element, w);
        } catch (Exception e) {
            throw docxFormatter.wrapWithReportingException("An error occurred while rendering docx template.", e);
        }

        return w.toString();
    }

    public boolean containsJustOneAlias(String value) {
        return docxFormatter.containsJustOneAlias(value);
    }

    public List<String> getAllAliases(String value){
        return docxFormatter.getAllAliases(value);
    }

    public String unwrapParameterName(String nameWithAlias) {
        return docxFormatter.unwrapParameterName(nameWithAlias);
    }

    public String insertBandDataToString(BandData bandData, String resultStr) {
        return docxFormatter.insertBandDataToString(bandData, resultStr);
    }

    public ReportFormattingException wrapWithReportingException(String message) {
        return docxFormatter.wrapWithReportingException(message);
    }

    public String inlineParameterValue(String template, String parameterName, String value) {
        return docxFormatter.inlineParameterValue(template, parameterName, value);
    }

    public AbstractFormatter.BandPathAndParameterName separateBandNameAndParameterName(String alias) {
        return docxFormatter.separateBandNameAndParameterName(alias);
    }

    public BandData findBandByPath(String path) {
        return docxFormatter.findBandByPath(path);
    }

    public String formatValue(Object value, String parameterName, String fullParameterName, String stringFunction) {
        return docxFormatter.formatValue(value, parameterName, fullParameterName, stringFunction);
    }

    public boolean tryToApplyInliners(String fullParameterName, Object paramValue, Text text) {
        return docxFormatter.tryToApplyInliners(fullParameterName, paramValue, text);
    }

    public String handleStringWithAliases(String template) {
        String result = template;
        Matcher matcher = AbstractFormatter.ALIAS_WITH_BAND_NAME_PATTERN.matcher(result);
        while (matcher.find()) {
            String alias = matcher.group(1);
            String stringFunction = matcher.group(2);

            AbstractFormatter.BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);

            if (isBlank(bandAndParameter.getBandPath()) || isBlank(bandAndParameter.getParameterName())) {
                if (alias.matches("[A-z0-9_\\.]+?")) {//skip aliases in tables
                    continue;
                }

                throw wrapWithReportingException("Bad alias : " + alias);
            }

            BandData band = findBandByPath(bandAndParameter.getBandPath());

            if (band == null) {
                throw wrapWithReportingException(String.format("No band for alias [%s] found", alias));
            }

            String fullParameterName = band.getName() + "." + bandAndParameter.getParameterName();
            Object parameterValue = band.getParameterValue(bandAndParameter.getParameterName());

            result = inlineParameterValue(result, alias,
                    formatValue(parameterValue, bandAndParameter.getParameterName(), fullParameterName, stringFunction));
        }

        return result;
    }
}