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

package io.jmix.reports.runner;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class stores the following information required for report running:
 * <ul>
 *     <li>{@link Report} entity</li>
 *     <li>{@link ReportTemplate} entity: if not specified the default template is used</li>
 *     <li>Input parameters</li>
 *     <li>Type of output document</li>
 *     <li>Output name pattern</li>
 * </ul>
 * <br>
 * The instance of the class may be created using the
 * constructor or using the {@link ReportRunner} bean.
 *
 * <br>
 * Creation examples:
 * <pre>
 * ReportRunContext context = new ReportRunContext(report)
 *                 .addParam("customer", customer)
 *                 .addParam("minOrdersDate", date)
 *                 .setOutputNamePattern("Orders");
 *
 * ReportRunContext context = new ReportRunContext(report)
 *                 .setReportTemplate(template)
 *                 .setOutputType(ReportOutputType.PDF)
 *                 .setParams(paramsMap);
 * </pre>
 * @see FluentReportRunner
 * @see ReportRunner
 */
public class ReportRunContext {
    protected Report report;
    protected ReportTemplate reportTemplate;
    protected ReportOutputType outputType;
    protected Map<String, Object> params = new HashMap<>();
    protected String outputNamePattern;

    public Report getReport() {
        return report;
    }

    public ReportRunContext() {
    }

    public ReportRunContext(Report report) {
        this.report = report;
    }

    public ReportRunContext setReport(Report report) {
        this.report = report;
        return this;
    }

    @Nullable
    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public ReportRunContext setReportTemplate(@Nullable ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
        return this;
    }

    public ReportRunContext addParam(String alias, Object value) {
        this.params.put(alias, value);
        return this;
    }

    @Nullable
    public ReportOutputType getOutputType() {
        return outputType;
    }

    public ReportRunContext setOutputType(@Nullable ReportOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public ReportRunContext setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    @Nullable
    public String getOutputNamePattern() {
        return outputNamePattern;
    }

    public ReportRunContext setOutputNamePattern(@Nullable String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
        return this;
    }

}
