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
package io.jmix.reports.yarg.formatters.factory;

import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportOutputType;
import io.jmix.reports.yarg.structure.ReportTemplate;

import java.io.OutputStream;

/**
 * The object is single input parameter for formatter constructor
 * If you need you formatter be created by factory - it should accept this object as constructor parameter
 */
public class FormatterFactoryInput {

    protected final String templateExtension;
    protected final BandData rootBand;
    protected final ReportTemplate reportTemplate;
    protected final OutputStream outputStream;

    protected ReportOutputType outputType = null;

    public FormatterFactoryInput(String templateExtension, BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        if (templateExtension == null) {
            throw new NullPointerException("templateExtension can not be null");
        }

        if (rootBand == null) {
            throw new NullPointerException("rootBand can not be null");
        }

        this.templateExtension = templateExtension;
        this.rootBand = rootBand;
        this.reportTemplate = reportTemplate;
        this.outputStream = outputStream;
    }

    public FormatterFactoryInput(String templateExtension, BandData rootBand, ReportTemplate reportTemplate, ReportOutputType outputType, OutputStream outputStream) {
        this(templateExtension, rootBand, reportTemplate, outputStream);
        this.outputType = outputType;
    }

    public String getTemplateExtension() {
        return templateExtension;
    }

    public BandData getRootBand() {
        return rootBand;
    }

    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public ReportOutputType getOutputType() {
        return outputType;
    }
}