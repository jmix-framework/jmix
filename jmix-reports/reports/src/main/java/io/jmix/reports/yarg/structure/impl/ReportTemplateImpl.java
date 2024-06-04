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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ReportTemplateImpl implements ReportTemplate {
    protected String code = ReportTemplate.DEFAULT_TEMPLATE_CODE;
    protected String documentName;
    protected String documentPath;
    protected byte[] documentContent;
    protected ReportOutputType reportOutputType;
    protected String outputNamePattern;

    protected boolean groovy = false;

    protected CustomReport customReport;
    protected boolean custom = false;

    ReportTemplateImpl() {
    }

    public ReportTemplateImpl(String code, String documentName, String documentPath, InputStream documentContent, ReportOutputType reportOutputType) throws IOException {
        this.code = code;
        this.documentName = documentName;
        this.documentPath = documentPath;
        this.documentContent = IOUtils.toByteArray(documentContent);
        this.reportOutputType = reportOutputType;
    }

    public ReportTemplateImpl(String code, String documentName, String documentPath, ReportOutputType reportOutputType) throws IOException {
        this(code, documentName, documentPath, FileUtils.openInputStream(new File(documentPath)), reportOutputType);

        validate();
    }

    public ReportTemplateImpl(String code, String documentName, String documentPath, ReportOutputType reportOutputType, boolean groovy) throws IOException {
        this(code, documentName, documentPath, reportOutputType);
        this.groovy = groovy;
    }

    void validate() {
        if (!isCustom()) {
            Preconditions.checkNotNull(this.code, "\"code\" parameter can not be null");
            Preconditions.checkNotNull(this.documentName, "\"documentName\" parameter can not be null");
            Preconditions.checkNotNull(this.documentPath, "\"documentPath\" parameter can not be null");
            Preconditions.checkNotNull(this.reportOutputType, "\"reportOutputType\" parameter can not be null");
            Preconditions.checkNotNull(this.documentContent, "\"documentContent\" can not be null");
        }
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDocumentName() {
        return documentName;
    }

    @Override
    public InputStream getDocumentContent() {
        return new ByteArrayInputStream(documentContent);
    }

    @Override
    public ReportOutputType getOutputType() {
        return reportOutputType;
    }

    @Override
    public String getDocumentPath() {
        return documentPath;
    }

    @Override
    public String getOutputNamePattern() {
        return outputNamePattern;
    }

    @Override
    public boolean isGroovy() {
        return groovy;
    }

    @Override
    public boolean isCustom() {
        return custom;
    }

    @Override
    public CustomReport getCustomReport() {
        return customReport;
    }
}