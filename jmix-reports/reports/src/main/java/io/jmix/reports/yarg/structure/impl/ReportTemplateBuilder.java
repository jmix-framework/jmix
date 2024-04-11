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
package io.jmix.reports.yarg.structure.impl;

import com.google.common.base.Preconditions;
import io.jmix.reports.yarg.formatters.CustomReport;
import io.jmix.reports.yarg.structure.ReportOutputType;
import io.jmix.reports.yarg.structure.ReportTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ReportTemplateBuilder {
    private ReportTemplateImpl reportTemplate;


    public ReportTemplateBuilder() {
        reportTemplate = new ReportTemplateImpl();
    }

    public ReportTemplateBuilder code(String code) {
        Preconditions.checkNotNull(code, "\"code\" parameter can not be null");
        reportTemplate.code = code;
        return this;
    }

    public ReportTemplateBuilder documentName(String documentName) {
        Preconditions.checkNotNull(documentName, "\"documentName\" parameter can not be null");
        reportTemplate.documentName = documentName;
        return this;
    }

    public ReportTemplateBuilder documentPath(String documentPath) {
        reportTemplate.documentPath = documentPath;
        return this;
    }

    public ReportTemplateBuilder readFileFromPath() throws IOException {
        Preconditions.checkNotNull(reportTemplate.documentPath, "\"documentPath\" parameter is null. Can not load data from null path");
        reportTemplate.documentContent = FileUtils.readFileToByteArray(new File(reportTemplate.documentPath));
        return this;
    }

    public ReportTemplateBuilder documentContent(byte[] documentContent) {
        Preconditions.checkNotNull(documentContent, "\"documentContent\" parameter can not be null");
        reportTemplate.documentContent = documentContent;
        return this;
    }

    public ReportTemplateBuilder documentContent(InputStream documentContent) throws IOException {
        Preconditions.checkNotNull(documentContent, "\"documentContent\" parameter can not be null");
        reportTemplate.documentContent = IOUtils.toByteArray(documentContent);
        return this;
    }

    public ReportTemplateBuilder outputType(ReportOutputType outputType) {
        Preconditions.checkNotNull(outputType, "\"outputType\" parameter can not be null");
        reportTemplate.reportOutputType = outputType;
        return this;
    }

    public ReportTemplateBuilder outputNamePattern(String outputNamePattern) {
        reportTemplate.outputNamePattern = outputNamePattern;
        return this;
    }

    public ReportTemplateBuilder custom(CustomReport customReport) {
        Preconditions.checkNotNull(customReport, "\"customReport\" parameter can not be null");
        reportTemplate.custom = true;
        reportTemplate.customReport = customReport;
        return this;
    }

    public ReportTemplate build() {
        reportTemplate.validate();
        ReportTemplateImpl result = reportTemplate;
        reportTemplate = new ReportTemplateImpl();
        return result;
    }
}