/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.entity;

import java.util.HashMap;
import java.util.Map;

public class ReportRunParams {
    protected Report report;
    protected ReportTemplate reportTemplate;
    protected ReportOutputType outputType;
    protected Map<String, Object> params = new HashMap<>();
    protected String outputNamePattern;

    public Report getReport() {
        return report;
    }

    public ReportRunParams setReport(Report report) {
        this.report = report;
        return this;
    }

    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public ReportRunParams setReportTemplate(ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
        return this;
    }

    public ReportOutputType getOutputType() {
        return outputType;
    }

    public ReportRunParams setOutputType(ReportOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public ReportRunParams setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public String getOutputNamePattern() {
        return outputNamePattern;
    }

    public ReportRunParams setOutputNamePattern(String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
        return this;
    }
}
