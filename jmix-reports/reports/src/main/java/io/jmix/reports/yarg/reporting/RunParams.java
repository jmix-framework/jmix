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
package io.jmix.reports.yarg.reporting;

import io.jmix.reports.yarg.structure.Report;
import io.jmix.reports.yarg.structure.ReportOutputType;
import io.jmix.reports.yarg.structure.ReportTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes parameters necessary for report run
 */
public class RunParams {
    protected Report report;
    protected ReportTemplate reportTemplate;
    protected ReportOutputType outputType;
    protected Map<String, Object> params = new HashMap<String, Object>();
    protected String outputNamePattern;

    public RunParams(Report report) {
        this.report = report;
        this.reportTemplate = report.getReportTemplates().get(ReportTemplate.DEFAULT_TEMPLATE_CODE);
    }

    /**
     * Setup necessary template by string code. Throws validation exception if code is null or template not found
     * @param templateCode - string code of template
     */
    public RunParams templateCode(String templateCode) {
        if (templateCode == null) {
            throw new NullPointerException("\"templateCode\" parameter can not be null");
        }
        this.reportTemplate = report.getReportTemplates().get(templateCode);
        if (reportTemplate == null) {
            throw new NullPointerException(String.format("Report template not found for code [%s]", templateCode));
        }
        return this;
    }

    /**
     * Setup template. Throws validation exception if template is null
     */
    public RunParams template(ReportTemplate reportTemplate) {
        if (reportTemplate == null) {
            throw new NullPointerException("\"reportTemplate\" parameter can not be null");
        }
        this.reportTemplate = reportTemplate;
        return this;
    }

    /**
     * Adds parameters from map
     */
    public RunParams params(Map<String, Object> params) {
        if (params == null) {
            throw new NullPointerException("\"params\" parameter can not be null");
        }
        this.params.putAll(params);
        return this;
    }

    /**
     * Add single parameter
     */
    public RunParams param(String key, Object value) {
        params.put(key, value);
        return this;
    }

    /**
     * Add output type
     */
    public RunParams output(ReportOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    /**
     * Add output name pattern
     */
    public RunParams outputNamePattern(String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
        return this;
    }
}